package com.gymbro.app.domain.usecase

import com.gymbro.app.data.local.entity.PrType
import com.gymbro.app.data.repository.ProgressRepository
import javax.inject.Inject

/**
 * Сохраняет выполненный подход и автоматически пересчитывает личные рекорды.
 */
class LogSetUseCase @Inject constructor(
    private val progressRepo: ProgressRepository,
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
    )

    suspend operator fun invoke(params: Params): Result {
        val res = progressRepo.logSet(
            sessionId  = params.sessionId,
            exerciseId = params.exerciseId,
            setNumber  = params.setNumber,
            weightKg   = params.weightKg,
            reps       = params.reps,
            rpe        = params.rpe,
            isWarmup   = params.isWarmup,
        )
        return Result(
            setLogId   = res.setLogId,
            newPrTypes = res.newPrTypes,
        )
    }
}