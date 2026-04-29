package com.gymbro.app.ui.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.repository.ExerciseRepository
import com.gymbro.app.data.repository.ExerciseTechniqueRepository
import com.gymbro.app.domain.model.ExerciseDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseDetailUiState(
    val isLoading: Boolean = true,
    val detail: ExerciseDetail? = null,
    val addedToWorkout: Boolean = false, // заглушка для кнопки
)

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val exerciseRepo: ExerciseRepository,
    private val techniqueRepo: ExerciseTechniqueRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val exerciseId: Long =
        savedStateHandle.get<Long>("exerciseId") ?: 0L

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

    /** Заглушка: в будущем откроет диалог выбора тренировки/плана. */
    fun onAddToWorkout() {
        _state.value = _state.value.copy(addedToWorkout = true)
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _state.value = _state.value.copy(addedToWorkout = false)
        }
    }
}