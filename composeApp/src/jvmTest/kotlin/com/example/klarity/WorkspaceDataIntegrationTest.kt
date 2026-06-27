package com.example.klarity

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.klarity.data.repositories.SqlDelightNoteRepository
import com.example.klarity.data.repositories.SqlDelightTaskRepository
import com.example.klarity.data.util.DispatcherProvider
import com.example.klarity.db.KlarityDatabase
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.Task
import com.example.klarity.domain.models.TaskStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * End-to-end integration test for the live data layer the Devbook UI is wired to.
 * Drives the real SQLDelight repositories against a fresh on-disk SQLite database (a temp file —
 * `:memory:` would give each JDBC connection its own empty DB), proving the create/read/update/
 * delete flows the screens depend on actually persist.
 */
class WorkspaceDataIntegrationTest {

    private val dispatchers = object : DispatcherProvider {
        // Repositories only use `io`; keep everything synchronous for deterministic tests.
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
    }

    private fun freshDb(): KlarityDatabase {
        val file = File.createTempFile("klarity-test", ".db").apply { delete(); deleteOnExit() }
        val driver = JdbcSqliteDriver("jdbc:sqlite:${file.absolutePath}")
        KlarityDatabase.Schema.create(driver)
        return KlarityDatabase(driver)
    }

    @Test
    fun note_create_read_update_pin_search_delete() = runBlocking {
        val repo = SqlDelightNoteRepository(freshDb(), dispatchers)

        assertTrue(repo.getAllNotes().first().isEmpty(), "DB starts empty")

        val created = repo.createNote(
            Note(title = "Gateway notes", content = "token bucket limiter", folderId = null),
        ).getOrThrow()

        val all = repo.getAllNotes().first()
        assertEquals(1, all.size, "note persisted")
        assertEquals("Gateway notes", all.first().title)

        repo.updateNote(created.copy(title = "Gateway notes v2", isPinned = true)).getOrThrow()
        assertEquals("Gateway notes v2", repo.getNoteById(created.id)?.title, "update persisted")

        val pinned = repo.getPinnedNotes().first()
        assertEquals(1, pinned.size, "pin reflected in getPinnedNotes")

        val hits = repo.searchNotes("token").first()
        assertEquals(1, hits.size, "content is searchable")

        repo.deleteNote(created.id).getOrThrow()
        assertTrue(repo.getAllNotes().first().isEmpty(), "empty after delete")
        assertNull(repo.getNoteById(created.id))
    }

    @Test
    fun task_create_move_count_delete() = runBlocking {
        val repo = SqlDelightTaskRepository(freshDb(), dispatchers)
        val now = Clock.System.now()

        repo.createTask(
            Task(id = "t1", title = "Finish gateway rate-limiter", status = TaskStatus.BACKLOG, createdAt = now, updatedAt = now),
        ).getOrThrow()

        assertEquals(1, repo.getAllTasks().first().size, "task persisted")
        assertEquals(1, repo.getTasksByStatus(TaskStatus.BACKLOG).first().size)

        repo.updateTaskStatus("t1", TaskStatus.IN_PROGRESS).getOrThrow()
        assertEquals(0, repo.getTasksByStatus(TaskStatus.BACKLOG).first().size, "moved out of backlog")
        assertEquals(1, repo.getTasksByStatus(TaskStatus.IN_PROGRESS).first().size, "moved into in-progress")
        assertEquals(1, repo.getTaskCountsByStatus()[TaskStatus.IN_PROGRESS], "counts reflect move")

        repo.deleteTask("t1").getOrThrow()
        assertTrue(repo.getAllTasks().first().isEmpty(), "empty after delete")
    }
}
