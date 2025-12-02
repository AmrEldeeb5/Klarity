package com.example.sentio

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.sentio.ui.SentioApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sentio",
    ) {
        SentioApp()
    }
}