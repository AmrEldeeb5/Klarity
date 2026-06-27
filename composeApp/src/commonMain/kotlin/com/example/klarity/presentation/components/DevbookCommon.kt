package com.example.klarity.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material.icons.outlined.ViewKanban
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.theme.DevbookTheme

/**
 * Shared Devbook UI primitives: the icon set (mapping the design's Material Symbols names to
 * Compose's Material Icons Extended), an avatar, the custom theme switch, and a hover helper.
 */

/** Central icon map — keyed by the Material Symbol names used in `Devbook.dc.html`. */
object DbIcons {
    val unfoldMore = Icons.Outlined.UnfoldMore
    val search = Icons.Outlined.Search
    val home = Icons.Outlined.Home
    val editNote = Icons.Outlined.EditNote
    val viewKanban = Icons.Outlined.ViewKanban
    val autoAwesome = Icons.Outlined.AutoAwesome
    val add = Icons.Outlined.Add
    val expandMore = Icons.Outlined.KeyboardArrowDown
    val chevronRight = Icons.AutoMirrored.Outlined.KeyboardArrowRight
    val chevronLeft = Icons.AutoMirrored.Outlined.KeyboardArrowLeft
    val folder = Icons.Outlined.Folder
    val description = Icons.Outlined.Description
    val delete = Icons.Outlined.Delete
    val code = Icons.Outlined.Code
    val bugReport = Icons.Outlined.BugReport
    val darkMode = Icons.Outlined.DarkMode
    val lightMode = Icons.Outlined.LightMode
    val settings = Icons.Outlined.Settings
    val history = Icons.Outlined.History
    val tune = Icons.Outlined.Tune
    val moreHoriz = Icons.Outlined.MoreHoriz
    val menu = Icons.Outlined.Menu
    val taskAlt = Icons.Outlined.TaskAlt
    val pushPin = Icons.Outlined.PushPin
    val today = Icons.Outlined.Today
    val checkCircle = Icons.Outlined.CheckCircle
    val radioUnchecked = Icons.Outlined.RadioButtonUnchecked
    val dragIndicator = Icons.Outlined.DragIndicator
    val lightbulb = Icons.Outlined.Lightbulb
    val checkBox = Icons.Outlined.CheckBox
    val checkBoxBlank = Icons.Outlined.CheckBoxOutlineBlank
    val contentCopy = Icons.Outlined.ContentCopy
    val viewList = Icons.AutoMirrored.Outlined.ViewList
    val timeline = Icons.Outlined.Timeline
    val calendar = Icons.Outlined.CalendarMonth
    val filterList = Icons.Outlined.FilterList
    val group = Icons.Outlined.Group
    val checklist = Icons.Outlined.Checklist
    val title = Icons.Outlined.Title
    val formatBulleted = Icons.AutoMirrored.Outlined.FormatListBulleted
    val formatNumbered = Icons.Outlined.FormatListNumbered
    val formatQuote = Icons.Outlined.FormatQuote
    val horizontalRule = Icons.Outlined.HorizontalRule
    val schedule = Icons.Outlined.Schedule
    val comment = Icons.AutoMirrored.Outlined.Comment
    val thumbUp = Icons.Outlined.ThumbUp
    val thumbDown = Icons.Outlined.ThumbDown
    val refresh = Icons.Outlined.Refresh
    val summarize = Icons.Outlined.Summarize
    val attachFile = Icons.Outlined.AttachFile
    val arrowUpward = Icons.Outlined.ArrowUpward
    val visibility = Icons.Outlined.Visibility
    val visibilityOff = Icons.Outlined.VisibilityOff
    val vpnKey = Icons.Outlined.VpnKey
    val close = Icons.Outlined.Close
    val openInFull = Icons.Outlined.OpenInFull
    val mic = Icons.Outlined.Mic
    val edit = Icons.Outlined.Edit
    val chat = Icons.AutoMirrored.Outlined.Chat
}

/**
 * A material icon rendered at a fixed pixel-faithful [size]. Pass [contentDescription] for
 * icon-only interactive controls so screen readers can announce them; leave null for purely
 * decorative icons that sit beside a text label.
 */
@Composable
fun MsIcon(
    icon: ImageVector,
    size: Dp,
    tint: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Icon(imageVector = icon, contentDescription = contentDescription, tint = tint, modifier = modifier.size(size))
}

/**
 * An icon-only **Material 3 [IconButton]** tinted with a Devbook token. Replaces the hand-rolled
 * `Box + clickable` affordances so every icon control gets a genuine M3 ripple, expressive
 * state-layer and the right accessibility semantics. [buttonSize] is honoured exactly — M3's 48dp
 * minimum-interactive-size floor is disabled here so the app's dense rows keep their Devbook
 * metrics (these icon controls were all sub-48dp by design); [iconSize] is the glyph size.
 */
@Composable
fun MsIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = DevbookTheme.colors.onv,
    buttonSize: Dp = 40.dp,
    iconSize: Dp = 22.dp,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        IconButton(
            onClick = onClick,
            modifier = modifier.size(buttonSize),
            enabled = enabled,
            colors = IconButtonDefaults.iconButtonColors(contentColor = tint),
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
        }
    }
}

/** Circular initials avatar. */
@Composable
fun Avatar(
    text: String,
    bg: Color,
    fg: Color,
    size: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .then(if (border != null) Modifier.border(border, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = fg, fontSize = fontSize, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * The footer theme toggle — now a genuine **Material 3 [Switch]**, themed to the Devbook palette so
 * it keeps the design's look (unchecked = neutral track + outline thumb for light mode; checked =
 * primary track + onPrimary thumb for dark mode) while gaining real M3 thumb-morph motion and a11y.
 */
@Composable
fun DevbookSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    primary: Color,
    onPrimary: Color,
    neutralTrack: Color,
    outline: Color,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = onPrimary,
            checkedTrackColor = primary,
            checkedBorderColor = primary,
            uncheckedThumbColor = outline,
            uncheckedTrackColor = neutralTrack,
            uncheckedBorderColor = outline,
        ),
    )
}

/** A small status dot. */
@Composable
fun Dot(color: Color, size: Dp = 9.dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(size).clip(CircleShape).background(color))
}

/** Applies a hover background (desktop) on top of an optional [base] colour, clipped to [shape]. */
@Composable
fun Modifier.hoverBg(shape: Shape, hover: Color, base: Color = Color.Transparent): Modifier {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    return this
        .clip(shape)
        .hoverable(source)
        .background(if (hovered) hover else base)
}
