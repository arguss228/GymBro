package com.gymbro.app.ui.bodyrank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.local.entity.ExerciseEntity
import com.gymbro.app.data.repository.BodyRankRepository
import com.gymbro.app.data.repository.ExerciseRepository
import com.gymbro.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BodyAnalysisUiState(
    val bodyRank: UserBodyRank? = null,
    val isLoading: Boolean = true,
    // Диалог добавления/редактирования
    val showAddDialog: Boolean = false,
    val dialogExercise: ExerciseEntity? = null,
    val dialogWeightInput: String = "",
    val allExercises: List<ExerciseEntity> = emptyList(),
    val dialogSearchQuery: String = "",
)

@HiltViewModel
class BodyAnalysisViewModel @Inject constructor(
    private val bodyRankRepo: BodyRankRepository,
    private val exerciseRepo: ExerciseRepository,
) : ViewModel() {

    private val _dialogState = MutableStateFlow(
        Triple(false, null as ExerciseEntity?, "")   // show, exercise, weight
    )

    val state: StateFlow<BodyAnalysisUiState> = combine(
        bodyRankRepo.observeUserBodyRank(),
        exerciseRepo.observeAll(),
        _dialogState,
    ) { bodyRank, exercises, (show, ex, weight) ->
        BodyAnalysisUiState(
            bodyRank      = bodyRank,
            isLoading     = false,
            showAddDialog = show,
            dialogExercise = ex,
            dialogWeightInput = weight,
            allExercises  = exercises,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BodyAnalysisUiState())

    fun openAddDialog(exercise: ExerciseEntity? = null) {
        viewModelScope.launch {
            val currentWeight = exercise?.let {
                bodyRankRepo.getMax(it.id)?.toString() ?: ""
            } ?: ""
            _dialogState.value = Triple(true, exercise, currentWeight)
        }
    }

    fun closeDialog() { _dialogState.value = Triple(false, null, "") }

    fun setDialogWeight(w: String) {
        val cur = _dialogState.value
        _dialogState.value = cur.copy(third = w)
    }

    fun setDialogExercise(ex: ExerciseEntity) {
        _dialogState.value = _dialogState.value.copy(second = ex)
    }

    fun saveDialogData() {
        val (_, ex, weightStr) = _dialogState.value
        val kg = weightStr.toDoubleOrNull() ?: return
        val exercise = ex ?: return
        viewModelScope.launch {
            bodyRankRepo.saveMax(exercise.id, kg)
            closeDialog()
        }
    }
}

private fun <A, B, C> Triple<A, B, C>.copy(
    first: A = this.first,
    second: B = this.second,
    third: C = this.third,
) = Triple(first, second, third)