package com.gymbro.app.ui.plandetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.local.entity.TrainingDayEntity
import com.gymbro.app.data.local.entity.TrainingDayExerciseEntity
import com.gymbro.app.data.local.entity.WorkoutPlanEntity
import com.gymbro.app.data.repository.ExerciseRepository
import com.gymbro.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DayWithExercises(
    val day: TrainingDayEntity,
    val exercises: List<TrainingDayExerciseEntity>,
)

data class TrainingPlanDetailUiState(
    val plan: WorkoutPlanEntity? = null,
    val days: List<DayWithExercises> = emptyList(),
    val exerciseNames: Map<Long, String> = emptyMap(),
    val isActive: Boolean = false,
    val isLoading: Boolean = true,
    val activatedEvent: Boolean = false,
)

@HiltViewModel
class TrainingPlanDetailViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val exerciseRepo: ExerciseRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val planId: Long = savedStateHandle.get<Long>("planId") ?: 0L

    private val _state = MutableStateFlow(TrainingPlanDetailUiState())
    val state: StateFlow<TrainingPlanDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val plan = workoutRepo.getPlan(planId)
            if (plan == null) {
                _state.value = _state.value.copy(isLoading = false)
                return@launch
            }

            val dbDays = workoutRepo.getDaysForPlan(planId)
            val daysData = dbDays.map { day ->
                val exercises = workoutRepo.getExercisesForDay(day.id)
                DayWithExercises(day, exercises)
            }

            // Collect all unique exercise IDs
            val allExIds = daysData.flatMap { it.exercises }.map { it.exerciseId }.distinct()
            val nameMap = allExIds.mapNotNull { id ->
                exerciseRepo.getById(id)?.let { id to it.name }
            }.toMap()

            // Check if this plan is active
            val activePlan = workoutRepo.observeActivePlan().firstOrNull()
            val isActive = activePlan?.id == planId

            _state.value = _state.value.copy(
                plan = plan,
                days = daysData,
                exerciseNames = nameMap,
                isActive = isActive,
                isLoading = false,
            )
        }
    }

    fun setAsActive() {
        viewModelScope.launch {
            workoutRepo.setActive(planId)
            _state.value = _state.value.copy(
                isActive = true,
                activatedEvent = true,
            )
        }
    }

    fun consumeActivatedEvent() {
        _state.value = _state.value.copy(activatedEvent = false)
    }
}