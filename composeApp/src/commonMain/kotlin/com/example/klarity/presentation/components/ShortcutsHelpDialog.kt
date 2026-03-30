package com.example.klarity.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.klarity.presentation.utils.*

/**
 * Keyboard Shortcuts Help Dialog
 * 
 * Displays all available keyboard shortcuts grouped by category
 * with a search feature and platform-specific key display.
 */

@Composable
fun ShortcutsHelpDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    
    var searchQuery by remember { mutableStateOf("") }
    
    // Handle Escape key to close dialog
    LaunchedEffect(visible) {
        if (visible) {
            searchQuery = ""
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .width(700.dp)
                .heightIn(max = 600.dp)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                        onDismiss()
                        true
                    } else {
                        false
                    }
                },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                ShortcutsDialogHeader(
                    onClose = onDismiss
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search bar
                ShortcutsSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Shortcuts list
                ShortcutsList(
                    searchQuery = searchQuery,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ShortcutsDialogHeader(
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Keyboard Shortcuts",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Navigate Klarity like a pro",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ShortcutsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Search shortcuts...",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    )
}

@Composable
private fun ShortcutsList(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val shortcuts = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            ShortcutsRegistry.groupedByCategory()
        } else {
            ShortcutsRegistry.search(searchQuery)
                .groupBy { it.category }
        }
    }
    
    if (shortcuts.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔍",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "No shortcuts found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Try a different search term",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        return
    }
    
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Sort categories by enum ordinal for consistent ordering
        val sortedCategories = shortcuts.keys.sortedBy { it.ordinal }
        
        sortedCategories.forEach { category ->
            val categoryShortcuts = shortcuts[category] ?: emptyList()
            
            item(key = "category_${category.name}") {
                ShortcutCategorySection(
                    category = category,
                    shortcuts = categoryShortcuts
                )
            }
        }
        
        // Add bottom padding
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ShortcutCategorySection(
    category: ShortcutCategory,
    shortcuts: List<KeyboardShortcut>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category header
        Text(
            text = category.label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // Shortcuts in this category
        shortcuts.forEach { shortcut ->
            ShortcutRow(shortcut = shortcut)
        }
    }
}

@Composable
private fun ShortcutRow(
    shortcut: KeyboardShortcut
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Description
        Text(
            text = shortcut.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Keys
        ShortcutKeys(
            key = shortcut.key,
            modifiers = shortcut.modifiers
        )
    }
}

@Composable
private fun ShortcutKeys(
    key: Key,
    modifiers: Set<KeyModifier>
) {
    val isMac = remember { isMacOS() }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Render modifiers first
        modifiers.sortedBy { it.ordinal }.forEach { modifier ->
            KeyboardKey(
                text = modifier.displayText(isMac)
            )
            Text(
                text = "+",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Render the main key
        KeyboardKey(
            text = key.displayText()
        )
    }
}

/**
 * A single keyboard key pill with macOS-inspired styling.
 */
@Composable
fun KeyboardKey(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ============================================================================
// Key Display Extensions
// ============================================================================

/**
 * Returns the display text for a modifier key.
 * Platform-specific: shows ⌘ on macOS, Ctrl on other platforms.
 */
fun KeyModifier.displayText(isMac: Boolean): String {
    return when (this) {
        KeyModifier.CTRL -> if (isMac) "⌃" else "Ctrl"
        KeyModifier.ALT -> if (isMac) "⌥" else "Alt"
        KeyModifier.SHIFT -> if (isMac) "⇧" else "Shift"
        KeyModifier.META -> if (isMac) "⌘" else "Win"
    }
}

/**
 * Returns the display text for a key.
 */
fun Key.displayText(): String {
    return when (this) {
        Key.DirectionUp -> "↑"
        Key.DirectionDown -> "↓"
        Key.DirectionLeft -> "←"
        Key.DirectionRight -> "→"
        Key.Enter -> "Enter"
        Key.Escape -> "Esc"
        Key.Tab -> "Tab"
        Key.Spacebar -> "Space"
        Key.Backspace -> "⌫"
        Key.Delete -> "Del"
        Key.Slash -> "/"
        Key.Comma -> ","
        Key.Period -> "."
        Key.Semicolon -> ";"
        Key.One -> "1"
        Key.Two -> "2"
        Key.Three -> "3"
        Key.Four -> "4"
        Key.Five -> "5"
        Key.Six -> "6"
        Key.Seven -> "7"
        Key.Eight -> "8"
        Key.Nine -> "9"
        Key.Zero -> "0"
        else -> {
            // For letter keys, convert to uppercase
            val keyName = this.toString()
            when {
                keyName.length == 1 -> keyName.uppercase()
                keyName.startsWith("Key") -> keyName.substring(3).uppercase()
                else -> keyName
            }
        }
    }
}
