package com.example.edit88.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.edit88.model.AudioTrack
import com.example.edit88.model.MediaClip
import com.example.edit88.model.OverlayClip
import com.example.edit88.model.Project
import com.example.edit88.model.SubtitleClip
import com.example.edit88.model.UrduFontChoice
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GulzarFontFamily
import com.example.ui.theme.LateefFontFamily
import com.example.ui.theme.NotoNastaliqUrduFontFamily
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.TrackAudioColor
import com.example.ui.theme.TrackOverlayColor
import com.example.ui.theme.TrackSubtitleColor
import com.example.ui.theme.TrackVideoColor
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.gestures.detectTransformGestures
import java.io.File

import com.example.edit88.model.TransitionType

fun triggerHapticFeedback(context: Context, haptic: HapticFeedback?) {
    try {
        haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50L)
            }
        }
    } catch (_: Exception) {
        // Safe fallback
    }
}


@Composable
fun MultiTrackTimeline(
    project: Project,
    currentPlayheadMs: Long,
    selectedClipId: String?,
    selectedAudioTrackId: String? = null,
    selectedSubtitleId: String? = null,
    selectedOverlayId: String? = null,
    timelineZoom: Float = 1.0f,
    onZoomChange: ((Float) -> Unit)? = null,
    onSelectClip: (String) -> Unit,
    onSelectAudioTrack: ((String) -> Unit)? = null,
    onSelectSubtitle: ((String) -> Unit)? = null,
    onEditSubtitle: ((SubtitleClip) -> Unit)? = null,
    onSelectOverlay: ((String) -> Unit)? = null,
    onReorderClips: ((Int, Int) -> Unit)? = null,
    onMoveAudioTrack: ((String, Long) -> Unit)? = null,
    onMoveSubtitle: ((String, Long) -> Unit)? = null,
    onMoveOverlay: ((String, Long) -> Unit)? = null,
    onUpdateSubtitleDuration: ((String, Long, Long) -> Unit)? = null,
    onUpdateClipTrim: ((String, Long, Long) -> Unit)? = null,
    onUpdateOverlayDuration: ((String, Long, Long) -> Unit)? = null,
    onAddVideoClipClick: (() -> Unit)? = null,
    onAddOverlayClick: (() -> Unit)? = null,
    onAddTextClick: (() -> Unit)? = null,
    onAddAudioClick: (() -> Unit)? = null,
    onGenerateSubtitlesClick: (() -> Unit)? = null,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Scale: 1000ms = 60dp (scaled by zoom from 0.25x overview to 5.0x frame-by-frame precision)
    val msPerDp = (1000f / 60f) / timelineZoom.coerceIn(0.25f, 5.0f)
    val totalDurationMs = project.totalDurationMs.coerceAtLeast(15000L)
    val timelineWidthDp = (totalDurationMs / msPerDp).dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF13151D))
            .border(1.dp, Color(0xFF222533))
    ) {
        // Track Labels, Zoom Controls & Time Ruler Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF181B26))
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left fixed column for track icons
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .padding(start = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Tracks", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            // Pinch-to-zoom & Scrollable Time Ruler
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
                    .pointerInput(timelineZoom) {
                        detectTransformGestures(panZoomLock = false) { _, _, zoom, _ ->
                            if (zoom != 1f) {
                                val newZoom = (timelineZoom * zoom).coerceIn(0.25f, 5.0f)
                                onZoomChange?.invoke(newZoom)
                            }
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.width(timelineWidthDp + 200.dp)
                ) {
                    val stepSec = when {
                        timelineZoom >= 3.0f -> 1
                        timelineZoom >= 1.5f -> 2
                        timelineZoom <= 0.6f -> 5
                        else -> 2
                    }
                    val totalSec = (totalDurationMs / 1000).toInt() + 4
                    for (sec in 0..totalSec step stepSec) {
                        val secWidthDp = (stepSec * 1000 / msPerDp).dp
                        Column(
                            modifier = Modifier.width(secWidthDp)
                        ) {
                            Text(
                                text = "${sec}s",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(6.dp)
                                    .background(Color(0xFF475569))
                            )
                        }
                    }
                }
            }

            // Quick Pinch/Zoom Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = String.format("%.2fx", timelineZoom),
                    color = Color(0xFF38BDF8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                        .clickable { onZoomChange?.invoke((timelineZoom - 0.25f).coerceIn(0.25f, 5.0f)) }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text("-", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .background(if (timelineZoom == 1.0f) Color(0xFF38BDF8) else Color(0xFF1E293B), RoundedCornerShape(4.dp))
                        .clickable { onZoomChange?.invoke(1.0f) }
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                ) {
                    Text("1x", color = if (timelineZoom == 1.0f) Color.Black else Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                        .clickable { onZoomChange?.invoke((timelineZoom + 0.35f).coerceIn(0.25f, 5.0f)) }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text("+", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Timeline Body with Tracks & Scrubbing Playhead
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Fixed Track Icon Rail with multi-item add shortcuts
                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF161822))
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TrackIconItem(icon = Icons.Default.Movie, color = TrackVideoColor, label = "Video", onAddClick = onAddVideoClipClick)
                    TrackIconItem(icon = Icons.Default.Layers, color = TrackOverlayColor, label = "PiP", onAddClick = onAddOverlayClick)
                    TrackIconItem(icon = Icons.Default.TextFields, color = TrackSubtitleColor, label = "Text", onAddClick = onAddTextClick)
                    TrackIconItem(icon = Icons.Default.Audiotrack, color = TrackAudioColor, label = "Audio", onAddClick = onAddAudioClick)
                }

                // Scrollable Tracks Area with Multi-Touch Pinch-to-Zoom
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(scrollState)
                        .pointerInput(timelineZoom) {
                            detectTransformGestures(panZoomLock = false) { _, _, zoom, _ ->
                                if (zoom != 1f) {
                                    val newZoom = (timelineZoom * zoom).coerceIn(0.25f, 5.0f)
                                    onZoomChange?.invoke(newZoom)
                                }
                            }
                        }
                        .pointerInput(totalDurationMs) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val xPosPx = change.position.x
                                val xPosDp = with(density) { xPosPx.toDp() }.value
                                val targetMs = (xPosDp * msPerDp).toLong().coerceIn(0L, totalDurationMs)
                                onSeekTo(targetMs)
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .width(timelineWidthDp + 240.dp)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 1. Video Track
                        VideoTrackRow(
                            clips = project.clips,
                            msPerDp = msPerDp,
                            selectedClipId = selectedClipId,
                            onSelectClip = onSelectClip,
                            onReorderClips = onReorderClips,
                            onUpdateClipTrim = onUpdateClipTrim,
                            onAddClipClick = onAddVideoClipClick
                        )

                        // 2. Overlay Track
                        OverlayTrackRow(
                            overlays = project.overlays,
                            msPerDp = msPerDp,
                            selectedOverlayId = selectedOverlayId,
                            onSelectOverlay = onSelectOverlay,
                            onMoveOverlay = onMoveOverlay,
                            onUpdateOverlayDuration = onUpdateOverlayDuration,
                            onAddOverlayClick = onAddOverlayClick,
                            onSeekTo = onSeekTo
                        )

                        // 3. Subtitle / Urdu Track
                        SubtitleTrackRow(
                            subtitles = project.subtitles,
                            msPerDp = msPerDp,
                            selectedSubtitleId = selectedSubtitleId,
                            onSelectSubtitle = onSelectSubtitle,
                            onEditSubtitle = onEditSubtitle,
                            onMoveSubtitle = onMoveSubtitle,
                            onUpdateSubtitleDuration = onUpdateSubtitleDuration,
                            onAddTextClick = onAddTextClick,
                            onGenerateSubtitlesClick = onGenerateSubtitlesClick,
                            onSeekTo = onSeekTo
                        )

                        // 4. Audio Track
                        AudioTrackRow(
                            audioTracks = project.audioTracks,
                            msPerDp = msPerDp,
                            selectedAudioTrackId = selectedAudioTrackId,
                            onSelectAudioTrack = onSelectAudioTrack,
                            onMoveAudioTrack = onMoveAudioTrack,
                            onAddAudioClick = onAddAudioClick,
                            onSeekTo = onSeekTo
                        )
                    }

                    // Playhead Line with red handle
                    val playheadOffsetDp = (currentPlayheadMs / msPerDp).dp
                    Box(
                        modifier = Modifier
                            .offset(x = playheadOffsetDp)
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(PlayheadColor)
                    ) {
                        // Pin Head
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-2).dp)
                                .size(12.dp)
                                .background(PlayheadColor, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackIconItem(
    icon: ImageVector,
    color: Color,
    label: String,
    onAddClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onAddClick?.invoke() }
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(17.dp)
            )
        }
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun VideoTrackRow(
    clips: List<MediaClip>,
    msPerDp: Float,
    selectedClipId: String?,
    onSelectClip: (String) -> Unit,
    onReorderClips: ((Int, Int) -> Unit)? = null,
    onUpdateClipTrim: ((String, Long, Long) -> Unit)? = null,
    onAddClipClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(Color(0xFF1E212D), RoundedCornerShape(6.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (clips.isEmpty()) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(34.dp)
                    .background(Color(0xFF2B2F40), RoundedCornerShape(4.dp))
                    .clickable { onAddClipClick?.invoke() }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("+ Add Media", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
        } else {
            clips.forEachIndexed { clipIndex, mediaClip ->
                val clipWidthDp = (mediaClip.effectiveDurationMs / msPerDp).dp.coerceAtLeast(36.dp)
                val isSelected = mediaClip.id == selectedClipId
                var isDragging by remember { mutableStateOf(false) }
                var dragAccumulatedPx by remember { mutableFloatStateOf(0f) }
                val clipWidthPx = with(density) { clipWidthDp.toPx() }

                Box(
                    modifier = Modifier
                        .width(clipWidthDp)
                        .height(34.dp)
                        .zIndex(if (isDragging) 10f else 1f)
                        .graphicsLayer {
                            translationX = if (isDragging) dragAccumulatedPx.coerceIn(-clipWidthPx * 1.2f, clipWidthPx * 1.2f) else 0f
                            scaleX = if (isDragging) 1.05f else 1f
                            scaleY = if (isDragging) 1.05f else 1f
                            shadowElevation = if (isDragging) 8f else 0f
                        }
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(mediaClip.primaryColorHex).copy(alpha = 0.85f),
                                    Color(mediaClip.secondaryColorHex).copy(alpha = 0.85f)
                                )
                            )
                        )
                        .border(
                            width = if (isDragging) 2.dp else if (isSelected) 2.dp else 0.5.dp,
                            color = if (isDragging) Color(0xFF38BDF8) else if (isSelected) Color(0xFFFACC15) else Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onSelectClip(mediaClip.id) }
                        .pointerInput(mediaClip.id, clipIndex, clips.size) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    isDragging = true
                                    dragAccumulatedPx = 0f
                                    onSelectClip(mediaClip.id)
                                    triggerHapticFeedback(context, haptic)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragAccumulatedPx += dragAmount.x
                                    val thresholdPx = clipWidthPx * 0.45f
                                    if (dragAccumulatedPx > thresholdPx && clipIndex < clips.size - 1) {
                                        onReorderClips?.invoke(clipIndex, clipIndex + 1)
                                        triggerHapticFeedback(context, haptic)
                                        dragAccumulatedPx = 0f
                                    } else if (dragAccumulatedPx < -thresholdPx && clipIndex > 0) {
                                        onReorderClips?.invoke(clipIndex, clipIndex - 1)
                                        triggerHapticFeedback(context, haptic)
                                        dragAccumulatedPx = 0f
                                    }
                                },
                                onDragEnd = {
                                    isDragging = false
                                    dragAccumulatedPx = 0f
                                },
                                onDragCancel = {
                                    isDragging = false
                                    dragAccumulatedPx = 0f
                                }
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Trim Handle on selected clip
                        if (isSelected && !isDragging) {
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFFFACC15), RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                                    .pointerInput(mediaClip.id) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val deltaMs = (dragAmount.x * msPerDp).toLong()
                                            val newStart = (mediaClip.startTrimMs + deltaMs).coerceIn(0L, mediaClip.endTrimMs - 300L)
                                            onUpdateClipTrim?.invoke(mediaClip.id, newStart, mediaClip.endTrimMs)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.width(1.5.dp).height(12.dp).background(Color.Black))
                            }
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isDragging) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF38BDF8), RoundedCornerShape(2.dp))
                                            .padding(horizontal = 3.dp, vertical = 1.dp)
                                    ) {
                                        Text("MOVE ⟷", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                } else if (mediaClip.isLocalVideo) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF06B6D4), RoundedCornerShape(2.dp))
                                            .padding(horizontal = 3.dp, vertical = 1.dp)
                                    ) {
                                        Text("LOCAL", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = mediaClip.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            if (isSelected) {
                                val durSec = mediaClip.effectiveDurationMs / 1000f
                                Text(
                                    text = String.format("%.1fs", durSec),
                                    color = Color(0xFFFEF08A),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else if (mediaClip.speed != 1.0f) {
                                Text(
                                    text = "${mediaClip.speed}x",
                                    color = Color(0xFFFDE047),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Right Trim Handle on selected clip
                        if (isSelected && !isDragging) {
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFFFACC15), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                                    .pointerInput(mediaClip.id) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val deltaMs = (dragAmount.x * msPerDp).toLong()
                                            val newEnd = (mediaClip.endTrimMs + deltaMs).coerceAtLeast(mediaClip.startTrimMs + 300L)
                                            onUpdateClipTrim?.invoke(mediaClip.id, mediaClip.startTrimMs, newEnd)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.width(1.5.dp).height(12.dp).background(Color.Black))
                            }
                        }
                    }
                }
                if (mediaClip.transitionToNext != TransitionType.NONE) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(0xFF0F172A), CircleShape)
                            .border(1.dp, Color(0xFF38BDF8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(mediaClip.transitionToNext.iconEmoji, fontSize = 9.sp)
                    }
                }
                Spacer(modifier = Modifier.width(2.dp))
            }

            // Multi-Item Insertion "+" Button for Video Track
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0xFF262A38), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(4.dp))
                    .clickable { onAddClipClick?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Video Clip",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun OverlayTrackRow(
    overlays: List<OverlayClip>,
    msPerDp: Float,
    selectedOverlayId: String? = null,
    onSelectOverlay: ((String) -> Unit)? = null,
    onMoveOverlay: ((String, Long) -> Unit)? = null,
    onUpdateOverlayDuration: ((String, Long, Long) -> Unit)? = null,
    onAddOverlayClick: (() -> Unit)? = null,
    onSeekTo: (Long) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color(0xFF171A24), RoundedCornerShape(4.dp))
            .padding(1.dp)
    ) {
        if (overlays.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(Color(0xFF7E22CE).copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(4.dp))
                    .clickable { onAddOverlayClick?.invoke() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("+ Add PiP / Overlay", color = Color(0xFFE9D5FF), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            overlays.forEach { overlay ->
                val isSelected = overlay.id == selectedOverlayId
                var isDragging by remember { mutableStateOf(false) }
                var dragStartMs by remember { mutableLongStateOf(overlay.startTimeMs) }
                LaunchedEffect(overlay.startTimeMs) {
                    if (!isDragging) dragStartMs = overlay.startTimeMs
                }
                val currentStartMs = if (isDragging) dragStartMs else overlay.startTimeMs
                val startDp = (currentStartMs / msPerDp).dp
                val widthDp = (overlay.durationMs / msPerDp).dp.coerceAtLeast(36.dp)

                Box(
                    modifier = Modifier
                        .offset(x = startDp)
                        .width(widthDp)
                        .height(30.dp)
                        .zIndex(if (isDragging) 10f else 1f)
                        .graphicsLayer {
                            scaleX = if (isDragging) 1.05f else 1f
                            scaleY = if (isDragging) 1.05f else 1f
                            shadowElevation = if (isDragging) 8f else 0f
                        }
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isDragging) Color(0xFFA855F7) else if (isSelected) Color(0xFF9333EA) else Color(0xFF7E22CE))
                        .border(
                            width = if (isDragging || isSelected) 2.dp else 0.5.dp,
                            color = if (isDragging) Color(0xFFE9D5FF) else if (isSelected) Color(0xFFFDE047) else Color(0xFFA855F7),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            onSelectOverlay?.invoke(overlay.id)
                            onSeekTo(overlay.startTimeMs)
                        }
                        .pointerInput(overlay.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    isDragging = true
                                    dragStartMs = overlay.startTimeMs
                                    onSelectOverlay?.invoke(overlay.id)
                                    triggerHapticFeedback(context, haptic)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val deltaMs = (dragAmount.x * msPerDp).toLong()
                                    dragStartMs = (dragStartMs + deltaMs).coerceAtLeast(0L)
                                    onMoveOverlay?.invoke(overlay.id, dragStartMs)
                                },
                                onDragEnd = { isDragging = false },
                                onDragCancel = { isDragging = false }
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected && !isDragging) {
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFFFACC15), RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                                    .pointerInput(overlay.id) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val deltaMs = (dragAmount.x * msPerDp).toLong()
                                            val newStart = (overlay.startTimeMs + deltaMs).coerceAtLeast(0L)
                                            onUpdateOverlayDuration?.invoke(overlay.id, newStart, overlay.durationMs)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.width(1.5.dp).height(10.dp).background(Color.Black))
                            }
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isDragging) "⟷ ${overlay.label}" else "${overlay.iconEmoji} ${overlay.label}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            if (overlay.chromaKeyEnabled && !isDragging) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF10B981), RoundedCornerShape(2.dp))
                                        .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                ) {
                                    Text("CHROMA", color = Color.Black, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (isSelected && !isDragging) {
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFFFACC15), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                                    .pointerInput(overlay.id) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val deltaMs = (dragAmount.x * msPerDp).toLong()
                                            val newDur = (overlay.durationMs + deltaMs).coerceAtLeast(500L)
                                            onUpdateOverlayDuration?.invoke(overlay.id, overlay.startTimeMs, newDur)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.width(1.5.dp).height(10.dp).background(Color.Black))
                            }
                        }
                    }
                }
            }

            // "+" Button to add more overlays
            val maxOverlayEnd = overlays.maxOfOrNull { it.startTimeMs + it.durationMs } ?: 0L
            val addBtnOffset = (maxOverlayEnd / msPerDp).dp + 6.dp
            Box(
                modifier = Modifier
                    .offset(x = addBtnOffset)
                    .size(28.dp)
                    .background(Color(0xFF261D3B), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(4.dp))
                    .clickable { onAddOverlayClick?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Overlay",
                    tint = Color(0xFFC084FC),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SubtitleTrackRow(
    subtitles: List<SubtitleClip>,
    msPerDp: Float,
    selectedSubtitleId: String?,
    onSelectSubtitle: ((String) -> Unit)?,
    onEditSubtitle: ((SubtitleClip) -> Unit)? = null,
    onMoveSubtitle: ((String, Long) -> Unit)? = null,
    onUpdateSubtitleDuration: ((String, Long, Long) -> Unit)?,
    onAddTextClick: (() -> Unit)?,
    onGenerateSubtitlesClick: (() -> Unit)?,
    onSeekTo: (Long) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(Color(0xFF171A24), RoundedCornerShape(4.dp))
            .padding(1.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (subtitles.isEmpty()) {
            Row(
                modifier = Modifier.padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFB45309).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                        .clickable { onAddTextClick?.invoke() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("+ Add Text", color = Color(0xFFFDE68A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0284C7).copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(4.dp))
                        .clickable { onGenerateSubtitlesClick?.invoke() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("✨ AI Audio Subtitles (Gemini)", color = Color(0xFFBAE6FD), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            subtitles.forEach { sub ->
                val isSelected = sub.id == selectedSubtitleId
                var isDragging by remember { mutableStateOf(false) }
                var dragStartMs by remember { mutableLongStateOf(sub.startMs) }
                LaunchedEffect(sub.startMs) {
                    if (!isDragging) dragStartMs = sub.startMs
                }
                val currentStartMs = if (isDragging) dragStartMs else sub.startMs
                val durMs = (sub.endMs - sub.startMs).coerceAtLeast(300L)
                val startDp = (currentStartMs / msPerDp).dp
                val widthDp = (durMs / msPerDp).dp.coerceAtLeast(36.dp)

                val subFont = when (sub.fontChoice) {
                    UrduFontChoice.NOTO_NASTALIQ -> NotoNastaliqUrduFontFamily
                    UrduFontChoice.GULZAR -> GulzarFontFamily
                    UrduFontChoice.AMIRI -> AmiriFontFamily
                    UrduFontChoice.LATEEF -> LateefFontFamily
                    UrduFontChoice.SYSTEM_SANS -> FontFamily.Default
                    UrduFontChoice.SERIF -> FontFamily.Serif
                    UrduFontChoice.IMPACT -> FontFamily.SansSerif
                    UrduFontChoice.CURSIVE -> FontFamily.Cursive
                    UrduFontChoice.CUSTOM_IMPORTED -> {
                        if (!sub.customFontPath.isNullOrBlank() && File(sub.customFontPath).exists()) {
                            try {
                                FontFamily(Font(File(sub.customFontPath)))
                            } catch (_: Exception) {
                                NotoNastaliqUrduFontFamily
                            }
                        } else NotoNastaliqUrduFontFamily
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(x = startDp)
                        .width(widthDp)
                        .height(32.dp)
                        .zIndex(if (isDragging) 10f else 1f)
                        .graphicsLayer {
                            scaleX = if (isDragging) 1.05f else 1f
                            scaleY = if (isDragging) 1.05f else 1f
                            shadowElevation = if (isDragging) 8f else 0f
                        }
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isDragging) Color(0xFFEA580C) else if (isSelected) Color(0xFFD97706) else Color(0xFF92400E))
                        .border(
                            width = if (isDragging || isSelected) 2.dp else 0.5.dp,
                            color = if (isDragging) Color(0xFFFDE047) else if (isSelected) Color(0xFFFACC15) else Color(0xFFF59E0B),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            onSelectSubtitle?.invoke(sub.id)
                            onSeekTo(sub.startMs)
                            onEditSubtitle?.invoke(sub)
                        }
                        .pointerInput(sub.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    isDragging = true
                                    dragStartMs = sub.startMs
                                    onSelectSubtitle?.invoke(sub.id)
                                    triggerHapticFeedback(context, haptic)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val deltaMs = (dragAmount.x * msPerDp).toLong()
                                    dragStartMs = (dragStartMs + deltaMs).coerceAtLeast(0L)
                                    onMoveSubtitle?.invoke(sub.id, dragStartMs)
                                },
                                onDragEnd = { isDragging = false },
                                onDragCancel = { isDragging = false }
                            )
                        }
                        .padding(horizontal = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left trim handle (adjust startMs)
                        if (isSelected && !isDragging) {
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFFFACC15), RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                                    .pointerInput(sub.id) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val deltaMs = (dragAmount.x * msPerDp).toLong()
                                            val newStart = (sub.startMs + deltaMs).coerceIn(0L, sub.endMs - 300L)
                                            onUpdateSubtitleDuration?.invoke(sub.id, newStart, sub.endMs)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.width(2.dp).height(12.dp).background(Color.Black))
                            }
                        }

                        // Middle text & duration
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isDragging) "⟷ ${sub.text}" else sub.text,
                                color = Color.White,
                                fontFamily = subFont,
                                fontSize = if (sub.isRtl) 10.5.sp else 10.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = true)
                                )
                            )
                            if (isSelected || isDragging) {
                                val durSec = durMs / 1000f
                                Text(
                                    text = if (isDragging) String.format("@%.1fs", currentStartMs / 1000f) else String.format("%.1fs", durSec),
                                    color = Color(0xFFFEF08A),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // Right trim handle (adjust endMs)
                        if (isSelected && !isDragging) {
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFFFACC15), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                                    .pointerInput(sub.id) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val deltaMs = (dragAmount.x * msPerDp).toLong()
                                            val newEnd = (sub.endMs + deltaMs).coerceAtLeast(sub.startMs + 300L)
                                            onUpdateSubtitleDuration?.invoke(sub.id, sub.startMs, newEnd)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.width(2.dp).height(12.dp).background(Color.Black))
                            }
                        }
                    }
                }
            }

            // "+" Button to add more text layers
            val maxSubEnd = subtitles.maxOfOrNull { it.endMs } ?: 0L
            val addTextOffset = (maxSubEnd / msPerDp).dp + 6.dp
            Box(
                modifier = Modifier
                    .offset(x = addTextOffset)
                    .size(28.dp)
                    .background(Color(0xFF2C2216), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                    .clickable { onAddTextClick?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Text Layer",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AudioTrackRow(
    audioTracks: List<AudioTrack>,
    msPerDp: Float,
    selectedAudioTrackId: String? = null,
    onSelectAudioTrack: ((String) -> Unit)? = null,
    onMoveAudioTrack: ((String, Long) -> Unit)? = null,
    onAddAudioClick: (() -> Unit)? = null,
    onSeekTo: (Long) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color(0xFF171A24), RoundedCornerShape(4.dp))
            .padding(1.dp)
    ) {
        if (audioTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(Color(0xFF065F46).copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(4.dp))
                    .clickable { onAddAudioClick?.invoke() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("+ Add Music / Audio", color = Color(0xFFA7F3D0), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            audioTracks.forEach { audio ->
                val isSelected = audio.id == selectedAudioTrackId
                var isDragging by remember { mutableStateOf(false) }
                var dragStartMs by remember { mutableLongStateOf(audio.startTimeMs) }
                LaunchedEffect(audio.startTimeMs) {
                    if (!isDragging) dragStartMs = audio.startTimeMs
                }
                val currentStartMs = if (isDragging) dragStartMs else audio.startTimeMs
                val startDp = (currentStartMs / msPerDp).dp
                val widthDp = (audio.durationMs / msPerDp).dp.coerceAtLeast(30.dp)

                Box(
                    modifier = Modifier
                        .offset(x = startDp)
                        .width(widthDp)
                        .height(30.dp)
                        .zIndex(if (isDragging) 10f else 1f)
                        .graphicsLayer {
                            scaleX = if (isDragging) 1.05f else 1f
                            scaleY = if (isDragging) 1.05f else 1f
                            shadowElevation = if (isDragging) 8f else 0f
                        }
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isDragging) Color(0xFF059669) else if (isSelected) Color(0xFF047857) else Color(0xFF065F46))
                        .border(
                            width = if (isDragging || isSelected) 2.dp else 0.5.dp,
                            color = if (isDragging) Color(0xFF6EE7B7) else if (isSelected) Color(0xFFFACC15) else Color(0xFF10B981),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            onSelectAudioTrack?.invoke(audio.id)
                            onSeekTo(audio.startTimeMs)
                        }
                        .pointerInput(audio.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    isDragging = true
                                    dragStartMs = audio.startTimeMs
                                    onSelectAudioTrack?.invoke(audio.id)
                                    triggerHapticFeedback(context, haptic)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val deltaMs = (dragAmount.x * msPerDp).toLong()
                                    dragStartMs = (dragStartMs + deltaMs).coerceAtLeast(0L)
                                    onMoveAudioTrack?.invoke(audio.id, dragStartMs)
                                },
                                onDragEnd = { isDragging = false },
                                onDragCancel = { isDragging = false }
                            )
                        }
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                            if (isDragging) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF34D399), RoundedCornerShape(2.dp))
                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                ) {
                                    Text("MOVE ⟷", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            } else if (audio.isExtracted) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF0284C7), RoundedCornerShape(2.dp))
                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                ) {
                                    Text("EXTRACTED", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            } else if (!audio.uriString.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF059669), RoundedCornerShape(2.dp))
                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                ) {
                                    Text("DEVICE", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "♪ ${audio.name}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                        if (isDragging || isSelected) {
                            Text(
                                text = if (isDragging) String.format("@%.1fs", currentStartMs / 1000f) else "${(audio.volume * 100).toInt()}%",
                                color = Color(0xFFFEF08A),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "${(audio.volume * 100).toInt()}%",
                                color = Color(0xFF6EE7B7),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            // "+" Button to add more audio tracks
            val maxAudioEnd = audioTracks.maxOfOrNull { it.startTimeMs + it.durationMs } ?: 0L
            val addAudioOffset = (maxAudioEnd / msPerDp).dp + 6.dp
            Box(
                modifier = Modifier
                    .offset(x = addAudioOffset)
                    .size(28.dp)
                    .background(Color(0xFF142E25), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(4.dp))
                    .clickable { onAddAudioClick?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Audio Track",
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
