package com.example.klarity.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.klarity.presentation.theme.DevbookColors
import com.example.klarity.presentation.theme.DevbookTheme

private data class BoardCol(val status: TaskStatus, val title: String, val accent: ColKind)
private enum class ColKind { OUTLINE, PRIMARY, TERTIARY, DONE }
private enum class BoardView { BOARD, LIST }

@Composable
fun DevbookTasksScreen(vm: WorkspaceViewModel) {
    val c = DevbookTheme.colors
    val tasks by vm.tasks.collectAsState()
    var editing by remember { mutableStateOf<Task?>(null) }
    var view by remember { mutableStateOf(BoardView.BOARD) }
    var priorityFilter by remember { mutableStateOf<TaskPriority?>(null) }
    var filterOpen by remember { mutableStateOf(false) }

    val columns = listOf(
        BoardCol(TaskStatus.BACKLOG, "Backlog", ColKind.OUTLINE),
        BoardCol(TaskStatus.IN_PROGRESS, "In progress", ColKind.PRIMARY),
        BoardCol(TaskStatus.IN_REVIEW, "In review", ColKind.TERTIARY),
        BoardCol(TaskStatus.DONE, "Done", ColKind.DONE),
    )
    val visible = priorityFilter?.let { p -> tasks.filter { it.priority == p } } ?: tasks

    Column(modifier = Modifier.fillMaxSize().background(c.bg).padding(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 40.dp)) {
        // Toolbar
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(c.sCont).padding(4.dp)) {
                SegItem(DbIcons.viewKanban, "Board", selected = view == BoardView.BOARD) { view = BoardView.BOARD }
                SegItem(DbIcons.viewList, "List", selected = view == BoardView.LIST) { view = BoardView.LIST }
            }
            Box {
                OutlinePill(
                    icon = DbIcons.filterList,
                    label = priorityFilter?.let { "Priority: ${it.label}" } ?: "Filter",
                    active = priorityFilter != null,
                    onClick = { filterOpen = true },
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
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    columns.forEach { col ->
                        val colTasks = visible.filter { it.status == col.status }.sortedBy { it.order }
                        BoardColumn(
                            modifier = Modifier.weight(1f),
                            col = col,
                            count = colTasks.size,
                            onAdd = { vm.createTask(status = col.status) },
                        ) {
                            colTasks.forEach { task ->
                                BoardCard(task = task, isDone = col.status == TaskStatus.DONE, onClick = { editing = task })
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

@Composable
private fun SegItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    Row(
        modifier = Modifier.height(32.dp).clip(RoundedCornerShape(16.dp)).background(if (selected) c.p else Color.Transparent).clickable { onClick() }.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        MsIcon(icon, 18.dp, if (selected) c.op else c.onv)
        Text(label, color = if (selected) c.op else c.onv, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun OutlinePill(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    val fg = if (active) c.p else c.onv
    Row(
        modifier = Modifier.height(34.dp).clip(RoundedCornerShape(18.dp)).border(1.dp, if (active) c.p else c.outline, RoundedCornerShape(18.dp)).clickable { onClick() }.padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        MsIcon(icon, 18.dp, fg)
        Text(label, color = fg, fontSize = 13.sp)
    }
}

private fun dotColorFor(col: BoardCol, c: DevbookColors): Color = when (col.accent) {
    ColKind.OUTLINE -> c.outline
    ColKind.PRIMARY -> c.p
    ColKind.TERTIARY -> c.tert
    ColKind.DONE -> c.p
}

// ── List view ────────────────────────────────────────────────────────────────

@Composable
private fun TaskListView(
    columns: List<BoardCol>,
    tasks: List<Task>,
    onOpen: (Task) -> Unit,
    onToggle: (Task) -> Unit,
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
                        if (col.accent == ColKind.DONE) MsIcon(DbIcons.checkCircle, 15.dp, c.p) else Dot(dotColorFor(col, c), 9.dp)
                        Text(col.title, color = c.on, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                        Text("${rows.size}", color = c.onv, fontSize = 12.sp)
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.sLow).border(1.dp, c.outlinev, RoundedCornerShape(16.dp)),
                    ) {
                        rows.forEachIndexed { i, task ->
                            ListRow(task = task, onClick = { onOpen(task) }, onToggle = { onToggle(task) })
                            if (i < rows.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(c.outlinev))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ListRow(task: Task, onClick: () -> Unit, onToggle: () -> Unit) {
    val c = DevbookTheme.colors
    val done = task.completed || task.status == TaskStatus.DONE
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onToggle() }.padding(2.dp)) {
            MsIcon(if (done) DbIcons.checkCircle else DbIcons.radioUnchecked, 22.dp, if (done) c.p else c.outline)
        }
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
    val dotColor = when (col.accent) {
        ColKind.OUTLINE -> c.outline
        ColKind.PRIMARY -> c.p
        ColKind.TERTIARY -> c.tert
        ColKind.DONE -> c.p
    }
    Column(modifier = modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (col.accent == ColKind.DONE) MsIcon(DbIcons.checkCircle, 15.dp, c.p) else Dot(dotColor, 9.dp)
            Text(col.title, color = c.on, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
            Text("$count", color = c.onv, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { onAdd() }.padding(2.dp)) {
                MsIcon(DbIcons.add, 18.dp, c.onv)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(2.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            cards()
        }
    }
}

@Composable
private fun BoardCard(task: Task, isDone: Boolean, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.sLow)
            .border(1.dp, if (task.priority == TaskPriority.HIGH && !isDone) c.p else c.outlinev, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        if (task.priority == TaskPriority.HIGH && !isDone) {
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
    }
}

@Composable
private fun PriorityTag(priority: TaskPriority) {
    val c = DevbookTheme.colors
    val bg = if (priority == TaskPriority.HIGH) c.errc else c.sHigh
    val fg = if (priority == TaskPriority.HIGH) c.err else c.onv
    Box(modifier = Modifier.clip(RoundedCornerShape(7.dp)).background(bg).padding(horizontal = 7.dp, vertical = 3.dp)) {
        Text(priority.label.uppercase(), color = fg, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Task editor dialog ──────────────────────────────────────────────────────

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
        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(c.sLowest)
                .border(1.dp, c.outlinev, RoundedCornerShape(24.dp))
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskPriority.entries.forEach { p ->
                    ChoiceChip(label = p.label, selected = p == priority, selectedColor = Color(p.color)) { priority = p }
                }
            }
            Spacer(Modifier.height(16.dp))

            FieldLabel("Status")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(TaskStatus.BACKLOG to "Backlog", TaskStatus.IN_PROGRESS to "In progress", TaskStatus.IN_REVIEW to "In review", TaskStatus.DONE to "Done").forEach { (s, label) ->
                    ChoiceChip(label = label, selected = s == status, selectedColor = c.p) { status = s }
                }
            }
            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { onDelete() }.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    MsIcon(DbIcons.delete, 18.dp, c.err)
                    Text("Delete", color = c.err, fontSize = 13.sp)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Cancel",
                    color = c.onv,
                    fontSize = 14.sp,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable { onDismiss() }.padding(horizontal = 16.dp, vertical = 9.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Save",
                    color = c.op,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(c.p).clickable {
                        onSave(task.copy(title = title.trim().ifBlank { "Untitled task" }, description = description.trim(), priority = priority, status = status))
                    }.padding(horizontal = 18.dp, vertical = 9.dp),
                )
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
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(c.sLow).border(1.dp, c.outlinev, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        if (value.isEmpty()) Text(placeholder, color = c.outline, fontSize = 14.sp)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = singleLine,
            textStyle = TextStyle(color = c.on, fontSize = 14.sp, lineHeight = 20.sp),
            cursorBrush = SolidColor(c.p),
            modifier = Modifier.fillMaxWidth().then(if (minHeight > 0.dp) Modifier.height(minHeight) else Modifier),
        )
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, selectedColor: Color, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) selectedColor else Color.Transparent)
            .border(1.dp, if (selected) selectedColor else c.outline, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(label, color = if (selected) Color.White else c.onv, fontSize = 12.5.sp, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal)
    }
}
