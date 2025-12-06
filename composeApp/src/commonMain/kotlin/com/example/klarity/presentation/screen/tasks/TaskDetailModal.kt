package com.example.klarity.presentation.screen.tasks

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.theme.KlarityColors

/**
 * TaskDetailModal - Bottom sheet modal for viewing and editing task details.
 * 
 * **Requirements 4.1, 4.2, 4.3, 4.4**:
 * - Displays task ID, title, description, subtasks, activity log, and properties
 * - Animates with slide-up transition from the bottom
 * - Closes on backdrop click or close button with slide-down animation
 * - Shows status, priority, and story points in a sidebar section
 */
@Composable
fun TaskDetailModal(
    task: Task?,
    isVisible: Boolean,
    onClose: () -> Unit,
    onTaskUpdate: (Task) -> Unit,
    onAddTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Backdrop with blur effect and click-to-close (Requirement 4.3)
    AnimatedVisibility(
        visible = isVisible && task != null,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KlarityColors.ModalOverlay)
                .clickable(
                    onClick = onClose,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        )
    }


    // Bottom sheet with slide-up/slide-down animation (Requirement 4.2)
    AnimatedVisibility(
        visible = isVisible && task != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(150)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            task?.let { currentTask ->
                TaskDetailContent(
                    task = currentTask,
                    onClose = onClose,
                    onTaskUpdate = onTaskUpdate,
                    onAddTag = onAddTag,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(KlarityColors.BgSecondary)
                        .clickable(
                            onClick = { /* Prevent click propagation to backdrop */ },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                )
            }
        }
    }
}

/**
 * Main content of the task detail modal.
 * 
 * **Requirement 4.1**: Display task ID, title, description, subtasks, activity log, and properties
 */
@Composable
private fun TaskDetailContent(
    task: Task,
    onClose: () -> Unit,
    onTaskUpdate: (Task) -> Unit,
    onAddTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Drag handle bar at top (Requirement 10.2)
        DragHandle()
        
        // Header with task ID badge, title, and close button
        ModalHeader(
            task = task,
            onClose = onClose
        )
        
        // Main content area with scrollable content and properties sidebar
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Left side: Description, subtasks, activity (scrollable)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Description section
                item {
                    DescriptionSection(description = task.description)
                }
                
                // Subtasks section
                if (task.subtasks.isNotEmpty()) {
                    item {
                        SubtasksSection(
                            subtasks = task.subtasks,
                            onSubtaskToggle = { subtaskId ->
                                val updatedSubtasks = task.subtasks.map { subtask ->
                                    if (subtask.id == subtaskId) {
                                        subtask.copy(isCompleted = !subtask.isCompleted)
                                    } else subtask
                                }
                                onTaskUpdate(task.copy(subtasks = updatedSubtasks))
                            }
                        )
                    }
                }
                
                // Activity section
                item {
                    ActivitySection(task = task)
                }
                
                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            
            // Right side: Properties sidebar (Requirement 4.4)
            PropertiesSidebar(
                task = task,
                onAddTag = onAddTag,
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
            )
        }
    }
}
