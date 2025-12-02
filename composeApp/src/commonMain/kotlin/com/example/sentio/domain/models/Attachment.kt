package com.example.sentio.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a file attachment (image, PDF, etc.) linked to a note
 */
@Serializable
data class Attachment(
    val id: String,
    val fileName: String,
    val filePath: String, // Local file path
    val fileSize: Long, // Size in bytes
    val mimeType: String, // e.g., "image/png", "application/pdf"
    val noteId: String
)
