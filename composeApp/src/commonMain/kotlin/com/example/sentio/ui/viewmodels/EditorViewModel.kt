package com.example.sentio.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sentio.domain.models.Note
import com.example.sentio.domain.repositories.NoteRepository
import com.example.sentio.domain.usecase.UpdateNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditorUiState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class EditorViewModel(
    private val noteRepository: NoteRepository,
    private val updateNoteUseCase: UpdateNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun loadNote(noteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val note = noteRepository.getNoteById(noteId)
                _uiState.value = EditorUiState(note = note, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = EditorUiState(
                    error = e.message ?: "Failed to load note",
                    isLoading = false
                )
            }
        }
    }

    fun updateTitle(title: String) {
        val currentNote = _uiState.value.note ?: return
        _uiState.value = _uiState.value.copy(
            note = currentNote.copy(title = title)
        )
    }

    fun updateContent(content: String) {
        val currentNote = _uiState.value.note ?: return
        _uiState.value = _uiState.value.copy(
            note = currentNote.copy(content = content)
        )
    }

    fun saveNote() {
        val note = _uiState.value.note ?: return
        viewModelScope.launch {
            updateNoteUseCase(note)
        }
    }

    fun togglePin() {
        val currentNote = _uiState.value.note ?: return
        val updatedNote = currentNote.copy(isPinned = !currentNote.isPinned)
        _uiState.value = _uiState.value.copy(note = updatedNote)
        viewModelScope.launch {
            updateNoteUseCase(updatedNote)
        }
    }

    fun toggleFavorite() {
        val currentNote = _uiState.value.note ?: return
        val updatedNote = currentNote.copy(isFavorite = !currentNote.isFavorite)
        _uiState.value = _uiState.value.copy(note = updatedNote)
        viewModelScope.launch {
            updateNoteUseCase(updatedNote)
        }
    }
}
