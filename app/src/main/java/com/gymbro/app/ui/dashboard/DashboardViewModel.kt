package com.gymbro.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.repository.BodyRankRepository
import com.gymbro.app.data.repository.ProgressRepository
import com.gymbro.app.data.repository.RankRepository
import com.gymbro.app.data.repository.RankState
import com.gymbro.app.domain.model.StrengthRank
import com.gymbro.app.domain.model.UserBodyRank
import com.gymbro.app.domain.usecase.StartWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean         = true,
    val rankState: RankState       = RankState(),
    val bodyRank: UserBodyRank?    = null,
    val totalSessions: Int         = 0,
    val rankUpEvent: StrengthRank? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val rankRepo: RankRepository,
    private val bodyRankRepo: BodyRankRepository,
    progressRepo: ProgressRepository,
    private val startWorkout: StartWorkoutUseCase,
) : ViewModel() {

    private val _rankUpEvent = MutableStateFlow<StrengthRank?>(null)

    val state: StateFlow<DashboardUiState> = combine(
        rankRepo.observeRankState(),
        bodyRankRepo.observeUserBodyRank(),
        progressRepo.observeTotalSessions(),
        _rankUpEvent,
    ) { rankState, bodyRank, sessions, rankUp ->
        DashboardUiState(
            isLoading     = false,
            rankState     = rankState,
            bodyRank      = bodyRank,
            totalSessions = sessions,
            rankUpEvent   = rankUp,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardUiState(),
    )

    fun onStartWorkout(onReady: (Long) -> Unit) {
        viewModelScope.launch {
            val result = startWorkout()
            onReady(result.sessionId)
        }
    }

    fun dismissRankUp() { _rankUpEvent.value = null }
}