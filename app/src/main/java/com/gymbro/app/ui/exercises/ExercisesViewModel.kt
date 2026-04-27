package com.gymbro.app.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.local.entity.ExerciseEntity
import com.gymbro.app.data.repository.ExerciseRepository
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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExercisesUiState(
    val query: String = "",
    val exercises: List<ExerciseEntity> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val repo: ExerciseRepository,
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")

    // Поиск с debounce, чтобы не дёргать БД на каждом символе.
    private val resultsFlow = queryFlow
        .debounce(150)
        .flatMapLatest { q -> repo.search(q) }

    // query в UI обновляется мгновенно, а список — по debounce.
    val state: StateFlow<ExercisesUiState> =
        combine(queryFlow, resultsFlow) { q, list ->
            ExercisesUiState(query = q, exercises = list)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ExercisesUiState(),
        )

    fun setQuery(q: String) {
        queryFlow.value = q
    }

    fun deleteUserExercise(id: Long) {
        viewModelScope.launch { repo.deleteUserExercise(id) }
    }
}
