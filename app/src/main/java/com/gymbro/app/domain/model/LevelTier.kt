package com.gymbro.app.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Визуальная группа уровня. Определяет подпись, цветовую гамму
 * и мотивирующее описание, которые отображаются на [LevelCard].
 *
 * Цвета намеренно яркие и не зависят от системной темы —
 * они используются как акцентные поверх полупрозрачного фона карточки,
 * который подстраивается под тему (светлый/тёмный).
 */
enum class LevelTier(
    val title: String,
    val primary: Color,
    val secondary: Color,
    val description: String,
) {
    NOVICE(
        title       = "Новичок",
        primary     = Color(0xFF9E9E9E),
        secondary   = Color(0xFFCFD8DC),
        description = "Закладываешь фундамент. Каждая тренировка — шаг вперёд.",
    ),
    INTERMEDIATE(
        title       = "Средний",
        primary     = Color(0xFF2979FF),
        secondary   = Color(0xFF82B1FF),
        description = "Стабильный прогресс. Продолжай добавлять вес.",
    ),
    ADVANCED(
        title       = "Опытный",
        primary     = Color(0xFFFFC107),
        secondary   = Color(0xFFFFE082),
        description = "Серьёзный атлет. Большинство об этом только мечтают.",
    ),
    ELITE(
        title       = "Элита",
        primary     = Color(0xFF7C4DFF),
        secondary   = Color(0xFFB388FF),
        description = "Вершина силы. Ты — среди лучших.",
    );

    companion object {
        fun of(level: Int): LevelTier = when (level) {
            in 1..4   -> NOVICE
            in 5..9   -> INTERMEDIATE
            in 10..13 -> ADVANCED
            else      -> ELITE  // 14–15
        }
    }
}