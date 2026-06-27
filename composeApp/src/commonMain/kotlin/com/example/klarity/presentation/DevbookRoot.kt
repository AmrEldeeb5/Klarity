package com.example.klarity.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.components.DbIcons
import com.example.klarity.presentation.components.DevbookSidebar
import com.example.klarity.presentation.components.DevbookTopBar
import com.example.klarity.presentation.screen.AssistantFab
import com.example.klarity.presentation.screen.AssistantPanel
import com.example.klarity.presentation.screen.DevbookAssistantScreen
import com.example.klarity.presentation.screen.DevbookHomeScreen
import com.example.klarity.presentation.screen.DevbookNotebookScreen
import com.example.klarity.presentation.screen.DevbookTasksScreen
import com.example.klarity.presentation.screen.SettingsDialog
import com.example.klarity.presentation.theme.Accent
import com.example.klarity.presentation.theme.DevbookAppTheme
import com.example.klarity.presentation.theme.DevbookTheme
import com.example.klarity.presentation.theme.LocalWindowMetrics
import com.example.klarity.presentation.theme.ThemeMode
import com.example.klarity.presentation.theme.windowMetricsFor
import org.koin.compose.viewmodel.koinViewModel

/**
 * The four in-shell destinations from `Devbook.dc.html`, carrying their sidebar-nav and
 * top-app-bar presentation in one place (mirrors the design's `bars` map).
 */
enum class DevbookScreen(
    val navLabel: String,
    val navIcon: ImageVector,
    val barIcon: ImageVector,
    val barCrumb: String,
    val barTitle: String,
    val barAction: ImageVector,
    val barCta: String,
) {
    HOME("Home", DbIcons.home, DbIcons.home, "Workspace", "Home", DbIcons.tune, "New note"),
    NOTEBOOK("Notebook", DbIcons.editNote, DbIcons.editNote, "Workspace", "Notebook", DbIcons.moreHoriz, "New note"),
    TASKS("Tasks", DbIcons.viewKanban, DbIcons.viewKanban, "Workspace", "Task board", DbIcons.moreHoriz, "New task"),
    ASSISTANT("Lou", DbIcons.autoAwesome, DbIcons.autoAwesome, "Knowledge", "Lou", DbIcons.moreHoriz, "New chat"),
}

/**
 * Root of the Devbook app — owns theme / accent / current-screen state, resolves the shared
 * [WorkspaceViewModel], and applies [DevbookAppTheme] before laying out the shell.
 */
