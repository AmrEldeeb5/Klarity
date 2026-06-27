package com.example.klarity.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Devbook design system — a Material 3 token-based theme ported faithfully from the
 * `Devbook.dc.html` Claude Design prototype.
 *
 * The design expresses its palette as CSS custom properties (`--bg`, `--p`, `--s-cont`, …)
 * for a light (`:root`) and dark (`[data-theme="dark"]`) theme, plus a fern/green accent that
 * overrides the primary colour. We mirror every token in [DevbookColors] and expose it through
 * [LocalDevbookColors] so the hand-built screens can reference tokens directly, while also
 * constructing a standard Material 3 ColorScheme for any stock M3 component.
 */

// ════════════════════════════════════════════════════════════════════════════════
// TOKENS
// ════════════════════════════════════════════════════════════════════════════════

@Immutable
data class DevbookColors(
    val bg: Color,
    val surf: Color,
    val sLowest: Color,
    val sLow: Color,
    val sCont: Color,
    val sHigh: Color,
    val sHighest: Color,
    val on: Color,
    val onv: Color,
    val outline: Color,
    val outlinev: Color,
    val p: Color,
    val op: Color,
    val pc: Color,
    val opc: Color,
    val sec: Color,
    val secc: Color,
    val onsecc: Color,
    val tert: Color,
    val tertc: Color,
    val ontertc: Color,
    val err: Color,
    val errc: Color,
    val isDark: Boolean,
)

/** Light palette — from the design's `:root`. */
private fun lightDevbookColors(primary: Color) = DevbookColors(
    bg = Color(0xFFF6FBF1),
    surf = Color(0xFFF6FBF1),
    sLowest = Color(0xFFFFFFFF),
    sLow = Color(0xFFF0F5EB),
    sCont = Color(0xFFEAEFE4),
    sHigh = Color(0xFFE4E9DE),
    sHighest = Color(0xFFDEE4D8),
    on = Color(0xFF181D17),
    onv = Color(0xFF414941),
    outline = Color(0xFF717971),
    outlinev = Color(0xFFC1C9BE),
    p = primary,
    op = Color(0xFFFFFFFF),
    pc = Color(0xFFB8F2BD),
    opc = Color(0xFF00210B),
    sec = Color(0xFF516350),
    secc = Color(0xFFD4E8CF),
    onsecc = Color(0xFF0F1F11),
    tert = Color(0xFF39656C),
    tertc = Color(0xFFBCEBF3),
    ontertc = Color(0xFF001F24),
    err = Color(0xFFBA1A1A),
    errc = Color(0xFFFFDAD6),
    isDark = false,
)

/** Dark palette — from the design's `[data-theme="dark"]`. */
private fun darkDevbookColors(primary: Color) = DevbookColors(
    bg = Color(0xFF0F140F),
    surf = Color(0xFF0F140F),
    sLowest = Color(0xFF0A0F0A),
    sLow = Color(0xFF171D16),
    sCont = Color(0xFF1B211A),
    sHigh = Color(0xFF262B24),
    sHighest = Color(0xFF30362E),
    on = Color(0xFFDFE4DA),
    onv = Color(0xFFC1C9BE),
    outline = Color(0xFF8B9389),
    outlinev = Color(0xFF414941),
    p = primary,
    op = Color(0xFF003915),
    pc = Color(0xFF1E5128),
    opc = Color(0xFFB8F2BD),
    sec = Color(0xFFB9CCB3),
    secc = Color(0xFF3A4B3A),
    onsecc = Color(0xFFD4E8CF),
    tert = Color(0xFFA1CED6),
    tertc = Color(0xFF1F4D54),
    ontertc = Color(0xFFBCEBF3),
    err = Color(0xFFFFB4AB),
    errc = Color(0xFF93000A),
    isDark = true,
)

// ════════════════════════════════════════════════════════════════════════════════
// THEME MODE + ACCENT
// ════════════════════════════════════════════════════════════════════════════════

enum class ThemeMode { LIGHT, DARK }

/**
 * Accent presets from the design. Each overrides the primary (`--p`) colour; "Fern" keeps the
 * theme's default primary.
 */
