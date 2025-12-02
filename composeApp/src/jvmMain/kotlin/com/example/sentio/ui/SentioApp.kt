package com.example.sentio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentio.ui.theme.SentioColors

@Composable
fun SentioApp() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SentioColors.BgPrimary
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Sidebar
            Sidebar(
                currentScreen = currentScreen,
                onScreenChange = { currentScreen = it }
            )

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (currentScreen) {
                    Screen.Home -> HomeScreen()
                    Screen.Notes -> NotesScreen()
                    Screen.Tasks -> TasksScreen()
                    Screen.Settings -> SettingsScreen()
                }
            }
        }
    }
}

@Composable
private fun Sidebar(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(SentioColors.BgSecondary)
            .padding(20.dp)
    ) {
        // Logo
        Text(
            text = "SENTIO",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                letterSpacing = 1.sp
            ),
            color = SentioColors.AccentPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Developer OS",
            style = MaterialTheme.typography.bodySmall,
            color = SentioColors.TextTertiary,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // AI Toolbelt section
        Text(
            text = "✨ AI TOOLBELT",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = SentioColors.TextTertiary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Navigation items
        SidebarButton(
            icon = "🏠",
            text = "Home",
            isActive = currentScreen == Screen.Home,
            onClick = { onScreenChange(Screen.Home) }
        )
        SidebarButton(
            icon = "📝",
            text = "Notes",
            isActive = currentScreen == Screen.Notes,
            onClick = { onScreenChange(Screen.Notes) }
        )
        SidebarButton(
            icon = "✅",
            text = "Tasks",
            isActive = currentScreen == Screen.Tasks,
            onClick = { onScreenChange(Screen.Tasks) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "⚙️ SYSTEM",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = SentioColors.TextTertiary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SidebarButton(
            icon = "🔍",
            text = "Search",
            isActive = false,
            onClick = { /* TODO */ }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bottom items
        SidebarButton(
            icon = "⚙️",
            text = "Settings",
            isActive = currentScreen == Screen.Settings,
            onClick = { onScreenChange(Screen.Settings) }
        )
    }
}

@Composable
private fun SidebarButton(
    icon: String,
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isActive) SentioColors.CardBgActive
                else androidx.compose.ui.graphics.Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isActive) SentioColors.TextPrimary else SentioColors.TextSecondary
        )
    }
}

// Screen composables
@Composable
private fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🚀",
                fontSize = 96.sp
            )
            Text(
                text = "Welcome to Sentio",
                style = MaterialTheme.typography.displayMedium,
                color = SentioColors.TextPrimary
            )
            Text(
                text = "Your Developer Operating System",
                style = MaterialTheme.typography.bodyLarge,
                color = SentioColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Press ⌘K to open command palette",
                style = MaterialTheme.typography.bodyMedium,
                color = SentioColors.TextTertiary
            )
        }
    }
}

@Composable
private fun NotesScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📝",
                fontSize = 72.sp
            )
            Text(
                text = "Notes Screen",
                style = MaterialTheme.typography.headlineMedium,
                color = SentioColors.TextPrimary
            )
            Text(
                text = "Coming soon...",
                style = MaterialTheme.typography.bodyMedium,
                color = SentioColors.TextSecondary
            )
        }
    }
}

@Composable
private fun TasksScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "✅",
                fontSize = 72.sp
            )
            Text(
                text = "Tasks Screen",
                style = MaterialTheme.typography.headlineMedium,
                color = SentioColors.TextPrimary
            )
            Text(
                text = "Coming soon...",
                style = MaterialTheme.typography.bodyMedium,
                color = SentioColors.TextSecondary
            )
        }
    }
}

@Composable
private fun SettingsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⚙️",
                fontSize = 72.sp
            )
            Text(
                text = "Settings Screen",
                style = MaterialTheme.typography.headlineMedium,
                color = SentioColors.TextPrimary
            )
            Text(
                text = "Coming soon...",
                style = MaterialTheme.typography.bodyMedium,
                color = SentioColors.TextSecondary
            )
        }
    }
}

enum class Screen {
    Home,
    Notes,
    Tasks,
    Settings
}