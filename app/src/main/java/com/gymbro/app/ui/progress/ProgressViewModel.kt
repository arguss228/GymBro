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
import com.gymbro.app.data.repository.RankRepository
import com.gymbro.app.data.repository.RankState
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


enum class ProgressPeriod(val label: String, val months: Int?) {
    ONE_MONTH("1 мес", 1),
    THREE_MONTHS("3 мес", 3),
    SIX_MONTHS("6 мес", 6),
    ONE_YEAR("1 год", 12),
    ALL_TIME("Всё время", null),
}

// Точка на графике: (timestamp, вес в кг)
data class ChartPoint(val timestampMs: Long, val weightKg: Float)

data class ProgressUiState(
    val rankState: RankState = RankState(),
    val level: StrengthLevel = StrengthLevel.PLACEHOLDER,
    val totalSessions: Int = 0,
    val winStreak: Int = 7, // Пока тестовое значение
    val personalRecords: List<PrWithExercise> = emptyList(),
    val allExercises: List<ExerciseEntity> = emptyList(),
    val filteredExercises: List<ExerciseEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedExerciseId: Long = BigThreeLift.BENCH_PRESS.seedId,
    val selectedExerciseName: String = "",
    val selectedPeriod: ProgressPeriod = ProgressPeriod.SIX_MONTHS,
    // График: только PR-точки (максимальный вес, не падает назад)
    val chartPoints: List<ChartPoint> = emptyList(),
    // Мин/макс для оси Y с шагом 5 кг
    val yAxisMin: Float = 0f,
    val yAxisMax: Float = 100f,
    val yAxisLabels: List<Float> = emptyList(),
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
    private val rankRepo: RankRepository,
    observeLevel: ObserveCurrentLevelUseCase,
) : ViewModel() {

    private val selectedExerciseIdFlow = MutableStateFlow(BigThreeLift.BENCH_PRESS.seedId)
    private val selectedPeriodFlow     = MutableStateFlow(ProgressPeriod.SIX_MONTHS)
    private val searchQueryFlow        = MutableStateFlow("")

    // История подходов для выбранного упражнения + периода
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

    // PR-записи для выбранного упражнения
    private val exercisePrsFlow = selectedExerciseIdFlow.flatMapLatest { exId ->
        progressRepo.observeAllPersonalRecords()
            .flatMapLatest { allPrs ->
                kotlinx.coroutines.flow.flowOf(allPrs.filter { it.exerciseId == exId })
            }
    }

    private val searchResultsFlow = searchQueryFlow
        .debounce(150)
        .flatMapLatest { q -> exerciseRepo.search(q) }

    val state: StateFlow<ProgressUiState> = combine(
        combine(
            rankRepo.observeRankState(),
            observeLevel(),
            progressRepo.observeTotalSessions(),
        ) { rankState, level, sessions ->
            Triple(rankState, level, sessions)
        },
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
        exercisePrsFlow,
    ) { meta, allExercises, partial, exercisePrs ->
        val rankState = meta.first
        val level     = meta.second
        val sessions  = meta.third

        val byId = allExercises.associateBy { it.id }

        // Только PR для выбранного упражнения — топ-3 по estimated1Rm
        val prItems = exercisePrs
            .mapNotNull { pr -> byId[pr.exerciseId]?.let { ex -> PrWithExercise(pr, ex.name) } }
            .sortedByDescending { it.pr.estimated1Rm }
            .take(3)

        // Строим «нарастающий максимум» — точки только когда устанавливается новый PR по весу
        val chartPoints = buildPrChart(partial.history)

        // Вычисляем оси Y с шагом 5 кг
        val (yMin, yMax, yLabels) = computeYAxis(chartPoints)

        ProgressUiState(
            rankState            = rankState,
            level                = level,
            totalSessions        = sessions,
            winStreak            = 7, // TODO: реальный подсчёт streak
            personalRecords      = prItems,
            allExercises         = allExercises,
            filteredExercises    = if (partial.searchQuery.isBlank()) allExercises else partial.searchResults,
            searchQuery          = partial.searchQuery,
            selectedExerciseId   = partial.selectedExerciseId,
            selectedExerciseName = byId[partial.selectedExerciseId]?.name ?: "",
            selectedPeriod       = partial.selectedPeriod,
            chartPoints          = chartPoints,
            yAxisMin             = yMin,
            yAxisMax             = yMax,
            yAxisLabels          = yLabels,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    fun selectExercise(id: Long) {
        selectedExerciseIdFlow.value = id
        searchQueryFlow.value = ""
    }

    fun setPeriod(period: ProgressPeriod) {
        selectedPeriodFlow.value = period
    }

    fun setSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    /**
     * Строит список точек, где каждая точка — новый максимальный вес за всё время.
     * Линия графика никогда не идёт вниз.
     */
    private fun buildPrChart(sets: List<SetLogEntity>): List<ChartPoint> {
        if (sets.isEmpty()) return emptyList()
        val result = mutableListOf<ChartPoint>()
        var runningMax = 0.0
        for (set in sets) {
            if (set.weightKg > runningMax) {
                runningMax = set.weightKg
                result.add(ChartPoint(set.performedAt, runningMax.toFloat()))
            }
        }
        return result
    }

    /**
     * Вычисляет параметры оси Y: шаг 5 кг, красиво округлённые границы.
     */
    private fun computeYAxis(points: List<ChartPoint>): Triple<Float, Float, List<Float>> {
        if (points.isEmpty()) return Triple(0f, 100f, (0..100 step 20).map { it.toFloat() })

        val minKg = points.minOf { it.weightKg }
        val maxKg = points.maxOf { it.weightKg }
        val step = 5f
        val padding = 10f // отступ сверху и снизу

        val yMin = (Math.floor(((minKg - padding) / step).toDouble()) * step).toFloat()
            .coerceAtLeast(0f)
        val yMax = (Math.ceil(((maxKg + padding) / step).toDouble()) * step).toFloat()

        val labels = mutableListOf<Float>()
        var cur = yMin
        while (cur <= yMax) {
            labels.add(cur)
            cur += step
        }

        return Triple(yMin, yMax, labels)
    }

    private data class PartialState(
        val selectedExerciseId: Long,
        val selectedPeriod: ProgressPeriod,
        val searchQuery: String,
        val searchResults: List<ExerciseEntity>,
        val history: List<SetLogEntity>,
    )
}