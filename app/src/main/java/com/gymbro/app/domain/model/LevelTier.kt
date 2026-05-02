package com.gymbro.app.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Визуальный тир плана — определяется по minLevel.
 * Используется только для отображения в PlansScreen.
 */
data class LevelTier(
    val title: String,
    val primary: Color,
) {
    companion object {
        fun of(minLevel: Int): LevelTier = when {
            minLevel <= 2  -> LevelTier("Новичок",   Color(0xFF9E9E9E))
            minLevel <= 5  -> LevelTier("Средний",   Color(0xFF2196F3))
            minLevel <= 8  -> LevelTier("Опытный",   Color(0xFFFFC107))
            else           -> LevelTier("Элита",     Color(0xFF7C4DFF))
        }
    }
}