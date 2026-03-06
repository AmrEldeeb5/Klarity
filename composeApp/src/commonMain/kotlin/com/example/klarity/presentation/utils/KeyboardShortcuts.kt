package com.example.klarity.presentation.utils

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.*

/**
 * Keyboard Shortcuts System
 * 
 * Provides a comprehensive keyboard shortcuts system for power users
 * to navigate and control the Klarity app efficiently.
 */

// ============================================================================
// Data Models
// ============================================================================

/**
 * Represents a single keyboard shortcut with its associated action.
 */
data class KeyboardShortcut(
    val key: Key,
    val modifiers: Set<KeyModifier> = emptySet(),
    val action: ShortcutAction,
    val description: String,
    val category: ShortcutCategory
)

/**
 * Keyboard modifiers for shortcuts.
 */
enum class KeyModifier {
    CTRL,
    ALT,
    SHIFT,
    META  // Command key on macOS, Windows key on Windows
}

/**
 * Categories of keyboard shortcuts for grouping in help dialog.
 */
enum class ShortcutCategory(val label: String) {
    GLOBAL("Global"),
    NAVIGATION("Navigation"),
    TASK_ACTIONS("Task Actions"),
    KANBAN_NAVIGATION("Kanban Navigation"),
    FILTERS("Filters"),
    EDITING("Editing"),
    VIEWS("Views")
}

/**
 * All possible shortcut actions in the app.
 */
sealed class ShortcutAction {
    // Global actions
    object OpenCommandPalette : ShortcutAction()
    object NewTask : ShortcutAction()
    object ShowShortcutsHelp : ShortcutAction()
    object FocusSearch : ShortcutAction()
    object OpenSettings : ShortcutAction()
    object CloseModal : ShortcutAction()
    
    // Navigation actions
    data class SwitchView(val mode: String) : ShortcutAction()
    
    // Task actions (when task is selected)
    object EditTask : ShortcutAction()
    object DeleteTask : ShortcutAction()
    object CopyTask : ShortcutAction()
    object CutTask : ShortcutAction()
    object PasteTask : ShortcutAction()
    object MoveTask : ShortcutAction()
    data class SetPriority(val priority: String) : ShortcutAction()
    object OpenTaskDetails : ShortcutAction()
    
    // Kanban navigation
    object NextColumn : ShortcutAction()
    object PreviousColumn : ShortcutAction()
    object NavigateUp : ShortcutAction()
    object NavigateDown : ShortcutAction()
    object NavigateLeft : ShortcutAction()
    object NavigateRight : ShortcutAction()
    object QuickPreview : ShortcutAction()
    
    // Filters
    object OpenFilterPanel : ShortcutAction()
    object FilterByPriority : ShortcutAction()
    object FilterByStatus : ShortcutAction()
    object ClearFilters : ShortcutAction()
    
    // Editing
    object ToggleComplete : ShortcutAction()
    object StartTimer : ShortcutAction()
    object AddTag : ShortcutAction()
}

// ============================================================================
// Shortcuts Registry
// ============================================================================

/**
 * Central registry of all keyboard shortcuts in the application.
 */
object ShortcutsRegistry {
    
