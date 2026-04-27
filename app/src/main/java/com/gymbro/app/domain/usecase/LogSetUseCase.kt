package com.gymbro.app.domain.usecase

import com.gymbro.app.data.local.entity.PrType
import com.gymbro.app.data.repository.ProgressRepository
import com.gymbro.app.domain.model.BigThreeLift
import javax.inject.Inject

/**
 * Сохраняет выполненный подход и, если он затронул один из Big Three лифтов,
 * пересчитывает уровень силы.
 */
class LogSetUseCase @Inject constructor(
    private val progressRepo: ProgressRepository,
    private val calculateLevel: CalculateStrengthLevelUseCase,
) {
    data class Params(
        val sessionId: Long,
        val exerciseId: Long,
        val setNumber: Int,
        val weightKg: Double,
        val reps: Int,
        val rpe: Double? = null,
        val isWarmup: Boolean = false,
    )

    data class Result(
        val setLogId: Long,
        val newPrTypes: List<PrType>,
        val levelRecalculated: Boolean,
    )

    suspend operator fun invoke(params: Params): Result {
        val res = progressRepo.logSet(
            sessionId = params.sessionId,
            exerciseId = params.exerciseId,
            setNumber = params.setNumber,
            weightKg = params.weightKg,
            reps = params.reps,
            rpe = params.rpe,
            isWarmup = params.isWarmup,
        )

        val isBigThree = BigThreeLift.fromSeedId(params.exerciseId) != null
        if (isBigThree && !params.isWarmup) {
            calculateLevel() // обновит level_progress, ViewModel подхватит через Flow
        }

        return Result(
            setLogId = res.setLogId,
            newPrTypes = res.newPrTypes,
            levelRecalculated = isBigThree,
        )
    }
}
