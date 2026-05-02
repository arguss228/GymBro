package com.gymbro.app.domain.model

/**
 * Упрощённая модель «уровня» — вычисляется на основе текущего ранга.
 * Используется для совместимости с PlansScreen (фильтр minLevel)
 * и ProgressScreen (отображение карточки уровня).
 */
data class StrengthLevel(
    /** Номер уровня 1..12 (соответствует индексу ранга + 1). */
    val level: Int = 1,
    /** Прогресс к следующему рангу, 0f..1f. */
    val progress: Float = 0f,
) {
    companion object {
        val PLACEHOLDER = StrengthLevel(level = 1, progress = 0f)
    }
}