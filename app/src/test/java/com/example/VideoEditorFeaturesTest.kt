package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.edit88.model.AspectRatioType
import com.example.edit88.model.AudioType
import com.example.edit88.model.MediaClip
import com.example.edit88.model.UrduFontChoice
import com.example.edit88.viewmodel.VideoEditorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VideoEditorFeaturesTest {

    @Test
    fun testImportVideoHasNoDefaultSubtitles() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = VideoEditorViewModel(app)

        val importedClips = listOf(
            MediaClip(
                title = "Device Video 1",
                uriString = "content://media/external/video/media/100",
                durationMs = 8000L,
                endTrimMs = 8000L
            )
        )

        viewModel.createProjectFromImportedClips(importedClips, AspectRatioType.RATIO_9_16)
        val state = viewModel.uiState.value

        // Requirement 1: Remove automatic default subtitle generation upon video import
        assertTrue("Subtitles must be empty upon video import", state.project.subtitles.isEmpty())
        assertEquals(1, state.project.clips.size)
        assertEquals("Device Video 1", state.project.clips.first().title)
    }

    @Test
    fun testManualGenerateSubtitles() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = VideoEditorViewModel(app)

        // Clear subtitles first
        viewModel.createProjectFromImportedClips(
            listOf(MediaClip(title = "Video Clip", durationMs = 12000L, endTrimMs = 12000L))
        )
        assertTrue(viewModel.uiState.value.project.subtitles.isEmpty())

        // Requirement 1: Manual trigger for subtitles
        viewModel.generateSubtitlesForProject()
        val subs = viewModel.uiState.value.project.subtitles
        assertTrue("Subtitles should be generated manually", subs.isNotEmpty())
        assertEquals(UrduFontChoice.NOTO_NASTALIQ, subs.first().fontChoice)
    }

    @Test
    fun testTextOverlayManagementAndDurationTrimming() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = VideoEditorViewModel(app)

        viewModel.addTextOverlay(
            text = "Custom Title",
            fontChoice = UrduFontChoice.GULZAR,
            fontSizeSp = 28f
        )

        val subId = viewModel.uiState.value.selectedSubtitleId
        assertNotNull("Added text should be selected", subId)

        val addedSub = viewModel.uiState.value.project.subtitles.firstOrNull { it.id == subId }
        assertNotNull(addedSub)
        assertEquals("Custom Title", addedSub?.text)
        assertEquals(UrduFontChoice.GULZAR, addedSub?.fontChoice)

        // Test timeline duration trimming
        viewModel.updateSubtitleDuration(subId!!, startMs = 500L, endMs = 4500L)
        val trimmed = viewModel.uiState.value.project.subtitles.first { it.id == subId }
        assertEquals(500L, trimmed.startMs)
        assertEquals(4500L, trimmed.endMs)

        // Test delete text
        viewModel.deleteSubtitle(subId)
        val exists = viewModel.uiState.value.project.subtitles.any { it.id == subId }
        assertTrue("Subtitle should be deleted", !exists)
    }

    @Test
    fun testAudioExtractionAndDeviceMusic() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = VideoEditorViewModel(app)

        val importedClips = listOf(
            MediaClip(
                title = "My Reel Video",
                uriString = "content://media/external/video/media/200",
                durationMs = 6000L,
                endTrimMs = 6000L
            )
        )
        viewModel.createProjectFromImportedClips(importedClips)

        // Requirement 3: Extract audio from video
        viewModel.extractAudioFromClip()
        val audioTracks = viewModel.uiState.value.project.audioTracks
        val extracted = audioTracks.firstOrNull { it.isExtracted }
        assertNotNull("Extracted audio track should be present", extracted)
        assertEquals("Extracted: My Reel Video", extracted?.name)
        assertEquals("content://media/external/video/media/200", extracted?.uriString)

        // Requirement 3: Add new music track from local storage
        viewModel.addLocalAudioTrack(
            name = "My Song.mp3",
            uriString = "content://media/external/audio/media/300",
            durationMs = 30000L
        )
        val deviceTrack = viewModel.uiState.value.project.audioTracks.firstOrNull { it.name == "My Song.mp3" }
        assertNotNull("Device music track should be added", deviceTrack)
        assertEquals(AudioType.BGM, deviceTrack?.type)
        assertEquals(30000L, deviceTrack?.durationMs)
    }
}
