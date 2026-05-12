package com.obsession.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obsession.app.data.local.dao.UserProfileDao
import com.obsession.app.data.local.entity.UserProfileEntity
import com.obsession.app.domain.goals.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
    private val goalRepo: GoalRepository,           // ИСПРАВЛЕНИЕ: проверка целей при смене веса
) : ViewModel() {

    val state: StateFlow<ProfileUiState> = profileDao.observe().map { entity ->
        ProfileUiState(
            name        = entity?.name,
            avatarIndex = entity?.avatarIndex ?: 0,
            weightKg    = entity?.weightKg,
            heightCm    = entity?.heightCm,
            age         = entity?.age,
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
                    name        = name.trim().ifBlank { null },
                    avatarIndex = avatarIndex,
                    weightKg    = weightKg,
                    heightCm    = heightCm,
                    age         = age,
                )
            )

            // ИСПРАВЛЕНИЕ: при изменении веса проверяем цели по весу тела
            if (weightKg != null) {
                goalRepo.checkAndUpdateGoals(currentWeightKg = weightKg)
            }
        }
    }
}