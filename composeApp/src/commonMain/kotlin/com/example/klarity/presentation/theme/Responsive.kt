package com.example.klarity.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Lightweight responsive layout system for the Devbook screens.
 *
 * The design was authored desktop-first (docked sidebar + multi-column rows). On a phone the
 * raw layout squeezes those columns into a few dozen dp each and pads the page like a 1200px
 * canvas. [WindowMetrics] turns the available width into a Material-style width class plus the
 * spacing tokens each screen should use, so layouts can reflow to a single column and breathe
 * on narrow screens while staying generous on desktop.
 */

enum class WindowWidthClass { COMPACT, MEDIUM, EXPANDED }

@Immutable
data class WindowMetrics(
    val widthClass: WindowWidthClass,
    /** Horizontal page padding (the gutter on each side of a screen's content). */
    val screenPaddingH: Dp,
    /** Top page padding. */
    val screenPaddingTop: Dp,
    /** Vertical gap between major sections of a screen. */
    val sectionGap: Dp,
) {
    val isCompact: Boolean get() = widthClass == WindowWidthClass.COMPACT
    val isExpanded: Boolean get() = widthClass == WindowWidthClass.EXPANDED
}

/** Map an available width to its width class and the spacing tokens that suit it. */
fun windowMetricsFor(maxWidth: Dp): WindowMetrics = when {
    maxWidth < 600.dp -> WindowMetrics(WindowWidthClass.COMPACT, screenPaddingH = 20.dp, screenPaddingTop = 24.dp, sectionGap = 26.dp)
    maxWidth < 840.dp -> WindowMetrics(WindowWidthClass.MEDIUM, screenPaddingH = 28.dp, screenPaddingTop = 28.dp, sectionGap = 28.dp)
    else -> WindowMetrics(WindowWidthClass.EXPANDED, screenPaddingH = 40.dp, screenPaddingTop = 32.dp, sectionGap = 28.dp)
}

/** Active window metrics, provided by the app shell. Defaults to the desktop (expanded) profile. */
val LocalWindowMetrics = staticCompositionLocalOf {
    WindowMetrics(WindowWidthClass.EXPANDED, screenPaddingH = 40.dp, screenPaddingTop = 32.dp, sectionGap = 28.dp)
}
