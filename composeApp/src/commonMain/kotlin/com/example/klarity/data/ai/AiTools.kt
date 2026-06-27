package com.example.klarity.data.ai

import com.example.klarity.domain.repositories.AiProvider
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** One workspace action the model asked to perform, before we map it to a concrete [AiAction]. */
data class ToolCall(val id: String, val name: String, val args: JsonObject)

/** The outcome of a tool-enabled completion: either a plain answer, or proposed actions (+ any text). */
sealed interface AiResult {
    data class Text(val text: String) : AiResult
    data class Actions(val text: String?, val calls: List<ToolCall>) : AiResult
}

/**
 * The tools Lou can call to act on the workspace, plus serialization to each provider's tool format.
 * The JSON Schemas are shared verbatim between Anthropic (`input_schema`) and OpenAI-compatible
 * (`function.parameters`) — only the envelope differs. Pure; no I/O.
 */
object AiTools {

    /** Status / priority enum values exposed to the model — must match the labels in [AiActions]. */
    private val STATUSES = listOf("Backlog", "To Do", "In Progress", "In Review", "Done", "Archived")
    private val PRIORITIES = listOf("High", "Medium", "Low", "None")

    private val TOOLS: List<ToolSpec> = listOf(
        ToolSpec(
            "create_note", "Create a new note in the user's workspace.",
            obj(mapOf(
                "title" to str("Short title for the note."),
                "content" to str("Body of the note, in Markdown. May be empty."),
                "tags" to stringArray("Optional tag labels for the note."),
            ), required = listOf("title")),
        ),
        ToolSpec(
            "update_note", "Edit an existing note's title, content, and/or tags. Use the note's id from context.",
            obj(mapOf(
                "note_id" to str("Id of the note to edit (from WORKSPACE CONTEXT)."),
                "title" to str("New title. Omit to keep the current one."),
                "content" to str("New Markdown content. Omit to keep the current one."),
                "tags" to stringArray("New tag labels (replaces existing). Omit to keep the current ones."),
            ), required = listOf("note_id")),
        ),
        ToolSpec(
            "delete_note", "Delete a note permanently. Use the note's id from context.",
            obj(mapOf("note_id" to str("Id of the note to delete.")), required = listOf("note_id")),
        ),
        ToolSpec(
            "set_note_pinned", "Pin or unpin a note.",
            obj(mapOf(
                "note_id" to str("Id of the note."),
                "pinned" to bool("true to pin, false to unpin."),
            ), required = listOf("note_id", "pinned")),
        ),
        ToolSpec(
            "create_task", "Create a new task on the board.",
            obj(mapOf(
                "title" to str("Short title for the task."),
                "description" to str("Optional details for the task."),
                "status" to enum("Board column. Defaults to To Do.", STATUSES),
                "priority" to enum("Priority. Defaults to Medium.", PRIORITIES),
                "due_date" to str("Optional due date as YYYY-MM-DD. Compute relative dates (e.g. \"Friday\", \"tomorrow\") from today's date given above."),
                "tags" to stringArray("Optional tag labels for the task."),
            ), required = listOf("title")),
        ),
        ToolSpec(
            "update_task", "Edit a task's title, description, priority, due date, and/or tags. Use the task's id from context.",
            obj(mapOf(
                "task_id" to str("Id of the task to edit (from WORKSPACE CONTEXT)."),
                "title" to str("New title. Omit to keep the current one."),
                "description" to str("New description. Omit to keep the current one."),
                "priority" to enum("New priority. Omit to keep the current one.", PRIORITIES),
                "due_date" to str("New due date as YYYY-MM-DD. Omit to keep the current one."),
                "tags" to stringArray("New tag labels (replaces existing). Omit to keep the current ones."),
            ), required = listOf("task_id")),
        ),
        ToolSpec(
            "set_task_status", "Move a task to a different board column.",
            obj(mapOf(
                "task_id" to str("Id of the task."),
                "status" to enum("Destination column.", STATUSES),
            ), required = listOf("task_id", "status")),
        ),
        ToolSpec(
            "complete_task", "Mark a task as complete (done).",
            obj(mapOf("task_id" to str("Id of the task to complete.")), required = listOf("task_id")),
        ),
        ToolSpec(
            "delete_task", "Delete a task permanently. Use the task's id from context.",
            obj(mapOf("task_id" to str("Id of the task to delete.")), required = listOf("task_id")),
        ),
    )

    /** Tool definitions in the given provider's wire format, as a JSON array for the request body. */
    fun toolsFor(provider: AiProvider): JsonArray =
        if (provider == AiProvider.ANTHROPIC) anthropicTools() else openAiTools()

    private fun anthropicTools(): JsonArray = buildJsonArray {
        TOOLS.forEach { tool ->
            addJsonObject {
                put("name", tool.name)
                put("description", tool.description)
                put("input_schema", tool.schema)
            }
        }
    }

    private fun openAiTools(): JsonArray = buildJsonArray {
        TOOLS.forEach { tool ->
            addJsonObject {
                put("type", "function")
                putJsonObject("function") {
                    put("name", tool.name)
                    put("description", tool.description)
                    put("parameters", tool.schema)
                }
            }
        }
    }

    private data class ToolSpec(val name: String, val description: String, val schema: JsonObject)

    // ── Schema builders ──────────────────────────────────────────────────────
    private fun str(desc: String): JsonObject = buildJsonObject {
        put("type", "string"); put("description", desc)
    }

    private fun bool(desc: String): JsonObject = buildJsonObject {
        put("type", "boolean"); put("description", desc)
    }

    private fun enum(desc: String, values: List<String>): JsonObject = buildJsonObject {
        put("type", "string"); put("description", desc)
        putJsonArray("enum") { values.forEach { add(it) } }
    }

    private fun stringArray(desc: String): JsonObject = buildJsonObject {
        put("type", "array"); put("description", desc)
        putJsonObject("items") { put("type", "string") }
    }

    private fun obj(properties: Map<String, JsonObject>, required: List<String>): JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") { properties.forEach { (k, v) -> put(k, v) } }
        putJsonArray("required") { required.forEach { add(it) } }
    }
}
