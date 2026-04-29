package com.gymbro.app.ui.navigation

sealed class Screen(val route: String) {
    data object Splash         : Screen("splash")
    data object Onboarding     : Screen("onboarding")
    data object Dashboard      : Screen("dashboard")
    data object Plans          : Screen("plans")
    data object PlanEditor     : Screen("plan_editor?planId={planId}") {
        fun build(planId: Long? = null) =
            "plan_editor" + (planId?.let { "?planId=$it" } ?: "")
    }
    data object Exercises      : Screen("exercises")
    data object ExerciseDetail : Screen("exercise_detail/{exerciseId}") {
        fun build(exerciseId: Long) = "exercise_detail/$exerciseId"
        const val ARG_EXERCISE_ID = "exerciseId"
    }
    data object WorkoutSession : Screen("workout/{sessionId}") {
        fun build(sessionId: Long) = "workout/$sessionId"
        const val ARG_SESSION_ID = "sessionId"
    }
    data object Progress       : Screen("progress")
    data object Settings       : Screen("settings")
}