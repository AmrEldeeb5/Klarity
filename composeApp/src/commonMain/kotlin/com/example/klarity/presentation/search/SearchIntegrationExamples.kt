package com.example.klarity.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.components.*
import com.example.klarity.presentation.screen.tasks.Task

/**
 * Integration Example: How to use Advanced Search in TasksScreen
 * 
 * This file demonstrates how to integrate the advanced search system
 * into your existing TasksScreen or any other screen.
 */

/**
 * Example 1: Simple Integration with Basic Search
 * 
 * Minimal setup with search bar and results display.
 */
@Composable
fun SimpleSearchIntegrationExample(
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(tasks) }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Search bar
        AdvancedSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { parsedQuery ->
                // Execute search with parsed query
                // This would typically be handled by your ViewModel
                println("Executing search: $parsedQuery")
            },
            matchingTasks = searchResults.take(5),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        // Results
        Text(
            text = "${searchResults.size} tasks found",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * Example 2: Integration with SearchStateManager
 * 
 * Full-featured integration with state management, history, and saved searches.
 */
@Composable
fun FullSearchIntegrationExample(
    tasks: List<Task>,
    searchStateManager: SearchStateManager,
    modifier: Modifier = Modifier
) {
    val searchQuery by searchStateManager.searchQuery.collectAsState()
    val searchResults by searchStateManager.searchResults.collectAsState()
    val searchHistory by searchStateManager.searchHistory.collectAsState()
    val savedSearches by searchStateManager.savedSearches.collectAsState()
    
    // Update tasks in state manager
    LaunchedEffect(tasks) {
        searchStateManager.updateTasks(tasks)
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Search bar with all features
        AdvancedSearchBar(
            query = searchQuery,
            onQueryChange = { searchStateManager.onSearchQueryChanged(it) },
            onSearch = { /* Handled by state manager */ },
            recentSearches = searchHistory,
            savedSearches = savedSearches,
            matchingTasks = searchResults.take(5),
            onShowHelp = { searchStateManager.showHelp() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        // Search syntax help dialog
        SearchSyntaxHelpDialog(
            visible = searchStateManager.showHelpDialog,
            onDismiss = { searchStateManager.hideHelp() }
        )
        
        // Results display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (searchQuery.isNotEmpty()) {
                Text(
                    text = "${searchResults.size} tasks found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Example 3: Integration into TasksViewModel
 * 
 * Shows how to integrate search into your existing ViewModel.
 */
/*
class TasksViewModel(
    private val taskRepository: TaskRepository,
    // ... other dependencies
) : ViewModel() {
    
    // Add search state manager
    private val searchStateManager = SearchStateManager(
        scope = viewModelScope
    )
    
    // Expose search state
    val searchQuery = searchStateManager.searchQuery
    val searchResults = searchStateManager.searchResults
    val searchHistory = searchStateManager.searchHistory
    val savedSearches = searchStateManager.savedSearches
    
    // All tasks flow
    private val allTasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Filtered tasks - either search results or all tasks
    val displayedTasks: StateFlow<List<Task>> = combine(
        allTasks,
        searchQuery,
        searchResults
    ) { tasks, query, results ->
        if (query.isEmpty()) tasks else results
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    init {
        // Update search manager when tasks change
        viewModelScope.launch {
            allTasks.collect { tasks ->
                searchStateManager.updateTasks(tasks)
            }
        }
    }
    
    // Search functions
    fun onSearchQueryChanged(query: String) {
        searchStateManager.onSearchQueryChanged(query)
    }
    
    fun clearSearch() {
        searchStateManager.clearSearch()
    }
    
    fun showSearchHelp() {
        searchStateManager.showHelp()
    }
    
    fun hideSearchHelp() {
        searchStateManager.hideHelp()
    }
    
    fun saveCurrentSearch(name: String, description: String? = null) {
        searchStateManager.saveCurrentSearch(name, description)
    }
    
    fun deleteSavedSearch(id: String) {
        searchStateManager.deleteSavedSearch(id)
    }
    
    fun applySavedSearch(savedSearch: SavedSearch) {
        searchStateManager.applySavedSearch(savedSearch)
    }
}
*/

/**
 * Example 4: Search with Keyboard Shortcut
 * 
 * Add Cmd/Ctrl+F shortcut to focus search bar.
 */
@Composable
fun SearchWithKeyboardShortcutExample(
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    
    // Handle keyboard shortcuts
    LaunchedEffect(Unit) {
        // In a real implementation, you'd use a keyboard event handler
        // to detect Cmd/Ctrl+F and call searchFocusRequester.requestFocus()
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        AdvancedSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { /* Execute search */ },
            matchingTasks = tasks.take(5),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

/**
 * Example 5: Search Results with Highlighting
 * 
 * Display search results with highlighted matching text.
 */
@Composable
fun SearchResultsWithHighlighting(
    searchQuery: String,
    searchResults: List<Task>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        searchResults.forEach { task ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Highlighted title
                    HighlightedText(
                        text = task.title,
                        query = searchQuery,
                        modifier = Modifier
                    )
                    
                    // Highlighted description
                    if (task.description.isNotEmpty()) {
                        HighlightedText(
                            text = task.description,
                            query = searchQuery,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Example 6: Custom Search Filter UI
 * 
 * Build custom filter UI using parsed query.
 */
@Composable
fun CustomSearchFilterUI(
    parsedQuery: com.example.klarity.domain.search.SearchQuery,
    onRemoveFilter: (com.example.klarity.domain.search.SearchFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (parsedQuery.text.isNotEmpty()) {
            FilterChip(
                selected = true,
                onClick = { /* Clear text */ },
                label = { Text("Text: ${parsedQuery.text}") }
            )
        }
        
        Row {
            parsedQuery.filters.forEach { filter ->
                FilterChip(
                    selected = true,
                    onClick = { onRemoveFilter(filter) },
                    label = { Text(filter.toDisplayString()) }
                )
            }
        }
    }
}

/**
 * Example 7: Save Search Dialog
 * 
 * Dialog for saving current search with a name.
 */
@Composable
fun SaveSearchDialog(
    query: String,
    onSave: (name: String, description: String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Search") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Save this search for quick access later")
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("e.g., High Priority Bugs") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    placeholder = { Text("What is this search for?") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Query: $query",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, description.ifBlank { null })
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
