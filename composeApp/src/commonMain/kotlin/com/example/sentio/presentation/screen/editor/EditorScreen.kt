package com.example.sentio.presentation.screen.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.sentio.presentation.state.EditorUiEvent
import com.example.sentio.presentation.state.EditorUiState
import com.example.sentio.presentation.viewmodel.EditorViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EditorScreen(
    noteId: String,
    onBack: () -> Unit,
    viewModel: EditorViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(noteId) {
        if (noteId != "new") {
            viewModel.onEvent(EditorUiEvent.LoadNote(noteId))
        } else {
            viewModel.onEvent(EditorUiEvent.CreateNewNote)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        EditorTopBar(
            onBack = onBack,
            isPinned = (uiState as? EditorUiState.Success)?.note?.isPinned ?: false,
            isFavorite = (uiState as? EditorUiState.Success)?.note?.isFavorite ?: false,
            onTogglePin = { viewModel.onEvent(EditorUiEvent.TogglePin) },
            onToggleFavorite = { viewModel.onEvent(EditorUiEvent.ToggleFavorite) }
        )

        when (val state = uiState) {
            is EditorUiState.Idle -> {}
            is EditorUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is EditorUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        state.retryAction?.let { retry ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = retry) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
            is EditorUiState.Success -> {
                EditorPane(
                    title = state.note.title,
                    content = state.note.content,
                    onTitleChange = { viewModel.onEvent(EditorUiEvent.UpdateTitle(it)) },
                    onContentChange = { viewModel.onEvent(EditorUiEvent.UpdateContent(it)) },
                    onSave = { viewModel.onEvent(EditorUiEvent.Save) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is EditorUiState.NewNote -> {
                EditorPane(
                    title = state.title,
                    content = state.content,
                    onTitleChange = { viewModel.onEvent(EditorUiEvent.UpdateTitle(it)) },
                    onContentChange = { viewModel.onEvent(EditorUiEvent.UpdateContent(it)) },
                    onSave = { viewModel.onEvent(EditorUiEvent.Save) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTopBar(
    onBack: () -> Unit,
    isPinned: Boolean,
    isFavorite: Boolean,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    TopAppBar(
        title = { Text("Editor") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        },
        actions = {
            IconButton(onClick = onTogglePin) {
                Icon(
                    if (isPinned) Icons.Filled.Star else Icons.Outlined.Star,
                    "Pin note"
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    "Favorite"
                )
            }
        }
    )
}

@Composable
private fun EditorPane(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var titleText by remember(title) { mutableStateOf(title) }
    var contentText by remember(content) { mutableStateOf(content) }

    // Auto-save with debouncing
    LaunchedEffect(titleText, contentText) {
        if (titleText != title) onTitleChange(titleText)
        if (contentText != content) onContentChange(contentText)
        
        // Delay and then auto-save
        kotlinx.coroutines.delay(1000)
        onSave()
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BasicTextField(
            value = titleText,
            onValueChange = { titleText = it },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                if (titleText.isEmpty()) {
                    Text(
                        "Note title...",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                innerTextField()
            },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        BasicTextField(
            value = contentText,
            onValueChange = { contentText = it },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box {
                    if (contentText.isEmpty()) {
                        Text(
                            "Start writing...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}


