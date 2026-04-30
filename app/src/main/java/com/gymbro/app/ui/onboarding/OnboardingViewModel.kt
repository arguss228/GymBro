package com.gymbro.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.domain.usecase.OnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingUseCase: OnboardingUseCase,
) : ViewModel() {

    fun submit(
        bench: Double?,
        squat: Double?,
        deadlift: Double?,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            onboardingUseCase(OnboardingUseCase.Params(bench, squat, deadlift))
            onDone()
        }
    }
}