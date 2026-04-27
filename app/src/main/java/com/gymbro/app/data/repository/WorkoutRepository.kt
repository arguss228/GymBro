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

    suspend fun setActive(planId: Long) = planDao.setActive(planId)

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

    suspend fun replaceDayExercises(dayId: Long, exercises: List<TrainingDayExerciseEntity>) {
        dayDao.clearDayExercises(dayId)
        dayDao.insertDayExercises(exercises.map { it.copy(id = 0L, dayId = dayId) })
    }

    suspend fun deleteUserPlan(id: Long): Boolean = planDao.deleteUserPlan(id) > 0
}
