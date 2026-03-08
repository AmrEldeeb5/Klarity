package com.example.klarity.presentation.examples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import com.example.klarity.presentation.components.ShortcutsHelpDialog
import com.example.klarity.presentation.screen.tasks.*
import com.example.klarity.presentation.state.TasksUiEvent
import com.example.klarity.presentation.utils.*
import com.example.klarity.presentation.viewmodel.TasksViewModel

/**
 * Example Integration: Keyboard Shortcuts System
 * 
 * This file demonstrates how to integrate the keyboard shortcuts system
 * into your application. Copy this pattern to your App.kt or main screen.
 */

@Composable
fun KeyboardShortcutsIntegrationExample(
    viewModel: TasksViewModel
) {
    // State for shortcuts help dialog
    var showShortcutsDialog by remember { mutableStateOf(false) }
    
    // State for selected task (for task-specific shortcuts)
    var selectedTaskId by remember { mutableStateOf<String?>(null) }
    
    // State for filter panel
    var showFilterPanel by remember { mutableStateOf(false) }
    
    // Create the keyboard event handler
    val keyEventHandler = remember(selectedTaskId, showFilterPanel) {
        createKeyEventHandler(
            enabled = true,
            onAction = { action ->
                handleKeyboardShortcut(
                    action = action,
                    viewModel = viewModel,
                    selectedTaskId = selectedTaskId,
                    showShortcutsDialog = { showShortcutsDialog = it },
                    showFilterPanel = { showFilterPanel = it }
                )
            }
        )
    }
    
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onPreviewKeyEvent(keyEventHandler) // Add keyboard handling here
        ) {
            // Your main content
            TasksScreen(
                viewModel = viewModel
            )
            
            // Shortcuts help dialog
            ShortcutsHelpDialog(
                visible = showShortcutsDialog,
                onDismiss = { showShortcutsDialog = false }
            )
        }
    }
}

/**
 * Handles all keyboard shortcut actions.
 * 
 * This is the central handler that converts shortcut actions into
 * actual app behavior. Customize this to match your app's needs.
 */
private fun handleKeyboardShortcut(
    action: ShortcutAction,
    viewModel: TasksViewModel,
    selectedTaskId: String?,
    showShortcutsDialog: (Boolean) -> Unit,
    showFilterPanel: (Boolean) -> Unit
) {
    when (action) {
        // ====================================================================
        // Global Actions
        // ====================================================================
        is ShortcutAction.ShowShortcutsHelp -> {
            showShortcutsDialog(true)
        }
        
        is ShortcutAction.CloseModal -> {
            // Close any open modals
            showShortcutsDialog(false)
            viewModel.onEvent(TasksUiEvent.ModalClosed)
        }
        
        is ShortcutAction.NewTask -> {
            viewModel.onEvent(TasksUiEvent.TaskCreated(TaskStatus.TODO))
        }
        
        is ShortcutAction.FocusSearch -> {
            // TODO: Focus search field
            // You'll need to manage focus state for this
        }
        
        is ShortcutAction.OpenCommandPalette -> {
            // TODO: Show command palette if you have one
        }
        
        is ShortcutAction.OpenSettings -> {
            // TODO: Navigate to settings
        }
        
        // ====================================================================
        // Navigation Actions
        // ====================================================================
        is ShortcutAction.SwitchView -> {
            val viewMode = when (action.mode) {
                "KANBAN" -> TaskViewMode.KANBAN
                "LIST" -> TaskViewMode.LIST
                "TIMELINE" -> TaskViewMode.TIMELINE
                "CALENDAR" -> TaskViewMode.CALENDAR
                else -> return
            }
            viewModel.onEvent(TasksUiEvent.ViewModeChanged(viewMode))
        }
        
        // ====================================================================
        // Task Actions (require selected task)
        // ====================================================================
        is ShortcutAction.EditTask -> {
            // TODO: Open edit dialog for selected task
            // selectedTaskId?.let { id ->
            //     viewModel.onEvent(TasksUiEvent.EditTaskRequested(id))
            // }
        }
        
        is ShortcutAction.DeleteTask -> {
            // TODO: Delete selected task
            // selectedTaskId?.let { id ->
            //     viewModel.onEvent(TasksUiEvent.TaskDeleted(id))
            // }
        }
        
        is ShortcutAction.SetPriority -> {
            val priority = when (action.priority) {
                "HIGH" -> TaskPriority.HIGH
                "MEDIUM" -> TaskPriority.MEDIUM
                "LOW" -> TaskPriority.LOW
                else -> return
            }
            // TODO: Update selected task priority
            // selectedTaskId?.let { id ->
            //     viewModel.onEvent(TasksUiEvent.TaskPriorityChanged(id, priority))
            // }
        }
        
        is ShortcutAction.OpenTaskDetails -> {
            // TODO: Open task details for selected task
            // selectedTaskId?.let { id ->
            //     viewModel.onEvent(TasksUiEvent.TaskClicked(id))
            // }
        }
        
        is ShortcutAction.CopyTask -> {
            // TODO: Copy selected task to clipboard
        }
        
        is ShortcutAction.CutTask -> {
            // TODO: Cut selected task
        }
        
        is ShortcutAction.PasteTask -> {
            // TODO: Paste task from clipboard
        }
        
        is ShortcutAction.MoveTask -> {
            // TODO: Show move task dialog
        }
        
        // ====================================================================
        // Kanban Navigation
        // ====================================================================
        is ShortcutAction.NavigateUp,
        is ShortcutAction.NavigateDown,
        is ShortcutAction.NavigateLeft,
        is ShortcutAction.NavigateRight -> {
            // TODO: Implement keyboard navigation in Kanban view
            // This requires maintaining focus state for tasks
        }
        
        is ShortcutAction.NextColumn -> {
            // TODO: Move focus to next column
        }
        
        is ShortcutAction.PreviousColumn -> {
            // TODO: Move focus to previous column
        }
        
        is ShortcutAction.QuickPreview -> {
            // TODO: Show quick preview of selected task
        }
        
        // ====================================================================
        // Filters
        // ====================================================================
        is ShortcutAction.OpenFilterPanel -> {
            showFilterPanel(true)
        }
        
        is ShortcutAction.FilterByPriority -> {
            // TODO: Show priority filter dropdown
        }
        
        is ShortcutAction.FilterByStatus -> {
            // TODO: Show status filter dropdown
        }
        
        is ShortcutAction.ClearFilters -> {
            viewModel.onEvent(TasksUiEvent.FilterChanged(TaskFilter()))
            showFilterPanel(false)
        }
        
        // ====================================================================
        // Editing
        // ====================================================================
        is ShortcutAction.ToggleComplete -> {
            selectedTaskId?.let { id ->
                viewModel.onEvent(TasksUiEvent.TaskToggleComplete(id))
            }
        }
        
        is ShortcutAction.StartTimer -> {
            // TODO: Start/stop timer for selected task
        }
        
        is ShortcutAction.AddTag -> {
            // TODO: Show add tag dialog for selected task
        }
    }
}