    val all: List<KeyboardShortcut> = listOf(
        // ====================================================================
        // Global Shortcuts (6)
        // ====================================================================
        KeyboardShortcut(
            key = Key.K,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.OpenCommandPalette,
            description = "Open command palette",
            category = ShortcutCategory.GLOBAL
        ),
        KeyboardShortcut(
            key = Key.N,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.NewTask,
            description = "Create new task",
            category = ShortcutCategory.GLOBAL
        ),
        KeyboardShortcut(
            key = Key.F,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.FocusSearch,
            description = "Focus search",
            category = ShortcutCategory.GLOBAL
        ),
        KeyboardShortcut(
            key = Key.Slash,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.ShowShortcutsHelp,
            description = "Show keyboard shortcuts",
            category = ShortcutCategory.GLOBAL
        ),
        KeyboardShortcut(
            key = Key.Comma,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.OpenSettings,
            description = "Open settings",
            category = ShortcutCategory.GLOBAL
        ),
        KeyboardShortcut(
            key = Key.Escape,
            modifiers = emptySet(),
            action = ShortcutAction.CloseModal,
            description = "Close modal or dialog",
            category = ShortcutCategory.GLOBAL
        ),
        
        // ====================================================================
        // Navigation Shortcuts (4)
        // ====================================================================
        KeyboardShortcut(
            key = Key.One,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.SwitchView("KANBAN"),
            description = "Switch to Kanban view",
            category = ShortcutCategory.NAVIGATION
        ),
        KeyboardShortcut(
            key = Key.Two,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.SwitchView("LIST"),
            description = "Switch to List view",
            category = ShortcutCategory.NAVIGATION
        ),
        KeyboardShortcut(
            key = Key.Three,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.SwitchView("TIMELINE"),
            description = "Switch to Timeline view",
            category = ShortcutCategory.NAVIGATION
        ),
        KeyboardShortcut(
            key = Key.Four,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.SwitchView("CALENDAR"),
            description = "Switch to Calendar view",
            category = ShortcutCategory.NAVIGATION
        ),
        
        // ====================================================================
        // Task Actions (10)
        // ====================================================================
        KeyboardShortcut(
            key = Key.E,
            modifiers = emptySet(),
            action = ShortcutAction.EditTask,
            description = "Edit selected task",
            category = ShortcutCategory.TASK_ACTIONS
        ),
        KeyboardShortcut(
            key = Key.D,
            modifiers = emptySet(),
            action = ShortcutAction.DeleteTask,
            description = "Delete selected task",
            category = ShortcutCategory.TASK_ACTIONS
        ),
        KeyboardShortcut(
            key = Key.C,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.CopyTask,
            description = "Copy selected task",
            category = ShortcutCategory.TASK_ACTIONS
        ),
        KeyboardShortcut(
            key = Key.X,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.CutTask,
            description = "Cut selected task",
            category = ShortcutCategory.TASK_ACTIONS
        ),
        KeyboardShortcut(
            key = Key.V,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.PasteTask,
            description = "Paste task",
            category = ShortcutCategory.TASK_ACTIONS
        ),
        KeyboardShortcut(
            key = Key.M,
            modifiers = emptySet(),
            action = ShortcutAction.MoveTask,
            description = "Move selected task",
            category = ShortcutCategory.TASK_ACTIONS
        ),
        KeyboardShortcut(
            key = Key.One,
            modifiers = emptySet(),
            action = ShortcutAction.SetPriority("HIGH"),
            description = "Set priority to High",
            category = ShortcutCategory.TASK_ACTIONS
        ),
        KeyboardShortcut(
            key = Key.Two,
            modifiers = emptySet(),
            action = ShortcutAction.SetPriority("MEDIUM"),
            description = "Set priority to Medium",
            category = ShortcutCategory.TASK_ACTIONS
        ),
        KeyboardShortcut(
            key = Key.Three,
            modifiers = emptySet(),
            action = ShortcutAction.SetPriority("LOW"),
            description = "Set priority to Low",
            category = ShortcutCategory.TASK_ACTIONS
        ),
        KeyboardShortcut(
            key = Key.Enter,
            modifiers = emptySet(),
            action = ShortcutAction.OpenTaskDetails,
            description = "Open task details",
            category = ShortcutCategory.TASK_ACTIONS
        ),
        
        // ====================================================================
        // Kanban Navigation (6)
        // ====================================================================
        KeyboardShortcut(
            key = Key.Tab,
            modifiers = emptySet(),
            action = ShortcutAction.NextColumn,
            description = "Move to next column",
            category = ShortcutCategory.KANBAN_NAVIGATION
        ),
        KeyboardShortcut(
            key = Key.Tab,
            modifiers = setOf(KeyModifier.SHIFT),
            action = ShortcutAction.PreviousColumn,
            description = "Move to previous column",
            category = ShortcutCategory.KANBAN_NAVIGATION
        ),
        KeyboardShortcut(
            key = Key.DirectionUp,
            modifiers = emptySet(),
            action = ShortcutAction.NavigateUp,
            description = "Navigate to task above",
            category = ShortcutCategory.KANBAN_NAVIGATION
        ),
        KeyboardShortcut(
            key = Key.DirectionDown,
            modifiers = emptySet(),
            action = ShortcutAction.NavigateDown,
            description = "Navigate to task below",
            category = ShortcutCategory.KANBAN_NAVIGATION
        ),
        KeyboardShortcut(
            key = Key.DirectionLeft,
            modifiers = emptySet(),
            action = ShortcutAction.NavigateLeft,
            description = "Navigate to previous column",
            category = ShortcutCategory.KANBAN_NAVIGATION
        ),
        KeyboardShortcut(
            key = Key.DirectionRight,
            modifiers = emptySet(),
            action = ShortcutAction.NavigateRight,
            description = "Navigate to next column",
            category = ShortcutCategory.KANBAN_NAVIGATION
        ),
        KeyboardShortcut(
            key = Key.Spacebar,
            modifiers = emptySet(),
            action = ShortcutAction.QuickPreview,
            description = "Quick preview task",
            category = ShortcutCategory.KANBAN_NAVIGATION
        ),
        
        // ====================================================================
        // Filters (4)
        // ====================================================================
        KeyboardShortcut(
            key = Key.F,
            modifiers = setOf(KeyModifier.META, KeyModifier.SHIFT),
            action = ShortcutAction.OpenFilterPanel,
            description = "Open filter panel",
            category = ShortcutCategory.FILTERS
        ),
        KeyboardShortcut(
            key = Key.P,
            modifiers = setOf(KeyModifier.META, KeyModifier.SHIFT),
            action = ShortcutAction.FilterByPriority,
            description = "Filter by priority",
            category = ShortcutCategory.FILTERS
        ),
        KeyboardShortcut(
            key = Key.S,
            modifiers = setOf(KeyModifier.META, KeyModifier.SHIFT),
            action = ShortcutAction.FilterByStatus,
            description = "Filter by status",
            category = ShortcutCategory.FILTERS
        ),
        KeyboardShortcut(
            key = Key.X,
            modifiers = setOf(KeyModifier.META, KeyModifier.SHIFT),
            action = ShortcutAction.ClearFilters,
            description = "Clear all filters",
            category = ShortcutCategory.FILTERS
        ),
        
        // ====================================================================
        // Editing (3)
        // ====================================================================
        KeyboardShortcut(
            key = Key.Enter,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.ToggleComplete,
            description = "Toggle task completion",
            category = ShortcutCategory.EDITING
        ),
        KeyboardShortcut(
            key = Key.T,
            modifiers = setOf(KeyModifier.META),
            action = ShortcutAction.StartTimer,
            description = "Start/stop timer",
            category = ShortcutCategory.EDITING
        ),
        KeyboardShortcut(
            key = Key.T,
            modifiers = setOf(KeyModifier.META, KeyModifier.SHIFT),
            action = ShortcutAction.AddTag,
            description = "Add tag to task",
            category = ShortcutCategory.EDITING
        )
    )
    
