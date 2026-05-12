package com.obsession.app.ui.splash

import androidx.lifecycle.ViewModel
import com.obsession.app.data.local.dao.UserProfileDao
import com.obsession.app.data.repository.RankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val rankRepo: RankRepository,
    private val profileDao: UserProfileDao,
) : ViewModel() {

    suspend fun isProfileCompleted(): Boolean =
        profileDao.get()?.profileStepCompleted == true

    suspend fun isOnboardingCompleted(): Boolean = rankRepo.isOnboardingDone()
    suspend fun needs1RmEntry(): Boolean = !rankRepo.hasData()
}