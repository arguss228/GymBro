package com.obsession.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Одна тренировочная сессия — хранит реальное время тренировки (из таймера,
 * который запускается вверху экрана тренировки), чтобы карточка "Общее время"
 * на главной странице показывала актуальные часы/минуты.
 */
@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val sessionId: Long,
    val startedAt: Long,
    val endedAt: Long? = null,
    /** Длительность тренировки в секундах — берётся напрямую из таймера на экране тренировки. */
    val durationSeconds: Long = 0L,
)