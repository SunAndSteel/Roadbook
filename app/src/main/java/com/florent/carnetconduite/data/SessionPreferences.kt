package com.florent.carnetconduite.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session_prefs")

object SessionPreferences {
    private val ONGOING_SESSION_ID_KEY = longPreferencesKey("ongoing_session_id")

    fun getOngoingSessionId(context: Context): Flow<Long?> =
        context.sessionDataStore.data.map { prefs ->
            prefs[ONGOING_SESSION_ID_KEY]?.takeIf { it > 0 }
        }

    suspend fun saveOngoingSessionId(context: Context, sessionId: Long) {
        context.sessionDataStore.edit { prefs ->
            prefs[ONGOING_SESSION_ID_KEY] = sessionId
        }
    }

    suspend fun clearOngoingSessionId(context: Context) {
        context.sessionDataStore.edit { prefs ->
            prefs[ONGOING_SESSION_ID_KEY] = 0L
        }
    }
}