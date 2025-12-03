package com.example.sentio.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.sentio.presentation.screen.editor.EditorScreen
import com.example.sentio.presentation.screen.home.HomeScreen

/**
 * Main navigation host for the app.
 */
@Composable
fun SentioNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home
    ) {
        composable<Screen.Home> {
            HomeScreen(
                onNoteClick = { noteId ->
                    navController.navigate(Screen.Editor(noteId))
                },
                onCreateNote = {
                    navController.navigate(Screen.Editor("new"))
                }
            )
        }

        composable<Screen.Editor> { backStackEntry ->
            val editor: Screen.Editor = backStackEntry.toRoute()
            EditorScreen(
                noteId = editor.noteId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
