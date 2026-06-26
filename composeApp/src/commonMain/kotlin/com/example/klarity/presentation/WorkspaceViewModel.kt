package com.example.klarity.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuid4
import com.example.klarity.data.ai.AiException
import com.example.klarity.data.ai.AiService
import com.example.klarity.data.ai.AiTurn
import com.example.klarity.domain.models.Folder
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.Task
import com.example.klarity.domain.models.TaskStatus
import com.example.klarity.domain.repositories.AiProvider
import com.example.klarity.domain.repositories.AiSettings
import com.example.klarity.domain.repositories.FolderRepository
import com.example.klarity.domain.repositories.NoteRepository
import com.example.klarity.domain.repositories.SettingsRepository
import com.example.klarity.domain.repositories.TagRepository
import com.example.klarity.domain.repositories.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** A single turn in the local Assistant thread. */
@Immutable
data class ChatMessage(
    val fromUser: Boolean,
    val text: String,
    val sources: List<Note> = emptyList(),
)

/**
 * Single shared state-holder for the Devbook screens. Exposes the live repository data as
 * [StateFlow]s and provides all note/task mutations. The local Assistant answers by searching the
 * user's own notes and tasks — no external API.
 */
class WorkspaceViewModel(
    private val noteRepo: NoteRepository,
    private val folderRepo: FolderRepository,
    private val taskRepo: TaskRepository,
    private val tagRepo: TagRepository,
    private val settingsRepo: SettingsRepository,
    private val ai: AiService,
) : ViewModel() {

    val notes: StateFlow<List<Note>> = noteRepo.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val folders: StateFlow<List<Folder>> = folderRepo.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tasks: StateFlow<List<Task>> = taskRepo.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedNoteId = MutableStateFlow<String?>(null)
    val selectedNote: StateFlow<Note?> =
        combine(notes, _selectedNoteId) { list, id -> list.firstOrNull { it.id == id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search

    private val _chat = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chat: StateFlow<List<ChatMessage>> = _chat

    /** True while an AI request is in flight (drives the "thinking…" indicator). */
    private val _thinking = MutableStateFlow(false)
    val thinking: StateFlow<Boolean> = _thinking

    /** Local AI settings (provider, API key, model, base URL). */
    val settings: StateFlow<AiSettings> = settingsRepo.settings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiSettings())

    // Greeting / date computed once at construction (kept simple; not live across midnight).
    val greeting: String
    val todayLabel: String

    init {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        greeting = when (now.hour) {
            in 0..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
        todayLabel = "${titleCase(now.dayOfWeek.name)}, ${titleCase(now.month.name)} ${now.dayOfMonth}"
    }

    // ── Selection / search ─────────────────────────────────────────────────────
    fun selectNote(id: String?) { _selectedNoteId.value = id }
    fun setSearch(query: String) { _search.value = query }

    // ── Note actions ───────────────────────────────────────────────────────────
    fun createNote(title: String = "Untitled note", folderId: String? = null) {
        viewModelScope.launch {
            val note = Note(title = title, content = "", folderId = folderId)
            noteRepo.createNote(note).onSuccess { _selectedNoteId.value = it.id }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch { noteRepo.updateNote(note.copy(updatedAt = Clock.System.now())) }
    }

    fun togglePin(note: Note) = updateNote(note.copy(isPinned = !note.isPinned))

    fun deleteNote(id: String) {
        viewModelScope.launch {
            noteRepo.deleteNote(id)
            if (_selectedNoteId.value == id) _selectedNoteId.value = null
        }
    }

    // ── Folder actions ─────────────────────────────────────────────────────────
    fun createFolder(name: String = "New folder", parentId: String? = null) {
        viewModelScope.launch {
            folderRepo.createFolder(
                Folder(id = uuid4().toString(), name = name, parentId = parentId, createdAt = Clock.System.now()),
            )
        }
    }

    // ── Task actions ───────────────────────────────────────────────────────────
    fun createTask(title: String = "New task", status: TaskStatus = TaskStatus.BACKLOG) {
        viewModelScope.launch {
            val now = Clock.System.now()
            taskRepo.createTask(
                Task(id = uuid4().toString(), title = title, status = status, createdAt = now, updatedAt = now),
            )
        }
    }

    fun moveTask(task: Task, status: TaskStatus) {
        viewModelScope.launch { taskRepo.updateTaskStatus(task.id, status, task.order) }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { taskRepo.updateTask(task.copy(updatedAt = Clock.System.now())) }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch { taskRepo.updateTaskCompletion(task.id, !task.completed) }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch { taskRepo.deleteTask(id) }
    }

    // ── Local Assistant (search-grounded, no external API) ──────────────────────
    fun clearChat() { _chat.value = emptyList() }

    /** Suggested-prompt: a quick stat overview of the workspace, grounded in current data. */
    fun summarizeWorkspace() {
        val n = notes.value
        val t = tasks.value
        _chat.update { it + ChatMessage(fromUser = true, text = "Summarize my workspace") }
        val answer = if (n.isEmpty() && t.isEmpty()) {
            "Your workspace is empty so far — create a note or task and I'll summarize it here."
        } else {
            val open = t.count { !it.completed && it.status != TaskStatus.DONE }
            val pinned = n.count { it.isPinned }
            buildString {
                append("You have ${n.size} note${plural(n.size)} (${pinned} pinned) ")
                append("and ${t.size} task${plural(t.size)}, $open still open.")
                val recent = n.sortedByDescending { it.updatedAt }.take(3)
                    .joinToString(", ") { "\"${it.title.ifBlank { "Untitled" }}\"" }
                if (recent.isNotEmpty()) append(" Recently edited: $recent.")
            }
        }
        _chat.update { it + ChatMessage(fromUser = false, text = answer, sources = n.filter { it.isPinned }.take(3)) }
    }

    /** Suggested-prompt: what's still open, grouped by status. */
    fun summarizeOpenTasks() {
        val open = tasks.value.filter { !it.completed && it.status != TaskStatus.DONE }
        _chat.update { it + ChatMessage(fromUser = true, text = "What's open?") }
        val answer = if (open.isEmpty()) {
            "Nothing open right now — every task is done or archived. 🎉"
        } else {
            val byStatus = open.groupBy { it.status }
                .entries.joinToString("; ") { (s, list) -> "${list.size} in ${s.label}" }
            val next = open.sortedBy { it.priority.ordinal }.take(3)
                .joinToString(", ") { "\"${it.title.ifBlank { "Untitled task" }}\"" }
            "You have ${open.size} open task${plural(open.size)} ($byStatus). Next up: $next."
        }
        _chat.update { it + ChatMessage(fromUser = false, text = answer) }
    }

    private fun plural(count: Int): String = if (count == 1) "" else "s"

    // ── Settings ─────────────────────────────────────────────────────────────
    fun saveAiSettings(provider: AiProvider, apiKey: String?, model: String, baseUrl: String) {
        viewModelScope.launch { settingsRepo.save(provider, apiKey, model, baseUrl) }
    }

    /** Fetches the models the given key/provider can use (for the Settings model picker). */
    suspend fun listModels(provider: AiProvider, apiKey: String?, baseUrl: String): List<String> {
        val key = apiKey?.takeIf { it.isNotBlank() } ?: throw AiException("Enter an API key first.")
        return ai.listModels(provider, key, baseUrl)
    }

    /**
     * Answer a question. With an API key set, the configured AI answers — grounded in the user's
     * notes & tasks (RAG-style). Without a key, falls back to the local search-only answer.
     */
    fun ask(query: String) {
        val q = query.trim()
        if (q.isEmpty() || _thinking.value) return
        viewModelScope.launch {
            _chat.update { it + ChatMessage(fromUser = true, text = q) }
            val noteMatches = noteRepo.searchNotes(q).first()
            val taskMatches = taskRepo.searchTasks(q).first()

            val cfg = settingsRepo.current()
            if (!cfg.enabled) {
                val answer = buildAnswer(q, noteMatches, taskMatches) +
                    "\n\n(Add an API key in Settings for full AI answers.)"
                _chat.update { it + ChatMessage(fromUser = false, text = answer, sources = noteMatches.take(3)) }
                return@launch
            }

            _thinking.value = true
            try {
                val system = buildSystemPrompt(noteMatches, taskMatches)
                val history = _chat.value.map { AiTurn(if (it.fromUser) "user" else "assistant", it.text) }
                val answer = ai.complete(settings = cfg, system = system, messages = history)
                _chat.update { it + ChatMessage(fromUser = false, text = answer, sources = noteMatches.take(3)) }
            } catch (e: AiException) {
                _chat.update { it + ChatMessage(fromUser = false, text = "⚠️ ${e.message}") }
            } catch (e: Exception) {
                _chat.update { it + ChatMessage(fromUser = false, text = "⚠️ Something went wrong contacting the AI service.") }
            } finally {
                _thinking.value = false
            }
        }
    }

    /** Builds the grounding system prompt from the notes & tasks most relevant to the query. */
    private fun buildSystemPrompt(notes: List<Note>, tasks: List<Task>): String = buildString {
        append("You are Klarity's assistant, embedded in the user's personal notes & tasks app. ")
        append("Use the workspace context below when it's relevant; if the answer isn't there, say so ")
        append("and answer briefly from general knowledge. Be concise and friendly. Use plain text.\n\n")
        if (notes.isEmpty() && tasks.isEmpty()) {
            append("WORKSPACE CONTEXT: (no notes or tasks matched this query)")
        } else {
            append("WORKSPACE CONTEXT:\n")
            if (notes.isNotEmpty()) {
                append("Notes:\n")
                notes.take(5).forEach { n ->
                    val body = n.content.take(500).ifBlank { "(empty)" }
                    append("- \"${n.title.ifBlank { "Untitled" }}\": $body\n")
                }
            }
            if (tasks.isNotEmpty()) {
                append("Tasks:\n")
                tasks.take(5).forEach { t ->
                    append("- [${t.status.label}] ${t.title.ifBlank { "Untitled" }}")
                    if (t.description.isNotBlank()) append(" — ${t.description.take(200)}")
                    append("\n")
                }
            }
        }
    }

    private fun buildAnswer(query: String, notes: List<Note>, tasks: List<Task>): String = when {
        notes.isEmpty() && tasks.isEmpty() ->
            "I couldn't find anything in your workspace about \"$query\" yet. Try adding a note or task first."
        notes.isNotEmpty() -> {
            val top = notes.first()
            val snippet = top.preview().ifBlank { "(no content yet)" }
            "I found ${notes.size} note(s) mentioning \"$query\"" +
                (if (tasks.isNotEmpty()) " and ${tasks.size} task(s)" else "") +
                ". The most relevant is \"${top.title}\": $snippet"
        }
        else ->
            "No notes matched, but ${tasks.size} task(s) mention \"$query\" — e.g. \"${tasks.first().title}\"."
    }

    private fun titleCase(s: String): String =
        s.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
