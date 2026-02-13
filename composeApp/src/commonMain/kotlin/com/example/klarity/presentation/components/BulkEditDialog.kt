package com.example.klarity.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.klarity.presentation.screen.tasks.TaskPriority
import com.example.klarity.presentation.screen.tasks.TaskStatus
import com.example.klarity.presentation.screen.tasks.TaskTag
import kotlinx.datetime.LocalDate

/**
 * Bulk Edit Dialog
 * 
 * Allows editing multiple properties across selected tasks:
 * - Priority
 * - Status
 * - Assignee
 * - Tags (add/remove)
 * - Due date
 * 
 * Features checkboxes to enable/disable each property change.
 */

/**
 * Properties that can be edited in bulk.
 */
data class BulkEditProperties(
    val priority: TaskPriority? = null,
    val status: TaskStatus? = null,
    val assignee: String? = null,
    val addTags: List<TaskTag> = emptyList(),
    val removeTags: List<TaskTag> = emptyList(),
    val dueDate: LocalDate? = null,
    val clearDueDate: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkEditDialog(
    visible: Boolean,
    selectedCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (BulkEditProperties) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    
    // State for each property
    var changePriority by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    
    var changeStatus by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf(TaskStatus.TODO) }
    
    var changeAssignee by remember { mutableStateOf(false) }
    var assigneeName by remember { mutableStateOf("") }
    
    var addTags by remember { mutableStateOf(false) }
    var selectedTags by remember { mutableStateOf(emptyList<TaskTag>()) }
    
    var changeDueDate by remember { mutableStateOf(false) }
    var clearDueDate by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.width(500.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Text(
                    text = "Edit $selectedCount tasks",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "Select properties to change",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Priority section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = changePriority,
                            onCheckedChange = { changePriority = it }
                        )
                        Text("Change priority")
                    }
                    
                    if (changePriority) {
                        var expandedPriority by remember { mutableStateOf(false) }
                        
                        Box {
                            FilledTonalButton(onClick = { expandedPriority = true }) {
                                Text(selectedPriority.emoji)
                                Spacer(Modifier.width(4.dp))
                                Text(selectedPriority.label)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            
                            DropdownMenu(
                                expanded = expandedPriority,
                                onDismissRequest = { expandedPriority = false }
                            ) {
                                TaskPriority.entries.forEach { priority ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(priority.emoji)
                                                Text(priority.label)
                                            }
                                        },
                                        onClick = {
                                            selectedPriority = priority
                                            expandedPriority = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Status section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = changeStatus,
                            onCheckedChange = { changeStatus = it }
                        )
                        Text("Change status")
                    }
                    
                    if (changeStatus) {
                        var expandedStatus by remember { mutableStateOf(false) }
                        
                        Box {
                            FilledTonalButton(onClick = { expandedStatus = true }) {
                                Text(selectedStatus.emoji)
                                Spacer(Modifier.width(4.dp))
                                Text(selectedStatus.label)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            
                            DropdownMenu(
                                expanded = expandedStatus,
                                onDismissRequest = { expandedStatus = false }
                            ) {
                                TaskStatus.entries.forEach { status ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(status.emoji)
                                                Text(status.label)
                                            }
                                        },
                                        onClick = {
                                            selectedStatus = status
                                            expandedStatus = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Assignee section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = changeAssignee,
                        onCheckedChange = { changeAssignee = it }
                    )
                    
                    if (changeAssignee) {
                        OutlinedTextField(
                            value = assigneeName,
                            onValueChange = { assigneeName = it },
                            label = { Text("Assign to") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            }
                        )
                    } else {
                        Text("Assign to")
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Tags section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = addTags,
                        onCheckedChange = { addTags = it }
                    )
                    Text("Add tags")
                }
                
                if (addTags) {
                    Text(
                        text = "Tag management coming soon",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 40.dp)
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Due date section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = changeDueDate,
                        onCheckedChange = { changeDueDate = it }
                    )
                    Text("Set due date")
                }
                
                if (changeDueDate) {
                    Row(
                        modifier = Modifier.padding(start = 40.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = clearDueDate,
                            onCheckedChange = { clearDueDate = it }
                        )
                        Text("Clear due date instead")
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Preview
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "$selectedCount tasks will be updated",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
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
                            val properties = BulkEditProperties(
                                priority = if (changePriority) selectedPriority else null,
                                status = if (changeStatus) selectedStatus else null,
                                assignee = if (changeAssignee) assigneeName.takeIf { it.isNotBlank() } else null,
                                addTags = if (addTags) selectedTags else emptyList(),
                                clearDueDate = changeDueDate && clearDueDate
                            )
                            onConfirm(properties)
                        },
                        enabled = changePriority || changeStatus || changeAssignee || addTags || changeDueDate
                    ) {
                        Text("Apply Changes")
                    }
                }
            }
        }
    }
}
