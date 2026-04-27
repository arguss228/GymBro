package com.gymbro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymbro.app.data.local.entity.TrainingDayEntity
import com.gymbro.app.data.local.entity.TrainingDayExerciseEntity
import com.gymbro.app.data.local.entity.WorkoutPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutPlanDao {

    @Query("SELECT * FROM workout_plans ORDER BY isPreset DESC, minLevel ASC, name ASC")
    fun observeAll(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE minLevel <= :userLevel ORDER BY minLevel DESC, name ASC")
    fun observeAvailableFor(userLevel: Int): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<WorkoutPlanEntity?>

    @Query("SELECT * FROM workout_plans WHERE id = :id")
    suspend fun getById(id: Long): WorkoutPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: WorkoutPlanEntity): Long

    @Update
    suspend fun update(plan: WorkoutPlanEntity)

    @Transaction
    suspend fun setActive(planId: Long) {
        clearActive()
        setActiveFlag(planId)
    }

    @Query("UPDATE workout_plans SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE workout_plans SET isActive = 1 WHERE id = :planId")
    suspend fun setActiveFlag(planId: Long)

    @Query("DELETE FROM workout_plans WHERE id = :id AND isPreset = 0")
    suspend fun deleteUserPlan(id: Long): Int

    @Query("SELECT COUNT(*) FROM workout_plans WHERE isPreset = 1")
    suspend fun presetCount(): Int
}

@Dao
interface TrainingDayDao {

    @Query("SELECT * FROM training_days WHERE plan_id = :planId ORDER BY orderIndex ASC")
    fun observeDaysForPlan(planId: Long): Flow<List<TrainingDayEntity>>

    @Query("SELECT * FROM training_days WHERE plan_id = :planId ORDER BY orderIndex ASC")
    suspend fun getDaysForPlan(planId: Long): List<TrainingDayEntity>

    @Query("SELECT * FROM training_day_exercises WHERE day_id = :dayId ORDER BY order_index ASC")
    fun observeExercisesForDay(dayId: Long): Flow<List<TrainingDayExerciseEntity>>

    @Query("SELECT * FROM training_day_exercises WHERE day_id = :dayId ORDER BY order_index ASC")
    suspend fun getExercisesForDay(dayId: Long): List<TrainingDayExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: TrainingDayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(days: List<TrainingDayEntity>): List<Long>

    @Update
    suspend fun updateDay(day: TrainingDayEntity)

    @Query("DELETE FROM training_days WHERE id = :dayId")
    suspend fun deleteDay(dayId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayExercise(exercise: TrainingDayExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayExercises(exercises: List<TrainingDayExerciseEntity>): List<Long>

    @Query("DELETE FROM training_day_exercises WHERE day_id = :dayId")
    suspend fun clearDayExercises(dayId: Long)

    /**
     * Полностью перезаписывает дни плана:
     * 1. Удаляет все существующие дни (каскадно удалятся и упражнения).
     * 2. Вставляет новые дни с обновлёнными orderIndex.
     */
    @Transaction
    suspend fun replaceDaysForPlan(planId: Long, days: List<TrainingDayEntity>): List<Long> {
        deleteDaysForPlan(planId)
        return insertDays(days)
    }

    @Query("DELETE FROM training_days WHERE plan_id = :planId")
    suspend fun deleteDaysForPlan(planId: Long)
}