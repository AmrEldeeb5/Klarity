package com.example.klarity.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.screen.tasks.Task
import com.example.klarity.presentation.state.SelectionMode

/**
 * Selectable Task Card Wrapper
 * 
 * Wraps any task card composable with selection functionality:
 * - Checkbox indicator when in selection mode
 * - Visual highlight when selected (border + background tint)
 * - Keyboard support (Cmd/Ctrl+Click, Shift+Click)
 * - Smooth animations between states
 * 
 * @param task The task being displayed
 * @param isSelected Whether this task is currently selected
 * @param selectionMode Current selection mode
 * @param onClick Normal click handler (when not in selection mode)
 * @param onSelectionToggle Toggle this task's selection
 * @param onRangeSelect Handle range selection (Shift+Click)
 * @param content The actual task card content to display
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SelectableTaskCard(
    task: Task,
    isSelected: Boolean,
    selectionMode: SelectionMode,
    onClick: () -> Unit,
    onSelectionToggle: () -> Unit,
    onRangeSelect: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val inSelectionMode = selectionMode != SelectionMode.NONE
    
    // Animation values
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "borderWidth"
    )
    
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.08f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "backgroundAlpha"
    )
    
    val checkboxAlpha by animateFloatAsState(
        targetValue = if (inSelectionMode) 1f else 0f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "checkboxAlpha"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            },
        shape = MaterialTheme.shapes.medium,
        border = if (isSelected) {
            BorderStroke(borderWidth, MaterialTheme.colorScheme.primary)
        } else null,
        tonalElevation = if (isSelected) 4.dp else 2.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = backgroundAlpha)
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                // Selection checkbox (appears when in selection mode)
                if (inSelectionMode) {
                    AnimatedVisibility(
                        visible = inSelectionMode,
                        enter = fadeIn(tween(150)) + expandHorizontally(tween(150)),
                        exit = fadeOut(tween(150)) + shrinkHorizontally(tween(150))
                    ) {
                        IconButton(
                            onClick = onSelectionToggle,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) {
                                    Icons.Default.CheckBox
                                } else {
                                    Icons.Default.CheckBoxOutlineBlank
                                },
                                contentDescription = if (isSelected) "Deselect task" else "Select task",
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
                
                // Task card content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start = if (inSelectionMode) 0.dp else 8.dp,
                            end = 8.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * Keyboard modifier for selection handling.
 * 
 * Detects:
 * - Cmd/Ctrl+Click: Toggle selection
 * - Shift+Click: Range selection
 */
@Composable
fun Modifier.selectionKeyHandler(
    onNormalClick: () -> Unit,
    onToggleClick: () -> Unit,
    onRangeClick: () -> Unit
): Modifier {
    var isCtrlPressed by remember { mutableStateOf(false) }
    var isShiftPressed by remember { mutableStateOf(false) }
    
    return this
        .onKeyEvent { keyEvent ->
            when (keyEvent.key) {
                Key.CtrlLeft, Key.CtrlRight, Key.MetaLeft, Key.MetaRight -> {
                    isCtrlPressed = keyEvent.type == KeyEventType.KeyDown
                    true
                }
                Key.ShiftLeft, Key.ShiftRight -> {
                    isShiftPressed = keyEvent.type == KeyEventType.KeyDown
                    true
                }
                else -> false
            }
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            when {
                isCtrlPressed -> onToggleClick()
                isShiftPressed -> onRangeClick()
                else -> onNormalClick()
            }
        }
}

/**
 * Selection indicator badge that appears in the top-right corner.
 */
@Composable
fun SelectionBadge(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isSelected,
        enter = scaleIn(tween(200)) + fadeIn(tween(200)),
        exit = scaleOut(tween(200)) + fadeOut(tween(200)),
        modifier = modifier
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.CheckBox,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Lightweight selection overlay for grid views.
 * Shows a semi-transparent overlay with a checkmark when selected.
 */
@Composable
fun SelectionOverlay(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isSelected,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(150)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckBox,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
