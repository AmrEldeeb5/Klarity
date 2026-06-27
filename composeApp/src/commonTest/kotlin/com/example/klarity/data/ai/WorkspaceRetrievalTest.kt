package com.example.klarity.data.ai

import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.Task
import com.example.klarity.domain.models.TaskTag
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [WorkspaceRetrieval] — the keyword ranking that grounds the AI assistant. These
 * guard the core fix: a natural-language question must retrieve the relevant note even though the
 * question never appears verbatim (which the old `LIKE '%query%'` could never match).
 */
class WorkspaceRetrievalTest {

    private fun note(title: String, content: String, tags: List<String> = emptyList(), epoch: Long = 0) =
        Note(title = title, content = content, folderId = null, tags = tags, updatedAt = Instant.fromEpochSeconds(epoch))

    private fun task(title: String, description: String = "", tags: List<String> = emptyList(), epoch: Long = 0) =
        Task(
            id = title, title = title, description = description,
            tags = tags.map { TaskTag(it) },
            createdAt = Instant.fromEpochSeconds(epoch), updatedAt = Instant.fromEpochSeconds(epoch),
        )

    @Test
    fun `keywords strips stopwords and short tokens`() {
        assertEquals(listOf("plan", "q3", "launch"), WorkspaceRetrieval.keywords("What did I plan for the Q3 launch?"))
    }

    @Test
    fun `keywords falls back to raw tokens when query is all stopwords`() {
        // "what is it" is entirely stopwords; rather than return nothing, keep the >=2-char tokens.
        assertEquals(listOf("what", "is", "it"), WorkspaceRetrieval.keywords("what is it"))
    }

    @Test
    fun `rankNotes finds the relevant note that a verbatim match would miss`() {
        val notes = listOf(
            note("Q3 launch plan", "Ship the marketing site and email campaign."),
            note("Grocery list", "milk, eggs, bread"),
        )
        // The old repo search did LIKE '%What did I plan for the Q3 launch?%' → zero hits.
        val ranked = WorkspaceRetrieval.rankNotes(notes, "What did I plan for the Q3 launch?", limit = 6)
        assertEquals("Q3 launch plan", ranked.first().title)
        assertFalse(ranked.any { it.title == "Grocery list" })
    }

    @Test
    fun `rankNotes ranks a title hit above a body-only hit`() {
        val titleHit = note("Launch checklist", "unrelated body")
        val bodyHit = note("Misc", "remember the launch later")
        val ranked = WorkspaceRetrieval.rankNotes(listOf(bodyHit, titleHit), "launch", limit = 6)
        assertEquals("Launch checklist", ranked.first().title)
    }

    @Test
    fun `rankNotes drops notes with no keyword hit`() {
        val notes = listOf(note("Budget", "numbers"), note("Recipes", "pasta"))
        assertTrue(WorkspaceRetrieval.rankNotes(notes, "launch", limit = 6).isEmpty())
    }

    @Test
    fun `rankNotes with no usable keywords falls back to most recent`() {
        val older = note("Older", "a", epoch = 1)
        val newer = note("Newer", "b", epoch = 2)
        // "?!" yields no alphanumeric tokens at all, so keyword extraction is empty.
        val ranked = WorkspaceRetrieval.rankNotes(listOf(older, newer), "?!", limit = 1)
        assertEquals("Newer", ranked.first().title)
    }

    @Test
    fun `rankTasks matches description and tags`() {
        val tasks = listOf(
            task("Fix login bug", description = "users can't authenticate"),
            task("Write docs", tags = listOf("authenticate")),
            task("Buy coffee"),
        )
        val ranked = WorkspaceRetrieval.rankTasks(tasks, "authenticate users", limit = 6)
        assertEquals(setOf("Fix login bug", "Write docs"), ranked.map { it.title }.toSet())
    }

    @Test
    fun `snippet centres on the match and marks trimming with ellipses`() {
        val content = "INTRO " + "x".repeat(800) + " SECRET_TERM " + "y".repeat(800)
        val snip = WorkspaceRetrieval.snippet(content, listOf("secret_term"), budget = 200)
        assertTrue(snip.contains("SECRET_TERM"), "snippet should include the matched passage")
        assertTrue(snip.startsWith("…"), "leading content was trimmed, so it should start with an ellipsis")
    }

    @Test
    fun `snippet returns full text when within budget`() {
        assertEquals("short note", WorkspaceRetrieval.snippet("short note", listOf("note"), budget = 600))
    }
}
