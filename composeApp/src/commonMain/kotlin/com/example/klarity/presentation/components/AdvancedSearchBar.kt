package com.example.klarity.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.search.*
import com.example.klarity.presentation.screen.tasks.Task
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

/**
 * Advanced Search Bar with filter syntax support.
 * 
 * Features:
 * - Text search with filter syntax
 * - Auto-complete suggestions
 * - Real-time filter chip display
 * - Keyboard shortcuts
 * - Search history
 */

@OptIn(FlowPreview::class)
@Composable
fun AdvancedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (SearchQuery) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search tasks... (type '?' for help)",
    recentSearches: List<String> = emptyList(),
    savedSearches: List<SavedSearch> = emptyList(),
    matchingTasks: List<Task> = emptyList(),
    onShowHelp: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val parser = remember { SearchParser() }
    val suggestionProvider = remember { SearchSuggestionProvider() }
    
    // Parse query
    val parsedQuery = remember(query) { parser.parse(query) }
    
    // Debounce search execution
    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            snapshotFlow { query }
                .debounce(300)
                .collect { onSearch(parsedQuery) }
        }
    }
    
    // Get suggestions
    val suggestions = remember(query, recentSearches, savedSearches, matchingTasks) {
        suggestionProvider.getSuggestions(
            query = query,
            recentSearches = recentSearches,
            savedSearches = savedSearches.map { it.name to it.query },
            matchingTasks = matchingTasks
        )
    }
    
    Column(modifier = modifier) {
        // Search input field
        SearchInputField(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = placeholder,
            isFocused = isFocused,
            onFocusChanged = { focused ->
                isFocused = focused
                showSuggestions = focused
            },
            onClear = { onQueryChange("") },
            onShowHelp = onShowHelp,
            focusRequester = focusRequester,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Active filter chips
        AnimatedVisibility(
            visible = parsedQuery.filters.isNotEmpty() || parsedQuery.negatedFilters.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            ActiveFilterChips(
                filters = parsedQuery.filters,
                negatedFilters = parsedQuery.negatedFilters,
                onRemoveFilter = { filter ->
                    // Remove filter from query text
                    val filterText = filter.toDisplayString()
                    val newQuery = query.replace(filterText, "").trim().replace(Regex("""\s+"""), " ")
                    onQueryChange(newQuery)
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        // Suggestions dropdown
        AnimatedVisibility(
            visible = showSuggestions && (suggestions.isNotEmpty() || query == "?"),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            SuggestionsDropdown(
                suggestions = if (query == "?") emptyList() else suggestions,
                showHelp = query == "?",
                onSuggestionClick = { suggestion ->
                    when (suggestion) {
                        is SearchSuggestion.FilterSyntax -> {
                            // Append or replace the last word with the syntax
                            val words = query.split(" ")
                            val newQuery = if (words.last().contains(":") || words.last().startsWith("@") || words.last().startsWith("#")) {
                                words.dropLast(1).joinToString(" ") + " " + suggestion.syntax
                            } else {
                                "$query ${suggestion.syntax}"
                            }.trim()
                            onQueryChange(newQuery)
                        }
                        is SearchSuggestion.RecentSearch -> {
                            onQueryChange(suggestion.query)
                        }
                        is SearchSuggestion.SavedSearch -> {
                            onQueryChange(suggestion.query)
                        }
                        is SearchSuggestion.TaskMatch -> {
                            // Could navigate to task or filter by task
                            onQueryChange(suggestion.task.title)
                        }
                    }
                },
                onShowHelp = onShowHelp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}

/**
 * Search input field with Material 3 styling.
 */
@Composable
private fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClear: () -> Unit,
    onShowHelp: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = if (isFocused) 2.dp else 0.dp,
            color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent
        ),
        shadowElevation = if (isFocused) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading search icon
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Input field
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { onFocusChanged(it.isFocused) }
                        .onPreviewKeyEvent { event ->
                            when {
                                event.type == KeyEventType.KeyDown && event.key == Key.Escape -> {
                                    onClear()
                                    true
                                }
                                else -> false
                            }
                        },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true
                )
            }
            
            // Trailing icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear button
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Help button
                IconButton(
                    onClick = onShowHelp,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Help",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Active filter chips displayed below search bar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveFilterChips(
    filters: List<SearchFilter>,
    negatedFilters: List<SearchFilter>,
    onRemoveFilter: (SearchFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Regular filters
        filters.forEach { filter ->
            FilterChip(
                selected = true,
                onClick = { onRemoveFilter(filter) },
                label = {
                    Text(
                        text = filter.toDisplayString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = true,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
        
        // Negated filters with different styling
        negatedFilters.forEach { filter ->
            FilterChip(
                selected = true,
                onClick = { onRemoveFilter(filter) },
                label = {
                    Text(
                        text = "-${filter.toDisplayString()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Negated",
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = true,
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    selectedBorderColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}

/**
 * Suggestions dropdown with different suggestion types.
 */
@Composable
private fun SuggestionsDropdown(
    suggestions: List<SearchSuggestion>,
    showHelp: Boolean,
    onSuggestionClick: (SearchSuggestion) -> Unit,
    onShowHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(max = 400.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        if (showHelp) {
            // Show help prompt
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Help",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Search Syntax Help",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Click the help icon (?) to see all available filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onShowHelp) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Show Full Help")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(suggestions) { suggestion ->
                    SuggestionItem(
                        suggestion = suggestion,
                        onClick = { onSuggestionClick(suggestion) }
                    )
                }
            }
        }
    }
}

/**
 * Individual suggestion item.
 */
@Composable
private fun SuggestionItem(
    suggestion: SearchSuggestion,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        when (suggestion) {
            is SearchSuggestion.FilterSyntax -> {
                FilterSyntaxSuggestion(suggestion)
            }
            is SearchSuggestion.RecentSearch -> {
                RecentSearchSuggestion(suggestion)
            }
            is SearchSuggestion.SavedSearch -> {
                SavedSearchSuggestion(suggestion)
            }
            is SearchSuggestion.TaskMatch -> {
                TaskMatchSuggestion(suggestion)
            }
        }
    }
}

@Composable
private fun FilterSyntaxSuggestion(suggestion: SearchSuggestion.FilterSyntax) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.syntax,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentSearchSuggestion(suggestion: SearchSuggestion.RecentSearch) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = suggestion.query,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SavedSearchSuggestion(suggestion: SearchSuggestion.SavedSearch) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = suggestion.query,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TaskMatchSuggestion(suggestion: SearchSuggestion.TaskMatch) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (suggestion.task.description.isNotEmpty()) {
                Text(
                    text = suggestion.task.description.take(50) + if (suggestion.task.description.length > 50) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Priority indicator
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(suggestion.task.priority.color).copy(alpha = 0.2f)
        ) {
            Text(
                text = suggestion.task.priority.label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(suggestion.task.priority.color),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
