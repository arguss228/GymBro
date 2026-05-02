package com.gymbro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gymbro.app.data.local.entity.ExerciseMaxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseMaxDao {

    @Query("SELECT * FROM exercise_max")
    fun observeAll(): Flow<List<ExerciseMaxEntity>>

    @Query("SELECT * FROM exercise_max WHERE exercise_id = :exerciseId LIMIT 1")
    suspend fun getForExercise(exerciseId: Long): ExerciseMaxEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ExerciseMaxEntity)

    @Query("DELETE FROM exercise_max WHERE exercise_id = :exerciseId")
    suspend fun deleteForExercise(exerciseId: Long)
}