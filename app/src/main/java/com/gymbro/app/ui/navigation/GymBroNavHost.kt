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
import com.gymbro.app.ui.dashboard.DashboardScreen
import com.gymbro.app.ui.exercises.ExerciseDetailScreen
import com.gymbro.app.ui.exercises.ExercisesScreen
import com.gymbro.app.ui.onboarding.OnboardingScreen
import com.gymbro.app.ui.planeditor.PlanEditorScreen
import com.gymbro.app.ui.plans.PlansScreen
import com.gymbro.app.ui.progress.ProgressScreen
import com.gymbro.app.ui.settings.SettingsScreen
import com.gymbro.app.ui.splash.SplashScreen
import com.gymbro.app.ui.workout.WorkoutSessionScreen

// Shared transition durations
private const val NAV_ANIM_MS = 350

@Composable
fun GymBroNavHost() {
    val nav = rememberNavController()

    // Default enter/exit transitions — horizontal slide + fade
    val slideIn: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInHorizontally(tween(NAV_ANIM_MS)) { it / 3 } + fadeIn(tween(NAV_ANIM_MS))
    }
    val slideOut: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutHorizontally(tween(NAV_ANIM_MS)) { -it / 3 } + fadeOut(tween(NAV_ANIM_MS))
    }
    val popEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInHorizontally(tween(NAV_ANIM_MS)) { -it / 3 } + fadeIn(tween(NAV_ANIM_MS))
    }
    val popExit: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutHorizontally(tween(NAV_ANIM_MS)) { it / 3 } + fadeOut(tween(NAV_ANIM_MS))
    }

    NavHost(
        navController    = nav,
        startDestination = Screen.Splash.route,
        enterTransition  = slideIn,
        exitTransition   = slideOut,
        popEnterTransition = popEnter,
        popExitTransition  = popExit,
    ) {

        // ── Splash ────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onOnboardingNeeded = {
                    nav.navigate(Screen.Onboarding.route) {
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

        // ── Onboarding ────────────────────────────────────────────
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    nav.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Dashboard ─────────────────────────────────────────────
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onStartWorkout  = { sessionId -> nav.navigate(Screen.WorkoutSession.build(sessionId)) },
                onOpenPlans     = { nav.navigate(Screen.Plans.route) },
                onOpenProgress  = { nav.navigate(Screen.Progress.route) },
                onOpenExercises = { nav.navigate(Screen.Exercises.route) },
                onOpenSettings  = { nav.navigate(Screen.Settings.route) },
                onOpenRanks = { nav.navigate(Screen.StrengthRanks.route) },
            )
        }

        // ── Plans ─────────────────────────────────────────────────
        composable(Screen.Plans.route) {
            PlansScreen(
                onBack       = { nav.popBackStack() },
                onCreatePlan = { nav.navigate(Screen.PlanEditor.build(null)) },
                onEditPlan   = { id -> nav.navigate(Screen.PlanEditor.build(id)) },
            )
        }

        // ── Plan Editor ───────────────────────────────────────────
        composable(
            route     = Screen.PlanEditor.route,
            arguments = listOf(navArgument("planId") {
                type         = NavType.LongType
                defaultValue = -1L
            }),
        ) {
            PlanEditorScreen(onBack = { nav.popBackStack() })
        }

        // ── Exercises list ────────────────────────────────────────
        composable(Screen.Exercises.route) {
            ExercisesScreen(
                onBack          = { nav.popBackStack() },
                onExerciseClick = { id -> nav.navigate(Screen.ExerciseDetail.build(id)) },
            )
        }

        // ── Exercise detail ───────────────────────────────────────
        composable(
            route     = Screen.ExerciseDetail.route,
            arguments = listOf(navArgument(Screen.ExerciseDetail.ARG_EXERCISE_ID) {
                type = NavType.LongType
            }),
        ) {
            ExerciseDetailScreen(onBack = { nav.popBackStack() })
        }

        // ── Workout session ───────────────────────────────────────
        composable(
            route     = Screen.WorkoutSession.route,
            arguments = listOf(navArgument(Screen.WorkoutSession.ARG_SESSION_ID) {
                type = NavType.LongType
            }),
        ) {
            WorkoutSessionScreen(onBack = { nav.popBackStack() })
        }

        // ── Progress ──────────────────────────────────────────────
        composable(Screen.Progress.route) {
            ProgressScreen(onBack = { nav.popBackStack() })
        }

        // ── Settings ──────────────────────────────────────────────
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
        composable(Screen.StrengthRanks.route) {
            StrengthRanksScreen(onBack = { nav.popBackStack() })
}
    }
}