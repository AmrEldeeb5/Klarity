package com.example.klarity.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
) {
    val c = DevbookTheme.colors
    val notes by vm.notes.collectAsState()
    val folders by vm.folders.collectAsState()
    val tasks by vm.tasks.collectAsState()
    val search by vm.search.collectAsState()
    val selected by vm.selectedNote.collectAsState()
    val openTasks = tasks.count { !it.completed && it.status != TaskStatus.DONE }

    val openNote: (String) -> Unit = { id ->
        vm.selectNote(id)
        onSelectScreen(DevbookScreen.NOTEBOOK)
    }

    Column(
        modifier = Modifier
            .width(SidebarWidth)
            .fillMaxHeight()
            .background(c.sLow)
            .border(width = 1.dp, color = c.outlinev)
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
                        NoteRow(note.title, selected = note.id == selected?.id, indent = false) { openNote(note.id) }
                    }
                }
            } else if (folders.isEmpty() && notes.isEmpty()) {
                EmptyHint("No notes yet — tap + to create one")
            } else {
                folders.filter { it.parentId == null }.forEach { folder ->
                    FolderRow(folder.name)
                    notes.filter { it.folderId == folder.id }.forEach { note ->
                        NoteRow(note.title, selected = note.id == selected?.id, indent = true) { openNote(note.id) }
                    }
                }
                notes.filter { it.folderId == null }.forEach { note ->
                    NoteRow(note.title, selected = note.id == selected?.id, indent = false) { openNote(note.id) }
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
            DevbookSwitch(dark = dark, primary = c.p, neutralTrack = c.sHighest, outline = c.outline, onPrimary = c.op)
        }
        Spacer(Modifier.height(2.dp))

        // Footer: profile
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
            MsIcon(DbIcons.settings, 20.dp, c.onv)
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
            MsIcon(DbIcons.menuBook, 24.dp, c.op)
        }
        Column(modifier = Modifier.weight(1f)) {
            // Branding kept as "Klarity" per product decision.
            Text("Klarity", color = c.on, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text("Your workspace", color = c.onv, fontSize = 12.sp)
        }
        Box {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .hoverBg(CircleShape, c.sHigh)
                    .clickable { menuOpen = true },
                contentAlignment = Alignment.Center,
            ) {
                MsIcon(DbIcons.unfoldMore, 20.dp, c.onv)
            }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(c.sHigh)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MsIcon(DbIcons.search, 20.dp, c.onv)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text("Search or ask AI…", color = c.onv, fontSize = 14.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = c.on, fontSize = 14.sp),
                cursorBrush = SolidColor(c.p),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(c.sLow).padding(horizontal = 6.dp, vertical = 2.dp)) {
            Text("⌘K", color = c.onv, fontSize = 12.sp, fontFamily = MonoFamily)
        }
    }
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
            Box(modifier = Modifier.size(22.dp).hoverBg(CircleShape, c.sHigh).clickable { menuOpen = true }, contentAlignment = Alignment.Center) {
                MsIcon(DbIcons.add, 18.dp, c.onv)
            }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .hoverBg(RoundedCornerShape(22.dp), c.sHigh, base = if (selected) c.secc else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        MsIcon(icon, 22.dp, fg)
        Text(label, color = fg, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        if (badge != null) {
            Box(
                modifier = Modifier.widthIn(min = 22.dp).height(20.dp).clip(RoundedCornerShape(10.dp)).background(c.p).padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("$badge", color = c.op, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun FolderRow(name: String) {
    val c = DevbookTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().height(34.dp).padding(start = 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        MsIcon(DbIcons.expandMore, 18.dp, c.onv)
        MsIcon(DbIcons.folder, 18.dp, c.tert)
        Text(name, color = c.onv, fontSize = 13.5.sp)
    }
}

@Composable
private fun NoteRow(title: String, selected: Boolean, indent: Boolean, onClick: () -> Unit) {
    val c = DevbookTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .hoverBg(RoundedCornerShape(9.dp), c.sHigh, base = if (selected) c.sHigh else Color.Transparent)
            .clickable { onClick() }
            .padding(start = if (indent) 32.dp else 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        MsIcon(DbIcons.description, 18.dp, if (selected) c.p else c.onv)
        Text(
            title.ifBlank { "Untitled note" },
            color = if (selected) c.on else c.onv,
            fontSize = 13.5.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(text, color = DevbookTheme.colors.onv, fontSize = 12.5.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
}
