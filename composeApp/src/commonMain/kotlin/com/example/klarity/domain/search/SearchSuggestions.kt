package com.example.klarity.domain.search

import com.example.klarity.presentation.screen.tasks.Task

/**
 * Search suggestion system for auto-complete.
 */

/**
 * Represents different types of search suggestions.
 */
sealed class SearchSuggestion {
    /**
     * Filter syntax suggestion with example.
     */
    data class FilterSyntax(
        val syntax: String,
        val description: String,
        val example: String
    ) : SearchSuggestion()
    
    /**
     * Recent search query.
     */
    data class RecentSearch(val query: String) : SearchSuggestion()
    
    /**
     * Saved search with name.
     */
    data class SavedSearch(val name: String, val query: String) : SearchSuggestion()
    
    /**
     * Task that matches the search.
     */
    data class TaskMatch(val task: Task) : SearchSuggestion()
}

/**
 * Generates search suggestions based on the current query.
 */
class SearchSuggestionProvider {
    
    /**
     * All available filter syntax suggestions grouped by category.
     */
    private val allSyntaxSuggestions = listOf(
        // Status filters
        SearchSuggestion.FilterSyntax(
            syntax = "status:todo",
            description = "Tasks with TODO status",
            example = "status:todo priority:high"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "status:in_progress",
            description = "Tasks in progress",
            example = "status:in_progress @john"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "status:in_review",
            description = "Tasks in review",
            example = "status:in_review"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "status:done",
            description = "Completed tasks",
            example = "status:done due:today"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "status:backlog",
            description = "Backlog tasks",
            example = "status:backlog"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "status:archived",
            description = "Archived tasks",
            example = "status:archived"
        ),
        
        // Priority filters
        SearchSuggestion.FilterSyntax(
            syntax = "priority:high",
            description = "High priority tasks",
            example = "priority:high -status:done"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "priority:medium",
            description = "Medium priority tasks",
            example = "priority:medium"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "priority:low",
            description = "Low priority tasks",
            example = "priority:low"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "is:urgent",
            description = "High priority tasks (alias)",
            example = "is:urgent status:todo"
        ),
        
        // Assignee filters
        SearchSuggestion.FilterSyntax(
            syntax = "@username",
            description = "Assigned to username",
            example = "@john priority:high"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "assignee:username",
            description = "Assigned to username",
            example = "assignee:sarah"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "is:unassigned",
            description = "Tasks without assignee",
            example = "is:unassigned priority:high"
        ),
        
        // Tag filters
        SearchSuggestion.FilterSyntax(
            syntax = "#tag",
            description = "Tagged with 'tag'",
            example = "#bug priority:high"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "tag:frontend",
            description = "Tagged with 'frontend'",
            example = "tag:frontend status:todo"
        ),
        
        // Due date filters
        SearchSuggestion.FilterSyntax(
            syntax = "due:today",
            description = "Due today",
            example = "due:today -status:done"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "due:tomorrow",
            description = "Due tomorrow",
            example = "due:tomorrow"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "due:this_week",
            description = "Due this week",
            example = "due:this_week"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "due:overdue",
            description = "Past due date",
            example = "due:overdue priority:high"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "is:overdue",
            description = "Past due date (alias)",
            example = "is:overdue @john"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "is:due_soon",
            description = "Due within 3 days",
            example = "is:due_soon"
        ),
        
        // Created/Updated filters
        SearchSuggestion.FilterSyntax(
            syntax = "created:today",
            description = "Created today",
            example = "created:today"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "created:this_week",
            description = "Created this week",
            example = "created:this_week"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "updated:today",
            description = "Updated today",
            example = "updated:today"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "updated:yesterday",
            description = "Updated yesterday",
            example = "updated:yesterday"
        ),
        
        // Subtasks
        SearchSuggestion.FilterSyntax(
            syntax = "has:subtasks",
            description = "Tasks with subtasks",
            example = "has:subtasks"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "is:parent",
            description = "Tasks with subtasks (alias)",
            example = "is:parent"
        ),
        
        // Notes
        SearchSuggestion.FilterSyntax(
            syntax = "has:notes",
            description = "Tasks with linked notes",
            example = "has:notes"
        ),
        
        // Timer
        SearchSuggestion.FilterSyntax(
            syntax = "is:active",
            description = "Tasks with timer running",
            example = "is:active"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "is:tracked",
            description = "Tasks with timer data",
            example = "is:tracked"
        ),
        
        // Status shortcuts
        SearchSuggestion.FilterSyntax(
            syntax = "is:done",
            description = "Completed tasks (alias)",
            example = "is:done created:today"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "is:todo",
            description = "TODO tasks (alias)",
            example = "is:todo"
        ),
        
        // Negation
        SearchSuggestion.FilterSyntax(
            syntax = "-status:done",
            description = "Exclude completed tasks",
            example = "-status:done priority:high"
        ),
        SearchSuggestion.FilterSyntax(
            syntax = "-is:archived",
            description = "Exclude archived tasks",
            example = "-is:archived"
        ),
        
        // OR logic
        SearchSuggestion.FilterSyntax(
            syntax = "priority:high OR priority:urgent",
            description = "Either condition matches",
            example = "priority:high OR priority:medium @john"
        )
    )
    
