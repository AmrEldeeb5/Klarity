package com.example.klarity.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.models.Task
import com.example.klarity.domain.models.TaskStatus
import com.example.klarity.presentation.components.StatusPill
import com.example.klarity.presentation.theme.DevbookTheme
import com.example.klarity.presentation.util.MONTH_ABBR
import com.example.klarity.presentation.util.WEEKDAY_ABBR
import com.example.klarity.presentation.util.daysTo
import com.example.klarity.presentation.util.plusDays
import com.example.klarity.presentation.util.toLocalDate
import com.example.klarity.presentation.util.todayDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

// Shared row metrics so the frozen label pane and the scrolling Gantt pane line up exactly.
private val ROW_H = 44.dp
private val GROUP_H = 36.dp
private val AXIS_H = 44.dp
private val DAY_W = 40.dp
private val BAR_H = 26.dp

/**
 * Gantt-style timeline of tasks. Each task is a bar spanning startDate→dueDate (a milestone marker
 * when only one date is set); rows are grouped by status and a vertical line marks today. The left
 * label column is frozen while the dated area scrolls horizontally. Read-only — tap a bar/label to edit.
 * Tasks with neither date can't be placed, so they're reported as a count rather than shown.
 */
@Composable
internal fun TaskTimelineView(tasks: List<Task>, compact: Boolean, onOpen: (Task) -> Unit) {
    val c = DevbookTheme.colors
    val today = remember { todayDate() }
    val scheduled = remember(tasks) { tasks.filter { it.startDate != null || it.dueDate != null } }
    val unscheduledCount = tasks.size - scheduled.size

    if (scheduled.isEmpty()) {
        Column(Modifier.fillMaxSize()) {
            Text("No scheduled tasks yet.", color = c.on, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text("Set a start or due date on a task to see it on the timeline.", color = c.onv, fontSize = 13.sp)
        }
        return
    }

    val dates = remember(scheduled) {
        scheduled.flatMap { listOfNotNull(it.startDate?.toLocalDate(), it.dueDate?.toLocalDate()) }
    }
    // Pad a couple of days each side, and always include today so the marker is reachable.
    val rangeStart = remember(dates, today) { (dates + today).min().plusDays(-2) }
    val rangeEnd = remember(dates, today) { (dates + today).max().plusDays(3) }
    val totalDays = remember(rangeStart, rangeEnd) { rangeStart.daysTo(rangeEnd) + 1 }
    val totalW = DAY_W * totalDays
    val todayX = DAY_W * rangeStart.daysTo(today)
    val labelW = if (compact) 132.dp else 200.dp

    val groups = remember(scheduled) {
        TaskStatus.entries.mapNotNull { st -> scheduled.filter { it.status == st }.takeIf { it.isNotEmpty() }?.let { st to it } }
    }

    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    Column(Modifier.fillMaxSize()) {
        // ── Axis header (corner + scrolling day ruler) ──
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.width(labelW).height(AXIS_H))
            Row(Modifier.horizontalScroll(hScroll)) {
                for (i in 0 until totalDays) {
                    val day = rangeStart.plusDays(i)
                    AxisDay(day = day, isToday = day == today)
                }
            }
        }
        HorizontalDivider(color = c.outlinev)

        // ── Body: frozen labels | scrolling bars (vertical scroll shared between the two) ──
        Row(Modifier.weight(1f)) {
            Column(Modifier.width(labelW).verticalScroll(vScroll)) {
                groups.forEach { (st, ts) ->
                    Box(Modifier.height(GROUP_H).fillMaxWidth().padding(start = 2.dp), contentAlignment = Alignment.CenterStart) {
                        StatusPill(status = st, label = st.label)
                    }
                    ts.forEach { task ->
                        Box(
                            Modifier.height(ROW_H).fillMaxWidth().clickable { onOpen(task) }.padding(horizontal = 4.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(task.title.ifBlank { "Untitled" }, color = c.on, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
            Box(Modifier.weight(1f).horizontalScroll(hScroll).verticalScroll(vScroll)) {
                Column(Modifier.width(totalW)) {
                    groups.forEach { (st, ts) ->
                        Box(Modifier.height(GROUP_H).width(totalW).background(c.sLow.copy(alpha = 0.4f))) {
                            TodayLine(todayX, GROUP_H)
                        }
                        ts.forEach { task ->
                            Box(Modifier.height(ROW_H).width(totalW)) {
                                TodayLine(todayX, ROW_H)
                                TaskBar(task, rangeStart, onOpen)
                            }
                        }
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }

        if (unscheduledCount > 0) {
            HorizontalDivider(color = c.outlinev)
            Text(
                "$unscheduledCount task${if (unscheduledCount == 1) "" else "s"} without dates aren't shown.",
                color = c.onv,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp, start = 2.dp),
            )
        }
    }
}

@Composable
private fun AxisDay(day: LocalDate, isToday: Boolean) {
    val c = DevbookTheme.colors
    val weekend = day.dayOfWeek.isoDayNumber >= 6
    val bg = when {
        isToday -> c.sCont
        weekend -> c.sLow.copy(alpha = 0.5f)
        else -> Color.Transparent
    }
    Column(
        Modifier.width(DAY_W).height(AXIS_H).background(bg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(WEEKDAY_ABBR[day.dayOfWeek.isoDayNumber % 7], color = c.onv, fontSize = 9.sp)
        Text(
            "${day.dayOfMonth}",
            color = if (isToday) c.p else c.on,
            fontSize = 12.sp,
            fontWeight = if (isToday || day.dayOfMonth == 1) FontWeight.Bold else FontWeight.Normal,
        )
        if (day.dayOfMonth == 1) Text(MONTH_ABBR[day.monthNumber - 1], color = c.onv, fontSize = 8.sp)
    }
}

private val TodayLineWidth = 2.dp

@Composable
private fun androidx.compose.foundation.layout.BoxScope.TodayLine(x: Dp, height: Dp) {
    val c = DevbookTheme.colors
    Box(Modifier.align(Alignment.TopStart).offset(x = x).width(TodayLineWidth).height(height).background(c.p.copy(alpha = 0.45f)))
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.TaskBar(task: Task, rangeStart: LocalDate, onOpen: (Task) -> Unit) {
    val c = DevbookTheme.colors
    val start = task.startDate?.toLocalDate()
    val due = task.dueDate?.toLocalDate()
    val color = Color(task.priority.color)
    val done = task.completed || task.status == TaskStatus.DONE

    if (start != null && due != null) {
        val s = minOf(start, due)
        val e = maxOf(start, due)
        val offsetX = DAY_W * rangeStart.daysTo(s)
        val w = DAY_W * (s.daysTo(e) + 1)
        val textColor = when {
            done -> c.onv
            color.luminance() > 0.6f -> Color(0xFF1A1A1A)
            else -> Color.White
        }
        Box(
            Modifier.align(Alignment.CenterStart).offset(x = offsetX).height(BAR_H).width(w)
                .clip(RoundedCornerShape(7.dp))
                .background(color.copy(alpha = if (done) 0.28f else 0.85f))
                .clickable { onOpen(task) }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(task.title.ifBlank { "Untitled" }, color = textColor, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    } else {
        // Single date → milestone marker, centred on its day.
        val d = (due ?: start)!!
        val offsetX = DAY_W * rangeStart.daysTo(d) + (DAY_W - 16.dp) / 2
        Box(
            Modifier.align(Alignment.CenterStart).offset(x = offsetX).size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = if (done) 0.35f else 1f))
                .clickable { onOpen(task) },
        )
    }
}
