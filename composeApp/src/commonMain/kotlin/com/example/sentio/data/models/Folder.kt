package com.example.sentio.data.models

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Folder(
    val id: String = uuid4().toString(),
    val name: String,
    val icon: String = "📁",
    val parentId: String? = null,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val color: String? = null
) {
    companion object {
        fun root() = Folder(
            name = "Root",
            icon = "🏠"
        )

        fun sample() = listOf(
            Folder(name = "Personal", icon = "👤"),
            Folder(name = "Work", icon = "💼"),
            Folder(name = "Projects", icon = "🚀"),
            Folder(name = "Ideas", icon = "💡")
        )
    }

    fun rename(newName: String): Folder {
        return copy(
            name = newName,
            updatedAt = Clock.System.now()
        )
    }

    fun changeIcon(newIcon: String): Folder {
        return copy(
            icon = newIcon,
            updatedAt = Clock.System.now()
        )
    }
}