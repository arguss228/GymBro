package com.gymbro.app.ui.rank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.repository.BodyRankRepository
import com.gymbro.app.data.repository.RankRepository
import com.gymbro.app.data.repository.RankState
import com.gymbro.app.domain.model.BigThreeLift
import com.gymbro.app.domain.model.StrengthRank
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RankViewModel @Inject constructor(
    private val repo: RankRepository,
    private val bodyRankRepo: BodyRankRepository,
) : ViewModel() {

    val rankState: StateFlow<RankState> = repo.observeRankState().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RankState(),
    )

    private val _rankUpEvent = MutableStateFlow<StrengthRank?>(null)
    val rankUpEvent: StateFlow<StrengthRank?> = _rankUpEvent.asStateFlow()

    /**
     * Сохранить введённые 1RM (онбординг).
     * Сохраняет в старую таблицу OneRm (для совместимости)
     * и в новую ExerciseMax (для системы рангов по всему телу).
     */
    fun save1Rm(bench: Double, squat: Double, deadlift: Double) {
        viewModelScope.launch {
            // Старая система (совместимость с RankDashboardCard и StrengthRanksScreen)
            repo.save1Rm(bench, squat, deadlift)

            // Новая система рангов по всему телу
            if (bench > 0.0) {
                bodyRankRepo.saveMax(
                    exerciseId = BigThreeLift.BENCH_PRESS.seedId,
                    oneRmKg    = bench,
                )
            }
            if (squat > 0.0) {
                bodyRankRepo.saveMax(
                    exerciseId = BigThreeLift.BACK_SQUAT.seedId,
                    oneRmKg    = squat,
                )
            }
            if (deadlift > 0.0) {
                bodyRankRepo.saveMax(
                    exerciseId = BigThreeLift.DEADLIFT.seedId,
                    oneRmKg    = deadlift,
                )
            }

            repo.completeOnboarding()
        }
    }

    fun tryUpdateAfterWorkout(bench: Double?, squat: Double?, deadlift: Double?) {
        viewModelScope.launch {
            val newRank = repo.updateIfBetter(bench, squat, deadlift)
            if (newRank != null) _rankUpEvent.value = newRank
        }
    }

    fun dismissRankUp() { _rankUpEvent.value = null }
}