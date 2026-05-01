package com.gymbro.app.ui.splash

import androidx.lifecycle.ViewModel
import com.gymbro.app.data.repository.LevelRepository
import com.gymbro.app.data.repository.RankRepository   // ← добавь этот импорт
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val levelRepo: LevelRepository,
    private val rankRepo: RankRepository     // ← добавляем зависимость
) : ViewModel() {
    suspend fun isOnboardingCompleted(): Boolean =
        levelRepo.getProfile().onboardingCompleted
    suspend fun needsOneRmEntry(): Boolean = 
        !rankRepo.hasData()
}