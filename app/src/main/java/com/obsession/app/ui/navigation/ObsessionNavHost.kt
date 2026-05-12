package com.obsession.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.obsession.app.ui.bodyrank.BodyAnalysisScreen
import com.obsession.app.ui.bodyrank.ExerciseRanksScreen
import com.obsession.app.ui.bodytab.BodyAnalysisTabScreen
import com.obsession.app.ui.exercises.ExerciseDetailScreen
import com.obsession.app.ui.exercises.ExercisesScreen
import com.obsession.app.ui.goals.AddGoalDialog
import com.obsession.app.ui.habits.HabitTrackerScreen
import com.obsession.app.ui.home.HomeScreen
import com.obsession.app.ui.onboarding.OnboardingProfileScreen
import com.obsession.app.ui.plandetail.TrainingPlanDetailScreen
import com.obsession.app.ui.planeditor.PlanEditorScreen
import com.obsession.app.ui.plans.PlansScreen
import com.obsession.app.ui.profile.ProfileScreen
import com.obsession.app.ui.progress.ProgressScreen
import com.obsession.app.ui.rank.Enter1RmScreen
import com.obsession.app.ui.rank.StrengthRanksScreen
import com.obsession.app.ui.settings.SettingsScreen
import com.obsession.app.ui.splash.SplashScreen
import com.obsession.app.ui.workout.WorkoutSessionScreen
import com.obsession.app.ui.workouts.WorkoutsTabScreen

private const val NAV_MS = 300

@Composable
fun ObsessionNavHost() {
    val nav = rememberNavController()
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val slideIn:  AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInHorizontally(tween(NAV_MS)) { it / 3 } + fadeIn(tween(NAV_MS))
    }
    val slideOut: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutHorizontally(tween(NAV_MS)) { -it / 3 } + fadeOut(tween(NAV_MS))
    }
    val popEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInHorizontally(tween(NAV_MS)) { -it / 3 } + fadeIn(tween(NAV_MS))
    }
    val popExit:  AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutHorizontally(tween(NAV_MS)) { it / 3 } + fadeOut(tween(NAV_MS))
    }
    val tabEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = { fadeIn(tween(200)) }
    val tabExit:  AnimatedContentTransitionScope<*>.() -> ExitTransition  = { fadeOut(tween(150)) }

    val showBottomBar = shouldShowBottomBar(currentRoute)

    Scaffold(
        bottomBar = { if (showBottomBar) ObsessionBottomNavBar(navController = nav) },
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        NavHost(
            navController    = nav,
            startDestination = Screen.Splash.route,
            modifier         = Modifier.padding(innerPadding),
            enterTransition  = slideIn,
            exitTransition   = slideOut,
            popEnterTransition = popEnter,
            popExitTransition  = popExit,
        ) {

            // ── Splash ────────────────────────────────────────────
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNeedsProfile = {
                        nav.navigate(Screen.OnboardingProfile.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
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

            // ── Onboarding ────────────────────────────────────────
            composable(Screen.OnboardingProfile.route) {
                OnboardingProfileScreen(
                    onDone = {
                        nav.navigate(Screen.Enter1Rm.route) {
                            popUpTo(Screen.OnboardingProfile.route) { inclusive = true }
                        }
                    }
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

            // ── Home (центр bottom nav) ───────────────────────────
            composable(
                route              = Screen.Home.route,
                enterTransition    = tabEnter,
                exitTransition     = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition  = tabExit,
            ) {
                // Диалог добавления цели управляется локально
                var showAddGoal by remember { mutableStateOf(false) }

                HomeScreen(
                    onStartWorkout = { sessionId ->
                        nav.navigate(Screen.WorkoutSession.build(sessionId))
                    },
                    onOpenSettings = { nav.navigate(Screen.Settings.route) },
                    onOpenRanks    = { nav.navigate(Screen.StrengthRanks.route) },
                    onOpenProfile  = { nav.navigate(Screen.Profile.route) },
                    onAddGoal      = { showAddGoal = true },
                )

                if (showAddGoal) {
                    // AddGoalDialog — инжектируется данными через ViewModel уровня HomeScreen
                    // Здесь упрощённо — передаём через nav
                    // Для полноценной реализации используйте GoalAddViewModel
                    showAddGoal = false // placeholder, реальный диалог показывается внутри HomeScreen
                }
            }

            // ── Workouts ──────────────────────────────────────────
            composable(
                route              = Screen.Workouts.route,
                enterTransition    = tabEnter,
                exitTransition     = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition  = tabExit,
            ) {
                WorkoutsTabScreen(
                    onCreatePlan   = { nav.navigate(Screen.PlanEditor.build(null)) },
                    onEditPlan     = { id -> nav.navigate(Screen.PlanEditor.build(id)) },
                    onViewPlan     = { id -> nav.navigate(Screen.PlanDetail.build(id)) },
                    onExerciseClick = { id -> nav.navigate(Screen.ExerciseDetail.build(id)) },
                )
            }

            // ── Progress ──────────────────────────────────────────
            composable(
                route              = Screen.Progress.route,
                enterTransition    = tabEnter,
                exitTransition     = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition  = tabExit,
            ) {
                ProgressScreen(onBack = { }, isEmbedded = true)
            }

            // ── Body Analysis Tab ─────────────────────────────────
            composable(
                route              = Screen.BodyAnalysisTab.route,
                enterTransition    = tabEnter,
                exitTransition     = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition  = tabExit,
            ) {
                BodyAnalysisTabScreen()
            }

            // ── Habit Tracker ─────────────────────────────────────
            composable(
                route              = Screen.HabitTracker.route,
                enterTransition    = tabEnter,
                exitTransition     = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition  = tabExit,
            ) {
                HabitTrackerScreen()
            }

            // ── Profile ───────────────────────────────────────────
            composable(Screen.Profile.route) {
                ProfileScreen(onOpenSettings = { nav.navigate(Screen.Settings.route) })
            }

            // ── Detail screens ────────────────────────────────────
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
                    onViewPlan   = { id -> nav.navigate(Screen.PlanDetail.build(id)) },
                )
            }
            composable(
                Screen.PlanDetail.route,
                arguments = listOf(navArgument(Screen.PlanDetail.ARG_PLAN_ID) { type = NavType.LongType }),
            ) {
                TrainingPlanDetailScreen(
                    onBack = { nav.popBackStack() },
                    onEdit = { id -> nav.navigate(Screen.PlanEditor.build(id)) },
                )
            }
            composable(
                Screen.PlanEditor.route,
                arguments = listOf(navArgument("planId") { type = NavType.LongType; defaultValue = -1L }),
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
                arguments = listOf(navArgument(Screen.ExerciseDetail.ARG_EXERCISE_ID) { type = NavType.LongType }),
            ) {
                ExerciseDetailScreen(onBack = { nav.popBackStack() })
            }
            composable(
                Screen.WorkoutSession.route,
                arguments = listOf(navArgument(Screen.WorkoutSession.ARG_SESSION_ID) { type = NavType.LongType }),
            ) {
                WorkoutSessionScreen(
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}