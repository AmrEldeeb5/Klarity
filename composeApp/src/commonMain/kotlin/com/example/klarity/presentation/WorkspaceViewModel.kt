package com.example.klarity.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuid4
import com.example.klarity.data.ai.AiException
import com.example.klarity.data.ai.AiService
import com.example.klarity.data.ai.AiTurn
import com.example.klarity.data.ai.WorkspaceRetrieval
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** A single turn in the local Assistant thread. */
@Immutable
data class ChatMessage(
    val fromUser: Boolean,
    val text: String,
    val sources: List<Note> = emptyList(),
)

/** One assistant conversation — a titled thread of [ChatMessage]s, like a Notion AI chat. */
@Immutable
data class Conversation(
    val id: String,
    val title: String,
    val messages: List<ChatMessage> = emptyList(),
    val updatedAt: Instant,
)

private const val NEW_CHAT_TITLE = "New AI chat"

/** How many ranked notes / tasks to feed the model as grounding context per question. */
private const val NOTE_CONTEXT_LIMIT = 6
private const val TASK_CONTEXT_LIMIT = 6

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

    // ── Notion-style create UX ──────────────────────────────────────────────────
    // A freshly created note opens with its title focused; a freshly created folder opens straight
    // into the sidebar's inline name editor. These one-shot signals carry the new item's id to the
    // UI, which consumes them once the focus / rename has been handed over.
    private val _pendingNoteFocus = MutableStateFlow<String?>(null)
    val pendingNoteFocus: StateFlow<String?> = _pendingNoteFocus
    fun consumeNoteFocus() { _pendingNoteFocus.value = null }

    private val _pendingFolderRename = MutableStateFlow<String?>(null)
    val pendingFolderRename: StateFlow<String?> = _pendingFolderRename
    fun consumeFolderRename() { _pendingFolderRename.value = null }

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search

    // The assistant keeps every chat from this session as a separate thread (Notion-style), so the
    // side panel can list a history and start fresh chats without losing the old ones. There is
    // always exactly one active conversation; [chat] is just its messages.
    private val _conversations = MutableStateFlow(listOf(freshConversation()))
    private val _activeId = MutableStateFlow(_conversations.value.first().id)

    /** Id of the conversation currently shown — drives the history highlight. */
    val activeConversationId: StateFlow<String> = _activeId

    /** Previous, non-empty conversations, newest first (for the history dropdown). */
    val conversations: StateFlow<List<Conversation>> = _conversations
        .map { list -> list.filter { it.messages.isNotEmpty() }.sortedByDescending { it.updatedAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Messages of the active conversation — what both the panel and full screen render. */
    val chat: StateFlow<List<ChatMessage>> = combine(_conversations, _activeId) { list, id ->
        list.firstOrNull { it.id == id }?.messages ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
    /** Creates a note and, like Notion, opens it with an empty title ready to type into. */
    fun createNote(title: String = "", folderId: String? = null) {
        viewModelScope.launch {
            val note = Note(title = title, content = "", folderId = folderId)
            noteRepo.createNote(note).onSuccess {
                _selectedNoteId.value = it.id
                _pendingNoteFocus.value = it.id
            }
        }
    }

    /** Inline rename from the sidebar tree (title may be blank — it shows as "Untitled note"). */
    fun renameNote(id: String, title: String) {
        val note = notes.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch { noteRepo.updateNote(note.copy(title = title.trim(), updatedAt = Clock.System.now())) }
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
    /** Creates a folder and signals the sidebar to open its inline name editor (Notion-style). */
    fun createFolder(name: String = "Untitled", parentId: String? = null) {
        viewModelScope.launch {
            val id = uuid4().toString()
            folderRepo.createFolder(
                Folder(id = id, name = name, parentId = parentId, createdAt = Clock.System.now()),
            ).onSuccess { _pendingFolderRename.value = id }
        }
    }

    fun renameFolder(id: String, name: String) {
        val folder = folders.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch { folderRepo.updateFolder(folder.copy(name = name.trim().ifBlank { "Untitled" })) }
    }

    /** Deletes a folder, re-homing its notes to the root so nothing is lost with it. */
    fun deleteFolder(id: String) {
        viewModelScope.launch {
            val now = Clock.System.now()
            notes.value.filter { it.folderId == id }.forEach {
                noteRepo.updateNote(it.copy(folderId = null, updatedAt = now))
            }
            folderRepo.deleteFolder(id)
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
        viewModelScope.launch { taskRepo.updateTaskStatus(task.id, status) }
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

    // ── Conversations ───────────────────────────────────────────────────────────
    private fun freshConversation(): Conversation =
        Conversation(id = uuid4().toString(), title = NEW_CHAT_TITLE, updatedAt = Clock.System.now())

    /** Appends a turn to the active conversation, titling a fresh chat from its first question. */
    private fun appendMessage(message: ChatMessage) {
        val now = Clock.System.now()
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id != _activeId.value) conv
                else conv.copy(
                    messages = conv.messages + message,
                    updatedAt = now,
                    title = if (conv.title == NEW_CHAT_TITLE && message.fromUser) titleFrom(message.text) else conv.title,
                )
            }
        }
    }

    private fun titleFrom(text: String): String {
        val one = text.trim().replace(Regex("\\s+"), " ")
        return if (one.length <= 42) one else one.take(42).trimEnd() + "…"
    }

    /** Starts a new chat. Reuses the current one when it's still empty (avoids stacking blanks). */
    fun newChat() {
        val active = _conversations.value.firstOrNull { it.id == _activeId.value }
        if (active != null && active.messages.isEmpty()) return
        val fresh = freshConversation()
        _conversations.update { listOf(fresh) + it }
        _activeId.value = fresh.id
    }

    /** Switches the active conversation (e.g. from the history list). */
    fun selectConversation(id: String) {
        if (_conversations.value.any { it.id == id }) _activeId.value = id
    }

    // ── Local Assistant (search-grounded, no external API) ──────────────────────
    /** The top-bar "New chat" CTA and the panel both start a fresh conversation. */
    fun clearChat() = newChat()

    /** Suggested-prompt: a quick stat overview of the workspace, grounded in current data. */
    fun summarizeWorkspace() {
        val n = notes.value
        val t = tasks.value
        appendMessage(ChatMessage(fromUser = true, text = "Summarize my workspace"))
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
        appendMessage(ChatMessage(fromUser = false, text = answer, sources = n.filter { it.isPinned }.take(3)))
    }

    /** Suggested-prompt: what's still open, grouped by status. */
    fun summarizeOpenTasks() {
        val open = tasks.value.filter { !it.completed && it.status != TaskStatus.DONE }
        appendMessage(ChatMessage(fromUser = true, text = "What's open?"))
        val answer = if (open.isEmpty()) {
            "Nothing open right now — every task is done or archived. 🎉"
        } else {
            val byStatus = open.groupBy { it.status }
                .entries.joinToString("; ") { (s, list) -> "${list.size} in ${s.label}" }
            val next = open.sortedBy { it.priority.ordinal }.take(3)
                .joinToString(", ") { "\"${it.title.ifBlank { "Untitled task" }}\"" }
            "You have ${open.size} open task${plural(open.size)} ($byStatus). Next up: $next."
        }
        appendMessage(ChatMessage(fromUser = false, text = answer))
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
            appendMessage(ChatMessage(fromUser = true, text = q))
            try {
                // These reads can fail (DB/IO); keep them inside the try so a failure becomes a
                // chat message rather than an unhandled crash in the launch.
                //
                // Rank the *whole* workspace by keyword relevance rather than calling the repo's
                // `searchNotes`/`searchTasks` — those substring-match the entire question, which a
                // natural-language query never matches verbatim. See [WorkspaceRetrieval].
                val allNotes = noteRepo.getAllNotes().first()
                val allTasks = taskRepo.getAllTasks().first()
                val noteMatches = WorkspaceRetrieval.rankNotes(allNotes, q, NOTE_CONTEXT_LIMIT)
                val taskMatches = WorkspaceRetrieval.rankTasks(allTasks, q, TASK_CONTEXT_LIMIT)
                val cfg = settingsRepo.current()

                if (!cfg.enabled) {
                    val answer = buildAnswer(q, noteMatches, taskMatches) +
                        "\n\n(Add an API key in Settings for full AI answers.)"
                    appendMessage(ChatMessage(fromUser = false, text = answer, sources = noteMatches.take(3)))
                    return@launch
                }

                _thinking.value = true
                try {
                    val system = buildSystemPrompt(q, noteMatches, taskMatches)
                    val history = chat.value.map { AiTurn(if (it.fromUser) "user" else "assistant", it.text) }
                    val answer = ai.complete(settings = cfg, system = system, messages = history)
                    appendMessage(ChatMessage(fromUser = false, text = answer, sources = noteMatches.take(3)))
                } finally {
                    _thinking.value = false
                }
            } catch (e: AiException) {
                appendMessage(ChatMessage(fromUser = false, text = "⚠️ ${e.message}"))
            } catch (e: Exception) {
                appendMessage(ChatMessage(fromUser = false, text = "⚠️ Something went wrong. Please try again."))
            }
        }
    }

    /**
     * Builds the grounding system prompt from the notes & tasks most relevant to [query]. The rules
     * are written defensively so even weaker models stay grounded: answer workspace questions only
     * from the context, admit when it isn't there, and mark any general-knowledge fallback as such.
     */
    private fun buildSystemPrompt(query: String, notes: List<Note>, tasks: List<Task>): String = buildString {
        val keys = WorkspaceRetrieval.keywords(query)
        val tz = TimeZone.currentSystemDefault()

        append("You are Lou, the friendly AI assistant built into Klarity — the user's personal notes & tasks app.\n")
        append("Today is ${todayString()}.\n\n")
        append("How to answer:\n")
        append("- For anything about the user's workspace, rely ONLY on the WORKSPACE CONTEXT below. ")
        append("Never invent notes, tasks, dates, or details that aren't there.\n")
        append("- If the context doesn't contain the answer, say so plainly (e.g. \"I couldn't find that in your notes\"). ")
        append("You may then add a brief general-knowledge answer, but clearly mark it as general info, not from their workspace.\n")
        append("- When you use a note or task, name it by its title so the user can find it.\n")
        append("- Be concise and friendly. Use Markdown when it helps — short headings, **bold**, ")
        append("bullet/numbered lists, `code`/fenced blocks — but keep it light for simple answers.\n\n")

        if (notes.isEmpty() && tasks.isEmpty()) {
            append("WORKSPACE CONTEXT: (nothing in the workspace matched this question)")
        } else {
            append("WORKSPACE CONTEXT:\n")
            if (notes.isNotEmpty()) {
                append("Notes:\n")
                notes.forEach { n ->
                    val title = n.title.ifBlank { "Untitled" }
                    val tags = if (n.tags.isNotEmpty()) " [tags: ${n.tags.joinToString(", ")}]" else ""
                    val body = WorkspaceRetrieval.snippet(n.content, keys).ifBlank { "(empty)" }
                    append("- \"$title\"$tags: $body\n")
                }
            }
            if (tasks.isNotEmpty()) {
                append("Tasks:\n")
                tasks.forEach { t ->
                    val title = t.title.ifBlank { "Untitled" }
                    append("- [${t.status.label} · ${t.priority.label} priority] \"$title\"")
                    t.dueDate?.let { append(" (due ${it.toLocalDateTime(tz).date})") }
                    if (t.description.isNotBlank()) {
                        append(" — ${WorkspaceRetrieval.snippet(t.description, keys, budget = 240)}")
                    }
                    append("\n")
                }
            }
        }
    }

    /** Human-readable current date (e.g. "Saturday, 2026-06-27") so Lou can reason about due dates. */
    private fun todayString(): String {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return "${titleCase(today.dayOfWeek.name)}, $today"
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
