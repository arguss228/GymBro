package com.obsession.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obsession.app.data.local.entity.ExerciseEntity
import com.obsession.app.data.local.entity.PersonalRecordEntity
import com.obsession.app.data.local.entity.PrType
import com.obsession.app.data.repository.ExerciseRepository
import com.obsession.app.data.repository.ProgressRepository
import com.obsession.app.data.repository.RankRepository
import com.obsession.app.data.repository.RankState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ════════════════════════════════════════════════════════════════
//  UI State
// ════════════════════════════════════════════════════════════════

data class ChartPoint(val timestampMs: Long, val weightKg: Float)
data class PrWithExercise(val pr: PersonalRecordEntity, val exercise: ExerciseEntity?)

enum class ProgressPeriod(val label: String, val days: Int) {
    WEEK("7 дней", 7),
    MONTH("Месяц", 30),
    THREE_MONTHS("3 мес.", 90),
    YEAR("Год", 365),
    ALL("Всё", Int.MAX_VALUE),
}

data class ProgressUiState(
    val rankState: RankState = RankState(),
    val totalSessions: Int = 0,
    // Win Streak
    val winStreak: Int = 0,
    val completedWorkoutDates: Set<String> = emptySet(), // "yyyy-MM-dd"
    // Chart / search
    val selectedExerciseId: Long = -1L,
    val selectedExerciseName: String = "Выберите упражнение",
    val searchQuery: String = "",
    val filteredExercises: List<ExerciseEntity> = emptyList(),
    val selectedPeriod: ProgressPeriod = ProgressPeriod.ALL,
    val chartPoints: List<ChartPoint> = emptyList(),
    val yAxisMin: Float = 0f,
    val yAxisMax: Float = 100f,
    val yAxisLabels: List<Float> = emptyList(),
    val personalRecords: List<PrWithExercise> = emptyList(),
)

// ════════════════════════════════════════════════════════════════
//  ViewModel
// ════════════════════════════════════════════════════════════════

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val exerciseRepo: ExerciseRepository,
    private val progressRepo: ProgressRepository,
    private val rankRepo: RankRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressUiState())
    val state: StateFlow<ProgressUiState> = _state.asStateFlow()

    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init {
        loadRank()
        loadSessions()
        loadExercises()
        observePrForSelected()
    }

    // ── Rank ─────────────────────────────────────────────────────

    private fun loadRank() {
        viewModelScope.launch {
            rankRepo.observeRankState().collect { rankState ->
                _state.value = _state.value.copy(rankState = rankState)
            }
        }
    }

    // ── Sessions → streak + calendar dates ───────────────────────

    private fun loadSessions() {
        viewModelScope.launch {
            progressRepo.observeTotalSessions().collect { total ->
                _state.value = _state.value.copy(totalSessions = total)
            }
        }
    }

    /**
     * Считает текущую серию начиная с сегодня / вчера.
     * Серия сбрасывается если разрыв между двумя тренировками > 6 дней.
     */
    private fun calculateStreak(sortedDates: Set<String>): Int {
        if (sortedDates.isEmpty()) return 0

        val cal = Calendar.getInstance()
        val todayKey = dayKeyFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayKey = dayKeyFormat.format(cal.time)

        // Проверяем, была ли тренировка сегодня или вчера (иначе серия не активна)
        val lastDate = sortedDates.last()
        if (lastDate != todayKey && lastDate != yesterdayKey) return 0

        val sortedList = sortedDates.sorted()
        var streak = 1

        for (i in sortedList.indices.reversed().drop(1)) {
            val current = parseDate(sortedList[i + 1]) ?: break
            val prev    = parseDate(sortedList[i])    ?: break
            val diffDays = ((current.time - prev.time) / (1000L * 60 * 60 * 24)).toInt()

            // Разрыв > 6 дней → серия сбрасывается
            if (diffDays > 6) break
            streak++
        }

        return streak
    }

    private fun parseDate(key: String): Date? = runCatching { dayKeyFormat.parse(key) }.getOrNull()

    // ── Exercises ────────────────────────────────────────────────

    private fun loadExercises() {
        viewModelScope.launch {
            exerciseRepo.observeAll().collect { exercises ->
                val filtered = if (_state.value.searchQuery.isBlank()) exercises
                               else exercises.filter { it.name.contains(_state.value.searchQuery, ignoreCase = true) }
                _state.value = _state.value.copy(filteredExercises = filtered)
            }
        }
    }

    fun setSearchQuery(q: String) {
        _state.value = _state.value.copy(searchQuery = q)
        viewModelScope.launch {
            exerciseRepo.search(q).collect { filtered ->
                _state.value = _state.value.copy(filteredExercises = filtered)
            }
        }
    }

    fun selectExercise(id: Long) {
        viewModelScope.launch {
            val ex = exerciseRepo.getById(id)
            _state.value = _state.value.copy(
                selectedExerciseId = id,
                selectedExerciseName = ex?.name ?: "Упражнение",
                searchQuery = "",
            )
        }
    }

    fun setPeriod(period: ProgressPeriod) {
        _state.value = _state.value.copy(selectedPeriod = period)
    }

    // ── PR chart ─────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePrForSelected() {
        viewModelScope.launch {
            combine(
                _state.map { it.selectedExerciseId }.distinctUntilChanged(),
                _state.map { it.selectedPeriod }.distinctUntilChanged(),
            ) { id, period -> Pair(id, period) }
                .flatMapLatest { (id, period) ->
                    if (id < 0L) flowOf<List<PersonalRecordEntity>>(emptyList())
                    else progressRepo.observePrsForExercise(id)
                        .map { prs ->
                            val cutoff = if (period.days == Int.MAX_VALUE) 0L
                                         else System.currentTimeMillis() - period.days * 24L * 3600_000L
                            prs.filter { it.achievedAt >= cutoff }
                                .sortedBy { it.achievedAt }
                        }
                }
                .collect { prs ->
                    val points = prs.map { ChartPoint(it.achievedAt, it.weightKg.toFloat()) }
                    val min = points.minOfOrNull { it.weightKg }?.minus(5f)?.coerceAtLeast(0f) ?: 0f
                    val max = points.maxOfOrNull { it.weightKg }?.plus(5f) ?: 100f
                    val step = ((max - min) / 5f).coerceAtLeast(1f)
                    val labels = (0..5).map { min + it * step }

                    val exerciseEntity = if (_state.value.selectedExerciseId >= 0)
                        exerciseRepo.getById(_state.value.selectedExerciseId) else null

                    _state.value = _state.value.copy(
                        chartPoints = points,
                        yAxisMin = min,
                        yAxisMax = max,
                        yAxisLabels = labels,
                        personalRecords = prs.sortedByDescending { it.achievedAt }
                            .map { PrWithExercise(it, exerciseEntity) },
                    )
                }
        }
    }
}