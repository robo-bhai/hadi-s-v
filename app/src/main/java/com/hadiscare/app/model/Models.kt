package com.hadiscare.app.model

import java.util.UUID

enum class MonitorStatus {
    IDLE,
    LISTENING,
    TRIGGER_DETECTED,
    PLAYING_RESPONSE,
    CALIBRATING
}

enum class NavigationTab(val title: String) {
    HOME("Home"),
    CONTROLS("Controls"),
    HISTORY("History")
}

data class AudioSampleInfo(
    val id: String, // "trigger" or "response"
    val title: String,
    val filePath: String?,
    val isSet: Boolean,
    val durationSec: Int = 0,
    val sourceType: String = "NONE" // "DEFAULT", "CUSTOM_FILE", "RECORDED", "NONE"
)

data class TriggerEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val peakDb: Float = 0f,
    val dominantFrequencyHz: Float = 0f,
    val matchConfidence: Float = 0f,
    val description: String = "Baby cry/voice matched"
)

data class MonitorSettings(
    val thresholdDb: Float = 48f,
    val frequencyFilterEnabled: Boolean = true,
    // Advanced Detection Toggles:
    val strictSample1Matching: Boolean = false,    // Only trigger on exact Sample 1 voice match
    val adultSpeechGuard: Boolean = true,          // Strict block on adult voices & TV
    val babyWhisperDetector: Boolean = true,       // Sensitive to faint weeping & soft whimpering
    val transientNoiseRejector: Boolean = true,    // Blocks claps, door slams, accidental clatter
    // Advanced Playback Toggles:
    val continuousSoothingLoop: Boolean = true,    // Repeats lullaby if baby continues crying
    val cooldownTimerEnabled: Boolean = true,      // Prevents rapid repetitive over-triggering
    val cooldownDurationSeconds: Int = 45,         // Cooldown rest interval in seconds
    val calibratedAmbientDb: Float = 36f,          // Room-calibrated ambient floor
    val requiredDurationMs: Int = 450,             // Responsive detection window
    val responseVolume: Float = 1.0f,
    val isFirstLaunchCompleted: Boolean = false
)
