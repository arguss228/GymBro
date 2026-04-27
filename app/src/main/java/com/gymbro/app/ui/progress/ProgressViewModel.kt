package com.gymbro.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.local.entity.ExerciseEntity
import com.gymbro.app.data.local.entity.PersonalRecordEntity
import com.gymbro.app.data.local.entity.SetLogEntity
import com.gymbro.app.data.repository.ExerciseRepository
import com.gymbro.app.data.repository.ProgressRepository
import com.gymbro.app.domain.model.BigThreeLift
import com.gymbro.app.domain.model.StrengthLevel
import com.gymbro.app.domain.usecase.ObserveCurrentLevelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Фильтр по временному периоду для графика прогресса. */
enum class ProgressPeriod(val label: String, val months: Int?) {
    ONE_MONTH("1 мес", 1),
    THREE_MONTHS("3 мес", 3),
    SIX_MONTHS("6 мес", 6),
    ONE_YEAR("1 год", 12),
    ALL_TIME("Всё время", null),
}

data class ProgressUiState(
    val level: StrengthLevel = StrengthLevel.PLACEHOLDER,
    val totalSessions: Int = 0,
    val personalRecords: List<PrWithExercise> = emptyList(),

    // Список всех упражнений (для поиска)
    val allExercises: List<ExerciseEntity> = emptyList(),
    // Отфильтрованные по поисковому запросу
    val filteredExercises: List<ExerciseEntity> = emptyList(),

    val searchQuery: String = "",
    val selectedExerciseId: Long = BigThreeLift.BENCH_PRESS.seedId,
    val selectedExerciseName: String = "",
    val selectedPeriod: ProgressPeriod = ProgressPeriod.SIX_MONTHS,

    /** Точки графика: (timestamp, runningMaxWeight) — только нарастающие максимумы. */
    val chartPoints: List<Pair<Long, Float>> = emptyList(),
)

data class PrWithExercise(
    val pr: PersonalRecordEntity,
    val exerciseName: String,
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val progressRepo: ProgressRepository,
    private val exerciseRepo: ExerciseRepository,
    observeLevel: ObserveCurrentLevelUseCase,
) : ViewModel() {

    private val selectedExerciseIdFlow = MutableStateFlow(BigThreeLift.BENCH_PRESS.seedId)
    private val selectedPeriodFlow = MutableStateFlow(ProgressPeriod.SIX_MONTHS)
    private val searchQueryFlow = MutableStateFlow("")

    /** Поток подходов для выбранного упражнения + выбранного периода. */
    private val historyFlow = combine(
        selectedExerciseIdFlow,
        selectedPeriodFlow,
    ) { id, period ->
        val sinceMillis = period.months
            ?.let { System.currentTimeMillis() - TimeUnit.DAYS.toMillis(it * 30L) }
            ?: 0L
        id to sinceMillis
    }.flatMapLatest { (id, since) ->
        progressRepo.observeExerciseHistorySince(id, since)
    }

    /** Результаты поиска по упражнениям с debounce. */
    private val searchResultsFlow = searchQueryFlow
        .debounce(150)
        .flatMapLatest { q -> exerciseRepo.search(q) }

    val state: StateFlow<ProgressUiState> = combine(
        observeLevel(),
        progressRepo.observeTotalSessions(),
        progressRepo.observeAllPersonalRecords(),
        exerciseRepo.observeAll(),
        combine(
            selectedExerciseIdFlow,
            selectedPeriodFlow,
            searchQueryFlow,
            searchResultsFlow,
            historyFlow,
        ) { exId, period, query, searchResults, history ->
            PartialState(exId, period, query, searchResults, history)
        },
    ) { level, sessions, prs, allExercises, partial ->
        val byId = allExercises.associateBy { it.id }
        val prItems = prs.mapNotNull { pr ->
            byId[pr.exerciseId]?.let { ex -> PrWithExercise(pr, ex.name) }
        }

        // Вычисляем нарастающий максимум (running max) — график никогда не падает.
        val chartPoints = buildRunningMaxChart(partial.history)

        ProgressUiState(
            level = level,
            totalSessions = sessions,
            personalRecords = prItems,
            allExercises = allExercises,
            filteredExercises = if (partial.searchQuery.isBlank()) allExercises else partial.searchResults,
            searchQuery = partial.searchQuery,
            selectedExerciseId = partial.selectedExerciseId,
            selectedExerciseName = byId[partial.selectedExerciseId]?.name ?: "",
            selectedPeriod = partial.selectedPeriod,
            chartPoints = chartPoints,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    fun selectExercise(id: Long) {
        selectedExerciseIdFlow.value = id
        // Сбрасываем поисковый запрос после выбора
        searchQueryFlow.value = ""
    }

    fun setPeriod(period: ProgressPeriod) {
        selectedPeriodFlow.value = period
    }

    fun setSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    /**
     * Строит список точек (timestamp, runningMax) из сырых подходов.
     * Точка добавляется только когда вес превышает текущий исторический максимум.
     * Это обеспечивает «ступенчатый» (step-like) нарастающий график.
     */
    private fun buildRunningMaxChart(sets: List<SetLogEntity>): List<Pair<Long, Float>> {
        if (sets.isEmpty()) return emptyList()

        val result = mutableListOf<Pair<Long, Float>>()
        var runningMax = 0.0

        // Подходы уже отсортированы по времени (ASC) из DAO.
        for (set in sets) {
            if (set.weightKg > runningMax) {
                runningMax = set.weightKg
                result.add(set.performedAt to runningMax.toFloat())
            }
        }
        return result
    }

    // Вспомогательный data class для промежуточного combine.
    private data class PartialState(
        val selectedExerciseId: Long,
        val selectedPeriod: ProgressPeriod,
        val searchQuery: String,
        val searchResults: List<ExerciseEntity>,
        val history: List<SetLogEntity>,
    )
}