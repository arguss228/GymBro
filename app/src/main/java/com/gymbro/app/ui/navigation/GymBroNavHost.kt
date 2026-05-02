package com.gymbro.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymbro.app.ui.bodyrank.BodyAnalysisScreen
import com.gymbro.app.ui.bodyrank.ExerciseRanksScreen
import com.gymbro.app.ui.dashboard.DashboardScreen
import com.gymbro.app.ui.exercises.ExerciseDetailScreen
import com.gymbro.app.ui.exercises.ExercisesScreen
import com.gymbro.app.ui.planeditor.PlanEditorScreen
import com.gymbro.app.ui.plans.PlansScreen
import com.gymbro.app.ui.progress.ProgressScreen
import com.gymbro.app.ui.rank.Enter1RmScreen
import com.gymbro.app.ui.rank.StrengthRanksScreen
import com.gymbro.app.ui.settings.SettingsScreen
import com.gymbro.app.ui.splash.SplashScreen
import com.gymbro.app.ui.workout.WorkoutSessionScreen

private const val NAV_MS = 350

@Composable
fun GymBroNavHost() {
    val nav = rememberNavController()

    val slideIn: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInHorizontally(tween(NAV_MS)) { it / 3 } + fadeIn(tween(NAV_MS))
    }
    val slideOut: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutHorizontally(tween(NAV_MS)) { -it / 3 } + fadeOut(tween(NAV_MS))
    }
    val popEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInHorizontally(tween(NAV_MS)) { -it / 3 } + fadeIn(tween(NAV_MS))
    }
    val popExit: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutHorizontally(tween(NAV_MS)) { it / 3 } + fadeOut(tween(NAV_MS))
    }

    NavHost(
        navController      = nav,
        startDestination   = Screen.Splash.route,
        enterTransition    = slideIn,
        exitTransition     = slideOut,
        popEnterTransition = popEnter,
        popExitTransition  = popExit,
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onNeeds1Rm = {
                    nav.navigate(Screen.Enter1Rm.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onReady = {
                    nav.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Enter1Rm.route) {
            Enter1RmScreen(
                onDone = {
                    nav.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Enter1Rm.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onStartWorkout      = { sessionId -> nav.navigate(Screen.WorkoutSession.build(sessionId)) },
                onOpenPlans         = { nav.navigate(Screen.Plans.route) },
                onOpenProgress      = { nav.navigate(Screen.Progress.route) },
                onOpenExercises     = { nav.navigate(Screen.Exercises.route) },
                onOpenSettings      = { nav.navigate(Screen.Settings.route) },
                onOpenRanks         = { nav.navigate(Screen.StrengthRanks.route) },
                onOpenBodyAnalysis  = { nav.navigate(Screen.BodyAnalysis.route) },
                onOpenExerciseRanks = { nav.navigate(Screen.ExerciseRanks.route) },
            )
        }

        composable(Screen.StrengthRanks.route) {
            StrengthRanksScreen(onBack = { nav.popBackStack() })
        }

        composable(Screen.BodyAnalysis.route) {
            BodyAnalysisScreen(onBack = { nav.popBackStack() })
        }

        composable(Screen.ExerciseRanks.route) {
            ExerciseRanksScreen(onBack = { nav.popBackStack() })
        }

        composable(Screen.Plans.route) {
            PlansScreen(
                onBack       = { nav.popBackStack() },
                onCreatePlan = { nav.navigate(Screen.PlanEditor.build(null)) },
                onEditPlan   = { id -> nav.navigate(Screen.PlanEditor.build(id)) },
            )
        }

        composable(
            Screen.PlanEditor.route,
            arguments = listOf(navArgument("planId") {
                type = NavType.LongType
                defaultValue = -1L
            }),
        ) {
            PlanEditorScreen(onBack = { nav.popBackStack() })
        }

        composable(Screen.Exercises.route) {
            ExercisesScreen(
                onBack          = { nav.popBackStack() },
                onExerciseClick = { id -> nav.navigate(Screen.ExerciseDetail.build(id)) },
            )
        }

        composable(
            Screen.ExerciseDetail.route,
            arguments = listOf(navArgument(Screen.ExerciseDetail.ARG_EXERCISE_ID) {
                type = NavType.LongType
            }),
        ) {
            ExerciseDetailScreen(onBack = { nav.popBackStack() })
        }

        composable(
            Screen.WorkoutSession.route,
            arguments = listOf(navArgument(Screen.WorkoutSession.ARG_SESSION_ID) {
                type = NavType.LongType
            }),
        ) {
            WorkoutSessionScreen(onBack = { nav.popBackStack() })
        }

        composable(Screen.Progress.route) {
            ProgressScreen(onBack = { nav.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
    }
}