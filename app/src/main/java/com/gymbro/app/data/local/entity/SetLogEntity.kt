package com.gymbro.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Факт выполненного подхода. Лог ведётся по упражнению (exerciseId),
 * а не по training_day_exercise, чтобы сохранять историю даже после удаления плана.
 *
 * @param sessionId идентификатор тренировочной сессии (один поход в зал).
 */
@Entity(
    tableName = "set_logs",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("exercise_id"),
        Index("session_id"),
        Index("performed_at")
    ]
)
data class SetLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "session_id")
    val sessionId: Long,

    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,

    @ColumnInfo(name = "set_number")
    val setNumber: Int,

    @ColumnInfo(name = "weight_kg")
    val weightKg: Double,

    @ColumnInfo(name = "reps")
    val reps: Int,

    /** RPE 1..10, опционально. */
    @ColumnInfo(name = "rpe")
    val rpe: Double? = null,

    @ColumnInfo(name = "is_warmup", defaultValue = "0")
    val isWarmup: Boolean = false,

    @ColumnInfo(name = "performed_at")
    val performedAt: Long = System.currentTimeMillis(),

    /** estimated 1RM, рассчитанный на момент логирования (кешируется для графиков). */
    @ColumnInfo(name = "estimated_1rm")
    val estimated1Rm: Double,
)
