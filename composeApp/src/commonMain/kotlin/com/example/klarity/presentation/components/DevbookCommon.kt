package com.example.klarity.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BugReport
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared Devbook UI primitives: the icon set (mapping the design's Material Symbols names to
 * Compose's Material Icons Extended), an avatar, the custom theme switch, and a hover helper.
 */

/** Central icon map — keyed by the Material Symbol names used in `Devbook.dc.html`. */
object DbIcons {
    val menuBook = Icons.AutoMirrored.Outlined.MenuBook
    val unfoldMore = Icons.Outlined.UnfoldMore
    val search = Icons.Outlined.Search
    val home = Icons.Outlined.Home
    val editNote = Icons.Outlined.EditNote
    val viewKanban = Icons.Outlined.ViewKanban
    val autoAwesome = Icons.Outlined.AutoAwesome
    val add = Icons.Outlined.Add
    val expandMore = Icons.Outlined.KeyboardArrowDown
    val chevronRight = Icons.AutoMirrored.Outlined.KeyboardArrowRight
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
    val filterList = Icons.Outlined.FilterList
    val group = Icons.Outlined.Group
    val checklist = Icons.Outlined.Checklist
    val schedule = Icons.Outlined.Schedule
    val comment = Icons.AutoMirrored.Outlined.Comment
    val thumbUp = Icons.Outlined.ThumbUp
    val thumbDown = Icons.Outlined.ThumbDown
    val refresh = Icons.Outlined.Refresh
    val summarize = Icons.Outlined.Summarize
    val attachFile = Icons.Outlined.AttachFile
    val arrowUpward = Icons.Outlined.ArrowUpward
}

/** A material icon rendered at a fixed pixel-faithful [size]. */
@Composable
fun MsIcon(
    icon: ImageVector,
    size: Dp,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = modifier.size(size))
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
 * The pill theme switch from the design footer.
 * Light: neutral track with an outline thumb at the left; Dark: primary track with the
 * onPrimary thumb slid to the right.
 */
@Composable
fun DevbookSwitch(dark: Boolean, primary: Color, neutralTrack: Color, outline: Color, onPrimary: Color) {
    val trackColor by animateColorAsState(if (dark) primary else neutralTrack)
    val borderColor by animateColorAsState(if (dark) primary else outline)
    val thumbColor by animateColorAsState(if (dark) onPrimary else outline)
    val thumbSize by animateDpAsState(if (dark) 20.dp else 14.dp)
    // Track is 46 wide; thumb centred at 30px (dark) / 13px (light) from the left.
    val thumbCenterX by animateDpAsState(if (dark) 30.dp else 13.dp)

    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(trackColor)
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(13.dp)),
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbCenterX - thumbSize / 2, y = (26.dp - thumbSize) / 2)
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor),
        )
    }
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
