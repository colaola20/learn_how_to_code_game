package com.sorych.learn_how_to_code.data

import android.content.ContentValues.TAG
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserProgressRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val WHAT_LEVEL = intPreferencesKey("what_level")
        val WHAT_SCORE = intPreferencesKey("what_score")
    }

    val whatProgress: Flow<GameProgress> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading progress.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { progress ->
            val level = progress[WHAT_LEVEL] ?: 1
            val score = progress[WHAT_SCORE] ?: 0
            Log.d("DataStore", "📖 Reading from DataStore - Level: $level, Score: $score")
            Log.d("DataStore", "📋 All preferences: ${progress.asMap()}")
            GameProgress(level, score)
        }

    suspend fun saveLevelProgress(level: Int) {
        Log.d("DataStore", "💾 Attempting to save level: $level")
        dataStore.edit { progress ->
            progress[WHAT_LEVEL] = level
            Log.d("DataStore", "✍️ Level $level written to preferences")
        }
        Log.d("DataStore", "✅ Level save completed")
    }

    suspend fun saveScoreProgress(score: Int) {
        Log.d("DataStore", "💾 Attempting to save score: $score")
        dataStore.edit { progress ->
            progress[WHAT_SCORE] = score
            Log.d("DataStore", "✍️ Score $score written to preferences")
        }
        Log.d("DataStore", "✅ Score save completed")
    }

}