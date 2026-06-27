package com.example.klarity.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.TaskStatus
import com.example.klarity.presentation.DevbookScreen
import com.example.klarity.presentation.WorkspaceViewModel
import com.example.klarity.presentation.theme.Accent
import com.example.klarity.presentation.theme.DevbookTheme
import com.example.klarity.presentation.theme.MonoFamily

private val SidebarWidth = 296.dp

@Composable
fun DevbookSidebar(
    vm: WorkspaceViewModel,
    screen: DevbookScreen,
    onSelectScreen: (DevbookScreen) -> Unit,
    dark: Boolean,
    onToggleTheme: () -> Unit,
    accent: Accent,
    onSelectAccent: (Accent) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val c = DevbookTheme.colors
    val notes by vm.activeNotes.collectAsState()
    val archivedNotes by vm.archivedNotes.collectAsState()
    val folders by vm.folders.collectAsState()
    val tasks by vm.tasks.collectAsState()
    val search by vm.search.collectAsState()
    val selected by vm.selectedNote.collectAsState()
    val pendingFolderRename by vm.pendingFolderRename.collectAsState()
    val openTasks = tasks.count { !it.completed && it.status != TaskStatus.DONE }

    // Inline-rename + expand/collapse state for the tree (Notion-style).
    var renamingFolderId by remember { mutableStateOf<String?>(null) }
    var renamingNoteId by remember { mutableStateOf<String?>(null) }
    var archivedOpen by remember { mutableStateOf(false) }
    val expanded = remember { mutableStateMapOf<String, Boolean>() }
    fun isExpanded(id: String) = expanded[id] ?: true

    // A just-created folder opens straight into its name editor.
    LaunchedEffect(pendingFolderRename) {
        pendingFolderRename?.let {
            renamingNoteId = null
            renamingFolderId = it
            vm.consumeFolderRename()
        }
    }

    val openNote: (String) -> Unit = { id ->
        vm.selectNote(id)
        onSelectScreen(DevbookScreen.NOTEBOOK)
    }
    val startNoteRename: (String) -> Unit = { id -> renamingFolderId = null; renamingNoteId = id }

    Surface(
        color = c.sLow,
        border = BorderStroke(1.dp, c.outlinev),
        modifier = Modifier
            .width(SidebarWidth)
            .fillMaxHeight(),
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 12.dp),
    ) {
        Brand(accent = accent, onSelectAccent = onSelectAccent)
        Spacer(Modifier.height(14.dp))
        SearchField(value = search, onChange = vm::setSearch)
        Spacer(Modifier.height(14.dp))

        // Primary nav
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            DevbookScreen.entries.forEach { item ->
                NavRow(
                    icon = item.navIcon,
                    label = item.navLabel,
                    badge = if (item == DevbookScreen.TASKS && openTasks > 0) openTasks else null,
                    selected = item == screen,
                    onClick = { onSelectScreen(item) },
                )
            }
        }

        // Workspace header with create menu
        WorkspaceHeader(onNewNote = { vm.createNote(); onSelectScreen(DevbookScreen.NOTEBOOK) }, onNewFolder = { vm.createFolder() })

        // Tree (scrolls)
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            if (search.isNotBlank()) {
                val results = notes.filter { it.matchesQuery(search) }
                if (results.isEmpty()) {
                    EmptyHint("No matches for \"$search\"")
                } else {
                    results.forEach { note ->
                        NoteTreeItem(
                            note = note,
                            selectedId = selected?.id,
                            indent = false,
                            renaming = renamingNoteId == note.id,
                            onOpen = { openNote(note.id) },
                            onStartRename = { startNoteRename(note.id) },
                            onCommitRename = { vm.renameNote(note.id, it); renamingNoteId = null },
                            onCancelRename = { renamingNoteId = null },
                            onDelete = { vm.deleteNote(note.id) },
                        )
                    }
                }
            } else if (folders.isEmpty() && notes.isEmpty()) {
                EmptyHint("No notes yet — tap + to create one")
            } else {
                folders.filter { it.parentId == null }.forEach { folder ->
                    if (renamingFolderId == folder.id) {
                        InlineNameEditor(
                            initial = folder.name,
                            indentStart = 8.dp,
                            leading = {
                                MsIcon(DbIcons.chevronRight, 18.dp, c.onv)
                                MsIcon(DbIcons.folder, 17.dp, c.tert)
                            },
                            onCommit = { vm.renameFolder(folder.id, it); renamingFolderId = null },
                            onCancel = { renamingFolderId = null },
                        )
                    } else {
                        FolderRow(
                            name = folder.name,
                            expanded = isExpanded(folder.id),
                            onToggle = { expanded[folder.id] = !isExpanded(folder.id) },
                            onAddNote = {
                                expanded[folder.id] = true
                                vm.createNote(folderId = folder.id)
                                onSelectScreen(DevbookScreen.NOTEBOOK)
                            },
                            onRename = { renamingNoteId = null; renamingFolderId = folder.id },
                            onDelete = { vm.deleteFolder(folder.id) },
                        )
                    }
                    if (isExpanded(folder.id)) {
                        notes.filter { it.folderId == folder.id }.forEach { note ->
                            NoteTreeItem(
                                note = note,
                                selectedId = selected?.id,
                                indent = true,
                                renaming = renamingNoteId == note.id,
                                onOpen = { openNote(note.id) },
                                onStartRename = { startNoteRename(note.id) },
                                onCommitRename = { vm.renameNote(note.id, it); renamingNoteId = null },
                                onCancelRename = { renamingNoteId = null },
                                onDelete = { vm.deleteNote(note.id) },
                            )
                        }
                    }
                }
                notes.filter { it.folderId == null }.forEach { note ->
                    NoteTreeItem(
                        note = note,
                        selectedId = selected?.id,
                        indent = false,
                        renaming = renamingNoteId == note.id,
                        onOpen = { openNote(note.id) },
                        onStartRename = { startNoteRename(note.id) },
                        onCommitRename = { vm.renameNote(note.id, it); renamingNoteId = null },
                        onCancelRename = { renamingNoteId = null },
                        onDelete = { vm.deleteNote(note.id) },
                    )
                }
            }

            // Archived notes — soft-deleted, kept out of the tree above but recoverable here.
            if (archivedNotes.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .hoverBg(RoundedCornerShape(8.dp), c.sHigh)
                        .clickable { archivedOpen = !archivedOpen }
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MsIcon(if (archivedOpen) DbIcons.expandMore else DbIcons.chevronRight, 18.dp, c.onv)
                    Text("Archived", color = c.onv, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Text("${archivedNotes.size}", color = c.onv, fontSize = 11.sp)
                }
                if (archivedOpen) {
                    archivedNotes.forEach { note ->
                        ArchivedNoteRow(
                            note = note,
                            onOpen = { openNote(note.id) },
                            onRestore = { vm.restoreNote(note.id) },
                        )
                    }
                }
            }
        }

        // Footer: theme toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .hoverBg(RoundedCornerShape(14.dp), c.sHigh)
                .clickable { onToggleTheme() }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MsIcon(if (dark) DbIcons.lightMode else DbIcons.darkMode, 20.dp, c.onv)
            Text(if (dark) "Dark theme" else "Light theme", color = c.onv, fontSize = 13.5.sp, modifier = Modifier.weight(1f))
            DevbookSwitch(checked = dark, onCheckedChange = { onToggleTheme() }, primary = c.p, onPrimary = c.op, neutralTrack = c.sHighest, outline = c.outline)
        }
        Spacer(Modifier.height(2.dp))

        // Footer: profile + settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .hoverBg(RoundedCornerShape(14.dp), c.sHigh)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Avatar("K", c.tertc, c.ontertc, 32.dp, 13.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text("Workspace", color = c.on, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("Local · on this device", color = c.onv, fontSize = 11.sp)
            }
            MsIconButton(
                DbIcons.settings,
                onClick = onOpenSettings,
                contentDescription = "Open settings",
                tint = c.onv,
                buttonSize = 34.dp,
                iconSize = 20.dp,
            )
        }
    }
    }
}

