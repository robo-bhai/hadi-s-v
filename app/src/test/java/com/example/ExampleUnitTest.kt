package com.example

import com.example.edit88.model.AspectRatioType
import com.example.edit88.model.MediaClip
import com.example.edit88.model.Project
import com.example.edit88.model.SubtitleClip
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
}
