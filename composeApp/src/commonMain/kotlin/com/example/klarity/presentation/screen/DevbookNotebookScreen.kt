package com.example.klarity.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.NoteStatus
import com.example.klarity.presentation.WorkspaceViewModel
import com.example.klarity.presentation.components.DbIcons
import com.example.klarity.presentation.components.Dot
import com.example.klarity.presentation.components.MsIcon
import com.example.klarity.presentation.components.MsIconButton
import com.example.klarity.presentation.theme.DevbookTheme
import com.example.klarity.presentation.theme.LocalWindowMetrics
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

@Composable
fun DevbookNotebookScreen(vm: WorkspaceViewModel) {
    val c = DevbookTheme.colors
    val note by vm.selectedNote.collectAsState()
    val current = note

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        if (current == null) {
            EmptyEditor(onCreate = { vm.createNote() })
        } else {
            NoteEditor(vm = vm, note = current)
        }
    }
}

@Composable
private fun NoteEditor(vm: WorkspaceViewModel, note: Note) {
    val c = DevbookTheme.colors
    // Local edit buffers, re-initialised when switching notes.
    var title by remember(note.id) { mutableStateOf(note.title) }
    var content by remember(note.id) { mutableStateOf(note.content) }

    // Debounced autosave: persist ~500ms after the last keystroke, merged onto the freshest
    // version of this note (so a meanwhile pin/status change isn't clobbered). Switching notes
    // cancels the pending save — the buffer is re-seeded from the new note, so no cross-write.
    val latestNote by rememberUpdatedState(note)
    LaunchedEffect(note.id) {
        snapshotFlow { title to content }
            .drop(1) // skip the initial buffer (== the note already on disk)
            .collectLatest { (t, ct) ->
                delay(500)
                val n = latestNote
                if (n.id == note.id && (t != n.title || ct != n.content)) {
                    vm.updateNote(n.copy(title = t, content = ct))
                }
            }
    }
    // Flush a still-pending edit if the editor leaves composition while staying on this note.
    DisposableEffect(note.id) {
        onDispose {
            val n = latestNote
            if (n.id == note.id && (title != n.title || content != n.content)) {
                vm.updateNote(n.copy(title = title, content = content))
            }
        }
    }

    // Notion-style: a just-created note opens with the title focused so you can type straight away.
    val titleFocus = remember { FocusRequester() }
    val contentFocus = remember { FocusRequester() }
    val pendingFocus by vm.pendingNoteFocus.collectAsState()
    LaunchedEffect(note.id, pendingFocus) {
        if (pendingFocus == note.id) {
            titleFocus.requestFocus()
            vm.consumeNoteFocus()
        }
    }

    val m = LocalWindowMetrics.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = m.screenPaddingH, end = m.screenPaddingH, top = m.screenPaddingTop, bottom = 120.dp),
    ) {
        Box(modifier = Modifier.widthIn(max = 820.dp).fillMaxWidth().align(Alignment.CenterHorizontally)) {
            Column {
                // Breadcrumb
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Workspace", color = c.onv, fontSize = 13.sp)
                    MsIcon(DbIcons.chevronRight, 16.dp, c.onv)
                    Text(title.ifBlank { "Untitled note" }, color = c.on, fontSize = 13.sp)
                }
                Spacer(Modifier.height(20.dp))

                // Title editor
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    textStyle = TextStyle(color = c.on, fontSize = 38.sp, fontWeight = FontWeight.SemiBold),
                    cursorBrush = SolidColor(c.p),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { contentFocus.requestFocus() }),
                    decorationBox = { inner ->
                        if (title.isEmpty()) Text("Untitled", color = c.outline, fontSize = 38.sp, fontWeight = FontWeight.SemiBold)
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth().focusRequester(titleFocus),
                )
                Spacer(Modifier.height(14.dp))

                // Meta row: status (cyclable), pin, delete
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StatusChip(note.status) {
                        vm.updateNote(note.copy(title = title, content = content, status = nextStatus(note.status)))
                    }
                    Spacer(Modifier.weight(1f))
                    IconChip(DbIcons.pushPin, active = note.isPinned, activeColor = c.p, contentDescription = if (note.isPinned) "Unpin note" else "Pin note") { vm.togglePin(note.copy(title = title, content = content)) }
                    IconChip(DbIcons.delete, active = false, activeColor = c.err, contentDescription = "Delete note") { vm.deleteNote(note.id) }
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = c.outlinev)
                Spacer(Modifier.height(16.dp))

                // Content editor
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    textStyle = TextStyle(color = c.on, fontSize = 16.sp, lineHeight = 27.sp),
                    cursorBrush = SolidColor(c.p),
                    decorationBox = { inner ->
                        if (content.isEmpty()) Text("Start writing — markdown welcome…", color = c.outline, fontSize = 16.sp, lineHeight = 27.sp)
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 240.dp).focusRequester(contentFocus),
                )
                Spacer(Modifier.height(20.dp))
                Text("${wordCount(content)} words", color = c.onv, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EmptyEditor(onCreate: () -> Unit) {
    val c = DevbookTheme.colors
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(c.sCont), contentAlignment = Alignment.Center) {
            MsIcon(DbIcons.editNote, 34.dp, c.onv)
        }
        Spacer(Modifier.height(16.dp))
        Text("No note selected", color = c.on, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text("Pick a note from the sidebar, or create a new one.", color = c.onv, fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onCreate,
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        ) {
            MsIcon(DbIcons.add, 20.dp, c.op)
            Spacer(Modifier.width(8.dp))
            Text("New note", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun StatusChip(status: NoteStatus, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    AssistChip(
        onClick = onClick,
        label = { Text(statusLabel(status), fontSize = 12.5.sp, fontWeight = FontWeight.Medium) },
        leadingIcon = { Dot(statusColor(status, c.p, c.onv), 8.dp) },
        border = null,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = c.secc,
            labelColor = c.onsecc,
        ),
    )
}

@Composable
private fun IconChip(icon: androidx.compose.ui.graphics.vector.ImageVector, active: Boolean, activeColor: Color, contentDescription: String, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    MsIconButton(
        icon = icon,
        onClick = onClick,
        contentDescription = contentDescription,
        tint = if (active) activeColor else c.onv,
        buttonSize = 34.dp,
        iconSize = 20.dp,
    )
}

private fun statusLabel(s: NoteStatus): String = when (s) {
    NoteStatus.NONE -> "No status"
    NoteStatus.IN_PROGRESS -> "In progress"
    NoteStatus.COMPLETED -> "Completed"
    NoteStatus.ON_HOLD -> "On hold"
    NoteStatus.ARCHIVED -> "Archived"
}

private fun statusColor(s: NoteStatus, primary: Color, neutral: Color): Color = when (s) {
    NoteStatus.NONE -> neutral
    NoteStatus.IN_PROGRESS -> primary
    NoteStatus.COMPLETED -> Color(0xFF34D399)
    NoteStatus.ON_HOLD -> Color(0xFFFBBF24)
    NoteStatus.ARCHIVED -> neutral
}

private fun nextStatus(s: NoteStatus): NoteStatus {
    val all = NoteStatus.entries
    return all[(s.ordinal + 1) % all.size]
}

private val WhitespaceRegex = Regex("\\s+")

private fun wordCount(text: String): Int = text.split(WhitespaceRegex).count { it.isNotBlank() }
