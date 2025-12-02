package com.example.sentio.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a link between two notes (for knowledge graph)
 */
@Serializable
data class Link(
    val id: String,
    val sourceNoteId: String,
    val targetNoteId: String,
    val linkType: LinkType = LinkType.REFERENCE
)

@Serializable
enum class LinkType {
    REFERENCE,      // Simple reference/mention
    RELATED,        // Related topic
    DEPENDS_ON,     // Dependency relationship
    PARENT_CHILD    // Hierarchical relationship
}
