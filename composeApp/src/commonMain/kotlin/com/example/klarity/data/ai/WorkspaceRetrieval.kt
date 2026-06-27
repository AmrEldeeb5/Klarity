package com.example.klarity.data.ai

import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.Task

/**
 * Keyword retrieval + ranking over the in-memory workspace, used to ground the AI assistant (Lou).
 *
 * The repository's `searchNotes`/`searchTasks` do a single SQL `LIKE '%query%'` on the *whole*
 * string — fine for the search bar (the user types a keyword), but useless for a natural-language
 * question like "what did I plan for the Q3 launch?", which never appears verbatim in any note.
 *
 * So for the assistant we tokenize the question into keywords, score every note/task by where its
 * keywords hit (title > tags > body), and return the best matches. This is the single biggest lever
 * on answer quality: even a weak model answers well when handed the right context, and the strongest
 * model fails when handed the wrong context.
 *
 * Pure and deterministic so it can be unit-tested without a database.
 */
object WorkspaceRetrieval {

    /** Field weights — a title hit is a far stronger relevance signal than a body hit. */
    private const val TITLE_WEIGHT = 5
    private const val TAG_WEIGHT = 3
    private const val BODY_WEIGHT = 2

    /**
     * Common English words that carry no retrieval signal. Dropping them keeps scoring focused on
     * the content words of a question ("Q3", "launch") instead of its scaffolding ("what", "did").
     */
    private val STOPWORDS = setOf(
        "the", "a", "an", "and", "or", "but", "if", "then", "so", "of", "for", "to", "in", "on",
        "at", "by", "with", "about", "as", "from", "into", "over", "is", "are", "was", "were", "be",
        "been", "being", "am", "do", "does", "did", "doing", "have", "has", "had", "can", "could",
        "would", "should", "will", "shall", "may", "might", "must", "i", "me", "my", "mine", "we",
        "our", "ours", "you", "your", "yours", "it", "its", "this", "that", "these", "those", "he",
        "she", "they", "them", "their", "what", "which", "who", "whom", "whose", "when", "where",
        "why", "how", "all", "any", "some", "no", "not", "there", "here", "please", "tell", "show",
        "give", "find", "get", "list", "let", "make", "want", "need", "know", "see", "up", "out",
    )

    /**
     * Splits a query into distinct, lowercased content keywords (stopwords and 1-char tokens removed).
     * Falls back to the longer raw tokens if stripping stopwords would leave nothing (e.g. a query
     * made entirely of common words), so retrieval still has something to match on.
     */
    fun keywords(query: String): List<String> {
        val tokens = query.lowercase()
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length >= 2 }
        val content = tokens.filter { it !in STOPWORDS }.distinct()
        return content.ifEmpty { tokens.distinct() }
    }

    /**
     * The notes most relevant to [query], best first, capped at [limit]. Notes with no keyword hit
     * are dropped. When the query has no usable keywords (all stopwords), falls back to the most
     * recently edited notes so the assistant still has ambient workspace context.
     */
    fun rankNotes(notes: List<Note>, query: String, limit: Int): List<Note> {
        val keys = keywords(query)
        if (keys.isEmpty()) return notes.sortedByDescending { it.updatedAt }.take(limit)
        return notes
            .map { it to scoreNote(it, keys) }
            .filter { it.second > 0 }
            // Score first; break ties by pinned, then recency — the most useful note wins.
            .sortedWith(compareByDescending<Pair<Note, Int>> { it.second }
                .thenByDescending { it.first.isPinned }
                .thenByDescending { it.first.updatedAt })
            .map { it.first }
            .take(limit)
    }

    /**
     * The tasks most relevant to [query], best first, capped at [limit]. Same scheme as [rankNotes];
     * with no usable keywords, falls back to the most recently updated tasks.
     */
    fun rankTasks(tasks: List<Task>, query: String, limit: Int): List<Task> {
        val keys = keywords(query)
        if (keys.isEmpty()) return tasks.sortedByDescending { it.updatedAt }.take(limit)
        return tasks
            .map { it to scoreTask(it, keys) }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<Task, Int>> { it.second }
                .thenByDescending { it.first.updatedAt })
            .map { it.first }
            .take(limit)
    }

    /**
     * A [budget]-sized excerpt of [content] centred on the earliest [keywords] hit, with ellipses
     * marking any trimming. Ensures the *matched* passage reaches the model — a note can score on a
     * keyword 2000 chars deep, which a plain `take(budget)` from the start would never include.
     */
    fun snippet(content: String, keywords: List<String>, budget: Int = 600): String {
        val text = content.trim()
        if (text.length <= budget) return text

        val lower = text.lowercase()
        val firstHit = keywords.mapNotNull { k -> lower.indexOf(k).takeIf { it >= 0 } }.minOrNull()
            ?: return text.take(budget).trimEnd() + "…"

        // Open a window a third before the hit so the match sits with leading context, not at the edge.
        val start = (firstHit - budget / 3).coerceIn(0, maxOf(0, text.length - budget))
        val end = (start + budget).coerceAtMost(text.length)
        val prefix = if (start > 0) "…" else ""
        val suffix = if (end < text.length) "…" else ""
        return prefix + text.substring(start, end).trim() + suffix
    }

    private fun scoreNote(note: Note, keys: List<String>): Int {
        val title = note.title.lowercase()
        val body = note.content.lowercase()
        val tags = note.tags.joinToString(" ") { it.lowercase() }
        var score = 0
        for (k in keys) {
            if (title.contains(k)) score += TITLE_WEIGHT
            if (tags.contains(k)) score += TAG_WEIGHT
            if (body.contains(k)) score += BODY_WEIGHT
        }
        return score
    }

    private fun scoreTask(task: Task, keys: List<String>): Int {
        val title = task.title.lowercase()
        val body = task.description.lowercase()
        val tags = task.tags.joinToString(" ") { it.label.lowercase() }
        var score = 0
        for (k in keys) {
            if (title.contains(k)) score += TITLE_WEIGHT
            if (tags.contains(k)) score += TAG_WEIGHT
            if (body.contains(k)) score += BODY_WEIGHT
        }
        return score
    }
}
