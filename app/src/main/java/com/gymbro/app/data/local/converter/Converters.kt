package com.gymbro.app.data.local.converter

import androidx.room.TypeConverter
import com.gymbro.app.data.local.entity.ExerciseCategory
import com.gymbro.app.data.local.entity.ExerciseEquipment
import com.gymbro.app.data.local.entity.PrType

class Converters {

    @TypeConverter
    fun fromCategory(value: ExerciseCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): ExerciseCategory = ExerciseCategory.valueOf(value)

    @TypeConverter
    fun fromEquipment(value: ExerciseEquipment): String = value.name

    @TypeConverter
    fun toEquipment(value: String): ExerciseEquipment = ExerciseEquipment.valueOf(value)

    @TypeConverter
    fun fromPrType(value: PrType): String = value.name

    @TypeConverter
    fun toPrType(value: String): PrType = PrType.valueOf(value)
}
