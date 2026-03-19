package com.example.klarity.presentation.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.klarity.domain.search.*
import com.example.klarity.presentation.screen.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Search state manager for handling search operations.
 * 
 * Manages search query, history, saved searches, and results.
 * Can be integrated into TasksViewModel or used standalone.
 */
class SearchStateManager(
    private val searchExecutor: SearchExecutor = SearchExecutor(),
    private val searchParser: SearchParser = SearchParser(),
    private val suggestionProvider: SearchSuggestionProvider = SearchSuggestionProvider(),
    private val historyRepository: SearchHistoryRepository = InMemorySearchHistoryRepository(),
    private val scope: CoroutineScope
) {
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Parsed query
    private val _parsedQuery = MutableStateFlow(SearchQuery())
    val parsedQuery: StateFlow<SearchQuery> = _parsedQuery.asStateFlow()
    
    // Search results
    private val _searchResults = MutableStateFlow<List<Task>>(emptyList())
    val searchResults: StateFlow<List<Task>> = _searchResults.asStateFlow()
    
    // Search history
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()
    
    // Saved searches
    private val _savedSearches = MutableStateFlow<List<SavedSearch>>(emptyList())
    val savedSearches: StateFlow<List<SavedSearch>> = _savedSearches.asStateFlow()
    
    // Show help dialog
    var showHelpDialog by mutableStateOf(false)
        private set
    
    // All tasks (to search against)
    private var allTasks: List<Task> = emptyList()
    
    init {
        loadHistory()
        loadSavedSearches()
    }
    
    /**
     * Updates the search query and executes the search.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        
        // Parse query
        val parsed = searchParser.parse(query)
        _parsedQuery.value = parsed
        
        // Execute search
        executeSearch(parsed)
        
        // Save to history if not empty
        if (query.isNotBlank() && query != _searchHistory.value.firstOrNull()) {
            scope.launch {
                historyRepository.addQuery(query)
                loadHistory()
            }
        }
    }
    
    /**
     * Updates the list of tasks to search against.
     */
    fun updateTasks(tasks: List<Task>) {
        allTasks = tasks
        
        // Re-execute search if there's a query
        if (_searchQuery.value.isNotEmpty()) {
            executeSearch(_parsedQuery.value)
        }
    }
    
    /**
     * Executes the search with the current query.
     */
    private fun executeSearch(query: SearchQuery) {
        val results = searchExecutor.executeSearch(query, allTasks)
        _searchResults.value = results
    }
    
    /**
     * Clears the search query and results.
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _parsedQuery.value = SearchQuery()
        _searchResults.value = emptyList()
    }
    
    /**
     * Shows the help dialog.
     */
    fun showHelp() {
        showHelpDialog = true
    }
    
    /**
     * Hides the help dialog.
     */
    fun hideHelp() {
        showHelpDialog = false
    }
    
    /**
     * Saves the current search with a name.
     */
    fun saveCurrentSearch(name: String, description: String? = null) {
        scope.launch {
            historyRepository.saveSearch(name, _searchQuery.value, description)
            loadSavedSearches()
        }
    }
    
    /**
     * Deletes a saved search.
     */
    fun deleteSavedSearch(id: String) {
        scope.launch {
            historyRepository.deleteSavedSearch(id)
            loadSavedSearches()
        }
    }
    
    /**
     * Applies a saved search.
     */
    fun applySavedSearch(savedSearch: SavedSearch) {
        onSearchQueryChanged(savedSearch.query)
    }
    
    /**
     * Clears search history.
     */
    fun clearHistory() {
        scope.launch {
            historyRepository.clearHistory()
            loadHistory()
        }
    }
    
    /**
     * Removes a query from history.
     */
    fun removeFromHistory(query: String) {
        scope.launch {
            historyRepository.removeQuery(query)
            loadHistory()
        }
    }
    
    /**
     * Gets suggestions for the current query.
     */
    fun getSuggestions(): List<SearchSuggestion> {
        return suggestionProvider.getSuggestions(
            query = _searchQuery.value,
            recentSearches = _searchHistory.value,
            savedSearches = _savedSearches.value.map { it.name to it.query },
            matchingTasks = _searchResults.value.take(5)
        )
    }
    
    /**
     * Loads search history from repository.
     */
    private fun loadHistory() {
        scope.launch {
            val history = historyRepository.getHistory()
            _searchHistory.value = history.queries
        }
    }
    
    /**
     * Loads saved searches from repository.
     */
    private fun loadSavedSearches() {
        scope.launch {
            val searches = historyRepository.getSavedSearches()
            _savedSearches.value = searches
        }
    }
}

/**
 * UI State for search feature.
 */
data class SearchUiState(
    val query: String = "",
    val parsedQuery: SearchQuery = SearchQuery(),
    val results: List<Task> = emptyList(),
    val history: List<String> = emptyList(),
    val savedSearches: List<SavedSearch> = emptyList(),
    val showHelp: Boolean = false,
    val isSearching: Boolean = false,
    val suggestions: List<SearchSuggestion> = emptyList()
)
