package com.obsession.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obsession.app.data.local.dao.ExerciseDao
import com.obsession.app.data.local.dao.PersonalRecordDao
import com.obsession.app.data.local.dao.SetLogDao
import com.obsession.app.data.local.dao.UserProfileDao
import com.obsession.app.data.local.dao.WorkoutPlanDao
import com.obsession.app.data.local.dao.WorkoutSessionDao
import com.obsession.app.data.local.entity.ExerciseEntity
import com.obsession.app.data.local.entity.PersonalRecordEntity
import com.obsession.app.data.local.entity.SetLogEntity
import com.obsession.app.data.local.entity.UserProfileEntity
import com.obsession.app.data.repository.BodyRankRepository
import com.obsession.app.data.repository.RankRepository
import com.obsession.app.data.repository.RankState
import com.obsession.app.domain.goals.AddGoalResult
import com.obsession.app.domain.goals.GoalParams
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
    /** Общий ранг тела — тот же, что и на вкладке «Анализ тела». */
    val bodyRankState: RankState = RankState(),
    /** Ранг в пауэрлифтинге (жим/присед/тяга). */
    val plRankState: RankState = RankState(),
    val totalTonnageKg: Double = 0.0,
    val totalWorkoutMinutes: Long = 0L,
    val totalRecords: Int = 0,
    val userWeightKg: Double = 0.0,
    val hasActivePlan: Boolean = false,
    val goals: List<UserGoal> = emptyList(),
    val achievedGoal: UserGoal? = null,
    val exercises: List<ExerciseEntity> = emptyList(),
)

private data class HomeCombinedData(
    val plRankState: RankState,
    val bodyRank: UserBodyRank,
    val profile: UserProfileEntity?,
    val logs: List<SetLogEntity>,
    val prs: List<PersonalRecordEntity>,
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
    private val exerciseDao: ExerciseDao,
    private val startWorkoutUseCase: StartWorkoutUseCase,
    private val workoutSessionDao: WorkoutSessionDao,
) : ViewModel() {

    private val _achievedGoal = MutableStateFlow<UserGoal?>(null)

    private val baseFlow: Flow<HomeCombinedData> = combine(
        rankRepo.observeRankState(),
        bodyRankRepo.observeUserBodyRank(),
        profileDao.observe(),
        setLogDao.observeAllSetLogs(),
        prDao.observeAll(),
    ) { plRankState, bodyRank, profile, logs, prs ->
        HomeCombinedData(plRankState, bodyRank, profile, logs, prs)
    }

    val state: StateFlow<HomeUiState> = baseFlow
        .combine(goalRepo.observeGoals()) { data, goals -> data to goals }
        .combine(workoutPlanDao.observeActive()) { (data, goals), activePlan -> Triple(data, goals, activePlan) }
        .combine(exerciseDao.observeAll()) { (data, goals, activePlan), exercises ->
            val tonnage = data.logs.sumOf { it.weightKg * it.reps }
            val records = data.prs.size
            val weightKg = data.profile?.weightKg ?: 0.0

            val achievedGoalFromList = goals.firstOrNull { !it.isCompleted && it.progress >= 1f }

            // Общий ранг тела берём из BodyRankRepository — тот же источник,
            // что использует вкладка "Анализ тела" (BodyAnalysisScreen).
            val bodyRankState = RankState(
                currentRank = data.bodyRank.overallRank,
                nextRank = data.bodyRank.nextRank,
                progress = data.bodyRank.progressToNext,
                hasData = data.bodyRank.hasData,
            )

            HomeUiState(
                isLoading = false,
                userName = data.profile?.name ?: "",
                bodyRankState = bodyRankState,
                plRankState = data.plRankState,
                totalTonnageKg = tonnage,
                totalRecords = records,
                userWeightKg = weightKg,
                hasActivePlan = activePlan != null,
                achievedGoal = _achievedGoal.value ?: achievedGoalFromList,
                exercises = exercises,
                goals = goals,
            )
        }
        .combine(_achievedGoal) { state, achievedGoal ->
            state.copy(achievedGoal = achievedGoal ?: state.achievedGoal)
        }
        // Реальное суммарное время тренировок — берётся из сохранённых сессий
        // (то самое время, которое отсчитывал таймер вверху экрана тренировки),
        // а не вычисляется приблизительно по времени логирования подходов.
        .combine(workoutSessionDao.observeTotalDurationSeconds()) { state, totalSeconds ->
            state.copy(totalWorkoutMinutes = totalSeconds / 60L)
        }
        .catch { emit(HomeUiState(isLoading = false)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onStartWorkout(onReady: (Long) -> Unit) {
        viewModelScope.launch {
            val result = startWorkoutUseCase()
            onReady(result.sessionId)
        }
    }

    /** ИСПРАВЛЕНИЕ БАГА: кнопка "Добавить цель" ранее ничего не делала. */
    fun addGoal(params: GoalParams, onResult: (AddGoalResult) -> Unit = {}) {
        viewModelScope.launch {
            val result = goalRepo.addGoal(params)
            onResult(result)
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