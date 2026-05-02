package com.gymbro.app.data.repository

import com.gymbro.app.data.local.dao.ExerciseDao
import com.gymbro.app.data.local.dao.ExerciseMaxDao
import com.gymbro.app.data.local.entity.ExerciseMaxEntity
import com.gymbro.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyRankRepository @Inject constructor(
    private val exerciseMaxDao: ExerciseMaxDao,
    private val exerciseDao: ExerciseDao,
) {
    fun observeUserBodyRank(): Flow<UserBodyRank> =
        combine(exerciseMaxDao.observeAll(), exerciseDao.observeAll()) { maxList, exercises ->
            val exerciseById = exercises.associateBy { it.id }
            val maxByExId = maxList.associateBy { it.exerciseId }

            // Собираем ExerciseRank для каждого упражнения с данными
            val exerciseRanks: List<ExerciseRank> = maxList.mapNotNull { maxEntity ->
                val ex = exerciseById[maxEntity.exerciseId] ?: return@mapNotNull null
                val rankIdx = ExerciseRankThresholds.rankIndexFor(ex.name, maxEntity.best1RmKg)
                ExerciseRank(
                    exerciseId   = ex.id,
                    exerciseName = ex.name,
                    best1Rm      = maxEntity.best1RmKg,
                    rankIndex    = rankIdx,
                )
            }

            val rankByExId = exerciseRanks.associateBy { it.exerciseId }

            // Группируем по мышечным группам
            val bodyPartOrder = listOf("Руки", "Ноги", "Кор", "Плечи", "Грудь", "Спина")
            val muscleGroups = MuscleGroupId.values().mapNotNull { groupId ->
                val (displayName, bodyPart) = ExerciseRankThresholds.muscleGroupMeta[groupId]
                    ?: return@mapNotNull null

                // Все упражнения, принадлежащие этой группе
                val groupExerciseRanks = ExerciseRankThresholds.exerciseToMuscleGroup
                    .filter { it.value == groupId }
                    .keys
                    .mapNotNull { exName ->
                        // Найти по имени среди имеющихся данных
                        val ex = exercises.firstOrNull { it.name == exName } ?: return@mapNotNull null
                        rankByExId[ex.id]
                    }

                MuscleGroupRank(
                    groupId       = groupId,
                    displayName   = displayName,
                    bodyPartName  = bodyPart,
                    exerciseRanks = groupExerciseRanks,
                )
            }.sortedWith(compareBy({ bodyPartOrder.indexOf(it.bodyPartName) }, { it.displayName }))

            // Общий ранг = среднее по группам, у которых есть данные
            val groupsWithData = muscleGroups.filter { it.exerciseRanks.isNotEmpty() }
            val overallIndex = if (groupsWithData.isEmpty()) 0.0
                               else groupsWithData.map { it.averageRankIndex }.average()
            val overallRankIdx = overallIndex.toInt().coerceIn(0, StrengthRanks.all.lastIndex)
            val overallRank = StrengthRanks.all[overallRankIdx]
            val nextRank = StrengthRanks.nextRank(overallRank)
            val progress = if (nextRank == null) 1f
                           else ((overallIndex - overallRankIdx) / 1.0).toFloat().coerceIn(0f, 1f)

            UserBodyRank(
                muscleGroups     = muscleGroups,
                overallRankIndex = overallIndex,
                overallRank      = overallRank,
                nextRank         = nextRank,
                progressToNext   = progress,
            )
        }

    /**
     * Обновляет максимум упражнения, если новый лучше.
     * Вызывается автоматически при логировании подхода.
     */
    suspend fun updateIfBetter(exerciseId: Long, new1Rm: Double) {
        val current = exerciseMaxDao.getForExercise(exerciseId)
        if (current == null || new1Rm > current.best1RmKg) {
            exerciseMaxDao.upsert(ExerciseMaxEntity(
                exerciseId = exerciseId,
                best1RmKg  = new1Rm,
            ))
        }
    }

    /** Ручное сохранение / обновление максимума (из экрана анализа тела). */
    suspend fun saveMax(exerciseId: Long, oneRmKg: Double) {
        exerciseMaxDao.upsert(ExerciseMaxEntity(
            exerciseId = exerciseId,
            best1RmKg  = oneRmKg,
        ))
    }

    suspend fun getMax(exerciseId: Long): Double? =
        exerciseMaxDao.getForExercise(exerciseId)?.best1RmKg
}