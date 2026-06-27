package com.example.klarity.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

/**
 * Task / Kanban domain models.
 *
 * These were originally defined under the presentation layer but are consumed by the
 * data/domain layers (TaskRepository, mappers, serialization), so they live in the
 * domain layer where they belong.
 *
 * All data classes are marked @Immutable for Compose recomposition optimization.
 */

// ============================================================================
// Task Status & Priority
// ============================================================================

enum class TaskStatus(val label: String, val emoji: String) {
    BACKLOG("Backlog", "📋"),
    TODO("To Do", "📝"),
    IN_PROGRESS("In Progress", "🔄"),
    IN_REVIEW("In Review", "👀"),
    DONE("Done", "✅"),
    ARCHIVED("Archived", "📦")
}

/**
 * Task priority levels with associated colors.
 * Colors follow the design spec: HIGH=red, MEDIUM=yellow, LOW=blue
 */
enum class TaskPriority(val label: String, val emoji: String, val color: Long) {
    HIGH("High", "🔴", 0xFFEF4444),      // Red
    MEDIUM("Medium", "🟡", 0xFFFACC15),  // Yellow
    LOW("Low", "🔵", 0xFF3B82F6),        // Blue
    NONE("None", "⚪", 0xFF9E9E9E)
}

// ============================================================================
// Tag Models
// ============================================================================

/**
 * Color options for task tags with background alpha and text color values.
 */
enum class TagColor(val bgAlpha: Float, val textColor: Long) {
    PURPLE(0.2f, 0xFFD8B4FE),
    BLUE(0.2f, 0xFF93C5FD),
    GREEN(0.2f, 0xFF86EFAC),
    RED(0.2f, 0xFFFCA5A5),
    ORANGE(0.2f, 0xFFFDBA74),
    GRAY(0.2f, 0xFFD1D5DB)
}

/**
 * A labeled tag for categorizing tasks.
 */
@Immutable
data class TaskTag(
    val label: String,
    val colorClass: TagColor = TagColor.GRAY
)

// ============================================================================
// Timer Model
// ============================================================================

/**
 * Tracks time spent on a task.
 *
 * @property startedAt When the timer was started
 * @property pausedDuration Total duration the timer has been paused
 * @property isPaused Whether the timer is currently paused
 */
@Immutable
data class TaskTimer(
    val startedAt: Instant,
    val pausedDuration: Duration = Duration.ZERO,
    val isPaused: Boolean = false
) {
    /**
     * Calculates the elapsed time, accounting for paused duration.
     * Returns Duration.ZERO if the timer hasn't started yet.
     */
    fun elapsedTime(now: Instant = Clock.System.now()): Duration {
        val totalElapsed = now - startedAt
        return maxOf(totalElapsed - pausedDuration, Duration.ZERO)
    }

    /**
     * Returns the elapsed time formatted as HH:MM:SS.
     * Caps at 99:59:59 to prevent overflow in display.
     */
    fun formattedTime(now: Instant = Clock.System.now()): String {
        val elapsed = elapsedTime(now)
        val totalSeconds = elapsed.inWholeSeconds.coerceAtMost(359999) // Cap at 99:59:59
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        fun pad(n: Long) = n.toString().padStart(2, '0')
        return "${pad(hours)}:${pad(minutes)}:${pad(seconds)}"
    }
}

// ============================================================================
// Task Model
// ============================================================================

@Immutable
data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val tags: List<TaskTag> = emptyList(),
    val points: Int? = null,  // Story points for effort estimation
    val assignee: String? = null,
    val dueDate: Instant? = null,
    val startDate: Instant? = null,
    val estimatedHours: Float? = null,
    val actualHours: Float? = null,
    val subtasks: List<Subtask> = emptyList(),
    val linkedNoteIds: List<String> = emptyList(),
    val timer: TaskTimer? = null,  // Active timer for time tracking
    val isActive: Boolean = false,  // Whether this task is currently focused
    val completed: Boolean = false,  // Task completion status
    val createdAt: Instant,
    val updatedAt: Instant,
    val completedAt: Instant? = null,
    val order: Int = 0
) {
    val progress: Float
        get() = if (subtasks.isEmpty()) {
            if (status == TaskStatus.DONE || completed) 1f else 0f
        } else {
            subtasks.count { it.isCompleted }.toFloat() / subtasks.size
        }

    val isOverdue: Boolean
        get() = dueDate != null && status != TaskStatus.DONE && !completed &&
                dueDate < Clock.System.now()

    /**
     * Returns true if this task has an active (non-null) timer.
     */
    val hasActiveTimer: Boolean
        get() = timer != null
}

@Immutable
data class Subtask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val order: Int = 0
)

// ============================================================================
// Kanban Column Model
// ============================================================================

@Immutable
data class KanbanColumn(
    val status: TaskStatus,
    val tasks: List<Task>,
    val isCollapsed: Boolean = false,
    val wipLimit: Int? = null // Work-in-progress limit
) {
    val isOverWipLimit: Boolean
        get() = wipLimit != null && tasks.size > wipLimit
}

// ============================================================================
// View Mode
// ============================================================================

enum class TaskViewMode(val label: String, val emoji: String) {
    KANBAN("Kanban", "📊"),
    LIST("List", "📋"),
    TIMELINE("Timeline", "📅"),
    CALENDAR("Calendar", "🗓️")
}

// ============================================================================
// Filter & Sort
// ============================================================================

@Immutable
data class TaskFilter(
    val statuses: Set<TaskStatus> = TaskStatus.entries.toSet(),
    val priorities: Set<TaskPriority> = TaskPriority.entries.toSet(),
    val tags: Set<String> = emptySet(),
    val assignees: Set<String> = emptySet(),
    val showOverdueOnly: Boolean = false,
    val searchQuery: String = ""
)

enum class TaskSortOption(val label: String) {
    PRIORITY("Priority"),
    DUE_DATE("Due Date"),
    CREATED("Created"),
    UPDATED("Updated"),
    TITLE("Title"),
    MANUAL("Manual")
}

// ============================================================================
// Drag & Drop State
// ============================================================================

@Immutable
data class DragState(
    val isDragging: Boolean = false,
    val draggedTaskId: String? = null,
    val sourceColumn: TaskStatus? = null,
    val targetColumn: TaskStatus? = null,
    val targetIndex: Int? = null
)
