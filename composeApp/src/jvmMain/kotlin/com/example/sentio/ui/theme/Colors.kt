package com.example.sentio.ui.theme

import androidx.compose.ui.graphics.Color

object SentioColors {
    // Backgrounds
    val BgPrimary = Color(0xFF0A1612)
    val BgSecondary = Color(0xFF0F1F1A)
    val BgTertiary = Color(0xFF152922)
    val BgElevated = Color(0xFF1A2F27)

    // Accents
    val AccentPrimary = Color(0xFF3DD68C)
    val AccentSecondary = Color(0xFF2FB874)
    val AccentAI = Color(0xFF667EEA)
    val AccentAISecondary = Color(0xFF764BA2)

    // Text
    val TextPrimary = Color(0xFFE0E6E3)
    val TextSecondary = Color(0xFF8B9D94)
    val TextTertiary = Color(0xFF566B61)
    val TextDisabled = Color(0xFF3A4F45)

    // Semantic
    val Success = Color(0xFF3DD68C)
    val Warning = Color(0xFFFFC107)
    val Error = Color(0xFFFF5252)
    val Info = Color(0xFF667EEA)

    // Borders
    val BorderPrimary = Color(0xFF1F3530)
    val BorderSecondary = Color(0xFF2A4A40)
    val BorderFocus = AccentPrimary

    // Overlays
    val Overlay = Color(0x800A1612) // 50% opacity
    val OverlayLight = Color(0x400A1612) // 25% opacity

    // Card backgrounds
    val CardBg = Color(0x0DFFFFFF) // 5% white
    val CardBgHover = Color(0x1AFFFFFF) // 10% white
    val CardBgActive = Color(0x26FFFFFF) // 15% white

    // AI specific
    val AIGradientStart = AccentAI
    val AIGradientEnd = AccentAISecondary

    // Syntax highlighting (for code blocks)
    val SyntaxKeyword = Color(0xFFFF79C6)
    val SyntaxString = Color(0xFFF1FA8C)
    val SyntaxComment = Color(0xFF6272A4)
    val SyntaxFunction = Color(0xFF50FA7B)
    val SyntaxNumber = Color(0xFFBD93F9)
}

// Helper to create gradient colors
data class GradientColors(
    val start: Color,
    val end: Color
)

val AccentGradient = GradientColors(
    start = SentioColors.AccentPrimary,
    end = SentioColors.AccentSecondary
)

val AIGradient = GradientColors(
    start = SentioColors.AccentAI,
    end = SentioColors.AccentAISecondary
)