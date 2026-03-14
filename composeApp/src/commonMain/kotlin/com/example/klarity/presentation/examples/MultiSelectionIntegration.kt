package com.example.klarity.presentation.examples

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.components.*
import com.example.klarity.presentation.screen.tasks.*
import com.example.klarity.presentation.state.*
import kotlinx.datetime.Clock

/**
 * Multi-Selection Integration Example
 * 
 * Demonstrates how to integrate the multi-selection system with the KanbanBoard.
 * Shows:
 * - Selection state management
 * - Keyboard shortcuts
 * - Bulk actions bar
 * - Dialogs for bulk operations
 */

@Composable
fun MultiSelectionIntegrationExample(
    columns: List<KanbanColumn>,
    onTasksUpdated: (List<Task>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Selection state
    var selectionState by remember { mutableStateOf(MultiSelectionState()) }
    
    // Dialog states
    var showBulkEditDialog by remember { mutableStateOf(false) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }
    var showBulkMoveDialog by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Main content with Kanban board
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with selection info
            if (selectionState.hasSelection) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectionState.selectedCount} tasks selected",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        TextButton(
                            onClick = { selectionState = selectionState.clearSelection() }
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
            
            // Kanban board with selectable cards
            SelectableKanbanBoard(
                columns = columns,
                selectionState = selectionState,
                onSelectionChanged = { newState ->
                    selectionState = newState
                },
                onTaskClick = { task ->
                    // Handle normal task click (open detail view)
                    // Only if not in selection mode
                    if (selectionState.selectionMode == SelectionMode.NONE) {
                        // Open task detail
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Bulk actions bar at bottom
        BulkActionsBar(
            selectedCount = selectionState.selectedCount,
            visible = selectionState.hasSelection,
            onClearSelection = {
                selectionState = selectionState.clearSelection()
            },
            onSelectAll = {
                val allTaskIds = columns.flatMap { it.tasks }.map { it.id }.toSet()
                selectionState = selectionState.copy(
                    selectedTaskIds = allTaskIds,
                    selectionMode = SelectionMode.MULTI
                )
            },
            onMoveToClick = {
                showBulkMoveDialog = true
            },
            onPriorityClick = {
                showPriorityMenu = true
            },
            onTagClick = {
                // Show tag dialog (not implemented in this example)
            },
            onDeleteClick = {
                showBulkDeleteDialog = true
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // Priority dropdown menu
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 80.dp, end = 200.dp)) {
            BulkPriorityMenu(
                expanded = showPriorityMenu,
                onDismiss = { showPriorityMenu = false },
                onPrioritySelected = { priority ->
                    // Update all selected tasks with new priority
                    updateTasksPriority(columns, selectionState.selectedTaskIds, priority, onTasksUpdated)
                    selectionState = selectionState.clearSelection()
                }
            )
        }
    }
    
    // Bulk edit dialog
    BulkEditDialog(
        visible = showBulkEditDialog,
        selectedCount = selectionState.selectedCount,
        onDismiss = { showBulkEditDialog = false },
        onConfirm = { properties ->
            // Apply bulk edit properties
            applyBulkEdit(columns, selectionState.selectedTaskIds, properties, onTasksUpdated)
            showBulkEditDialog = false
            selectionState = selectionState.clearSelection()
        }
    )
    
    // Bulk delete confirmation
    BulkDeleteConfirmDialog(
        visible = showBulkDeleteDialog,
        count = selectionState.selectedCount,
        onConfirm = {
            // Delete selected tasks
            deleteSelectedTasks(columns, selectionState.selectedTaskIds, onTasksUpdated)
            showBulkDeleteDialog = false
            selectionState = selectionState.clearSelection()
        },
        onCancel = {
            showBulkDeleteDialog = false
        }
    )
    
    // Bulk move dialog
    BulkMoveDialog(
        visible = showBulkMoveDialog,
        selectedCount = selectionState.selectedCount,
        currentColumns = columns,
        onMove = { status ->
            // Move all selected tasks to new status
            moveSelectedTasks(columns, selectionState.selectedTaskIds, status, onTasksUpdated)
            showBulkMoveDialog = false
            selectionState = selectionState.clearSelection()
        },
        onDismiss = {
            showBulkMoveDialog = false
        }
    )
}

/**
 * Kanban board with selection support.
 */
@Composable
private fun SelectableKanbanBoard(
    columns: List<KanbanColumn>,
    selectionState: MultiSelectionState,
    onSelectionChanged: (MultiSelectionState) -> Unit,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    // In a real implementation, this would wrap the KanbanBoard component
    // and add selection handling to each task card
    
    Column(modifier = modifier) {
        Text(
            text = "Kanban Board with Multi-Selection",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        
        // Sample task cards with selection
        columns.forEach { column ->
            Text(
                text = "${column.status.emoji} ${column.status.label}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            column.tasks.forEach { task ->
                SelectableTaskCard(
                    task = task,
                    isSelected = selectionState.isSelected(task.id),
                    selectionMode = selectionState.selectionMode,
                    onClick = {
                        if (selectionState.selectionMode == SelectionMode.NONE) {
                            onTaskClick(task)
                        } else {
                            // Toggle selection on click when in selection mode
                            onSelectionChanged(selectionState.toggleTask(task.id))
                        }
                    },
                    onSelectionToggle = {
                        onSelectionChanged(selectionState.toggleTask(task.id))
                    },
                    onRangeSelect = {
                        // Handle range selection
                        // Would need to calculate task IDs in range
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Task card content
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

// Helper functions for bulk operations

private fun updateTasksPriority(
    columns: List<KanbanColumn>,
    selectedIds: Set<String>,
    priority: TaskPriority,
    onTasksUpdated: (List<Task>) -> Unit
) {
    val allTasks = columns.flatMap { it.tasks }
    val updatedTasks = allTasks.map { task ->
        if (task.id in selectedIds) {
            task.copy(priority = priority, updatedAt = Clock.System.now())
        } else {
            task
        }
    }
    onTasksUpdated(updatedTasks)
}

private fun applyBulkEdit(
    columns: List<KanbanColumn>,
    selectedIds: Set<String>,
    properties: BulkEditProperties,
    onTasksUpdated: (List<Task>) -> Unit
) {
    val allTasks = columns.flatMap { it.tasks }
    val updatedTasks = allTasks.map { task ->
        if (task.id in selectedIds) {
            var updated = task
            properties.priority?.let { updated = updated.copy(priority = it) }
            properties.status?.let { updated = updated.copy(status = it) }
            properties.assignee?.let { updated = updated.copy(assignee = it) }
            updated.copy(updatedAt = Clock.System.now())
        } else {
            task
        }
    }
    onTasksUpdated(updatedTasks)
}

private fun deleteSelectedTasks(
    columns: List<KanbanColumn>,
    selectedIds: Set<String>,
    onTasksUpdated: (List<Task>) -> Unit
) {
    val allTasks = columns.flatMap { it.tasks }
    val remainingTasks = allTasks.filterNot { it.id in selectedIds }
    onTasksUpdated(remainingTasks)
}

private fun moveSelectedTasks(
    columns: List<KanbanColumn>,
    selectedIds: Set<String>,
    newStatus: TaskStatus,
    onTasksUpdated: (List<Task>) -> Unit
) {
    val allTasks = columns.flatMap { it.tasks }
    val updatedTasks = allTasks.map { task ->
        if (task.id in selectedIds) {
            task.copy(status = newStatus, updatedAt = Clock.System.now())
        } else {
            task
        }
    }
    onTasksUpdated(updatedTasks)
}
