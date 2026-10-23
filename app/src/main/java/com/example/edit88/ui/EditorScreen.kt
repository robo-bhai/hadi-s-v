package com.example.edit88.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ZoomIn
import com.example.ui.theme.AccentAmber
import com.example.edit88.model.SpeedCurvePreset
import com.example.edit88.model.TransitionType
import com.example.edit88.ui.sheets.AddMediaSheet
import com.example.edit88.ui.sheets.ClipEditSheet
import com.example.edit88.ui.sheets.OverlayPipSheet
import com.example.edit88.ui.sheets.OverlayStickersSheet
import com.example.edit88.ui.sheets.SpeedCurveSheet
import com.example.edit88.ui.sheets.SrtExportSheet
import com.example.edit88.ui.sheets.TransitionsSheet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.AspectRatioType
import com.example.edit88.model.ColorGrading
import com.example.edit88.model.FilterType
import com.example.edit88.ui.components.MultiTrackTimeline
import com.example.edit88.ui.components.VideoCanvasPreview
import com.example.edit88.ui.sheets.AiStudioSheet
import com.example.edit88.ui.sheets.AudioEffectsSheet
import com.example.edit88.ui.sheets.ColorAndFiltersSheet
import com.example.edit88.ui.sheets.ExportDialog
import com.example.edit88.ui.sheets.SettingsSheet
import com.example.edit88.ui.sheets.UrduSubtitleSheet
import com.example.edit88.viewmodel.SheetType
import com.example.edit88.viewmodel.VideoEditorViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: VideoEditorViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val project = uiState.project
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showAspectMenu by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EditorTopBar(
                title = project.title,
                aspectRatio = project.aspectRatio,
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                onBack = onNavigateBack,
                onAspectClick = { showAspectMenu = true },
                onImportClick = { viewModel.openSheet(SheetType.ADD_MEDIA) },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onExport = { viewModel.openSheet(SheetType.EXPORT) }
            )
        },
        bottomBar = {
            EditorBottomToolbar(
                onActionClick = { sheet ->
                    viewModel.openSheet(sheet)
                }
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dropdown menu for aspect ratio
            Box(modifier = Modifier.padding(start = 60.dp)) {
                DropdownMenu(
                    expanded = showAspectMenu,
                    onDismissRequest = { showAspectMenu = false },
                    modifier = Modifier.background(DarkSurfaceVariant)
                ) {
                    AspectRatioType.values().forEach { ratio ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${ratio.label} (${ratio.iconDesc})",
                                    color = if (ratio == project.aspectRatio) AccentCyan else Color.White,
                                    fontSize = 12.sp
                                )
                            },
                            onClick = {
                                viewModel.setAspectRatio(ratio)
                                showAspectMenu = false
                            }
                        )
                    }
                }
            }

            // 1. Live Video Canvas Preview with Interactive Drag, Rotate, Scale
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                VideoCanvasPreview(
                    project = project,
                    currentPlayheadMs = uiState.currentPlayheadMs,
                    isPlaying = uiState.isPlaying,
                    selectedSubtitleId = uiState.selectedSubtitleId,
                    selectedOverlayId = uiState.selectedOverlayId,
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onUpdateOverlay = { viewModel.updateOverlay(it) },
                    onDeleteOverlay = { viewModel.deleteOverlay(it) },
                    onSelectOverlay = { viewModel.selectOverlay(it) },
                    onSelectSubtitle = { viewModel.selectSubtitle(it) },
                    onUpdateSubtitlePosition = { id, x, y, scale, rot ->
                        viewModel.updateSubtitlePosition(id, x, y, scale, rot)
                    },
                    onDeleteSubtitle = { viewModel.deleteSubtitle(it) },
                    onEditSubtitle = {
                        viewModel.selectSubtitle(it.id)
                        viewModel.openSheet(SheetType.SUBTITLES)
                    }
                )

                if (uiState.isGeneratingSubtitles) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                            .background(Color(0xEE0B132B), RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF38BDF8),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🎙️ Gemini AI audio transcribe kr raha he...",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 2. Playback Control Bar, Live Frame Counter & Zoom
            PlaybackBar(
                currentMs = uiState.currentPlayheadMs,
                totalMs = project.totalDurationMs,
                timelineZoom = uiState.timelineZoom,
                isPlaying = uiState.isPlaying,
                onTogglePlay = { viewModel.togglePlayPause() },
                onSetZoom = { viewModel.setTimelineZoom(it) }
            )

            // 3. Quick Clip Tools Floating Ribbon
            ClipActionRibbon(
                selectedClipId = uiState.selectedClipId,
                hasSelection = uiState.hasSelection,
                selectedTypeLabel = uiState.selectedTypeLabel,
                onAddClip = { viewModel.openSheet(SheetType.ADD_MEDIA) },
                onEditClip = { viewModel.openSheet(SheetType.CLIP_EDIT) },
                onAddText = { viewModel.openSheet(SheetType.SUBTITLES) },
                onOverlay = { viewModel.openSheet(SheetType.OVERLAY_PIP) },
                onExtractAudio = { viewModel.extractAudioFromClip() },
                onGenerateSubtitles = { viewModel.generateSubtitlesForProject() },
                onSplit = { viewModel.splitClipAtPlayhead() },
                onSpeed = { showSpeedDialog = true },
                onTransition = { viewModel.openSheet(SheetType.TRANSITIONS) },
                onStickers = { viewModel.openSheet(SheetType.STICKERS) },
                onDuplicate = { viewModel.duplicateSelectedClip() },
                onDelete = { viewModel.deleteSelected() },
                onFilter = { viewModel.openSheet(SheetType.FILTERS) },
                onExportSrt = { viewModel.openSheet(SheetType.SRT_EXPORT) }
            )

            // 4. Multi-Track Timeline with Subtitle duration trimming and Track Badges
            MultiTrackTimeline(
                project = project,
                currentPlayheadMs = uiState.currentPlayheadMs,
                selectedClipId = uiState.selectedClipId,
                selectedAudioTrackId = uiState.selectedAudioTrackId,
                selectedSubtitleId = uiState.selectedSubtitleId,
                selectedOverlayId = uiState.selectedOverlayId,
                timelineZoom = uiState.timelineZoom,
                onSelectClip = { viewModel.selectClip(it) },
                onSelectAudioTrack = { viewModel.selectAudioTrack(it) },
                onSelectSubtitle = { viewModel.selectSubtitle(it) },
                onSelectOverlay = { viewModel.selectOverlay(it) },
                onReorderClips = { from, to -> viewModel.reorderClips(from, to) },
                onMoveAudioTrack = { id, start -> viewModel.moveAudioTrack(id, start) },
                onMoveSubtitle = { id, start -> viewModel.moveSubtitle(id, start) },
                onMoveOverlay = { id, start -> viewModel.moveOverlay(id, start) },
                onUpdateSubtitleDuration = { id, start, end -> viewModel.updateSubtitleDuration(id, start, end) },
                onUpdateClipTrim = { id, start, end -> viewModel.updateClipTrim(id, start, end) },
                onUpdateOverlayDuration = { id, start, dur -> viewModel.updateOverlayDuration(id, start, dur) },
                onAddVideoClipClick = { viewModel.openSheet(SheetType.ADD_MEDIA) },
                onAddOverlayClick = { viewModel.openSheet(SheetType.OVERLAY_PIP) },
                onAddTextClick = { viewModel.openSheet(SheetType.SUBTITLES) },
                onAddAudioClick = { viewModel.openSheet(SheetType.AUDIO) },
                onGenerateSubtitlesClick = { viewModel.generateSubtitlesForProject() },
                onSeekTo = { viewModel.seekTo(it) }
            )
        }
    }

    // Modal Bottom Sheets for Pro Editing Suites
    if (uiState.activeSheet != SheetType.NONE && uiState.activeSheet != SheetType.EXPORT) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeSheet() },
            sheetState = sheetState,
            containerColor = DarkSurface
        ) {
            when (uiState.activeSheet) {
                SheetType.SETTINGS -> SettingsSheet(
                    settingsRepository = viewModel.settingsRepository,
                    geminiService = viewModel.geminiService,
                    onClose = { viewModel.closeSheet() }
                )
                SheetType.SUBTITLES, SheetType.TEXT_EDIT -> UrduSubtitleSheet(
                    project = project,
                    currentPlayheadMs = uiState.currentPlayheadMs,
                    selectedSubtitleId = uiState.selectedSubtitleId,
                    isGeneratingSubtitles = uiState.isGeneratingSubtitles,
                    subtitleGenerationStatus = uiState.subtitleGenerationStatus,
                    onAddSubtitle = { viewModel.addSubtitle(it) },
                    onUpdateSubtitle = { viewModel.updateSubtitle(it) },
                    onDeleteSubtitle = { viewModel.deleteSubtitle(it) },
                    onGenerateSubtitles = { viewModel.generateSubtitlesForProject() },
                    onClose = { viewModel.closeSheet() }
                )
                SheetType.AI_STUDIO -> AiStudioSheet(
                    project = project,
                    settingsRepository = viewModel.settingsRepository,
                    geminiService = viewModel.geminiService,
                    onApplySubtitles = { viewModel.applyAiSubtitles(it) },
                    onApplyScenes = { scenes, subs -> viewModel.applyAiScenes(scenes, subs) },
                    onApplyColorGrading = { filter, grading -> viewModel.applyColorGrading(filter, grading) },
                    onSeekTo = { viewModel.seekTo(it) },
                    onOpenSettings = { viewModel.openSheet(SheetType.SETTINGS) },
                    onClose = { viewModel.closeSheet() }
                )
                SheetType.AUDIO -> AudioEffectsSheet(
                    project = project,
                    currentPlayheadMs = uiState.currentPlayheadMs,
                    onAddAudioTrack = { viewModel.addAudioTrack(it) },
                    onExtractAudio = { viewModel.extractAudioFromClip() },
                    onAddLocalAudio = { name, uri, dur -> viewModel.addLocalAudioTrack(name, uri, dur) },
                    onAddRecordedAudio = { name, uri, dur -> viewModel.addRecordedAudioTrack(name, uri, dur) },
                    onDeleteAudioTrack = { viewModel.deleteAudioTrack(it) },
                    onUpdateAudioTrack = { viewModel.updateAudioTrack(it) },
                    onClose = { viewModel.closeSheet() }
                )
                SheetType.CLIP_EDIT -> {
                    val sel = project.clips.firstOrNull { it.id == uiState.selectedClipId } ?: project.clips.firstOrNull()
                    if (sel != null) {
                        ClipEditSheet(
                            clip = sel,
                            currentPlayheadMs = uiState.currentPlayheadMs,
                            onUpdateTrim = { id, start, end -> viewModel.updateClipTrim(id, start, end) },
                            onUpdateSpeed = { id, spd -> viewModel.updateClipSpeedAndCurve(spd, sel.speedCurve) },
                            onSplitAtPlayhead = { viewModel.splitClipAtPlayhead() },
                            onExtractAudio = { viewModel.extractAudioFromClip(sel.id) },
                            onDuplicateClip = { viewModel.duplicateSelectedClip() },
                            onDeleteClip = {
                                viewModel.deleteSelectedClip()
                                viewModel.closeSheet()
                            },
                            onOpenFilters = { viewModel.openSheet(SheetType.FILTERS) },
                            onOpenSpeedCurves = {
                                viewModel.closeSheet()
                                showSpeedDialog = true
                            },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                }
                SheetType.OVERLAY_PIP -> OverlayPipSheet(
                    project = project,
                    currentPlayheadMs = uiState.currentPlayheadMs,
                    selectedOverlayId = uiState.selectedOverlayId,
                    onAddOverlay = { viewModel.addOverlayClip(it) },
                    onUpdateOverlay = { viewModel.updateOverlay(it) },
                    onDeleteOverlay = { viewModel.deleteOverlay(it) },
                    onSelectOverlay = { viewModel.selectOverlay(it) },
                    onClose = { viewModel.closeSheet() }
                )
                SheetType.FILTERS -> {
                    val sel = project.clips.firstOrNull { it.id == uiState.selectedClipId } ?: project.clips.firstOrNull()
                    ColorAndFiltersSheet(
                        initialFilter = sel?.filter ?: FilterType.NONE,
                        initialGrading = sel?.colorGrading ?: ColorGrading(),
                        onApply = { filter, grading -> viewModel.applyColorGrading(filter, grading) },
                        onClose = { viewModel.closeSheet() }
                    )
                }
                SheetType.TRANSITIONS -> {
                    val sel = project.clips.firstOrNull { it.id == uiState.selectedClipId } ?: project.clips.firstOrNull()
                    TransitionsSheet(
                        initialTransition = sel?.transitionToNext ?: TransitionType.NONE,
                        initialDurationMs = sel?.transitionDurationMs ?: 500L,
                        onApply = { trans, dur, toAll -> viewModel.applyTransition(trans, dur, toAll) },
                        onClose = { viewModel.closeSheet() }
                    )
                }
                SheetType.STICKERS -> OverlayStickersSheet(
                    currentPlayheadMs = uiState.currentPlayheadMs,
                    onAddOverlay = { viewModel.addOverlay(it) },
                    onClose = { viewModel.closeSheet() }
                )
                SheetType.ADD_MEDIA -> AddMediaSheet(
                    onAddClip = { viewModel.addMediaClip(it) },
                    onAddClips = { viewModel.addMediaClips(it) },
                    onClose = { viewModel.closeSheet() }
                )
                SheetType.SRT_EXPORT -> SrtExportSheet(
                    subtitles = project.subtitles,
                    onClose = { viewModel.closeSheet() }
                )
                else -> {}
            }
        }
    }

    // Export Dialog
    if (uiState.activeSheet == SheetType.EXPORT) {
        ExportDialog(
            project = project,
            onDismiss = { viewModel.closeSheet() }
        )
    }

    // Speed & Curve Sheet
    if (showSpeedDialog) {
        val selClip = project.clips.firstOrNull { it.id == uiState.selectedClipId } ?: project.clips.firstOrNull()
        ModalBottomSheet(
            onDismissRequest = { showSpeedDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = DarkSurface
        ) {
            SpeedCurveSheet(
                initialSpeed = selClip?.speed ?: 1.0f,
                initialCurve = selClip?.speedCurve ?: SpeedCurvePreset.STANDARD,
                onApplySpeed = { speed, curve ->
                    viewModel.updateClipSpeedAndCurve(speed, curve)
                    showSpeedDialog = false
                },
                onClose = { showSpeedDialog = false }
            )
        }
    }
}

@Composable
private fun EditorTopBar(
    title: String,
    aspectRatio: AspectRatioType,
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit,
    onAspectClick: () -> Unit,
    onImportClick: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onExport: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            // Aspect Ratio Selector Chip
            Box(
                modifier = Modifier
                    .background(Color(0xFF222636), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF384056), RoundedCornerShape(12.dp))
                    .clickable(onClick = onAspectClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AspectRatio, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(aspectRatio.label, color = Color(0xFFE2E8F0), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Quick Import Button
            Box(
                modifier = Modifier
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .border(1.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onImportClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Import", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Undo & Redo
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onUndo, enabled = canUndo) {
                Icon(
                    Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) Color.White else Color(0xFF475569)
                )
            }
            IconButton(onClick = onRedo, enabled = canRedo) {
                Icon(
                    Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) Color.White else Color(0xFF475569)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Export Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(AccentPurple, AccentPink)
                        )
                    )
                    .clickable(onClick = onExport)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PlaybackBar(
    currentMs: Long,
    totalMs: Long,
    timelineZoom: Float,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onSetZoom: (Float) -> Unit
) {
    val curFrame = (currentMs * 30 / 1000).toInt()
    val totFrames = (totalMs * 30 / 1000).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF13151E))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timecode & Frame Counter
        Column {
            Text(
                text = "${formatTime(currentMs)} / ${formatTime(totalMs)}",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Frame $curFrame / $totFrames @30fps",
                color = AccentCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Center Play / Pause button
        IconButton(onClick = onTogglePlay, modifier = Modifier.size(34.dp)) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Timeline Zoom Controls
        Row(
            modifier = Modifier
                .background(Color(0xFF1E2333), RoundedCornerShape(12.dp))
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(1.0f, 1.5f, 2.0f).forEach { z ->
                val isSel = (timelineZoom == z)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) AccentPurple else Color.Transparent)
                        .clickable { onSetZoom(z) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${z}x",
                        color = if (isSel) Color.White else Color(0xFF94A3B8),
                        fontSize = 9.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun ClipActionRibbon(
    selectedClipId: String?,
    hasSelection: Boolean = false,
    selectedTypeLabel: String = "",
    onAddClip: () -> Unit,
    onEditClip: () -> Unit,
    onAddText: () -> Unit,
    onOverlay: () -> Unit,
    onExtractAudio: () -> Unit,
    onGenerateSubtitles: () -> Unit,
    onSplit: () -> Unit,
    onSpeed: () -> Unit,
    onTransition: () -> Unit,
    onStickers: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onFilter: () -> Unit,
    onExportSrt: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF181B26))
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RibbonItem(icon = Icons.Default.Add, label = "+ Add Clip", onClick = onAddClip, tint = AccentCyan)
        RibbonItem(icon = Icons.Default.Movie, label = "Edit / Trim", onClick = onEditClip, tint = Color(0xFF38BDF8))
        RibbonItem(icon = Icons.Default.TextFields, label = "+ Add Text", onClick = onAddText, tint = AccentAmber)
        RibbonItem(icon = Icons.Default.Layers, label = "Overlay (PiP)", onClick = onOverlay, tint = AccentPurple)
        RibbonItem(icon = Icons.AutoMirrored.Filled.VolumeUp, label = "Extract Audio", onClick = onExtractAudio, tint = Color(0xFF34D399))
        RibbonItem(icon = Icons.Default.AutoAwesome, label = "✨ Auto Subs", onClick = onGenerateSubtitles, tint = Color(0xFFFBBF24))
        RibbonItem(icon = Icons.Default.Movie, label = "Split", onClick = onSplit)
        RibbonItem(icon = Icons.Default.Speed, label = "Speed", onClick = onSpeed)
        RibbonItem(icon = Icons.Default.FlashOn, label = "Transition", onClick = onTransition, tint = AccentPurple)
        RibbonItem(icon = Icons.Default.Layers, label = "Stickers", onClick = onStickers, tint = AccentPink)
        RibbonItem(icon = Icons.Default.ContentCopy, label = "Duplicate", onClick = onDuplicate)
        RibbonItem(icon = Icons.Default.ColorLens, label = "Grading", onClick = onFilter)
        RibbonItem(icon = Icons.Default.Subtitles, label = ".SRT Subtitles", onClick = onExportSrt, tint = Color(0xFFFBBF24))
        RibbonItem(
            icon = Icons.Default.Delete,
            label = if (hasSelection) "Delete $selectedTypeLabel" else "Delete",
            onClick = onDelete,
            tint = Color(0xFFF87171),
            highlighted = hasSelection
        )
    }
}

@Composable
private fun RibbonItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color(0xFFCBD5E1),
    highlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .background(
                if (highlighted) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF222636),
                RoundedCornerShape(8.dp)
            )
            .border(
                width = if (highlighted) 1.dp else 0.dp,
                color = if (highlighted) Color(0xFFF87171) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = tint, fontSize = 11.sp, fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun EditorBottomToolbar(
    onActionClick: (SheetType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF11131B))
            .navigationBarsPadding()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarActionItem(icon = Icons.Default.Add, label = "Media", onClick = { onActionClick(SheetType.ADD_MEDIA) })
        ToolbarActionItem(
            icon = Icons.Default.Movie,
            label = "Edit / Trim",
            isHighlighted = true,
            highlightColor = Color(0xFF38BDF8),
            onClick = { onActionClick(SheetType.CLIP_EDIT) }
        )
        ToolbarActionItem(
            icon = Icons.Default.TextFields,
            label = "Add Text",
            isHighlighted = true,
            highlightColor = AccentAmber,
            onClick = { onActionClick(SheetType.SUBTITLES) }
        )
        ToolbarActionItem(
            icon = Icons.Default.Layers,
            label = "Overlay (PiP)",
            isHighlighted = true,
            highlightColor = AccentPurple,
            onClick = { onActionClick(SheetType.OVERLAY_PIP) }
        )
        ToolbarActionItem(
            icon = Icons.Default.Audiotrack,
            label = "Audio & Voice",
            isHighlighted = true,
            highlightColor = Color(0xFF10B981),
            onClick = { onActionClick(SheetType.AUDIO) }
        )
        ToolbarActionItem(
            icon = Icons.Default.AutoAwesome,
            label = "Auto Subs",
            isHighlighted = true,
            highlightColor = Color(0xFFFBBF24),
            onClick = { onActionClick(SheetType.SUBTITLES) }
        )
        ToolbarActionItem(icon = Icons.Default.ColorLens, label = "Filters & LUTs", onClick = { onActionClick(SheetType.FILTERS) })
        ToolbarActionItem(icon = Icons.Default.FlashOn, label = "Transition", onClick = { onActionClick(SheetType.TRANSITIONS) })
        ToolbarActionItem(icon = Icons.Default.Layers, label = "Stickers", onClick = { onActionClick(SheetType.STICKERS) })
        ToolbarActionItem(icon = Icons.Default.AutoAwesome, label = "AI Studio", onClick = { onActionClick(SheetType.AI_STUDIO) })
        ToolbarActionItem(icon = Icons.Default.Settings, label = "Settings", onClick = { onActionClick(SheetType.SETTINGS) })
    }
}

@Composable
private fun ToolbarActionItem(
    icon: ImageVector,
    label: String,
    isHighlighted: Boolean = false,
    highlightColor: Color = AccentPurple,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = if (isHighlighted) {
                Modifier
                    .size(34.dp)
                    .background(highlightColor.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, highlightColor, CircleShape)
            } else {
                Modifier.size(34.dp)
            },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isHighlighted) highlightColor else Color(0xFFCBD5E1),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isHighlighted) highlightColor else Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SpeedSelectionDialog(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF2C3247), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Clip Speed Control", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                val speedPresets = listOf(0.25f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f, 5.0f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    speedPresets.forEach { spd ->
                        val isSel = (spd == currentSpeed)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSel) AccentPurple else Color(0xFF1E212D), RoundedCornerShape(8.dp))
                                .clickable { onSpeedSelected(spd) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${spd}x", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262C3E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color(0xFFE2E8F0))
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    val hund = (ms % 1000) / 10
    return "%02d:%02d.%02d".format(min, sec, hund)
}
