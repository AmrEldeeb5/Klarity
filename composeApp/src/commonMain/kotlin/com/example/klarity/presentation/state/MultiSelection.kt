package com.example.klarity.presentation.state

import androidx.compose.runtime.Immutable
import com.example.klarity.presentation.screen.tasks.TaskStatus

/**
 * Multi-Selection State Management
 * 
 * Handles selection state for bulk operations on tasks including:
 * - Multi-select mode
 * - Range selection (Shift+Click)
 * - Selection across columns
 * - Batch operations
 */

/**
 * State representing the current multi-selection.
 * 
 * @property selectedTaskIds Set of selected task IDs for O(1) lookup performance
 * @property selectionMode Current selection mode
 * @property anchorTaskId Task ID used as anchor point for range selection (Shift+Click)
 */
@Immutable
data class MultiSelectionState(
    val selectedTaskIds: Set<String> = emptySet(),
    val selectionMode: SelectionMode = SelectionMode.NONE,
    val anchorTaskId: String? = null
) {
    /**
     * Returns true if any tasks are selected.
     */
    val hasSelection: Boolean
        get() = selectedTaskIds.isNotEmpty()
    
    /**
     * Returns the count of selected tasks.
     */
    val selectedCount: Int
        get() = selectedTaskIds.size
    
    /**
     * Returns true if the given task ID is selected.
     */
    fun isSelected(taskId: String): Boolean = taskId in selectedTaskIds
    
    /**
     * Returns a new state with the given task ID toggled.
     */
    fun toggleTask(taskId: String): MultiSelectionState {
        val newSelection = if (taskId in selectedTaskIds) {
            selectedTaskIds - taskId
        } else {
            selectedTaskIds + taskId
        }
        
        return copy(
            selectedTaskIds = newSelection,
            selectionMode = if (newSelection.isEmpty()) SelectionMode.NONE else SelectionMode.MULTI,
            anchorTaskId = if (newSelection.isEmpty()) null else taskId
        )
    }
    
    /**
     * Returns a new state with the given task IDs added to selection.
     */
    fun addTasks(taskIds: Set<String>): MultiSelectionState {
        return copy(
            selectedTaskIds = selectedTaskIds + taskIds,
            selectionMode = if (taskIds.isEmpty()) selectionMode else SelectionMode.MULTI
        )
    }
    
    /**
     * Returns a new state with all selections cleared.
     */
    fun clearSelection(): MultiSelectionState {
        return MultiSelectionState()
    }
}

/**
 * Selection mode for the multi-selection system.
 */
enum class SelectionMode {
    /**
     * No selection active - normal interaction mode.
     */
    NONE,
    
    /**
     * Multiple individual tasks selected (Cmd/Ctrl+Click).
     */
    MULTI,
    
    /**
     * Range selection in progress (Shift+Click).
     */
    RANGE
}

/**
 * Actions that can be performed on the selection state.
 */
sealed interface SelectionAction {
    /**
     * Toggle selection of a single task (Cmd/Ctrl+Click).
     */
    data class ToggleTask(val taskId: String) : SelectionAction
    
    /**
     * Select a range of tasks from anchor to target (Shift+Click).
     * 
     * @property fromId Starting task ID (anchor)
     * @property toId Ending task ID (clicked)
     * @property taskIdsInRange All task IDs between from and to in visual order
     */
    data class RangeSelect(
        val fromId: String,
        val toId: String,
        val taskIdsInRange: List<String>
    ) : SelectionAction
    
    /**
     * Select all tasks in the current view.
     */
    data object SelectAll : SelectionAction
    
    /**
     * Clear all selections.
     */
    data object ClearSelection : SelectionAction
    
    /**
     * Select all tasks in a specific column.
     */
    data class SelectInColumn(val status: TaskStatus) : SelectionAction
    
    /**
     * Remove tasks from selection (used when tasks are deleted).
     */
    data class RemoveTasks(val taskIds: Set<String>) : SelectionAction
}

/**
 * Reducer function to handle selection actions.
 */
fun reduceSelectionAction(
    state: MultiSelectionState,
    action: SelectionAction
): MultiSelectionState {
    return when (action) {
        is SelectionAction.ToggleTask -> state.toggleTask(action.taskId)
        
        is SelectionAction.RangeSelect -> {
            state.copy(
                selectedTaskIds = action.taskIdsInRange.toSet(),
                selectionMode = SelectionMode.RANGE,
                anchorTaskId = action.fromId
            )
        }
        
        is SelectionAction.SelectAll -> {
            // SelectAll is handled in the ViewModel with full task list
            state
        }
        
        is SelectionAction.ClearSelection -> state.clearSelection()
        
        is SelectionAction.SelectInColumn -> {
            // SelectInColumn is handled in the ViewModel with column tasks
            state
        }
        
        is SelectionAction.RemoveTasks -> {
            val newSelection = state.selectedTaskIds - action.taskIds
            state.copy(
                selectedTaskIds = newSelection,
                selectionMode = if (newSelection.isEmpty()) SelectionMode.NONE else state.selectionMode,
                anchorTaskId = if (newSelection.isEmpty()) null else state.anchorTaskId
            )
        }
    }
}
