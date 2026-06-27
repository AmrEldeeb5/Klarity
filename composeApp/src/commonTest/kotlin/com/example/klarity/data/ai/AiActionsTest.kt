package com.example.klarity.data.ai

import com.example.klarity.domain.models.TaskPriority
import com.example.klarity.domain.models.TaskStatus
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiActionsTest {

    private fun call(name: String, args: JsonObject) = ToolCall(id = "id", name = name, args = args)

    @Test
    fun `create_note parses title, content, and tags`() {
        val action = AiActions.parse(call("create_note", buildJsonObject {
            put("title", "Groceries"); put("content", "milk, eggs")
            putJsonArray("tags") { add("shopping"); add("home") }
        }))
        assertEquals(AiAction.CreateNote("Groceries", "milk, eggs", listOf("shopping", "home")), action)
    }

    @Test
    fun `create_note without tags yields an empty list`() {
        val action = AiActions.parse(call("create_note", buildJsonObject { put("title", "X") })) as AiAction.CreateNote
        assertEquals(emptyList(), action.tags)
    }

    @Test
    fun `create_note without a title is rejected`() {
        assertNull(AiActions.parse(call("create_note", buildJsonObject { put("content", "orphan") })))
    }

    @Test
    fun `create_task applies defaults and parses enums by label`() {
        val action = AiActions.parse(call("create_task", buildJsonObject {
            put("title", "Ship it"); put("status", "In Progress"); put("priority", "High")
        })) as AiAction.CreateTask
        assertEquals("Ship it", action.title)
        assertEquals(TaskStatus.IN_PROGRESS, action.status)
        assertEquals(TaskPriority.HIGH, action.priority)
    }

    @Test
    fun `create_task defaults to a visible column and medium priority when omitted`() {
        val action = AiActions.parse(call("create_task", buildJsonObject { put("title", "Untitled") })) as AiAction.CreateTask
        // BACKLOG, not TODO — TODO renders in no board/list view.
        assertEquals(TaskStatus.BACKLOG, action.status)
        assertEquals(TaskPriority.MEDIUM, action.priority)
        assertNull(action.dueDate)
        assertEquals(emptyList(), action.tags)
    }

    @Test
    fun `create_task parses a valid ISO due date and ignores a bad one`() {
        val withDate = AiActions.parse(call("create_task", buildJsonObject {
            put("title", "Pay rent"); put("due_date", "2026-07-03")
        })) as AiAction.CreateTask
        assertTrue(withDate.dueDate != null, "a valid YYYY-MM-DD should parse to an Instant")

        val badDate = AiActions.parse(call("create_task", buildJsonObject {
            put("title", "Pay rent"); put("due_date", "next friday")
        })) as AiAction.CreateTask
        assertNull(badDate.dueDate)
    }

    @Test
    fun `set_task_status without a status is rejected`() {
        assertNull(AiActions.parse(call("set_task_status", buildJsonObject { put("task_id", "t1") })))
    }

    @Test
    fun `set_note_pinned parses the boolean`() {
        val action = AiActions.parse(call("set_note_pinned", buildJsonObject {
            put("note_id", "n1"); put("pinned", true)
        }))
        assertEquals(AiAction.SetNotePinned("n1", true), action)
    }

    @Test
    fun `parseStatus accepts enum name, label, and loose spacing`() {
        assertEquals(TaskStatus.IN_REVIEW, AiActions.parseStatus("IN_REVIEW"))
        assertEquals(TaskStatus.IN_REVIEW, AiActions.parseStatus("in review"))
        assertEquals(TaskStatus.TODO, AiActions.parseStatus("To Do"))
        assertNull(AiActions.parseStatus("nonsense"))
        assertNull(AiActions.parseStatus(null))
    }

    @Test
    fun `unknown tool name yields null`() {
        assertNull(AiActions.parse(call("launch_rockets", buildJsonObject { put("x", "y") })))
    }

    @Test
    fun `delete_task maps to a recoverable archive, not a hard delete`() {
        val action = AiActions.parse(call("delete_task", buildJsonObject { put("task_id", "t9") }))
        assertTrue(action is AiAction.ArchiveTask && action.taskId == "t9")
    }
}
