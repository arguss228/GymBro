package com.gymbro.app.domain.usecase

import com.gymbro.app.data.repository.RankRepository
import com.gymbro.app.domain.model.StrengthLevel
import com.gymbro.app.domain.model.StrengthRanks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Возвращает текущий «уровень» пользователя в виде [StrengthLevel].
 * Уровень вычисляется как индекс текущего ранга (0-based) + 1,
 * чтобы обеспечить совместимость с PlansScreen (minLevel фильтр).
 */
class ObserveCurrentLevelUseCase @Inject constructor(
    private val rankRepo: RankRepository,
) {
    operator fun invoke(): Flow<StrengthLevel> = rankRepo.observeRankState().map { state ->
        val rankIndex = StrengthRanks.all.indexOf(state.currentRank).coerceAtLeast(0)
        StrengthLevel(
            level    = rankIndex + 1,
            progress = state.progress,
        )
    }
}