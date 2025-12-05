package com.example.sentio.presentation.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sentio.presentation.state.HomeUiEffect
import com.example.sentio.presentation.state.HomeUiEvent
import com.example.sentio.presentation.theme.SentioColors
import com.example.sentio.presentation.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main Home Screen - Adaptive Multi-Pane Workspace
 *
 * Layout Modes:
 * - Single Pane: Full focus on Notes, Graph, or Tasks
 * - Dual Pane: Notes list + Editor, Tasks + Editor, Graph + AI Chat
 * - Tri-Pane: List + Editor + AI Context
 * - Focus: Minimal UI, just editor
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedNoteId by viewModel.selectedNoteId.collectAsState()
    val expandedFolderIds by viewModel.expandedFolderIds.collectAsState()
    val pinnedSectionExpanded by viewModel.pinnedSectionExpanded.collectAsState()
    
    val selectedNote = notes.find { it.id == selectedNoteId }
    var showSlashMenu by remember { mutableStateOf(false) }
    var currentNavDestination by remember { mutableStateOf(NavDestination.NOTES) }
    
    // Workspace layout state
    var workspaceConfig by remember { mutableStateOf(WorkspacePresets.notesDefault) }

    // Handle effects (snackbar, errors)
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeUiEffect.NavigateToEditor -> { /* No longer navigating - editing in place */ }
                is HomeUiEffect.ShowSnackbar -> { /* TODO: Show snackbar */ }
                is HomeUiEffect.ShowError -> { /* TODO: Show error */ }
            }
        }
    }
    
    // Update workspace config based on navigation destination
    LaunchedEffect(currentNavDestination) {
        workspaceConfig = when (currentNavDestination) {
            NavDestination.HOME -> WorkspacePresets.notesDefault
            NavDestination.NOTES -> workspaceConfig // Keep current notes config
            NavDestination.TASKS -> WorkspacePresets.tasksFull
            NavDestination.GRAPH -> WorkspacePresets.graphFull
            NavDestination.AI_ASSISTANT -> WorkspacePresets.aiChat
            NavDestination.SETTINGS -> workspaceConfig // Keep current config
        }
    }

    Row(modifier = Modifier.fillMaxSize().background(SentioColors.BgPrimary)) {
        // Left Navigation Rail (always visible, 72px)
        NavigationRail(
            currentDestination = currentNavDestination,
            onDestinationSelected = { destination ->
                currentNavDestination = destination
            }
        )

        // Main Workspace Area
        Column(modifier = Modifier.fillMaxSize().weight(1f)) {
            // Top Bar with layout controls
            WorkspaceTopBar(
                currentMode = workspaceConfig.mode,
                currentDestination = currentNavDestination,
                onLayoutModeChange = { mode ->
                    workspaceConfig = when (mode) {
                        WorkspaceLayoutMode.SINGLE_PANE -> workspaceConfig.copy(
                            mode = mode,
                            leftPane = null,
                            rightPane = null
                        )
                        WorkspaceLayoutMode.DUAL_PANE -> workspaceConfig.copy(
                            mode = mode,
                            leftPane = PaneType.NOTES_LIST,
                            rightPane = null
                        )
                        WorkspaceLayoutMode.TRI_PANE -> WorkspacePresets.notesDefault
                        WorkspaceLayoutMode.FOCUS -> workspaceConfig.copy(
                            mode = mode,
                            leftPane = null,
                            rightPane = null
                        )
                    }
                },
                onToggleLeftPane = {
                    workspaceConfig = if (workspaceConfig.leftPane != null) {
                        workspaceConfig.copy(leftPane = null)
                    } else {
                        workspaceConfig.copy(leftPane = PaneType.NOTES_LIST)
                    }
                },
                onToggleRightPane = {
                    workspaceConfig = if (workspaceConfig.rightPane != null) {
                        workspaceConfig.copy(rightPane = null)
                    } else {
                        workspaceConfig.copy(rightPane = PaneType.AI_CONTEXT)
                    }
                }
            )

            // Adaptive Workspace
            AdaptiveWorkspace(
                config = workspaceConfig,
                onConfigChange = { workspaceConfig = it },
                leftPaneContent = { modifier ->
                    // Left pane content based on type
                    when (workspaceConfig.leftPane) {
                        PaneType.NOTES_LIST -> NotesTreeSidebar(
                            notes = notes,
                            folders = folders,
                            expandedFolderIds = expandedFolderIds,
                            pinnedSectionExpanded = pinnedSectionExpanded,
                            searchQuery = searchQuery,
                            selectedNoteId = selectedNoteId,
                            onSearchQueryChange = { viewModel.onEvent(HomeUiEvent.SearchQueryChanged(it)) },
                            onNoteSelect = { viewModel.onEvent(HomeUiEvent.SelectNote(it)) },
                            onCreateNote = { viewModel.onEvent(HomeUiEvent.CreateNote) },
                            onToggleFolder = { viewModel.onEvent(HomeUiEvent.ToggleFolder(it)) },
                            onTogglePinnedSection = { viewModel.onEvent(HomeUiEvent.TogglePinnedSection) },
                            onTogglePin = { noteId -> viewModel.onEvent(HomeUiEvent.ToggleNotePin(noteId)) },
                            onDeleteNote = { noteId -> viewModel.onEvent(HomeUiEvent.DeleteNote(noteId)) }
                        )
                        PaneType.TASKS -> TasksPane(modifier = modifier)
                        PaneType.GRAPH -> GraphPane(modifier = modifier)
                        else -> {}
                    }
                },
                centerPaneContent = { modifier ->
                    // Center pane content based on type and destination
                    when (workspaceConfig.centerPane ?: PaneType.EDITOR) {
                        PaneType.EDITOR -> EditorPanel(
                            selectedNote = selectedNote,
                            folders = folders,
                            showSlashMenu = showSlashMenu,
                            onToggleSlashMenu = { showSlashMenu = !showSlashMenu },
                            onTitleChange = { title ->
                                selectedNote?.let { viewModel.onEvent(HomeUiEvent.UpdateNoteTitle(it.id, title)) }
                            },
                            onContentChange = { content ->
                                selectedNote?.let { viewModel.onEvent(HomeUiEvent.UpdateNoteContent(it.id, content)) }
                            },
                            onTogglePin = {
                                selectedNote?.let { viewModel.onEvent(HomeUiEvent.ToggleNotePin(it.id)) }
                            },
                            onDelete = {
                                selectedNote?.let { viewModel.onEvent(HomeUiEvent.DeleteNote(it.id)) }
                            },
                            onStatusChange = { status ->
                                selectedNote?.let { viewModel.onEvent(HomeUiEvent.UpdateNoteStatus(it.id, status)) }
                            },
                            modifier = modifier
                        )
                        PaneType.GRAPH -> GraphPane(modifier = modifier)
                        PaneType.TASKS -> TasksPane(modifier = modifier)
                        PaneType.AI_CHAT -> AIChatPane(modifier = modifier)
                        else -> EditorPanel(
                            selectedNote = selectedNote,
                            folders = folders,
                            showSlashMenu = showSlashMenu,
                            onToggleSlashMenu = { showSlashMenu = !showSlashMenu },
                            onTitleChange = { title ->
                                selectedNote?.let { viewModel.onEvent(HomeUiEvent.UpdateNoteTitle(it.id, title)) }
                            },
                            onContentChange = { content ->
                                selectedNote?.let { viewModel.onEvent(HomeUiEvent.UpdateNoteContent(it.id, content)) }
                            },
                            onTogglePin = {
                                selectedNote?.let { viewModel.onEvent(HomeUiEvent.ToggleNotePin(it.id)) }
                            },
                            onDelete = {
                                selectedNote?.let { viewModel.onEvent(HomeUiEvent.DeleteNote(it.id)) }
                            },
                            onStatusChange = { status ->
                                selectedNote?.let { viewModel.onEvent(HomeUiEvent.UpdateNoteStatus(it.id, status)) }
                            },
                            modifier = modifier
                        )
                    }
                },
                rightPaneContent = { modifier ->
                    // Right pane content
                    when (workspaceConfig.rightPane) {
                        PaneType.AI_CONTEXT -> AIContextSidebar(note = selectedNote)
                        PaneType.AI_CHAT -> AIChatPane(modifier = modifier)
                        else -> {}
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
