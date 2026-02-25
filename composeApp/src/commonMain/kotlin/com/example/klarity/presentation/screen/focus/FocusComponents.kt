package com.example.klarity.presentation.screen.focus

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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

/**
 * Phase transition overlay shown between work and break phases.
 * 
 * Displays a celebratory message and button to start the next phase.
 */
@Composable
fun PhaseTransitionOverlay(
    fromPhase: FocusPhase,
    toPhase: FocusPhase,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut(animationSpec = tween(300)) + scaleOut(
            targetScale = 0.9f,
            animationSpec = tween(300)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(500.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Emoji with animation
                    val emoji = when (toPhase) {
                        FocusPhase.SHORT_BREAK -> "🎉"
                        FocusPhase.LONG_BREAK -> "🌟"
                        FocusPhase.WORK -> "💪"
                        FocusPhase.COMPLETED -> "✅"
                    }
                    
                    // Animated emoji
                    val infiniteTransition = rememberInfiniteTransition()
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    Text(
                        text = emoji,
                        fontSize = (64 * scale).sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    // Main message
                    Text(
                        text = when (toPhase) {
                            FocusPhase.SHORT_BREAK -> "Great work!"
                            FocusPhase.LONG_BREAK -> "Excellent progress!"
                            FocusPhase.WORK -> "Ready to focus?"
                            FocusPhase.COMPLETED -> "Task completed!"
                        },
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Secondary message
                    Text(
                        text = when (toPhase) {
                            FocusPhase.SHORT_BREAK -> "Take a 5 minute break"
                            FocusPhase.LONG_BREAK -> "Take a 15 minute break"
                            FocusPhase.WORK -> "Next focus session"
                            FocusPhase.COMPLETED -> "All sessions done!"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 18.sp
                    )
                    
                    if (toPhase != FocusPhase.COMPLETED) {
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Start",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        TextButton(onClick = onDismiss) {
                            Text("Skip")
                        }
                    } else {
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Finish",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Ambient background animation for focus mode.
 * 
 * Creates a subtle, calming background effect without being distracting.
 */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier
) {
    // Subtle gradient animation
    val infiniteTransition = rememberInfiniteTransition()
    
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF0F0F0F).copy(alpha = 0.5f + gradientOffset * 0.2f),
                        Color(0xFF0A0A0A)
                    )
                )
            )
    )
}

/**
 * Minimalist task title display.
 */
@Composable
fun FocusTaskTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.displayMedium,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier,
        lineHeight = 40.sp
    )
}

/**
 * Keyboard hint text displayed at the top of the screen.
 */
@Composable
fun KeyboardHint(
    hint: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = hint,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White.copy(alpha = 0.5f),
        fontSize = 12.sp,
        modifier = modifier
    )
}

/**
 * Focus mode controls hint bar.
 */
@Composable
fun FocusControlsHint(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlHint("[Space]", "Pause")
            ControlHint("[Enter]", "Complete")
            ControlHint("[N]", "Next task")
        }
    }
}

/**
 * Individual control hint.
 */
@Composable
private fun ControlHint(
    key: String,
    action: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        Text(
            text = action,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
    }
}
