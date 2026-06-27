package com.example.klarity.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.klarity.domain.models.Task
import com.example.klarity.domain.models.TaskPriority
import com.example.klarity.domain.models.TaskStatus
import com.example.klarity.presentation.WorkspaceViewModel
import com.example.klarity.presentation.components.DbIcons
import com.example.klarity.presentation.components.Dot
import com.example.klarity.presentation.components.MsIcon
import com.example.klarity.presentation.components.MsIconButton
import com.example.klarity.presentation.components.StatusPill
import com.example.klarity.presentation.components.statusStyle
import com.example.klarity.presentation.theme.DevbookTheme
import com.example.klarity.presentation.theme.LocalWindowMetrics

private data class BoardCol(val status: TaskStatus, val title: String)
private enum class BoardView { BOARD, LIST }

/**
 * The board's columns in flow order. Single source of truth for every status selector
 * (column header, card chip, editor) so they can never drift out of sync.
 */
private val BoardStatuses: List<Pair<TaskStatus, String>> = listOf(
    TaskStatus.BACKLOG to "Backlog",
    TaskStatus.IN_PROGRESS to "In progress",
    TaskStatus.IN_REVIEW to "In review",
    TaskStatus.DONE to "Done",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevbookTasksScreen(vm: WorkspaceViewModel) {
    val c = DevbookTheme.colors
    val tasks by vm.tasks.collectAsState()
    var editing by remember { mutableStateOf<Task?>(null) }
    var view by remember { mutableStateOf(BoardView.BOARD) }
    var priorityFilter by remember { mutableStateOf<TaskPriority?>(null) }
    var filterOpen by remember { mutableStateOf(false) }

    val columns = BoardStatuses.map { (status, title) -> BoardCol(status, title) }
    val visible = priorityFilter?.let { p -> tasks.filter { it.priority == p } } ?: tasks
    val m = LocalWindowMetrics.current
    val compact = m.isCompact
    val boardScroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().background(c.bg).padding(start = m.screenPaddingH, end = m.screenPaddingH, top = m.screenPaddingTop, bottom = 40.dp)) {
        // Toolbar
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            val segColors = SegmentedButtonDefaults.colors(
                activeContainerColor = c.p,
                activeContentColor = c.op,
                activeBorderColor = Color.Transparent,
                inactiveContainerColor = c.sCont,
                inactiveContentColor = c.onv,
                inactiveBorderColor = Color.Transparent,
            )
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = view == BoardView.BOARD,
                    onClick = { view = BoardView.BOARD },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = segColors,
                    icon = { MsIcon(DbIcons.viewKanban, 18.dp, if (view == BoardView.BOARD) c.op else c.onv) },
                    label = { Text("Board", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                )
                SegmentedButton(
                    selected = view == BoardView.LIST,
                    onClick = { view = BoardView.LIST },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = segColors,
                    icon = { MsIcon(DbIcons.viewList, 18.dp, if (view == BoardView.LIST) c.op else c.onv) },
                    label = { Text("List", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                )
            }
            Box {
                val filterActive = priorityFilter != null
                val filterFg = if (filterActive) c.p else c.onv
                AssistChip(
                    onClick = { filterOpen = true },
                    label = { Text(priorityFilter?.let { "Priority: ${it.label}" } ?: "Filter", fontSize = 13.sp) },
                    leadingIcon = { MsIcon(DbIcons.filterList, 18.dp, filterFg) },
                    colors = AssistChipDefaults.assistChipColors(labelColor = filterFg, leadingIconContentColor = filterFg),
                    border = BorderStroke(1.dp, if (filterActive) c.p else c.outline),
                )
                DropdownMenu(expanded = filterOpen, onDismissRequest = { filterOpen = false }) {
                    DropdownMenuItem(text = { Text("All priorities") }, onClick = { priorityFilter = null; filterOpen = false })
                    TaskPriority.entries.forEach { p ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Dot(Color(p.color), 10.dp)
                                    Text(p.label)
                                }
                            },
                            onClick = { priorityFilter = p; filterOpen = false },
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Text("${visible.size} task${if (visible.size == 1) "" else "s"}", color = c.onv, fontSize = 13.sp)
        }
        Spacer(Modifier.height(20.dp))

        when (view) {
            BoardView.BOARD ->
                // On phones the four columns won't fit side-by-side, so give each a usable fixed
                // width and let the board scroll horizontally instead of crushing every column.
                Row(
                    modifier = if (compact) Modifier.fillMaxSize().horizontalScroll(boardScroll) else Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 16.dp),
                ) {
                    columns.forEach { col ->
                        val colTasks = visible.filter { it.status == col.status }.sortedBy { it.order }
                        BoardColumn(
                            modifier = if (compact) Modifier.width(300.dp) else Modifier.weight(1f),
                            col = col,
                            count = colTasks.size,
                            onAdd = { vm.createTask(status = col.status) },
                        ) {
                            colTasks.forEach { task ->
                                BoardCard(task = task, isDone = col.status == TaskStatus.DONE, onClick = { editing = task }, onMove = { vm.moveTask(task, it) })
                            }
                        }
                    }
                }
            BoardView.LIST ->
                TaskListView(
                    columns = columns,
                    tasks = visible,
                    onOpen = { editing = it },
                    onToggle = { vm.toggleTaskComplete(it) },
                    onMove = { task, status -> vm.moveTask(task, status) },
                )
        }
    }

    editing?.let { task ->
        TaskEditorDialog(
            task = task,
            onDismiss = { editing = null },
            onSave = { vm.updateTask(it); editing = null },
            onDelete = { vm.deleteTask(task.id); editing = null },
        )
    }
}

// ── Status selectors ──────────────────────────────────────────────────────────
// The status palette + StatusPill live in components/StatusChip.kt so Home can reuse them.

/**
 * The status pill, made tappable: opens a dropdown of every board status so a task can be moved
 * between columns without drag — the Material 3 way to reassign a "select" property.
 */
@Composable
private fun StatusMenuChip(current: TaskStatus, onSelect: (TaskStatus) -> Unit) {
    val st = statusStyle(current)
    val label = BoardStatuses.firstOrNull { it.first == current }?.second ?: current.label
    var open by remember { mutableStateOf(false) }
    Box {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = st.container,
            contentColor = st.onContainer,
            modifier = Modifier.clickable { open = true },
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp, end = 5.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Dot(st.dot, 7.dp)
                Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                MsIcon(DbIcons.expandMore, 14.dp, st.onContainer)
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            BoardStatuses.forEach { (s, lbl) ->
                val ds = statusStyle(s)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Dot(ds.dot, 9.dp)
                            Text(lbl, color = DevbookTheme.colors.on, fontWeight = if (s == current) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    },
                    trailingIcon = if (s == current) ({ MsIcon(DbIcons.checkCircle, 16.dp, ds.dot) }) else null,
                    onClick = {
                        if (s != current) onSelect(s)
                        open = false
                    },
                )
            }
        }
    }
}

