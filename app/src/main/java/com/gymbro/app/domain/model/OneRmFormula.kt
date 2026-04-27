package com.gymbro.app.domain.model

import kotlin.math.roundToInt

/**
 * Формулы для оценки одноповторного максимума.
 * По умолчанию используем Epley — она широко применяется и даёт адекватную оценку
 * в диапазоне 1–10 повторений.
 */
object OneRmFormula {

    /** 1RM = weight * (1 + reps / 30) — формула Epley. */
    fun epley(weight: Double, reps: Int): Double {
        if (weight <= 0.0 || reps <= 0) return 0.0
        if (reps == 1) return weight
        return weight * (1.0 + reps.toDouble() / 30.0)
    }

    /** Brzycki — немного более консервативна для высоких повторений. */
    fun brzycki(weight: Double, reps: Int): Double {
        if (weight <= 0.0 || reps <= 0 || reps >= 37) return 0.0
        if (reps == 1) return weight
        return weight * 36.0 / (37.0 - reps)
    }

    /**
     * Обратный расчёт — какой вес можно поднять на `targetReps` повторений,
     * зная 1RM. Используем инвертированную Epley.
     */
    fun weightForReps(oneRm: Double, targetReps: Int): Double {
        if (oneRm <= 0.0 || targetReps <= 0) return 0.0
        if (targetReps == 1) return oneRm
        return oneRm / (1.0 + targetReps.toDouble() / 30.0)
    }

    fun Double.roundToKgStep(stepKg: Double = 2.5): Double {
        if (stepKg <= 0.0) return this
        return ((this / stepKg).roundToInt() * stepKg)
    }
}
