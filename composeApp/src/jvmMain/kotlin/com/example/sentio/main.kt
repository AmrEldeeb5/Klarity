package com.example.sentio

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.sentio.di.appModule
import org.koin.core.context.startKoin

fun main() = application {
    // Initialize Koin
    startKoin {
        modules(appModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Sentio - The Developer Operating System",
    ) {
        App()
    }
}