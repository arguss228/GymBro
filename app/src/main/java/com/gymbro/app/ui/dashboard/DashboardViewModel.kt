package com.gymbro.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.local.entity.LevelProgressEntity
import com.gymbro.app.data.local.entity.UserProfileEntity
import com.gymbro.app.data.repository.LevelRepository
import com.gymbro.app.data.repository.ProgressRepository
import com.gymbro.app.data.repository.RankRepository
import com.gymbro.app.data.repository.RankState
import com.gymbro.app.domain.model.StrengthLevel
import com.gymbro.app.domain.model.StrengthRank
import com.gymbro.app.domain.usecase.ObserveCurrentLevelUseCase
import com.gymbro.app.domain.usecase.StartWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    private val rankRepo: RankRepository,
) : ViewModel() {

    private val _showRankUp = MutableStateFlow<StrengthRank?>(null)

    val state: StateFlow<DashboardUiState> = combine(
        observeLevel(),
        progressRepo.observeTotalSessions(),
        levelRepo.observeProfile(),
        levelRepo.observePendingCelebration(),
        rankRepo.observeRankState(),
        _showRankUp,
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val level = args[0] as StrengthLevel
        val sessions = args[1] as Int
        val profile = args[2] as UserProfileEntity?
        val celebration = args[3] as LevelProgressEntity?
        val rankState = args[4] as RankState
        val showRankUp = args[5] as StrengthRank?
        DashboardUiState(
            isLoading = false,
            level = level,
            totalSessions = sessions,
            profile = profile,
            pendingCelebration = celebration,
            rankState = rankState,
            showRankUp = showRankUp,
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

    fun dismissRankUp() {
        _showRankUp.value = null
    }
}