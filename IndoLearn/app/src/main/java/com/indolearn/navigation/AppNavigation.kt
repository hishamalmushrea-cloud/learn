package com.indolearn.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.indolearn.ui.screens.*
import com.indolearn.viewmodel.MainViewModel

@Composable
fun AppNavigation(navController: NavHostController, viewModel: MainViewModel) {
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController, viewModel) }
        composable("lessons") { LessonsScreen(navController, viewModel) }
        composable("lesson/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 1
            LessonDetailScreen(navController, viewModel, id)
        }
        composable("vocabulary") { VocabularyScreen(navController, viewModel) }
        composable("grammar") { GrammarScreen(navController, viewModel) }
        composable("progress") { ProgressScreen(navController, viewModel) }
        composable("settings") { SettingsScreen(navController) }
        composable("flashcards") { FlashcardScreen(navController, viewModel) }
        composable("quiz") { QuizScreen(navController, viewModel) }
        composable("search") { SearchScreen(navController, viewModel) }
        composable("review") { ReviewScreen(navController, viewModel) }
        composable("dialogue") { DialogueScreen(navController) }
        composable("grammar_detail") { GrammarDetailScreen(navController) }
        composable("favorites") { FavoritesScreen(navController, viewModel) }
        composable("casual") { CasualScreen(navController, viewModel) }
        composable("casual_interactive") { CasualInteractiveScreen(navController, viewModel) }
        composable("curriculum") { CurriculumScreen(navController, viewModel) }
    }
}