package com.gymbro.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Хранит лучший 1RM пользователя по каждому упражнению.
 * Обновляется автоматически при каждом новом PR.
 */
@Entity(
    tableName = "exercise_max",
    foreignKeys = [ForeignKey(
        entity = ExerciseEntity::class,
        parentColumns = ["id"],
        childColumns = ["exercise_id"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("exercise_id", unique = true)],
)
data class ExerciseMaxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,

    @ColumnInfo(name = "best_1rm_kg")
    val best1RmKg: Double,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)