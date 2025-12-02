package com.example.sentio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.sentio.ui.screens.editor.EditorScreen
import com.example.sentio.ui.screens.home.HomeScreen

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
                    // Create note and navigate to editor
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
