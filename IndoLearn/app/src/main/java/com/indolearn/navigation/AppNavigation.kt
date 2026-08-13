package com.indolearn.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.indolearn.ui.screens.*
import com.indolearn.viewmodel.HomeViewModel
import com.indolearn.viewmodel.LearnViewModel
import com.indolearn.viewmodel.OnboardingViewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val onboardingCompleted = onboardingViewModel.onboardingCompleted.collectAsState().value

    if (onboardingCompleted == null) {
        // Loading state
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (onboardingCompleted) "home" else "onboarding",
        enterTransition = { fadeIn(animationSpec = tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) },
        exitTransition = { fadeOut(animationSpec = tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) },
        popEnterTransition = { fadeIn(animationSpec = tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) },
        popExitTransition = { fadeOut(animationSpec = tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) }
    ) {
        composable("onboarding") {
            OnboardingScreen(navController, onboardingViewModel)
        }
        composable("home") { 
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(navController, homeViewModel) 
        }
        composable("lessons") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            LessonsScreen(navController, learnViewModel, 0) 
        }
        composable("lessons/{level}") { backStackEntry ->
            val learnViewModel: LearnViewModel = hiltViewModel()
            val level = backStackEntry.arguments?.getString("level")?.toIntOrNull() ?: 0
            learnViewModel.loadLessonsForLevel(level)
            LessonsScreen(navController, learnViewModel, level)
        }
        composable("lesson/{id}") { backStackEntry ->
            val learnViewModel: LearnViewModel = hiltViewModel()
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 1
            LessonDetailScreen(navController, learnViewModel, id)
        }
        composable("vocabulary") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            VocabularyScreen(navController, learnViewModel) 
        }
        composable("grammar") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            GrammarScreen(navController, learnViewModel) 
        }
        composable("progress") { 
            val homeViewModel: HomeViewModel = hiltViewModel()
            ProgressScreen(navController, homeViewModel) 
        }
        composable("settings") { SettingsScreen(navController) }
        composable("flashcards") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            FlashcardScreen(navController, learnViewModel) 
        }
        composable("quiz") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            QuizScreen(navController, learnViewModel) 
        }
        composable("search") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            SearchScreen(navController, learnViewModel) 
        }
        composable("review") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            ReviewScreen(navController, learnViewModel) 
        }
        composable("dialogue") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            DialogueScreen(navController, learnViewModel) 
        }
        composable("favorites") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            FavoritesScreen(navController, learnViewModel) 
        }
        composable("casual") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            CasualScreen(navController, learnViewModel) 
        }
        composable("casual_interactive") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            CasualInteractiveScreen(navController, learnViewModel) 
        }
        composable("curriculum") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            CurriculumScreen(navController, learnViewModel) 
        }
        composable("notebook") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            NotebookScreen(navController, learnViewModel) 
        }
        composable("study_guide") { 
            val learnViewModel: LearnViewModel = hiltViewModel()
            StudyGuideScreen(navController, learnViewModel) 
        }
        composable("riyada_guide") { 
            RiyadaGuideScreen(navController) 
        }
        composable("encyclopedia") { 
            EncyclopediaScreen(navController) 
        }
        // المكتبة المرجعية: موسوعة تعلّم الإندونيسية (27 ملفاً، أوفلاين)
        composable("library") {
            val learnViewModel: LearnViewModel = hiltViewModel()
            LibraryScreen(navController, learnViewModel)
        }
    }
}