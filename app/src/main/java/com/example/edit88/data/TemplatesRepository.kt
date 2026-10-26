package com.example.edit88.data

import com.example.edit88.model.AspectRatioType
import com.example.edit88.model.AudioTrack
import com.example.edit88.model.AudioType
import com.example.edit88.model.ColorGrading
import com.example.edit88.model.FilterType
import com.example.edit88.model.MediaClip
import com.example.edit88.model.MediaType
import com.example.edit88.model.Project
import com.example.edit88.model.SubtitleClip
import com.example.edit88.model.SubtitleStylePreset
import com.example.edit88.model.TimedWord
import com.example.edit88.model.TransitionType
import com.example.edit88.model.UrduFontChoice
import com.example.edit88.model.VideoTemplate

object TemplatesRepository {
    val templates: List<VideoTemplate> = listOf(
        VideoTemplate(
            id = "template_urdu_poetry",
            title = "اردو شاعری و جذبات (Urdu Poetry)",
            category = "Urdu Poetry",
            description = "Aesthetic moody reel with Noto Nastaliq word-by-word karaoke glow and ambient lofi soundtrack.",
            aspectRatio = AspectRatioType.RATIO_9_16,
            durationSec = 14,
            badge = "🔥 Most Popular",
            primaryColorHex = 0xFF8B5CF6,
            sampleClips = listOf(
                MediaClip(
                    title = "سفر کا آغاز (The Journey)",
                    visualTheme = "Neon City",
                    durationMs = 4500L,
                    endTrimMs = 4500L,
                    primaryColorHex = 0xFF1E1B4B,
                    secondaryColorHex = 0xFF6D28D9,
                    transitionToNext = TransitionType.DISSOLVE
                ),
                MediaClip(
                    title = "خاموش لمحات (Silent Echoes)",
                    visualTheme = "Mountain Sunrise",
                    durationMs = 5000L,
                    endTrimMs = 5000L,
                    primaryColorHex = 0xFF3B1E08,
                    secondaryColorHex = 0xFF854D0E,
                    filter = FilterType.WARM,
                    transitionToNext = TransitionType.LIGHT_LEAK
                ),
                MediaClip(
                    title = "امید کی کرن (Ray of Hope)",
                    visualTheme = "Cyberpunk Glitch",
                    durationMs = 4500L,
                    endTrimMs = 4500L,
                    primaryColorHex = 0xFF042F2E,
                    secondaryColorHex = 0xFF06B6D4
                )
            ),
            sampleSubtitles = listOf(
                SubtitleClip(
                    text = "ہر خواب کی اپنی ایک قیمت ہے",
                    startMs = 500L,
                    endMs = 4500L,
                    fontChoice = UrduFontChoice.NOTO_NASTALIQ,
                    stylePreset = SubtitleStylePreset.KARAOKE,
                    words = listOf(
                        TimedWord("ہر", 500L, 1200L),
                        TimedWord("خواب کی", 1200L, 2500L),
                        TimedWord("اپنی ایک قیمت ہے", 2500L, 4500L)
                    )
                ),
                SubtitleClip(
                    text = "دل کی بات زباں پر لانا سیکھیں",
                    startMs = 5000L,
                    endMs = 9200L,
                    fontChoice = UrduFontChoice.GULZAR,
                    stylePreset = SubtitleStylePreset.NEON_GLOW,
                    words = listOf(
                        TimedWord("دل کی بات", 5000L, 6800L),
                        TimedWord("زباں پر لانا سیکھیں", 6800L, 9200L)
                    )
                ),
                SubtitleClip(
                    text = "وقت کبھی کسی کا محتاج نہیں ہوتا",
                    startMs = 9600L,
                    endMs = 14000L,
                    fontChoice = UrduFontChoice.NOTO_NASTALIQ,
                    stylePreset = SubtitleStylePreset.KARAOKE,
                    words = listOf(
                        TimedWord("وقت کبھی", 9600L, 11200L),
                        TimedWord("کسی کا محتاج", 11200L, 12800L),
                        TimedWord("نہیں ہوتا!", 12800L, 14000L)
                    )
                )
            ),
            sampleAudio = AudioTrack(
                name = "Lofi Chillhop Urdu Beat",
                type = AudioType.BGM,
                durationMs = 15000L,
                volume = 0.85f
            )
        ),
        VideoTemplate(
            id = "template_viral_hook",
            title = "3-Second Viral Hook",
            category = "Viral Hooks",
            description = "High-retention social media opener designed to maximize watch time and prevent scrolling.",
            aspectRatio = AspectRatioType.RATIO_9_16,
            durationSec = 10,
            badge = "🚀 High Retention",
            primaryColorHex = 0xFFEC4899,
            sampleClips = listOf(
                MediaClip(
                    title = "Attention Grasp Hook",
                    visualTheme = "Tech Studio",
                    durationMs = 3000L,
                    endTrimMs = 3000L,
                    primaryColorHex = 0xFF7F1D1D,
                    secondaryColorHex = 0xFFEF4444,
                    transitionToNext = TransitionType.FLASH_WHITE
                ),
                MediaClip(
                    title = "The Secret Climax",
                    visualTheme = "Neon City",
                    durationMs = 7000L,
                    endTrimMs = 7000L,
                    primaryColorHex = 0xFF14532D,
                    secondaryColorHex = 0xFF22C55E
                )
            ),
            sampleSubtitles = listOf(
                SubtitleClip(
                    text = "STOP SCROLLING! Look at this:",
                    startMs = 0L,
                    endMs = 3000L,
                    fontChoice = UrduFontChoice.SYSTEM_SANS,
                    stylePreset = SubtitleStylePreset.RETRO_STROKE,
                    isRtl = false,
                    words = listOf(
                        TimedWord("STOP", 0L, 800L),
                        TimedWord("SCROLLING!", 800L, 1800L),
                        TimedWord("Look at this:", 1800L, 3000L)
                    )
                ),
                SubtitleClip(
                    text = "یہ ایک راز آپ کی زندگی بدل دے گا",
                    startMs = 3200L,
                    endMs = 9800L,
                    fontChoice = UrduFontChoice.NOTO_NASTALIQ,
                    stylePreset = SubtitleStylePreset.KARAOKE,
                    words = listOf(
                        TimedWord("یہ ایک راز", 3200L, 5500L),
                        TimedWord("آپ کی زندگی بدل دے گا", 5500L, 9800L)
                    )
                )
            ),
            sampleAudio = AudioTrack(
                name = "Cinematic Impact Beat",
                type = AudioType.BGM,
                durationMs = 10000L,
                volume = 0.9f
            )
        ),
        VideoTemplate(
            id = "template_cyberpunk_teaser",
            title = "Cyberpunk 2088 Cinema",
            category = "Cinematic",
            description = "Widescreen 16:9 cinematic teaser with glitch transitions, teal-magenta grading, and synth beats.",
            aspectRatio = AspectRatioType.RATIO_16_9,
            durationSec = 12,
            badge = "🎬 16:9 Cinema",
            primaryColorHex = 0xFF06B6D4,
            sampleClips = listOf(
                MediaClip(
                    title = "Neon Overdrive",
                    visualTheme = "Cyberpunk Glitch",
                    durationMs = 6000L,
                    endTrimMs = 6000L,
                    primaryColorHex = 0xFF0C4A6E,
                    secondaryColorHex = 0xFF0284C7,
                    filter = FilterType.CYBERPUNK,
                    transitionToNext = TransitionType.GLITCH
                ),
                MediaClip(
                    title = "Future City Aerial",
                    visualTheme = "Neon City",
                    durationMs = 6000L,
                    endTrimMs = 6000L,
                    primaryColorHex = 0xFF4A044E,
                    secondaryColorHex = 0xFFA21CAF,
                    filter = FilterType.CYBERPUNK
                )
            ),
            sampleSubtitles = listOf(
                SubtitleClip(
                    text = "Welcome to the Neon Horizon",
                    startMs = 1000L,
                    endMs = 5500L,
                    fontChoice = UrduFontChoice.SYSTEM_SANS,
                    stylePreset = SubtitleStylePreset.NEON_GLOW,
                    isRtl = false
                ),
                SubtitleClip(
                    text = "مستقبل کی دنیا میں خوش آمدید",
                    startMs = 6200L,
                    endMs = 11500L,
                    fontChoice = UrduFontChoice.GULZAR,
                    stylePreset = SubtitleStylePreset.NEON_GLOW,
                    isRtl = true
                )
            ),
            sampleAudio = AudioTrack(
                name = "Cyberpunk Synthwave",
                type = AudioType.BGM,
                durationMs = 12000L,
                volume = 0.85f
            )
        )
    )

    fun createProjectFromTemplate(template: VideoTemplate): Project {
        return Project(
            title = template.title,
            aspectRatio = template.aspectRatio,
            clips = template.sampleClips,
            subtitles = template.sampleSubtitles,
            audioTracks = listOf(template.sampleAudio)
        )
    }
}
