package com.example.klarity.domain.search

import com.example.klarity.presentation.screen.tasks.TaskPriority
import com.example.klarity.presentation.screen.tasks.TaskStatus

/**
 * Advanced Search Parser for Klarity
 * 
 * Parses search queries with filter syntax into structured SearchQuery objects.
 * Supports text search, filters, negation, and logical operators.
 * 
 * Examples:
 * - "fix bug" → Text search
 * - "status:todo" → Status filter
 * - "@john priority:high" → Multiple filters
 * - "-status:done" → Negation
 * - "priority:high OR priority:urgent" → OR logic
 */

// ============================================================================
// Data Models
// ============================================================================

/**
 * Represents a parsed search query with text and filters.
 */
data class SearchQuery(
    val text: String = "",
    val filters: List<SearchFilter> = emptyList(),
    val negatedFilters: List<SearchFilter> = emptyList(),
    val logicalOperator: LogicalOperator = LogicalOperator.AND
) {
    /**
     * Returns true if the query is empty (no text and no filters).
     */
    val isEmpty: Boolean
        get() = text.isEmpty() && filters.isEmpty() && negatedFilters.isEmpty()
}

/**
 * Sealed class representing different types of search filters.
 */
sealed class SearchFilter {
    data class Status(val status: TaskStatus) : SearchFilter()
    data class Priority(val priority: TaskPriority) : SearchFilter()
    data class Assignee(val assignee: String) : SearchFilter()
    data class Tag(val tag: String) : SearchFilter()
    data class DueDate(val predicate: DatePredicate) : SearchFilter()
    data class CreatedDate(val predicate: DatePredicate) : SearchFilter()
    data class UpdatedDate(val predicate: DatePredicate) : SearchFilter()
    object HasSubtasks : SearchFilter()
    object HasNotes : SearchFilter()
    object IsActive : SearchFilter()
    object IsOverdue : SearchFilter()
    object IsDueSoon : SearchFilter()
    object IsUnassigned : SearchFilter()
    object IsTracked : SearchFilter()
    
    /**
     * Converts filter to display string for chips.
     */
    fun toDisplayString(): String = when (this) {
        is Status -> "status:${status.name.lowercase()}"
        is Priority -> "priority:${priority.name.lowercase()}"
        is Assignee -> "@$assignee"
        is Tag -> "#$tag"
        is DueDate -> "due:${predicate.name.lowercase()}"
        is CreatedDate -> "created:${predicate.name.lowercase()}"
        is UpdatedDate -> "updated:${predicate.name.lowercase()}"
        HasSubtasks -> "has:subtasks"
        HasNotes -> "has:notes"
        IsActive -> "is:active"
        IsOverdue -> "is:overdue"
        IsDueSoon -> "is:due_soon"
        IsUnassigned -> "is:unassigned"
        IsTracked -> "is:tracked"
    }
}

/**
 * Date predicates for temporal filters.
 */
enum class DatePredicate {
    TODAY,
    TOMORROW,
    YESTERDAY,
    THIS_WEEK,
    NEXT_WEEK,
    LAST_WEEK,
    THIS_MONTH,
    NEXT_MONTH,
    LAST_MONTH,
    OVERDUE,
    DUE_SOON
}

/**
 * Logical operators for combining filters.
 */
enum class LogicalOperator {
    AND,
    OR
}

// ============================================================================
// Parser
// ============================================================================

/**
 * Parser for search queries with filter syntax.
 */
class SearchParser {
    
