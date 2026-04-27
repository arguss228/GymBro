package com.gymbro.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ExerciseCategory { CHEST, BACK, LEGS, SHOULDERS, ARMS, CORE, FULL_BODY, OTHER }
enum class ExerciseEquipment { BARBELL, DUMBBELL, MACHINE, CABLE, BODYWEIGHT, KETTLEBELL, OTHER }

/**
 * Справочник упражнений. Содержит и системные (seed), и пользовательские.
 * Системные помечаются [isSystem] = true, их нельзя удалять.
 * Для базовых упражнений (жим/присед/тяга) id зафиксированы в [BigThreeLift.seedId].
 */
@Entity(
    tableName = "exercises",
    indices = [Index(value = ["name"], unique = false)]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,

    @ColumnInfo(name = "category")
    val category: ExerciseCategory,

    @ColumnInfo(name = "equipment")
    val equipment: ExerciseEquipment,

    @ColumnInfo(name = "primary_muscle")
    val primaryMuscle: String,

    @ColumnInfo(name = "is_system", defaultValue = "0")
    val isSystem: Boolean = false,

    @ColumnInfo(name = "description")
    val description: String? = null,
)