/**
 * Alternative: Simple Integration
 * 
 * If you don't need all the features, here's a simpler version
 * that just handles the essentials.
 */
@Composable
fun SimpleKeyboardShortcutsExample(
    viewModel: TasksViewModel
) {
    var showShortcutsDialog by remember { mutableStateOf(false) }
    
    val keyEventHandler = remember {
        createKeyEventHandler(
            enabled = true,
            onAction = { action ->
                when (action) {
                    is ShortcutAction.ShowShortcutsHelp -> showShortcutsDialog = true
                    is ShortcutAction.CloseModal -> showShortcutsDialog = false
                    is ShortcutAction.NewTask -> {
                        viewModel.onEvent(TasksUiEvent.TaskCreated(TaskStatus.TODO))
                    }
                    is ShortcutAction.SwitchView -> {
                        val mode = when (action.mode) {
                            "KANBAN" -> TaskViewMode.KANBAN
                            "LIST" -> TaskViewMode.LIST
                            "TIMELINE" -> TaskViewMode.TIMELINE
                            "CALENDAR" -> TaskViewMode.CALENDAR
                            else -> return@createKeyEventHandler
                        }
                        viewModel.onEvent(TasksUiEvent.ViewModeChanged(mode))
                    }
                    else -> {} // Ignore other shortcuts for now
                }
            }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent(keyEventHandler)
    ) {
        TasksScreen(viewModel = viewModel)
        
        ShortcutsHelpDialog(
            visible = showShortcutsDialog,
            onDismiss = { showShortcutsDialog = false }
        )
    }
}

/**
 * Tips for Integration:
 * 
 * 1. Place the keyboard handler at the highest level possible
 *    (e.g., in App.kt or your main screen) so it catches all events.
 * 
 * 2. Use `onPreviewKeyEvent` instead of `onKeyEvent` to intercept
 *    events before they reach children (like text fields).
 * 
 * 3. Disable shortcuts when text fields are focused:
 *    ```
 *    var isTextFieldFocused by remember { mutableStateOf(false) }
 *    createKeyEventHandler(enabled = !isTextFieldFocused, ...)
 *    ```
 * 
 * 4. For task-specific shortcuts (Edit, Delete, etc.), you'll need
 *    to track which task is currently selected. This typically means
 *    adding a "selected task" concept to your UI state.
 * 
 * 5. Some shortcuts (like Copy/Paste) may require implementing a
 *    clipboard service. For desktop apps, you can use:
 *    ```
 *    java.awt.Toolkit.getDefaultToolkit().systemClipboard
 *    ```
 * 
 * 6. Test shortcuts on all platforms (Windows, macOS, Linux) as
 *    modifier keys behave differently on each.
 */