    companion object {
        // Regex patterns for parsing
        private val STATUS_PATTERN = Regex("""status:(\w+)""")
        private val PRIORITY_PATTERN = Regex("""priority:(\w+)""")
        private val ASSIGNEE_PATTERN = Regex("""@(\w+)""")
        private val ASSIGNEE_FILTER_PATTERN = Regex("""assignee:(\w+)""")
        private val TAG_HASH_PATTERN = Regex("""#(\w+)""")
        private val TAG_PATTERN = Regex("""tag:(\w+)""")
        private val DUE_PATTERN = Regex("""due:(\w+)""")
        private val CREATED_PATTERN = Regex("""created:(\w+)""")
        private val UPDATED_PATTERN = Regex("""updated:(\w+)""")
        private val IS_PATTERN = Regex("""is:(\w+)""")
        private val HAS_PATTERN = Regex("""has:(\w+)""")
        private val NEGATION_PREFIX = "-"
        
        // All filter patterns combined for removal
        private val ALL_FILTER_PATTERNS = listOf(
            STATUS_PATTERN,
            PRIORITY_PATTERN,
            ASSIGNEE_PATTERN,
            ASSIGNEE_FILTER_PATTERN,
            TAG_HASH_PATTERN,
            TAG_PATTERN,
            DUE_PATTERN,
            CREATED_PATTERN,
            UPDATED_PATTERN,
            IS_PATTERN,
            HAS_PATTERN
        )
    }
    
    /**
     * Parses a search query string into a structured SearchQuery.
     */
    fun parse(input: String): SearchQuery {
        if (input.isBlank()) {
            return SearchQuery()
        }
        
        val filters = mutableListOf<SearchFilter>()
        val negatedFilters = mutableListOf<SearchFilter>()
        var remainingText = input
        
        // Check for OR operator
        val hasOrOperator = input.contains(" OR ", ignoreCase = true)
        val logicalOperator = if (hasOrOperator) LogicalOperator.OR else LogicalOperator.AND
        
        // Remove OR from text if present
        if (hasOrOperator) {
            remainingText = remainingText.replace(" OR ", " ", ignoreCase = true)
        }
        
        // Parse all filters
        parseFilters(remainingText, filters, negatedFilters)
        
        // Remove all filter syntax from text to get clean search text
        var cleanText = remainingText
        ALL_FILTER_PATTERNS.forEach { pattern ->
            cleanText = cleanText.replace(pattern, "")
        }
        
        // Also remove negated filters
        cleanText = cleanText.replace(Regex("""-\w+:\w+"""), "")
        cleanText = cleanText.replace(Regex("""-@\w+"""), "")
        cleanText = cleanText.replace(Regex("""-#\w+"""), "")
        
        // Clean up extra whitespace
        cleanText = cleanText.trim().replace(Regex("""\s+"""), " ")
        
        return SearchQuery(
            text = cleanText,
            filters = filters,
            negatedFilters = negatedFilters,
            logicalOperator = logicalOperator
        )
    }
    
    /**
     * Parses all filters from the input string.
     */
    private fun parseFilters(
        input: String,
        filters: MutableList<SearchFilter>,
        negatedFilters: MutableList<SearchFilter>
    ) {
        // Parse status filters
        parseStatusFilters(input, filters, negatedFilters)
        
        // Parse priority filters
        parsePriorityFilters(input, filters, negatedFilters)
        
        // Parse assignee filters
        parseAssigneeFilters(input, filters, negatedFilters)
        
        // Parse tag filters
        parseTagFilters(input, filters, negatedFilters)
        
        // Parse date filters
        parseDateFilters(input, filters, negatedFilters)
        
        // Parse is: filters
        parseIsFilters(input, filters, negatedFilters)
        
        // Parse has: filters
        parseHasFilters(input, filters, negatedFilters)
    }
    
    private fun parseStatusFilters(
        input: String,
        filters: MutableList<SearchFilter>,
        negatedFilters: MutableList<SearchFilter>
    ) {
        STATUS_PATTERN.findAll(input).forEach { match ->
            val isNegated = input.getOrNull(match.range.first - 1) == '-'
            val statusStr = match.groupValues[1].uppercase()
            
            val status = when (statusStr) {
                "TODO" -> TaskStatus.TODO
                "IN_PROGRESS", "INPROGRESS" -> TaskStatus.IN_PROGRESS
                "IN_REVIEW", "INREVIEW" -> TaskStatus.IN_REVIEW
                "DONE" -> TaskStatus.DONE
                "BACKLOG" -> TaskStatus.BACKLOG
                "ARCHIVED" -> TaskStatus.ARCHIVED
                else -> null
            }
            
            status?.let {
                val filter = SearchFilter.Status(it)
                if (isNegated) {
                    negatedFilters.add(filter)
                } else {
                    filters.add(filter)
                }
            }
        }
    }
    
