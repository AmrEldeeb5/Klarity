package com.example.sentio.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = SentioColors.AccentPrimary,
    onPrimary = SentioColors.BgPrimary,
    primaryContainer = SentioColors.AccentSecondary,
    onPrimaryContainer = SentioColors.TextPrimary,

    secondary = SentioColors.AccentAI,
    onSecondary = SentioColors.TextPrimary,
    secondaryContainer = SentioColors.AccentAISecondary,
    onSecondaryContainer = SentioColors.TextPrimary,

    tertiary = SentioColors.Info,
    onTertiary = SentioColors.TextPrimary,

    error = SentioColors.Error,
    onError = SentioColors.TextPrimary,

    background = SentioColors.BgPrimary,
    onBackground = SentioColors.TextPrimary,

    surface = SentioColors.BgSecondary,
    onSurface = SentioColors.TextPrimary,
    surfaceVariant = SentioColors.BgTertiary,
    onSurfaceVariant = SentioColors.TextSecondary,

    outline = SentioColors.BorderPrimary,
    outlineVariant = SentioColors.BorderSecondary
)

private val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Sentio app theme - Dark theme optimized for developers.
 */
@Composable
fun SentioTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = SentioTypography,
        shapes = Shapes,
        content = content
    )
}
