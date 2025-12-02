package com.example.sentio.ui.screens.editor

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.sentio.ui.viewmodels.EditorViewModel
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
            viewModel.loadNote(noteId)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        EditorTopBar(
            onBack = onBack,
            isPinned = uiState.note?.isPinned ?: false,
            isFavorite = uiState.note?.isFavorite ?: false,
            onTogglePin = viewModel::togglePin,
            onToggleFavorite = viewModel::toggleFavorite
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Editor
                    EditorPane(
                        title = uiState.note?.title ?: "",
                        content = uiState.note?.content ?: "",
                        onTitleChange = viewModel::updateTitle,
                        onContentChange = viewModel::updateContent,
                        onSave = viewModel::saveNote,
                        modifier = Modifier.weight(1f)
                    )

                    Divider(modifier = Modifier.width(1.dp).fillMaxHeight())

                    // Preview
                    MarkdownPreview(
                        content = uiState.note?.content ?: "",
                        modifier = Modifier.weight(1f)
                    )
                }
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

    // Auto-save on text change (debounced in real app)
    LaunchedEffect(titleText, contentText) {
        if (titleText != title) onTitleChange(titleText)
        if (contentText != content) onContentChange(contentText)
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title input
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

        Divider()

        // Content input
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

@Composable
private fun MarkdownPreview(
    content: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // For now, just show raw text
            // TODO: Implement proper markdown rendering
            Text(
                text = content.ifEmpty { "Preview will appear here..." },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
