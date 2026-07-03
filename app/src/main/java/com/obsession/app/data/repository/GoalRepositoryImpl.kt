package com.obsession.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.obsession.app.data.local.dao.PersonalRecordDao
import com.obsession.app.data.local.dao.UserProfileDao
import com.obsession.app.data.local.entity.PrType
import com.obsession.app.domain.goals.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.goalsDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "obsession_goals")

@Singleton
class GoalRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileDao: UserProfileDao,
    private val prDao: PersonalRecordDao,
) : GoalRepository {

    private val GOALS_KEY = stringPreferencesKey("goals_json")
    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale("ru"))
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class GoalEntity(
        val id: String,
        val type: String,
        val title: String,
        val subtitle: String,
        val targetValue: Double,
        val currentValue: Double,
        val isCompleted: Boolean,
        val deadlineMillis: Long?,
        val exerciseId: Long?,
        val targetReps: Int?,
        val streakDays: Int?,
    )

    override fun observeGoals(): Flow<List<UserGoal>> =
        context.goalsDataStore.data.map { prefs ->
            val raw = prefs[GOALS_KEY] ?: return@map emptyList()
            runCatching {
                json.decodeFromString<List<GoalEntity>>(raw)
                    .map { it.toUserGoal() }
            }.getOrElse { emptyList() }
        }

    override suspend fun addGoal(params: GoalParams): AddGoalResult {
        // Для "Веса тела" и "Win Streak" разрешена только одна активная цель
        // одновременно — цель по силе может быть несколько (для разных упражнений).
        val exclusiveType = when (params) {
            is GoalParams.BodyWeight -> "BODY_WEIGHT"
            is GoalParams.WinStreak -> "WIN_STREAK"
            is GoalParams.Strength -> null
        }

        if (exclusiveType != null) {
            val currentGoals = readGoals()
            val hasActiveOfType = currentGoals.any { it.type == exclusiveType && !it.isCompleted }
            if (hasActiveOfType) return AddGoalResult.DuplicateCategory
        }

        val entity = when (params) {
            is GoalParams.Strength -> GoalEntity(
                id = UUID.randomUUID().toString(),
                type = "STRENGTH",
                title = "Цель по силе: ${params.exerciseName}",
                subtitle = "${params.targetWeightKg.toInt()} кг × ${params.targetReps} повт.",
                targetValue = params.targetWeightKg,
                currentValue = 0.0,
                isCompleted = false,
                deadlineMillis = params.deadlineMillis,
                exerciseId = params.exerciseId,
                targetReps = params.targetReps,
                streakDays = null,
            )
            is GoalParams.BodyWeight -> GoalEntity(
                id = UUID.randomUUID().toString(),
                type = "BODY_WEIGHT",
                title = "Цель по весу тела",
                subtitle = "${params.currentWeightKg.toInt()} → ${params.targetWeightKg.toInt()} кг",
                targetValue = params.targetWeightKg,
                currentValue = params.currentWeightKg,
                isCompleted = false,
                deadlineMillis = params.deadlineMillis,
                exerciseId = null,
                targetReps = null,
                streakDays = null,
            )
            is GoalParams.WinStreak -> GoalEntity(
                id = UUID.randomUUID().toString(),
                type = "WIN_STREAK",
                title = "Win Streak: ${params.duration.label}",
                subtitle = "${params.duration.days} дней подряд",
                targetValue = params.duration.days.toDouble(),
                currentValue = 0.0,
                isCompleted = false,
                deadlineMillis = null,
                exerciseId = null,
                targetReps = null,
                streakDays = params.duration.days,
            )
        }
        updateGoals { current -> current + entity }
        return AddGoalResult.Success
    }

    private suspend fun readGoals(): List<GoalEntity> =
        context.goalsDataStore.data.map { prefs ->
            prefs[GOALS_KEY]?.let { raw ->
                runCatching { json.decodeFromString<List<GoalEntity>>(raw) }.getOrElse { emptyList() }
            } ?: emptyList()
        }.first()

    override suspend fun markCompleted(id: String) {
        updateGoals { goals ->
            goals.map { if (it.id == id) it.copy(isCompleted = true) else it }
        }
    }

    override suspend fun deleteGoal(id: String) {
        updateGoals { goals -> goals.filter { it.id != id } }
    }

    override suspend fun checkAndUpdateGoals(
        currentWeightKg: Double?,
        currentStreak: Int?,
    ): List<UserGoal> {
        var achieved = emptyList<UserGoal>()

        updateGoals { goals ->
            val updated = goals.map { entity ->
                when (entity.type) {
                    "BODY_WEIGHT" -> {
                        val weight = currentWeightKg ?: profileDao.get()?.weightKg ?: entity.currentValue
                        val progress = computeWeightProgress(
                            current = entity.currentValue,
                            target = entity.targetValue,
                            latest = weight,
                        )
                        val done = progress >= 1f && !entity.isCompleted
                        entity.copy(currentValue = weight, isCompleted = entity.isCompleted || done)
                    }
                    "WIN_STREAK" -> {
                        val streak = currentStreak?.toDouble() ?: entity.currentValue
                        val progress = (streak / entity.targetValue).coerceIn(0.0, 1.0)
                        val done = progress >= 1.0 && !entity.isCompleted
                        entity.copy(currentValue = streak, isCompleted = entity.isCompleted || done)
                    }
                    "STRENGTH" -> {
                        val exId = entity.exerciseId ?: return@map entity
                        val bestPr = prDao.getPr(exId, PrType.ONE_RM)
                        val current = bestPr?.weightKg ?: entity.currentValue
                        val done = current >= entity.targetValue && !entity.isCompleted
                        entity.copy(currentValue = current, isCompleted = entity.isCompleted || done)
                    }
                    else -> entity
                }
            }

            val originalById = goals.associateBy { it.id }
            achieved = updated
                .filter { it.isCompleted && originalById[it.id]?.isCompleted == false }
                .map { it.toUserGoal() }

            updated
        }

        return achieved
    }

    private suspend fun updateGoals(transform: suspend (List<GoalEntity>) -> List<GoalEntity>) {
        context.goalsDataStore.edit { prefs ->
            val current = prefs[GOALS_KEY]?.let {
                runCatching { json.decodeFromString<List<GoalEntity>>(it) }.getOrElse { emptyList() }
            } ?: emptyList()
            prefs[GOALS_KEY] = json.encodeToString(transform(current))
        }
    }

    private fun computeWeightProgress(current: Double, target: Double, latest: Double): Float {
        if (current == target) return 1f
        val total = Math.abs(target - current)
        val done = Math.abs(latest - current)
        return (done / total).toFloat().coerceIn(0f, 1f)
    }

    private fun GoalEntity.toUserGoal(): UserGoal {
        val progress = when (type) {
            "BODY_WEIGHT" -> computeWeightProgress(currentValue, targetValue, currentValue)
            else -> if (targetValue > 0) (currentValue / targetValue).toFloat().coerceIn(0f, 1f) else 0f
        }
        val deadlineStr = deadlineMillis?.let {
            runCatching { dateFormat.format(Date(it)) }.getOrNull()
        }
        return UserGoal(
            id = id,
            type = GoalType.valueOf(type),
            title = title,
            subtitle = subtitle,
            progress = if (isCompleted) 1f else progress,
            isCompleted = isCompleted,
            deadline = deadlineStr,
            targetValue = targetValue,
            currentValue = currentValue,
            exerciseId = exerciseId,
            targetReps = targetReps,
        )
    }
}