    /**
     * Finds a keyboard shortcut by key and modifiers.
     * 
     * @param key The key that was pressed
     * @param modifiers The set of active modifiers
     * @return The matching KeyboardShortcut or null if not found
     */
    fun findByKey(key: Key, modifiers: Set<KeyModifier>): KeyboardShortcut? {
        return all.firstOrNull { shortcut ->
            shortcut.key == key && shortcut.modifiers == modifiers
        }
    }
    
    /**
     * Groups all shortcuts by category for display in help dialog.
     */
    fun groupedByCategory(): Map<ShortcutCategory, List<KeyboardShortcut>> {
        return all.groupBy { it.category }
    }
    
    /**
     * Searches shortcuts by description.
     * 
     * @param query The search query
     * @return List of matching shortcuts
     */
    fun search(query: String): List<KeyboardShortcut> {
        if (query.isBlank()) return all
        val lowerQuery = query.lowercase()
        return all.filter { 
            it.description.lowercase().contains(lowerQuery) ||
            it.category.label.lowercase().contains(lowerQuery)
        }
    }
}

// ============================================================================
// Keyboard Event Handling
// ============================================================================

/**
 * Converts a KeyEvent to a set of active modifiers.
 */
fun KeyEvent.toModifiers(): Set<KeyModifier> {
    return buildSet {
        if (isCtrlPressed) add(KeyModifier.CTRL)
        if (isAltPressed) add(KeyModifier.ALT)
        if (isShiftPressed) add(KeyModifier.SHIFT)
        if (isMetaPressed) add(KeyModifier.META)
    }
}

/**
 * Composable that handles keyboard shortcuts throughout the app.
 * 
 * @param enabled Whether keyboard shortcuts are currently enabled
 * @param onAction Callback when a shortcut action is triggered
 */
@Composable
fun KeyboardShortcutHandler(
    enabled: Boolean = true,
    onAction: (ShortcutAction) -> Unit,
    content: @Composable () -> Unit
) {
    // Note: In Compose Multiplatform, we need to implement this at a higher level
    // using focus modifiers. This is a placeholder for the integration pattern.
    // The actual implementation should be done in the root composable using
    // Modifier.onPreviewKeyEvent or similar.
    content()
}

/**
 * Creates a key event handler that processes keyboard shortcuts.
 * Use this with Modifier.onPreviewKeyEvent at the root of your UI.
 * 
 * @param enabled Whether shortcuts are enabled
 * @param onAction Callback when a shortcut is triggered
 * @return true if the event was consumed, false otherwise
 */
fun createKeyEventHandler(
    enabled: Boolean,
    onAction: (ShortcutAction) -> Unit
): (KeyEvent) -> Boolean {
    return { event ->
        if (enabled && event.type == KeyEventType.KeyDown) {
            val modifiers = event.toModifiers()
            val shortcut = ShortcutsRegistry.findByKey(event.key, modifiers)
            
            if (shortcut != null) {
                onAction(shortcut.action)
                true // Consume the event
            } else {
                false // Let it propagate
            }
        } else {
            false
        }
    }
}
