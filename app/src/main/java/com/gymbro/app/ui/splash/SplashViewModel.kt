package com.gymbro.app.ui.splash

import androidx.lifecycle.ViewModel
import com.gymbro.app.data.repository.RankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val rankRepo: RankRepository,
) : ViewModel() {
    suspend fun isOnboardingCompleted(): Boolean = rankRepo.isOnboardingDone()
    suspend fun needs1RmEntry(): Boolean         = !rankRepo.hasData()
}