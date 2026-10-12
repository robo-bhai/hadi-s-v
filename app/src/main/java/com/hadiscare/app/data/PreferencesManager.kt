package com.hadiscare.app.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.hadiscare.app.audio.DefaultAudioGenerator
import com.hadiscare.app.model.AudioSampleInfo
import com.hadiscare.app.model.MonitorSettings
import com.hadiscare.app.model.TriggerEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class PreferencesManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("hadis_care_preferences", Context.MODE_PRIVATE)

    private val _triggerSamples = MutableStateFlow(loadTriggerSamples())
    val triggerSamples: StateFlow<List<AudioSampleInfo>> = _triggerSamples.asStateFlow()

    private val _triggerSample = MutableStateFlow(loadTriggerSample())
    val triggerSample: StateFlow<AudioSampleInfo> = _triggerSample.asStateFlow()

    private val _responseSample = MutableStateFlow(loadResponseSample())
    val responseSample: StateFlow<AudioSampleInfo> = _responseSample.asStateFlow()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<MonitorSettings> = _settings.asStateFlow()

    private val _triggerHistory = MutableStateFlow(loadHistory())
    val triggerHistory: StateFlow<List<TriggerEvent>> = _triggerHistory.asStateFlow()

    private val _isMonitoringActive = MutableStateFlow(prefs.getBoolean(KEY_MONITORING_ACTIVE, false))
    val isMonitoringActive: StateFlow<Boolean> = _isMonitoringActive.asStateFlow()

    init {
        if (!_triggerSample.value.isSet || !_responseSample.value.isSet) {
            setupDefaultsIfEmpty()
        }
    }

    fun setMonitoringActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_MONITORING_ACTIVE, active).apply()
        _isMonitoringActive.value = active
    }

    fun isMonitoringActive(): Boolean {
        return prefs.getBoolean(KEY_MONITORING_ACTIVE, false)
    }

    fun setupDefaultsIfEmpty() {
        val (defTrigger, defResponse) = DefaultAudioGenerator.ensureDefaultAudioFiles(context)
        if (!_triggerSample.value.isSet) {
            val defaultSample = AudioSampleInfo(
                id = "default_trigger",
                title = "Preset: Infant Cry Audio",
                filePath = defTrigger.absolutePath,
                isSet = true,
                durationSec = 4,
                sourceType = "DEFAULT"
            )
            setTriggerSample(
                title = defaultSample.title,
                filePath = defaultSample.filePath!!,
                durationSec = defaultSample.durationSec,
                sourceType = defaultSample.sourceType
            )
            if (_triggerSamples.value.isEmpty()) {
                val list = listOf(defaultSample)
                saveTriggerSamples(list)
                _triggerSamples.value = list
            }
        }
        if (!_responseSample.value.isSet) {
            setResponseSample(
                title = "Preset: Soothing Lullaby",
                filePath = defResponse.absolutePath,
                durationSec = 7,
                sourceType = "DEFAULT"
            )
        }
    }

    fun addTriggerSample(title: String, filePath: String, durationSec: Int, sourceType: String): AudioSampleInfo {
        val newSample = AudioSampleInfo(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            filePath = filePath,
            isSet = true,
            durationSec = durationSec,
            sourceType = sourceType
        )
        val currentList = _triggerSamples.value.toMutableList()
        currentList.add(newSample)
        saveTriggerSamples(currentList)
        _triggerSamples.value = currentList

        // Keep primary triggerSample in sync with first item
        _triggerSample.value = currentList.first()
        savePrimaryTriggerSampleToPrefs(currentList.first())

        return newSample
    }

    fun removeTriggerSample(id: String): Boolean {
        val currentList = _triggerSamples.value.toMutableList()
        val toRemove = currentList.find { it.id == id } ?: return false

        // Delete audio file if it is a user recorded/custom file
        if (toRemove.sourceType == "RECORDED" && toRemove.filePath != null) {
            try {
                val f = File(toRemove.filePath)
                if (f.exists()) f.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        currentList.remove(toRemove)

        // Ensure at least one default sample exists if list is now empty
        if (currentList.isEmpty()) {
            val (defTrigger, _) = DefaultAudioGenerator.ensureDefaultAudioFiles(context)
            currentList.add(
                AudioSampleInfo(
                    id = "default_trigger",
                    title = "Preset: Infant Cry Audio",
                    filePath = defTrigger.absolutePath,
                    isSet = true,
                    durationSec = 4,
                    sourceType = "DEFAULT"
                )
            )
        }

        saveTriggerSamples(currentList)
        _triggerSamples.value = currentList
        _triggerSample.value = currentList.first()
        savePrimaryTriggerSampleToPrefs(currentList.first())
        return true
    }

    private fun savePrimaryTriggerSampleToPrefs(sample: AudioSampleInfo) {
        prefs.edit()
            .putString(KEY_TRIGGER_TITLE, sample.title)
            .putString(KEY_TRIGGER_PATH, sample.filePath)
            .putInt(KEY_TRIGGER_DURATION, sample.durationSec)
            .putString(KEY_TRIGGER_SOURCE, sample.sourceType)
            .putBoolean(KEY_TRIGGER_IS_SET, sample.isSet)
            .apply()
    }

    fun setTriggerSample(title: String, filePath: String, durationSec: Int, sourceType: String) {
        val sample = AudioSampleInfo(
            id = "trigger_main",
            title = title,
            filePath = filePath,
            isSet = true,
            durationSec = durationSec,
            sourceType = sourceType
        )
        savePrimaryTriggerSampleToPrefs(sample)
        _triggerSample.value = sample

        val currentList = _triggerSamples.value.toMutableList()
        if (currentList.isEmpty()) {
            currentList.add(sample)
        } else {
            currentList[0] = sample
        }
        saveTriggerSamples(currentList)
        _triggerSamples.value = currentList
    }

    fun setResponseSample(title: String, filePath: String, durationSec: Int, sourceType: String) {
        prefs.edit()
            .putString(KEY_RESPONSE_TITLE, title)
            .putString(KEY_RESPONSE_PATH, filePath)
            .putInt(KEY_RESPONSE_DURATION, durationSec)
            .putString(KEY_RESPONSE_SOURCE, sourceType)
            .putBoolean(KEY_RESPONSE_IS_SET, true)
            .apply()

        _responseSample.value = AudioSampleInfo(
            id = "response",
            title = title,
            filePath = filePath,
            isSet = true,
            durationSec = durationSec,
            sourceType = sourceType
        )
    }

    fun saveAudioFromUri(uri: Uri, targetFileName: String): File? {
        return try {
            val destination = File(context.filesDir, targetFileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }
            destination
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun updateSettings(newSettings: MonitorSettings) {
        prefs.edit()
            .putFloat(KEY_THRESHOLD_DB, newSettings.thresholdDb)
            .putBoolean(KEY_FREQ_FILTER, newSettings.frequencyFilterEnabled)
            .putBoolean(KEY_STRICT_SAMPLE1, newSettings.strictSample1Matching)
            .putBoolean(KEY_ADULT_GUARD, newSettings.adultSpeechGuard)
            .putBoolean(KEY_BABY_WHISPER, newSettings.babyWhisperDetector)
            .putBoolean(KEY_TRANSIENT_REJECTOR, newSettings.transientNoiseRejector)
            .putBoolean(KEY_CONTINUOUS_LOOP, newSettings.continuousSoothingLoop)
            .putBoolean(KEY_COOLDOWN_ENABLED, newSettings.cooldownTimerEnabled)
            .putInt(KEY_COOLDOWN_SECONDS, newSettings.cooldownDurationSeconds)
            .putFloat(KEY_CALIBRATED_AMBIENT, newSettings.calibratedAmbientDb)
            .putInt(KEY_REQUIRED_DURATION, newSettings.requiredDurationMs)
            .putFloat(KEY_RESPONSE_VOLUME, newSettings.responseVolume)
            .putBoolean(KEY_FIRST_LAUNCH, newSettings.isFirstLaunchCompleted)
            .apply()

        _settings.value = newSettings
    }

    fun markFirstLaunchComplete() {
        val current = _settings.value
        updateSettings(current.copy(isFirstLaunchCompleted = true))
    }

    fun addTriggerEvent(event: TriggerEvent) {
        val currentList = _triggerHistory.value.toMutableList()
        currentList.add(0, event)
        val trimmed = if (currentList.size > 50) currentList.take(50) else currentList
        saveHistory(trimmed)
        _triggerHistory.value = trimmed
    }

    fun clearTriggerHistory() {
        saveHistory(emptyList())
        _triggerHistory.value = emptyList()
    }

    private fun loadTriggerSample(): AudioSampleInfo {
        val isSet = prefs.getBoolean(KEY_TRIGGER_IS_SET, false)
        val path = prefs.getString(KEY_TRIGGER_PATH, null)
        val title = prefs.getString(KEY_TRIGGER_TITLE, "Target Cry / Sound") ?: "Target Cry / Sound"
        val duration = prefs.getInt(KEY_TRIGGER_DURATION, 0)
        val source = prefs.getString(KEY_TRIGGER_SOURCE, "NONE") ?: "NONE"
        return AudioSampleInfo(
            id = "trigger",
            title = title,
            filePath = path,
            isSet = isSet && path != null && File(path).exists(),
            durationSec = duration,
            sourceType = source
        )
    }

    private fun loadResponseSample(): AudioSampleInfo {
        val isSet = prefs.getBoolean(KEY_RESPONSE_IS_SET, false)
        val path = prefs.getString(KEY_RESPONSE_PATH, null)
        val title = prefs.getString(KEY_RESPONSE_TITLE, "Soothing Lullaby") ?: "Soothing Lullaby"
        val duration = prefs.getInt(KEY_RESPONSE_DURATION, 0)
        val source = prefs.getString(KEY_RESPONSE_SOURCE, "NONE") ?: "NONE"
        return AudioSampleInfo(
            id = "response",
            title = title,
            filePath = path,
            isSet = isSet && path != null && File(path).exists(),
            durationSec = duration,
            sourceType = source
        )
    }

    private fun loadSettings(): MonitorSettings {
        return MonitorSettings(
            thresholdDb = prefs.getFloat(KEY_THRESHOLD_DB, 48f),
            frequencyFilterEnabled = prefs.getBoolean(KEY_FREQ_FILTER, true),
            strictSample1Matching = prefs.getBoolean(KEY_STRICT_SAMPLE1, false),
            adultSpeechGuard = prefs.getBoolean(KEY_ADULT_GUARD, true),
            babyWhisperDetector = prefs.getBoolean(KEY_BABY_WHISPER, true),
            transientNoiseRejector = prefs.getBoolean(KEY_TRANSIENT_REJECTOR, true),
            continuousSoothingLoop = prefs.getBoolean(KEY_CONTINUOUS_LOOP, true),
            cooldownTimerEnabled = prefs.getBoolean(KEY_COOLDOWN_ENABLED, true),
            cooldownDurationSeconds = prefs.getInt(KEY_COOLDOWN_SECONDS, 45),
            calibratedAmbientDb = prefs.getFloat(KEY_CALIBRATED_AMBIENT, 36f),
            requiredDurationMs = prefs.getInt(KEY_REQUIRED_DURATION, 450),
            responseVolume = prefs.getFloat(KEY_RESPONSE_VOLUME, 1.0f),
            isFirstLaunchCompleted = prefs.getBoolean(KEY_FIRST_LAUNCH, false)
        )
    }

    private fun saveHistory(list: List<TriggerEvent>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("timestamp", item.timestamp)
            obj.put("peakDb", item.peakDb.toDouble())
            obj.put("freq", item.dominantFrequencyHz.toDouble())
            obj.put("confidence", item.matchConfidence.toDouble())
            obj.put("desc", item.description)
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY_JSON, array.toString()).apply()
    }

    private fun loadHistory(): List<TriggerEvent> {
        val raw = prefs.getString(KEY_HISTORY_JSON, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<TriggerEvent>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TriggerEvent(
                        id = obj.optString("id"),
                        timestamp = obj.optLong("timestamp"),
                        peakDb = obj.optDouble("peakDb", 60.0).toFloat(),
                        dominantFrequencyHz = obj.optDouble("freq", 450.0).toFloat(),
                        matchConfidence = obj.optDouble("confidence", 0.75).toFloat(),
                        description = obj.optString("desc", "Cry detected")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveTriggerSamples(list: List<AudioSampleInfo>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("path", item.filePath)
            obj.put("duration", item.durationSec)
            obj.put("source", item.sourceType)
            obj.put("isSet", item.isSet)
            array.put(obj)
        }
        prefs.edit().putString(KEY_TRIGGER_SAMPLES_JSON, array.toString()).apply()
    }

    private fun loadTriggerSamples(): List<AudioSampleInfo> {
        val raw = prefs.getString(KEY_TRIGGER_SAMPLES_JSON, null)
        if (!raw.isNullOrEmpty()) {
            try {
                val array = JSONArray(raw)
                val list = mutableListOf<AudioSampleInfo>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val path = obj.optString("path", null)
                    list.add(
                        AudioSampleInfo(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            title = obj.optString("title", "Baby Cry Sample"),
                            filePath = path,
                            isSet = obj.optBoolean("isSet", true) && path != null && File(path).exists(),
                            durationSec = obj.optInt("duration", 0),
                            sourceType = obj.optString("source", "RECORDED")
                        )
                    )
                }
                if (list.isNotEmpty()) return list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback: load primary trigger sample as a single-element list
        val primary = loadTriggerSample()
        return if (primary.isSet) listOf(primary) else emptyList()
    }

    companion object {
        const val KEY_MONITORING_ACTIVE = "key_monitoring_active"
        private const val KEY_TRIGGER_SAMPLES_JSON = "trigger_samples_json"
        private const val KEY_TRIGGER_TITLE = "trigger_title"
        private const val KEY_TRIGGER_PATH = "trigger_path"
        private const val KEY_TRIGGER_DURATION = "trigger_duration"
        private const val KEY_TRIGGER_SOURCE = "trigger_source"
        private const val KEY_TRIGGER_IS_SET = "trigger_is_set"

        private const val KEY_RESPONSE_TITLE = "response_title"
        private const val KEY_RESPONSE_PATH = "response_path"
        private const val KEY_RESPONSE_DURATION = "response_duration"
        private const val KEY_RESPONSE_SOURCE = "response_source"
        private const val KEY_RESPONSE_IS_SET = "response_is_set"

        private const val KEY_THRESHOLD_DB = "threshold_db"
        private const val KEY_FREQ_FILTER = "frequency_filter"
        private const val KEY_STRICT_SAMPLE1 = "strict_sample1_matching"
        private const val KEY_ADULT_GUARD = "adult_speech_guard"
        private const val KEY_BABY_WHISPER = "baby_whisper_detector"
        private const val KEY_TRANSIENT_REJECTOR = "transient_noise_rejector"
        private const val KEY_CONTINUOUS_LOOP = "continuous_soothing_loop"
        private const val KEY_COOLDOWN_ENABLED = "cooldown_timer_enabled"
        private const val KEY_COOLDOWN_SECONDS = "cooldown_duration_seconds"
        private const val KEY_CALIBRATED_AMBIENT = "calibrated_ambient_db"
        private const val KEY_REQUIRED_DURATION = "required_duration"
        private const val KEY_RESPONSE_VOLUME = "response_volume"
        private const val KEY_FIRST_LAUNCH = "first_launch_completed"
        private const val KEY_HISTORY_JSON = "trigger_history_json"
    }
}
