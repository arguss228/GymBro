package com.gymbro.app.ui.planeditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.local.entity.ExerciseEntity
import com.gymbro.app.data.local.entity.TrainingDayEntity
import com.gymbro.app.data.local.entity.TrainingDayExerciseEntity
import com.gymbro.app.data.local.entity.WorkoutPlanEntity
import com.gymbro.app.data.repository.ExerciseRepository
import com.gymbro.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** Черновик дня в UI-состоянии. */
data class DayDraft(
    val uiId: String = UUID.randomUUID().toString(),
    val name: String,
    val exercises: List<ExerciseDraft> = emptyList(),
)

data class ExerciseDraft(
    val uiId: String = UUID.randomUUID().toString(),
    val exerciseId: Long,
    val exerciseName: String,
    val sets: Int = 3,
    val reps: Int = 8,
    val restSeconds: Int = 120,
)

data class PlanEditorUiState(
    val name: String = "",
    val description: String = "",
    val daysPerWeek: Int = 3,
    val minLevel: Int = 1,
    val days: List<DayDraft> = listOf(
        DayDraft(name = "День A"),
        DayDraft(name = "День B"),
        DayDraft(name = "День C"),
    ),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
)

@HiltViewModel
class PlanEditorViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val exerciseRepo: ExerciseRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val editingPlanId: Long? = savedStateHandle.get<Long>("planId")
        ?.takeIf { it > 0 }

    private val _state = MutableStateFlow(PlanEditorUiState())
    val state = _state.asStateFlow()

    fun setName(v: String) = update { it.copy(name = v) }
    fun setDescription(v: String) = update { it.copy(description = v) }
    fun setDaysPerWeek(v: Int) = update { cur ->
        val n = v.coerceIn(3, 6)
        val days = adjustDays(cur.days, n)
        cur.copy(daysPerWeek = n, days = days)
    }
    fun setMinLevel(v: Int) = update { it.copy(minLevel = v.coerceIn(1, 15)) }

    fun renameDay(uiId: String, name: String) = update { cur ->
        cur.copy(days = cur.days.map { if (it.uiId == uiId) it.copy(name = name) else it })
    }

    fun addExerciseToDay(dayUiId: String, exerciseId: Long) {
        viewModelScope.launch {
            val ex = exerciseRepo.getById(exerciseId) ?: return@launch
            update { cur ->
                cur.copy(days = cur.days.map { day ->
                    if (day.uiId != dayUiId) day
                    else day.copy(exercises = day.exercises + ExerciseDraft(
                        exerciseId = ex.id,
                        exerciseName = ex.name,
                    ))
                })
            }
        }
    }

    fun updateExercise(dayUiId: String, exerciseUiId: String, transform: (ExerciseDraft) -> ExerciseDraft) {
        update { cur ->
            cur.copy(days = cur.days.map { day ->
                if (day.uiId != dayUiId) day
                else day.copy(exercises = day.exercises.map {
                    if (it.uiId == exerciseUiId) transform(it) else it
                })
            })
        }
    }

    fun removeExercise(dayUiId: String, exerciseUiId: String) {
        update { cur ->
            cur.copy(days = cur.days.map { day ->
                if (day.uiId != dayUiId) day
                else day.copy(exercises = day.exercises.filterNot { it.uiId == exerciseUiId })
            })
        }
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank() || s.isSaving) return
        _state.value = s.copy(isSaving = true)

        viewModelScope.launch {
            val plan = WorkoutPlanEntity(
                name = s.name.trim(),
                description = s.description.trim().ifBlank { null },
                minLevel = s.minLevel,
                daysPerWeek = s.daysPerWeek,
                isPreset = false,
            )
            val daysMapped = s.days.mapIndexed { dayIdx, draft ->
                val dayEntity = TrainingDayEntity(
                    planId = 0L,
                    name = draft.name.ifBlank { "День ${'A' + dayIdx}" },
                    orderIndex = dayIdx,
                )
                val exercisesEntities = draft.exercises.mapIndexed { exIdx, ex ->
                    TrainingDayExerciseEntity(
                        dayId = 0L,
                        exerciseId = ex.exerciseId,
                        orderIndex = exIdx,
                        targetSets = ex.sets,
                        targetReps = ex.reps,
                        restSeconds = ex.restSeconds,
                    )
                }
                dayEntity to exercisesEntities
            }
            workoutRepo.createCustomPlan(plan, daysMapped)
            _state.value = s.copy(isSaving = false, saved = true)
        }
    }

    private fun update(transform: (PlanEditorUiState) -> PlanEditorUiState) {
        _state.value = transform(_state.value)
    }

    private fun adjustDays(current: List<DayDraft>, target: Int): List<DayDraft> {
        if (current.size == target) return current
        if (current.size > target) return current.take(target)
        return current + (current.size until target).map { i ->
            DayDraft(name = "День ${'A' + i}")
        }
    }
}
