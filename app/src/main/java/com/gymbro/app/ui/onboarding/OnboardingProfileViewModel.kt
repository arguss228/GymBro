package com.gymbro.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.local.dao.UserProfileDao
import com.gymbro.app.data.local.entity.UserProfileEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingProfileViewModel @Inject constructor(
    private val profileDao: UserProfileDao,
) : ViewModel() {

    fun saveProfile(
        name: String,
        avatarIndex: Int,
        weightKg: Double?,
        heightCm: Int?,
        age: Int?,
    ) {
        viewModelScope.launch {
            val existing = profileDao.get() ?: UserProfileEntity()
            profileDao.upsert(
                existing.copy(
                    name = name.trim(),
                    avatarIndex = avatarIndex,
                    weightKg = weightKg,
                    heightCm = heightCm,
                    age = age,
                    profileStepCompleted = true,
                )
            )
        }
    }
}