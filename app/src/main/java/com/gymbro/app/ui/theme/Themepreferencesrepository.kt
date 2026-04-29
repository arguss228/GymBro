package com.gymbro.app.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Режим темы приложения. */
enum class ThemeMode(val label: String) {
    LIGHT("Светлая"),
    DARK("Тёмная"),
    SYSTEM("По системе"),
}

private val Context.themeDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "gymbro_theme_prefs")

@Singleton
class ThemePreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val KEY_THEME = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { prefs ->
        val raw = prefs[KEY_THEME] ?: ThemeMode.SYSTEM.name
        ThemeMode.values().firstOrNull { it.name == raw } ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME] = mode.name
        }
    }
}