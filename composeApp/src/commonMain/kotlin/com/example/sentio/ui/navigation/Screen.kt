package com.example.sentio.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Home : Screen

    @Serializable
    data class Editor(val noteId: String) : Screen

    @Serializable
    data object Settings : Screen
}
