package com.gymbro.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PrType {
    /** Лучший вес на одно повторение (реальный или estimated). */
    ONE_RM,
    /** Лучший вес на 5 повторений — используется для расчёта уровня силы. */
    FIVE_RM,
    /** Максимальный объём за сессию (weight × reps), полезно для графика. */
    SESSION_VOLUME,
}

/**
 * Личный рекорд. Обновляется автоматически в [ProgressRepository] при логировании подхода,
 * либо при завершении сессии (для SESSION_VOLUME).
 */
@Entity(
    tableName = "personal_records",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["exercise_id", "type"], unique = true),
        Index("achieved_at")
    ]
)
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,

    val type: PrType,

    @ColumnInfo(name = "weight_kg")
    val weightKg: Double,

    val reps: Int,

    /** estimated 1RM на момент рекорда — кешируем, чтобы не пересчитывать. */
    @ColumnInfo(name = "estimated_1rm")
    val estimated1Rm: Double,

    @ColumnInfo(name = "achieved_at")
    val achievedAt: Long = System.currentTimeMillis(),

    /** Ссылка на конкретный set_log, который установил рекорд. */
    @ColumnInfo(name = "set_log_id")
    val setLogId: Long? = null,
)
