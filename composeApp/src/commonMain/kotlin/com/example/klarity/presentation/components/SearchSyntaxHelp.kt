package com.example.klarity.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Search Syntax Help Dialog
 * 
 * Displays comprehensive guide for search filter syntax with examples.
 * Similar to ShortcutsHelpDialog with search functionality and copy buttons.
 */

@Composable
fun SearchSyntaxHelpDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(visible) {
        if (visible) {
            searchQuery = ""
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .width(800.dp)
                .heightIn(max = 700.dp)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                        onDismiss()
                        true
                    } else {
                        false
                    }
                },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                SyntaxHelpHeader(onClose = onDismiss)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search bar
                SyntaxHelpSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Syntax list
                SyntaxHelpList(
                    searchQuery = searchQuery,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SyntaxHelpHeader(onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Search Syntax Guide",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Master the advanced search filters",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SyntaxHelpSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Search syntax...",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    )
}

@Composable
private fun SyntaxHelpList(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val syntaxItems = remember { getSyntaxHelpItems() }
    
    val filteredItems = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            syntaxItems
        } else {
            syntaxItems.mapNotNull { category ->
                val filteredEntries = category.entries.filter {
                    it.syntax.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.example.contains(searchQuery, ignoreCase = true)
                }
                if (filteredEntries.isNotEmpty()) {
                    category.copy(entries = filteredEntries)
                } else {
                    null
                }
            }
        }
    }
    
    if (filteredItems.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "No syntax found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Try a different search term",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        return
    }
    
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        filteredItems.forEach { category ->
            item(key = "category_${category.title}") {
                SyntaxCategorySection(category = category)
            }
        }
        
        // Add bottom padding
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SyntaxCategorySection(category: SyntaxCategory) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category header with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Syntax entries
        category.entries.forEach { entry ->
            SyntaxEntryRow(entry = entry)
        }
    }
}

