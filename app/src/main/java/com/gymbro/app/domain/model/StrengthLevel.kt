// domain/model/StrengthLevel.kt
package com.gymbro.app.domain.model

data class StrengthLevel(
    val level: Int,
    val tier: LevelTier,
    val progressToNext: Float,
    val kgToNextByLift: Map<BigThreeLift, Double>,
    val best1RM: Map<BigThreeLift, Double>,
    // ── Новые поля для системы рангов ──
    val strengthRank: StrengthRank = StrengthRanks.all.first(),
) {
    val isMaxLevel: Boolean get() = level >= MAX_LEVEL

    companion object {
        const val MAX_LEVEL = 15

        val PLACEHOLDER = StrengthLevel(
            level = 1,
            tier = LevelTier.NOVICE,
            progressToNext = 0f,
            kgToNextByLift = emptyMap(),
            best1RM = emptyMap(),
            strengthRank = StrengthRanks.all.first(),
        )
    }
}