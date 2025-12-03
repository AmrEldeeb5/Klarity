package com.example.sentio.presentation.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sentio.domain.models.Note
import com.example.sentio.presentation.theme.SentioColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Note Preview - Read-only view shown when a note is selected (single click).
 * Shows title, content, metadata, and action buttons.
 */
@Composable
fun NotePreview(
    note: Note,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header with actions
            PreviewHeader(
                note = note,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onToggleFavorite = onToggleFavorite,
                onTogglePin = onTogglePin
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = SentioColors.BorderPrimary)

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata (tags, dates)
            PreviewMetadata(note = note)

            Spacer(modifier = Modifier.height(24.dp))

            // Content (scrollable)
            PreviewContent(
                content = note.content,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PreviewHeader(
    note: Note,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePin: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Title
        Text(
            text = note.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Pin button
            IconButton(onClick = onTogglePin) {
                Icon(
                    imageVector = if (note.isPinned) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = if (note.isPinned) "Unpin" else "Pin",
                    tint = if (note.isPinned) SentioColors.Warning else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Favorite button
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (note.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (note.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (note.isFavorite) SentioColors.Error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Edit button
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit note",
                    tint = SentioColors.AccentPrimary
                )
            }

            // Delete button
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete note",
                    tint = SentioColors.Error
                )
            }
        }
    }
}

@Composable
private fun PreviewMetadata(note: Note) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tags
        if (note.tags.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                note.tags.forEach { tagName ->
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = tagName,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = SentioColors.BgElevated,
                            labelColor = SentioColors.AccentPrimary
                        )
                    )
                }
            }
        }

        // Dates
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Created: ${formatDate(note.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Updated: ${formatDate(note.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PreviewContent(
    content: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SentioColors.BgSecondary,
        shape = MaterialTheme.shapes.medium
    ) {
        if (content.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No content yet. Double-click to edit.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            )
        }
    }
}

/**
 * Format Instant to readable date string
 */
private fun formatDate(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.monthNumber}/${localDateTime.dayOfMonth}/${localDateTime.year}"
}
