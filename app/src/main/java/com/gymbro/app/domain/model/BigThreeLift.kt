package com.gymbro.app.domain.model

/**
 * Три базовых упражнения, на основе которых считается уровень силы.
 * Их ID в базе упражнений зафиксированы в [ExerciseSeed].
 */
enum class BigThreeLift(
    val seedId: Long,
    val displayName: String,
    val shortName: String,
) {
    BENCH_PRESS(seedId = 1L, displayName = "Жим лёжа", shortName = "Жим"),
    BACK_SQUAT(seedId = 2L, displayName = "Присед со штангой", shortName = "Присед"),
    DEADLIFT(seedId = 3L, displayName = "Становая тяга", shortName = "Тяга");

    companion object {
        fun fromSeedId(id: Long): BigThreeLift? = values().firstOrNull { it.seedId == id }
    }
}
