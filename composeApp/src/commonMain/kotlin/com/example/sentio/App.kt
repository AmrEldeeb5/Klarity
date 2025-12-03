package com.example.sentio

import androidx.compose.runtime.Composable
import com.example.sentio.presentation.navigation.SentioNavigation
import com.example.sentio.presentation.theme.SentioTheme
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        SentioTheme {
            SentioNavigation()
        }
    }
}
