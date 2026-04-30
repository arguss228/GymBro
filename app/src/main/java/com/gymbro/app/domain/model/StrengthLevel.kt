package com.gymbro.app.domain.model

/**
 * Состояние текущего уровня силы пользователя + прогресс к следующему уровню.
 *
 * @param level текущий уровень (1..15)
 * @param tier визуальная группа
 * @param progressToNext 0f..1f — прогресс по совокупному недобору веса до следующего уровня
 * @param kgToNextByLift сколько кг осталось добавить к лучшему весу × 5 повторений по каждому лифту
 * @param best5RM лучший вес × 5 за последние N месяцев по каждому лифту
 */
data class StrengthLevel(
    val level: Int,
    val tier: LevelTier,
    val progressToNext: Float,
    val kgToNextByLift: Map<BigThreeLift, Double>,
    val best1RM: Map<BigThreeLift, Double>
) {
    val isMaxLevel: Boolean get() = level >= MAX_LEVEL

    companion object {
        const val MAX_LEVEL = 15

        val PLACEHOLDER = StrengthLevel(
            level = 1,
            tier = LevelTier.NOVICE,
            progressToNext = 0f,
            kgToNextByLift = emptyMap(),
            best1RM = emptyMap()
        )
    }
}
