package com.example.klarity.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.screen.tasks.TaskPriority
import com.example.klarity.presentation.screen.tasks.TaskStatus

/**
 * Bulk Actions Bar
 * 
 * Appears at the bottom of the screen when tasks are selected.
 * Provides quick access to bulk operations like move, priority, tag, and delete.
 * 
 * Features:
 * - Slide-up animation from bottom
 * - Material 3 elevated surface
 * - Selection count display
 * - Clear selection button
 * - Select all option
 * - Action buttons with icons
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BulkActionsBar(
    selectedCount: Int,
    visible: Boolean,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onMoveToClick: () -> Unit,
    onPriorityClick: () -> Unit,
    onTagClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(
                durationMillis = 200,
                easing = LinearOutSlowInEasing
            )
        ) + fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Selection info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onClearSelection) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear selection"
                        )
                    }
                    
                    Text(
                        text = "$selectedCount selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    TextButton(onClick = onSelectAll) {
                        Text("Select All")
                    }
                }
                
                // Right: Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Move to
                    FilledTonalButton(
                        onClick = onMoveToClick,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DriveFileMove,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Move")
                    }
                    
                    // Priority
                    FilledTonalButton(
                        onClick = onPriorityClick,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Priority")
                    }
                    
                    // Tag
                    FilledTonalButton(
                        onClick = onTagClick,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Tag")
                    }
                    
                    // Delete
                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

/**
 * Priority selection dropdown menu for bulk operations.
 */
@Composable
fun BulkPriorityMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onPrioritySelected: (TaskPriority) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        TaskPriority.entries.forEach { priority ->
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(priority.emoji)
                        Text(priority.label)
                    }
                },
                onClick = {
                    onPrioritySelected(priority)
                    onDismiss()
                }
            )
        }
    }
}

/**
 * Compact bulk actions bar for smaller screens.
 * Shows icon-only buttons to save space.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CompactBulkActionsBar(
    selectedCount: Int,
    visible: Boolean,
    onClearSelection: () -> Unit,
    onMoveToClick: () -> Unit,
    onPriorityClick: () -> Unit,
    onTagClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200, easing = LinearOutSlowInEasing)
        ) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Selection count and clear
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onClearSelection) {
                        Icon(Icons.Default.Close, "Clear")
                    }
                    Text(
                        text = "$selectedCount",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Right: Action icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onMoveToClick) {
                        Icon(Icons.Default.DriveFileMove, "Move")
                    }
                    IconButton(onClick = onPriorityClick) {
                        Icon(Icons.Default.PriorityHigh, "Priority")
                    }
                    IconButton(onClick = onTagClick) {
                        Icon(Icons.Default.Label, "Tag")
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
        }
    }
}
