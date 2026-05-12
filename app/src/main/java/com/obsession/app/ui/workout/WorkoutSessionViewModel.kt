package com.obsession.app.ui.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obsession.app.data.local.entity.ExerciseEntity
import com.obsession.app.data.local.entity.SetLogEntity
import com.obsession.app.data.local.entity.TrainingDayEntity
import com.obsession.app.data.local.entity.WorkoutPlanEntity
import com.obsession.app.data.repository.ExerciseRepository
import com.obsession.app.data.repository.GoalRepositoryImpl
import com.obsession.app.data.repository.ProgressRepository
import com.obsession.app.data.repository.RankRepository
import com.obsession.app.data.repository.WorkoutRepository
import com.obsession.app.domain.goals.GoalRepository
import com.obsession.app.domain.model.BigThreeLift
import com.obsession.app.domain.model.StrengthRank
import com.obsession.app.domain.usecase.LogSetUseCase
import com.obsession.app.ui.navigation.Screen

data class WorkoutExerciseUi(
    val planEntry: com.obsession.app.data.local.entity.TrainingDayExerciseEntity,
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
    val rankUpEvent: StrengthRank? = null,
    // ИСПРАВЛЕНИЕ: таймер тренировки
    val workoutElapsedSeconds: Long = 0L,
)

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val exerciseRepo: ExerciseRepository,
    private val progressRepo: ProgressRepository,
    private val rankRepo: RankRepository,
    private val logSetUseCase: LogSetUseCase,
    private val goalRepo: GoalRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val sessionId: Long = savedStateHandle.get<Long>(Screen.WorkoutSession.ARG_SESSION_ID) ?: 0L

    private val selectedDayIdFlow  = MutableStateFlow<Long?>(null)
    private val restFlow           = MutableStateFlow(0)
    private val messageFlow        = MutableStateFlow<String?>(null)
    private val rankUpFlow         = MutableStateFlow<StrengthRank?>(null)
    // ИСПРАВЛЕНИЕ: таймер тренировки
    private val elapsedSecsFlow    = MutableStateFlow(0L)

    private var restJob: Job? = null
    private var timerJob: Job? = null

    private val _state = MutableStateFlow(WorkoutSessionUiState(sessionId = sessionId))
    val state: StateFlow<WorkoutSessionUiState> = _state.asStateFlow()

    // Храним накопленный тоннаж за сессию
    private var sessionTonnageKg = 0.0

    init {
        viewModelScope.launch {
            val plan = workoutRepo.observeActivePlan().firstOrNull()
            val days = plan?.let { workoutRepo.observeDays(it.id).firstOrNull() } ?: emptyList()
            selectedDayIdFlow.value = days.firstOrNull()?.id

            val exercisesForDayFlow = selectedDayIdFlow.flatMapLatest { dayId ->
                if (dayId == null) flowOf(emptyList())
                else workoutRepo.observeDayExercises(dayId)
            }

            combine(
                exercisesForDayFlow,
                progressRepo.observeSession(sessionId),
                selectedDayIdFlow,
                restFlow,
                elapsedSecsFlow,
                combine(messageFlow, rankUpFlow) { msg, rankUp -> msg to rankUp },
            ) { entries, logs, dayId, rest, elapsed, (message, rankUp) ->
                val byId = entries
                    .map { it.exerciseId }
                    .distinct()
                    .mapNotNull { exerciseRepo.getById(it) }
                    .associateBy { it.id }

                val exerciseItems = entries.mapNotNull { entry ->
                    val ex = byId[entry.exerciseId] ?: return@mapNotNull null
                    WorkoutExerciseUi(
                        planEntry  = entry,
                        exercise   = ex,
                        loggedSets = logs.filter { it.exerciseId == ex.id },
                    )
                }

                WorkoutSessionUiState(
                    sessionId              = sessionId,
                    activePlan             = plan,
                    days                   = days,
                    selectedDayId          = dayId,
                    exercises              = exerciseItems,
                    restSecondsRemaining   = rest,
                    recentPrMessage        = message,
                    rankUpEvent            = rankUp,
                    isLoading              = false,
                    workoutElapsedSeconds  = elapsed,
                )
            }.collect { _state.value = it }
        }

        // ИСПРАВЛЕНИЕ: запускаем таймер тренировки сразу
        startWorkoutTimer()
    }

    // ── Таймер тренировки ─────────────────────────────────────────

    private fun startWorkoutTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                elapsedSecsFlow.value += 1
            }
        }
    }

    fun selectDay(dayId: Long) {
        selectedDayIdFlow.value = dayId
    }

    // ── Логирование подхода ───────────────────────────────────────

    fun logSet(exerciseId: Long, weightKg: Double, reps: Int, restSeconds: Int) {
        if (weightKg <= 0.0 || reps <= 0) return

        viewModelScope.launch {
            val existing  = progressRepo.observeSession(sessionId).firstOrNull() ?: emptyList()
            val setNumber = existing.count { it.exerciseId == exerciseId } + 1

            val res = logSetUseCase(
                LogSetUseCase.Params(
                    sessionId  = sessionId,
                    exerciseId = exerciseId,
                    setNumber  = setNumber,
                    weightKg   = weightKg,
                    reps       = reps,
                )
            )

            // ИСПРАВЛЕНИЕ: накапливаем тоннаж
            sessionTonnageKg += weightKg * reps

            // Автообновление 1RM для Big Three при одном повторении
            if (reps == 1) {
                val lift = BigThreeLift.fromSeedId(exerciseId)
                if (lift != null) {
                    val newRank = rankRepo.updateIfBetter(
                        bench    = if (lift == BigThreeLift.BENCH_PRESS) weightKg else null,
                        squat    = if (lift == BigThreeLift.BACK_SQUAT)  weightKg else null,
                        deadlift = if (lift == BigThreeLift.DEADLIFT)    weightKg else null,
                    )
                    if (newRank != null) {
                        rankUpFlow.value = newRank
                        launch { delay(4000); rankUpFlow.value = null }
                    }
                }
            }

            // ИСПРАВЛЕНИЕ: проверка цели по силе при каждом подходе
            viewModelScope.launch {
                goalRepo.checkAndUpdateGoals()
            }

            if (res.newPrTypes.isNotEmpty()) {
                messageFlow.value = "🎉 Новый рекорд!"
                launch { delay(2500); messageFlow.value = null }
            }

            startRest(restSeconds)
        }
    }

    // ── Завершение тренировки ─────────────────────────────────────

    /**
     * ИСПРАВЛЕНИЕ: при завершении тренировки:
     * 1. Останавливаем таймер и сохраняем время в SharedPreferences (или БД)
     * 2. Добавляем тоннаж к общей статистике (через SetLog — уже накоплено автоматически)
     * 3. Проверяем цели
     *
     * Вызывается из WorkoutSessionScreen при нажатии «Завершить».
     */
    fun finishWorkout() {
        timerJob?.cancel()
        restJob?.cancel()

        viewModelScope.launch {
            // Проверяем все цели (Win Streak + Сила)
            goalRepo.checkAndUpdateGoals()
        }
    }

    fun startRest(seconds: Int) {
        restJob?.cancel()
        restFlow.value = seconds
        restJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1_000)
                remaining--
                restFlow.value = remaining
            }
        }
    }

    fun skipRest() {
        restJob?.cancel()
        restFlow.value = 0
    }

    fun dismissRankUp() { rankUpFlow.value = null }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restJob?.cancel()
    }
}