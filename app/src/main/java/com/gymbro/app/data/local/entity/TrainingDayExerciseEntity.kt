package com.gymbro.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Конкретное упражнение внутри тренировочного дня с плановыми параметрами:
 * целевые подходы, повторения, отдых.
 */
@Entity(
    tableName = "training_day_exercises",
    foreignKeys = [
        ForeignKey(
            entity = TrainingDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["day_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("day_id"), Index("exercise_id")]
)
data class TrainingDayExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "day_id")
    val dayId: Long,

    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,

    @ColumnInfo(name = "order_index")
    val orderIndex: Int,

    @ColumnInfo(name = "target_sets")
    val targetSets: Int,

    @ColumnInfo(name = "target_reps")
    val targetReps: Int,

    @ColumnInfo(name = "rest_seconds")
    val restSeconds: Int,

    /** Рекомендуемый рабочий вес, кг. null — «по ощущениям». */
    @ColumnInfo(name = "target_weight_kg")
    val targetWeightKg: Double? = null,

    val notes: String? = null,
)
