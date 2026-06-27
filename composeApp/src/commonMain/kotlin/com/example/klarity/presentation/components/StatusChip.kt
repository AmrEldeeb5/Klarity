package com.example.klarity.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.models.TaskStatus
import com.example.klarity.presentation.theme.DevbookTheme

/**
 * The shared task-status colour system. A status maps to a shade of the green theme — neutral grey
 * (Backlog) → soft green (In progress) → teal (In review) → vivid green (Done) — drawn entirely from
 * the theme's own tonal containers so it stays Material 3 and tracks the active accent. Lives here so
 * every surface that shows a status (board, list, cards, editor, home) renders it identically.
 */

/** Per-status colour bundle. */
data class StatusStyle(val dot: Color, val container: Color, val onContainer: Color)

/** Short, sentence-case label shown on the status pill everywhere. */
fun statusLabel(status: TaskStatus): String = when (status) {
    TaskStatus.BACKLOG -> "Backlog"
    TaskStatus.TODO -> "To do"
    TaskStatus.IN_PROGRESS -> "In progress"
    TaskStatus.IN_REVIEW -> "In review"
    TaskStatus.DONE -> "Done"
    TaskStatus.ARCHIVED -> "Archived"
}

@Composable
fun statusStyle(status: TaskStatus): StatusStyle {
    val c = DevbookTheme.colors
    return when (status) {
        TaskStatus.IN_PROGRESS -> StatusStyle(c.sec, c.secc, c.onsecc)
        TaskStatus.IN_REVIEW -> StatusStyle(c.tert, c.tertc, c.ontertc)
        TaskStatus.DONE -> StatusStyle(c.p, c.pc, c.opc)
        else -> StatusStyle(c.outline, c.sHigh, c.onv) // Backlog / To Do / Archived → neutral
    }
}

/** A tonal Notion-style status chip: coloured dot + label on the status's container colour. */
@Composable
fun StatusPill(status: TaskStatus, label: String = statusLabel(status), modifier: Modifier = Modifier) {
    val st = statusStyle(status)
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = st.container, contentColor = st.onContainer) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Dot(st.dot, 8.dp)
            Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