    private fun parsePriorityFilters(
        input: String,
        filters: MutableList<SearchFilter>,
        negatedFilters: MutableList<SearchFilter>
    ) {
        PRIORITY_PATTERN.findAll(input).forEach { match ->
            val isNegated = input.getOrNull(match.range.first - 1) == '-'
            val priorityStr = match.groupValues[1].uppercase()
            
            val priority = when (priorityStr) {
                "HIGH", "URGENT" -> TaskPriority.HIGH
                "MEDIUM", "MED" -> TaskPriority.MEDIUM
                "LOW" -> TaskPriority.LOW
                "NONE" -> TaskPriority.NONE
                else -> null
            }
            
            priority?.let {
                val filter = SearchFilter.Priority(it)
                if (isNegated) {
                    negatedFilters.add(filter)
                } else {
                    filters.add(filter)
                }
            }
        }
    }
    
    private fun parseAssigneeFilters(
        input: String,
        filters: MutableList<SearchFilter>,
        negatedFilters: MutableList<SearchFilter>
    ) {
        // Parse @username format
        ASSIGNEE_PATTERN.findAll(input).forEach { match ->
            val isNegated = input.getOrNull(match.range.first - 1) == '-'
            val assignee = match.groupValues[1]
            val filter = SearchFilter.Assignee(assignee)
            
            if (isNegated) {
                negatedFilters.add(filter)
            } else {
                filters.add(filter)
            }
        }
        
        // Parse assignee:username format
        ASSIGNEE_FILTER_PATTERN.findAll(input).forEach { match ->
            val isNegated = input.getOrNull(match.range.first - 1) == '-'
            val assignee = match.groupValues[1]
            val filter = SearchFilter.Assignee(assignee)
            
            if (isNegated) {
                negatedFilters.add(filter)
            } else {
                filters.add(filter)
            }
        }
    }
    
    private fun parseTagFilters(
        input: String,
        filters: MutableList<SearchFilter>,
        negatedFilters: MutableList<SearchFilter>
    ) {
        // Parse #tag format
        TAG_HASH_PATTERN.findAll(input).forEach { match ->
            val isNegated = input.getOrNull(match.range.first - 1) == '-'
            val tag = match.groupValues[1]
            val filter = SearchFilter.Tag(tag)
            
            if (isNegated) {
                negatedFilters.add(filter)
            } else {
                filters.add(filter)
            }
        }
        
        // Parse tag:name format
        TAG_PATTERN.findAll(input).forEach { match ->
            val isNegated = input.getOrNull(match.range.first - 1) == '-'
            val tag = match.groupValues[1]
            val filter = SearchFilter.Tag(tag)
            
            if (isNegated) {
                negatedFilters.add(filter)
            } else {
                filters.add(filter)
            }
        }
    }
    
