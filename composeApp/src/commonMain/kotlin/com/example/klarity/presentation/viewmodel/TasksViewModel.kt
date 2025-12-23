package com.example.klarity.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.klarity.domain.repositories.TaskRepository
import com.example.klarity.presentation.screen.tasks.KanbanColumn
import com.example.klarity.presentation.screen.tasks.Task
import com.example.klarity.presentation.screen.tasks.TaskFilter
import com.example.klarity.presentation.screen.tasks.TaskPriority
import com.example.klarity.presentation.screen.tasks.TaskSortOption
import com.example.klarity.presentation.screen.tasks.TaskStatus
import com.example.klarity.presentation.screen.tasks.TaskTimer
import com.example.klarity.presentation.screen.tasks.TaskViewMode
import com.example.klarity.presentation.state.TasksUiEffect
import com.example.klarity.presentation.state.TasksUiEvent
import com.example.klarity.presentation.state.TasksUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ViewModel for the Tasks/Kanban screen.
 * 
 * Manages task state, filtering, sorting, and timer functionality
 * using an event-based architecture with repository-backed persistence.
 */
class TasksViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TasksUiState>(TasksUiState.Loading)
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val _effects = Channel<TasksUiEffect>(Channel.BUFFERED)
    val effects: Flow<TasksUiEffect> = _effects.receiveAsFlow()

    init {
        loadInitialData()
        observeTasks()
    }
    
    /**
     * Observe tasks from repository and update UI state reactively.
     */
    private fun observeTasks() {
        viewModelScope.launch {
            taskRepository.getAllTasks().collect { tasks ->
                updateColumnsFromTasks(tasks)
            }
        }
    }
    
    /**
     * Update columns from task list, grouping by status.
     */
    private fun updateColumnsFromTasks(tasks: List<Task>) {
        val currentState = _uiState.value
        if (currentState !is TasksUiState.Success) return
        
        val tasksByStatus = tasks.groupBy { it.status }
        val updatedColumns = currentState.columns.map { column ->
            val columnTasks = tasksByStatus[column.status] ?: emptyList()
            column.copy(tasks = columnTasks.sortedBy { it.order })
        }
        
        _uiState.value = currentState.copy(columns = updatedColumns)
    }

    /**
     * Handle UI events from the Tasks screen.
     */
    fun onEvent(event: TasksUiEvent) {
        when (event) {
            // Task interaction events
            is TasksUiEvent.TaskClicked -> handleTaskClicked(event.task)
            is TasksUiEvent.TaskToggleComplete -> handleTaskToggleComplete(event.taskId)
            is TasksUiEvent.TaskMoved -> handleTaskMoved(event.taskId, event.toColumn, event.index)
            is TasksUiEvent.TaskCreated -> handleTaskCreated(event.status)
            is TasksUiEvent.TaskDeleted -> handleTaskDeleted(event.task)
            is TasksUiEvent.TaskUpdated -> handleTaskUpdated(event.task)
            
            // Timer events
            is TasksUiEvent.TimerStarted -> handleTimerStarted(event.taskId)
            is TasksUiEvent.TimerStopped -> handleTimerStopped(event.taskId)
            is TasksUiEvent.TimerPaused -> handleTimerPaused(event.taskId)
            is TasksUiEvent.TimerResumed -> handleTimerResumed(event.taskId)
            
            // Filter and sort events
            is TasksUiEvent.FilterChanged -> handleFilterChanged(event.filter)
            is TasksUiEvent.SortChanged -> handleSortChanged(event.sortBy)
            is TasksUiEvent.AssigneeFilterChanged -> handleAssigneeFilterChanged(event.assignee)
            is TasksUiEvent.TagFilterChanged -> handleTagFilterChanged(event.tags)
            is TasksUiEvent.PriorityFilterChanged -> handlePriorityFilterChanged(event.priority)
            
            // View mode events
            is TasksUiEvent.ViewModeChanged -> handleViewModeChanged(event.mode)
            
            // Column events
            is TasksUiEvent.ColumnAdded -> handleColumnAdded(event.title)
            is TasksUiEvent.ColumnCollapsed -> handleColumnCollapsed(event.status, event.collapsed)
            
            // Modal events
            TasksUiEvent.ModalClosed -> handleModalClosed()
            
            // Refresh
            TasksUiEvent.Refresh -> loadInitialData()
            
            // AI Suggestion events
            TasksUiEvent.ReviewAiSuggestions -> handleReviewAiSuggestions()
            TasksUiEvent.DismissAiSuggestion -> handleDismissAiSuggestion()
        }
    }
    
    // ============================================================================
    // AI Suggestion Handlers
    // ============================================================================
    
    private fun handleReviewAiSuggestions() {
        // TODO: Open AI suggestions review modal
        viewModelScope.launch {
            _effects.send(TasksUiEffect.ShowSnackbar("AI Suggestions review coming soon"))
        }
    }
    
    private fun handleDismissAiSuggestion() {
        updateSuccessState { state ->
            state.copy(showAiSuggestion = false)
        }
    }


    // ============================================================================
    // Task Interaction Handlers
    // ============================================================================

    private fun handleTaskClicked(task: Task) {
        updateSuccessState { state ->
            state.copy(
                selectedTask = task,
                isModalOpen = true
            )
        }
    }

    private fun handleTaskToggleComplete(taskId: String) {
        viewModelScope.launch {
            // Find current task state
            val currentState = _uiState.value as? TasksUiState.Success ?: return@launch
            val task = currentState.columns.flatMap { it.tasks }.find { it.id == taskId } ?: return@launch
            
            // Toggle completion in repository
            taskRepository.updateTaskCompletion(taskId, !task.completed)
                .onSuccess {
                    // UI will update via observeTasks flow
                }
                .onFailure { error ->
                    _effects.send(TasksUiEffect.ShowError("Failed to update task: ${error.message}"))
                }
        }
    }

    private fun handleTaskMoved(taskId: String, toColumn: TaskStatus, index: Int) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, toColumn, index)
                .onSuccess {
                    // UI will update via observeTasks flow
                }
                .onFailure { error ->
                    _effects.send(TasksUiEffect.ShowError("Failed to move task: ${error.message}"))
                }
        }
    }

    private fun handleTaskCreated(status: TaskStatus) {
        viewModelScope.launch {
            val currentState = _uiState.value as? TasksUiState.Success ?: return@launch
            val columnTasks = currentState.columns.find { it.status == status }?.tasks ?: emptyList()
            
            val newTask = Task(
                id = "task-${Clock.System.now().toEpochMilliseconds()}",
                title = "New Task",
                status = status,
                order = columnTasks.size,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now()
            )
            
            taskRepository.createTask(newTask)
                .onSuccess { task ->
                    updateSuccessState { state ->
                        state.copy(
                            selectedTask = task,
                            isModalOpen = true
                        )
                    }
                    _effects.send(TasksUiEffect.ShowSnackbar("Task created"))
                }
                .onFailure { error ->
                    _effects.send(TasksUiEffect.ShowError("Failed to create task: ${error.message}"))
                }
        }
    }

    private fun handleTaskDeleted(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task.id)
                .onSuccess {
                    updateSuccessState { state ->
                        state.copy(
                            selectedTask = if (state.selectedTask?.id == task.id) null else state.selectedTask,
                            isModalOpen = if (state.selectedTask?.id == task.id) false else state.isModalOpen
                        )
                    }
                    _effects.send(TasksUiEffect.ShowSnackbar("Task deleted"))
                }
                .onFailure { error ->
                    _effects.send(TasksUiEffect.ShowError("Failed to delete task: ${error.message}"))
                }
        }
    }

    private fun handleTaskUpdated(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(updatedAt = Clock.System.now())
            taskRepository.updateTask(updatedTask)
                .onSuccess {
                    updateSuccessState { state ->
                        state.copy(
                            selectedTask = if (state.selectedTask?.id == task.id) updatedTask else state.selectedTask
                        )
                    }
                }
                .onFailure { error ->
                    _effects.send(TasksUiEffect.ShowError("Failed to update task: ${error.message}"))
                }
        }
    }

    // ============================================================================
    // Timer Handlers
    // ============================================================================

    private fun handleTimerStarted(taskId: String) {
        viewModelScope.launch {
            taskRepository.startTimer(taskId)
                .onFailure { error ->
                    _effects.send(TasksUiEffect.ShowError("Failed to start timer: ${error.message}"))
                }
        }
    }

    private fun handleTimerStopped(taskId: String) {
        viewModelScope.launch {
            taskRepository.stopTimer(taskId)
                .onFailure { error ->
                    _effects.send(TasksUiEffect.ShowError("Failed to stop timer: ${error.message}"))
                }
        }
    }

    private fun handleTimerPaused(taskId: String) {
        viewModelScope.launch {
            // Get current task and update with paused state
            val currentState = _uiState.value as? TasksUiState.Success ?: return@launch
            val task = currentState.columns.flatMap { it.tasks }.find { it.id == taskId } ?: return@launch
            
            if (task.timer != null) {
                val updatedTask = task.copy(
                    timer = task.timer.copy(isPaused = true),
                    updatedAt = Clock.System.now()
                )
                taskRepository.updateTask(updatedTask)
            }
        }
    }

    private fun handleTimerResumed(taskId: String) {
        viewModelScope.launch {
            // Get current task and update with resumed state
            val currentState = _uiState.value as? TasksUiState.Success ?: return@launch
            val task = currentState.columns.flatMap { it.tasks }.find { it.id == taskId } ?: return@launch
            
            if (task.timer != null) {
                val updatedTask = task.copy(
                    timer = task.timer.copy(isPaused = false),
                    updatedAt = Clock.System.now()
                )
                taskRepository.updateTask(updatedTask)
            }
        }
    }


    // ============================================================================
    // Filter and Sort Handlers
    // ============================================================================

    private fun handleFilterChanged(filter: TaskFilter) {
        updateSuccessState { state ->
            state.copy(filter = filter)
        }
    }

    private fun handleSortChanged(sortBy: TaskSortOption) {
        updateSuccessState { state ->
            state.copy(sortBy = sortBy)
        }
    }

    private fun handleAssigneeFilterChanged(assignee: String?) {
        updateSuccessState { state ->
            val newAssignees = if (assignee != null) {
                if (assignee in state.filter.assignees) {
                    state.filter.assignees - assignee
                } else {
                    state.filter.assignees + assignee
                }
            } else {
                emptySet()
            }
            state.copy(filter = state.filter.copy(assignees = newAssignees))
        }
    }

    private fun handleTagFilterChanged(tags: Set<String>) {
        updateSuccessState { state ->
            state.copy(filter = state.filter.copy(tags = tags))
        }
    }

    private fun handlePriorityFilterChanged(priority: TaskPriority?) {
        updateSuccessState { state ->
            val newPriorities = if (priority != null) {
                if (priority in state.filter.priorities) {
                    state.filter.priorities - priority
                } else {
                    state.filter.priorities + priority
                }
            } else {
                TaskPriority.entries.toSet()
            }
            state.copy(filter = state.filter.copy(priorities = newPriorities))
        }
    }

    // ============================================================================
    // View Mode Handlers
    // ============================================================================

    private fun handleViewModeChanged(mode: TaskViewMode) {
        updateSuccessState { state ->
            state.copy(viewMode = mode)
        }
    }

    // ============================================================================
    // Column Handlers
    // ============================================================================

    private fun handleColumnAdded(title: String) {
        // For now, we don't support custom columns beyond the predefined statuses
        viewModelScope.launch {
            _effects.send(TasksUiEffect.ShowSnackbar("Custom columns coming soon"))
        }
    }

    private fun handleColumnCollapsed(status: TaskStatus, collapsed: Boolean) {
        updateSuccessState { state ->
            val updatedColumns = state.columns.map { column ->
                if (column.status == status) {
                    column.copy(isCollapsed = collapsed)
                } else column
            }
            state.copy(columns = updatedColumns)
        }
    }

    // ============================================================================
    // Modal Handlers
    // ============================================================================

    private fun handleModalClosed() {
        updateSuccessState { state ->
            state.copy(
                selectedTask = null,
                isModalOpen = false
            )
        }
    }

    // ============================================================================
    // Helper Functions
    // ============================================================================

    private fun loadInitialData() {
        _uiState.value = TasksUiState.Loading
        
        viewModelScope.launch {
            // Initialize with empty columns, tasks will be loaded via observeTasks
            val columns = createInitialColumns()
            
            _uiState.value = TasksUiState.Success(
                columns = columns,
                viewMode = TaskViewMode.KANBAN,
                filter = TaskFilter(),
                sortBy = TaskSortOption.PRIORITY
            )
            
            // Create sample data if database is empty
            createSampleDataIfEmpty()
        }
    }
    
    /**
     * Creates sample tasks if the database is empty.
     * This helps demonstrate the UI on first launch.
     */
    private suspend fun createSampleDataIfEmpty() {
        val counts = taskRepository.getTaskCountsByStatus()
        val totalTasks = counts.values.sum()
        
        if (totalTasks == 0) {
            val now = Clock.System.now()
            val sampleTasks = listOf(
                Task(
                    id = "task-sample-1",
                    title = "Design Login Flow Mockups",
                    description = "Create wireframes and high-fidelity mockups for the login flow",
                    status = TaskStatus.TODO,
                    priority = TaskPriority.HIGH,
                    tags = listOf(
                        com.example.klarity.presentation.screen.tasks.TaskTag("UI Design", com.example.klarity.presentation.screen.tasks.TagColor.PURPLE),
                        com.example.klarity.presentation.screen.tasks.TaskTag("High-Effort", com.example.klarity.presentation.screen.tasks.TagColor.ORANGE)
                    ),
                    points = 3,
                    assignee = "Alice",
                    order = 0,
                    createdAt = now,
                    updatedAt = now
                ),
                Task(
                    id = "task-sample-2",
                    title = "Setup Database Schema",
                    description = "Define and implement the database schema for user data",
                    status = TaskStatus.TODO,
                    priority = TaskPriority.MEDIUM,
                    tags = listOf(
                        com.example.klarity.presentation.screen.tasks.TaskTag("Backend", com.example.klarity.presentation.screen.tasks.TagColor.BLUE)
                    ),
                    points = 2,
                    assignee = "Bob",
                    order = 1,
                    createdAt = now,
                    updatedAt = now
                ),
                Task(
                    id = "task-sample-3",
                    title = "Develop Authentication API",
                    description = "Implement JWT-based authentication endpoints",
                    status = TaskStatus.IN_PROGRESS,
                    priority = TaskPriority.HIGH,
                    tags = listOf(
                        com.example.klarity.presentation.screen.tasks.TaskTag("Backend", com.example.klarity.presentation.screen.tasks.TagColor.BLUE),
                        com.example.klarity.presentation.screen.tasks.TaskTag("High-Effort", com.example.klarity.presentation.screen.tasks.TagColor.ORANGE)
                    ),
                    points = 5,
                    assignee = "Charlie",
                    order = 0,
                    createdAt = now,
                    updatedAt = now
                ),
                Task(
                    id = "task-sample-4",
                    title = "Onboarding Flow User Research",
                    description = "Conduct user interviews and analyze onboarding patterns",
                    status = TaskStatus.BACKLOG,
                    priority = TaskPriority.LOW,
                    tags = listOf(
                        com.example.klarity.presentation.screen.tasks.TaskTag("Research", com.example.klarity.presentation.screen.tasks.TagColor.GREEN)
                    ),
                    points = 2,
                    order = 0,
                    createdAt = now,
                    updatedAt = now
                ),
                Task(
                    id = "task-sample-5",
                    title = "Landing Page Copywriting",
                    description = "Write compelling copy for the marketing landing page",
                    status = TaskStatus.IN_REVIEW,
                    priority = TaskPriority.MEDIUM,
                    tags = listOf(
                        com.example.klarity.presentation.screen.tasks.TaskTag("Marketing", com.example.klarity.presentation.screen.tasks.TagColor.PURPLE)
                    ),
                    points = 1,
                    assignee = "Diana",
                    order = 0,
                    createdAt = now,
                    updatedAt = now
                ),
                Task(
                    id = "task-sample-6",
                    title = "Update Brand Guidelines",
                    description = "Refresh brand colors and typography guidelines",
                    status = TaskStatus.DONE,
                    priority = TaskPriority.LOW,
                    completed = true,
                    tags = listOf(
                        com.example.klarity.presentation.screen.tasks.TaskTag("Design", com.example.klarity.presentation.screen.tasks.TagColor.PURPLE)
                    ),
                    order = 0,
                    createdAt = now,
                    updatedAt = now,
                    completedAt = now
                )
            )
            
            sampleTasks.forEach { task ->
                taskRepository.createTask(task)
            }
        }
    }

    private fun updateSuccessState(update: (TasksUiState.Success) -> TasksUiState.Success) {
        _uiState.update { currentState ->
            when (currentState) {
                is TasksUiState.Success -> update(currentState)
                else -> currentState
            }
        }
    }

    /**
     * Get filtered tasks based on current filter settings.
     * This is a pure function that can be used for property testing.
     */
    fun getFilteredTasks(tasks: List<Task>, filter: TaskFilter): List<Task> {
        return tasks.filter { task ->
            // Filter by assignee
            val matchesAssignee = filter.assignees.isEmpty() || 
                (task.assignee != null && task.assignee in filter.assignees)
            
            // Filter by tags
            val matchesTags = filter.tags.isEmpty() || 
                task.tags.any { it.label in filter.tags }
            
            // Filter by priority
            val matchesPriority = filter.priorities.isEmpty() || 
                task.priority in filter.priorities
            
            // Filter by status
            val matchesStatus = filter.statuses.isEmpty() || 
                task.status in filter.statuses
            
            // Filter by search query
            val matchesSearch = filter.searchQuery.isBlank() ||
                task.title.contains(filter.searchQuery, ignoreCase = true) ||
                task.description.contains(filter.searchQuery, ignoreCase = true)
            
            // Filter by overdue
            val matchesOverdue = !filter.showOverdueOnly || task.isOverdue
            
            matchesAssignee && matchesTags && matchesPriority && 
                matchesStatus && matchesSearch && matchesOverdue
        }
    }

    /**
     * Filter tasks by assignee.
     * Returns only tasks where the assignee matches the filter value.
     */
    fun filterByAssignee(tasks: List<Task>, assignee: String): List<Task> {
        return tasks.filter { task -> task.assignee == assignee }
    }

    /**
     * Filter tasks by tags.
     * Returns only tasks that have at least one matching tag.
     */
    fun filterByTags(tasks: List<Task>, tags: Set<String>): List<Task> {
        if (tags.isEmpty()) return tasks
        return tasks.filter { task ->
            task.tags.any { it.label in tags }
        }
    }

    private fun createInitialColumns(): List<KanbanColumn> {
        val now = Clock.System.now()
        return listOf(
            KanbanColumn(status = TaskStatus.BACKLOG, tasks = emptyList()),
            KanbanColumn(status = TaskStatus.TODO, tasks = emptyList(), wipLimit = 5),
            KanbanColumn(status = TaskStatus.IN_PROGRESS, tasks = emptyList(), wipLimit = 3),
            KanbanColumn(status = TaskStatus.IN_REVIEW, tasks = emptyList()),
            KanbanColumn(status = TaskStatus.DONE, tasks = emptyList())
        )
    }
}
