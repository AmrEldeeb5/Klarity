package com.example.sentio.presentation.state

import com.example.sentio.domain.models.Note

/**
 * Sealed class representing the UI state for the Home screen.
 * Using sealed classes provides type-safe state handling.
 */
sealed class HomeUiState {
    /**
     * Initial/idle state - no data loaded yet.
     */
    data object Idle : HomeUiState()

    /**
     * Loading state - data is being fetched.
     */
    data object Loading : HomeUiState()

    /**
     * Success state - data has been loaded successfully.
     */
    data class Success(
        val notes: List<Note> = emptyList(),
        val pinnedNotes: List<Note> = emptyList(),
        val searchQuery: String = "",
        val isSearching: Boolean = false,
        /** Currently selected note (single click - highlighted in sidebar) */
        val selectedNoteId: String? = null,
        /** Currently opened note (double click - shown in editor) */
        val openedNoteId: String? = null
    ) : HomeUiState()

    /**
     * Error state - an error occurred while loading data.
     */
    data class Error(
        val message: String,
        val retryAction: (() -> Unit)? = null
    ) : HomeUiState()
}

/**
 * UI events that can be triggered from the Home screen.
 */
sealed class HomeUiEvent {
    data class SearchQueryChanged(val query: String) : HomeUiEvent()
    data object ClearSearch : HomeUiEvent()
    /** Single click - select note (highlight in sidebar) */
    data class SelectNote(val noteId: String) : HomeUiEvent()
    /** Double click - open note in editor */
    data class OpenNote(val noteId: String) : HomeUiEvent()
    /** Close the currently opened note */
    data object CloseNote : HomeUiEvent()
    data class DeleteNote(val noteId: String) : HomeUiEvent()
    data object CreateNote : HomeUiEvent()
    data object Refresh : HomeUiEvent()
}

/**
 * Side effects that should be handled by the UI.
 */
sealed class HomeUiEffect {
    data class NavigateToEditor(val noteId: String) : HomeUiEffect()
    data class ShowSnackbar(val message: String) : HomeUiEffect()
    data class ShowError(val message: String) : HomeUiEffect()
}
