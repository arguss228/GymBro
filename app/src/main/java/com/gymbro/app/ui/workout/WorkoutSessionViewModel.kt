package com.gymbro.app.ui.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.local.entity.ExerciseEntity
import com.gymbro.app.data.local.entity.SetLogEntity
import com.gymbro.app.data.local.entity.TrainingDayEntity
import com.gymbro.app.data.local.entity.TrainingDayExerciseEntity
import com.gymbro.app.data.local.entity.WorkoutPlanEntity
import com.gymbro.app.data.repository.ExerciseRepository
import com.gymbro.app.data.repository.ProgressRepository
import com.gymbro.app.data.repository.WorkoutRepository
import com.gymbro.app.domain.usecase.LogSetUseCase
import com.gymbro.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutExerciseUi(
    val planEntry: TrainingDayExerciseEntity,
    val exercise: ExerciseEntity,
    val loggedSets: List<SetLogEntity>,
)

data class WorkoutSessionUiState(
    val sessionId: Long = 0L,
    val activePlan: WorkoutPlanEntity? = null,
    val days: List<TrainingDayEntity> = emptyList(),
    val selectedDayId: Long? = null,
    val exercises: List<WorkoutExerciseUi> = emptyList(),
    val restSecondsRemaining: Int = 0,
    val isLoading: Boolean = true,
    val recentPrMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val exerciseRepo: ExerciseRepository,
    private val progressRepo: ProgressRepository,
    private val logSetUseCase: LogSetUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val sessionId: Long =
        savedStateHandle.get<Long>(Screen.WorkoutSession.ARG_SESSION_ID) ?: 0L

    private val selectedDayIdFlow = MutableStateFlow<Long?>(null)
    private val restFlow = MutableStateFlow(0)
    private val messageFlow = MutableStateFlow<String?>(null)
    private var restJob: Job? = null

    private val _state = MutableStateFlow(WorkoutSessionUiState(sessionId = sessionId))
    val state: StateFlow<WorkoutSessionUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Активный план + его дни (читаем один раз).
            val plan = workoutRepo.observeActivePlan().firstOrNull()
            val days = plan?.let { workoutRepo.observeDays(it.id).firstOrNull() } ?: emptyList()
            selectedDayIdFlow.value = days.firstOrNull()?.id

            // Поток упражнений для выбранного дня — переподписываемся при смене дня.
            val exercisesForDayFlow = selectedDayIdFlow.flatMapLatest { dayId ->
                if (dayId == null) flowOf(emptyList())
                else workoutRepo.observeDayExercises(dayId)
            }

            combine(
                exercisesForDayFlow,
                progressRepo.observeSession(sessionId),
                selectedDayIdFlow,
                restFlow,
                messageFlow,
            ) { entries, logs, dayId, rest, message ->
                val byId = entries
                    .map { it.exerciseId }
                    .distinct()
                    .mapNotNull { exerciseRepo.getById(it) }
                    .associateBy { it.id }

                val exerciseItems = entries.mapNotNull { entry ->
                    val ex = byId[entry.exerciseId] ?: return@mapNotNull null
                    WorkoutExerciseUi(
                        planEntry = entry,
                        exercise = ex,
                        loggedSets = logs.filter { it.exerciseId == ex.id },
                    )
                }

                WorkoutSessionUiState(
                    sessionId = sessionId,
                    activePlan = plan,
                    days = days,
                    selectedDayId = dayId,
                    exercises = exerciseItems,
                    restSecondsRemaining = rest,
                    recentPrMessage = message,
                    isLoading = false,
                )
            }.collect { _state.value = it }
        }
    }

    fun selectDay(dayId: Long) {
        selectedDayIdFlow.value = dayId
    }

    fun logSet(exerciseId: Long, weightKg: Double, reps: Int, restSeconds: Int) {
        if (weightKg <= 0.0 || reps <= 0) return
        viewModelScope.launch {
            val existing = progressRepo.observeSession(sessionId).firstOrNull() ?: emptyList()
            val setNumber = existing.count { it.exerciseId == exerciseId } + 1
            val res = logSetUseCase(
                LogSetUseCase.Params(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    setNumber = setNumber,
                    weightKg = weightKg,
                    reps = reps,
                )
            )
            if (res.newPrTypes.isNotEmpty()) {
                messageFlow.value = "🎉 Новый рекорд!"
                launch {
                    delay(2500)
                    messageFlow.value = null
                }
            }
            startRest(restSeconds)
        }
    }

    fun startRest(seconds: Int) {
        restJob?.cancel()
        restFlow.value = seconds
        restJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
                restFlow.value = remaining
            }
        }
    }

    fun skipRest() {
        restJob?.cancel()
        restFlow.value = 0
    }
}
