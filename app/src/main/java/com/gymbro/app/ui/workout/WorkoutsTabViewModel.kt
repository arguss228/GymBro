package com.gymbro.app.ui.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.local.entity.WorkoutPlanEntity
import com.gymbro.app.data.repository.WorkoutRepository
import com.gymbro.app.domain.usecase.ObserveCurrentLevelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutsTabUiState(
    val userPlans: List<WorkoutPlanEntity> = emptyList(),
    val activePlanId: Long? = null,
    val userLevel: Int = 1,
    val isLoading: Boolean = true,
)

@HiltViewModel
class WorkoutsTabViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    observeLevel: ObserveCurrentLevelUseCase,
) : ViewModel() {

    val state: StateFlow<WorkoutsTabUiState> = combine(
        workoutRepo.observeAllPlans(),
        workoutRepo.observeActivePlan(),
        observeLevel(),
    ) { plans, active, level ->
        WorkoutsTabUiState(
            // Только пользовательские планы (не preset)
            userPlans    = plans.filter { !it.isPreset },
            activePlanId = active?.id,
            userLevel    = level.level,
            isLoading    = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkoutsTabUiState())

    fun setActive(planId: Long) {
        viewModelScope.launch { workoutRepo.setActive(planId) }
    }

    fun deletePlan(planId: Long) {
        viewModelScope.launch { workoutRepo.deleteUserPlan(planId) }
    }
}