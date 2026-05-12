package com.obsession.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.ColumnInfo
import com.obsession.app.data.local.entity.SetLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SetLogEntity): Long

    @Query("SELECT * FROM set_logs WHERE session_id = :sessionId ORDER BY performed_at ASC")
    fun observeSession(sessionId: Long): Flow<List<SetLogEntity>>

    /** Все подходы (для статистики тоннажа, рекордов и т.д.) */
    @Query("SELECT * FROM set_logs ORDER BY performed_at ASC")
    fun observeAllSetLogs(): Flow<List<SetLogEntity>>

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

    @Query("""
        SELECT * FROM set_logs
        WHERE exercise_id = :exerciseId
          AND is_warmup = 0
          AND performed_at >= :sinceMillis
        ORDER BY performed_at ASC
    """)
    fun observeForExerciseSince(exerciseId: Long, sinceMillis: Long): Flow<List<SetLogEntity>>

    @Query("""
        SELECT MAX(weight_kg) FROM set_logs 
        WHERE exercise_id = :exerciseId
          AND is_warmup = 0
          AND reps >= :minReps
          AND performed_at >= :sinceMillis
    """)
    suspend fun getMaxWeightForReps(exerciseId: Long, minReps: Int, sinceMillis: Long): Double?

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

    /** 
     * Суммарный тоннаж: weight_kg * reps для всех рабочих подходов.
     * ИСПРАВЛЕНИЕ: учитывает каждый подход × повторения × вес.
     */
    @Query("SELECT SUM(weight_kg * reps) FROM set_logs WHERE is_warmup = 0")
    fun observeTotalTonnage(): Flow<Double?>

    /**
     * Количество уникальных сессий — для подсчёта примерного времени тренировок.
     */
    @Query("SELECT COUNT(DISTINCT session_id) FROM set_logs")
    suspend fun countUniqueSessions(): Int

    /**
     * Данные по сессии: первое и последнее время подхода.
     * Используется для подсчёта реального времени тренировки.
     */
    @Query("""
        SELECT MIN(performed_at), MAX(performed_at)
        FROM set_logs
        WHERE session_id = :sessionId
    """)
    suspend fun getSessionTimeRange(sessionId: Long): SessionTimeRange?

    /**
     * Все уникальные session_id для подсчёта времени.
     */
    @Query("SELECT DISTINCT session_id FROM set_logs ORDER BY session_id ASC")
    suspend fun getAllSessionIds(): List<Long>
}

data class SessionTimeRange(
    @ColumnInfo(name = "MIN(performed_at)") val startMs: Long,
    @ColumnInfo(name = "MAX(performed_at)") val endMs: Long,
)