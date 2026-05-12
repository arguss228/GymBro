package com.obsession.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Long = SINGLETON_ID,

    val name: String? = null,

    /** Выбранный аватар (индекс из встроенного набора) */
    @ColumnInfo(name = "avatar_index")
    val avatarIndex: Int = 0,

    /** Вес тела, кг */
    @ColumnInfo(name = "weight_kg")
    val weightKg: Double? = null,

    /** Рост, см */
    @ColumnInfo(name = "height_cm")
    val heightCm: Int? = null,

    /** Возраст */
    val age: Int? = null,

    val unitsKg: Boolean = true,

    /** Пройден ли онбординг (ввод профиля + 1RM). */
    @ColumnInfo(name = "onboarding_completed")
    val onboardingCompleted: Boolean = false,

    /** Завершён ли шаг профиля онбординга */
    @ColumnInfo(name = "profile_step_completed")
    val profileStepCompleted: Boolean = false,

    val levelWindowMonths: Int = 6,

    val createdAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val SINGLETON_ID = 1L
    }
}