package com.gymbro.app.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Визуальная группа уровня. Определяет подпись и цветовую гамму
 * иконки/карточки — от серого (новичок) к золотому/фиолетовому (продвинутый).
 */
enum class LevelTier(
    val title: String,
    val primary: Color,
    val secondary: Color
) {
    NOVICE(
        title = "Новичок",
        primary = Color(0xFF9E9E9E),
        secondary = Color(0xFFCFD8DC)
    ),
    INTERMEDIATE(
        title = "Средний",
        primary = Color(0xFF2196F3),
        secondary = Color(0xFF64B5F6)
    ),
    ADVANCED(
        title = "Опытный",
        primary = Color(0xFFFFC107),
        secondary = Color(0xFFFFE082)
    ),
    ELITE(
        title = "Элита",
        primary = Color(0xFF7C4DFF),
        secondary = Color(0xFFB388FF)
    );

    companion object {
        fun of(level: Int): LevelTier = when (level) {
            in 1..4 -> NOVICE
            in 5..9 -> INTERMEDIATE
            in 10..13 -> ADVANCED
            else -> ELITE // 14-15
        }
    }
}
