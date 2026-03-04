package com.example.klarity.presentation.screen.tasks

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Inline Quick Add Component for Kanban Columns
 * 
 * Allows users to quickly create tasks without opening a dialog.
 * Features two states: collapsed (button) and expanded (input field).
 * 
 * Collapsed state:
 * - Shows "+ Add task" button
 * - Light styling with onSurfaceVariant color
 * 
 * Expanded state:
 * - Text input field with auto-focus
 * - Quick actions (confirm/cancel)
 * - Enter to create task
 * - Esc to cancel
 * 
 * Enhanced features:
 * - Detects priority keywords: #high, #urgent → High priority
 * - Detects assignee: @name → Set assignee
 * - Shows pills for detected metadata
 * 
 * @param columnStatus The status of the column this component is in
 * @param onTaskCreate Callback when a task is created (title, status)
 * @param modifier Optional modifier for the component
 */
@Composable
fun InlineQuickAdd(
    columnStatus: TaskStatus,
    onTaskCreate: (title: String, status: TaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    
    // Parse metadata from input
    val parsedData = remember(taskTitle) { parseTaskInput(taskTitle) }
    
    // Auto-focus when expanded
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            focusRequester.requestFocus()
        }
    }
    
    // Handle task creation
    fun createTask() {
        val trimmedTitle = parsedData.cleanTitle.trim()
        if (trimmedTitle.isNotEmpty()) {
            onTaskCreate(trimmedTitle, columnStatus)
            taskTitle = ""
            // Stay expanded for quick batch adding
        }
    }
    
    // Handle cancel
    fun cancelInput() {
        taskTitle = ""
        isExpanded = false
    }
    
    AnimatedContent(
        targetState = isExpanded,
        transitionSpec = {
            fadeIn(animationSpec = tween(200)) + expandVertically(
                animationSpec = tween(200),
                expandFrom = Alignment.Top
            ) togetherWith fadeOut(animationSpec = tween(200)) + shrinkVertically(
                animationSpec = tween(200),
                shrinkTowards = Alignment.Top
            )
        },
        label = "inline_quick_add_transition"
    ) { expanded ->
        if (expanded) {
            ExpandedState(
                taskTitle = taskTitle,
                onTitleChange = { taskTitle = it },
                onConfirm = ::createTask,
                onCancel = ::cancelInput,
                focusRequester = focusRequester,
                parsedData = parsedData,
                modifier = modifier
            )
        } else {
            CollapsedState(
                onClick = { isExpanded = true },
                modifier = modifier
            )
        }
    }
}

/**
 * Collapsed state - Simple button to expand
 */
@Composable
private fun CollapsedState(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add task",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Add task",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Expanded state - Input field with actions
 */
@Composable
private fun ExpandedState(
    taskTitle: String,
    onTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    focusRequester: FocusRequester,
    parsedData: ParsedTaskData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Text input field
            TextField(
                value = taskTitle,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onKeyEvent { keyEvent ->
                        when {
                            // Enter to create task (without Shift)
                            keyEvent.key == Key.Enter && 
                            keyEvent.type == KeyEventType.KeyDown && 
                            !keyEvent.isShiftPressed -> {
                                onConfirm()
                                true
                            }
                            // Esc to cancel
                            keyEvent.key == Key.Escape && 
                            keyEvent.type == KeyEventType.KeyDown -> {
                                onCancel()
                                true
                            }
                            else -> false
                        }
                    },
                placeholder = {
                    Text(
                        text = "Task title...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                minLines = 1,
                maxLines = 3
            )
            
            // Show detected metadata pills
            if (parsedData.hasPriority || parsedData.hasAssignee) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (parsedData.hasPriority) {
                        MetadataPill(
                            emoji = "🔴",
                            text = "High Priority",
                            color = Color(0xFFEF4444)
                        )
                    }
                    if (parsedData.hasAssignee) {
                        parsedData.assignee?.let { assignee ->
                            MetadataPill(
                                emoji = "👤",
                                text = assignee,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            // Bottom row: Hint text and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hint text
                Text(
                    text = "Enter to add • Esc to cancel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Cancel button
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    // Confirm button
                    IconButton(
                        onClick = onConfirm,
                        modifier = Modifier.size(32.dp),
                        enabled = taskTitle.trim().isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Add task",
                            modifier = Modifier.size(16.dp),
                            tint = if (taskTitle.trim().isNotEmpty())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Metadata pill component for showing detected metadata
 */
@Composable
private fun MetadataPill(
    emoji: String,
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 10.sp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Data class to hold parsed task input
 */
private data class ParsedTaskData(
    val cleanTitle: String,
    val hasPriority: Boolean,
    val hasAssignee: Boolean,
    val assignee: String?
)

/**
 * Parse task input for metadata keywords
 * 
 * Detects:
 * - Priority: #high, #urgent, #important → High priority
 * - Assignee: @name → Set assignee
 * 
 * Returns cleaned title without metadata keywords and parsed data
 */
private fun parseTaskInput(input: String): ParsedTaskData {
    var cleanTitle = input
    var hasPriority = false
    var hasAssignee = false
    var assignee: String? = null
    
    // Detect priority keywords
    val priorityKeywords = listOf("#high", "#urgent", "#important")
    hasPriority = priorityKeywords.any { keyword ->
        input.lowercase().contains(keyword.lowercase())
    }
    
    // Remove priority keywords from title
    priorityKeywords.forEach { keyword ->
        cleanTitle = cleanTitle.replace(Regex("$keyword\\b", RegexOption.IGNORE_CASE), "").trim()
    }
    
    // Detect assignee (@name)
    val assigneeRegex = Regex("@(\\w+)")
    val assigneeMatch = assigneeRegex.find(input)
    if (assigneeMatch != null) {
        hasAssignee = true
        assignee = assigneeMatch.groupValues[1].replaceFirstChar { it.uppercase() }
        // Remove @mention from title
        cleanTitle = cleanTitle.replace(assigneeRegex, "").trim()
    }
    
    // Clean up extra whitespace
    cleanTitle = cleanTitle.replace(Regex("\\s+"), " ").trim()
    
    return ParsedTaskData(
        cleanTitle = cleanTitle,
        hasPriority = hasPriority,
        hasAssignee = hasAssignee,
        assignee = assignee
    )
}
