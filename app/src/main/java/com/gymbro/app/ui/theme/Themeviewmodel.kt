package com.gymbro.app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val repo: ThemePreferencesRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = repo.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,   // нужен до первого рендера
        initialValue = ThemeMode.SYSTEM,
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repo.setThemeMode(mode) }
    }
}