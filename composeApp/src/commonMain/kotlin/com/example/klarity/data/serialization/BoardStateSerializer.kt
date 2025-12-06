package com.example.klarity.data.serialization

import com.example.klarity.presentation.screen.tasks.*
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

/**
 * Serializer for Kanban board state.
 * Provides encode/decode functions for JSON serialization.
 * 
 * Requirements: 9.3 - WHEN serializing board data THEN the System SHALL encode using JSON format
 * Requirements: 9.4 - WHEN deserializing board data THEN the System SHALL parse JSON and reconstruct the board state
 */
object BoardStateSerializer {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
        isLenient = true
    }
    
    /**
     * Encodes a BoardState to a JSON string.
     * Handles null fields and empty collections gracefully.
     */
    fun encodeBoardState(state: BoardState): String {
        return json.encodeToString(BoardState.serializer(), state)
    }
    
    /**
     * Decodes a JSON string to a BoardState.
     * Returns an empty BoardState if parsing fails.
     */
    fun decodeBoardState(jsonString: String): BoardState {
        return try {
            json.decodeFromString(BoardState.serializer(), jsonString)
        } catch (e: Exception) {
            BoardState()
        }
    }
    
    // ============================================================================
    // Domain Model Conversion Functions
    // ============================================================================
    
    /**
     * Converts domain KanbanColumn list and Task list to serializable BoardState.
     */
    fun toSerializable(columns: List<KanbanColumn>): BoardState {
        val columnStates = columns.mapIndexed { index, column ->
            ColumnState(
                id = column.status.name,
                title = column.status.label,
                status = column.status.name,
                order = index,
                isCollapsed = column.isCollapsed,
                wipLimit = column.wipLimit
            )
        }
        
        val taskStates = columns.flatMap { column ->
            column.tasks.map { task -> taskToState(task) }
        }
        
        return BoardState(columns = columnStates, tasks = taskStates)
    }
    
    /**
     * Converts serializable BoardState back to domain models.
     */
    fun fromSerializable(state: BoardState): List<KanbanColumn> {
        val tasksByStatus = state.tasks.groupBy { it.status }
        
        return state.columns.sortedBy { it.order }.map { columnState ->
            val status = TaskStatus.entries.find { it.name == columnState.status } 
                ?: TaskStatus.TODO
            val tasks = tasksByStatus[columnState.status]
                ?.sortedBy { it.order }
                ?.map { taskState -> stateToTask(taskState) }
                ?: emptyList()
            
            KanbanColumn(
                status = status,
                tasks = tasks,
                isCollapsed = columnState.isCollapsed,
                wipLimit = columnState.wipLimit
            )
        }
    }
    
    // ============================================================================
    // Private Conversion Helpers
    // ============================================================================
    
    private fun taskToState(task: Task): TaskState {
        return TaskState(
            id = task.id,
            title = task.title,
            description = task.description,
            status = task.status.name,
            priority = task.priority.name,
            tags = task.tags.map { TagState(it.label, it.colorClass.name) },
            points = task.points,
            assignee = task.assignee,
            dueDate = task.dueDate?.toEpochMilliseconds(),
            startDate = task.startDate?.toEpochMilliseconds(),
            estimatedHours = task.estimatedHours,
            actualHours = task.actualHours,
            subtasks = task.subtasks.map { SubtaskState(it.id, it.title, it.isCompleted, it.order) },
            linkedNoteIds = task.linkedNoteIds,
            timerStartedAt = task.timer?.startedAt?.toEpochMilliseconds(),
            timerPausedDuration = task.timer?.pausedDuration?.inWholeMilliseconds,
            timerIsPaused = task.timer?.isPaused ?: false,
            isActive = task.isActive,
            completed = task.completed,
            createdAt = task.createdAt.toEpochMilliseconds(),
            updatedAt = task.updatedAt.toEpochMilliseconds(),
            completedAt = task.completedAt?.toEpochMilliseconds(),
            order = task.order
        )
    }
    
    private fun stateToTask(state: TaskState): Task {
        val timer = if (state.timerStartedAt != null) {
            TaskTimer(
                startedAt = Instant.fromEpochMilliseconds(state.timerStartedAt),
                pausedDuration = (state.timerPausedDuration ?: 0L).milliseconds,
                isPaused = state.timerIsPaused
            )
        } else null
        
        return Task(
            id = state.id,
            title = state.title,
            description = state.description,
            status = TaskStatus.entries.find { it.name == state.status } ?: TaskStatus.TODO,
            priority = TaskPriority.entries.find { it.name == state.priority } ?: TaskPriority.MEDIUM,
            tags = state.tags.map { 
                TaskTag(
                    label = it.label, 
                    colorClass = TagColor.entries.find { c -> c.name == it.color } ?: TagColor.GRAY
                )
            },
            points = state.points,
            assignee = state.assignee,
            dueDate = state.dueDate?.let { Instant.fromEpochMilliseconds(it) },
            startDate = state.startDate?.let { Instant.fromEpochMilliseconds(it) },
            estimatedHours = state.estimatedHours,
            actualHours = state.actualHours,
            subtasks = state.subtasks.map { Subtask(it.id, it.title, it.isCompleted, it.order) },
            linkedNoteIds = state.linkedNoteIds,
            timer = timer,
            isActive = state.isActive,
            completed = state.completed,
            createdAt = Instant.fromEpochMilliseconds(state.createdAt),
            updatedAt = Instant.fromEpochMilliseconds(state.updatedAt),
            completedAt = state.completedAt?.let { Instant.fromEpochMilliseconds(it) },
            order = state.order
        )
    }
}
