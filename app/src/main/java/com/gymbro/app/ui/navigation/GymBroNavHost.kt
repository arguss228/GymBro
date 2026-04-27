package com.gymbro.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymbro.app.ui.dashboard.DashboardScreen
import com.gymbro.app.ui.exercises.ExercisesScreen
import com.gymbro.app.ui.onboarding.OnboardingScreen
import com.gymbro.app.ui.planeditor.PlanEditorScreen
import com.gymbro.app.ui.plans.PlansScreen
import com.gymbro.app.ui.progress.ProgressScreen
import com.gymbro.app.ui.splash.SplashScreen
import com.gymbro.app.ui.workout.WorkoutSessionScreen

@Composable
fun GymBroNavHost() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Screen.Splash.route,
    ) {
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

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    nav.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onStartWorkout = { sessionId ->
                    nav.navigate(Screen.WorkoutSession.build(sessionId))
                },
                onOpenPlans = { nav.navigate(Screen.Plans.route) },
                onOpenProgress = { nav.navigate(Screen.Progress.route) },
                onOpenExercises = { nav.navigate(Screen.Exercises.route) },
            )
        }

        composable(Screen.Plans.route) {
            PlansScreen(
                onBack = { nav.popBackStack() },
                onCreatePlan = { nav.navigate(Screen.PlanEditor.build(null)) },
                onEditPlan = { id -> nav.navigate(Screen.PlanEditor.build(id)) },
            )
        }

        composable(
            route = Screen.PlanEditor.route,
            arguments = listOf(navArgument("planId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) {
            PlanEditorScreen(onBack = { nav.popBackStack() })
        }

        composable(Screen.Exercises.route) {
            ExercisesScreen(onBack = { nav.popBackStack() })
        }

        composable(
            route = Screen.WorkoutSession.route,
            arguments = listOf(navArgument(Screen.WorkoutSession.ARG_SESSION_ID) {
                type = NavType.LongType
            })
        ) {
            WorkoutSessionScreen(onBack = { nav.popBackStack() })
        }

        composable(Screen.Progress.route) {
            ProgressScreen(onBack = { nav.popBackStack() })
        }
    }
}
