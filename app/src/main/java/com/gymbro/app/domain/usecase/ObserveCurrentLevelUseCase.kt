package com.gymbro.app.domain.usecase

import com.gymbro.app.data.repository.LevelRepository
import com.gymbro.app.data.repository.ProgressRepository
import com.gymbro.app.domain.model.StrengthLevel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Реактивный поток [StrengthLevel], который перевычисляется при любом изменении истории
 * (новый set_log или новая запись в level_progress).
 */
class ObserveCurrentLevelUseCase @Inject constructor(
    private val calc: CalculateStrengthLevelUseCase,
    private val progressRepo: ProgressRepository,
    private val levelRepo: LevelRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<StrengthLevel> {
        // Триггерим пересчёт, когда меняется количество сессий или последний снимок уровня.
        return combine(
            progressRepo.observeTotalSessions(),
            levelRepo.observeLatestLevel(),
        ) { _, _ -> Unit }.let { trigger ->
            flow {
                emit(calc()) // первоначальный расчёт
                trigger.collect { emit(calc()) }
            }.flowOn(Dispatchers.IO)
        }
    }
}
