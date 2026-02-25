package com.example.klarity.presentation.screen.focus

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.time.Duration.Companion.minutes

/**
 * Settings dialog for configuring focus mode timer settings.
 */
@Composable
fun FocusSettingsDialog(
    settings: FocusTimerSettings,
    onSettingsChange: (FocusTimerSettings) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(500.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                // Title
                Text(
                    text = "Focus Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Work duration slider
                SliderWithLabel(
                    label = "Work duration",
                    value = settings.workDuration.inWholeMinutes.toFloat(),
                    onValueChange = { newValue ->
                        onSettingsChange(settings.copy(workDuration = newValue.toInt().minutes))
                    },
                    valueRange = 5f..60f,
                    steps = 10,
                    suffix = "min"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Break duration slider
                SliderWithLabel(
                    label = "Short break duration",
                    value = settings.breakDuration.inWholeMinutes.toFloat(),
                    onValueChange = { newValue ->
                        onSettingsChange(settings.copy(breakDuration = newValue.toInt().minutes))
                    },
                    valueRange = 1f..15f,
                    steps = 13,
                    suffix = "min"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Long break duration slider
                SliderWithLabel(
                    label = "Long break duration",
                    value = settings.longBreakDuration.inWholeMinutes.toFloat(),
                    onValueChange = { newValue ->
                        onSettingsChange(settings.copy(longBreakDuration = newValue.toInt().minutes))
                    },
                    valueRange = 10f..30f,
                    steps = 19,
                    suffix = "min"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sessions until long break slider
                SliderWithLabel(
                    label = "Sessions until long break",
                    value = settings.sessionsUntilLongBreak.toFloat(),
                    onValueChange = { newValue ->
                        onSettingsChange(settings.copy(sessionsUntilLongBreak = newValue.toInt()))
                    },
                    valueRange = 2f..8f,
                    steps = 5,
                    suffix = "sessions"
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Auto-start toggles
                SwitchWithLabel(
                    label = "Auto-start breaks",
                    description = "Automatically start break when work session ends",
                    checked = settings.autoStartBreak,
                    onCheckedChange = { checked ->
                        onSettingsChange(settings.copy(autoStartBreak = checked))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SwitchWithLabel(
                    label = "Auto-start work",
                    description = "Automatically start work when break ends",
                    checked = settings.autoStartWork,
                    onCheckedChange = { checked ->
                        onSettingsChange(settings.copy(autoStartWork = checked))
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(onClick = onDismiss) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

/**
 * Slider with label and value display.
 */
@Composable
private fun SliderWithLabel(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    suffix: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "${value.toInt()} $suffix",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

/**
 * Switch with label and optional description.
 */
@Composable
private fun SwitchWithLabel(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

/**
 * Exit confirmation dialog.
 */
@Composable
fun ExitFocusModeDialog(
    visible: Boolean,
    timeRemaining: kotlin.time.Duration,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    if (!visible) return
    
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Exit focus mode?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            val minutes = timeRemaining.inWholeMinutes
            Text(
                text = "You have $minutes minutes remaining. Your progress will be saved.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Exit")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Stay")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    )
}

/**
 * Keyboard shortcuts help overlay.
 */
@Composable
fun KeyboardShortcutsOverlay(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(600.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                ) {
                    Text(
                        text = "Keyboard Shortcuts",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Shortcuts list
                    val shortcuts = listOf(
                        "Space" to "Pause/Resume timer",
                        "Enter" to "Complete task",
                        "N" to "Next task",
                        "R" to "Reset timer",
                        "+/-" to "Add/subtract 5 minutes",
                        "B" to "Start break manually",
                        "S" to "Open settings",
                        "Q / Esc" to "Exit focus mode",
                        "?" to "Show keyboard shortcuts"
                    )
                    
                    shortcuts.forEach { (key, description) ->
                        ShortcutRow(key = key, description = description)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

/**
 * Individual shortcut row.
 */
@Composable
private fun ShortcutRow(
    key: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Text(
                text = key,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
