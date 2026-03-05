package com.example.klarity.presentation.integration

/**
 * Integration Guide: How to add Undo/Redo to existing TasksViewModel
 * 
 * This file provides step-by-step instructions for integrating the undo/redo system
 * into the existing TasksViewModel without breaking existing functionality.
 */

/*
 * STEP 1: Add UndoRedoManager to TasksViewModel
 * ================================================
 * 
 * In TasksViewModel.kt, add:
 * 
 * ```kotlin
 * import com.example.klarity.domain.undo.UndoRedoManager
 * import com.example.klarity.domain.undo.TaskCommand
 * 
 * class TasksViewModel(
 *     private val taskRepository: TaskRepository
 * ) : ViewModel() {
 *     
 *     // Add this
 *     private val undoRedoManager = UndoRedoManager(maxHistorySize = 50)
 *     
 *     // Expose state
 *     val canUndo: StateFlow<Boolean> = undoRedoManager.canUndo
 *     val canRedo: StateFlow<Boolean> = undoRedoManager.canRedo
 *     val lastAction: StateFlow<UndoableCommand?> = undoRedoManager.lastAction
 *     
 *     // ... existing code
 * }
 * ```
 */

/*
 * STEP 2: Add Undo/Redo Events to TasksUiEvent
 * =============================================
 * 
 * In TasksUiState.kt, add these events to the sealed interface:
 * 
 * ```kotlin
 * sealed interface TasksUiEvent {
 *     // ... existing events
 *     
 *     // Add these new events
 *     data object Undo : TasksUiEvent
 *     data object Redo : TasksUiEvent
 *     data object ToggleHistoryPanel : TasksUiEvent
 * }
 * ```
 */

/*
 * STEP 3: Update handleTaskDeleted to use UndoableCommand
 * ========================================================
 * 
 * In TasksViewModel.kt, modify the handleTaskDeleted function:
 * 
 * BEFORE:
 * ```kotlin
 * private fun handleTaskDeleted(task: Task) {
 *     viewModelScope.launch {
 *         taskRepository.deleteTask(task.id)
 *             .onSuccess {
 *                 updateSuccessState { state ->
 *                     state.copy(
 *                         selectedTask = if (state.selectedTask?.id == task.id) null else state.selectedTask,
 *                         isModalOpen = if (state.selectedTask?.id == task.id) false else state.isModalOpen
 *                     )
 *                 }
 *                 _effects.send(TasksUiEffect.ShowSnackbar("Task deleted"))
 *             }
 *             .onFailure { error ->
 *                 _effects.send(TasksUiEffect.ShowError("Failed to delete task: ${error.message}"))
 *             }
 *     }
 * }
 * ```
 * 
 * AFTER:
 * ```kotlin
 * private fun handleTaskDeleted(task: Task) {
 *     viewModelScope.launch {
 *         try {
 *             val command = TaskCommand.DeleteTask(task, taskRepository)
 *             undoRedoManager.execute(command)
 *             
 *             updateSuccessState { state ->
 *                 state.copy(
 *                     selectedTask = if (state.selectedTask?.id == task.id) null else state.selectedTask,
 *                     isModalOpen = if (state.selectedTask?.id == task.id) false else state.isModalOpen
 *                 )
 *             }
 *             // Note: Snackbar will be shown automatically by observing lastAction
 *         } catch (error: Exception) {
 *             _effects.send(TasksUiEffect.ShowError("Failed to delete task: ${error.message}"))
 *         }
 *     }
 * }
 * ```
 */

/*
 * STEP 4: Update handleTaskMoved to use UndoableCommand
 * ======================================================
 * 
 * BEFORE:
 * ```kotlin
 * private fun handleTaskMoved(taskId: String, toColumn: TaskStatus, index: Int) {
 *     viewModelScope.launch {
 *         taskRepository.updateTaskStatus(taskId, toColumn, index)
 *             .onSuccess {
 *                 // UI will update via observeTasks flow
 *             }
 *             .onFailure { error ->
 *                 _effects.send(TasksUiEffect.ShowError("Failed to move task: ${error.message}"))
 *             }
 *     }
 * }
 * ```
 * 
 * AFTER:
 * ```kotlin
 * private fun handleTaskMoved(taskId: String, toColumn: TaskStatus, index: Int) {
 *     viewModelScope.launch {
 *         try {
 *             // Get current state to capture fromStatus and fromOrder
 *             val currentState = _uiState.value as? TasksUiState.Success ?: return@launch
 *             val task = currentState.columns.flatMap { it.tasks }.find { it.id == taskId } ?: return@launch
 *             val fromStatus = task.status
 *             val fromOrder = task.order
 *             
 *             val command = TaskCommand.MoveTask(
 *                 taskId = taskId,
 *                 fromStatus = fromStatus,
 *                 toStatus = toColumn,
 *                 fromOrder = fromOrder,
 *                 toOrder = index,
 *                 repository = taskRepository
 *             )
 *             undoRedoManager.execute(command)
 *         } catch (error: Exception) {
 *             _effects.send(TasksUiEffect.ShowError("Failed to move task: ${error.message}"))
 *         }
 *     }
 * }
 * ```
 */