    /**
     * Gets suggestions based on the current query.
     */
    fun getSuggestions(
        query: String,
        recentSearches: List<String> = emptyList(),
        savedSearches: List<Pair<String, String>> = emptyList(),
        matchingTasks: List<Task> = emptyList(),
        maxResults: Int = 10
    ): List<SearchSuggestion> {
        if (query.isEmpty()) {
            // Show recent and saved searches when query is empty
            val suggestions = mutableListOf<SearchSuggestion>()
            
            // Add saved searches first
            suggestions.addAll(
                savedSearches.take(3).map { (name, searchQuery) ->
                    SearchSuggestion.SavedSearch(name, searchQuery)
                }
            )
            
            // Add recent searches
            suggestions.addAll(
                recentSearches.take(5).map { SearchSuggestion.RecentSearch(it) }
            )
            
            return suggestions.take(maxResults)
        }
        
        val suggestions = mutableListOf<SearchSuggestion>()
        
        // Check if user is typing a filter
        val filterPrefix = getFilterPrefix(query)
        
        if (filterPrefix != null) {
            // Show relevant filter syntax suggestions
            suggestions.addAll(
                getSyntaxSuggestionsForPrefix(filterPrefix).take(maxResults)
            )
        } else {
            // Show mixed suggestions
            
            // Add filter syntax suggestions (top 5)
            suggestions.addAll(
                allSyntaxSuggestions
                    .filter { 
                        it.syntax.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
                    }
                    .take(5)
            )
            
            // Add recent searches that match
            suggestions.addAll(
                recentSearches
                    .filter { it.contains(query, ignoreCase = true) }
                    .take(3)
                    .map { SearchSuggestion.RecentSearch(it) }
            )
            
            // Add matching tasks (top 2)
            suggestions.addAll(
                matchingTasks.take(2).map { SearchSuggestion.TaskMatch(it) }
            )
        }
        
        return suggestions.take(maxResults)
    }
    
    /**
     * Gets the filter prefix being typed (e.g., "status:", "priority:").
     */
    private fun getFilterPrefix(query: String): String? {
        val lastWord = query.split(" ").lastOrNull() ?: return null
        
        val prefixes = listOf(
            "status:", "priority:", "assignee:", "tag:", "due:", 
            "created:", "updated:", "is:", "has:", "@", "#"
        )
        
        return prefixes.find { lastWord.startsWith(it, ignoreCase = true) }
    }
    
    /**
     * Gets syntax suggestions for a specific filter prefix.
     */
    private fun getSyntaxSuggestionsForPrefix(prefix: String): List<SearchSuggestion.FilterSyntax> {
        return when {
            prefix.startsWith("status:", ignoreCase = true) -> {
                listOf(
                    SearchSuggestion.FilterSyntax("status:todo", "TODO tasks", "status:todo"),
                    SearchSuggestion.FilterSyntax("status:in_progress", "In progress tasks", "status:in_progress"),
                    SearchSuggestion.FilterSyntax("status:in_review", "In review tasks", "status:in_review"),
                    SearchSuggestion.FilterSyntax("status:done", "Completed tasks", "status:done"),
                    SearchSuggestion.FilterSyntax("status:backlog", "Backlog tasks", "status:backlog"),
                    SearchSuggestion.FilterSyntax("status:archived", "Archived tasks", "status:archived")
                )
            }
            prefix.startsWith("priority:", ignoreCase = true) -> {
                listOf(
                    SearchSuggestion.FilterSyntax("priority:high", "High priority", "priority:high"),
                    SearchSuggestion.FilterSyntax("priority:medium", "Medium priority", "priority:medium"),
                    SearchSuggestion.FilterSyntax("priority:low", "Low priority", "priority:low")
                )
            }
            prefix.startsWith("due:", ignoreCase = true) -> {
                listOf(
                    SearchSuggestion.FilterSyntax("due:today", "Due today", "due:today"),
                    SearchSuggestion.FilterSyntax("due:tomorrow", "Due tomorrow", "due:tomorrow"),
                    SearchSuggestion.FilterSyntax("due:this_week", "Due this week", "due:this_week"),
                    SearchSuggestion.FilterSyntax("due:overdue", "Overdue", "due:overdue")
                )
            }
            prefix.startsWith("is:", ignoreCase = true) -> {
                listOf(
                    SearchSuggestion.FilterSyntax("is:done", "Completed", "is:done"),
                    SearchSuggestion.FilterSyntax("is:urgent", "High priority", "is:urgent"),
                    SearchSuggestion.FilterSyntax("is:overdue", "Overdue", "is:overdue"),
                    SearchSuggestion.FilterSyntax("is:due_soon", "Due soon", "is:due_soon"),
                    SearchSuggestion.FilterSyntax("is:unassigned", "No assignee", "is:unassigned"),
                    SearchSuggestion.FilterSyntax("is:active", "Timer running", "is:active"),
                    SearchSuggestion.FilterSyntax("is:tracked", "Has timer data", "is:tracked")
                )
            }
            prefix.startsWith("has:", ignoreCase = true) -> {
                listOf(
                    SearchSuggestion.FilterSyntax("has:subtasks", "Has subtasks", "has:subtasks"),
                    SearchSuggestion.FilterSyntax("has:notes", "Has notes", "has:notes")
                )
            }
            prefix.startsWith("created:", ignoreCase = true) -> {
                listOf(
                    SearchSuggestion.FilterSyntax("created:today", "Created today", "created:today"),
                    SearchSuggestion.FilterSyntax("created:yesterday", "Created yesterday", "created:yesterday"),
                    SearchSuggestion.FilterSyntax("created:this_week", "Created this week", "created:this_week")
                )
            }
            prefix.startsWith("updated:", ignoreCase = true) -> {
                listOf(
                    SearchSuggestion.FilterSyntax("updated:today", "Updated today", "updated:today"),
                    SearchSuggestion.FilterSyntax("updated:yesterday", "Updated yesterday", "updated:yesterday"),
                    SearchSuggestion.FilterSyntax("updated:this_week", "Updated this week", "updated:this_week")
                )
            }
            else -> emptyList()
        }
    }
}
