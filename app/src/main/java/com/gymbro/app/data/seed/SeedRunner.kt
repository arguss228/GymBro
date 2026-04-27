package com.gymbro.app.data.seed

import com.gymbro.app.data.local.GymBroDatabase
import com.gymbro.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Заполняет БД начальными данными, если это первый запуск.
 * Вызывается один раз при инициализации БД (из Hilt-модуля).
 */
@Singleton
class SeedRunner @Inject constructor(
    private val db: GymBroDatabase,
) {
    fun runIfNeeded(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            seedProfile()
            seedExercises()
            seedPlans()
        }
    }

    private suspend fun seedProfile() {
        if (db.userProfileDao().get() == null) {
            db.userProfileDao().upsert(UserProfileEntity())
        }
    }

    private suspend fun seedExercises() {
        val dao = db.exerciseDao()
        if (dao.count() == 0) {
            dao.insertAll(ExerciseSeed.exercises)
        }
    }

    private suspend fun seedPlans() {
        val planDao = db.workoutPlanDao()
        val dayDao = db.trainingDayDao()
        if (planDao.presetCount() > 0) return

        // Карта «имя упражнения → id» по данным, уже вставленным в БД.
        val nameToId = db.exerciseDao().getAll().associate { it.name to it.id }
        val resolver = WorkoutPlanSeed.ExerciseResolver { name ->
            nameToId[name] ?: error("Exercise not found in seed: '$name'")
        }

        WorkoutPlanSeed.plans.forEach { bundle ->
            val planId = planDao.insert(bundle.plan.copy(id = 0L))
            bundle.buildDays(resolver).forEach { dayBundle ->
                val dayId = dayDao.insertDay(dayBundle.day.copy(id = 0L, planId = planId))
                val dayExercises = dayBundle.exercises.map { it.copy(id = 0L, dayId = dayId) }
                dayDao.insertDayExercises(dayExercises)
            }
        }
    }
}
