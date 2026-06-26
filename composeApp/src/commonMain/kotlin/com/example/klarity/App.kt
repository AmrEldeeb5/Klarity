package com.example.klarity

import androidx.compose.runtime.Composable
import com.example.klarity.presentation.DevbookRoot
import org.koin.compose.KoinContext

/**
 * App entry point — the Devbook (Material 3) UI, wired to the live data layer.
 *
 * [KoinContext] exposes the already-started Koin (desktop: `main()`, Android: [KlarityApplication])
 * to the composition so screens can resolve the [com.example.klarity.presentation.WorkspaceViewModel].
 */
@Composable
fun App() {
    KoinContext {
        DevbookRoot()
    }
}
