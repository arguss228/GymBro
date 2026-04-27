package com.gymbro.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Профиль пользователя. В приложении поддерживается один профиль — всегда id = 1.
 * Хранит настройки, которые влияют на расчёт уровня и UI.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Long = SINGLETON_ID,

    val name: String? = null,

    /** Единицы измерения (на будущее — пока только кг). */
    val unitsKg: Boolean = true,

    /** Пройден ли онбординг. */
    val onboardingCompleted: Boolean = false,

    /** Окно для расчёта уровня силы, в месяцах. По умолчанию 6. */
    val levelWindowMonths: Int = 6,

    /** Дата создания профиля. */
    val createdAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val SINGLETON_ID = 1L
    }
}
