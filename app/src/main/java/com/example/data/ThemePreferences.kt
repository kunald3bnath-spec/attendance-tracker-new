package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_prefs")

class ThemePreferences(private val context: Context) {
    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    }

    val isDarkTheme: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[DARK_THEME_KEY]
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] ?: false
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = enabled
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }
}
