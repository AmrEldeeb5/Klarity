package com.example.klarity.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.klarity.domain.models.Task
import com.example.klarity.domain.models.TaskStatus
import com.example.klarity.presentation.components.DbIcons
import com.example.klarity.presentation.components.Dot
import com.example.klarity.presentation.components.MsIconButton
import com.example.klarity.presentation.theme.DevbookTheme
import com.example.klarity.presentation.util.MONTH_ABBR
import com.example.klarity.presentation.util.WEEKDAY_ABBR
import com.example.klarity.presentation.util.addMonths
import com.example.klarity.presentation.util.firstOfMonth
import com.example.klarity.presentation.util.monthGrid
import com.example.klarity.presentation.util.monthTitle
import com.example.klarity.presentation.util.toLocalDate
import com.example.klarity.presentation.util.toStartInstant
import com.example.klarity.presentation.util.todayDate
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

// ── Shared month-grid pieces ────────────────────────────────────────────────────

/** Month title with ‹ › navigation and a "Today" jump. */
@Composable
private fun MonthHeaderBar(month: LocalDate, onPrev: () -> Unit, onNext: () -> Unit, onToday: () -> Unit) {
    val c = DevbookTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(month.monthTitle(), color = c.on, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onToday) { Text("Today", fontSize = 13.sp) }
        MsIconButton(icon = DbIcons.chevronLeft, onClick = onPrev, contentDescription = "Previous month", tint = c.onv, buttonSize = 34.dp, iconSize = 22.dp)
        MsIconButton(icon = DbIcons.chevronRight, onClick = onNext, contentDescription = "Next month", tint = c.onv, buttonSize = 34.dp, iconSize = 22.dp)
    }
}

