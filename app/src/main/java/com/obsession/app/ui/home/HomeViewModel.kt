package com.obsession.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obsession.app.data.local.dao.PersonalRecordDao
import com.obsession.app.data.local.dao.SetLogDao
import com.obsession.app.data.local.dao.UserProfileDao
import com.obsession.app.data.local.dao.WorkoutPlanDao
import com.obsession.app.data.repository.BodyRankRepository
import com.obsession.app.data.repository.RankRepository
import com.obsession.app.data.repository.RankState
import com.obsession.app.domain.goals.GoalRepository
import com.obsession.app.domain.goals.GoalType
import com.obsession.app.domain.goals.UserGoal
import com.obsession.app.domain.model.UserBodyRank
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    // Ранги
    val bodyRankState: RankState = RankState(),
    val plRankState: RankState = RankState(),
    // Статистика
    val totalTonnageKg: Double = 0.0,
    val totalWorkoutMinutes: Long = 0L,
    val totalRecords: Int = 0,
    val userWeightKg: Double = 0.0,
    // Планы
    val hasActivePlan: Boolean = false,
    // Цели
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
) : ViewModel() {

    private val _achievedGoal = MutableStateFlow<UserGoal?>(null)

    val state: StateFlow<HomeUiState> = combine(
        // 1) Ранги
        combine(rankRepo.observeRankState(), bodyRankRepo.observeUserBodyRank()) { plRank, bodyRank ->
            Pair(plRank, bodyRank)
        },
        // 2) Профиль
        profileDao.observe(),
        // 3) Статистика
        combine(
            setLogDao.observeAllSetLogs(),
            prDao.observeAll(),
        ) { logs, prs -> Pair(logs, prs) },
        // 4) Цели
        goalRepo.observeGoals(),
        // 5) Активный план
        workoutPlanDao.observeActive(),
        // 6) Достигнутая цель
        _achievedGoal,
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val (ranks, profile, stats, goals, activePlan, achievedGoal) = args as Array<Any?>

        val (plRankState, bodyRank) = ranks as Pair<RankState, UserBodyRank>
        val profileEntity = profile as? com.obsession.app.data.local.entity.UserProfileEntity
        val (logs, prs) = stats as Pair<*, *>
        val setLogs = logs as List<com.obsession.app.data.local.entity.SetLogEntity>
        val allPrs  = prs  as List<com.obsession.app.data.local.entity.PersonalRecordEntity>
        val goalList = goals as List<UserGoal>

        // Тоннаж: сумма weightKg * reps по всем подходам
        val tonnage = setLogs.sumOf { it.weightKg * it.reps }

        // Время тренировок: из сессий (session_duration_seconds если есть, иначе 0)
        // Пока суммируем по уникальным сессиям, каждая ~ 60 мин (заглушка до реального таймера)
        val sessionIds = setLogs.map { it.sessionId }.distinct()
        val workoutMinutes = sessionIds.size.toLong() * 60L // TODO: реальный таймер

        // Рекорды
        val records = allPrs.size

        // Вес пользователя из профиля
        val weightKg = profileEntity?.weightKg ?: 0.0

        // Проверка достижения целей
        val achievedGoalFromList = goalList.firstOrNull { goal ->
            !goal.isCompleted && goal.progress >= 1f
        }

        HomeUiState(
            isLoading           = false,
            userName            = profileEntity?.name ?: "",
            bodyRankState       = plRankState, // TODO: отдельный bodyRankState когда будет готов
            plRankState         = plRankState,
            totalTonnageKg      = tonnage,
            totalWorkoutMinutes = workoutMinutes,
            totalRecords        = records,
            userWeightKg        = weightKg,
            hasActivePlan       = activePlan != null,
            goals               = goalList,
            achievedGoal        = achievedGoal as? UserGoal ?: achievedGoalFromList,
        )
    }.catch { emit(HomeUiState(isLoading = false)) }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun dismissAchievedGoal() {
        _achievedGoal.value = null
        // Отметить цель как выполненную в репозитории
        val goal = state.value.achievedGoal ?: return
        viewModelScope.launch {
            goalRepo.markCompleted(goal.id)
        }
    }
}