package com.example.klarity.presentation.screen.focus

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Focus Timer Component - Large circular timer with progress indicator.
 * 
 * @param timeRemaining Time remaining in the current phase
 * @param phase Current focus phase (work, break, etc.)
 * @param isPaused Whether the timer is paused
 * @param progress Progress from 0.0 to 1.0
 * @param onPauseToggle Callback when pause/resume is requested
 * @param modifier Modifier for the component
 */
@Composable
fun FocusTimer(
    timeRemaining: kotlin.time.Duration,
    phase: FocusPhase,
    isPaused: Boolean,
    progress: Float,
    onPauseToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Circular progress indicator
        CircularProgressRing(
            progress = progress,
            phase = phase,
            isPaused = isPaused,
            modifier = Modifier.size(240.dp)
        )
        
        // Time display in center
        TimerDisplay(
            timeRemaining = timeRemaining,
            phase = phase,
            isPaused = isPaused
        )
    }
}

/**
 * Circular progress ring that animates as time decreases.
 */
@Composable
private fun CircularProgressRing(
    progress: Float,
    phase: FocusPhase,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when (phase) {
        FocusPhase.WORK -> MaterialTheme.colorScheme.primary
        FocusPhase.SHORT_BREAK, FocusPhase.LONG_BREAK -> MaterialTheme.colorScheme.secondary
        FocusPhase.COMPLETED -> MaterialTheme.colorScheme.tertiary
    }
    
    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300, easing = EaseInOut)
    )
    
    // Glow animation when running
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val strokeWidth = 12.dp.toPx()
        val radius = (canvasSize - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        // Background circle (track)
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        
        // Progress arc
        val sweepAngle = 360f * animatedProgress
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Glow effect when running
        if (!isPaused) {
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = glowAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius + 20.dp.toPx()
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius - 10.dp.toPx(), center.y - radius - 10.dp.toPx()),
                size = Size((radius + 10.dp.toPx()) * 2, (radius + 10.dp.toPx()) * 2),
                style = Stroke(width = strokeWidth + 20.dp.toPx())
            )
        }
    }
}

/**
 * Timer display showing time remaining and phase indicator.
 */
@Composable
private fun TimerDisplay(
    timeRemaining: kotlin.time.Duration,
    phase: FocusPhase,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    // Format time as MM:SS
    val totalSeconds = timeRemaining.inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val formattedTime = "%02d:%02d".format(minutes, seconds)
    
    // Breathing animation when running
    val infiniteTransition = rememberInfiniteTransition()
    val breathAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Phase indicator
        Text(
            text = when (phase) {
                FocusPhase.WORK -> "Focus"
                FocusPhase.SHORT_BREAK -> "Short Break"
                FocusPhase.LONG_BREAK -> "Long Break"
                FocusPhase.COMPLETED -> "Complete!"
            },
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Time display
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.displayLarge,
            fontFamily = FontFamily.Monospace,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPaused) {
                Color.White.copy(alpha = 0.6f)
            } else {
                Color.White.copy(alpha = breathAlpha)
            }
        )
        
        // Pause indicator
        if (isPaused) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Paused",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Session progress indicator showing completed sessions.
 */
@Composable
fun SessionProgress(
    sessionsCompleted: Int,
    totalSessions: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSessions) { index ->
                SessionDot(
                    filled = index < sessionsCompleted,
                    size = 12.dp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Session count text
        Text(
            text = "$sessionsCompleted/$totalSessions sessions",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
    }
}

/**
 * Individual session dot indicator.
 */
@Composable
private fun SessionDot(
    filled: Boolean,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = if (filled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 300)
    )
    
    Box(
        modifier = modifier
            .size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = color,
                radius = this.size.minDimension / 2
            )
        }
    }
}

/**
 * Subtask progress bar component.
 */
@Composable
fun SubtaskProgressBar(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    if (total == 0) return
    
    val progress = completed.toFloat() / total.toFloat()
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Progress text
        Text(
            text = "$completed/$total subtasks • ${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
    }
}
