package com.gymbro.app.ui.rank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.repository.RankRepository
import com.gymbro.app.data.repository.RankState
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
) : ViewModel() {

    val rankState: StateFlow<RankState> = repo.observeRankState().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RankState(),
    )

    private val _rankUpEvent = MutableStateFlow<StrengthRank?>(null)
    val rankUpEvent: StateFlow<StrengthRank?> = _rankUpEvent.asStateFlow()

    /**
     * Сохранить введённые 1RM и завершить онбординг.
     * Вызывается с Enter1RmScreen.
     */
    fun save1Rm(bench: Double, squat: Double, deadlift: Double) {
        viewModelScope.launch {
            repo.save1Rm(bench, squat, deadlift)
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