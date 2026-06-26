package com.example.klarity.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.klarity.presentation.components.hoverBg
import com.example.klarity.presentation.theme.DevbookTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun DevbookHomeScreen(vm: WorkspaceViewModel, navigate: (DevbookScreen) -> Unit) {
    val c = DevbookTheme.colors
    val notes by vm.notes.collectAsState()
    val tasks by vm.tasks.collectAsState()
    val chat by vm.chat.collectAsState()

    val openTasks = tasks.filter { it.status != TaskStatus.DONE && !it.completed }
    val pinned = notes.filter { it.isPinned }
    val openNote: (Note) -> Unit = { vm.selectNote(it.id); navigate(DevbookScreen.NOTEBOOK) }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 1080.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 40.dp, end = 40.dp, top = 34.dp, bottom = 80.dp),
        ) {
            Text(vm.todayLabel, color = c.onv, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text("${vm.greeting} 👋", color = c.on, fontSize = 30.sp, fontWeight = FontWeight.Normal)
            Spacer(Modifier.height(26.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(Modifier.weight(1f), DbIcons.taskAlt, c.opc, "${openTasks.size}", "Tasks to do", c.pc, c.opc) { navigate(DevbookScreen.TASKS) }
                StatCard(Modifier.weight(1f), DbIcons.editNote, c.tert, "${notes.size}", "Notes", c.sCont, c.on, labelColor = c.onv) { navigate(DevbookScreen.NOTEBOOK) }
                StatCard(Modifier.weight(1f), DbIcons.autoAwesome, c.ontertc, "${chat.count { !it.fromUser }}", "Assistant answers", c.tertc, c.ontertc) { navigate(DevbookScreen.ASSISTANT) }
            }
            Spacer(Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
                // Pinned notes
                Column(modifier = Modifier.weight(1.3f)) {
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
                // Today / open tasks
                Column(modifier = Modifier.weight(1f)) {
                    SectionHeader(DbIcons.today, "Open tasks")
                    Spacer(Modifier.height(12.dp))
                    if (openTasks.isEmpty()) {
                        EmptyCard("No open tasks. Add one on the Tasks board.")
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.sLow).border(1.dp, c.outlinev, RoundedCornerShape(18.dp)).padding(8.dp),
                        ) {
                            openTasks.take(6).forEach { task -> TaskRow(task) { vm.moveTask(task, TaskStatus.DONE) } }
                        }
                    }
                }
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
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(24.dp)).background(bg).clickable { onClick() }.padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MsIcon(icon, 26.dp, iconTint)
        Text(value, color = fg, fontSize = 34.sp, fontWeight = FontWeight.Medium)
        Text(label, color = labelColor, fontSize = 14.sp)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .hoverBg(RoundedCornerShape(18.dp), c.sCont, base = c.sLow)
            .border(1.dp, c.outlinev, RoundedCornerShape(18.dp))
            .clickable { onClick() }
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

@Composable
private fun TaskRow(task: Task, onComplete: () -> Unit) {
    val c = DevbookTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onComplete() }) {
            MsIcon(DbIcons.radioUnchecked, 22.dp, c.outline)
        }
        Text(task.title.ifBlank { "Untitled task" }, color = c.on, fontSize = 14.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun EmptyCard(text: String) {
    val c = DevbookTheme.colors
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.sLow).border(1.dp, c.outlinev, RoundedCornerShape(18.dp)).padding(20.dp),
    ) {
        Text(text, color = c.onv, fontSize = 13.5.sp)
    }
}

private fun relativeTime(instant: Instant): String {
    val diff = Clock.System.now() - instant
    return when {
        diff < 1.minutes -> "now"
        diff < 1.hours -> "${diff.inWholeMinutes}m"
        diff < 1.days -> "${diff.inWholeHours}h"
        else -> "${diff.inWholeDays}d"
    }
}
