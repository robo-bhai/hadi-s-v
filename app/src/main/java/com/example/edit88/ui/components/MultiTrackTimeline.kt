package com.example.edit88.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.AudioTrack
import com.example.edit88.model.MediaClip
import com.example.edit88.model.OverlayClip
import com.example.edit88.model.Project
import com.example.edit88.model.SubtitleClip
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.TrackAudioColor
import com.example.ui.theme.TrackOverlayColor
import com.example.ui.theme.TrackSubtitleColor
import com.example.ui.theme.TrackVideoColor

import com.example.edit88.model.TransitionType

@Composable
fun MultiTrackTimeline(
    project: Project,
    currentPlayheadMs: Long,
    selectedClipId: String?,
    selectedSubtitleId: String? = null,
    selectedOverlayId: String? = null,
    timelineZoom: Float = 1.0f,
    onSelectClip: (String) -> Unit,
    onSelectSubtitle: ((String) -> Unit)? = null,
    onSelectOverlay: ((String) -> Unit)? = null,
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

    // Scale: 1000ms = 60dp (scaled by zoom)
    val msPerDp = (1000f / 60f) / timelineZoom.coerceIn(0.5f, 3.0f)
    val totalDurationMs = project.totalDurationMs.coerceAtLeast(15000L)
    val timelineWidthDp = (totalDurationMs / msPerDp).dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF13151D))
            .border(1.dp, Color(0xFF222533))
    ) {
        // Track Labels & Time Ruler Header
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

            // Scrollable Time Ruler
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier.width(timelineWidthDp + 200.dp)
                ) {
                    val stepSec = 2
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

                // Scrollable Tracks Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(scrollState)
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
                            onUpdateClipTrim = onUpdateClipTrim,
                            onAddClipClick = onAddVideoClipClick
                        )

                        // 2. Overlay Track
                        OverlayTrackRow(
                            overlays = project.overlays,
                            msPerDp = msPerDp,
                            selectedOverlayId = selectedOverlayId,
                            onSelectOverlay = onSelectOverlay,
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
                            onUpdateSubtitleDuration = onUpdateSubtitleDuration,
                            onAddTextClick = onAddTextClick,
                            onGenerateSubtitlesClick = onGenerateSubtitlesClick,
                            onSeekTo = onSeekTo
                        )

                        // 4. Audio Track
                        AudioTrackRow(
                            audioTracks = project.audioTracks,
                            msPerDp = msPerDp,
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
    onUpdateClipTrim: ((String, Long, Long) -> Unit)? = null,
    onAddClipClick: (() -> Unit)? = null
) {
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
            clips.forEach { mediaClip ->
                val clipWidthDp = (mediaClip.effectiveDurationMs / msPerDp).dp.coerceAtLeast(36.dp)
                val isSelected = mediaClip.id == selectedClipId

                Box(
                    modifier = Modifier
                        .width(clipWidthDp)
                        .height(34.dp)
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
                            width = if (isSelected) 2.dp else 0.5.dp,
                            color = if (isSelected) Color(0xFFFACC15) else Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onSelectClip(mediaClip.id) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Trim Handle on selected clip
                        if (isSelected) {
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
                                if (mediaClip.isLocalVideo) {
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
                        if (isSelected) {
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
    onUpdateOverlayDuration: ((String, Long, Long) -> Unit)? = null,
    onAddOverlayClick: (() -> Unit)? = null,
    onSeekTo: (Long) -> Unit
) {
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
                val startDp = (overlay.startTimeMs / msPerDp).dp
                val widthDp = (overlay.durationMs / msPerDp).dp.coerceAtLeast(36.dp)

                Box(
                    modifier = Modifier
                        .offset(x = startDp)
                        .width(widthDp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) Color(0xFF9333EA) else Color(0xFF7E22CE))
                        .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) Color(0xFFFDE047) else Color(0xFFA855F7),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            onSelectOverlay?.invoke(overlay.id)
                            onSeekTo(overlay.startTimeMs)
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
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
                                text = "${overlay.iconEmoji} ${overlay.label}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            if (overlay.chromaKeyEnabled) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF10B981), RoundedCornerShape(2.dp))
                                        .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                ) {
                                    Text("CHROMA", color = Color.Black, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (isSelected) {
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
    onUpdateSubtitleDuration: ((String, Long, Long) -> Unit)?,
    onAddTextClick: (() -> Unit)?,
    onGenerateSubtitlesClick: (() -> Unit)?,
    onSeekTo: (Long) -> Unit
) {
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
                        .background(Color(0xFF0284C7).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(4.dp))
                        .clickable { onGenerateSubtitlesClick?.invoke() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("✨ Generate Subtitles", color = Color(0xFFBAE6FD), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            subtitles.forEach { sub ->
                val isSelected = sub.id == selectedSubtitleId
                val startDp = (sub.startMs / msPerDp).dp
                val widthDp = ((sub.endMs - sub.startMs) / msPerDp).dp.coerceAtLeast(36.dp)

                Box(
                    modifier = Modifier
                        .offset(x = startDp)
                        .width(widthDp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) Color(0xFFD97706) else Color(0xFF92400E))
                        .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) Color(0xFFFDE047) else Color(0xFFF59E0B),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            onSelectSubtitle?.invoke(sub.id)
                            onSeekTo(sub.startMs)
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
                        if (isSelected) {
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
                                text = sub.text,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            if (isSelected) {
                                val durSec = (sub.endMs - sub.startMs) / 1000f
                                Text(
                                    text = String.format("%.1fs", durSec),
                                    color = Color(0xFFFEF08A),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // Right trim handle (adjust endMs)
                        if (isSelected) {
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
    onAddAudioClick: (() -> Unit)? = null,
    onSeekTo: (Long) -> Unit
) {
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
            val startDp = (audio.startTimeMs / msPerDp).dp
            val widthDp = (audio.durationMs / msPerDp).dp.coerceAtLeast(30.dp)

            Box(
                modifier = Modifier
                    .offset(x = startDp)
                    .width(widthDp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF047857))
                    .border(0.5.dp, Color(0xFF10B981), RoundedCornerShape(4.dp))
                    .clickable { onSeekTo(audio.startTimeMs) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                        if (audio.isExtracted) {
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
                    Text(
                        text = "${(audio.volume * 100).toInt()}%",
                        color = Color(0xFF6EE7B7),
                        fontSize = 9.sp
                    )
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
