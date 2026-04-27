package com.gymbro.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * День тренировки внутри плана (например, «День А — грудь/трицепс»).
 */
@Entity(
    tableName = "training_days",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["plan_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["plan_id"])]
)
data class TrainingDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @androidx.room.ColumnInfo(name = "plan_id")
    val planId: Long,

    val name: String,

    /** Порядковый номер внутри плана (0..N). */
    val orderIndex: Int,
)
