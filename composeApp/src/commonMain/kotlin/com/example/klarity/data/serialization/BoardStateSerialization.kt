package com.example.klarity.data.serialization

import kotlinx.serialization.Serializable

/**
 * Serializable data classes for board state persistence.
 * These classes are used for JSON serialization/deserialization of the Kanban board state.
 * 
 * Requirements: 9.3 - WHEN serializing board data THEN the System SHALL encode using JSON format
 */

/**
 * Represents the complete state of a Kanban board for serialization.
 */
@Serializable
data class BoardState(
    val columns: List<ColumnState> = emptyList(),
    val tasks: List<TaskState> = emptyList()
)

/**
 * Represents a Kanban column for serialization.
 */
@Serializable
data class ColumnState(
    val id: String,
    val title: String,
    val status: String,
    val order: Int,
    val isCollapsed: Boolean = false,
    val wipLimit: Int? = null
)

/**
 * Represents a task for serialization.
 */
@Serializable
data class TaskState(
    val id: String,
    val title: String,
    val description: String = "",
    val status: String,
    val priority: String,
    val tags: List<TagState> = emptyList(),
    val points: Int? = null,
    val assignee: String? = null,
    val dueDate: Long? = null,
    val startDate: Long? = null,
    val estimatedHours: Float? = null,
    val actualHours: Float? = null,
    val subtasks: List<SubtaskState> = emptyList(),
    val linkedNoteIds: List<String> = emptyList(),
    val timerStartedAt: Long? = null,
    val timerPausedDuration: Long? = null,
    val timerIsPaused: Boolean = false,
    val isActive: Boolean = false,
    val completed: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long? = null,
    val order: Int = 0
)

/**
 * Represents a task tag for serialization.
 */
@Serializable
data class TagState(
    val label: String,
    val color: String
)

/**
 * Represents a subtask for serialization.
 */
@Serializable
data class SubtaskState(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val order: Int = 0
)
