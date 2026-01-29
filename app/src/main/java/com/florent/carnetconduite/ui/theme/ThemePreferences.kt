package com.florent.carnetconduite.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_prefs")

object ThemePreferences {

    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

    fun getThemeMode(context: Context): Flow<ThemeMode> =
        context.dataStore.data.map { prefs ->
            ThemeMode.valueOf(
                prefs[THEME_MODE_KEY] ?: ThemeMode.DYNAMIC.name
            )
        }

    suspend fun saveThemeMode(context: Context, themeMode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = themeMode.name
        }
    }
}