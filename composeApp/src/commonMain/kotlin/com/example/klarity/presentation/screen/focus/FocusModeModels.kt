package com.example.klarity.presentation.screen.focus

import androidx.compose.runtime.Immutable
import com.example.klarity.presentation.screen.tasks.Task
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Focus Mode Data Models
 * 
 * Models for the deep work/focus mode feature that helps users
 * concentrate on a single task with Pomodoro timer functionality.
 */

// ============================================================================
// Focus Phase
// ============================================================================

/**
 * Represents the different phases in a focus session.
 */
enum class FocusPhase {
    WORK,
    SHORT_BREAK,
    LONG_BREAK,
    COMPLETED
}

// ============================================================================
// Focus Timer Settings
// ============================================================================

/**
 * Configuration for the Pomodoro timer in focus mode.
 * 
 * @property workDuration Duration of a work session (default: 25 minutes)
 * @property breakDuration Duration of a short break (default: 5 minutes)
 * @property longBreakDuration Duration of a long break (default: 15 minutes)
 * @property sessionsUntilLongBreak Number of work sessions before a long break (default: 4)
 * @property autoStartBreak Automatically start break when work session ends
 * @property autoStartWork Automatically start work when break ends
 */
@Immutable
data class FocusTimerSettings(
    val workDuration: Duration = 25.minutes,
    val breakDuration: Duration = 5.minutes,
    val longBreakDuration: Duration = 15.minutes,
    val sessionsUntilLongBreak: Int = 4,
    val autoStartBreak: Boolean = true,
    val autoStartWork: Boolean = false
)

// ============================================================================
// Focus Mode State
// ============================================================================

/**
 * Represents the current state of a focus session.
 * 
 * @property task The task being focused on
 * @property phase Current phase (work, break, etc.)
 * @property timeRemaining Time remaining in current phase
 * @property sessionsCompleted Number of work sessions completed
 * @property isPaused Whether the timer is currently paused
 * @property showTransition Whether to show phase transition overlay
 * @property showExitDialog Whether to show exit confirmation dialog
 * @property showSettingsDialog Whether to show settings dialog
 * @property showKeyboardHelp Whether to show keyboard shortcuts overlay
 */
@Immutable
data class FocusModeState(
    val task: Task,
    val phase: FocusPhase,
    val timeRemaining: Duration,
    val sessionsCompleted: Int,
    val isPaused: Boolean = false,
    val showTransition: Boolean = false,
    val showExitDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showKeyboardHelp: Boolean = false,
    val settings: FocusTimerSettings = FocusTimerSettings()
) {
    /**
     * Calculate the progress percentage for the current phase.
     */
    val progress: Float
        get() {
            val totalDuration = when (phase) {
                FocusPhase.WORK -> settings.workDuration
                FocusPhase.SHORT_BREAK -> settings.breakDuration
                FocusPhase.LONG_BREAK -> settings.longBreakDuration
                FocusPhase.COMPLETED -> Duration.ZERO
            }
            
            if (totalDuration == Duration.ZERO) return 1f
            
            val elapsed = totalDuration - timeRemaining
            return (elapsed.inWholeSeconds.toFloat() / totalDuration.inWholeSeconds.toFloat())
                .coerceIn(0f, 1f)
        }
    
    /**
     * Check if it's time for a long break.
     */
    val isLongBreakTime: Boolean
        get() = sessionsCompleted > 0 && sessionsCompleted % settings.sessionsUntilLongBreak == 0
    
    /**
     * Format time remaining as MM:SS.
     */
    val formattedTime: String
        get() {
            val totalSeconds = timeRemaining.inWholeSeconds
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}

// ============================================================================
// Focus Session Record
// ============================================================================

/**
 * Represents a completed focus session for statistics tracking.
 * 
 * @property taskId ID of the task that was focused on
 * @property startTime When the session started
 * @property duration How long the session lasted
 * @property completed Whether the session was completed (not exited early)
 * @property sessionsCompleted Number of Pomodoro sessions completed
 */
@Immutable
data class FocusSession(
    val taskId: String,
    val startTime: Instant,
    val duration: Duration,
    val completed: Boolean,
    val sessionsCompleted: Int = 0
)

// ============================================================================
// Focus Queue Item
// ============================================================================

/**
 * Represents a task in the focus queue.
 * 
 * @property task The task to focus on
 * @property order Position in the queue
 * @property isActive Whether this is the currently active task
 */
@Immutable
data class FocusQueueItem(
    val task: Task,
    val order: Int,
    val isActive: Boolean = false
)