@Composable
fun DevbookRoot() {
    val vm: WorkspaceViewModel = koinViewModel()
    var screen by remember { mutableStateOf(DevbookScreen.HOME) }
    var themeMode by remember { mutableStateOf(ThemeMode.LIGHT) }
    var accent by remember { mutableStateOf(Accent.FERN) }
    var showSettings by remember { mutableStateOf(false) }

    DevbookAppTheme(themeMode = themeMode, accent = accent) {
        DevbookShell(
            vm = vm,
            screen = screen,
            onSelectScreen = { screen = it },
            dark = themeMode == ThemeMode.DARK,
            onToggleTheme = { themeMode = if (themeMode == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK },
            accent = accent,
            onSelectAccent = { accent = it },
            onOpenSettings = { showSettings = true },
        )
        if (showSettings) {
            SettingsDialog(vm = vm, onDismiss = { showSettings = false })
        }
    }
}

/**
 * Sidebar + main layout. Desktop-first: at >= 900dp the 296dp sidebar is docked; below that it
 * collapses behind a menu button and slides in as a scrim-backed drawer (keeps Android usable).
 */
@Composable
private fun DevbookShell(
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
    var drawerOpen by remember { mutableStateOf(false) }
    // The Notion-style assistant side panel — opened from the floating launcher, available on every
    // screen except the full-screen Assistant tab (which already is the chat).
    var panelOpen by remember { mutableStateOf(false) }

    // The top-bar "New …" CTA does the right thing per screen.
    val onCta: () -> Unit = {
        when (screen) {
            DevbookScreen.HOME, DevbookScreen.NOTEBOOK -> {
                vm.createNote()
                onSelectScreen(DevbookScreen.NOTEBOOK)
            }
            DevbookScreen.TASKS -> vm.createTask()
            DevbookScreen.ASSISTANT -> vm.clearChat()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(c.bg)) {
        val wide = maxWidth >= 900.dp
        val metrics = windowMetricsFor(maxWidth)
        val drawerPanelWidth = maxWidth.coerceAtMost(420.dp)

        // If the layout grows wide enough to dock the sidebar, drop any open mobile drawer.
        LaunchedEffect(wide) { if (wide) drawerOpen = false }

        CompositionLocalProvider(LocalWindowMetrics provides metrics) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (wide) {
                DevbookSidebar(
                    vm = vm,
                    screen = screen,
                    onSelectScreen = onSelectScreen,
                    dark = dark,
                    onToggleTheme = onToggleTheme,
                    accent = accent,
                    onSelectAccent = onSelectAccent,
                    onOpenSettings = onOpenSettings,
                )
            }
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                DevbookTopBar(
                    screen = screen,
                    showMenu = !wide,
                    onMenu = { drawerOpen = true },
                    onCta = onCta,
                )
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (screen) {
                        DevbookScreen.HOME -> DevbookHomeScreen(vm, onSelectScreen)
                        DevbookScreen.NOTEBOOK -> DevbookNotebookScreen(vm)
                        DevbookScreen.TASKS -> DevbookTasksScreen(vm)
                        DevbookScreen.ASSISTANT -> DevbookAssistantScreen(vm, onSelectScreen, onOpenSettings)
                    }
                }
            }
            // Docked assistant side panel on wide screens.
            if (wide && panelOpen && screen != DevbookScreen.ASSISTANT) {
                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(c.outlinev))
                AssistantPanel(
                    vm = vm,
                    onClose = { panelOpen = false },
                    onExpand = { onSelectScreen(DevbookScreen.ASSISTANT); panelOpen = false },
                    onOpenSettings = onOpenSettings,
                    onOpenNote = { vm.selectNote(it.id); onSelectScreen(DevbookScreen.NOTEBOOK) },
                    modifier = Modifier.width(400.dp).fillMaxHeight(),
                )
            }
        }

        // Narrow-screen drawer
        if (!wide) {
            AnimatedVisibility(visible = drawerOpen, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { drawerOpen = false },
                )
            }
            AnimatedVisibility(
                visible = drawerOpen,
                enter = slideInHorizontally(initialOffsetX = { -it }),
                exit = slideOutHorizontally(targetOffsetX = { -it }),
            ) {
                DevbookSidebar(
                    vm = vm,
                    screen = screen,
                    onSelectScreen = {
                        onSelectScreen(it)
                        drawerOpen = false
                    },
                    dark = dark,
                    onToggleTheme = onToggleTheme,
                    accent = accent,
                    onSelectAccent = onSelectAccent,
                    onOpenSettings = {
                        drawerOpen = false
                        onOpenSettings()
                    },
                )
            }
        }

        // Assistant side panel as a right-edge drawer on narrow screens.
        if (!wide && screen != DevbookScreen.ASSISTANT) {
            AnimatedVisibility(visible = panelOpen, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { panelOpen = false },
                )
            }
            AnimatedVisibility(
                visible = panelOpen,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                    AssistantPanel(
                        vm = vm,
                        onClose = { panelOpen = false },
                        onExpand = { onSelectScreen(DevbookScreen.ASSISTANT); panelOpen = false },
                        onOpenSettings = onOpenSettings,
                        onOpenNote = { vm.selectNote(it.id); onSelectScreen(DevbookScreen.NOTEBOOK); panelOpen = false },
                        modifier = Modifier.width(drawerPanelWidth).fillMaxHeight().shadow(16.dp),
                    )
                }
            }
        }

        // Floating launcher that opens the assistant panel.
        if (!panelOpen && screen != DevbookScreen.ASSISTANT) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                AssistantFab(onClick = { panelOpen = true }, modifier = Modifier.padding(24.dp))
            }
        }
        }
    }
}
