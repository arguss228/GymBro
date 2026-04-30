package com.gymbro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymbro.app.data.local.entity.TrainingDayEntity
import com.gymbro.app.data.local.entity.TrainingDayExerciseEntity
import kotlinx.coroutines.flow.Flow

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

    @Transaction
    suspend fun replaceDaysForPlan(planId: Long, days: List<TrainingDayEntity>): List<Long> {
        deleteDaysForPlan(planId)
        return insertDays(days)
    }

    @Query("DELETE FROM training_days WHERE plan_id = :planId")
    suspend fun deleteDaysForPlan(planId: Long)
}