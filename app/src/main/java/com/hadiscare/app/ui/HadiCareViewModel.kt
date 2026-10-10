package com.hadiscare.app.ui

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hadiscare.app.audio.DefaultAudioGenerator
import com.hadiscare.app.data.PreferencesManager
import com.hadiscare.app.model.AudioSampleInfo
import com.hadiscare.app.model.MonitorSettings
import com.hadiscare.app.model.MonitorStatus
import com.hadiscare.app.model.TriggerEvent
import com.hadiscare.app.service.HadiCareService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class HadiCareViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    val triggerSample: StateFlow<AudioSampleInfo> = preferencesManager.triggerSample
    val responseSample: StateFlow<AudioSampleInfo> = preferencesManager.responseSample
    val settings: StateFlow<MonitorSettings> = preferencesManager.settings
    val triggerHistory: StateFlow<List<TriggerEvent>> = preferencesManager.triggerHistory

    val monitorStatus: StateFlow<MonitorStatus> = HadiCareService.monitorStatus
    val isServiceRunning: StateFlow<Boolean> = HadiCareService.isServiceRunning
    val currentDecibels: StateFlow<Float> = HadiCareService.currentDecibels
    val dominantFrequency: StateFlow<Float> = HadiCareService.dominantFrequency
    val soundClassification: StateFlow<String> = HadiCareService.soundClassification
    val matchConfidence: StateFlow<Float> = HadiCareService.matchConfidence
    val sample1Similarity: StateFlow<Float> = HadiCareService.sample1Similarity
    val triggerCount: StateFlow<Int> = HadiCareService.triggerCount
    val isCalibrating: StateFlow<Boolean> = HadiCareService.isCalibrating
    val calibrationCountdown: StateFlow<Int> = HadiCareService.calibrationCountdown

    private val _playingPreviewId = MutableStateFlow<String?>(null)
    val playingPreviewId: StateFlow<String?> = _playingPreviewId.asStateFlow()

    private var previewMediaPlayer: MediaPlayer? = null

    fun toggleMonitoring(context: android.content.Context) {
        if (isServiceRunning.value) {
            stopMonitoring(context)
        } else {
            startMonitoring(context)
        }
    }

    fun startMonitoring(context: android.content.Context) {
        stopPreview()
        HadiCareService.start(context)
    }

    fun stopMonitoring(context: android.content.Context) {
        HadiCareService.stop(context)
    }

    fun simulateTestTrigger(context: android.content.Context) {
        stopPreview()
        HadiCareService.simulateTrigger(context)
    }

    fun startRoomCalibration(context: android.content.Context) {
        stopPreview()
        HadiCareService.startCalibration(context)
    }

    fun saveAudioFileFromPicker(uri: Uri, isTrigger: Boolean, originalName: String) {
        viewModelScope.launch {
            val fileName = if (isTrigger) "user_trigger_sample.mp3" else "user_response_sample.mp3"
            val savedFile = preferencesManager.saveAudioFromUri(uri, fileName)
            if (savedFile != null && savedFile.exists()) {
                val cleanTitle = originalName.ifBlank {
                    if (isTrigger) "Custom Cry Sample" else "Custom Lullaby"
                }
                if (isTrigger) {
                    preferencesManager.setTriggerSample(
                        title = cleanTitle,
                        filePath = savedFile.absolutePath,
                        durationSec = getAudioDurationSeconds(savedFile),
                        sourceType = "CUSTOM_FILE"
                    )
                } else {
                    preferencesManager.setResponseSample(
                        title = cleanTitle,
                        filePath = savedFile.absolutePath,
                        durationSec = getAudioDurationSeconds(savedFile),
                        sourceType = "CUSTOM_FILE"
                    )
                }
            }
        }
    }

    fun saveRecordedAudio(file: File, isTrigger: Boolean, title: String) {
        viewModelScope.launch {
            val duration = getAudioDurationSeconds(file)
            if (isTrigger) {
                preferencesManager.setTriggerSample(
                    title = title,
                    filePath = file.absolutePath,
                    durationSec = duration,
                    sourceType = "RECORDED"
                )
            } else {
                preferencesManager.setResponseSample(
                    title = title,
                    filePath = file.absolutePath,
                    durationSec = duration,
                    sourceType = "RECORDED"
                )
            }
        }
    }

    fun useDefaultTriggerSample() {
        val (defTrigger, _) = DefaultAudioGenerator.ensureDefaultAudioFiles(getApplication())
        preferencesManager.setTriggerSample(
            title = "Preset: Infant Cry Audio",
            filePath = defTrigger.absolutePath,
            durationSec = 4,
            sourceType = "DEFAULT"
        )
    }

    fun useDefaultResponseSample() {
        val (_, defResponse) = DefaultAudioGenerator.ensureDefaultAudioFiles(getApplication())
        preferencesManager.setResponseSample(
            title = "Preset: Soothing Lullaby",
            filePath = defResponse.absolutePath,
            durationSec = 7,
            sourceType = "DEFAULT"
        )
    }

    fun togglePlayPreview(sampleId: String, filePath: String?) {
        if (_playingPreviewId.value == sampleId) {
            stopPreview()
            return
        }

        if (filePath.isNullOrEmpty() || !File(filePath).exists()) return

        stopPreview()
        try {
            previewMediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    it.release()
                    previewMediaPlayer = null
                    _playingPreviewId.value = null
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    previewMediaPlayer = null
                    _playingPreviewId.value = null
                    true
                }
                start()
            }
            _playingPreviewId.value = sampleId
        } catch (e: Exception) {
            e.printStackTrace()
            _playingPreviewId.value = null
        }
    }

    fun stopPreview() {
        try {
            previewMediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            previewMediaPlayer = null
            _playingPreviewId.value = null
        }
    }

    fun updateThreshold(db: Float) {
        val current = settings.value
        preferencesManager.updateSettings(current.copy(thresholdDb = db))
    }

    fun updateFrequencyFilter(enabled: Boolean) {
        val current = settings.value
        preferencesManager.updateSettings(current.copy(frequencyFilterEnabled = enabled))
    }

    fun updateStrictSample1Matching(enabled: Boolean) {
        val current = settings.value
        preferencesManager.updateSettings(current.copy(strictSample1Matching = enabled))
    }

    fun updateAdultSpeechGuard(enabled: Boolean) {
        val current = settings.value
        preferencesManager.updateSettings(current.copy(adultSpeechGuard = enabled))
    }

    fun updateBabyWhisperDetector(enabled: Boolean) {
        val current = settings.value
        preferencesManager.updateSettings(current.copy(babyWhisperDetector = enabled))
    }

    fun updateTransientNoiseRejector(enabled: Boolean) {
        val current = settings.value
        preferencesManager.updateSettings(current.copy(transientNoiseRejector = enabled))
    }

    fun updateContinuousSoothingLoop(enabled: Boolean) {
        val current = settings.value
        preferencesManager.updateSettings(current.copy(continuousSoothingLoop = enabled))
    }

    fun updateCooldownTimerEnabled(enabled: Boolean) {
        val current = settings.value
        preferencesManager.updateSettings(current.copy(cooldownTimerEnabled = enabled))
    }

    fun updateCooldownDurationSeconds(seconds: Int) {
        val current = settings.value
        preferencesManager.updateSettings(current.copy(cooldownDurationSeconds = seconds))
    }

    fun updateRequiredDuration(durationMs: Int) {
        val current = settings.value
        preferencesManager.updateSettings(current.copy(requiredDurationMs = durationMs))
    }

    fun updateVolume(vol: Float) {
        val current = settings.value
        preferencesManager.updateSettings(current.copy(responseVolume = vol))
    }

    fun clearTriggerHistory() {
        preferencesManager.clearTriggerHistory()
    }

    fun markFirstLaunchComplete() {
        preferencesManager.markFirstLaunchComplete()
    }

    private fun getAudioDurationSeconds(file: File): Int {
        return try {
            val mp = MediaPlayer()
            mp.setDataSource(file.absolutePath)
            mp.prepare()
            val dur = mp.duration / 1000
            mp.release()
            dur.coerceAtLeast(1)
        } catch (e: Exception) {
            3
        }
    }

    override fun onCleared() {
        stopPreview()
        super.onCleared()
    }
}
