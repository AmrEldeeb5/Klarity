package com.example.klarity.presentation.screen.tasks

import com.example.klarity.data.serialization.BoardState
import com.example.klarity.data.serialization.BoardStateSerializer
import com.example.klarity.data.serialization.ColumnState
import com.example.klarity.data.serialization.SubtaskState
import com.example.klarity.data.serialization.TagState
import com.example.klarity.data.serialization.TaskState
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Property-based tests for TaskModels.
 * 
 * These tests verify correctness properties that should hold across all valid inputs.
 */
class TaskModelsPropertyTest {

    // ============================================================================
    // Generators
    // ============================================================================

    /**
     * Generates valid TaskTimer instances with reasonable time ranges.
     * - startedAt: within the last 100 hours from now
     * - pausedDuration: 0 to 10 hours
     */
    private fun arbTaskTimer(): Arb<TaskTimer> = arbitrary {
        val now = Clock.System.now()
        // Generate a start time within the last 100 hours
        val hoursAgo = Arb.long(0L, 100L).bind()
        val minutesAgo = Arb.long(0L, 59L).bind()
        val secondsAgo = Arb.long(0L, 59L).bind()
        
        val startedAt = now - hoursAgo.hours - minutesAgo.minutes - secondsAgo.seconds
        
        // Generate paused duration (0 to 10 hours, but less than elapsed time)
        val maxPausedSeconds = (hoursAgo * 3600 + minutesAgo * 60 + secondsAgo).coerceAtLeast(0)
        val pausedSeconds = if (maxPausedSeconds > 0) {
            Arb.long(0L, maxPausedSeconds.coerceAtMost(36000)).bind() // Max 10 hours
        } else {
            0L
        }
        
        TaskTimer(
            startedAt = startedAt,
            pausedDuration = pausedSeconds.seconds,
            isPaused = false
        )
    }

    // ============================================================================
    // Property 8: Timer display presence
    // **Feature: kanban-board, Property 8: Timer display presence**
    // **Validates: Requirements 6.1**
    // 
    // For any task with an active timer (timerStartedAt is not null), 
    // the task card SHALL display the elapsed time.
    // ============================================================================

    @Test
    fun property8_timerWithActiveTimerReturnsFormattedTimeString() {
        runBlocking {
            checkAll(100, arbTaskTimer()) { timer ->
                // When we have a timer, formattedTime should return a valid HH:MM:SS string
                val formattedTime = timer.formattedTime()
                
                // Verify format is HH:MM:SS
                val regex = Regex("""^\d{2}:\d{2}:\d{2}$""")
                assertTrue(
                    regex.matches(formattedTime),
                    "Timer formatted time '$formattedTime' should match HH:MM:SS format"
                )
                
                // Verify the parts are valid time components
                val parts = formattedTime.split(":")
                assertEquals(3, parts.size, "Should have 3 parts separated by ':'")
                
                val hours = parts[0].toInt()
                val minutes = parts[1].toInt()
                val seconds = parts[2].toInt()
                
                assertTrue(hours in 0..99, "Hours should be 0-99, got $hours")
                assertTrue(minutes in 0..59, "Minutes should be 0-59, got $minutes")
                assertTrue(seconds in 0..59, "Seconds should be 0-59, got $seconds")
            }
        }
    }

    @Test
    fun property8_taskWithTimerHasActiveTimerTrue() {
        runBlocking {
            checkAll(100, arbTaskTimer()) { timer ->
                val now = Clock.System.now()
                val task = Task(
                    id = "test-task",
                    title = "Test Task",
                    timer = timer,
                    createdAt = now,
                    updatedAt = now
                )
                
                // A task with a non-null timer should report hasActiveTimer as true
                assertTrue(
                    task.hasActiveTimer,
                    "Task with timer should have hasActiveTimer = true"
                )
            }
        }
    }

    @Test
    fun property8_taskWithoutTimerHasActiveTimerFalse() {
        val now = Clock.System.now()
        val task = Task(
            id = "test-task",
            title = "Test Task",
            timer = null,
            createdAt = now,
            updatedAt = now
        )
        
        assertTrue(
            !task.hasActiveTimer,
            "Task without timer should have hasActiveTimer = false"
        )
    }

    // ============================================================================
    // Property 10: Priority indicator color mapping
    // **Feature: kanban-board, Property 10: Priority indicator color mapping**
    // **Validates: Requirements 8.1, 8.2, 8.3, 8.4**
    //
    // For any task, the priority indicator color SHALL match the defined color 
    // for that priority level (HIGH=red, MEDIUM=yellow, LOW=blue).
    // ============================================================================

