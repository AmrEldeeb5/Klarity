package com.example.klarity.domain.search

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Search history management for storing recent searches.
 */

/**
 * Data class representing search history.
 */
data class SearchHistory(
    val queries: List<String> = emptyList(),
    val maxSize: Int = 20
) {
    /**
     * Adds a new query to the history.
     * Maintains unique queries and respects max size.
     */
    fun add(query: String): SearchHistory {
        if (query.isBlank()) return this
        
        val updated = (listOf(query) + queries)
            .distinct()
            .take(maxSize)
        
        return copy(queries = updated)
    }
    
    /**
     * Removes a query from history.
     */
    fun remove(query: String): SearchHistory {
        return copy(queries = queries.filter { it != query })
    }
    
    /**
     * Clears all history.
     */
    fun clear(): SearchHistory {
        return copy(queries = emptyList())
    }
}

/**
 * Saved search with metadata.
 */
data class SavedSearch(
    val id: String,
    val name: String,
    val query: String,
    val createdAt: Instant = Clock.System.now(),
    val description: String? = null,
    val color: String? = null, // Optional color for visual grouping
    val icon: String? = null   // Optional icon emoji
)

/**
 * Repository interface for persisting search history.
 */
interface SearchHistoryRepository {
    /**
     * Gets the search history.
     */
    suspend fun getHistory(): SearchHistory
    
    /**
     * Adds a query to the history.
     */
    suspend fun addQuery(query: String)
    
    /**
     * Removes a query from history.
     */
    suspend fun removeQuery(query: String)
    
    /**
     * Clears all history.
     */
    suspend fun clearHistory()
    
    /**
     * Gets all saved searches.
     */
    suspend fun getSavedSearches(): List<SavedSearch>
    
    /**
     * Saves a search with a name.
     */
    suspend fun saveSearch(name: String, query: String, description: String? = null)
    
    /**
     * Deletes a saved search.
     */
    suspend fun deleteSavedSearch(id: String)
    
    /**
     * Updates a saved search.
     */
    suspend fun updateSavedSearch(search: SavedSearch)
}

/**
 * In-memory implementation of SearchHistoryRepository.
 * For production, this should be replaced with a persistent storage implementation.
 */
class InMemorySearchHistoryRepository : SearchHistoryRepository {
    private var history = SearchHistory()
    private val savedSearches = mutableListOf<SavedSearch>()
    
    override suspend fun getHistory(): SearchHistory {
        return history
    }
    
    override suspend fun addQuery(query: String) {
        history = history.add(query)
    }
    
    override suspend fun removeQuery(query: String) {
        history = history.remove(query)
    }
    
    override suspend fun clearHistory() {
        history = history.clear()
    }
    
    override suspend fun getSavedSearches(): List<SavedSearch> {
        return savedSearches.toList()
    }
    
    override suspend fun saveSearch(name: String, query: String, description: String?) {
        val search = SavedSearch(
            id = generateId(),
            name = name,
            query = query,
            description = description
        )
        savedSearches.add(search)
    }
    
    override suspend fun deleteSavedSearch(id: String) {
        savedSearches.removeAll { it.id == id }
    }
    
    override suspend fun updateSavedSearch(search: SavedSearch) {
        val index = savedSearches.indexOfFirst { it.id == search.id }
        if (index != -1) {
            savedSearches[index] = search
        }
    }
    
    private fun generateId(): String {
        return "search_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
    }
}
