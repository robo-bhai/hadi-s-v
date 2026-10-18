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

data class ColorGrading(
    val brightness: Float = 0f,    // -1f to 1f
    val contrast: Float = 1f,      // 0.5f to 2f
    val saturation: Float = 1f,    // 0f to 2f
    val temperature: Float = 0f,   // -1f (cool) to 1f (warm)
    val vignette: Float = 0f       // 0f to 1f
)

data class MediaClip(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: MediaType = MediaType.VIDEO,
    val durationMs: Long = 5000L,
    val startTrimMs: Long = 0L,
    val endTrimMs: Long = 5000L,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isReversed: Boolean = false,
    val filter: FilterType = FilterType.NONE,
    val colorGrading: ColorGrading = ColorGrading(),
    val visualTheme: String = "Neon City",
    val primaryColorHex: Long = 0xFF3B82F6,
    val secondaryColorHex: Long = 0xFF8B5CF6
) {
    val effectiveDurationMs: Long
        get() = ((endTrimMs - startTrimMs) / speed).toLong().coerceAtLeast(100L)
}

enum class AudioType(val label: String) {
    BGM("Background Music"),
    SFX("Sound Effect"),
    VOICEOVER("AI Voiceover")
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
    val startTimeMs: Long = 0L,
    val durationMs: Long = 10000L,
    val volume: Float = 0.8f,
    val isMuted: Boolean = false,
    val noiseReduction: Boolean = false,
    val voicePreset: VoicePreset = VoicePreset.NORMAL,
    val soundSampleKey: String = "cinematic_beat"
)

enum class UrduFontChoice(val label: String) {
    NOTO_NASTALIQ("Noto Nastaliq (اردو)"),
    GULZAR("Gulzar Nastaliq (گلزار)"),
    SYSTEM_SANS("Modern Sans")
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
    val words: List<TimedWord> = emptyList()
)

data class OverlayClip(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val iconEmoji: String = "✨",
    val startTimeMs: Long = 0L,
    val durationMs: Long = 4000L,
    val posX: Float = 0.5f, // Normalized 0f - 1f
    val posY: Float = 0.25f,
    val scale: Float = 1.0f,
    val opacity: Float = 0.9f
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
