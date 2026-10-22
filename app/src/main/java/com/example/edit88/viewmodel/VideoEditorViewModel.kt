package com.example.edit88.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.edit88.ai.GeminiService
import com.example.edit88.data.SettingsRepository
import com.example.edit88.model.AspectRatioType
import com.example.edit88.model.AudioTrack
import com.example.edit88.model.AudioType
import com.example.edit88.model.ColorGrading
import com.example.edit88.model.FilterType
import com.example.edit88.model.MediaClip
import com.example.edit88.model.MediaType
import com.example.edit88.model.OverlayClip
import com.example.edit88.model.Project
import com.example.edit88.model.SubtitleClip
import com.example.edit88.data.TemplatesRepository
import com.example.edit88.model.SpeedCurvePreset
import com.example.edit88.model.SubtitleStylePreset
import com.example.edit88.model.TimedWord
import com.example.edit88.model.TransitionType
import com.example.edit88.model.UrduFontChoice
import com.example.edit88.model.VideoTemplate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Stack

enum class SheetType {
    NONE,
    SETTINGS,
    SUBTITLES,
    AI_STUDIO,
    AUDIO,
    FILTERS,
    EXPORT,
    CLIP_EDIT,
    TRANSITIONS,
    STICKERS,
    ADD_MEDIA,
    SRT_EXPORT,
    TEXT_EDIT,
    OVERLAY_PIP
}

