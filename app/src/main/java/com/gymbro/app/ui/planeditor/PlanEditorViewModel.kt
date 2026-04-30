package com.gymbro.app.ui.planeditor

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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** Черновик дня в UI-состоянии. */
data class DayDraft(
    val uiId: String = UUID.randomUUID().toString(),
    /** null — день ещё не сохранён в БД (новый). */
    val dbId: Long? = null,
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
    val isEditMode: Boolean = false,           // true — редактирование существующего плана
    val isLoading: Boolean = false,
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
    /** Индекс дня, для которого показывается диалог подтверждения удаления. */
    val pendingDeleteDayUiId: String? = null,
    /** Пара (dayUiId, exerciseUiId) для диалога подтверждения удаления упражнения. */
    val pendingDeleteExercise: Pair<String, String>? = null,
    /** Пара (dayUiId, exerciseDraft?) для диалога добавления/редактирования упражнения.
     *  null exerciseDraft — режим добавления нового. */
    val exercisePickerDayUiId: String? = null,
)

@HiltViewModel
class PlanEditorViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val exerciseRepo: ExerciseRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val editingPlanId: Long? = savedStateHandle.get<Long>("planId")
        ?.takeIf { it > 0 }

    /** Ссылка на оригинальную сущность плана (нужна при обновлении, чтобы не потерять флаги). */
    private var originalPlan: WorkoutPlanEntity? = null

    private val _state = MutableStateFlow(PlanEditorUiState())
    val state = _state.asStateFlow()

    init {
        if (editingPlanId != null) {
            loadPlanForEditing(editingPlanId)
        }
    }

    // ─── Loading ──────────────────────────────────────────────────────────────

    private fun loadPlanForEditing(planId: Long) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val plan = workoutRepo.getPlan(planId) ?: return@launch
            originalPlan = plan

            val dbDays = workoutRepo.getDaysForPlan(planId)
            val dayDrafts = dbDays.map { day ->
                val exercises = workoutRepo.getExercisesForDay(day.id)
                val exerciseDrafts = exercises.mapNotNull { ex ->
                    val entity = exerciseRepo.getById(ex.exerciseId) ?: return@mapNotNull null
                    ExerciseDraft(
                        exerciseId = ex.exerciseId,
                        exerciseName = entity.name,
                        sets = ex.targetSets,
                        reps = ex.targetReps,
                        restSeconds = ex.restSeconds,
                    )
                }
                DayDraft(
                    dbId = day.id,
                    name = day.name,
                    exercises = exerciseDrafts,
                )
            }

            _state.value = _state.value.copy(
                isEditMode = true,
                isLoading = false,
                name = plan.name,
                description = plan.description ?: "",
                daysPerWeek = plan.daysPerWeek,
                minLevel = plan.minLevel,
                days = dayDrafts,
            )
        }
    }

    // ─── Plan metadata ────────────────────────────────────────────────────────

    fun setName(v: String) = update { it.copy(name = v) }
    fun setDescription(v: String) = update { it.copy(description = v) }
    fun setDaysPerWeek(v: Int) = update { cur ->
        val n = v.coerceIn(3, 6)
        val days = adjustDays(cur.days, n)
        cur.copy(daysPerWeek = n, days = days)
    }
    fun setMinLevel(v: Int) = update { it.copy(minLevel = v.coerceIn(1, 15)) }

    // ─── Day operations ───────────────────────────────────────────────────────

    fun renameDay(uiId: String, name: String) = update { cur ->
        cur.copy(days = cur.days.map { if (it.uiId == uiId) it.copy(name = name) else it })
    }

    fun addDay() = update { cur ->
        val idx = cur.days.size
        val newDays = cur.days + DayDraft(name = "День ${'A' + idx}")
        cur.copy(days = newDays, daysPerWeek = newDays.size.coerceAtMost(6))
    }

    /** Показывает диалог подтверждения удаления дня. */
    fun requestDeleteDay(uiId: String) = update { it.copy(pendingDeleteDayUiId = uiId) }

    fun cancelDeleteDay() = update { it.copy(pendingDeleteDayUiId = null) }

    fun confirmDeleteDay() = update { cur ->
        val filtered = cur.days.filterNot { it.uiId == cur.pendingDeleteDayUiId }
        cur.copy(
            days = filtered,
            daysPerWeek = filtered.size.coerceAtLeast(1),
            pendingDeleteDayUiId = null,
        )
    }

    /**
     * Drag & drop: меняет порядок дней.
     * Вызывается после завершения перетаскивания — передаём новый список целиком.
     */
    fun reorderDays(newDays: List<DayDraft>) = update { it.copy(days = newDays) }

    // ─── Exercise operations ──────────────────────────────────────────────────

    /** Открывает пикер упражнений для указанного дня. */
    fun openExercisePicker(dayUiId: String) =
        update { it.copy(exercisePickerDayUiId = dayUiId) }

    fun closeExercisePicker() = update { it.copy(exercisePickerDayUiId = null) }

    fun addExerciseToDay(dayUiId: String, exerciseId: Long) {
        viewModelScope.launch {
            val ex = exerciseRepo.getById(exerciseId) ?: return@launch
            update { cur ->
                cur.copy(
                    days = cur.days.map { day ->
                        if (day.uiId != dayUiId) day
                        else day.copy(
                            exercises = day.exercises + ExerciseDraft(
                                exerciseId = ex.id,
                                exerciseName = ex.name,
                            )
                        )
                    },
                    exercisePickerDayUiId = null,
                )
            }
        }
    }

    fun updateExercise(
        dayUiId: String,
        exerciseUiId: String,
        transform: (ExerciseDraft) -> ExerciseDraft,
    ) {
        update { cur ->
            cur.copy(days = cur.days.map { day ->
                if (day.uiId != dayUiId) day
                else day.copy(exercises = day.exercises.map {
                    if (it.uiId == exerciseUiId) transform(it) else it
                })
            })
        }
    }

    /** Показывает диалог подтверждения удаления упражнения. */
    fun requestDeleteExercise(dayUiId: String, exerciseUiId: String) =
        update { it.copy(pendingDeleteExercise = dayUiId to exerciseUiId) }

    fun cancelDeleteExercise() = update { it.copy(pendingDeleteExercise = null) }

    fun confirmDeleteExercise() = update { cur ->
        val (dayUiId, exUiId) = cur.pendingDeleteExercise ?: return@update cur
        cur.copy(
            days = cur.days.map { day ->
                if (day.uiId != dayUiId) day
                else day.copy(exercises = day.exercises.filterNot { it.uiId == exUiId })
            },
            pendingDeleteExercise = null,
        )
    }

    /**
     * Drag & drop упражнений внутри дня.
     */
    fun reorderExercises(dayUiId: String, newExercises: List<ExerciseDraft>) = update { cur ->
        cur.copy(days = cur.days.map { day ->
            if (day.uiId != dayUiId) day else day.copy(exercises = newExercises)
        })
    }

    // ─── Save ─────────────────────────────────────────────────────────────────

    fun save() {
        val s = _state.value
        if (s.name.isBlank() || s.isSaving) return
        _state.value = s.copy(isSaving = true)

        viewModelScope.launch {
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

            if (s.isEditMode && editingPlanId != null) {
                // Обновляем существующий план, сохраняя оригинальные флаги (isPreset, isActive и т.д.)
                val updatedPlan = (originalPlan ?: WorkoutPlanEntity(
                    id = editingPlanId,
                    name = s.name.trim(),
                    description = s.description.trim().ifBlank { null },
                    minLevel = s.minLevel,
                    daysPerWeek = s.daysPerWeek,
                    isPreset = false // Или другое значение по умолчанию
                )).copy(
                    name = s.name.trim(),
                    description = s.description.trim().ifBlank { null },
                    minLevel = s.minLevel,
                    daysPerWeek = s.daysPerWeek,
                )
                workoutRepo.updatePlan(updatedPlan, daysMapped)
            } else {
                val plan = WorkoutPlanEntity(
                    name = s.name.trim(),
                    description = s.description.trim().ifBlank { null },
                    minLevel = s.minLevel,
                    daysPerWeek = s.daysPerWeek,
                    isPreset = false,
                )
                workoutRepo.createCustomPlan(plan, daysMapped)
            }

            _state.value = s.copy(isSaving = false, saved = true)
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

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