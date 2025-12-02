package com.example.sentio.data.models

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE,
    BLOCKED
}

@Serializable
enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

@Serializable
data class Task(
    val id: String = uuid4().toString(),
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val linkedNoteId: String? = null,
    val projectId: String? = null,
    val dueDate: Instant? = null,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val completedAt: Instant? = null,
    val aiGenerated: Boolean = false
) {
    companion object {
        fun empty() = Task(
            title = "New Task",
            description = ""
        )

        fun sample() = Task(
            title = "Implement AI Chat Drawer",
            description = "Build the AI chat drawer component with Claude API integration",
            status = TaskStatus.IN_PROGRESS,
            priority = TaskPriority.HIGH,
            aiGenerated = false
        )
    }

    fun updateStatus(newStatus: TaskStatus): Task {
        return copy(
            status = newStatus,
            updatedAt = Clock.System.now(),
            completedAt = if (newStatus == TaskStatus.DONE) Clock.System.now() else null
        )
    }

    fun updatePriority(newPriority: TaskPriority): Task {
        return copy(
            priority = newPriority,
            updatedAt = Clock.System.now()
        )
    }

    fun linkToNote(noteId: String): Task {
        return copy(
            linkedNoteId = noteId,
            updatedAt = Clock.System.now()
        )
    }

    fun isOverdue(): Boolean {
        val now = Clock.System.now()
        return dueDate != null && dueDate < now && status != TaskStatus.DONE
    }
}