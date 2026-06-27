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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.NoteStatus
import com.example.klarity.presentation.WorkspaceViewModel
import com.example.klarity.presentation.components.DbIcons
import com.example.klarity.presentation.components.Dot
import com.example.klarity.presentation.components.MarkdownText
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
    // Local edit buffers, re-initialised when switching notes. Content is a TextFieldValue so the
    // slash menu can read the caret and insert block markdown at the right offset.
    var title by remember(note.id) { mutableStateOf(note.title) }
    var content by remember(note.id) { mutableStateOf(TextFieldValue(note.content)) }
    // Slash-command menu: anchor = index of the active "/", layout = caret geometry for the popup.
    var slashAnchor by remember(note.id) { mutableStateOf<Int?>(null) }
    var prevLen by remember(note.id) { mutableStateOf(note.content.length) }
    var contentLayout by remember(note.id) { mutableStateOf<TextLayoutResult?>(null) }
    var readMode by remember(note.id) { mutableStateOf(false) }

    // Debounced autosave: persist ~500ms after the last keystroke, merged onto the freshest
    // version of this note (so a meanwhile pin/status change isn't clobbered). Switching notes
    // cancels the pending save — the buffer is re-seeded from the new note, so no cross-write.
    val latestNote by rememberUpdatedState(note)
    LaunchedEffect(note.id) {
        snapshotFlow { title to content.text }
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
            if (n.id == note.id && (title != n.title || content.text != n.content)) {
                vm.updateNote(n.copy(title = title, content = content.text))
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
                        vm.updateNote(note.copy(title = title, content = content.text, status = nextStatus(note.status)))
                    }
                    Spacer(Modifier.weight(1f))
                    IconChip(if (readMode) DbIcons.edit else DbIcons.visibility, active = readMode, activeColor = c.p, contentDescription = if (readMode) "Edit note" else "Preview note") {
                        if (!readMode) slashAnchor = null
                        readMode = !readMode
                    }
                    IconChip(DbIcons.pushPin, active = note.isPinned, activeColor = c.p, contentDescription = if (note.isPinned) "Unpin note" else "Pin note") { vm.togglePin(note.copy(title = title, content = content.text)) }
                    IconChip(DbIcons.delete, active = false, activeColor = c.err, contentDescription = "Delete note") { vm.deleteNote(note.id) }
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = c.outlinev)
                Spacer(Modifier.height(16.dp))

                // Content: a rendered read view, or an editor with the Notion-style "/" slash menu.
                if (readMode) {
                    if (content.text.isBlank()) {
                        Text("Nothing here yet — switch to edit and start writing.", color = c.outline, fontSize = 16.sp, lineHeight = 27.sp)
                    } else {
                        MarkdownText(
                            content.text,
                            color = c.on,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 16.sp,
                            lineHeight = 27.sp,
                            onToggleTodo = { i -> content = TextFieldValue(toggleTodo(content.text, i)) },
                        )
                    }
                } else {
                val density = LocalDensity.current
                val slashQuery = slashQueryFor(content, slashAnchor)
                val slashMatches = remember(slashQuery) {
                    if (slashQuery == null) emptyList() else SlashItems.filter { it.matches(slashQuery) }
                }
                Box {
                    BasicTextField(
                        value = content,
                        onValueChange = { nv ->
                            val grew = nv.text.length == prevLen + 1
                            prevLen = nv.text.length
                            content = nv
                            when {
                                // An open menu closes once the trigger is no longer valid (space typed,
                                // "/" deleted, caret moved away).
                                slashAnchor != null && slashQueryFor(nv, slashAnchor) == null -> slashAnchor = null
                                // A "/" typed at the start of a line opens the menu.
                                slashAnchor == null && grew && nv.selection.collapsed -> {
                                    val cur = nv.selection.end
                                    if (cur in 1..nv.text.length && nv.text[cur - 1] == '/' && (cur - 1 == 0 || nv.text[cur - 2] == '\n')) {
                                        slashAnchor = cur - 1
                                    }
                                }
                            }
                        },
                        onTextLayout = { contentLayout = it },
                        textStyle = TextStyle(color = c.on, fontSize = 16.sp, lineHeight = 27.sp),
                        cursorBrush = SolidColor(c.p),
                        decorationBox = { inner ->
                            if (content.text.isEmpty()) Text("Start writing — type / for blocks…", color = c.outline, fontSize = 16.sp, lineHeight = 27.sp)
                            inner()
                        },
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 240.dp).focusRequester(contentFocus),
                    )
                    if (slashAnchor != null && slashMatches.isNotEmpty()) {
                        val caret = contentLayout?.let { tl ->
                            runCatching { tl.getCursorRect(slashAnchor!!.coerceIn(0, content.text.length)) }.getOrNull()
                        }
                        val menuOffset = caret?.let { with(density) { DpOffset(it.left.toDp(), it.bottom.toDp()) } } ?: DpOffset(0.dp, 0.dp)
                        DropdownMenu(
                            expanded = true,
                            onDismissRequest = { slashAnchor = null },
                            offset = menuOffset,
                            // Non-focusable so keystrokes keep flowing into the field to filter the menu.
                            properties = PopupProperties(focusable = false),
                        ) {
                            slashMatches.forEach { item ->
                                DropdownMenuItem(
                                    leadingIcon = { MsIcon(item.icon, 18.dp, c.onv) },
                                    text = { Text(item.label, fontSize = 14.sp) },
                                    onClick = {
                                        val applied = applySlash(content, slashAnchor!!, item)
                                        content = applied
                                        prevLen = applied.text.length
                                        slashAnchor = null
                                    },
                                )
                            }
                        }
                    }
                }
                }
                Spacer(Modifier.height(20.dp))
                Text("${wordCount(content.text)} words", color = c.onv, fontSize = 12.sp)
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

