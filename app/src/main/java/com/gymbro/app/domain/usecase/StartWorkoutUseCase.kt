package com.gymbro.app.domain.usecase

import com.gymbro.app.data.repository.ProgressRepository
import com.gymbro.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Подготавливает новую тренировочную сессию: генерирует sessionId
 * и возвращает активный план (если есть).
 */
class StartWorkoutUseCase @Inject constructor(
    private val progressRepo: ProgressRepository,
    private val workoutRepo: WorkoutRepository,
) {
    data class Result(
        val sessionId: Long,
        val activePlanId: Long?,
    )

    suspend operator fun invoke(): Result {
        val sessionId = progressRepo.getNextSessionId()
        val active = workoutRepo.observeActivePlan().firstOrNull()
        return Result(sessionId = sessionId, activePlanId = active?.id)
    }
}
