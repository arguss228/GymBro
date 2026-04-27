package com.gymbro.app.domain.usecase

import com.gymbro.app.data.repository.LevelRepository
import javax.inject.Inject

/**
 * Завершает онбординг. Пользователь может ввести свои текущие рабочие веса ×5
 * для жима/приседа/тяги, либо пропустить — тогда стартует с уровня 1.
 *
 * После сохранения запускает расчёт уровня.
 */
class OnboardingUseCase @Inject constructor(
    private val levelRepo: LevelRepository,
    private val calculateLevel: CalculateStrengthLevelUseCase,
) {
    data class Params(
        val bench5RmKg: Double?,
        val squat5RmKg: Double?,
        val deadlift5RmKg: Double?,
    )

    suspend operator fun invoke(params: Params) {
        levelRepo.completeOnboarding(
            initialBench5Rm = params.bench5RmKg,
            initialSquat5Rm = params.squat5RmKg,
            initialDeadlift5Rm = params.deadlift5RmKg,
        )
        calculateLevel()
    }
}
