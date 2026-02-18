package com.example.klarity.presentation.screen.focus

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.klarity.presentation.screen.tasks.Task

/**
 * Focus Mode Integration Example
 * 
 * This file demonstrates how to integrate Focus Mode into the TasksScreen
 * and other parts of the Klarity application.
 */

// ============================================================================
// Integration Example 1: TasksHeader Deep Work Mode Button
// ============================================================================

/**
 * In TasksHeader.kt, the Deep Work Mode button should trigger focus mode:
 * 
 * ```kotlin
 * // In TasksScreen.kt
 * @Composable
 * fun TasksScreen(
 *     viewModel: TasksViewModel = viewModel(),
 *     navController: NavController
 * ) {
 *     val uiState by viewModel.uiState.collectAsState()
 *     var showFocusMode by remember { mutableStateOf(false) }
 *     var focusTask by remember { mutableStateOf<Task?>(null) }
 *     
 *     if (showFocusMode && focusTask != null) {
 *         FocusModeIntegration(
 *             task = focusTask!!,
 *             onExit = {
 *                 showFocusMode = false
 *                 focusTask = null
 *             }
 *         )
 *     } else {
 *         // Regular TasksScreen content
 *         Column {
 *             TasksHeader(
 *                 currentViewMode = uiState.viewMode,
 *                 onViewModeChange = { /* ... */ },
 *                 onDeepWorkModeClick = {
 *                     // Get selected or first task
 *                     val task = getSelectedTask(uiState) ?: getFirstInProgressTask(uiState)
 *                     if (task != null) {
 *                         focusTask = task
 *                         showFocusMode = true
 *                     }
 *                 },
 *                 onNotificationsClick = { /* ... */ },
 *                 onUserAvatarClick = { /* ... */ }
 *             )
 *             // ... rest of screen
 *         }
 *     }
 * }
 * ```
 */

/**
 * Helper function to get the selected task from UI state.
 */
fun getSelectedTask(uiState: com.example.klarity.presentation.state.TasksUiState): Task? {
    return when (uiState) {
        is com.example.klarity.presentation.state.TasksUiState.Success -> uiState.selectedTask
        else -> null
    }
}

/**
 * Helper function to get the first in-progress task.
 */
fun getFirstInProgressTask(uiState: com.example.klarity.presentation.state.TasksUiState): Task? {
    return when (uiState) {
        is com.example.klarity.presentation.state.TasksUiState.Success -> {
            uiState.columns
                .flatMap { it.tasks }
                .firstOrNull { it.status == com.example.klarity.presentation.screen.tasks.TaskStatus.IN_PROGRESS }
        }
        else -> null
    }
}

// ============================================================================
// Integration Example 2: Full Focus Mode Integration Component
// ============================================================================

/**
 * Complete Focus Mode integration component with ViewModel.
 * 
 * Usage:
 * ```kotlin
 * FocusModeIntegration(
 *     task = myTask,
 *     onExit = { /* handle exit */ }
 * )
 * ```
 */
@Composable
fun FocusModeIntegration(
    task: Task,
    settings: FocusTimerSettings = FocusTimerSettings(),
    onExit: () -> Unit,
    viewModel: FocusModeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    
    // Start focus session on first composition
    LaunchedEffect(task.id) {
        viewModel.startFocusSession(task, settings)
    }
    
    // Handle exit
    var showExitDialog by remember { mutableStateOf(false) }
    
    if (state != null) {
        FocusModeScreen(
            state = state!!,
            onPauseToggle = {
                if (state!!.isPaused) {
                    viewModel.resumeTimer()
                } else {
                    viewModel.pauseTimer()
                }
            },
            onReset = { viewModel.resetTimer() },
            onComplete = { 
                viewModel.completeTask()
                onExit()
            },
            onExit = { 
                viewModel.showExitDialog()
            },
            onAddTime = { viewModel.addTime(kotlin.time.Duration.parse("5m")) },
            onSubtractTime = { viewModel.subtractTime(kotlin.time.Duration.parse("5m")) },
            onStartBreak = { viewModel.startBreakManually() },
            onToggleSubtask = { subtaskId -> viewModel.toggleSubtask(subtaskId) },
            onDismissTransition = { viewModel.dismissTransition() },
            onShowSettings = { 
                if (state!!.showSettingsDialog) {
                    viewModel.dismissSettingsDialog()
                } else {
                    viewModel.showSettingsDialog()
                }
            },
            onToggleKeyboardHelp = { viewModel.toggleKeyboardHelp() },
            onUpdateSettings = { newSettings -> viewModel.updateSettings(newSettings) },
            onConfirmExit = {
                viewModel.exitFocusMode()
                onExit()
            },
            onCancelExit = { viewModel.dismissExitDialog() }
        )
    }
}

// ============================================================================
// Integration Example 3: Navigation Integration
// ============================================================================

/**
 * Navigation setup for Focus Mode:
 * 
 * In KlarityNavigation.kt or Screen.kt:
 * 
 * ```kotlin
 * sealed class Screen(val route: String) {
 *     // ... existing screens
 *     object FocusMode : Screen("focus/{taskId}") {
 *         fun createRoute(taskId: String) = "focus/$taskId"
 *     }
 * }
 * 
 * // In navigation setup
 * composable(
 *     route = Screen.FocusMode.route,
 *     arguments = listOf(navArgument("taskId") { type = NavType.StringType })
 * ) { backStackEntry ->
 *     val taskId = backStackEntry.arguments?.getString("taskId")
 *     
 *     // Get task from ViewModel or repository
 *     val task = tasksViewModel.getTaskById(taskId)
 *     
 *     if (task != null) {
 *         FocusModeIntegration(
 *             task = task,
 *             onExit = { navController.popBackStack() }
 *         )
 *     }
 * }
 * ```
 */

// ============================================================================
// Integration Example 4: Dependency Injection Setup
// ============================================================================

/**
 * In AppModule.kt or similar DI configuration:
 * 
 * ```kotlin
 * // Focus Mode dependencies
 * single<FocusSoundPlayer> { 
 *     // Platform-specific implementation
 *     if (Platform.isDesktop) {
 *         DesktopFocusSoundPlayer()
 *     } else {
 *         NoOpFocusSoundPlayer()
 *     }
 * }
 * 
 * single<FocusNotifications> {
 *     if (Platform.isDesktop) {
 *         DesktopFocusNotifications()
 *     } else {
 *         NoOpFocusNotifications()
 *     }
 * }
 * 
 * viewModel { 
 *     FocusModeViewModel(
 *         taskRepository = get(),
 *         soundPlayer = get()
 *     )
 * }
 * ```
 */

// ============================================================================
// Integration Example 5: Task Context Menu
// ============================================================================

/**
 * Add "Focus on this task" to task context menu:
 * 
 * ```kotlin
 * @Composable
 * fun TaskContextMenu(
 *     task: Task,
 *     onFocusMode: (Task) -> Unit,
 *     // ... other actions
 * ) {
 *     DropdownMenu(/* ... */) {
 *         DropdownMenuItem(
 *             text = { Text("Focus on this task") },
 *             leadingIcon = { Icon(Icons.Default.Spa, null) },
 *             onClick = { onFocusMode(task) }
 *         )
 *         // ... other menu items
 *     }
 * }
 * ```
 */

// ============================================================================
// Integration Example 6: Keyboard Shortcut
// ============================================================================

/**
 * Add global keyboard shortcut to enter focus mode:
 * 
 * In KeyboardShortcuts.kt or similar:
 * 
 * ```kotlin
 * modifier = Modifier.onKeyEvent { event ->
 *     when {
 *         event.isCtrlPressed && event.key == Key.F -> {
 *             // Enter focus mode with selected task
 *             enterFocusMode()
 *             true
 *         }
 *         // ... other shortcuts
 *         else -> false
 *     }
 * }
 * ```
 */

// ============================================================================
// Platform-Specific Notes
// ============================================================================

/**
 * DESKTOP (JVM) IMPLEMENTATION NOTES:
 * 
 * 1. Sound playback:
 *    - Use javax.sound.sampled.AudioSystem
 *    - Load sound files from resources
 *    - Example: AudioSystem.getClip().open(audioInputStream)
 * 
 * 2. Desktop notifications:
 *    - Use java.awt.SystemTray
 *    - Display tray notifications
 *    - Example: trayIcon.displayMessage("Focus", "Session complete!", TrayIcon.MessageType.INFO)
 * 
 * 3. Window management:
 *    - Can use Window.setAlwaysOnTop(true) for distraction-free mode
 *    - Consider fullscreen mode with Window.setExtendedState()
 * 
 * ANDROID IMPLEMENTATION NOTES:
 * 
 * 1. Sound playback:
 *    - Use MediaPlayer or SoundPool
 *    - Load from res/raw
 * 
 * 2. Notifications:
 *    - Use NotificationManager
 *    - Create notification channel
 *    - Show heads-up notification for completed sessions
 * 
 * 3. Keep screen on:
 *    - Use WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
 * 
 * IOS IMPLEMENTATION NOTES:
 * 
 * 1. Sound playback:
 *    - Use AVFoundation / AVAudioPlayer
 * 
 * 2. Notifications:
 *    - Use UNUserNotificationCenter
 * 
 * 3. Keep screen on:
 *    - UIApplication.shared.isIdleTimerDisabled = true
 */