/*
 * STEP 5: Update handleTaskUpdated to use UndoableCommand
 * ========================================================
 * 
 * BEFORE:
 * ```kotlin
 * private fun handleTaskUpdated(task: Task) {
 *     viewModelScope.launch {
 *         val updatedTask = task.copy(updatedAt = Clock.System.now())
 *         taskRepository.updateTask(updatedTask)
 *             .onSuccess {
 *                 updateSuccessState { state ->
 *                     state.copy(
 *                         selectedTask = if (state.selectedTask?.id == task.id) updatedTask else state.selectedTask
 *                     )
 *                 }
 *             }
 *             .onFailure { error ->
 *                 _effects.send(TasksUiEffect.ShowError("Failed to update task: ${error.message}"))
 *             }
 *     }
 * }
 * ```
 * 
 * AFTER:
 * ```kotlin
 * private fun handleTaskUpdated(task: Task) {
 *     viewModelScope.launch {
 *         try {
 *             // Get old task state
 *             val currentState = _uiState.value as? TasksUiState.Success ?: return@launch
 *             val oldTask = currentState.columns.flatMap { it.tasks }.find { it.id == task.id } ?: return@launch
 *             
 *             val updatedTask = task.copy(updatedAt = Clock.System.now())
 *             
 *             val command = TaskCommand.UpdateTask(
 *                 oldTask = oldTask,
 *                 newTask = updatedTask,
 *                 repository = taskRepository
 *             )
 *             undoRedoManager.execute(command)
 *             
 *             updateSuccessState { state ->
 *                 state.copy(
 *                     selectedTask = if (state.selectedTask?.id == task.id) updatedTask else state.selectedTask
 *                 )
 *             }
 *         } catch (error: Exception) {
 *             _effects.send(TasksUiEffect.ShowError("Failed to update task: ${error.message}"))
 *         }
 *     }
 * }
 * ```
 */

/*
 * STEP 6: Add Undo/Redo Event Handlers
 * =====================================
 * 
 * In TasksViewModel.kt, add to the onEvent function:
 * 
 * ```kotlin
 * fun onEvent(event: TasksUiEvent) {
 *     when (event) {
 *         // ... existing events
 *         
 *         // Add these new handlers
 *         TasksUiEvent.Undo -> handleUndo()
 *         TasksUiEvent.Redo -> handleRedo()
 *         TasksUiEvent.ToggleHistoryPanel -> handleToggleHistoryPanel()
 *     }
 * }
 * 
 * private fun handleUndo() {
 *     viewModelScope.launch {
 *         try {
 *             undoRedoManager.undo()
 *         } catch (e: Exception) {
 *             _effects.send(TasksUiEffect.ShowError("Undo failed: ${e.message}"))
 *         }
 *     }
 * }
 * 
 * private fun handleRedo() {
 *     viewModelScope.launch {
 *         try {
 *             undoRedoManager.redo()
 *         } catch (e: Exception) {
 *             _effects.send(TasksUiEffect.ShowError("Redo failed: ${e.message}"))
 *         }
 *     }
 * }
 * 
 * private val _showHistoryPanel = MutableStateFlow(false)
 * val showHistoryPanel: StateFlow<Boolean> = _showHistoryPanel.asStateFlow()
 * 
 * private fun handleToggleHistoryPanel() {
 *     _showHistoryPanel.value = !_showHistoryPanel.value
 * }
 * 
 * fun getUndoStack() = undoRedoManager.getUndoStack()
 * fun getRedoStack() = undoRedoManager.getRedoStack()
 * ```
 */