/** Editor status selector chip — the FilterChip, tinted in the status's own tonal colour. */
@Composable
private fun StatusChoiceChip(status: TaskStatus, label: String, selected: Boolean, onClick: () -> Unit) {
    val st = statusStyle(status)
    FilterChip(
        selected = selected,
        onClick = onClick,
        leadingIcon = { Dot(st.dot, 9.dp) },
        label = { Text(label, fontSize = 12.5.sp, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal) },
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            labelColor = DevbookTheme.colors.onv,
            selectedContainerColor = st.container,
            selectedLabelColor = st.onContainer,
        ),
    )
}

// ── List view ────────────────────────────────────────────────────────────────

@Composable
private fun TaskListView(
    columns: List<BoardCol>,
    tasks: List<Task>,
    onOpen: (Task) -> Unit,
    onToggle: (Task) -> Unit,
    onMove: (Task, TaskStatus) -> Unit,
) {
    val c = DevbookTheme.colors
    if (tasks.isEmpty()) {
        Text("No tasks match this filter.", color = c.onv, fontSize = 14.sp)
        return
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        columns.forEach { col ->
            val rows = tasks.filter { it.status == col.status }.sortedBy { it.order }
            if (rows.isNotEmpty()) {
                Column {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StatusPill(status = col.status, label = col.title)
                        Text("${rows.size}", color = c.onv, fontSize = 12.sp)
                    }
                    OutlinedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = c.sLow),
                        border = BorderStroke(1.dp, c.outlinev),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        rows.forEachIndexed { i, task ->
                            ListRow(task = task, onClick = { onOpen(task) }, onToggle = { onToggle(task) }, onMove = { onMove(task, it) })
                            if (i < rows.lastIndex) HorizontalDivider(color = c.outlinev)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ListRow(task: Task, onClick: () -> Unit, onToggle: () -> Unit, onMove: (TaskStatus) -> Unit) {
    val c = DevbookTheme.colors
    val done = task.completed || task.status == TaskStatus.DONE
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MsIconButton(
            icon = if (done) DbIcons.checkCircle else DbIcons.radioUnchecked,
            onClick = onToggle,
            contentDescription = if (done) "Mark task not done" else "Mark task done",
            tint = if (done) c.p else c.outline,
            buttonSize = 32.dp,
            iconSize = 22.dp,
        )
        Text(
            task.title.ifBlank { "Untitled task" },
            color = c.on,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            textDecoration = if (done) TextDecoration.LineThrough else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (task.priority != TaskPriority.NONE) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Dot(Color(task.priority.color), 8.dp)
                Text(task.priority.label, color = c.onv, fontSize = 12.sp)
            }
        }
        StatusMenuChip(current = task.status, onSelect = onMove)
    }
}

@Composable
private fun BoardColumn(
    modifier: Modifier,
    col: BoardCol,
    count: Int,
    onAdd: () -> Unit,
    cards: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val c = DevbookTheme.colors
    Column(modifier = modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusPill(status = col.status, label = col.title)
            Text("$count", color = c.onv, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            MsIconButton(
                icon = DbIcons.add,
                onClick = onAdd,
                contentDescription = "Add task to ${col.title}",
                buttonSize = 28.dp,
                iconSize = 18.dp,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(2.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            cards()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoardCard(task: Task, isDone: Boolean, onClick: () -> Unit, onMove: (TaskStatus) -> Unit) {
    val c = DevbookTheme.colors
    val highlight = task.priority == TaskPriority.HIGH && !isDone
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = c.sLow),
        border = BorderStroke(1.dp, if (highlight) c.p else c.outlinev),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (highlight) {
                PriorityTag(task.priority)
                Spacer(Modifier.height(8.dp))
            }
            Text(
                task.title.ifBlank { "Untitled task" },
                color = c.on,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                textDecoration = if (isDone) TextDecoration.LineThrough else null,
            )
            if (task.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(task.description, color = c.onv, fontSize = 12.5.sp, lineHeight = 17.sp, maxLines = 2)
            }
            Spacer(Modifier.height(12.dp))
            StatusMenuChip(current = task.status, onSelect = onMove)
        }
    }
}

@Composable
private fun PriorityTag(priority: TaskPriority) {
    val c = DevbookTheme.colors
    val bg = if (priority == TaskPriority.HIGH) c.errc else c.sHigh
    val fg = if (priority == TaskPriority.HIGH) c.err else c.onv
    Surface(color = bg, contentColor = fg, shape = RoundedCornerShape(7.dp)) {
        Text(
            priority.label.uppercase(),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
        )
    }
}

// ── Task editor dialog ──────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskEditorDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit,
    onDelete: () -> Unit,
) {
    val c = DevbookTheme.colors
    var title by remember(task.id) { mutableStateOf(task.title) }
    var description by remember(task.id) { mutableStateOf(task.description) }
    var priority by remember(task.id) { mutableStateOf(task.priority) }
    var status by remember(task.id) { mutableStateOf(task.status) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = c.sLowest,
            border = BorderStroke(1.dp, c.outlinev),
            modifier = Modifier.imePadding().widthIn(max = 460.dp).fillMaxWidth(),
        ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Text("Edit task", color = c.on, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))

            FieldLabel("Title")
            EditorField(value = title, onChange = { title = it }, placeholder = "Task title", singleLine = true)
            Spacer(Modifier.height(14.dp))

            FieldLabel("Description")
            EditorField(value = description, onChange = { description = it }, placeholder = "Add details…", minHeight = 80.dp)
            Spacer(Modifier.height(16.dp))

            FieldLabel("Priority")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TaskPriority.entries.forEach { p ->
                    ChoiceChip(label = p.label, selected = p == priority, selectedColor = Color(p.color)) { priority = p }
                }
            }
            Spacer(Modifier.height(16.dp))

            FieldLabel("Status")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BoardStatuses.forEach { (s, label) ->
                    StatusChoiceChip(status = s, label = label, selected = s == status) { status = s }
                }
            }
            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = c.err),
                ) {
                    MsIcon(DbIcons.delete, 18.dp, c.err)
                    Spacer(Modifier.width(6.dp))
                    Text("Delete", fontSize = 13.sp)
                }
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = c.onv),
                ) {
                    Text("Cancel", fontSize = 14.sp)
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        onSave(task.copy(title = title.trim().ifBlank { "Untitled task" }, description = description.trim(), priority = priority, status = status))
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 9.dp),
                ) {
                    Text("Save", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, color = DevbookTheme.colors.onv, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
}

@Composable
private fun EditorField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = false,
    minHeight: androidx.compose.ui.unit.Dp = 0.dp,
) {
    val c = DevbookTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        singleLine = singleLine,
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        textStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = c.sLow,
            unfocusedContainerColor = c.sLow,
            focusedTextColor = c.on,
            unfocusedTextColor = c.on,
            cursorColor = c.p,
            focusedBorderColor = c.p,
            unfocusedBorderColor = c.outlinev,
            focusedPlaceholderColor = c.outline,
            unfocusedPlaceholderColor = c.outline,
        ),
        modifier = Modifier.fillMaxWidth().then(if (minHeight > 0.dp) Modifier.heightIn(min = minHeight) else Modifier),
    )
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, selectedColor: Color, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.5.sp, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal) },
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            labelColor = c.onv,
            selectedContainerColor = selectedColor,
            selectedLabelColor = onChipColor(selectedColor),
        ),
    )
}

/** Legible foreground for a filled chip — dark text on light fills (e.g. yellow), white otherwise. */
private fun onChipColor(bg: Color): Color = if (bg.luminance() > 0.55f) Color(0xFF1A1A1A) else Color.White
