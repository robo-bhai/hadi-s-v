package com.example

import com.example.edit88.data.TemplatesRepository
import com.example.edit88.model.AspectRatioType
import com.example.edit88.model.MediaClip
import com.example.edit88.model.Project
import com.example.edit88.model.SubtitleClip
import com.example.edit88.model.TransitionType
import com.example.edit88.model.toSrtString
import com.example.edit88.model.toVttString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testProjectDurationCalculation() {
        val clip1 = MediaClip(title = "Clip 1", durationMs = 4000L, endTrimMs = 4000L)
        val clip2 = MediaClip(title = "Clip 2", durationMs = 6000L, endTrimMs = 6000L)
        val project = Project(clips = listOf(clip1, clip2))

        assertEquals(10000L, project.totalDurationMs)
    }

    @Test
    fun testClipEffectiveDurationWithSpeed() {
        val clip = MediaClip(title = "Fast Clip", startTrimMs = 0L, endTrimMs = 4000L, speed = 2.0f)
        assertEquals(2000L, clip.effectiveDurationMs)
    }

    @Test
    fun testAspectRatioValues() {
        assertEquals(9f / 16f, AspectRatioType.RATIO_9_16.ratio, 0.001f)
        assertEquals(16f / 9f, AspectRatioType.RATIO_16_9.ratio, 0.001f)
        assertEquals(1f, AspectRatioType.RATIO_1_1.ratio, 0.001f)
    }

    @Test
    fun testUrduSubtitleRtlDefault() {
        val sub = SubtitleClip(text = "خوابوں کی تعبیر", startMs = 0L, endMs = 3000L, isRtl = true)
        assertTrue(sub.isRtl)
    }

    @Test
    fun testSrtExportFormatting() {
        val subs = listOf(
            SubtitleClip(text = "Hello Edit88", startMs = 1000L, endMs = 3500L)
        )
        val srt = subs.toSrtString()
        assertTrue(srt.contains("00:00:01,000 --> 00:00:03,500"))
        assertTrue(srt.contains("Hello Edit88"))
    }

    @Test
    fun testVttExportFormatting() {
        val subs = listOf(
            SubtitleClip(text = "Urdu Caption", startMs = 500L, endMs = 2500L)
        )
        val vtt = subs.toVttString()
        assertTrue(vtt.startsWith("WEBVTT"))
        assertTrue(vtt.contains("00:00:00.500 --> 00:00:02.500"))
    }

    @Test
    fun testTransitionAssignment() {
        val clip = MediaClip(
            title = "Scene with Glitch",
            transitionToNext = TransitionType.GLITCH,
            transitionDurationMs = 600L
        )
        assertEquals(TransitionType.GLITCH, clip.transitionToNext)
        assertEquals(600L, clip.transitionDurationMs)
    }

    @Test
    fun testTemplatesRepository() {
        val templates = TemplatesRepository.templates
        assertTrue(templates.isNotEmpty())
        val first = templates.first()
        val proj = TemplatesRepository.createProjectFromTemplate(first)
        assertEquals(first.title, proj.title)
        assertTrue(proj.clips.isNotEmpty())
    }
}
