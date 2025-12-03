package com.example.sentio.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sentio.domain.repositories.NoteRepository
import com.example.sentio.domain.usecase.NoteUseCases
import com.example.sentio.presentation.state.EditorUiEffect
import com.example.sentio.presentation.state.EditorUiEvent
import com.example.sentio.presentation.state.EditorUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Editor screen following MVVM pattern.
 * Uses sealed classes for state management.
 */
class EditorViewModel(
    private val noteRepository: NoteRepository,
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditorUiState>(EditorUiState.Idle)
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _effects = Channel<EditorUiEffect>(Channel.BUFFERED)
    val effects: Flow<EditorUiEffect> = _effects.receiveAsFlow()

    /**
     * Handle UI events from the screen.
     */
    fun onEvent(event: EditorUiEvent) {
        when (event) {
            is EditorUiEvent.LoadNote -> loadNote(event.noteId)
            is EditorUiEvent.CreateNewNote -> createNewNote()
            is EditorUiEvent.UpdateTitle -> updateTitle(event.title)
            is EditorUiEvent.UpdateContent -> updateContent(event.content)
            is EditorUiEvent.TogglePin -> togglePin()
            is EditorUiEvent.ToggleFavorite -> toggleFavorite()
            is EditorUiEvent.Save -> saveNote()
            is EditorUiEvent.Delete -> deleteNote()
            is EditorUiEvent.AddTag -> addTag(event.tag)
            is EditorUiEvent.RemoveTag -> removeTag(event.tag)
        }
    }

    private fun loadNote(noteId: String) {
        viewModelScope.launch {
            _uiState.value = EditorUiState.Loading
            try {
                val note = noteRepository.getNoteById(noteId)
                if (note != null) {
                    _uiState.value = EditorUiState.Success(note = note)
                } else {
                    _uiState.value = EditorUiState.Error(
                        message = "Note not found",
                        retryAction = { loadNote(noteId) }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = EditorUiState.Error(
                    message = e.message ?: "Failed to load note",
                    retryAction = { loadNote(noteId) }
                )
            }
        }
    }

    private fun createNewNote() {
        _uiState.value = EditorUiState.NewNote()
    }

    private fun updateTitle(title: String) {
        when (val state = _uiState.value) {
            is EditorUiState.Success -> {
                _uiState.value = state.copy(
                    note = state.note.copy(title = title),
                    hasUnsavedChanges = true
                )
            }
            is EditorUiState.NewNote -> {
                _uiState.value = state.copy(title = title)
            }
            else -> {}
        }
    }

    private fun updateContent(content: String) {
        when (val state = _uiState.value) {
            is EditorUiState.Success -> {
                _uiState.value = state.copy(
                    note = state.note.copy(content = content),
                    hasUnsavedChanges = true
                )
            }
            is EditorUiState.NewNote -> {
                _uiState.value = state.copy(content = content)
            }
            else -> {}
        }
    }

    private fun saveNote() {
        val state = _uiState.value
        if (state !is EditorUiState.Success) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            noteUseCases.update(state.note)
                .onSuccess {
                    _uiState.value = state.copy(
                        isSaving = false,
                        hasUnsavedChanges = false,
                        lastSavedAt = System.currentTimeMillis()
                    )
                    _effects.send(EditorUiEffect.NoteSaved)
                }
                .onFailure { error ->
                    _uiState.value = state.copy(isSaving = false)
                    _effects.send(EditorUiEffect.ShowError(error.message ?: "Failed to save"))
                }
        }
    }

    private fun togglePin() {
        val state = _uiState.value
        if (state !is EditorUiState.Success) return

        val updatedNote = state.note.copy(isPinned = !state.note.isPinned)
        _uiState.value = state.copy(note = updatedNote, hasUnsavedChanges = true)

        viewModelScope.launch {
            noteUseCases.update(updatedNote)
        }
    }

    private fun toggleFavorite() {
        val state = _uiState.value
        if (state !is EditorUiState.Success) return

        val updatedNote = state.note.copy(isFavorite = !state.note.isFavorite)
        _uiState.value = state.copy(note = updatedNote, hasUnsavedChanges = true)

        viewModelScope.launch {
            noteUseCases.update(updatedNote)
        }
    }

    private fun deleteNote() {
        viewModelScope.launch {
            _effects.send(EditorUiEffect.NoteDeleted)
            _effects.send(EditorUiEffect.NavigateBack)
        }
    }

    private fun addTag(tag: String) {
        val state = _uiState.value
        if (state !is EditorUiState.Success) return

        val updatedTags = (state.note.tags + tag).distinct()
        _uiState.value = state.copy(
            note = state.note.copy(tags = updatedTags),
            hasUnsavedChanges = true
        )
    }

    private fun removeTag(tag: String) {
        val state = _uiState.value
        if (state !is EditorUiState.Success) return

        val updatedTags = state.note.tags.filter { it != tag }
        _uiState.value = state.copy(
            note = state.note.copy(tags = updatedTags),
            hasUnsavedChanges = true
        )
    }
}
