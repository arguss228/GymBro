package com.obsession.app.ui.navigation

sealed class Screen(val route: String) {

    // ── Splash / Onboarding ───────────────────────────────────────
    data object Splash             : Screen("splash")
    data object OnboardingProfile  : Screen("onboarding_profile")
    data object Enter1Rm           : Screen("enter_1rm")

    // ── Bottom Navigation (5 вкладок) ────────────────────────────
    // Порядок: Тренировки | Прогресс | Главная (центр) | Анализ тела | Трекер привычек
    data object Workouts           : Screen("workouts")
    data object Progress           : Screen("progress")
    data object Home               : Screen("home")
    data object BodyAnalysisTab    : Screen("body_tab")
    data object HabitTracker       : Screen("habit_tracker")

    // ── Профиль (НЕ в bottom nav, открывается из главной) ────────
    data object Profile            : Screen("profile")

    // ── Detail / Nested screens ───────────────────────────────────
    data object StrengthRanks      : Screen("strength_ranks")
    data object BodyAnalysis       : Screen("body_analysis")
    data object ExerciseRanks      : Screen("exercise_ranks")
    data object Plans              : Screen("plans")
    data object Settings           : Screen("settings")
    data object Exercises          : Screen("exercises")

    data object PlanEditor : Screen("plan_editor?planId={planId}") {
        fun build(planId: Long? = null): String =
            if (planId != null) "plan_editor?planId=$planId" else "plan_editor"
    }

    data object PlanDetail : Screen("plan_detail/{planId}") {
        const val ARG_PLAN_ID = "planId"
        fun build(planId: Long): String = "plan_detail/$planId"
    }

    data object ExerciseDetail : Screen("exercise_detail/{exerciseId}") {
        const val ARG_EXERCISE_ID = "exerciseId"
        fun build(exerciseId: Long): String = "exercise_detail/$exerciseId"
    }

    data object WorkoutSession : Screen("workout/{sessionId}") {
        const val ARG_SESSION_ID = "sessionId"
        fun build(sessionId: Long): String = "workout/$sessionId"
    }
}