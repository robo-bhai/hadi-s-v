package com.example.edit88.model

import java.util.UUID

enum class AspectRatioType(val label: String, val ratio: Float, val widthRatio: Int, val heightRatio: Int, val iconDesc: String) {
    RATIO_9_16("9:16 Reels", 9f / 16f, 9, 16, "TikTok / Reels / Shorts"),
    RATIO_16_9("16:9 Cinema", 16f / 9f, 16, 9, "YouTube / Landscape"),
    RATIO_1_1("1:1 Square", 1f, 1, 1, "Instagram / Post"),
    RATIO_4_5("4:5 Portrait", 4f / 5f, 4, 5, "Social Feed")
}

enum class MediaType {
    VIDEO,
    IMAGE,
    COLOR
}

enum class FilterType(val displayName: String) {
    NONE("Original"),
    CINEMATIC("Cinematic Teal"),
    VIVID("Vivid Pop"),
    CYBERPUNK("Cyber Neon"),
    RETRO("Vintage 90s"),
    MONO("B&W Noir"),
    WARM("Golden Sunset"),
    COLD("Arctic Blue")
}

enum class TransitionType(val displayName: String, val iconEmoji: String) {
    NONE("Direct Cut", "✂"),
    DISSOLVE("Crossfade", "🌫"),
    ZOOM_IN("Zoom In", "🔍"),
    ZOOM_OUT("Zoom Out", "🔭"),
    GLITCH("RGB Glitch", "⚡"),
    FLASH_WHITE("Flash White", "✨"),
    FLASH_BLACK("Fade Black", "🌑"),
    WHIP_LEFT("Whip Left", "👈"),
    WHIP_RIGHT("Whip Right", "👉"),
    LIGHT_LEAK("Film Burn", "🔥")
}

data class ColorGrading(
    val brightness: Float = 0f,    // -1f to 1f
    val contrast: Float = 1f,      // 0.5f to 2f
    val saturation: Float = 1f,    // 0f to 2f
    val exposure: Float = 0f,      // -1f to 1f
    val temperature: Float = 0f,   // -1f (cool) to 1f (warm)
    val vignette: Float = 0f,      // 0f to 1f
    val hue: Float = 0f            // -180f to 180f
)

enum class SpeedCurvePreset(val displayName: String, val points: List<Float>, val description: String) {
    STANDARD("Linear", listOf(1.0f, 1.0f, 1.0f, 1.0f, 1.0f), "Constant linear speed"),
    HERO("Hero Climax", listOf(2.5f, 1.8f, 0.3f, 1.8f, 2.5f), "Fast intro, slow-mo climax, fast outro"),
    BULLET("Bullet Time", listOf(3.0f, 0.2f, 0.2f, 0.2f, 3.0f), "Extreme freeze at the peak beat"),
    MONTAGE("Montage Pulse", listOf(0.5f, 2.2f, 0.5f, 2.2f, 0.5f), "Rhythmic pulse for edits"),
    JUMP_CUT("Jump Speed", listOf(1.0f, 3.5f, 1.0f, 3.5f, 1.0f), "Stepped speed bursts")
}

data class MediaClip(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: MediaType = MediaType.VIDEO,
    val uriString: String? = null,
    val videoWidth: Int = 1080,
    val videoHeight: Int = 1920,
    val durationMs: Long = 5000L,
    val startTrimMs: Long = 0L,
    val endTrimMs: Long = 5000L,
    val speed: Float = 1.0f,
    val speedCurve: SpeedCurvePreset = SpeedCurvePreset.STANDARD,
    val volume: Float = 1.0f,
    val isReversed: Boolean = false,
    val filter: FilterType = FilterType.NONE,
    val colorGrading: ColorGrading = ColorGrading(),
    val visualTheme: String = "Neon City",
    val primaryColorHex: Long = 0xFF3B82F6,
    val secondaryColorHex: Long = 0xFF8B5CF6,
    val transitionToNext: TransitionType = TransitionType.NONE,
    val transitionDurationMs: Long = 500L
) {
    val isLocalVideo: Boolean
        get() = !uriString.isNullOrBlank()

    val effectiveDurationMs: Long
        get() = ((endTrimMs - startTrimMs) / speed).toLong().coerceAtLeast(100L)
}

enum class AudioType(val label: String) {
    BGM("Background Music"),
    SFX("Sound Effect"),
    VOICEOVER("AI Voiceover"),
    MIC_RECORDING("Voice Recording")
}

enum class VoicePreset(val label: String) {
    NORMAL("Natural Voice"),
    ROBOT("Cyborg"),
    CHIPMUNK("High Pitch"),
    DEEP("Deep Bass"),
    RADIO("Lo-Fi Radio"),
    ECHO("Spatial Echo")
}

data class AudioTrack(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: AudioType = AudioType.BGM,
    val uriString: String? = null,
    val isExtracted: Boolean = false,
    val startTimeMs: Long = 0L,
    val durationMs: Long = 10000L,
    val volume: Float = 0.8f,
    val isMuted: Boolean = false,
    val noiseReduction: Boolean = false,
    val voicePreset: VoicePreset = VoicePreset.NORMAL,
    val soundSampleKey: String = "cinematic_beat",
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L
)

enum class UrduFontChoice(val label: String) {
    NOTO_NASTALIQ("Noto Nastaliq (اردو)"),
    GULZAR("Gulzar Nastaliq (گلزار)"),
    SYSTEM_SANS("Modern Sans"),
    SERIF("Classic Serif"),
    IMPACT("Bold Impact"),
    CURSIVE("Script / Cursive")
}

