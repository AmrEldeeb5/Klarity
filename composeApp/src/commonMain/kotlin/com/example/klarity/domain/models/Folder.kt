package com.example.klarity.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a folder for organizing notes hierarchically.
 * Marked @Immutable for Compose recomposition optimization.
 */
@Immutable
@Serializable
data class Folder(
    val id: String,
    val name: String,
    val parentId: String?, // null for root folders
    val createdAt: Instant,
    val icon: String? = null // Emoji or icon identifier
)
