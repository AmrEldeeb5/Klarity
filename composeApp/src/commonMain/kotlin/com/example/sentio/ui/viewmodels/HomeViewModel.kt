package com.example.sentio.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sentio.domain.models.Note
import com.example.sentio.domain.repositories.NoteRepository
import com.example.sentio.domain.usecase.CreateNoteUseCase
import com.example.sentio.domain.usecase.DeleteNoteUseCase
import com.example.sentio.domain.usecase.SearchNotesUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val noteRepository: NoteRepository,
    private val createNoteUseCase: CreateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val searchNotesUseCase: SearchNotesUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val notes: StateFlow<List<Note>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                noteRepository.getAllNotes()
            } else {
                searchNotesUseCase(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pinnedNotes: StateFlow<List<Note>> = noteRepository.getPinnedNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createNote(title: String = "Untitled Note") {
        viewModelScope.launch {
            createNoteUseCase(title = title)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            deleteNoteUseCase(noteId)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}
