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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.components.DbIcons
import com.example.klarity.presentation.components.DevbookSidebar
import com.example.klarity.presentation.components.DevbookTopBar
import com.example.klarity.presentation.screen.DevbookAssistantScreen
import com.example.klarity.presentation.screen.DevbookHomeScreen
import com.example.klarity.presentation.screen.DevbookNotebookScreen
import com.example.klarity.presentation.screen.DevbookTasksScreen
import com.example.klarity.presentation.screen.SettingsDialog
import com.example.klarity.presentation.theme.Accent
import com.example.klarity.presentation.theme.DevbookAppTheme
import com.example.klarity.presentation.theme.DevbookTheme
import com.example.klarity.presentation.theme.ThemeMode
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
    ASSISTANT("Assistant", DbIcons.autoAwesome, DbIcons.autoAwesome, "Knowledge", "Assistant", DbIcons.moreHoriz, "New chat"),
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
    }
}
