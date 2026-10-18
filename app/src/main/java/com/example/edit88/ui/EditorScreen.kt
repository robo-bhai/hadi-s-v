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
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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

            // 1. Live Video Canvas Preview
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
                    onTogglePlay = { viewModel.togglePlayPause() }
                )
            }

            // 2. Playback Control Bar & Timecode
            PlaybackBar(
                currentMs = uiState.currentPlayheadMs,
                totalMs = project.totalDurationMs,
                isPlaying = uiState.isPlaying,
                onTogglePlay = { viewModel.togglePlayPause() },
                onRewind = { viewModel.seekTo(0L) },
                onForward = { viewModel.seekTo(project.totalDurationMs) }
            )

            // 3. Quick Clip Tools Floating Ribbon
            ClipActionRibbon(
                selectedClipId = uiState.selectedClipId,
                onSplit = { viewModel.splitClipAtPlayhead() },
                onSpeed = { showSpeedDialog = true },
                onDuplicate = { viewModel.duplicateSelectedClip() },
                onDelete = { viewModel.deleteSelectedClip() },
                onFilter = { viewModel.openSheet(SheetType.FILTERS) }
            )

            // 4. Multi-Track Timeline
            MultiTrackTimeline(
                project = project,
                currentPlayheadMs = uiState.currentPlayheadMs,
                selectedClipId = uiState.selectedClipId,
                onSelectClip = { viewModel.selectClip(it) },
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
                SheetType.SUBTITLES -> UrduSubtitleSheet(
                    currentPlayheadMs = uiState.currentPlayheadMs,
                    onAddSubtitle = { viewModel.addSubtitle(it) },
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
                    currentPlayheadMs = uiState.currentPlayheadMs,
                    onAddAudioTrack = { viewModel.addAudioTrack(it) },
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

    // Speed Dialog
    if (showSpeedDialog) {
        SpeedSelectionDialog(
            currentSpeed = project.clips.firstOrNull { it.id == uiState.selectedClipId }?.speed ?: 1.0f,
            onSpeedSelected = {
                viewModel.updateSelectedClipSpeed(it)
                showSpeedDialog = false
            },
            onDismiss = { showSpeedDialog = false }
        )
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
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF13151E))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timecode display
        val curSec = currentMs / 1000f
        val totSec = totalMs / 1000f
        Text(
            text = "${formatTime(currentMs)} / ${formatTime(totalMs)}",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Center Play / Pause button
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onTogglePlay, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Live badge
        Box(
            modifier = Modifier
                .background(Color(0xFF1E2333), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text("Edit88 Canvas", color = Color(0xFF67E8F9), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ClipActionRibbon(
    selectedClipId: String?,
    onSplit: () -> Unit,
    onSpeed: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onFilter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF181B26))
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RibbonItem(icon = Icons.Default.Movie, label = "Split", onClick = onSplit)
        RibbonItem(icon = Icons.Default.Speed, label = "Speed", onClick = onSpeed)
        RibbonItem(icon = Icons.Default.ContentCopy, label = "Duplicate", onClick = onDuplicate)
        RibbonItem(icon = Icons.Default.ColorLens, label = "Grading", onClick = onFilter)
        RibbonItem(icon = Icons.Default.Delete, label = "Delete", onClick = onDelete, tint = Color(0xFFF87171))
    }
}

@Composable
private fun RibbonItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color(0xFFCBD5E1)
) {
    Row(
        modifier = Modifier
            .background(Color(0xFF222636), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Medium)
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
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarActionItem(icon = Icons.Default.Movie, label = "Clips", onClick = { onActionClick(SheetType.FILTERS) })
        ToolbarActionItem(icon = Icons.Default.Audiotrack, label = "Audio", onClick = { onActionClick(SheetType.AUDIO) })
        ToolbarActionItem(
            icon = Icons.Default.TextFields,
            label = "Urdu Text",
            isHighlighted = true,
            onClick = { onActionClick(SheetType.SUBTITLES) }
        )
        ToolbarActionItem(
            icon = Icons.Default.AutoAwesome,
            label = "AI Studio",
            isHighlighted = true,
            highlightColor = AccentCyan,
            onClick = { onActionClick(SheetType.AI_STUDIO) }
        )
        ToolbarActionItem(icon = Icons.Default.ColorLens, label = "Filters", onClick = { onActionClick(SheetType.FILTERS) })
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
