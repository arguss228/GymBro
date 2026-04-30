package com.gymbro.app.ui.splash

import androidx.lifecycle.ViewModel
import com.gymbro.app.data.repository.LevelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val levelRepo: LevelRepository,
) : ViewModel() {

    suspend fun isOnboardingCompleted(): Boolean =
        levelRepo.getProfile().onboardingCompleted
}