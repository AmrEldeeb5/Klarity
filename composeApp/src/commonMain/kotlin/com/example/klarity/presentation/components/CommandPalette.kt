package com.example.klarity.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.klarity.presentation.screen.tasks.TaskViewMode

/**
 * Command Palette - Quick command execution and navigation
 * 
 * Inspired by Linear/Raycast command palettes. Provides keyboard-driven
 * interface for executing commands, navigating between screens, and
 * applying filters.
 * 
 * Features:
 * - Fuzzy search filtering
 * - Keyboard navigation (Arrow keys, Enter, Esc)
 * - Categorized commands
 * - Recent commands tracking
 * - Keyboard shortcuts display
 * - Dark overlay with blur effect
 * - Smooth animations
 * 
 * Usage:
 * ```kotlin
 * var isOpen by remember { mutableStateOf(false) }
 * 
 * CommandPalette(
 *     isOpen = isOpen,
 *     onDismiss = { isOpen = false },
 *     onCommandSelected = { command ->
 *         when (command) {
 *             is ActionCommand.NewTask -> { /* Handle new task */ }
 *             is NavigationCommand.GoToTasks -> { /* Navigate to tasks */ }
 *             // ... handle other commands
 *         }
 *     }
 * )
 * ```
 * 
 * @param isOpen Whether the command palette is visible
 * @param onDismiss Callback when the palette should be dismissed
 * @param onCommandSelected Callback when a command is selected
 * @param modifier Optional modifier for customization
 */
@Composable
fun CommandPalette(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onCommandSelected: (Command) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return
    
    // Search query state
    var searchQuery by remember { mutableStateOf("") }
    
    // Selected command index for keyboard navigation
    var selectedIndex by remember { mutableStateOf(0) }
    
    // Focus requester for auto-focusing search input
    val focusRequester = remember { FocusRequester() }
    
    // Get all available commands
    val allCommands = remember { getAllCommands() }
    
    // Filter commands based on search query
    val filteredCommands = remember(searchQuery, allCommands) {
        if (searchQuery.isBlank()) {
            allCommands
        } else {
            allCommands.filter { command ->
                fuzzyMatch(searchQuery, command.label) ||
                command.keywords.any { fuzzyMatch(searchQuery, it) }
            }
        }
    }
    
    // Group commands by category
    val groupedCommands = remember(filteredCommands) {
        filteredCommands.groupBy { it.category }
    }
    
    // Reset selected index when filtered commands change
    LaunchedEffect(filteredCommands) {
        selectedIndex = 0
    }
    
    // Auto-focus search input when opened
    LaunchedEffect(isOpen) {
        if (isOpen) {
            searchQuery = ""
            selectedIndex = 0
            focusRequester.requestFocus()
        }
    }
    
    // Dialog with custom properties for overlay effect
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            // Command palette surface
            AnimatedVisibility(
                visible = isOpen,
                enter = fadeIn(animationSpec = tween(200)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(150)) +
                        scaleOut(targetScale = 0.95f, animationSpec = tween(150))
            ) {
                Surface(
                    modifier = modifier
                        .padding(top = 120.dp)
                        .width(600.dp)
                        .heightIn(max = 500.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(
                            onClick = {},
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .onKeyEvent { keyEvent ->
                            handleKeyEvent(
                                keyEvent = keyEvent,
                                filteredCommands = filteredCommands,
                                selectedIndex = selectedIndex,
                                onSelectedIndexChange = { selectedIndex = it },
                                onCommandSelected = { command ->
                                    onCommandSelected(command)
                                    onDismiss()
                                },
                                onDismiss = onDismiss
                            )
                        }
                        .semantics {
                            contentDescription = "Command Palette"
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Search input
                        SearchInput(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            focusRequester = focusRequester,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        
                        // Command list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            if (filteredCommands.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No commands found",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                var currentIndex = 0
                                CommandCategory.entries.forEach { category ->
                                    val commandsInCategory = groupedCommands[category] ?: emptyList()
                                    if (commandsInCategory.isNotEmpty()) {
                                        // Category header
                                        item(key = "header_$category") {
                                            CategoryHeader(category = category)
                                        }
                                        
                                        // Commands in category
                                        itemsIndexed(
                                            items = commandsInCategory,
                                            key = { _, command -> command.id }
                                        ) { indexInCategory, command ->
                                            val isSelected = currentIndex == selectedIndex
                                            CommandItem(
                                                command = command,
                                                isSelected = isSelected,
                                                onClick = {
                                                    onCommandSelected(command)
                                                    onDismiss()
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            currentIndex++
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Footer with keyboard hints
                        CommandPaletteFooter()
                    }
                }
            }
        }
    }
}

/**
 * Search input field for filtering commands
 */
@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .focusRequester(focusRequester),
        placeholder = {
            Text(
                text = "Type a command or search...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Text(
                text = "🔍",
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Text(text = "✕", fontSize = 16.sp)
                }
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

/**
 * Category header for grouping commands
 */
@Composable
private fun CategoryHeader(
    category: CommandCategory,
    modifier: Modifier = Modifier
) {
    Text(
        text = category.displayName,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * Individual command item in the list
 */
@Composable
private fun CommandItem(
    command: Command,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            isHovered -> MaterialTheme.colorScheme.surfaceVariant
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 150),
        label = "commandItemBackground"
    )
    
    Row(
        modifier = modifier
            .background(backgroundColor)
            .hoverable(interactionSource = interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .semantics {
                contentDescription = "${command.label}, ${command.category.displayName}"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Command icon
            Text(
                text = command.icon,
                fontSize = 18.sp
            )
            
            // Command label
            Text(
                text = command.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Keyboard shortcut
        command.shortcut?.let { shortcut ->
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = shortcut,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Footer with keyboard navigation hints
 */
@Composable
private fun CommandPaletteFooter(
    modifier: Modifier = Modifier
) {
    Divider(color = MaterialTheme.colorScheme.outlineVariant)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KeyboardHint(keys = "↑↓", label = "Navigate")
        KeyboardHint(keys = "↵", label = "Select")
        KeyboardHint(keys = "Esc", label = "Close")
    }
}

/**
 * Individual keyboard hint display
 */
@Composable
private fun KeyboardHint(
    keys: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(0.dp)
        ) {
            Text(
                text = keys,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ============================================================================
// Command Data Models
// ============================================================================

/**
 * Command categories for organizing commands
 */
enum class CommandCategory(val displayName: String) {
    ACTIONS("Actions"),
    NAVIGATION("Navigation"),
    VIEWS("Views"),
    FILTERS("Filters")
}

/**
 * Base interface for all commands
 */
sealed interface Command {
    val id: String
    val label: String
    val icon: String
    val shortcut: String?
    val category: CommandCategory
    val keywords: List<String>
}

/**
 * Action commands for creating and manipulating content
 */
sealed class ActionCommand(
    override val id: String,
    override val label: String,
    override val icon: String,
    override val shortcut: String?,
    override val keywords: List<String>
) : Command {
    override val category = CommandCategory.ACTIONS
    
    data object NewTask : ActionCommand(
        id = "action.new_task",
        label = "New Task",
        icon = "📝",
        shortcut = "Cmd+N",
        keywords = listOf("create", "add", "task", "new")
    )
    
    data object NewNote : ActionCommand(
        id = "action.new_note",
        label = "New Note",
        icon = "📄",
        shortcut = "Cmd+Shift+N",
        keywords = listOf("create", "add", "note", "new", "document")
    )
    
    data object QuickCapture : ActionCommand(
        id = "action.quick_capture",
        label = "Quick Capture",
        icon = "⚡",
        shortcut = "Cmd+Q",
        keywords = listOf("quick", "capture", "add", "fast")
    )
    
    data object Refresh : ActionCommand(
        id = "action.refresh",
        label = "Refresh",
        icon = "🔄",
        shortcut = "Cmd+R",
        keywords = listOf("refresh", "reload", "sync")
    )
}

/**
 * Navigation commands for moving between screens
 */
sealed class NavigationCommand(
    override val id: String,
    override val label: String,
    override val icon: String,
    override val shortcut: String?,
    override val keywords: List<String>
) : Command {
    override val category = CommandCategory.NAVIGATION
    
    data object GoToTasks : NavigationCommand(
        id = "nav.tasks",
        label = "Go to Tasks",
        icon = "📊",
        shortcut = "Cmd+1",
        keywords = listOf("go", "navigate", "tasks", "kanban", "board")
    )
    
    data object GoToNotes : NavigationCommand(
        id = "nav.notes",
        label = "Go to Notes",
        icon = "📝",
        shortcut = "Cmd+2",
        keywords = listOf("go", "navigate", "notes", "documents")
    )
    
    data object GoToEditor : NavigationCommand(
        id = "nav.editor",
        label = "Go to Editor",
        icon = "✏️",
        shortcut = "Cmd+3",
        keywords = listOf("go", "navigate", "editor", "write")
    )
    
    data object GoToSettings : NavigationCommand(
        id = "nav.settings",
        label = "Go to Settings",
        icon = "⚙️",
        shortcut = "Cmd+,",
        keywords = listOf("go", "navigate", "settings", "preferences", "config")
    )
}

/**
 * View commands for switching between different view modes
 */
sealed class ViewCommand(
    override val id: String,
    override val label: String,
    override val icon: String,
    override val shortcut: String?,
    override val keywords: List<String>,
    val viewMode: TaskViewMode
) : Command {
    override val category = CommandCategory.VIEWS
    
    data object SwitchToKanban : ViewCommand(
        id = "view.kanban",
        label = "Switch to Kanban",
        icon = "📊",
        shortcut = null,
        keywords = listOf("switch", "view", "kanban", "board", "columns"),
        viewMode = TaskViewMode.KANBAN
    )
    
    data object SwitchToList : ViewCommand(
        id = "view.list",
        label = "Switch to List",
        icon = "📋",
        shortcut = null,
        keywords = listOf("switch", "view", "list", "tasks"),
        viewMode = TaskViewMode.LIST
    )
    
    data object SwitchToTimeline : ViewCommand(
        id = "view.timeline",
        label = "Switch to Timeline",
        icon = "📅",
        shortcut = null,
        keywords = listOf("switch", "view", "timeline", "gantt", "schedule"),
        viewMode = TaskViewMode.TIMELINE
    )
    
    data object SwitchToCalendar : ViewCommand(
        id = "view.calendar",
        label = "Switch to Calendar",
        icon = "🗓️",
        shortcut = null,
        keywords = listOf("switch", "view", "calendar", "month", "week"),
        viewMode = TaskViewMode.CALENDAR
    )
}

/**
 * Filter commands for filtering and sorting tasks
 */
sealed class FilterCommand(
    override val id: String,
    override val label: String,
    override val icon: String,
    override val shortcut: String?,
    override val keywords: List<String>
) : Command {
    override val category = CommandCategory.FILTERS
    
    data object FilterByPriority : FilterCommand(
        id = "filter.priority",
        label = "Filter by Priority",
        icon = "🔴",
        shortcut = null,
        keywords = listOf("filter", "priority", "high", "medium", "low")
    )
    
    data object FilterByStatus : FilterCommand(
        id = "filter.status",
        label = "Filter by Status",
        icon = "📌",
        shortcut = null,
        keywords = listOf("filter", "status", "todo", "progress", "done")
    )
    
    data object FilterByTag : FilterCommand(
        id = "filter.tag",
        label = "Filter by Tag",
        icon = "🏷️",
        shortcut = null,
        keywords = listOf("filter", "tag", "label", "category")
    )
    
    data object ClearFilters : FilterCommand(
        id = "filter.clear",
        label = "Clear Filters",
        icon = "✕",
        shortcut = null,
        keywords = listOf("clear", "reset", "remove", "filters")
    )
}

// ============================================================================
// Helper Functions
// ============================================================================

/**
 * Returns all available commands
 */
private fun getAllCommands(): List<Command> = buildList {
    // Actions
    add(ActionCommand.NewTask)
    add(ActionCommand.NewNote)
    add(ActionCommand.QuickCapture)
    add(ActionCommand.Refresh)
    
    // Navigation
    add(NavigationCommand.GoToTasks)
    add(NavigationCommand.GoToNotes)
    add(NavigationCommand.GoToEditor)
    add(NavigationCommand.GoToSettings)
    
    // Views
    add(ViewCommand.SwitchToKanban)
    add(ViewCommand.SwitchToList)
    add(ViewCommand.SwitchToTimeline)
    add(ViewCommand.SwitchToCalendar)
    
    // Filters
    add(FilterCommand.FilterByPriority)
    add(FilterCommand.FilterByStatus)
    add(FilterCommand.FilterByTag)
    add(FilterCommand.ClearFilters)
}

/**
 * Simple fuzzy matching algorithm
 * Returns true if all characters in the query appear in order in the target string
 */
private fun fuzzyMatch(query: String, target: String): Boolean {
    if (query.isBlank()) return true
    
    val queryLower = query.lowercase()
    val targetLower = target.lowercase()
    
    var queryIndex = 0
    var targetIndex = 0
    
    while (queryIndex < queryLower.length && targetIndex < targetLower.length) {
        if (queryLower[queryIndex] == targetLower[targetIndex]) {
            queryIndex++
        }
        targetIndex++
    }
    
    return queryIndex == queryLower.length
}

/**
 * Handles keyboard events for navigation and selection
 */
private fun handleKeyEvent(
    keyEvent: KeyEvent,
    filteredCommands: List<Command>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    onCommandSelected: (Command) -> Unit,
    onDismiss: () -> Unit
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false
    
    when (keyEvent.key) {
        Key.Escape -> {
            onDismiss()
            return true
        }
        Key.DirectionUp -> {
            if (filteredCommands.isNotEmpty()) {
                val newIndex = (selectedIndex - 1).coerceAtLeast(0)
                onSelectedIndexChange(newIndex)
            }
            return true
        }
        Key.DirectionDown -> {
            if (filteredCommands.isNotEmpty()) {
                val newIndex = (selectedIndex + 1).coerceAtMost(filteredCommands.size - 1)
                onSelectedIndexChange(newIndex)
            }
            return true
        }
        Key.Enter -> {
            if (filteredCommands.isNotEmpty() && selectedIndex < filteredCommands.size) {
                onCommandSelected(filteredCommands[selectedIndex])
            }
            return true
        }
    }
    
    return false
}
