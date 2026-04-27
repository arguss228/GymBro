package com.gymbro.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * План тренировок. Может быть готовый (isPreset = true) или собственный.
 * Связан с [TrainingDayEntity] через planId.
 */
@Entity(tableName = "workout_plans")
data class WorkoutPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,

    val description: String? = null,

    /** 1..4 Новичок, 5..9 Средний, 10..15 Опытный. Для фильтра готовых планов. */
    val minLevel: Int = 1,

    /** Сколько тренировочных дней в неделю (3..6). */
    val daysPerWeek: Int = 3,

    /** Готовый план (из seed) — true, пользовательский — false. */
    val isPreset: Boolean = false,

    /** Выбран ли как активный у пользователя. Одновременно активным может быть ровно один. */
    val isActive: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
)