data class EditorUiState(
    val project: Project = Project(),
    val currentPlayheadMs: Long = 0L,
    val isPlaying: Boolean = false,
    val selectedClipId: String? = null,
    val selectedSubtitleId: String? = null,
    val selectedOverlayId: String? = null,
    val selectedAudioTrackId: String? = null,
    val activeSheet: SheetType = SheetType.NONE,
    val recentProjects: List<Project> = emptyList(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val timelineZoom: Float = 1.0f
) {
    val hasSelection: Boolean
        get() = selectedClipId != null || selectedSubtitleId != null || selectedOverlayId != null || selectedAudioTrackId != null

    val selectedTypeLabel: String
        get() = when {
            selectedAudioTrackId != null -> "Audio"
            selectedSubtitleId != null -> "Text"
            selectedOverlayId != null -> "Overlay"
            selectedClipId != null -> "Clip"
            else -> "Item"
        }
}

class VideoEditorViewModel(application: Application) : AndroidViewModel(application) {
    val settingsRepository = SettingsRepository(application)
    val geminiService = GeminiService()

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private val undoStack = Stack<Project>()
    private val redoStack = Stack<Project>()

    init {
        // Initialize with a rich default CapCut Pro style project
        val defaultProject = createInitialSampleProject()
        _uiState.update {
            it.copy(
                project = defaultProject,
                selectedClipId = defaultProject.clips.firstOrNull()?.id,
                recentProjects = listOf(
                    defaultProject,
                    createCyberpunkSampleProject(),
                    createTechReviewProject()
                )
            )
        }
    }

    private fun pushHistory() {
        undoStack.push(_uiState.value.project)
        redoStack.clear()
        updateHistoryFlags()
    }

    private fun updateHistoryFlags() {
        _uiState.update {
            it.copy(canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty())
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.pop()
            redoStack.push(_uiState.value.project)
            _uiState.update { it.copy(project = prev) }
            updateHistoryFlags()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.pop()
            undoStack.push(_uiState.value.project)
            _uiState.update { it.copy(project = next) }
            updateHistoryFlags()
        }
    }

    fun togglePlayPause() {
        val currentlyPlaying = _uiState.value.isPlaying
        if (currentlyPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = true) }
        playbackJob = viewModelScope.launch {
            while (isActive) {
                delay(40) // ~25 fps playback step
                val totalMs = _uiState.value.project.totalDurationMs
                val next = _uiState.value.currentPlayheadMs + 40L
                if (next >= totalMs) {
                    _uiState.update { it.copy(currentPlayheadMs = 0L, isPlaying = false) }
                    break
                } else {
                    _uiState.update { it.copy(currentPlayheadMs = next) }
                }
            }
        }
    }

    fun pause() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun seekTo(timeMs: Long) {
        val bounded = timeMs.coerceIn(0L, _uiState.value.project.totalDurationMs)
        _uiState.update { it.copy(currentPlayheadMs = bounded) }
    }

    fun selectClip(id: String?) {
        _uiState.update {
            it.copy(
                selectedClipId = id,
                selectedSubtitleId = if (id != null) null else it.selectedSubtitleId,
                selectedOverlayId = if (id != null) null else it.selectedOverlayId,
                selectedAudioTrackId = if (id != null) null else it.selectedAudioTrackId
            )
        }
    }

    fun selectAudioTrack(id: String?) {
        _uiState.update {
            it.copy(
                selectedAudioTrackId = id,
                selectedClipId = if (id != null) null else it.selectedClipId,
                selectedSubtitleId = if (id != null) null else it.selectedSubtitleId,
                selectedOverlayId = if (id != null) null else it.selectedOverlayId
            )
        }
    }

    fun deselectAll() {
        _uiState.update {
            it.copy(
                selectedClipId = null,
                selectedSubtitleId = null,
                selectedOverlayId = null,
                selectedAudioTrackId = null
            )
        }
    }

    fun deleteSelected() {
        val state = _uiState.value
        when {
            state.selectedAudioTrackId != null -> {
                deleteAudioTrack(state.selectedAudioTrackId)
            }
            state.selectedSubtitleId != null -> {
                deleteSubtitle(state.selectedSubtitleId)
            }
            state.selectedOverlayId != null -> {
                deleteOverlay(state.selectedOverlayId)
            }
            state.selectedClipId != null -> {
                deleteSelectedClip()
            }
            state.project.clips.isNotEmpty() -> {
                val first = state.project.clips.firstOrNull()
                if (first != null) {
                    _uiState.update { it.copy(selectedClipId = first.id) }
                    deleteSelectedClip()
                }
            }
        }
    }

    fun reorderClips(fromIndex: Int, toIndex: Int) {
        val currentClips = _uiState.value.project.clips
        if (fromIndex !in currentClips.indices || toIndex !in currentClips.indices || fromIndex == toIndex) return
        pushHistory()
        val mutable = currentClips.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        _uiState.update {
            it.copy(
                project = it.project.copy(clips = mutable, updatedAt = System.currentTimeMillis()),
                selectedClipId = item.id
            )
        }
    }

    fun moveClip(clipId: String, deltaIndices: Int) {
        val currentClips = _uiState.value.project.clips
        val idx = currentClips.indexOfFirst { it.id == clipId }
        if (idx == -1) return
        val targetIdx = (idx + deltaIndices).coerceIn(0, currentClips.size - 1)
        if (idx != targetIdx) {
            reorderClips(idx, targetIdx)
        }
    }

    fun moveAudioTrack(trackId: String, newStartTimeMs: Long) {
        val validStart = newStartTimeMs.coerceAtLeast(0L)
        val updated = _uiState.value.project.audioTracks.map {
            if (it.id == trackId) it.copy(startTimeMs = validStart) else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(audioTracks = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun moveSubtitle(subtitleId: String, newStartMs: Long) {
        val validStart = newStartMs.coerceAtLeast(0L)
        val updated = _uiState.value.project.subtitles.map {
            if (it.id == subtitleId) {
                val dur = it.endMs - it.startMs
                it.copy(startMs = validStart, endMs = validStart + dur)
            } else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(subtitles = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun moveOverlay(overlayId: String, newStartMs: Long) {
        val validStart = newStartMs.coerceAtLeast(0L)
        val updated = _uiState.value.project.overlays.map {
            if (it.id == overlayId) it.copy(startTimeMs = validStart) else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(overlays = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun openSheet(sheet: SheetType) {
        pause()
        _uiState.update { it.copy(activeSheet = sheet) }
    }

    fun closeSheet() {
        _uiState.update { it.copy(activeSheet = SheetType.NONE) }
    }

    fun setAspectRatio(aspectRatio: AspectRatioType) {
        pushHistory()
        _uiState.update {
            it.copy(project = it.project.copy(aspectRatio = aspectRatio, updatedAt = System.currentTimeMillis()))
        }
    }

    fun splitClipAtPlayhead() {
        val currentPlayhead = _uiState.value.currentPlayheadMs
        val project = _uiState.value.project
        var accumulatedMs = 0L

        val clipIndex = project.clips.indexOfFirst { clip ->
            val dur = clip.effectiveDurationMs
            val inside = currentPlayhead in (accumulatedMs + 200L)..(accumulatedMs + dur - 200L)
            accumulatedMs += dur
            inside
        }

        if (clipIndex != -1) {
            pushHistory()
            val targetClip = project.clips[clipIndex]
            var clipStartAccum = 0L
            for (i in 0 until clipIndex) {
                clipStartAccum += project.clips[i].effectiveDurationMs
            }
            val splitOffsetMs = currentPlayhead - clipStartAccum
            val originalEffective = targetClip.effectiveDurationMs

            val firstPartEndTrim = targetClip.startTrimMs + (splitOffsetMs * targetClip.speed).toLong()

            val clipPart1 = targetClip.copy(
                endTrimMs = firstPartEndTrim
            )
            val clipPart2 = targetClip.copy(
                id = java.util.UUID.randomUUID().toString(),
                title = "${targetClip.title} (Part 2)",
                startTrimMs = firstPartEndTrim
            )

            val updatedClips = project.clips.toMutableList().apply {
                removeAt(clipIndex)
                add(clipIndex, clipPart2)
                add(clipIndex, clipPart1)
            }

            _uiState.update {
                it.copy(
                    project = project.copy(clips = updatedClips, updatedAt = System.currentTimeMillis()),
                    selectedClipId = clipPart2.id
                )
            }
        }
    }

    fun deleteSelectedClip() {
        val selectedId = _uiState.value.selectedClipId ?: return
        pushHistory()
        val updated = _uiState.value.project.clips.filter { it.id != selectedId }
        _uiState.update {
            it.copy(
                project = it.project.copy(clips = updated, updatedAt = System.currentTimeMillis()),
                selectedClipId = updated.firstOrNull()?.id
            )
        }
    }

    fun duplicateSelectedClip() {
        val selectedId = _uiState.value.selectedClipId ?: return
        val clip = _uiState.value.project.clips.firstOrNull { it.id == selectedId } ?: return
        pushHistory()
        val duplicate = clip.copy(
            id = java.util.UUID.randomUUID().toString(),
            title = "${clip.title} (Copy)"
        )
        val idx = _uiState.value.project.clips.indexOf(clip)
        val updated = _uiState.value.project.clips.toMutableList().apply {
            add(idx + 1, duplicate)
        }
        _uiState.update {
            it.copy(
                project = it.project.copy(clips = updated, updatedAt = System.currentTimeMillis()),
                selectedClipId = duplicate.id
            )
        }
    }

    fun updateSelectedClipSpeed(speed: Float) {
        val selectedId = _uiState.value.selectedClipId ?: return
        pushHistory()
        val updated = _uiState.value.project.clips.map {
            if (it.id == selectedId) it.copy(speed = speed) else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(clips = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateClipSpeedAndCurve(speed: Float, curve: SpeedCurvePreset) {
        val selectedId = _uiState.value.selectedClipId ?: return
        pushHistory()
        val updated = _uiState.value.project.clips.map {
            if (it.id == selectedId) it.copy(speed = speed, speedCurve = curve) else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(clips = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateClipTrim(clipId: String, startTrimMs: Long, endTrimMs: Long) {
        val project = _uiState.value.project
        val targetClip = project.clips.firstOrNull { it.id == clipId } ?: return
        val validStart = startTrimMs.coerceIn(0L, targetClip.durationMs - 200L)
        val validEnd = endTrimMs.coerceIn(validStart + 200L, targetClip.durationMs)
        pushHistory()
        val updated = project.clips.map {
            if (it.id == clipId) it.copy(startTrimMs = validStart, endTrimMs = validEnd) else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(clips = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateClipDuration(clipId: String, newEffectiveDurationMs: Long) {
        val project = _uiState.value.project
        val targetClip = project.clips.firstOrNull { it.id == clipId } ?: return
        val clampedDuration = newEffectiveDurationMs.coerceAtLeast(200L)
        pushHistory()
        val neededRawDuration = (clampedDuration * targetClip.speed).toLong()
        val newEndTrim = targetClip.startTrimMs + neededRawDuration
        val updated = project.clips.map {
            if (it.id == clipId) {
                it.copy(
                    endTrimMs = newEndTrim,
                    durationMs = maxOf(it.durationMs, newEndTrim)
                )
            } else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(clips = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun selectOverlay(id: String?) {
        _uiState.update { it.copy(selectedOverlayId = id) }
    }

    fun addOverlayClip(overlay: OverlayClip) {
        pushHistory()
        val updated = _uiState.value.project.overlays + overlay
        _uiState.update {
            it.copy(
                project = it.project.copy(overlays = updated, updatedAt = System.currentTimeMillis()),
                selectedOverlayId = overlay.id
            )
        }
    }

    fun updateOverlay(overlay: OverlayClip) {
        val updated = _uiState.value.project.overlays.map {
            if (it.id == overlay.id) overlay else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(overlays = updated))
        }
    }

    fun updateOverlayTransform(id: String, posX: Float, posY: Float, scale: Float, rotation: Float) {
        val updated = _uiState.value.project.overlays.map {
            if (it.id == id) it.copy(posX = posX, posY = posY, scale = scale, rotation = rotation) else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(overlays = updated))
        }
    }

    fun updateOverlayDuration(id: String, startTimeMs: Long, durationMs: Long) {
        val updated = _uiState.value.project.overlays.map {
            if (it.id == id) it.copy(
                startTimeMs = startTimeMs.coerceAtLeast(0L),
                durationMs = durationMs.coerceAtLeast(200L)
            ) else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(overlays = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateOverlayChromaKey(
        id: String,
        enabled: Boolean,
        colorHex: Long = 0xFF00FF00,
        similarity: Float = 0.4f,
        smoothness: Float = 0.1f
    ) {
        pushHistory()
        val updated = _uiState.value.project.overlays.map {
            if (it.id == id) it.copy(
                chromaKeyEnabled = enabled,
                chromaKeyColorHex = colorHex,
                chromaSimilarity = similarity,
                chromaSmoothness = smoothness
            ) else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(overlays = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteOverlay(overlayId: String) {
        pushHistory()
        val updated = _uiState.value.project.overlays.filter { it.id != overlayId }
        _uiState.update {
            it.copy(
                project = it.project.copy(overlays = updated, updatedAt = System.currentTimeMillis()),
                selectedOverlayId = if (it.selectedOverlayId == overlayId) null else it.selectedOverlayId
            )
        }
    }

    fun selectSubtitle(id: String?) {
        _uiState.update { it.copy(selectedSubtitleId = id) }
    }

    fun updateSubtitlePosition(id: String, posX: Float, posY: Float, scale: Float, rotation: Float = 0f) {
        val updated = _uiState.value.project.subtitles.map {
            if (it.id == id) it.copy(posX = posX, posY = posY, scale = scale, rotation = rotation) else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(subtitles = updated))
        }
    }

    fun updateSubtitle(updatedClip: SubtitleClip) {
        pushHistory()
        val updated = _uiState.value.project.subtitles.map {
            if (it.id == updatedClip.id) updatedClip else it
        }
        _uiState.update {
            it.copy(
                project = it.project.copy(subtitles = updated, updatedAt = System.currentTimeMillis()),
                selectedSubtitleId = updatedClip.id
            )
        }
    }

    fun updateSubtitleDuration(id: String, startMs: Long, endMs: Long) {
        val validStart = startMs.coerceAtLeast(0L)
        val validEnd = maxOf(validStart + 200L, endMs)
        val updated = _uiState.value.project.subtitles.map {
            if (it.id == id) it.copy(startMs = validStart, endMs = validEnd) else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(subtitles = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteSubtitle(id: String) {
        pushHistory()
        val updated = _uiState.value.project.subtitles.filter { it.id != id }
        _uiState.update {
            it.copy(
                project = it.project.copy(subtitles = updated, updatedAt = System.currentTimeMillis()),
                selectedSubtitleId = if (it.selectedSubtitleId == id) null else it.selectedSubtitleId
            )
        }
    }

    fun addTextOverlay(
        text: String = "نیا عنوان شامل کریں",
        fontChoice: UrduFontChoice = UrduFontChoice.NOTO_NASTALIQ,
        stylePreset: SubtitleStylePreset = SubtitleStylePreset.KARAOKE,
        fontSizeSp: Float = 24f,
        textColorHex: Long = 0xFFFFFFFF,
        isRtl: Boolean = true
    ) {
        pushHistory()
        val start = _uiState.value.currentPlayheadMs
        val end = start + 3500L
        val wordsList = text.trim().split(" ").filter { it.isNotBlank() }
        val timedWords = if (wordsList.isNotEmpty()) {
            val wordDur = 3500L / wordsList.size
            wordsList.mapIndexed { idx, w ->
                TimedWord(w, start + (idx * wordDur), start + ((idx + 1) * wordDur))
            }
        } else emptyList()

        val newTextClip = SubtitleClip(
            text = text,
            startMs = start,
            endMs = end,
            fontChoice = fontChoice,
            stylePreset = stylePreset,
            fontSizeSp = fontSizeSp,
            textColorHex = textColorHex,
            isRtl = isRtl,
            words = timedWords,
            posX = 0.5f,
            posY = 0.75f,
            scale = 1.0f,
            rotation = 0f
        )
        val updated = _uiState.value.project.subtitles + newTextClip
        _uiState.update {
            it.copy(
                project = it.project.copy(subtitles = updated, updatedAt = System.currentTimeMillis()),
                selectedSubtitleId = newTextClip.id
            )
        }
    }

    fun generateSubtitlesForProject() {
        pushHistory()
        val project = _uiState.value.project
        val totalMs = project.totalDurationMs
        val sampleUrduSentences = listOf(
            "ہر مشکل کے بعد آسانی ہے",
            "اپنے خوابوں پر یقین رکھیں اور محنت کریں",
            "کامیابی صرف ایک قدم دور ہے",
            "وقت کی قدر کریں اور آگے بڑھیں",
            "یہ سفر آپ کی محنت سے خوبصورت ہے"
        )
        val stepMs = 4000L
        val count = ((totalMs / stepMs).toInt() + 1).coerceAtLeast(1)
        val generated = mutableListOf<SubtitleClip>()

        for (i in 0 until minOf(count, 5)) {
            val sStart = i * stepMs
            val sEnd = minOf(sStart + 3500L, totalMs)
            if (sStart >= totalMs) break
            val line = sampleUrduSentences[i % sampleUrduSentences.size]
            val words = line.split(" ")
            val wordStep = (sEnd - sStart) / maxOf(1, words.size)
            val timedWords = words.mapIndexed { wIdx, w ->
                TimedWord(w, sStart + (wIdx * wordStep), sStart + ((wIdx + 1) * wordStep))
            }
            generated.add(
                SubtitleClip(
                    text = line,
                    startMs = sStart,
                    endMs = sEnd,
                    fontChoice = UrduFontChoice.NOTO_NASTALIQ,
                    stylePreset = SubtitleStylePreset.KARAOKE,
                    fontSizeSp = 22f,
                    isRtl = true,
                    words = timedWords,
                    posX = 0.5f,
                    posY = 0.8f
                )
            )
        }

        _uiState.update {
            it.copy(
                project = it.project.copy(subtitles = generated, updatedAt = System.currentTimeMillis()),
                selectedSubtitleId = generated.firstOrNull()?.id
            )
        }
    }

    fun extractAudioFromClip(clipId: String? = null) {
        pushHistory()
        val project = _uiState.value.project
        val targetClip = if (clipId != null) {
            project.clips.firstOrNull { it.id == clipId }
        } else {
            project.clips.firstOrNull { it.id == _uiState.value.selectedClipId } ?: project.clips.firstOrNull()
        } ?: return

        val extractedTrack = AudioTrack(
            name = "Extracted: ${targetClip.title}",
            type = AudioType.BGM,
            uriString = targetClip.uriString,
            isExtracted = true,
            startTimeMs = 0L,
            durationMs = targetClip.effectiveDurationMs,
            volume = 1.0f
        )
        val updated = project.audioTracks + extractedTrack
        _uiState.update {
            it.copy(project = it.project.copy(audioTracks = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun addLocalAudioTrack(name: String, uriString: String, durationMs: Long) {
        pushHistory()
        val track = AudioTrack(
            name = name,
            type = AudioType.BGM,
            uriString = uriString,
            isExtracted = false,
            startTimeMs = _uiState.value.currentPlayheadMs,
            durationMs = durationMs.coerceAtLeast(3000L),
            volume = 0.9f
        )
        val updated = _uiState.value.project.audioTracks + track
        _uiState.update {
            it.copy(project = it.project.copy(audioTracks = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun addRecordedAudioTrack(name: String, uriString: String?, durationMs: Long) {
        pushHistory()
        val track = AudioTrack(
            name = name,
            type = AudioType.MIC_RECORDING,
            uriString = uriString,
            isExtracted = false,
            startTimeMs = _uiState.value.currentPlayheadMs,
            durationMs = durationMs.coerceAtLeast(1000L),
            volume = 1.0f
        )
        val updated = _uiState.value.project.audioTracks + track
        _uiState.update {
            it.copy(project = it.project.copy(audioTracks = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteAudioTrack(trackId: String) {
        pushHistory()
        val updated = _uiState.value.project.audioTracks.filter { it.id != trackId }
        _uiState.update {
            it.copy(
                project = it.project.copy(audioTracks = updated, updatedAt = System.currentTimeMillis()),
                selectedAudioTrackId = if (it.selectedAudioTrackId == trackId) null else it.selectedAudioTrackId
            )
        }
    }

    fun updateAudioTrack(track: AudioTrack) {
        pushHistory()
        val updated = _uiState.value.project.audioTracks.map {
            if (it.id == track.id) track else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(audioTracks = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun addSubtitle(subtitleClip: SubtitleClip) {
        pushHistory()
        val updated = _uiState.value.project.subtitles + subtitleClip
        _uiState.update {
            it.copy(
                project = it.project.copy(subtitles = updated, updatedAt = System.currentTimeMillis()),
                selectedSubtitleId = subtitleClip.id
            )
        }
    }

    fun applyAiSubtitles(subtitles: List<SubtitleClip>) {
        pushHistory()
        _uiState.update {
            it.copy(
                project = it.project.copy(subtitles = subtitles, updatedAt = System.currentTimeMillis()),
                selectedSubtitleId = subtitles.firstOrNull()?.id
            )
        }
    }

    fun applyAiScenes(newClips: List<MediaClip>, newSubs: List<SubtitleClip>) {
        pushHistory()
        _uiState.update {
            it.copy(
                project = it.project.copy(
                    clips = newClips,
                    subtitles = newSubs,
                    updatedAt = System.currentTimeMillis()
                ),
                selectedClipId = newClips.firstOrNull()?.id,
                currentPlayheadMs = 0L
            )
        }
    }

    fun applyColorGrading(filterType: FilterType, colorGrading: ColorGrading) {
        pushHistory()
        val selectedId = _uiState.value.selectedClipId
        val updated = _uiState.value.project.clips.map { clip ->
            if (selectedId == null || clip.id == selectedId) {
                clip.copy(filter = filterType, colorGrading = colorGrading)
            } else {
                clip
            }
        }
        _uiState.update {
            it.copy(project = it.project.copy(clips = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun addAudioTrack(track: AudioTrack) {
        pushHistory()
        val updated = _uiState.value.project.audioTracks + track
        _uiState.update {
            it.copy(project = it.project.copy(audioTracks = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun addOverlay(overlayClip: OverlayClip) {
        pushHistory()
        val updated = _uiState.value.project.overlays + overlayClip
        _uiState.update {
            it.copy(project = it.project.copy(overlays = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun setTimelineZoom(zoom: Float) {
        _uiState.update { it.copy(timelineZoom = zoom.coerceIn(0.5f, 3.0f)) }
    }

    fun addMediaClip(clip: MediaClip) {
        pushHistory()
        val updated = _uiState.value.project.clips + clip
        _uiState.update {
            it.copy(
                project = it.project.copy(clips = updated, updatedAt = System.currentTimeMillis()),
                selectedClipId = clip.id
            )
        }
    }

    fun addMediaClips(clips: List<MediaClip>) {
        if (clips.isEmpty()) return
        pushHistory()
        val updated = _uiState.value.project.clips + clips
        _uiState.update {
            it.copy(
                project = it.project.copy(clips = updated, updatedAt = System.currentTimeMillis()),
                selectedClipId = clips.first().id
            )
        }
    }

    fun createProjectFromImportedClips(clips: List<MediaClip>, aspectRatio: AspectRatioType? = null) {
        if (clips.isEmpty()) return
        pause()
        val resolvedRatio = aspectRatio ?: com.example.edit88.util.MediaImportHelper.detectBestAspectRatio(clips)
        val firstClipTitle = clips.first().title
        val projectTitle = if (clips.size == 1) firstClipTitle else "$firstClipTitle & ${clips.size - 1} clips"
        val totalMs = clips.sumOf { it.effectiveDurationMs }

        val newProj = Project(
            title = projectTitle,
            aspectRatio = resolvedRatio,
            clips = clips,
            subtitles = emptyList(), // No default subtitles on import per Requirement 1
            audioTracks = listOf(
                AudioTrack(
                    name = "Original Video Audio",
                    type = AudioType.BGM,
                    durationMs = totalMs.coerceAtLeast(5000L),
                    volume = 1.0f
                )
            )
        )

        undoStack.clear()
        redoStack.clear()
        _uiState.update {
            it.copy(
                project = newProj,
                currentPlayheadMs = 0L,
                selectedClipId = clips.first().id,
                recentProjects = listOf(newProj) + it.recentProjects,
                canUndo = false,
                canRedo = false
            )
        }
    }

    fun applyTransition(transition: TransitionType, durationMs: Long, applyToAll: Boolean) {
        pushHistory()
        val selectedId = _uiState.value.selectedClipId
        val updated = _uiState.value.project.clips.map { clip ->
            if (applyToAll || clip.id == selectedId) {
                clip.copy(transitionToNext = transition, transitionDurationMs = durationMs)
            } else {
                clip
            }
        }
        _uiState.update {
            it.copy(project = it.project.copy(clips = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun loadTemplate(template: VideoTemplate) {
        pause()
        val proj = TemplatesRepository.createProjectFromTemplate(template)
        undoStack.clear()
        redoStack.clear()
        _uiState.update {
            it.copy(
                project = proj,
                currentPlayheadMs = 0L,
                selectedClipId = proj.clips.firstOrNull()?.id,
                recentProjects = listOf(proj) + it.recentProjects.filter { p -> p.id != proj.id },
                canUndo = false,
                canRedo = false
            )
        }
    }

    fun loadProject(project: Project) {
        pause()
        undoStack.clear()
        redoStack.clear()
        _uiState.update {
            it.copy(
                project = project,
                currentPlayheadMs = 0L,
                selectedClipId = project.clips.firstOrNull()?.id,
                canUndo = false,
                canRedo = false
            )
        }
    }

    fun deleteProject(projectId: String) {
        _uiState.update {
            val filtered = it.recentProjects.filter { p -> p.id != projectId }
            it.copy(recentProjects = filtered)
        }
    }

    fun duplicateProject(project: Project) {
        val copy = project.copy(
            id = java.util.UUID.randomUUID().toString(),
            title = "${project.title} (Copy)",
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update {
            it.copy(recentProjects = listOf(copy) + it.recentProjects)
        }
    }

    fun createNewProject(aspectRatio: AspectRatioType) {
        pause()
        val newProj = Project(
            title = "Edit88 Project",
            aspectRatio = aspectRatio,
            clips = listOf(
                MediaClip(title = "Opening Hook", visualTheme = "Neon City", durationMs = 4000L, endTrimMs = 4000L, primaryColorHex = 0xFF2563EB, secondaryColorHex = 0xFF8B5CF6),
                MediaClip(title = "Main Scene", visualTheme = "Tech Studio", durationMs = 5000L, endTrimMs = 5000L, primaryColorHex = 0xFF059669, secondaryColorHex = 0xFF06B6D4),
                MediaClip(title = "Outro Reel", visualTheme = "Cyberpunk Glitch", durationMs = 4000L, endTrimMs = 4000L, primaryColorHex = 0xFFD97706, secondaryColorHex = 0xFFEC4899)
            ),
            subtitles = listOf(
                SubtitleClip(
                    text = "اپنے منفرد انداز میں ویڈیو بنائیں",
                    startMs = 500L,
                    endMs = 3800L,
                    fontChoice = UrduFontChoice.NOTO_NASTALIQ,
                    stylePreset = SubtitleStylePreset.KARAOKE,
                    words = listOf(TimedWord("اپنے", 500L, 1200L), TimedWord("منفرد انداز میں", 1200L, 2500L), TimedWord("ویڈیو بنائیں", 2500L, 3800L))
                )
            ),
            audioTracks = listOf(
                AudioTrack(name = "Cyberpunk Synth", type = AudioType.BGM, durationMs = 13000L)
            )
        )
        undoStack.clear()
        redoStack.clear()
        _uiState.update {
            it.copy(
                project = newProj,
                currentPlayheadMs = 0L,
                selectedClipId = newProj.clips.firstOrNull()?.id,
                recentProjects = listOf(newProj) + it.recentProjects,
                canUndo = false,
                canRedo = false
            )
        }
    }

    private fun createInitialSampleProject(): Project {
        return Project(
            title = "Urdu Viral Reel #1",
            aspectRatio = AspectRatioType.RATIO_9_16,
            clips = listOf(
                MediaClip(
                    title = "Scene 1: The Hook",
                    visualTheme = "Neon City",
                    durationMs = 4000L,
                    endTrimMs = 4000L,
                    primaryColorHex = 0xFF2563EB,
                    secondaryColorHex = 0xFF7C3AED
                ),
                MediaClip(
                    title = "Scene 2: Drama Clock",
                    visualTheme = "Mountain Sunrise",
                    durationMs = 5000L,
                    endTrimMs = 5000L,
                    primaryColorHex = 0xFFB45309,
                    secondaryColorHex = 0xFFF59E0B
                ),
                MediaClip(
                    title = "Scene 3: Victory Outro",
                    visualTheme = "Cyberpunk Glitch",
                    durationMs = 5000L,
                    endTrimMs = 5000L,
                    primaryColorHex = 0xFF059669,
                    secondaryColorHex = 0xFF06B6D4
                )
            ),
            subtitles = emptyList(), // Manual generation only per requirement
            audioTracks = listOf(
                AudioTrack(
                    name = "Cinematic Beat",
                    type = AudioType.BGM,
                    startTimeMs = 0L,
                    durationMs = 14000L,
                    volume = 0.85f
                )
            ),
            overlays = listOf(
                OverlayClip(
                    label = "Viral Fire",
                    iconEmoji = "🔥",
                    startTimeMs = 200L,
                    durationMs = 3600L,
                    posX = 0.6f,
                    posY = -0.4f
                )
            )
        )
    }

    private fun createCyberpunkSampleProject(): Project {
        return Project(
            title = "Cyberpunk 2088 Teaser",
            aspectRatio = AspectRatioType.RATIO_16_9,
            clips = listOf(
                MediaClip(title = "Neon Highway", visualTheme = "Cyberpunk Glitch", durationMs = 6000L, endTrimMs = 6000L)
            )
        )
    }

    private fun createTechReviewProject(): Project {
        return Project(
            title = "Smartphone Review Reel",
            aspectRatio = AspectRatioType.RATIO_9_16,
            clips = listOf(
                MediaClip(title = "Hands-on Close Up", visualTheme = "Tech Studio", durationMs = 8000L, endTrimMs = 8000L)
            )
        )
    }
}