enum class Accent(val label: String, val override: Color?) {
    FERN("Fern (default)", null),
    FOREST("Forest", Color(0xFF2E6B4F)),
    PINE("Pine", Color(0xFF1E5631)),
    MOSS("Moss", Color(0xFF4C7A3F)),
    EMERALD("Emerald", Color(0xFF1B7A4B)),
}

private val DefaultLightPrimary = Color(0xFF36693F)
private val DefaultDarkPrimary = Color(0xFF9DD69E)

// ════════════════════════════════════════════════════════════════════════════════
// COMPOSITION LOCAL + ACCESSOR
// ════════════════════════════════════════════════════════════════════════════════

val LocalDevbookColors = staticCompositionLocalOf { lightDevbookColors(DefaultLightPrimary) }

/** Access the active Devbook tokens, e.g. `DevbookTheme.colors.p`. */
object DevbookTheme {
    val colors: DevbookColors
        @Composable get() = LocalDevbookColors.current
}

/** Roboto-based monospace family for code blocks (FontFamily.Monospace ≈ Roboto Mono on Android). */
val MonoFamily = FontFamily.Monospace

// ════════════════════════════════════════════════════════════════════════════════
// TYPOGRAPHY + SHAPES
// ════════════════════════════════════════════════════════════════════════════════

private val DevbookTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 30.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
)

private val DevbookShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(9.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

// ════════════════════════════════════════════════════════════════════════════════
// THEME
// ════════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DevbookAppTheme(
    themeMode: ThemeMode,
    accent: Accent,
    content: @Composable () -> Unit,
) {
    val dark = themeMode == ThemeMode.DARK
    val primary = accent.override ?: if (dark) DefaultDarkPrimary else DefaultLightPrimary
    val c = if (dark) darkDevbookColors(primary) else lightDevbookColors(primary)

    val scheme = if (dark) {
        darkColorScheme(
            primary = c.p, onPrimary = c.op, primaryContainer = c.pc, onPrimaryContainer = c.opc,
            secondary = c.sec, onSecondary = Color(0xFF243325), secondaryContainer = c.secc, onSecondaryContainer = c.onsecc,
            tertiary = c.tert, onTertiary = Color(0xFF003640), tertiaryContainer = c.tertc, onTertiaryContainer = c.ontertc,
            error = c.err, onError = Color(0xFF690005), errorContainer = c.errc, onErrorContainer = Color(0xFFFFDAD6),
            background = c.bg, onBackground = c.on, surface = c.surf, onSurface = c.on,
            surfaceVariant = c.sHigh, onSurfaceVariant = c.onv,
            surfaceContainerLowest = c.sLowest, surfaceContainerLow = c.sLow, surfaceContainer = c.sCont,
            surfaceContainerHigh = c.sHigh, surfaceContainerHighest = c.sHighest,
            outline = c.outline, outlineVariant = c.outlinev,
        )
    } else {
        lightColorScheme(
            primary = c.p, onPrimary = c.op, primaryContainer = c.pc, onPrimaryContainer = c.opc,
            secondary = c.sec, onSecondary = Color.White, secondaryContainer = c.secc, onSecondaryContainer = c.onsecc,
            tertiary = c.tert, onTertiary = Color.White, tertiaryContainer = c.tertc, onTertiaryContainer = c.ontertc,
            error = c.err, onError = Color.White, errorContainer = c.errc, onErrorContainer = Color(0xFF410002),
            background = c.bg, onBackground = c.on, surface = c.surf, onSurface = c.on,
            surfaceVariant = c.sHigh, onSurfaceVariant = c.onv,
            surfaceContainerLowest = c.sLowest, surfaceContainerLow = c.sLow, surfaceContainer = c.sCont,
            surfaceContainerHigh = c.sHigh, surfaceContainerHighest = c.sHighest,
            outline = c.outline, outlineVariant = c.outlinev,
        )
    }

    CompositionLocalProvider(LocalDevbookColors provides c) {
        // Material 3 Expressive: the springy expressive MotionScheme drives every stock M3
        // component's transitions (state-layer, ripple, container morphing), giving the app the
        // authentic Material 3 feel while the colour scheme / shapes stay faithful to Devbook.
        MaterialExpressiveTheme(
            colorScheme = scheme,
            motionScheme = MotionScheme.expressive(),
            shapes = DevbookShapes,
            typography = DevbookTypography,
            content = content,
        )
    }
}
