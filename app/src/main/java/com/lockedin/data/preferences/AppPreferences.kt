package com.lockedin.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val IS_LOCKED = booleanPreferencesKey("IS_LOCKED")
        val PIN_HASH = stringPreferencesKey("PIN_HASH")
        val PIN_SET = booleanPreferencesKey("PIN_SET")
        val TOTAL_STUDY_MINUTES = intPreferencesKey("TOTAL_STUDY_MINUTES")
        val POMODORO_CYCLES = intPreferencesKey("POMODORO_CYCLES")
        val AI_API_KEY = stringPreferencesKey("AI_API_KEY")
    }

    val isLocked: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_LOCKED]
    }

    val pinHash: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.PIN_HASH]
    }

    val isPinSet: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        prefs[Keys.PIN_SET]
    }

    val totalStudyMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.TOTAL_STUDY_MINUTES] ?: 0
    }

    val pomodoroCycles: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.POMODORO_CYCLES] ?: 0
    }

    val aiApiKey: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.AI_API_KEY]
    }

    suspend fun setLocked(isLocked: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOCKED] = isLocked
        }
    }

    suspend fun setPinHash(hash: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_HASH] = hash
            prefs[Keys.PIN_SET] = true
        }
    }

    suspend fun setTotalStudyMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOTAL_STUDY_MINUTES] = minutes
        }
    }

    suspend fun addStudyMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.TOTAL_STUDY_MINUTES] ?: 0
            prefs[Keys.TOTAL_STUDY_MINUTES] = current + minutes
        }
    }

    suspend fun setPomodoroCycles(cycles: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.POMODORO_CYCLES] = cycles
        }
    }

    suspend fun incrementPomodoroCycles() {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.POMODORO_CYCLES] ?: 0
            prefs[Keys.POMODORO_CYCLES] = current + 1
        }
    }

    suspend fun setAiApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AI_API_KEY] = key
        }
    }
}
