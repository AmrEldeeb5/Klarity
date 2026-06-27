package com.example.klarity.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.Task
import com.example.klarity.domain.models.TaskStatus
import com.example.klarity.presentation.DevbookScreen
import com.example.klarity.presentation.WorkspaceViewModel
import com.example.klarity.presentation.components.DbIcons
import com.example.klarity.presentation.components.MsIcon
import com.example.klarity.presentation.components.MsIconButton
import com.example.klarity.presentation.components.StatusPill
import com.example.klarity.presentation.theme.DevbookTheme
import com.example.klarity.presentation.theme.LocalWindowMetrics
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Composable
fun DevbookHomeScreen(vm: WorkspaceViewModel, navigate: (DevbookScreen) -> Unit) {
    val c = DevbookTheme.colors
    val notes by vm.notes.collectAsState()
    val tasks by vm.tasks.collectAsState()
    val chat by vm.chat.collectAsState()

    val openTasks = tasks.filter { it.status != TaskStatus.DONE && !it.completed }
    val pinned = notes.filter { it.isPinned }
    val openNote: (Note) -> Unit = { vm.selectNote(it.id); navigate(DevbookScreen.NOTEBOOK) }
    val m = LocalWindowMetrics.current
    val compact = m.isCompact

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 1080.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = m.screenPaddingH, end = m.screenPaddingH, top = m.screenPaddingTop, bottom = 96.dp),
        ) {
            Text(vm.todayLabel, color = c.onv, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text("${vm.greeting} 👋", color = c.on, fontSize = if (compact) 26.sp else 30.sp, fontWeight = FontWeight.Normal)
            Spacer(Modifier.height(if (compact) 22.dp else 26.dp))

            // Stat cards: a single equal row on desktop; full-width stacked tiles on phones so
            // each value/label has room to breathe instead of being squeezed into a third of the width.
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(Modifier.fillMaxWidth(), DbIcons.taskAlt, c.opc, "${openTasks.size}", "Tasks to do", c.pc, c.opc, compact = true) { navigate(DevbookScreen.TASKS) }
                    StatCard(Modifier.fillMaxWidth(), DbIcons.editNote, c.tert, "${notes.size}", "Notes", c.sCont, c.on, labelColor = c.onv, compact = true) { navigate(DevbookScreen.NOTEBOOK) }
                    StatCard(Modifier.fillMaxWidth(), DbIcons.autoAwesome, c.ontertc, "${chat.count { !it.fromUser }}", "Lou answers", c.tertc, c.ontertc, compact = true) { navigate(DevbookScreen.ASSISTANT) }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(Modifier.weight(1f), DbIcons.taskAlt, c.opc, "${openTasks.size}", "Tasks to do", c.pc, c.opc) { navigate(DevbookScreen.TASKS) }
                    StatCard(Modifier.weight(1f), DbIcons.editNote, c.tert, "${notes.size}", "Notes", c.sCont, c.on, labelColor = c.onv) { navigate(DevbookScreen.NOTEBOOK) }
                    StatCard(Modifier.weight(1f), DbIcons.autoAwesome, c.ontertc, "${chat.count { !it.fromUser }}", "Lou answers", c.tertc, c.ontertc) { navigate(DevbookScreen.ASSISTANT) }
                }
            }
            Spacer(Modifier.height(m.sectionGap))

            // Pinned notes + open tasks: side-by-side on desktop, stacked on phones.
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(m.sectionGap), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) { PinnedSection(pinned, openNote) }
                    Column(modifier = Modifier.fillMaxWidth()) { OpenTasksSection(openTasks) { task -> vm.moveTask(task, TaskStatus.DONE) } }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1.3f)) { PinnedSection(pinned, openNote) }
                    Column(modifier = Modifier.weight(1f)) { OpenTasksSection(openTasks) { task -> vm.moveTask(task, TaskStatus.DONE) } }
                }
            }
        }
    }
}

@Composable
private fun PinnedSection(pinned: List<Note>, openNote: (Note) -> Unit) {
    SectionHeader(DbIcons.pushPin, "Pinned notes")
    Spacer(Modifier.height(12.dp))
    if (pinned.isEmpty()) {
        EmptyCard("No pinned notes yet. Open a note and tap the pin.")
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            pinned.forEach { note -> PinnedNote(note) { openNote(note) } }
        }
    }
}

@Composable
private fun OpenTasksSection(openTasks: List<Task>, onComplete: (Task) -> Unit) {
    val c = DevbookTheme.colors
    SectionHeader(DbIcons.today, "Open tasks")
    Spacer(Modifier.height(12.dp))
    if (openTasks.isEmpty()) {
        EmptyCard("No open tasks. Add one on the Tasks board.")
    } else {
        OutlinedCard(
            colors = CardDefaults.cardColors(containerColor = c.sLow),
            border = BorderStroke(1.dp, c.outlinev),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                openTasks.take(6).forEach { task -> TaskRow(task) { onComplete(task) } }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    bg: Color,
    fg: Color,
    labelColor: Color = fg,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    if (compact) {
        // Phone tile: icon + value + label on one row, full width.
        Surface(
            onClick = onClick,
            color = bg,
            contentColor = fg,
            shape = RoundedCornerShape(20.dp),
            modifier = modifier,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MsIcon(icon, 26.dp, iconTint)
                Text(value, color = fg, fontSize = 30.sp, fontWeight = FontWeight.Medium)
                Text(label, color = labelColor, fontSize = 15.sp, modifier = Modifier.weight(1f))
            }
        }
    } else {
        Surface(
            onClick = onClick,
            color = bg,
            contentColor = fg,
            shape = RoundedCornerShape(24.dp),
            modifier = modifier,
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MsIcon(icon, 26.dp, iconTint)
                Text(value, color = fg, fontSize = 34.sp, fontWeight = FontWeight.Medium)
                Text(label, color = labelColor, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    val c = DevbookTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MsIcon(icon, 20.dp, c.onv)
        Text(title, color = c.on, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PinnedNote(note: Note, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = c.sLow),
        border = BorderStroke(1.dp, c.outlinev),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(c.pc), contentAlignment = Alignment.Center) {
                MsIcon(DbIcons.description, 22.dp, c.opc)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(note.title.ifBlank { "Untitled note" }, color = c.on, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(3.dp))
                Text(note.preview().ifBlank { "No content yet" }, color = c.onv, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(relativeTime(note.updatedAt), color = c.onv, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TaskRow(task: Task, onComplete: () -> Unit) {
    val c = DevbookTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MsIconButton(
            DbIcons.radioUnchecked,
            onComplete,
            "Mark task done",
            tint = c.outline,
            buttonSize = 32.dp,
            iconSize = 22.dp,
        )
        Text(
            task.title.ifBlank { "Untitled task" },
            color = c.on,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        StatusPill(task.status)
    }
}

@Composable
private fun EmptyCard(text: String) {
    val c = DevbookTheme.colors
    OutlinedCard(
        colors = CardDefaults.cardColors(containerColor = c.sLow),
        border = BorderStroke(1.dp, c.outlinev),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text, color = c.onv, fontSize = 13.5.sp, modifier = Modifier.padding(20.dp))
    }
}

private fun relativeTime(instant: Instant): String {
    // Work in whole minutes (kotlin.time.Duration.inWholeMinutes) and derive hours/days by integer
    // math — avoids the Duration comparison-literal APIs and keeps this stable across datetime versions.
    val minutes = (Clock.System.now() - instant).inWholeMinutes
    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m"
        minutes < 60 * 24 -> "${minutes / 60}h"
        else -> "${minutes / (60 * 24)}d"
    }
}
