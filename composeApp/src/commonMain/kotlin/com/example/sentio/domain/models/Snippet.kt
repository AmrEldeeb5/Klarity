package com.example.sentio.domain.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a code snippet with syntax highlighting
 */
@Serializable
data class Snippet(
    val id: String,
    val title: String,
    val code: String,
    val language: String, // e.g., "kotlin", "python", "javascript"
    val description: String? = null,
    val tags: List<Tag>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isFavorite: Boolean = false
)
