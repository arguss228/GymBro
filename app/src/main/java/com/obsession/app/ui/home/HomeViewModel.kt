package com.obsession.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obsession.app.data.local.dao.PersonalRecordDao
import com.obsession.app.data.local.dao.SetLogDao
import com.obsession.app.data.local.dao.UserProfileDao
import com.obsession.app.data.local.dao.WorkoutPlanDao
import com.obsession.app.data.local.entity.PersonalRecordEntity
import com.obsession.app.data.local.entity.SetLogEntity
import com.obsession.app.data.local.entity.UserProfileEntity
import com.obsession.app.data.repository.BodyRankRepository
import com.obsession.app.data.repository.RankRepository
import com.obsession.app.data.repository.RankState
import com.obsession.app.domain.goals.GoalRepository
import com.obsession.app.domain.goals.UserGoal
import com.obsession.app.domain.model.UserBodyRank
import com.obsession.app.domain.usecase.StartWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val bodyRankState: RankState = RankState(),
    val plRankState: RankState = RankState(),
    val totalTonnageKg: Double = 0.0,
    val totalWorkoutMinutes: Long = 0L,
    val totalRecords: Int = 0,
    val userWeightKg: Double = 0.0,
    val hasActivePlan: Boolean = false,
    val goals: List<UserGoal> = emptyList(),
    val achievedGoal: UserGoal? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val rankRepo: RankRepository,
    private val bodyRankRepo: BodyRankRepository,
    private val profileDao: UserProfileDao,
    private val setLogDao: SetLogDao,
    private val prDao: PersonalRecordDao,
    private val workoutPlanDao: WorkoutPlanDao,
    private val goalRepo: GoalRepository,
    private val startWorkoutUseCase: StartWorkoutUseCase,
) : ViewModel() {

    private val _achievedGoal = MutableStateFlow<UserGoal?>(null)

    val state: StateFlow<HomeUiState> = combine(
        rankRepo.observeRankState(),
        profileDao.observe(),
        setLogDao.observeAllSetLogs(),
        prDao.observeAll(),
        goalRepo.observeGoals(),
        workoutPlanDao.observeActive(),
    ) { rankState, profile, logs, prs, goals, activePlan ->
        val tonnage = logs.sumOf { it.weightKg * it.reps }
        val sessionIds = logs.map { it.sessionId }.distinct()
        val workoutMinutes = sessionIds.size.toLong() * 60L
        val records = prs.size
        val weightKg = profile?.weightKg ?: 0.0

        val achievedGoalFromList = goals.firstOrNull { !it.isCompleted && it.progress >= 1f }

        HomeUiState(
            isLoading = false,
            userName = profile?.name ?: "",
            bodyRankState = rankState,
            plRankState = rankState,
            totalTonnageKg = tonnage,
            totalWorkoutMinutes = workoutMinutes,
            totalRecords = records,
            userWeightKg = weightKg,
            hasActivePlan = activePlan != null,
            goals = goals,
            achievedGoal = _achievedGoal.value ?: achievedGoalFromList,
        )
    }.combine(_achievedGoal) { state, achievedGoal ->
        state.copy(achievedGoal = achievedGoal ?: state.achievedGoal)
    }.catch { emit(HomeUiState(isLoading = false)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onStartWorkout(onReady: (Long) -> Unit) {
        viewModelScope.launch {
            val result = startWorkoutUseCase()
            onReady(result.sessionId)
        }
    }

    fun dismissAchievedGoal() {
        val goal = state.value.achievedGoal ?: return
        _achievedGoal.value = null
        viewModelScope.launch {
            goalRepo.markCompleted(goal.id)
        }
    }
}