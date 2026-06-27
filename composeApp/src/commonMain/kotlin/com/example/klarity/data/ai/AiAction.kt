package com.example.klarity.data.ai

import com.example.klarity.domain.models.TaskPriority
import com.example.klarity.domain.models.TaskStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * A concrete, validated workspace action parsed from a model [ToolCall]. Kept free of UI/repo
 * concerns: the ViewModel resolves display titles and executes these against the repositories.
 */
sealed interface AiAction {
    data class CreateNote(val title: String, val content: String, val tags: List<String>) : AiAction
    data class UpdateNote(val noteId: String, val title: String?, val content: String?, val tags: List<String>?) : AiAction
    data class DeleteNote(val noteId: String) : AiAction
    data class SetNotePinned(val noteId: String, val pinned: Boolean) : AiAction

    data class CreateTask(
        val title: String,
        val description: String,
        val status: TaskStatus,
        val priority: TaskPriority,
        val dueDate: Instant?,
        val tags: List<String>,
    ) : AiAction
    data class UpdateTask(
        val taskId: String,
        val title: String?,
        val description: String?,
        val priority: TaskPriority?,
        val dueDate: Instant?,
        val tags: List<String>?,
    ) : AiAction
    data class SetTaskStatus(val taskId: String, val status: TaskStatus) : AiAction
    data class CompleteTask(val taskId: String) : AiAction
    data class DeleteTask(val taskId: String) : AiAction
}

/** Whether confirming this action destroys data — drives the warning styling on the confirm card. */
val AiAction.isDestructive: Boolean
    get() = this is AiAction.DeleteNote || this is AiAction.DeleteTask

/** Maps a raw [ToolCall] to a typed [AiAction]; returns null if required arguments are missing. */
object AiActions {

    fun parse(call: ToolCall): AiAction? {
        val a = call.args
        return when (call.name) {
            "create_note" -> AiAction.CreateNote(
                title = a.str("title") ?: return null,
                content = a.str("content") ?: "",
                tags = a.strings("tags") ?: emptyList(),
            )
            "update_note" -> AiAction.UpdateNote(
                noteId = a.str("note_id") ?: return null,
                title = a.str("title"),
                content = a.str("content"),
                tags = a.strings("tags"),
            )
            "delete_note" -> AiAction.DeleteNote(noteId = a.str("note_id") ?: return null)
            "set_note_pinned" -> AiAction.SetNotePinned(
                noteId = a.str("note_id") ?: return null,
                pinned = a.bool("pinned") ?: return null,
            )
            "create_task" -> AiAction.CreateTask(
                title = a.str("title") ?: return null,
                description = a.str("description") ?: "",
                status = parseStatus(a.str("status")) ?: TaskStatus.TODO,
                priority = parsePriority(a.str("priority")) ?: TaskPriority.MEDIUM,
                dueDate = parseDueDate(a.str("due_date")),
                tags = a.strings("tags") ?: emptyList(),
            )
            "update_task" -> AiAction.UpdateTask(
                taskId = a.str("task_id") ?: return null,
                title = a.str("title"),
                description = a.str("description"),
                priority = parsePriority(a.str("priority")),
                dueDate = parseDueDate(a.str("due_date")),
                tags = a.strings("tags"),
            )
            "set_task_status" -> AiAction.SetTaskStatus(
                taskId = a.str("task_id") ?: return null,
                status = parseStatus(a.str("status")) ?: return null,
            )
            "complete_task" -> AiAction.CompleteTask(taskId = a.str("task_id") ?: return null)
            "delete_task" -> AiAction.DeleteTask(taskId = a.str("task_id") ?: return null)
            else -> null
        }
    }

    /** Matches a status by enum name or label, ignoring case/spacing/underscores ("in progress" → IN_PROGRESS). */
    fun parseStatus(raw: String?): TaskStatus? {
        val norm = raw.normalizeEnum() ?: return null
        return TaskStatus.entries.firstOrNull { it.name.normalizeEnum() == norm || it.label.normalizeEnum() == norm }
    }

    fun parsePriority(raw: String?): TaskPriority? {
        val norm = raw.normalizeEnum() ?: return null
        return TaskPriority.entries.firstOrNull { it.name.normalizeEnum() == norm || it.label.normalizeEnum() == norm }
    }

    private fun String?.normalizeEnum(): String? =
        this?.trim()?.lowercase()?.replace(" ", "")?.replace("_", "")?.takeIf { it.isNotEmpty() }

    /** Parses an ISO `YYYY-MM-DD` due date to an [Instant] at the start of that local day; null if invalid. */
    fun parseDueDate(raw: String?): Instant? {
        val text = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val date = runCatching { LocalDate.parse(text) }.getOrNull() ?: return null
        return date.atStartOfDayIn(TimeZone.currentSystemDefault())
    }

    private fun JsonObject.str(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content?.trim()?.takeIf { it.isNotEmpty() }

    private fun JsonObject.bool(key: String): Boolean? = this[key]?.jsonPrimitive?.booleanOrNull

    /** Reads a JSON string array into a clean list of labels; null if absent, never an empty list. */
    private fun JsonObject.strings(key: String): List<String>? =
        (this[key] as? JsonArray)
            ?.mapNotNull { (it as? JsonPrimitive)?.takeIf { p -> p.isString }?.content?.trim()?.takeIf { s -> s.isNotEmpty() } }
            ?.takeIf { it.isNotEmpty() }
}
