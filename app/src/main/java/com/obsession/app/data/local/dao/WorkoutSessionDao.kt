package com.obsession.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.obsession.app.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WorkoutSessionEntity)

    /** Обновляет длительность тренировки — вызывается при завершении/выходе из сессии. */
    @Query("UPDATE workout_sessions SET endedAt = :endedAt, durationSeconds = :durationSeconds WHERE sessionId = :sessionId")
    suspend fun updateDuration(sessionId: Long, endedAt: Long, durationSeconds: Long)

    @Query("SELECT * FROM workout_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getById(sessionId: Long): WorkoutSessionEntity?

    /** Суммарное время всех тренировок в секундах — используется в карточке на главной странице. */
    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM workout_sessions")
    fun observeTotalDurationSeconds(): Flow<Long>
}