    private fun parseDateFilters(
        input: String,
        filters: MutableList<SearchFilter>,
        negatedFilters: MutableList<SearchFilter>
    ) {
        // Parse due: filters
        DUE_PATTERN.findAll(input).forEach { match ->
            val isNegated = input.getOrNull(match.range.first - 1) == '-'
            val predicateStr = match.groupValues[1].uppercase()
            
            parseDatePredicate(predicateStr)?.let { predicate ->
                val filter = SearchFilter.DueDate(predicate)
                if (isNegated) {
                    negatedFilters.add(filter)
                } else {
                    filters.add(filter)
                }
            }
        }
        
        // Parse created: filters
        CREATED_PATTERN.findAll(input).forEach { match ->
            val isNegated = input.getOrNull(match.range.first - 1) == '-'
            val predicateStr = match.groupValues[1].uppercase()
            
            parseDatePredicate(predicateStr)?.let { predicate ->
                val filter = SearchFilter.CreatedDate(predicate)
                if (isNegated) {
                    negatedFilters.add(filter)
                } else {
                    filters.add(filter)
                }
            }
        }
        
        // Parse updated: filters
        UPDATED_PATTERN.findAll(input).forEach { match ->
            val isNegated = input.getOrNull(match.range.first - 1) == '-'
            val predicateStr = match.groupValues[1].uppercase()
            
            parseDatePredicate(predicateStr)?.let { predicate ->
                val filter = SearchFilter.UpdatedDate(predicate)
                if (isNegated) {
                    negatedFilters.add(filter)
                } else {
                    filters.add(filter)
                }
            }
        }
    }
    
    private fun parseIsFilters(
        input: String,
        filters: MutableList<SearchFilter>,
        negatedFilters: MutableList<SearchFilter>
    ) {
        IS_PATTERN.findAll(input).forEach { match ->
            val isNegated = input.getOrNull(match.range.first - 1) == '-'
            val value = match.groupValues[1].lowercase()
            
            val filter = when (value) {
                "done" -> SearchFilter.Status(TaskStatus.DONE)
                "todo" -> SearchFilter.Status(TaskStatus.TODO)
                "urgent" -> SearchFilter.Priority(TaskPriority.HIGH)
                "unassigned" -> SearchFilter.IsUnassigned
                "overdue" -> SearchFilter.IsOverdue
                "due_soon", "duesoon" -> SearchFilter.IsDueSoon
                "active" -> SearchFilter.IsActive
                "tracked" -> SearchFilter.IsTracked
                "parent" -> SearchFilter.HasSubtasks
                "archived" -> SearchFilter.Status(TaskStatus.ARCHIVED)
                else -> null
            }
            
            filter?.let {
                if (isNegated) {
                    negatedFilters.add(it)
                } else {
                    filters.add(it)
                }
            }
        }
    }
    
    private fun parseHasFilters(
        input: String,
        filters: MutableList<SearchFilter>,
        negatedFilters: MutableList<SearchFilter>
    ) {
        HAS_PATTERN.findAll(input).forEach { match ->
            val isNegated = input.getOrNull(match.range.first - 1) == '-'
            val value = match.groupValues[1].lowercase()
            
            val filter = when (value) {
                "subtasks" -> SearchFilter.HasSubtasks
                "notes" -> SearchFilter.HasNotes
                else -> null
            }
            
            filter?.let {
                if (isNegated) {
                    negatedFilters.add(it)
                } else {
                    filters.add(it)
                }
            }
        }
    }
    
    private fun parseDatePredicate(predicateStr: String): DatePredicate? {
        return when (predicateStr) {
            "TODAY" -> DatePredicate.TODAY
            "TOMORROW" -> DatePredicate.TOMORROW
            "YESTERDAY" -> DatePredicate.YESTERDAY
            "THIS_WEEK", "THISWEEK" -> DatePredicate.THIS_WEEK
            "NEXT_WEEK", "NEXTWEEK" -> DatePredicate.NEXT_WEEK
            "LAST_WEEK", "LASTWEEK" -> DatePredicate.LAST_WEEK
            "THIS_MONTH", "THISMONTH" -> DatePredicate.THIS_MONTH
            "NEXT_MONTH", "NEXTMONTH" -> DatePredicate.NEXT_MONTH
            "LAST_MONTH", "LASTMONTH" -> DatePredicate.LAST_MONTH
            "OVERDUE" -> DatePredicate.OVERDUE
            "DUE_SOON", "DUESOON" -> DatePredicate.DUE_SOON
            else -> null
        }
    }
}
