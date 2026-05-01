package com.gymbro.app.ui.rank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.repository.RankRepository
import com.gymbro.app.data.repository.RankState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RankViewModel @Inject constructor(
    private val repo: RankRepository,
) : ViewModel() {

    val state: StateFlow<RankState> = repo.observeRankState().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RankState(),
    )

    fun save1Rm(bench: Double, squat: Double, deadlift: Double) {
        viewModelScope.launch { repo.save1Rm(bench, squat, deadlift) }
    }
}