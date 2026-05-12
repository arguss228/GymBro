package com.obsession.app.ui.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obsession.app.data.local.entity.TrainingDayEntity
import com.obsession.app.data.local.entity.TrainingDayExerciseEntity
import com.obsession.app.data.local.entity.WorkoutPlanEntity
import com.obsession.app.data.repository.ExerciseRepository
import com.obsession.app.data.repository.ExerciseTechniqueRepository
import com.obsession.app.data.repository.WorkoutRepository
import com.obsession.app.domain.model.ExerciseDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseDetailUiState(
    val isLoading: Boolean = true,
    val detail: ExerciseDetail? = null,
    // Добавление в тренировку
    val showPlanPicker: Boolean = false,
    val showDayPicker: Boolean = false,
    val availablePlans: List<WorkoutPlanEntity> = emptyList(),
    val selectedPlan: WorkoutPlanEntity? = null,
    val availableDays: List<TrainingDayEntity> = emptyList(),
    val addedToWorkoutMessage: String? = null,
)

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val exerciseRepo: ExerciseRepository,
    private val techniqueRepo: ExerciseTechniqueRepository,
    private val workoutRepo: WorkoutRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val exerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: 0L

    private val _state = MutableStateFlow(ExerciseDetailUiState())
    val state: StateFlow<ExerciseDetailUiState> = _state.asStateFlow()

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            val entity = exerciseRepo.getById(exerciseId)
            if (entity == null) {
                _state.value = _state.value.copy(isLoading = false)
                return@launch
            }
            val detail = techniqueRepo.buildDetail(entity)
            _state.value = _state.value.copy(isLoading = false, detail = detail)
        }
    }

    /** Нажали «Добавить в тренировку» — показываем список планов. */
    fun onAddToWorkoutClick() {
        viewModelScope.launch {
            val plans = workoutRepo.observeAllPlans().let { flow ->
                // Берём текущее значение
                var result: List<WorkoutPlanEntity> = emptyList()
                val job = viewModelScope.launch {
                    flow.collect {
                        result = it
                        this.coroutineContext[kotlinx.coroutines.Job]?.cancel()
                    }
                }
                job.join()
                result
            }
            _state.value = _state.value.copy(
                showPlanPicker = true,
                availablePlans = plans,
            )
        }
    }

    fun onSelectPlan(plan: WorkoutPlanEntity) {
        viewModelScope.launch {
            val days = workoutRepo.getDaysForPlan(plan.id)
            _state.value = _state.value.copy(
                showPlanPicker = false,
                showDayPicker = true,
                selectedPlan = plan,
                availableDays = days,
            )
        }
    }

    fun onSelectDay(day: TrainingDayEntity) {
        viewModelScope.launch {
            val currentExercises = workoutRepo.getExercisesForDay(day.id)
            val newOrderIndex = currentExercises.size
            val newEntry = TrainingDayExerciseEntity(
                dayId = day.id,
                exerciseId = exerciseId,
                orderIndex = newOrderIndex,
                targetSets = 3,
                targetReps = 10,
                restSeconds = 90,
            )
            workoutRepo.replaceDayExercises(day.id, currentExercises + newEntry)
            _state.value = _state.value.copy(
                showDayPicker = false,
                selectedPlan = null,
                availableDays = emptyList(),
                addedToWorkoutMessage = "✓ Добавлено в «${state.value.selectedPlan?.name ?: "план"}» — ${day.name}",
            )
        }
    }

    fun dismissPlanPicker() {
        _state.value = _state.value.copy(showPlanPicker = false)
    }

    fun dismissDayPicker() {
        _state.value = _state.value.copy(showDayPicker = false, selectedPlan = null)
    }

    fun clearAddedMessage() {
        _state.value = _state.value.copy(addedToWorkoutMessage = null)
    }
}