package com.example.sentio.data.models

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String = uuid4().toString(),
    val title: String,
    val content: String,
    val folderId: String? = null,
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val wordCount: Int = 0,
    val embedding: List<Float>? = null // For AI search
) {
    companion object {
        fun empty() = Note(
            title = "Untitled Note",
            content = "",
            wordCount = 0
        )

        fun sample() = Note(
            title = "Sample Note",
            content = """
                # Getting Started with Sentio
                
                This is a **sample note** with some markdown content.
                
                ## Features
                - AI-powered search
                - Syntax highlighting
                - Real-time sync
                
                ```kotlin
                fun main() {
                    println("Hello, Sentio!")
                }
                ```
            """.trimIndent(),
            tags = listOf("sample", "tutorial"),
            wordCount = 42
        )
    }

    fun updateContent(newContent: String): Note {
        return copy(
            content = newContent,
            updatedAt = Clock.System.now(),
            wordCount = newContent.split("\\s+".toRegex()).size
        )
    }

    fun updateTitle(newTitle: String): Note {
        return copy(
            title = newTitle,
            updatedAt = Clock.System.now()
        )
    }

    fun addTag(tag: String): Note {
        return copy(
            tags = (tags + tag).distinct(),
            updatedAt = Clock.System.now()
        )
    }

    fun removeTag(tag: String): Note {
        return copy(
            tags = tags.filter { it != tag },
            updatedAt = Clock.System.now()
        )
    }

    fun togglePin(): Note {
        return copy(
            isPinned = !isPinned,
            updatedAt = Clock.System.now()
        )
    }
}