enum class SubtitleStylePreset(val label: String) {
    KARAOKE("Karaoke Word Glow"),
    NEON_GLOW("Neon Cyberpunk"),
    BOXED_SUB("Classic Box Sub"),
    RETRO_STROKE("Bold Outline"),
    MINIMAL("Clean Minimal")
}

data class TimedWord(
    val word: String,
    val startOffsetMs: Long,
    val endOffsetMs: Long
)

data class SubtitleClip(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val fontChoice: UrduFontChoice = UrduFontChoice.NOTO_NASTALIQ,
    val stylePreset: SubtitleStylePreset = SubtitleStylePreset.KARAOKE,
    val fontSizeSp: Float = 22f,
    val textColorHex: Long = 0xFFFFFFFF,
    val activeColorHex: Long = 0xFFFACC15, // Yellow highlight for karaoke
    val outlineColorHex: Long = 0xFF000000,
    val bgColorHex: Long = 0x99000000,
    val isRtl: Boolean = true,
    val words: List<TimedWord> = emptyList(),
    val posX: Float = 0.5f, // Normalized 0f to 1f
    val posY: Float = 0.82f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f
)

data class OverlayClip(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val iconEmoji: String = "✨",
    val uriString: String? = null,
    val isVideo: Boolean = false,
    val startTimeMs: Long = 0L,
    val durationMs: Long = 4000L,
    val posX: Float = 0.5f, // Normalized 0f - 1f
    val posY: Float = 0.35f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val opacity: Float = 0.95f,
    // CapCut Pro Chroma Key (Green Screen Removal)
    val chromaKeyEnabled: Boolean = false,
    val chromaKeyColorHex: Long = 0xFF00FF00, // Default green screen 0x00FF00
    val chromaSimilarity: Float = 0.4f,       // 0.1f to 1.0f
    val chromaSmoothness: Float = 0.1f        // edge blend
)

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "My Edit88 Reel",
    val aspectRatio: AspectRatioType = AspectRatioType.RATIO_9_16,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val clips: List<MediaClip> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList(),
    val subtitles: List<SubtitleClip> = emptyList(),
    val overlays: List<OverlayClip> = emptyList()
) {
    val totalDurationMs: Long
        get() {
            val clipsDuration = clips.sumOf { it.effectiveDurationMs }
            val audioDuration = audioTracks.maxOfOrNull { it.startTimeMs + it.durationMs } ?: 0L
            val subtitleDuration = subtitles.maxOfOrNull { it.endMs } ?: 0L
            return maxOf(clipsDuration, audioDuration, subtitleDuration).coerceAtLeast(3000L)
        }
}

enum class ExportResolution(val label: String, val width: Int, val height: Int) {
    P720("720p HD", 720, 1280),
    P1080("1080p Full HD", 1080, 1920),
    P4K("4K Ultra HD", 2160, 3840)
}

enum class ExportFps(val label: String, val fps: Int) {
    FPS_24("24 fps (Cinema)", 24),
    FPS_30("30 fps (Standard)", 30),
    FPS_60("60 fps (Ultra Smooth)", 60)
}

enum class ExportFormat(val label: String, val extension: String) {
    MP4("MP4 Video", "mp4"),
    MOV("QuickTime MOV", "mov"),
    GIF("Animated GIF", "gif"),
    MP3("MP3 Audio", "mp3")
}

data class ExportConfig(
    val resolution: ExportResolution = ExportResolution.P1080,
    val fps: ExportFps = ExportFps.FPS_30,
    val format: ExportFormat = ExportFormat.MP4,
    val bitrateMbps: Int = 16
)

data class VideoTemplate(
    val id: String,
    val title: String,
    val category: String, // "Urdu Poetry", "Viral Hooks", "Tech Review", "Motivational", "Cinematic"
    val description: String,
    val aspectRatio: AspectRatioType,
    val durationSec: Int,
    val badge: String = "Trending",
    val primaryColorHex: Long = 0xFF8B5CF6,
    val sampleClips: List<MediaClip>,
    val sampleSubtitles: List<SubtitleClip>,
    val sampleAudio: AudioTrack
)

// Subtitle Export Utilities
fun List<SubtitleClip>.toSrtString(): String {
    val sb = StringBuilder()
    this.sortedBy { it.startMs }.forEachIndexed { index, sub ->
        sb.append("${index + 1}\n")
        sb.append("${formatSrtTimestamp(sub.startMs)} --> ${formatSrtTimestamp(sub.endMs)}\n")
        sb.append("${sub.text}\n\n")
    }
    return sb.toString()
}

fun List<SubtitleClip>.toVttString(): String {
    val sb = StringBuilder()
    sb.append("WEBVTT\n\n")
    this.sortedBy { it.startMs }.forEachIndexed { index, sub ->
        sb.append("${index + 1}\n")
        sb.append("${formatVttTimestamp(sub.startMs)} --> ${formatVttTimestamp(sub.endMs)}\n")
        sb.append("${sub.text}\n\n")
    }
    return sb.toString()
}

private fun formatSrtTimestamp(ms: Long): String {
    val hours = ms / 3600000
    val minutes = (ms % 3600000) / 60000
    val seconds = (ms % 60000) / 1000
    val millis = ms % 1000
    return "%02d:%02d:%02d,%03d".format(hours, minutes, seconds, millis)
}

private fun formatVttTimestamp(ms: Long): String {
    val hours = ms / 3600000
    val minutes = (ms % 3600000) / 60000
    val seconds = (ms % 60000) / 1000
    val millis = ms % 1000
    return "%02d:%02d:%02d.%03d".format(hours, minutes, seconds, millis)
}
