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
    SRT_EXPORT
}

data class EditorUiState(
    val project: Project = Project(),
    val currentPlayheadMs: Long = 0L,
    val isPlaying: Boolean = false,
    val selectedClipId: String? = null,
    val activeSheet: SheetType = SheetType.NONE,
    val recentProjects: List<Project> = emptyList(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val timelineZoom: Float = 1.0f
)

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

    fun selectClip(id: String) {
        _uiState.update { it.copy(selectedClipId = id) }
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
        if (_uiState.value.project.clips.size <= 1) return // Keep at least one clip
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

    fun updateOverlay(overlay: OverlayClip) {
        val updated = _uiState.value.project.overlays.map {
            if (it.id == overlay.id) overlay else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(overlays = updated))
        }
    }

    fun deleteOverlay(overlayId: String) {
        pushHistory()
        val updated = _uiState.value.project.overlays.filter { it.id != overlayId }
        _uiState.update {
            it.copy(project = it.project.copy(overlays = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateSubtitlePosition(id: String, posX: Float, posY: Float, scale: Float) {
        val updated = _uiState.value.project.subtitles.map {
            if (it.id == id) it.copy(posX = posX, posY = posY, scale = scale) else it
        }
        _uiState.update {
            it.copy(project = it.project.copy(subtitles = updated))
        }
    }

    fun addSubtitle(subtitleClip: SubtitleClip) {
        pushHistory()
        val updated = _uiState.value.project.subtitles + subtitleClip
        _uiState.update {
            it.copy(project = it.project.copy(subtitles = updated, updatedAt = System.currentTimeMillis()))
        }
    }

    fun applyAiSubtitles(subtitles: List<SubtitleClip>) {
        pushHistory()
        _uiState.update {
            it.copy(project = it.project.copy(subtitles = subtitles, updatedAt = System.currentTimeMillis()))
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
            subtitles = listOf(
                SubtitleClip(
                    text = "اپنے منفرد انداز میں بنائیں",
                    startMs = 500L,
                    endMs = minOf(4000L, totalMs),
                    fontChoice = UrduFontChoice.NOTO_NASTALIQ,
                    stylePreset = SubtitleStylePreset.KARAOKE,
                    words = listOf(
                        TimedWord("اپنے", 500L, 1200L),
                        TimedWord("منفرد انداز", 1200L, 2600L),
                        TimedWord("میں بنائیں", 2600L, minOf(4000L, totalMs))
                    )
                )
            ),
            audioTracks = listOf(
                AudioTrack(
                    name = "Vlog Beat Ambient",
                    type = AudioType.BGM,
                    durationMs = totalMs.coerceAtLeast(10000L),
                    volume = 0.5f
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
            subtitles = listOf(
                SubtitleClip(
                    text = "خواب دیکھنا آسان ہے",
                    startMs = 0L,
                    endMs = 3800L,
                    fontChoice = UrduFontChoice.NOTO_NASTALIQ,
                    stylePreset = SubtitleStylePreset.KARAOKE,
                    isRtl = true,
                    words = listOf(
                        TimedWord("خواب", 0L, 1200L),
                        TimedWord("دیکھنا", 1200L, 2400L),
                        TimedWord("آسان ہے", 2400L, 3800L)
                    )
                ),
                SubtitleClip(
                    text = "مگر انہیں پورا کرنا اصل ہمت ہے",
                    startMs = 4200L,
                    endMs = 8800L,
                    fontChoice = UrduFontChoice.GULZAR,
                    stylePreset = SubtitleStylePreset.NEON_GLOW,
                    isRtl = true,
                    words = listOf(
                        TimedWord("مگر", 4200L, 5200L),
                        TimedWord("انہیں پورا کرنا", 5200L, 7000L),
                        TimedWord("اصل ہمت ہے", 7000L, 8800L)
                    )
                ),
                SubtitleClip(
                    text = "آج ہی وہ پہلا قدم اٹھائیں!",
                    startMs = 9200L,
                    endMs = 14000L,
                    fontChoice = UrduFontChoice.NOTO_NASTALIQ,
                    stylePreset = SubtitleStylePreset.KARAOKE,
                    isRtl = true,
                    words = listOf(
                        TimedWord("آج ہی وہ", 9200L, 10800L),
                        TimedWord("پہلا قدم", 10800L, 12400L),
                        TimedWord("اٹھائیں!", 12400L, 14000L)
                    )
                )
            ),
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
