package com.gymbro.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * История изменений уровня силы. Каждый раз, когда [CalculateStrengthLevelUseCase]
 * обнаруживает, что уровень пересчитан и ИЗМЕНИЛСЯ, сюда добавляется новая запись.
 *
 * Даёт возможность:
 *  — показать график прогрессии по уровням во времени,
 *  — триггерить анимацию «Новый уровень!» на Dashboard.
 */
@Entity(
    tableName = "level_progress",
    indices = [Index("achieved_at")]
)
data class LevelProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val level: Int,

    @ColumnInfo(name = "bench_5rm_kg")
    val bench5RmKg: Double,

    @ColumnInfo(name = "squat_5rm_kg")
    val squat5RmKg: Double,

    @ColumnInfo(name = "deadlift_5rm_kg")
    val deadlift5RmKg: Double,

    @ColumnInfo(name = "achieved_at")
    val achievedAt: Long = System.currentTimeMillis(),

    /** Пользователь уже видел анимацию повышения? */
    @ColumnInfo(name = "celebration_shown", defaultValue = "0")
    val celebrationShown: Boolean = false,
)
