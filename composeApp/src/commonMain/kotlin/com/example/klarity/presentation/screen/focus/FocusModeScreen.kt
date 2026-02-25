package com.example.klarity.presentation.screen.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.screen.tasks.Subtask
import kotlin.time.Duration.Companion.minutes

/**
 * Focus Mode Screen - Distraction-free deep work interface.
 * 
 * Full-screen immersive view for focused task work with:
 * - Large, centered task display
 * - Pomodoro timer with circular progress
 * - Minimal UI
 * - Keyboard-only controls
 * - Dark background for eye comfort
 * 
 * Keyboard shortcuts:
 * - Space: Pause/Resume
 * - Enter: Complete task
 * - N: Next task
 * - R: Reset timer
 * - +/-: Add/subtract 5 minutes
 * - B: Start break manually
 * - S: Open settings
 * - Q/Esc: Exit focus mode
 * - ?: Show keyboard shortcuts
 */
@Composable
fun FocusModeScreen(
    state: FocusModeState,
    onPauseToggle: () -> Unit,
    onReset: () -> Unit,
    onComplete: () -> Unit,
    onExit: () -> Unit,
    onAddTime: () -> Unit,
    onSubtractTime: () -> Unit,
    onStartBreak: () -> Unit,
    onToggleSubtask: (String) -> Unit,
    onDismissTransition: () -> Unit,
    onShowSettings: () -> Unit,
    onToggleKeyboardHelp: () -> Unit,
    onUpdateSettings: (FocusTimerSettings) -> Unit,
    onConfirmExit: () -> Unit,
    onCancelExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle keyboard shortcuts
    var keyHandlerModifier = modifier.onKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            when (keyEvent.key) {
                Key.Spacebar -> {
                    onPauseToggle()
                    true
                }
                Key.Enter -> {
                    onComplete()
                    true
                }
                Key.N -> {
                    // Next task (if queue exists)
                    true
                }
                Key.R -> {
                    onReset()
                    true
                }
                Key.Plus, Key.Equals -> {
                    onAddTime()
                    true
                }
                Key.Minus -> {
                    onSubtractTime()
                    true
                }
                Key.B -> {
                    onStartBreak()
                    true
                }
                Key.S -> {
                    onShowSettings()
                    true
                }
                Key.Q, Key.Escape -> {
                    onExit()
                    true
                }
                Key.Slash -> {
                    if (keyEvent.isShiftPressed) {
                        // ? key
                        onToggleKeyboardHelp()
                        true
                    } else false
                }
                else -> false
            }
        } else false
    }
    
    Box(
        modifier = keyHandlerModifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // Ambient background animation
        AmbientBackground()
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section: Exit hint
            KeyboardHint(
                hint = "[Esc] to exit • [?] for shortcuts",
                modifier = Modifier.align(Alignment.Start)
            )
            
            // Middle section: Task and timer
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Task title
                FocusTaskTitle(
                    title = state.task.title,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
                
                // Timer
                FocusTimer(
                    timeRemaining = state.timeRemaining,
                    phase = state.phase,
                    isPaused = state.isPaused,
                    progress = state.progress,
                    onPauseToggle = onPauseToggle,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                // Session progress
                SessionProgress(
                    sessionsCompleted = state.sessionsCompleted,
                    totalSessions = state.settings.sessionsUntilLongBreak,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
                
                // Subtasks progress (if any)
                if (state.task.subtasks.isNotEmpty()) {
                    val completedSubtasks = state.task.subtasks.count { it.isCompleted }
                    SubtaskProgressBar(
                        completed = completedSubtasks,
                        total = state.task.subtasks.size,
                        modifier = Modifier
                            .width(400.dp)
                            .padding(bottom = 32.dp)
                    )
                    
                    // Subtasks list
                    FocusSubtasksList(
                        subtasks = state.task.subtasks,
                        onToggle = onToggleSubtask,
                        modifier = Modifier.width(400.dp)
                    )
                }
            }
            
            // Bottom section: Controls hint
            FocusControlsHint()
        }
        
        // Phase transition overlay
        PhaseTransitionOverlay(
            fromPhase = state.phase,
            toPhase = when (state.phase) {
                FocusPhase.WORK -> if (state.isLongBreakTime) FocusPhase.LONG_BREAK else FocusPhase.SHORT_BREAK
                FocusPhase.SHORT_BREAK, FocusPhase.LONG_BREAK -> FocusPhase.WORK
                FocusPhase.COMPLETED -> FocusPhase.COMPLETED
            },
            visible = state.showTransition,
            onDismiss = onDismissTransition
        )
        
        // Settings dialog
        if (state.showSettingsDialog) {
            FocusSettingsDialog(
                settings = state.settings,
                onSettingsChange = onUpdateSettings,
                onDismiss = onShowSettings
            )
        }
        
        // Exit confirmation dialog
        ExitFocusModeDialog(
            visible = state.showExitDialog,
            timeRemaining = state.timeRemaining,
            onConfirm = onConfirmExit,
            onCancel = onCancelExit
        )
        
        // Keyboard shortcuts overlay
        KeyboardShortcutsOverlay(
            visible = state.showKeyboardHelp,
            onDismiss = onToggleKeyboardHelp
        )
    }
}

/**
 * Subtasks list for focus mode.
 */
@Composable
private fun FocusSubtasksList(
    subtasks: List<Subtask>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(subtasks) { subtask ->
            FocusSubtaskItem(
                subtask = subtask,
                onToggle = { onToggle(subtask.id) }
            )
        }
    }
}

/**
 * Individual subtask item in focus mode.
 */
@Composable
private fun FocusSubtaskItem(
    subtask: Subtask,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = subtask.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = Color.White.copy(alpha = 0.3f),
                    checkmarkColor = Color.White
                )
            )
            
            Text(
                text = subtask.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = if (subtask.isCompleted) 0.5f else 0.9f),
                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else null
            )
        }
    }
}

/**
 * Preview version of FocusModeScreen with sample data.
 */
@Composable
fun FocusModeScreenPreview() {
    val sampleTask = com.example.klarity.presentation.screen.tasks.Task(
        id = "preview-task",
        title = "Complete project documentation",
        description = "Write comprehensive documentation for the focus mode feature",
        status = com.example.klarity.presentation.screen.tasks.TaskStatus.IN_PROGRESS,
        priority = com.example.klarity.presentation.screen.tasks.TaskPriority.HIGH,
        subtasks = listOf(
            Subtask(id = "1", title = "Write user guide", isCompleted = true, order = 0),
            Subtask(id = "2", title = "Create API documentation", isCompleted = false, order = 1),
            Subtask(id = "3", title = "Add code examples", isCompleted = false, order = 2)
        ),
        createdAt = kotlinx.datetime.Clock.System.now(),
        updatedAt = kotlinx.datetime.Clock.System.now()
    )
    
    val sampleState = FocusModeState(
        task = sampleTask,
        phase = FocusPhase.WORK,
        timeRemaining = 23.minutes,
        sessionsCompleted = 2,
        isPaused = false
    )
    
    FocusModeScreen(
        state = sampleState,
        onPauseToggle = {},
        onReset = {},
        onComplete = {},
        onExit = {},
        onAddTime = {},
        onSubtractTime = {},
        onStartBreak = {},
        onToggleSubtask = {},
        onDismissTransition = {},
        onShowSettings = {},
        onToggleKeyboardHelp = {},
        onUpdateSettings = {},
        onConfirmExit = {},
        onCancelExit = {}
    )
}
