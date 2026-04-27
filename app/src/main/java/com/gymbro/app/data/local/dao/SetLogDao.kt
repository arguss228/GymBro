package com.gymbro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gymbro.app.data.local.entity.SetLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SetLogEntity): Long

    @Query("SELECT * FROM set_logs WHERE session_id = :sessionId ORDER BY performed_at ASC")
    fun observeSession(sessionId: Long): Flow<List<SetLogEntity>>

    @Query("""
        SELECT * FROM set_logs 
        WHERE exercise_id = :exerciseId 
          AND is_warmup = 0
          AND performed_at >= :sinceMillis
        ORDER BY performed_at DESC
    """)
    suspend fun getForExerciseSince(exerciseId: Long, sinceMillis: Long): List<SetLogEntity>

    @Query("""
        SELECT * FROM set_logs 
        WHERE exercise_id = :exerciseId AND is_warmup = 0
        ORDER BY performed_at DESC
    """)
    fun observeForExercise(exerciseId: Long): Flow<List<SetLogEntity>>

    /**
     * Возвращает все рабочие подходы для упражнения за период,
     * отсортированные по времени. Используется для построения
     * графика «максимальный вес» (нарастающий исторический максимум).
     */
    @Query("""
        SELECT * FROM set_logs
        WHERE exercise_id = :exerciseId
          AND is_warmup = 0
          AND performed_at >= :sinceMillis
        ORDER BY performed_at ASC
    """)
    fun observeForExerciseSince(exerciseId: Long, sinceMillis: Long): Flow<List<SetLogEntity>>

    /**
     * Возвращает максимальный вес, с которым пользователь сделал хотя бы [minReps] повторений
     * в пределах отчётного окна. Используется для расчёта уровня силы (best 5RM).
     */
    @Query("""
        SELECT MAX(weight_kg) FROM set_logs 
        WHERE exercise_id = :exerciseId
          AND is_warmup = 0
          AND reps >= :minReps
          AND performed_at >= :sinceMillis
    """)
    suspend fun getMaxWeightForReps(
        exerciseId: Long,
        minReps: Int,
        sinceMillis: Long
    ): Double?

    /** Максимальный estimated 1RM за период. */
    @Query("""
        SELECT MAX(estimated_1rm) FROM set_logs 
        WHERE exercise_id = :exerciseId
          AND is_warmup = 0
          AND performed_at >= :sinceMillis
    """)
    suspend fun getMaxEstimated1Rm(exerciseId: Long, sinceMillis: Long): Double?

    @Query("SELECT MAX(session_id) FROM set_logs")
    suspend fun getLastSessionId(): Long?

    @Query("SELECT COUNT(DISTINCT session_id) FROM set_logs")
    fun observeTotalSessions(): Flow<Int>
}