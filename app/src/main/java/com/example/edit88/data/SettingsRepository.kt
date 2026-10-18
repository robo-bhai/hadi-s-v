package com.example.edit88.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("edit88_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_LAST_STATUS = "gemini_last_status"
        private const val KEY_LAST_LATENCY_MS = "gemini_last_latency_ms"
    }

    fun getApiKey(): String {
        val savedKey = prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
        if (savedKey.isNotBlank()) return savedKey

        // Fallback to BuildConfig if present and not placeholder
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_GEMINI_API_KEY, apiKey.trim()).apply()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_GEMINI_API_KEY).apply()
    }

    fun saveTestStatus(status: String, latencyMs: Long) {
        prefs.edit()
            .putString(KEY_LAST_STATUS, status)
            .putLong(KEY_LAST_LATENCY_MS, latencyMs)
            .apply()
    }

    fun getLastStatus(): String = prefs.getString(KEY_LAST_STATUS, "Not verified") ?: "Not verified"
    fun getLastLatencyMs(): Long = prefs.getLong(KEY_LAST_LATENCY_MS, -1L)
}