    @Test
    fun property10_priorityColorsMatchSpecification() {
        runBlocking {
            // Define expected colors from the design spec
            val expectedColors = mapOf(
                TaskPriority.HIGH to 0xFFEF4444L,    // Red
                TaskPriority.MEDIUM to 0xFFFACC15L,  // Yellow
                TaskPriority.LOW to 0xFF3B82F6L      // Blue
            )
            
            checkAll(100, Arb.enum<TaskPriority>()) { priority ->
                when (priority) {
                    TaskPriority.HIGH -> {
                        assertEquals(
                            expectedColors[TaskPriority.HIGH],
                            priority.color,
                            "HIGH priority should be red (0xFFEF4444)"
                        )
                    }
                    TaskPriority.MEDIUM -> {
                        assertEquals(
                            expectedColors[TaskPriority.MEDIUM],
                            priority.color,
                            "MEDIUM priority should be yellow (0xFFFACC15)"
                        )
                    }
                    TaskPriority.LOW -> {
                        assertEquals(
                            expectedColors[TaskPriority.LOW],
                            priority.color,
                            "LOW priority should be blue (0xFF3B82F6)"
                        )
                    }
                    TaskPriority.NONE -> {
                        // NONE priority has its own color, not specified in requirements
                        assertEquals(
                            0xFF9E9E9EL,
                            priority.color,
                            "NONE priority should be gray (0xFF9E9E9E)"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun property10_allPriorityValuesHaveDistinctColors() {
        val colors = TaskPriority.entries.map { it.color }
        val uniqueColors = colors.toSet()
        
        assertEquals(
            colors.size,
            uniqueColors.size,
            "All priority levels should have distinct colors"
        )
    }

    @Test
    fun property10_taskPriorityColorIsAccessibleViaTask() {
        runBlocking {
            val now = Clock.System.now()
            
            checkAll(100, Arb.enum<TaskPriority>()) { priority ->
                val task = Task(
                    id = "test-task",
                    title = "Test Task",
                    priority = priority,
                    createdAt = now,
                    updatedAt = now
                )
                
                // The task's priority color should match the enum's color
                assertEquals(
                    priority.color,
                    task.priority.color,
                    "Task priority color should match the priority enum color"
                )
            }
        }
    }

    // ============================================================================
    // Property 11: Board state serialization round-trip
    // **Feature: kanban-board, Property 11: Board state serialization round-trip**
    // **Validates: Requirements 9.3, 9.4**
    //
    // For any valid board state, serializing to JSON then deserializing 
    // SHALL produce an equivalent board state.
    // ============================================================================

    /**
     * Generates valid TagState instances.
     */
    private fun arbTagState(): Arb<TagState> = arbitrary {
        TagState(
            label = Arb.string(1..20).bind(),
            color = Arb.enum<TagColor>().bind().name
        )
    }

    /**
     * Generates valid SubtaskState instances.
     */
    private fun arbSubtaskState(): Arb<SubtaskState> = arbitrary {
        SubtaskState(
            id = "subtask-${Arb.string(5..10).bind()}",
            title = Arb.string(1..50).bind(),
            isCompleted = Arb.boolean().bind(),
            order = Arb.int(0..100).bind()
        )
    }

    /**
     * Generates valid TaskState instances with reasonable values.
     */
    private fun arbTaskState(): Arb<TaskState> = arbitrary {
        val now = Clock.System.now().toEpochMilliseconds()
        TaskState(
            id = "task-${Arb.string(5..10).bind()}",
            title = Arb.string(1..100).bind(),
            description = Arb.string(0..200).bind(),
            status = Arb.enum<TaskStatus>().bind().name,
            priority = Arb.enum<TaskPriority>().bind().name,
            tags = Arb.list(arbTagState(), 0..5).bind(),
            points = Arb.int(1..13).orNull().bind(),
            assignee = Arb.string(1..20).orNull().bind(),
            dueDate = Arb.long(now, now + 86400000L * 30).orNull().bind(),
            startDate = Arb.long(now - 86400000L * 30, now).orNull().bind(),
            estimatedHours = null,
            actualHours = null,
            subtasks = Arb.list(arbSubtaskState(), 0..3).bind(),
            linkedNoteIds = emptyList(),
            timerStartedAt = Arb.long(now - 3600000L, now).orNull().bind(),
            timerPausedDuration = Arb.long(0L, 3600000L).orNull().bind(),
            timerIsPaused = Arb.boolean().bind(),
            isActive = Arb.boolean().bind(),
            completed = Arb.boolean().bind(),
            createdAt = now - 86400000L,
            updatedAt = now,
            completedAt = null,
            order = Arb.int(0..100).bind()
        )
    }

    /**
     * Generates valid ColumnState instances.
     */
    private fun arbColumnState(): Arb<ColumnState> = arbitrary {
        val status = Arb.enum<TaskStatus>().bind()
        ColumnState(
            id = status.name,
            title = status.label,
            status = status.name,
            order = Arb.int(0..10).bind(),
            isCollapsed = Arb.boolean().bind(),
            wipLimit = Arb.int(1..10).orNull().bind()
        )
    }

    /**
     * Generates valid BoardState instances.
     */
    private fun arbBoardState(): Arb<BoardState> = arbitrary {
        BoardState(
            columns = Arb.list(arbColumnState(), 1..6).bind(),
            tasks = Arb.list(arbTaskState(), 0..10).bind()
        )
    }

    @Test
    fun property11_boardStateSerializationRoundTrip() {
        runBlocking {
            checkAll(100, arbBoardState()) { originalState ->
                // Serialize to JSON
                val json = BoardStateSerializer.encodeBoardState(originalState)
                
                // Deserialize back to BoardState
                val deserializedState = BoardStateSerializer.decodeBoardState(json)
                
                // Verify the round-trip produces equivalent state
                assertEquals(
                    originalState.columns.size,
                    deserializedState.columns.size,
                    "Column count should be preserved after round-trip"
                )
                
                assertEquals(
                    originalState.tasks.size,
                    deserializedState.tasks.size,
                    "Task count should be preserved after round-trip"
                )
                
                // Verify each column is preserved
                originalState.columns.forEachIndexed { index, originalColumn ->
                    val deserializedColumn = deserializedState.columns[index]
                    assertEquals(originalColumn.id, deserializedColumn.id, "Column id should match")
                    assertEquals(originalColumn.title, deserializedColumn.title, "Column title should match")
                    assertEquals(originalColumn.status, deserializedColumn.status, "Column status should match")
                    assertEquals(originalColumn.order, deserializedColumn.order, "Column order should match")
                    assertEquals(originalColumn.isCollapsed, deserializedColumn.isCollapsed, "Column isCollapsed should match")
                    assertEquals(originalColumn.wipLimit, deserializedColumn.wipLimit, "Column wipLimit should match")
                }
                
                // Verify each task is preserved
                originalState.tasks.forEachIndexed { index, originalTask ->
                    val deserializedTask = deserializedState.tasks[index]
                    assertEquals(originalTask.id, deserializedTask.id, "Task id should match")
                    assertEquals(originalTask.title, deserializedTask.title, "Task title should match")
                    assertEquals(originalTask.description, deserializedTask.description, "Task description should match")
                    assertEquals(originalTask.status, deserializedTask.status, "Task status should match")
                    assertEquals(originalTask.priority, deserializedTask.priority, "Task priority should match")
                    assertEquals(originalTask.points, deserializedTask.points, "Task points should match")
                    assertEquals(originalTask.assignee, deserializedTask.assignee, "Task assignee should match")
                    assertEquals(originalTask.completed, deserializedTask.completed, "Task completed should match")
                    assertEquals(originalTask.isActive, deserializedTask.isActive, "Task isActive should match")
                    assertEquals(originalTask.order, deserializedTask.order, "Task order should match")
                    assertEquals(originalTask.tags.size, deserializedTask.tags.size, "Task tags count should match")
                    assertEquals(originalTask.subtasks.size, deserializedTask.subtasks.size, "Task subtasks count should match")
                }
            }
        }
    }

    @Test
    fun property11_emptyBoardStateSerializationRoundTrip() {
        // Test edge case: empty board state
        val emptyState = BoardState()
        val json = BoardStateSerializer.encodeBoardState(emptyState)
        val deserializedState = BoardStateSerializer.decodeBoardState(json)
        
        assertEquals(0, deserializedState.columns.size, "Empty board should have no columns")
        assertEquals(0, deserializedState.tasks.size, "Empty board should have no tasks")
    }

    @Test
    fun property11_invalidJsonReturnsEmptyBoardState() {
        // Test edge case: invalid JSON should return empty BoardState
        val invalidJson = "{ invalid json }"
        val result = BoardStateSerializer.decodeBoardState(invalidJson)
        
        assertEquals(0, result.columns.size, "Invalid JSON should return empty columns")
        assertEquals(0, result.tasks.size, "Invalid JSON should return empty tasks")
    }
}