// ── Slash command menu ──────────────────────────────────────────────────────────
// The note body stays a Markdown string; the slash menu just inserts the right block syntax at the
// caret, so search / Lou / preview keep reading it as before. [cursorOffset] is where the caret lands
// after insertion, measured from the start of [insert].

private data class SlashItem(
    val label: String,
    val keywords: String,
    val icon: ImageVector,
    val insert: String,
    val cursorOffset: Int,
) {
    fun matches(q: String): Boolean = q.isBlank() || label.contains(q, ignoreCase = true) || keywords.contains(q, ignoreCase = true)
}

private val SlashItems = listOf(
    SlashItem("Heading 1", "h1 title", DbIcons.title, "# ", 2),
    SlashItem("Heading 2", "h2 subtitle", DbIcons.title, "## ", 3),
    SlashItem("Heading 3", "h3", DbIcons.title, "### ", 4),
    SlashItem("Bullet list", "ul unordered bullet point", DbIcons.formatBulleted, "- ", 2),
    SlashItem("Numbered list", "ol ordered number", DbIcons.formatNumbered, "1. ", 3),
    SlashItem("To-do", "todo task checkbox check", DbIcons.checklist, "- [ ] ", 6),
    SlashItem("Quote", "quote blockquote", DbIcons.formatQuote, "> ", 2),
    SlashItem("Code", "code snippet block", DbIcons.code, "```\n\n```", 4),
    SlashItem("Divider", "divider hr rule line", DbIcons.horizontalRule, "---\n", 4),
)

/** The text typed after the active "/" (for filtering), or null when there's no live slash trigger. */
private fun slashQueryFor(value: TextFieldValue, anchor: Int?): String? {
    if (anchor == null || !value.selection.collapsed) return null
    val t = value.text
    val cur = value.selection.end
    if (anchor >= t.length || t[anchor] != '/' || cur < anchor + 1) return null
    val q = t.substring(anchor + 1, cur)
    return if (q.any { it == ' ' || it == '\n' }) null else q
}

/** Replaces the active "/" (plus any typed filter) with [item]'s block markdown and repositions the caret. */
private fun applySlash(value: TextFieldValue, anchor: Int, item: SlashItem): TextFieldValue {
    val t = value.text
    val cur = value.selection.end.coerceAtLeast(anchor)
    val newText = t.substring(0, anchor) + item.insert + t.substring(cur)
    val caret = (anchor + item.cursorOffset).coerceIn(0, newText.length)
    return TextFieldValue(newText, TextRange(caret))
}

private val TodoLineRegex = Regex("""^(\s*[-*]\s+)\[([ xX])\](.*)$""")

/** Flips the [index]-th `- [ ]`/`- [x]` line in [content] (matching MarkdownText's to-do ordering). */
private fun toggleTodo(content: String, index: Int): String {
    var seen = -1
    return content.split("\n").joinToString("\n") { line ->
        val m = TodoLineRegex.find(line) ?: return@joinToString line
        seen++
        if (seen != index) line
        else m.groupValues[1] + (if (m.groupValues[2].isNotBlank()) "[ ]" else "[x]") + m.groupValues[3]
    }
}
