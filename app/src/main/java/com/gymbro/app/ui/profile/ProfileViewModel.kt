package com.gymbro.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbro.app.data.local.dao.UserProfileDao
import com.gymbro.app.data.local.entity.UserProfileEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val name: String? = null,
    val avatarIndex: Int = 0,
    val weightKg: Double? = null,
    val heightCm: Int? = null,
    val age: Int? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileDao: UserProfileDao,
) : ViewModel() {

    val state: StateFlow<ProfileUiState> = profileDao.observe().map { entity ->
        ProfileUiState(
            name = entity?.name,
            avatarIndex = entity?.avatarIndex ?: 0,
            weightKg = entity?.weightKg,
            heightCm = entity?.heightCm,
            age = entity?.age,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

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
                    name = name.trim().ifBlank { null },
                    avatarIndex = avatarIndex,
                    weightKg = weightKg,
                    heightCm = heightCm,
                    age = age,
                )
            )
        }
    }
}