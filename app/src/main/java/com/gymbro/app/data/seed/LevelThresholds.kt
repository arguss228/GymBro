package com.gymbro.app.data.seed

import com.gymbro.app.domain.model.BigThreeLift

/**
 * Минимальные целевые веса (на 5 повторений) для каждого уровня.
 * Уровень достигнут, когда пользователь взял **все три** указанных веса ×5
 * в любой из тренировок за отчётный период (по умолчанию 6 месяцев).
 *
 * Опорные точки заданы заказчиком (уровни 1, 5, 10, 15), промежуточные —
 * равномерной интерполяцией с шагом 2.5 кг.
 */
object LevelThresholds {

    /**
     * Threshold[level-1] = (bench, squat, deadlift) в кг на 5 повторений.
     * Уровни 1..15.
     */
    val table: List<LevelTargets> = listOf(
        //              Bench  Squat  Deadlift
        LevelTargets(1,  50.0,  60.0,  80.0),   // Новичок
        LevelTargets(2,  55.0,  67.5,  87.5),
        LevelTargets(3,  60.0,  75.0,  95.0),
        LevelTargets(4,  65.0,  82.5, 102.5),
        LevelTargets(5,  70.0,  90.0, 110.0),   // Средний
        LevelTargets(6,  76.0,  96.0, 116.0),
        LevelTargets(7,  82.0, 102.0, 122.0),
        LevelTargets(8,  88.0, 108.0, 128.0),
        LevelTargets(9,  94.0, 114.0, 134.0),
        LevelTargets(10,100.0, 120.0, 140.0),   // Опытный
        LevelTargets(11,108.0, 128.0, 152.0),
        LevelTargets(12,116.0, 136.0, 164.0),
        LevelTargets(13,124.0, 144.0, 176.0),
        LevelTargets(14,132.0, 152.0, 188.0),
        LevelTargets(15,140.0, 160.0, 200.0),   // Продвинутый
    )

    fun targetsFor(level: Int): LevelTargets {
        require(level in 1..15) { "Level must be in 1..15, got $level" }
        return table[level - 1]
    }

    fun targetFor(level: Int, lift: BigThreeLift): Double = with(targetsFor(level)) {
        when (lift) {
            BigThreeLift.BENCH_PRESS -> bench
            BigThreeLift.BACK_SQUAT -> squat
            BigThreeLift.DEADLIFT -> deadlift
        }
    }
}

/**
 * Минимальные веса ×5 для одного уровня.
 */
data class LevelTargets(
    val level: Int,
    val bench: Double,
    val squat: Double,
    val deadlift: Double
) {
    fun asMap(): Map<BigThreeLift, Double> = mapOf(
        BigThreeLift.BENCH_PRESS to bench,
        BigThreeLift.BACK_SQUAT to squat,
        BigThreeLift.DEADLIFT to deadlift
    )
}
