package com.gymbro.app.ui.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.local.entity.WorkoutPlanEntity
import com.gymbro.app.data.repository.WorkoutRepository
import com.gymbro.app.domain.usecase.ObserveCurrentLevelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PlanFilter(val title: String, val range: IntRange?) {
    ALL("Все", null),
    NOVICE("Новичок", 1..4),
    INTERMEDIATE("Средний", 5..9),
    ADVANCED("Опытный", 10..15),
}

data class PlansUiState(
    val plans: List<WorkoutPlanEntity> = emptyList(),
    val filter: PlanFilter = PlanFilter.ALL,
    val userLevel: Int = 1,
    val activePlanId: Long? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    observeLevel: ObserveCurrentLevelUseCase,
) : ViewModel() {

    private val filterFlow = MutableStateFlow(PlanFilter.ALL)

    val state: StateFlow<PlansUiState> = combine(
        workoutRepo.observeAllPlans(),
        workoutRepo.observeActivePlan(),
        observeLevel(),
        filterFlow,
    ) { plans, active, level, filter ->
        val filtered = if (filter.range == null) plans
        else plans.filter { it.minLevel in filter.range }
        PlansUiState(
            plans = filtered,
            filter = filter,
            userLevel = level.level,
            activePlanId = active?.id,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlansUiState())

    fun setFilter(filter: PlanFilter) {
        filterFlow.value = filter
    }

    fun setActive(planId: Long) {
        viewModelScope.launch { workoutRepo.setActive(planId) }
    }

    fun deletePlan(planId: Long) {
        viewModelScope.launch { workoutRepo.deleteUserPlan(planId) }
    }
}