@Composable
private fun Brand(accent: Accent, onSelectAccent: (Accent) -> Unit) {
    val c = DevbookTheme.colors
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(c.p),
            contentAlignment = Alignment.Center,
        ) {
            Text("K", color = c.op, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f)) {
            // Branding kept as "Klarity" per product decision.
            Text("Klarity", color = c.on, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text("Your workspace", color = c.onv, fontSize = 12.sp)
        }
        Box {
            MsIconButton(
                DbIcons.unfoldMore,
                onClick = { menuOpen = true },
                contentDescription = "Change accent color",
                tint = c.onv,
                buttonSize = 32.dp,
                iconSize = 20.dp,
            )
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                Text(
                    "Accent",
                    color = c.onv,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
                Accent.entries.forEach { a ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Dot(a.override ?: c.p, 12.dp)
                                Text(a.label, color = if (a == accent) c.p else c.on, fontWeight = if (a == accent) FontWeight.SemiBold else FontWeight.Normal)
                            }
                        },
                        onClick = {
                            onSelectAccent(a)
                            menuOpen = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    val c = DevbookTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(color = c.on, fontSize = 14.sp),
        shape = RoundedCornerShape(22.dp),
        placeholder = { Text("Search or ask AI…", color = c.onv, fontSize = 14.sp) },
        leadingIcon = { MsIcon(DbIcons.search, 20.dp, c.onv) },
        trailingIcon = {
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(c.sLow)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text("⌘K", color = c.onv, fontSize = 12.sp, fontFamily = MonoFamily)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = c.sHigh,
            unfocusedContainerColor = c.sHigh,
            focusedTextColor = c.on,
            unfocusedTextColor = c.on,
            cursorColor = c.p,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedPlaceholderColor = c.onv,
            unfocusedPlaceholderColor = c.onv,
        ),
    )
}

@Composable
private fun WorkspaceHeader(onNewNote: () -> Unit, onNewFolder: () -> Unit) {
    val c = DevbookTheme.colors
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("WORKSPACE", color = c.onv, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
        Spacer(Modifier.weight(1f))
        Box {
            MsIconButton(
                DbIcons.add,
                onClick = { menuOpen = true },
                contentDescription = "New note or folder",
                tint = c.onv,
                buttonSize = 22.dp,
                iconSize = 18.dp,
            )
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("New note") }, onClick = { onNewNote(); menuOpen = false })
                DropdownMenuItem(text = { Text("New folder") }, onClick = { onNewFolder(); menuOpen = false })
            }
        }
    }
}

