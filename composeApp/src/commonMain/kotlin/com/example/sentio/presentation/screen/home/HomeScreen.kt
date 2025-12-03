package com.example.sentio.presentation.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sentio.domain.models.Note
import com.example.sentio.presentation.state.HomeUiEvent
import com.example.sentio.presentation.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNoteClick: (String) -> Unit,
    onCreateNote: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar
        Sidebar(modifier = Modifier.width(250.dp).fillMaxHeight())
        
        // Main content area
        MainContent(
            notes = notes,
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.onEvent(HomeUiEvent.SearchQueryChanged(it)) },
            onNoteClick = onNoteClick,
            onCreateNote = {
                viewModel.onEvent(HomeUiEvent.CreateNote)
            },
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
    }
}

@Composable
private fun Sidebar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🌿 Sentio",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            NavigationItem("All Notes")
            NavigationItem("Favorites")
            NavigationItem("Recent")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Folders",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            NavigationItem("📁 Work")
            NavigationItem("📁 Personal")
            NavigationItem("📁 Learning")
        }
    }
}

@Composable
private fun NavigationItem(text: String) {
    TextButton(
        onClick = { /* TODO */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun MainContent(
    notes: List<Note>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onCreateNote = onCreateNote
            )
            
            if (notes.isEmpty()) {
                EmptyState(onCreateNote = onCreateNote)
            } else {
                NotesList(
                    notes = notes,
                    onNoteClick = onNoteClick
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCreateNote: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search notes...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            FloatingActionButton(onClick = onCreateNote) {
                Icon(Icons.Default.Add, "Create note")
            }
        }
    }
}

@Composable
private fun NotesList(
    notes: List<Note>,
    onNoteClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteListItem(
                note = note,
                onClick = { onNoteClick(note.id) }
            )
        }
    }
}

@Composable
private fun NoteListItem(
    note: Note,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (note.content.isNotBlank()) {
                Text(
                    text = note.preview(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (note.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    note.tags.take(3).forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onCreateNote: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Sentio",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your unified developer operating system",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onCreateNote) {
            Text("Create Your First Note")
        }
    }
}
