package com.gymbro.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.local.entity.LevelProgressEntity
import com.gymbro.app.data.local.entity.UserProfileEntity
import com.gymbro.app.data.repository.LevelRepository
import com.gymbro.app.data.repository.ProgressRepository
import com.gymbro.app.domain.model.StrengthLevel
import com.gymbro.app.domain.usecase.ObserveCurrentLevelUseCase
import com.gymbro.app.domain.usecase.StartWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val level: StrengthLevel = StrengthLevel.PLACEHOLDER,
    val totalSessions: Int = 0,
    val profile: UserProfileEntity? = null,
    val pendingCelebration: LevelProgressEntity? = null,
    val rankState: RankState = RankState(),
    val showRankUp: StrengthRank? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeLevel: ObserveCurrentLevelUseCase,
    progressRepo: ProgressRepository,
    private val levelRepo: LevelRepository,
    private val startWorkout: StartWorkoutUseCase,
    val rankState: StateFlow<RankState> = rankRepo.observeRankState()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RankState())
) : ViewModel() {

    val state: StateFlow<DashboardUiState> = combine(
        observeLevel(),
        progressRepo.observeTotalSessions(),
        levelRepo.observeProfile(),
        levelRepo.observePendingCelebration(),
    ) { level, sessions, profile, celebration ->
        DashboardUiState(
            isLoading = false,
            level = level,
            totalSessions = sessions,
            profile = profile,
            pendingCelebration = celebration,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )

    fun onStartWorkout(onReady: (sessionId: Long) -> Unit) {
        viewModelScope.launch {
            val result = startWorkout()
            onReady(result.sessionId)
        }
    }

    fun onCelebrationDismissed(entryId: Long) {
        viewModelScope.launch {
            levelRepo.markCelebrationShown(entryId)
        }
    }
}