/*
 * STEP 7: Add UI Components to TasksScreen
 * =========================================
 * 
 * In TasksScreen.kt, add the snackbar and history panel:
 * 
 * ```kotlin
 * @Composable
 * fun TasksScreen(viewModel: TasksViewModel) {
 *     val canUndo by viewModel.canUndo.collectAsState()
 *     val canRedo by viewModel.canRedo.collectAsState()
 *     val lastAction by viewModel.lastAction.collectAsState()
 *     val showHistoryPanel by viewModel.showHistoryPanel.collectAsState()
 *     
 *     var showUndoSnackbar by remember { mutableStateOf(false) }
 *     
 *     // Show snackbar when action is performed
 *     LaunchedEffect(lastAction) {
 *         if (lastAction != null) {
 *             showUndoSnackbar = true
 *             delay(4000)
 *             showUndoSnackbar = false
 *         }
 *     }
 *     
 *     Box(
 *         modifier = Modifier
 *             .fillMaxSize()
 *             .onPreviewKeyEvent { event ->
 *                 // Handle keyboard shortcuts
 *                 when {
 *                     event.isCtrlPressed && event.key == Key.Z && !event.isShiftPressed -> {
 *                         if (event.type == KeyEventType.KeyDown && canUndo) {
 *                             viewModel.onEvent(TasksUiEvent.Undo)
 *                             true
 *                         } else false
 *                     }
 *                     event.isCtrlPressed && event.isShiftPressed && event.key == Key.Z -> {
 *                         if (event.type == KeyEventType.KeyDown && canRedo) {
 *                             viewModel.onEvent(TasksUiEvent.Redo)
 *                             true
 *                         } else false
 *                     }
 *                     event.isCtrlPressed && event.key == Key.H -> {
 *                         if (event.type == KeyEventType.KeyDown) {
 *                             viewModel.onEvent(TasksUiEvent.ToggleHistoryPanel)
 *                             true
 *                         } else false
 *                     }
 *                     else -> false
 *                 }
 *             }
 *     ) {
 *         // Existing content
 *         TasksScreenContent(...)
 *         
 *         // Undo snackbar at bottom-left
 *         Box(modifier = Modifier.align(Alignment.BottomStart)) {
 *             UndoRedoSnackbar(
 *                 message = lastAction?.description ?: "",
 *                 visible = showUndoSnackbar,
 *                 canUndo = canUndo,
 *                 onUndo = {
 *                     viewModel.onEvent(TasksUiEvent.Undo)
 *                     showUndoSnackbar = false
 *                 },
 *                 onDismiss = { showUndoSnackbar = false },
 *                 icon = getActionIcon(lastAction?.description ?: "")
 *             )
 *         }
 *         
 *         // History panel at right
 *         Box(modifier = Modifier.align(Alignment.CenterEnd)) {
 *             UndoRedoHistoryPanel(
 *                 undoStack = viewModel.getUndoStack(),
 *                 redoStack = viewModel.getRedoStack(),
 *                 onUndoTo = { index -> 
 *                     viewModel.undoTo(index)
 *                     viewModel.onEvent(TasksUiEvent.ToggleHistoryPanel)
 *                 },
 *                 onRedoTo = { index ->
 *                     viewModel.redoTo(index)
 *                     viewModel.onEvent(TasksUiEvent.ToggleHistoryPanel)
 *                 },
 *                 visible = showHistoryPanel,
 *                 onDismiss = {
 *                     viewModel.onEvent(TasksUiEvent.ToggleHistoryPanel)
 *                 }
 *             )
 *         }
 *     }
 * }
 * ```
 */

/*
 * STEP 8: Testing
 * ===============
 * 
 * Test the integration:
 * 
 * 1. Create a task → See snackbar → Press Cmd+Z → Task should be deleted
 * 2. Delete a task → See snackbar → Press Cmd+Z → Task should be restored
 * 3. Move a task → See snackbar → Press Cmd+Z → Task should move back
 * 4. Press Cmd+H → History panel should open
 * 5. Click on any history item → Should undo/redo to that point
 * 6. Hover over snackbar → Should pause auto-dismiss
 * 7. Press Cmd+Shift+Z or Cmd+Y → Should redo last undone action
 */

/*
 * SUMMARY OF CHANGES
 * ==================
 * 
 * Files to modify:
 * 1. TasksViewModel.kt - Add UndoRedoManager and update handlers
 * 2. TasksUiState.kt - Add Undo/Redo events
 * 3. TasksScreen.kt - Add snackbar and history panel UI
 * 
 * Files to import:
 * 1. domain/undo/UndoableCommand.kt
 * 2. domain/undo/UndoRedoManager.kt
 * 3. domain/undo/TaskCommand.kt
 * 4. presentation/components/UndoRedoSnackbar.kt
 * 5. presentation/components/UndoRedoHistoryPanel.kt
 * 
 * No breaking changes to existing functionality!
 */
