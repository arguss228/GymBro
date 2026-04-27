package com.gymbro.app.data.repository

import com.gymbro.app.data.local.dao.TrainingDayDao
import com.gymbro.app.data.local.dao.WorkoutPlanDao
import com.gymbro.app.data.local.entity.TrainingDayEntity
import com.gymbro.app.data.local.entity.TrainingDayExerciseEntity
import com.gymbro.app.data.local.entity.WorkoutPlanEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val planDao: WorkoutPlanDao,
    private val dayDao: TrainingDayDao,
) {
    fun observeAllPlans(): Flow<List<WorkoutPlanEntity>> = planDao.observeAll()

    fun observePlansForLevel(userLevel: Int): Flow<List<WorkoutPlanEntity>> =
        planDao.observeAvailableFor(userLevel)

    fun observeActivePlan(): Flow<WorkoutPlanEntity?> = planDao.observeActive()

    suspend fun getPlan(id: Long): WorkoutPlanEntity? = planDao.getById(id)

    fun observeDays(planId: Long): Flow<List<TrainingDayEntity>> =
        dayDao.observeDaysForPlan(planId)

    fun observeDayExercises(dayId: Long): Flow<List<TrainingDayExerciseEntity>> =
        dayDao.observeExercisesForDay(dayId)

    suspend fun getDaysForPlan(planId: Long): List<TrainingDayEntity> =
        dayDao.getDaysForPlan(planId)

    suspend fun getExercisesForDay(dayId: Long): List<TrainingDayExerciseEntity> =
        dayDao.getExercisesForDay(dayId)

    suspend fun setActive(planId: Long) = planDao.setActive(planId)

    /** Создаёт новый пользовательский план со всеми днями и упражнениями. */
    suspend fun createCustomPlan(
        plan: WorkoutPlanEntity,
        days: List<Pair<TrainingDayEntity, List<TrainingDayExerciseEntity>>>,
    ): Long {
        val planId = planDao.insert(plan.copy(id = 0L, isPreset = false))
        days.forEachIndexed { index, (day, exercises) ->
            val dayId = dayDao.insertDay(
                day.copy(id = 0L, planId = planId, orderIndex = index)
            )
            dayDao.insertDayExercises(exercises.map { it.copy(id = 0L, dayId = dayId) })
        }
        return planId
    }

    /**
     * Обновляет существующий план:
     * 1. Обновляет метаданные плана (название, описание и т.д.).
     * 2. Полностью перезаписывает дни и их упражнения в правильном порядке.
     *
     * Используется при редактировании как пользовательских, так и preset-планов,
     * но preset-флаг не изменяется.
     */
    suspend fun updatePlan(
        plan: WorkoutPlanEntity,
        days: List<Pair<TrainingDayEntity, List<TrainingDayExerciseEntity>>>,
    ) {
        planDao.update(plan)

        // Атомарно удаляем все старые дни и вставляем новые с актуальными orderIndex.
        val newDays = days.mapIndexed { index, (day, _) ->
            day.copy(planId = plan.id, orderIndex = index, id = 0L)
        }
        val dayIds = dayDao.replaceDaysForPlan(plan.id, newDays)

        // Вставляем упражнения для каждого нового дня.
        days.forEachIndexed { index, (_, exercises) ->
            val dayId = dayIds[index]
            dayDao.insertDayExercises(
                exercises.mapIndexed { exIndex, ex ->
                    ex.copy(id = 0L, dayId = dayId, orderIndex = exIndex)
                }
            )
        }
    }

    suspend fun replaceDayExercises(dayId: Long, exercises: List<TrainingDayExerciseEntity>) {
        dayDao.clearDayExercises(dayId)
        dayDao.insertDayExercises(exercises.map { it.copy(id = 0L, dayId = dayId) })
    }

    suspend fun deleteUserPlan(id: Long): Boolean = planDao.deleteUserPlan(id) > 0
}