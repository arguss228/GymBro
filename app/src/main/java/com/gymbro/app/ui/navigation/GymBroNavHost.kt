package com.gymbro.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymbro.app.ui.bodyrank.BodyAnalysisScreen
import com.gymbro.app.ui.bodyrank.ExerciseRanksScreen
import com.gymbro.app.ui.bodytab.BodyAnalysisTabScreen
import com.gymbro.app.ui.exercises.ExerciseDetailScreen
import com.gymbro.app.ui.exercises.ExercisesScreen
import com.gymbro.app.ui.home.HomeScreen
import com.gymbro.app.ui.planeditor.PlanEditorScreen
import com.gymbro.app.ui.plans.PlansScreen
import com.gymbro.app.ui.profile.ProfileScreen
import com.gymbro.app.ui.progress.ProgressScreen
import com.gymbro.app.ui.rank.Enter1RmScreen
import com.gymbro.app.ui.rank.StrengthRanksScreen
import com.gymbro.app.ui.settings.SettingsScreen
import com.gymbro.app.ui.splash.SplashScreen
import com.gymbro.app.ui.workout.WorkoutSessionScreen
import com.gymbro.app.ui.workouts.WorkoutsTabScreen

private const val NAV_MS = 300

@Composable
fun GymBroNavHost() {
    val nav = rememberNavController()
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Slide transitions for detail screens
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

    // Bottom-tab cross-fade (no slide)
    val tabEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        fadeIn(tween(200))
    }
    val tabExit: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        fadeOut(tween(150))
    }

    val showBottomBar = shouldShowBottomBar(currentRoute)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                GymBroBottomNavBar(navController = nav)
            }
        },
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
            // Default transitions (for detail screens)
            enterTransition = slideIn,
            exitTransition = slideOut,
            popEnterTransition = popEnter,
            popExitTransition = popExit,
        ) {

            // ── Splash / Onboarding ──────────────────────────────────
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNeeds1Rm = {
                        nav.navigate(Screen.Enter1Rm.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onReady = {
                        nav.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(Screen.Enter1Rm.route) {
                Enter1RmScreen(
                    onDone = {
                        nav.navigate(Screen.Home.route) {
                            popUpTo(Screen.Enter1Rm.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Bottom Nav: Home ─────────────────────────────────────
            composable(
                route = Screen.Home.route,
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit,
            ) {
                HomeScreen(
                    onStartWorkout = { sessionId ->
                        nav.navigate(Screen.WorkoutSession.build(sessionId))
                    },
                    onOpenSettings = { nav.navigate(Screen.Settings.route) },
                    onOpenRanks = { nav.navigate(Screen.StrengthRanks.route) },
                )
            }

            // ── Bottom Nav: Workouts ─────────────────────────────────
            composable(
                route = Screen.Workouts.route,
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit,
            ) {
                WorkoutsTabScreen(
                    onCreatePlan = { nav.navigate(Screen.PlanEditor.build(null)) },
                    onEditPlan = { id -> nav.navigate(Screen.PlanEditor.build(id)) },
                    onExerciseClick = { id -> nav.navigate(Screen.ExerciseDetail.build(id)) },
                )
            }

            // ── Bottom Nav: Progress ─────────────────────────────────
            composable(
                route = Screen.Progress.route,
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit,
            ) {
                ProgressScreen(
                    onBack = { /* No back in tab */ },
                    isEmbedded = true,
                )
            }

            // ── Bottom Nav: Body Analysis ────────────────────────────
            composable(
                route = Screen.BodyAnalysisTab.route,
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit,
            ) {
                BodyAnalysisTabScreen()
            }

            // ── Bottom Nav: Profile ──────────────────────────────────
            composable(
                route = Screen.Profile.route,
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit,
            ) {
                ProfileScreen()
            }

            // ── Detail screens (with slide animation) ────────────────

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
                    onBack = { nav.popBackStack() },
                    onCreatePlan = { nav.navigate(Screen.PlanEditor.build(null)) },
                    onEditPlan = { id -> nav.navigate(Screen.PlanEditor.build(id)) },
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
                    onBack = { nav.popBackStack() },
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

            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}