@Composable
private fun WeekdayHeader() {
    val c = DevbookTheme.colors
    Row(Modifier.fillMaxWidth()) {
        WEEKDAY_ABBR.forEach { d ->
            Text(
                d,
                color = c.onv,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** A small pill for a task on the calendar, tinted in its priority colour. */
@Composable
private fun CalTaskChip(task: Task, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    val done = task.completed || task.status == TaskStatus.DONE
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = Color(task.priority.color).copy(alpha = 0.16f),
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp).clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Dot(Color(task.priority.color), 6.dp)
            Text(
                task.title.ifBlank { "Untitled" },
                fontSize = 10.5.sp,
                color = c.on,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (done) TextDecoration.LineThrough else null,
            )
        }
    }
}

// ── Full Calendar view (Tasks screen) ───────────────────────────────────────────

/**
 * Month calendar of tasks, anchored on each task's due date. Tasks with no due date — which today is
 * every task — collect in an "Unscheduled" rail beneath the grid. On a narrow window the grid is
 * replaced by an agenda list. Tapping a chip opens the task; tapping an empty day creates one due that day.
 */
@Composable
internal fun TaskCalendarView(
    tasks: List<Task>,
    compact: Boolean,
    onOpen: (Task) -> Unit,
    onAddOnDay: (Instant) -> Unit,
) {
    val today = remember { todayDate() }
    var month by remember { mutableStateOf(today.firstOfMonth()) }
    val byDay = remember(tasks) { tasks.filter { it.dueDate != null }.groupBy { it.dueDate!!.toLocalDate() } }
    val unscheduled = remember(tasks) { tasks.filter { it.dueDate == null } }

    Column(Modifier.fillMaxSize()) {
        MonthHeaderBar(month, onPrev = { month = month.addMonths(-1) }, onNext = { month = month.addMonths(1) }, onToday = { month = today.firstOfMonth() })
        Spacer(Modifier.height(12.dp))
        if (compact) {
            CalendarAgenda(month, byDay, unscheduled, onOpen)
        } else {
            WeekdayHeader()
            Spacer(Modifier.height(6.dp))
            val grid = remember(month) { month.monthGrid() }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                grid.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            DayCell(
                                modifier = Modifier.weight(1f),
                                day = day,
                                inMonth = day.monthNumber == month.monthNumber,
                                isToday = day == today,
                                dayTasks = byDay[day].orEmpty(),
                                onOpen = onOpen,
                                onAdd = { onAddOnDay(day.toStartInstant()) },
                            )
                        }
                    }
                }
                if (unscheduled.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    UnscheduledRail(unscheduled, onOpen)
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun DayCell(
    modifier: Modifier,
    day: LocalDate,
    inMonth: Boolean,
    isToday: Boolean,
    dayTasks: List<Task>,
    onOpen: (Task) -> Unit,
    onAdd: () -> Unit,
) {
    val c = DevbookTheme.colors
    Column(
        modifier = modifier
            .height(116.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (inMonth) c.sLow else c.bg)
            .border(1.dp, c.outlinev, RoundedCornerShape(8.dp))
            .clickable { onAdd() }
            .padding(6.dp),
    ) {
        if (isToday) {
            Box(Modifier.size(22.dp).clip(CircleShape).background(c.p), contentAlignment = Alignment.Center) {
                Text("${day.dayOfMonth}", color = c.op, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                "${day.dayOfMonth}",
                color = if (inMonth) c.on else c.outline,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 2.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        val shown = dayTasks.take(3)
        shown.forEach { CalTaskChip(it, onClick = { onOpen(it) }) }
        if (dayTasks.size > shown.size) {
            Text("+${dayTasks.size - shown.size} more", color = c.onv, fontSize = 10.sp, modifier = Modifier.padding(start = 2.dp, top = 1.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UnscheduledRail(tasks: List<Task>, onOpen: (Task) -> Unit) {
    val c = DevbookTheme.colors
    Column {
        Text(
            "Unscheduled · ${tasks.size}",
            color = c.onv,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tasks.forEach { t -> UnscheduledPill(t, onOpen) }
        }
    }
}

@Composable
private fun UnscheduledPill(task: Task, onOpen: (Task) -> Unit) {
    val c = DevbookTheme.colors
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = c.sLow,
        border = BorderStroke(1.dp, c.outlinev),
        modifier = Modifier.clickable { onOpen(task) },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp).width(180.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Dot(Color(task.priority.color), 7.dp)
            Text(task.title.ifBlank { "Untitled" }, fontSize = 12.sp, color = c.on, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CalendarAgenda(
    month: LocalDate,
    byDay: Map<LocalDate, List<Task>>,
    unscheduled: List<Task>,
    onOpen: (Task) -> Unit,
) {
    val c = DevbookTheme.colors
    val days = remember(month, byDay) {
        byDay.keys.filter { it.monthNumber == month.monthNumber && it.year == month.year }.sorted()
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (days.isEmpty() && unscheduled.isEmpty()) {
            Text("No tasks this month.", color = c.onv, fontSize = 14.sp)
        }
        days.forEach { day ->
            Column {
                Text(
                    "${WEEKDAY_ABBR[day.dayOfWeek.isoDayNumber % 7]}, ${MONTH_ABBR[day.monthNumber - 1]} ${day.dayOfMonth}",
                    color = c.onv,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                byDay.getValue(day).forEach { AgendaRow(it, onOpen) }
            }
        }
        if (unscheduled.isNotEmpty()) UnscheduledRail(unscheduled, onOpen)
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun AgendaRow(task: Task, onOpen: (Task) -> Unit) {
    val c = DevbookTheme.colors
    val done = task.completed || task.status == TaskStatus.DONE
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onOpen(task) }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Dot(Color(task.priority.color), 8.dp)
        Text(
            task.title.ifBlank { "Untitled" },
            color = c.on,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (done) TextDecoration.LineThrough else null,
        )
    }
}

// ── Custom date picker popup (replaces the M3 DatePicker; safe under the kotlinx-datetime 0.6.1 pin) ──

/**
 * A compact month-grid date picker shown in a dialog. Built on the same [monthGrid] math as the
 * Calendar view so it works under the 0.6.1 datetime pin — unlike Material 3's DatePicker, whose
 * desktop CalendarModel is compiled against 0.7.x-only APIs and crashes here.
 */
@Composable
internal fun DatePickerPopup(initial: LocalDate?, onPick: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val c = DevbookTheme.colors
    val today = remember { todayDate() }
    var month by remember { mutableStateOf((initial ?: today).firstOfMonth()) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = c.sLowest,
            border = BorderStroke(1.dp, c.outlinev),
        ) {
            Column(Modifier.width(312.dp).padding(16.dp)) {
                MonthHeaderBar(month, onPrev = { month = month.addMonths(-1) }, onNext = { month = month.addMonths(1) }, onToday = { month = today.firstOfMonth() })
                Spacer(Modifier.height(8.dp))
                WeekdayHeader()
                Spacer(Modifier.height(4.dp))
                val grid = remember(month) { month.monthGrid() }
                grid.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            PickerDayCell(
                                modifier = Modifier.weight(1f),
                                day = day,
                                inMonth = day.monthNumber == month.monthNumber,
                                isToday = day == today,
                                selected = day == initial,
                                onClick = { onPick(day) },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", fontSize = 14.sp) }
                }
            }
        }
    }
}

@Composable
private fun PickerDayCell(
    modifier: Modifier,
    day: LocalDate,
    inMonth: Boolean,
    isToday: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val c = DevbookTheme.colors
    Box(
        modifier = modifier
            .padding(2.dp)
            .size(38.dp)
            .clip(CircleShape)
            .background(if (selected) c.p else if (isToday) c.sCont else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "${day.dayOfMonth}",
            fontSize = 13.sp,
            color = when {
                selected -> c.op
                !inMonth -> c.outline
                else -> c.on
            },
            fontWeight = if (selected || isToday) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
