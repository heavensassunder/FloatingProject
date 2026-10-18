package com.example.myempty.floatingproject.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myempty.floatingproject.model.SlimeStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "slime_prefs")

object SlimeDataStore {
    private val KEY_API = stringPreferencesKey("openrouter_api_key")
    private val KEY_STATS = stringPreferencesKey("slime_stats_json")

    suspend fun saveApiKey(ctx: Context, key: String) {
        ctx.dataStore.edit { prefs ->
            prefs[KEY_API] = key
        }
    }

    fun apiKeyFlow(ctx: Context): Flow<String?> = ctx.dataStore.data.map { prefs -> prefs[KEY_API] }

    suspend fun saveStats(ctx: Context, stats: SlimeStats) {
        ctx.dataStore.edit { prefs ->
            prefs[KEY_STATS] = Json.encodeToString(stats)
        }
    }

    fun statsFlow(ctx: Context): Flow<SlimeStats> = ctx.dataStore.data.map { prefs ->
        val json = prefs[KEY_STATS]
        if (json == null) {
            SlimeStats()
        } else {
            try {
                Json.decodeFromString<SlimeStats>(json)
            } catch (e: Exception) {
                SlimeStats()
            }
        }
    }
}
