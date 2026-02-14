package com.example.klarity.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.klarity.presentation.screen.tasks.KanbanColumn
import com.example.klarity.presentation.screen.tasks.TaskStatus

/**
 * Bulk Move Dialog
 * 
 * Allows moving multiple selected tasks to a chosen column/status.
 * Shows a list of available columns with task counts and visual indicators.
 */
@Composable
fun BulkMoveDialog(
    visible: Boolean,
    selectedCount: Int,
    currentColumns: List<KanbanColumn>,
    onMove: (TaskStatus) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    
    var selectedStatus by remember { mutableStateOf<TaskStatus?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.width(400.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Text(
                    text = "Move $selectedCount ${if (selectedCount == 1) "task" else "tasks"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "Select destination column",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Column list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 400.dp)
                ) {
                    items(currentColumns) { column ->
                        ColumnOption(
                            column = column,
                            isSelected = selectedStatus == column.status,
                            onClick = { selectedStatus = column.status }
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Preview
                if (selectedStatus != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            val targetColumn = currentColumns.find { it.status == selectedStatus }
                            Text(
                                text = "Move to ${targetColumn?.status?.label ?: ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                }
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            selectedStatus?.let { status ->
                                onMove(status)
                                onDismiss()
                            }
                        },
                        enabled = selectedStatus != null
                    ) {
                        Text("Move")
                    }
                }
            }
        }
    }
}

/**
 * Individual column option in the move dialog.
 */
@Composable
private fun ColumnOption(
    column: KanbanColumn,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status emoji
                Text(
                    text = column.status.emoji,
                    style = MaterialTheme.typography.titleLarge
                )
                
                // Column info
                Column {
                    Text(
                        text = column.status.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    Text(
                        text = "${column.tasks.size} ${if (column.tasks.size == 1) "task" else "tasks"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                }
            }
            
            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Quick move dropdown for single column selection.
 * More compact alternative to the full dialog.
 */
@Composable
fun QuickMoveMenu(
    expanded: Boolean,
    selectedCount: Int,
    currentColumns: List<KanbanColumn>,
    onMove: (TaskStatus) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        // Header
        Text(
            text = "Move $selectedCount to",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Divider()
        
        // Column options
        currentColumns.forEach { column ->
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(column.status.emoji)
                        
                        Column {
                            Text(
                                text = column.status.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${column.tasks.size} tasks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                onClick = {
                    onMove(column.status)
                    onDismiss()
                }
            )
        }
    }
}
