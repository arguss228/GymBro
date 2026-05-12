package com.obsession.app.domain.goals

import kotlinx.coroutines.flow.Flow

// ════════════════════════════════════════════════════════════════
//  Типы целей
// ════════════════════════════════════════════════════════════════

enum class GoalType {
    STRENGTH,     // Цель по силе — новый максимум в упражнении
    BODY_WEIGHT,  // Цель по весу тела
    WIN_STREAK,   // Цель по серии тренировок
}

// Варианты продолжительности Win Streak
enum class StreakGoalDuration(val label: String, val days: Int) {
    ONE_WEEK("1 неделя", 7),
    TWO_WEEKS("2 недели", 14),
    THIRTY_DAYS("30 дней", 30),
    FIFTY_DAYS("50 дней", 50),
}

// Сложность цели по весу тела
enum class WeightGoalDifficulty(val label: String, val emoji: String) {
    EASY("Легко достичь", "😊"),        // ±3 кг
    REASONABLE("Разумная цель", "💪"),  // ±3–5 кг
    HARD("Сложно достичь", "🔥"),       // более ±5 кг
}

fun weightDifficulty(currentKg: Double, targetKg: Double): WeightGoalDifficulty {
    val diff = Math.abs(targetKg - currentKg)
    return when {
        diff <= 3.0 -> WeightGoalDifficulty.EASY
        diff <= 5.0 -> WeightGoalDifficulty.REASONABLE
        else        -> WeightGoalDifficulty.HARD
    }
}

// ════════════════════════════════════════════════════════════════
//  UI-модель цели
// ════════════════════════════════════════════════════════════════

data class UserGoal(
    val id: String,
    val type: GoalType,
    val title: String,
    val subtitle: String,
    val progress: Float,      // 0f..1f
    val isCompleted: Boolean,
    val deadline: String?,    // "dd MMM yyyy" или null
    // Доп. данные для проверки
    val targetValue: Double = 0.0,
    val currentValue: Double = 0.0,
    val exerciseId: Long? = null,
    val targetReps: Int? = null,
)

// ════════════════════════════════════════════════════════════════
//  Параметры новой цели (передаются из диалога добавления)
// ════════════════════════════════════════════════════════════════

sealed class GoalParams {
    /** Цель по силе */
    data class Strength(
        val exerciseId: Long,
        val exerciseName: String,
        val targetWeightKg: Double,
        val targetReps: Int,
        val deadlineMillis: Long?,
    ) : GoalParams()

    /** Цель по весу тела */
    data class BodyWeight(
        val currentWeightKg: Double,
        val targetWeightKg: Double,
        val deadlineMillis: Long?,
    ) : GoalParams()

    /** Цель по Win Streak */
    data class WinStreak(
        val duration: StreakGoalDuration,
    ) : GoalParams()
}

// ════════════════════════════════════════════════════════════════
//  Репозиторий целей (интерфейс — реализация через Room/DataStore)
// ════════════════════════════════════════════════════════════════

interface GoalRepository {
    fun observeGoals(): Flow<List<UserGoal>>
    suspend fun addGoal(params: GoalParams)
    suspend fun markCompleted(id: String)
    suspend fun deleteGoal(id: String)
    /** Проверяет цели и возвращает только что достигнутые (для поздравления). */
    suspend fun checkAndUpdateGoals(
        currentWeightKg: Double? = null,
        currentStreak: Int? = null,
    ): List<UserGoal>
}