@Composable
private fun SyntaxEntryRow(entry: SyntaxEntry) {
    val clipboardManager = LocalClipboardManager.current
    var showCopied by remember { mutableStateOf(false) }
    
    LaunchedEffect(showCopied) {
        if (showCopied) {
            kotlinx.coroutines.delay(1500)
            showCopied = false
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Syntax with code styling
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = entry.syntax,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Description
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Example
                if (entry.example.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Example:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = entry.example,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Copy button
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(entry.syntax))
                    showCopied = true
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (showCopied) Icons.Default.Check else Icons.Default.Star,
                    contentDescription = "Copy syntax",
                    tint = if (showCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ============================================================================
// Data Models
// ============================================================================

private data class SyntaxCategory(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val entries: List<SyntaxEntry>
)

private data class SyntaxEntry(
    val syntax: String,
    val description: String,
    val example: String = ""
)

/**
 * Returns all syntax help categories with entries.
 */
private fun getSyntaxHelpItems(): List<SyntaxCategory> {
    return listOf(
        SyntaxCategory(
            title = "Status Filters",
            icon = Icons.Default.List,
            entries = listOf(
                SyntaxEntry(
                    syntax = "status:todo",
                    description = "Tasks with TODO status",
                    example = "status:todo priority:high"
                ),
                SyntaxEntry(
                    syntax = "status:in_progress",
                    description = "Tasks in progress",
                    example = "status:in_progress @john"
                ),
                SyntaxEntry(
                    syntax = "status:in_review",
                    description = "Tasks in review",
                    example = "status:in_review"
                ),
                SyntaxEntry(
                    syntax = "status:done",
                    description = "Completed tasks",
                    example = "status:done created:today"
                ),
                SyntaxEntry(
                    syntax = "status:backlog",
                    description = "Backlog tasks",
                    example = "status:backlog"
                ),
                SyntaxEntry(
                    syntax = "status:archived",
                    description = "Archived tasks",
                    example = "status:archived"
                ),
                SyntaxEntry(
                    syntax = "is:done",
                    description = "Completed tasks (shortcut)",
                    example = "is:done due:today"
                )
            )
        ),
        SyntaxCategory(
            title = "Priority Filters",
            icon = Icons.Default.Warning,
            entries = listOf(
                SyntaxEntry(
                    syntax = "priority:high",
                    description = "High priority tasks",
                    example = "priority:high -status:done"
                ),
                SyntaxEntry(
                    syntax = "priority:medium",
                    description = "Medium priority tasks",
                    example = "priority:medium @sarah"
                ),
                SyntaxEntry(
                    syntax = "priority:low",
                    description = "Low priority tasks",
                    example = "priority:low"
                ),
                SyntaxEntry(
                    syntax = "is:urgent",
                    description = "High priority (shortcut)",
                    example = "is:urgent status:todo"
                )
            )
        ),
        SyntaxCategory(
            title = "Assignee Filters",
            icon = Icons.Default.Person,
            entries = listOf(
                SyntaxEntry(
                    syntax = "@username",
                    description = "Assigned to username",
                    example = "@john priority:high"
                ),
                SyntaxEntry(
                    syntax = "assignee:username",
                    description = "Assigned to username (alternative)",
                    example = "assignee:sarah #frontend"
                ),
                SyntaxEntry(
                    syntax = "is:unassigned",
                    description = "Tasks without assignee",
                    example = "is:unassigned priority:high"
                )
            )
        ),
        SyntaxCategory(
            title = "Tag Filters",
            icon = Icons.Default.Star,
            entries = listOf(
                SyntaxEntry(
                    syntax = "#tag",
                    description = "Tagged with 'tag'",
                    example = "#bug priority:high"
                ),
                SyntaxEntry(
                    syntax = "tag:frontend",
                    description = "Tagged with 'frontend'",
                    example = "tag:frontend status:todo"
                )
            )
        ),
        SyntaxCategory(
            title = "Due Date Filters",
            icon = Icons.Default.DateRange,
            entries = listOf(
                SyntaxEntry(
                    syntax = "due:today",
                    description = "Due today",
                    example = "due:today -status:done"
                ),
                SyntaxEntry(
                    syntax = "due:tomorrow",
                    description = "Due tomorrow",
                    example = "due:tomorrow"
                ),
                SyntaxEntry(
                    syntax = "due:this_week",
                    description = "Due this week",
                    example = "due:this_week @john"
                ),
                SyntaxEntry(
                    syntax = "due:overdue",
                    description = "Past due date",
                    example = "due:overdue priority:high"
                ),
                SyntaxEntry(
                    syntax = "is:overdue",
                    description = "Past due date (shortcut)",
                    example = "is:overdue"
                ),
                SyntaxEntry(
                    syntax = "is:due_soon",
                    description = "Due within 3 days",
                    example = "is:due_soon -is:done"
                )
            )
        ),
        SyntaxCategory(
            title = "Date Filters",
            icon = Icons.Default.DateRange,
            entries = listOf(
                SyntaxEntry(
                    syntax = "created:today",
                    description = "Created today",
                    example = "created:today"
                ),
                SyntaxEntry(
                    syntax = "created:this_week",
                    description = "Created this week",
                    example = "created:this_week"
                ),
                SyntaxEntry(
                    syntax = "updated:today",
                    description = "Updated today",
                    example = "updated:today"
                ),
                SyntaxEntry(
                    syntax = "updated:yesterday",
                    description = "Updated yesterday",
                    example = "updated:yesterday"
                )
            )
        ),
        SyntaxCategory(
            title = "Task Properties",
            icon = Icons.Default.Build,
            entries = listOf(
                SyntaxEntry(
                    syntax = "has:subtasks",
                    description = "Tasks with subtasks",
                    example = "has:subtasks status:in_progress"
                ),
                SyntaxEntry(
                    syntax = "has:notes",
                    description = "Tasks with linked notes",
                    example = "has:notes"
                ),
                SyntaxEntry(
                    syntax = "is:active",
                    description = "Tasks with timer running",
                    example = "is:active"
                ),
                SyntaxEntry(
                    syntax = "is:tracked",
                    description = "Tasks with timer data",
                    example = "is:tracked @john"
                ),
                SyntaxEntry(
                    syntax = "is:parent",
                    description = "Tasks with subtasks (alias)",
                    example = "is:parent"
                )
            )
        ),
        SyntaxCategory(
            title = "Advanced Operators",
            icon = Icons.Default.Settings,
            entries = listOf(
                SyntaxEntry(
                    syntax = "-filter",
                    description = "Negation - exclude matching items",
                    example = "-status:done -is:archived"
                ),
                SyntaxEntry(
                    syntax = "filter1 OR filter2",
                    description = "OR logic - either condition matches",
                    example = "priority:high OR priority:urgent"
                ),
                SyntaxEntry(
                    syntax = "filter1 filter2",
                    description = "AND logic (default) - all conditions match",
                    example = "priority:high @john -status:done"
                )
            )
        ),
        SyntaxCategory(
            title = "Combination Examples",
            icon = Icons.Default.Info,
            entries = listOf(
                SyntaxEntry(
                    syntax = "fix bug priority:high",
                    description = "Text search + priority filter",
                    example = "Search for 'fix bug' with high priority"
                ),
                SyntaxEntry(
                    syntax = "@john status:todo -is:archived",
                    description = "Multiple filters with negation",
                    example = "John's TODO tasks, excluding archived"
                ),
                SyntaxEntry(
                    syntax = "due:today priority:high OR priority:urgent",
                    description = "Complex query with OR logic",
                    example = "Today's high/urgent tasks"
                ),
                SyntaxEntry(
                    syntax = "#bug is:overdue @sarah",
                    description = "Tag + status + assignee",
                    example = "Sarah's overdue bugs"
                )
            )
        )
    )
}
