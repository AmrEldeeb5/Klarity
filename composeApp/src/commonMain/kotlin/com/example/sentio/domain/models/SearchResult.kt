package com.example.sentio.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a search result with relevance scoring
 */
@Serializable
data class SearchResult(
    val note: Note,
    val score: Float, // Relevance score (0.0 - 1.0)
    val matchedSnippets: List<String> = emptyList() // Highlighted text snippets
)