@Composable
private fun NavRow(icon: ImageVector, label: String, badge: Int?, selected: Boolean, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    val fg = if (selected) c.onsecc else c.onv
    NavigationDrawerItem(
        label = { Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
        selected = selected,
        onClick = onClick,
        icon = { MsIcon(icon, 22.dp, fg) },
        badge = badge?.let { count -> { Badge(containerColor = c.p, contentColor = c.op) { Text("$count") } } },
        shape = RoundedCornerShape(22.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = c.secc,
            unselectedContainerColor = Color.Transparent,
            selectedIconColor = c.onsecc,
            unselectedIconColor = c.onv,
            selectedTextColor = c.onsecc,
            unselectedTextColor = c.onv,
        ),
        modifier = Modifier.fillMaxWidth().height(44.dp),
    )
}

@Composable
private fun FolderRow(
    name: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddNote: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = DevbookTheme.colors
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .hoverBg(RoundedCornerShape(9.dp), c.sHigh)
            .hoverable(source)
            .clickable { onToggle() }
            .padding(start = 8.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        MsIcon(if (expanded) DbIcons.expandMore else DbIcons.chevronRight, 18.dp, c.onv)
        MsIcon(DbIcons.folder, 17.dp, c.tert)
        Text(
            name,
            color = c.onv,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (hovered || menuOpen) {
            SmallIconButton(DbIcons.add, "Add note in folder", onAddNote)
            Box {
                SmallIconButton(DbIcons.moreHoriz, "Folder actions") { menuOpen = true }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Rename") }, onClick = { menuOpen = false; onRename() })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { menuOpen = false; onDelete() })
                }
            }
        }
    }
}

/** A row in the sidebar's Archived list — opens the note, or restores it to the active workspace. */
@Composable
private fun ArchivedNoteRow(note: Note, onOpen: () -> Unit, onRestore: () -> Unit) {
    val c = DevbookTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth()
            .hoverBg(RoundedCornerShape(8.dp), c.sHigh)
            .clickable { onOpen() }
            .padding(start = 26.dp, top = 5.dp, end = 4.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MsIcon(DbIcons.description, 16.dp, c.onv)
        Text(
            note.title.ifBlank { "Untitled note" },
            color = c.onv,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        MsIconButton(
            DbIcons.refresh,
            onClick = onRestore,
            contentDescription = "Restore note",
            tint = c.p,
            buttonSize = 28.dp,
            iconSize = 16.dp,
        )
    }
}

@Composable
private fun NoteTreeItem(
    note: Note,
    selectedId: String?,
    indent: Boolean,
    renaming: Boolean,
    onOpen: () -> Unit,
    onStartRename: () -> Unit,
    onCommitRename: (String) -> Unit,
    onCancelRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = DevbookTheme.colors
    if (renaming) {
        InlineNameEditor(
            initial = note.title,
            indentStart = if (indent) NoteIndent else 12.dp,
            leading = { MsIcon(DbIcons.description, 18.dp, c.onv) },
            onCommit = onCommitRename,
            onCancel = onCancelRename,
        )
    } else {
        NoteRow(note.title, selected = note.id == selectedId, indent = indent, onClick = onOpen, onRename = onStartRename, onDelete = onDelete)
    }
}

@Composable
private fun NoteRow(
    title: String,
    selected: Boolean,
    indent: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = DevbookTheme.colors
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .hoverBg(RoundedCornerShape(9.dp), c.sHigh, base = if (selected) c.sHigh else Color.Transparent)
            .hoverable(source)
            .clickable { onClick() }
            .padding(start = if (indent) NoteIndent else 12.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        MsIcon(DbIcons.description, 18.dp, if (selected) c.p else c.onv)
        Text(
            title.ifBlank { "Untitled note" },
            color = if (selected) c.on else c.onv,
            fontSize = 13.5.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (hovered || menuOpen) {
            Box {
                SmallIconButton(DbIcons.moreHoriz, "Note actions") { menuOpen = true }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Rename") }, onClick = { menuOpen = false; onRename() })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { menuOpen = false; onDelete() })
                }
            }
        }
    }
}

/**
 * Inline name editor used for renaming folders & notes (and for naming a freshly created folder).
 * Opens focused with the text selected; commits on Enter or blur, reverts on Escape — like Notion.
 */
@Composable
private fun InlineNameEditor(
    initial: String,
    indentStart: Dp,
    leading: @Composable () -> Unit,
    onCommit: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val c = DevbookTheme.colors
    val focus = remember { FocusRequester() }
    var value by remember { mutableStateOf(TextFieldValue(initial, selection = TextRange(0, initial.length))) }
    var settled by remember { mutableStateOf(false) }
    var hasFocused by remember { mutableStateOf(false) }
    fun commit() { if (!settled) { settled = true; onCommit(value.text) } }

    LaunchedEffect(Unit) { focus.requestFocus() }

    Row(
        modifier = Modifier.fillMaxWidth().height(34.dp).padding(start = indentStart, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        leading()
        BasicTextField(
            value = value,
            onValueChange = { value = it },
            singleLine = true,
            textStyle = TextStyle(color = c.on, fontSize = 13.5.sp, fontWeight = FontWeight.Medium),
            cursorBrush = SolidColor(c.p),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { commit() }),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focus)
                .onPreviewKeyEvent { e ->
                    if (e.type == KeyEventType.KeyDown && e.key == Key.Escape) {
                        settled = true; onCancel(); true
                    } else {
                        false
                    }
                }
                .onFocusChanged {
                    if (it.isFocused) hasFocused = true
                    else if (hasFocused && !settled) commit()
                }
                .clip(RoundedCornerShape(6.dp))
                .background(c.sCont)
                .border(1.dp, c.p, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun SmallIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    Box(
        modifier = Modifier.size(24.dp).hoverBg(RoundedCornerShape(6.dp), c.sHighest).clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        MsIcon(icon, 16.dp, c.onv, contentDescription = contentDescription)
    }
}

private val NoteIndent = 30.dp

@Composable
private fun EmptyHint(text: String) {
    Text(text, color = DevbookTheme.colors.onv, fontSize = 12.5.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
}
