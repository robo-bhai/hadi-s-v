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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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

@Composable
fun MultiTrackTimeline(
    project: Project,
    currentPlayheadMs: Long,
    selectedClipId: String?,
    onSelectClip: (String) -> Unit,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Scale: 1000ms = 60dp
    val msPerDp = 1000f / 60f
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
                // Fixed Track Icon Rail
                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF161822))
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TrackIconItem(icon = Icons.Default.Movie, color = TrackVideoColor, label = "Video")
                    TrackIconItem(icon = Icons.Default.Layers, color = TrackOverlayColor, label = "PiP")
                    TrackIconItem(icon = Icons.Default.TextFields, color = TrackSubtitleColor, label = "Urdu")
                    TrackIconItem(icon = Icons.Default.Audiotrack, color = TrackAudioColor, label = "Audio")
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
                        .clickable {
                            // Quick click seek
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .width(timelineWidthDp + 200.dp)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 1. Video Track
                        VideoTrackRow(
                            clips = project.clips,
                            msPerDp = msPerDp,
                            selectedClipId = selectedClipId,
                            onSelectClip = onSelectClip
                        )

                        // 2. Overlay Track
                        OverlayTrackRow(
                            overlays = project.overlays,
                            msPerDp = msPerDp,
                            onSeekTo = onSeekTo
                        )

                        // 3. Subtitle / Urdu Track
                        SubtitleTrackRow(
                            subtitles = project.subtitles,
                            msPerDp = msPerDp,
                            onSeekTo = onSeekTo
                        )

                        // 4. Audio Track
                        AudioTrackRow(
                            audioTracks = project.audioTracks,
                            msPerDp = msPerDp,
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
private fun TrackIconItem(icon: ImageVector, color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            color = Color(0xFF64748B),
            fontSize = 9.sp
        )
    }
}

@Composable
private fun VideoTrackRow(
    clips: List<MediaClip>,
    msPerDp: Float,
    selectedClipId: String?,
    onSelectClip: (String) -> Unit
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
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("+ Add Media", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
        } else {
            clips.forEach { clip ->
                val clipWidthDp = (clip.effectiveDurationMs / msPerDp).dp.coerceAtLeast(36.dp)
                val isSelected = clip.id == selectedClipId

                Box(
                    modifier = Modifier
                        .width(clipWidthDp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(clip.primaryColorHex).copy(alpha = 0.85f),
                                    Color(clip.secondaryColorHex).copy(alpha = 0.85f)
                                )
                            )
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.5.dp,
                            color = if (isSelected) Color(0xFFFACC15) else Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onSelectClip(clip.id) }
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = clip.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (clip.speed != 1.0f) {
                            Text(
                                text = "${clip.speed}x",
                                color = Color(0xFFFDE047),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}

@Composable
private fun OverlayTrackRow(
    overlays: List<OverlayClip>,
    msPerDp: Float,
    onSeekTo: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color(0xFF171A24), RoundedCornerShape(4.dp))
            .padding(1.dp)
    ) {
        overlays.forEach { overlay ->
            val startDp = (overlay.startTimeMs / msPerDp).dp
            val widthDp = (overlay.durationMs / msPerDp).dp.coerceAtLeast(30.dp)

            Box(
                modifier = Modifier
                    .offset(x = startDp)
                    .width(widthDp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF7E22CE))
                    .border(0.5.dp, Color(0xFFA855F7), RoundedCornerShape(4.dp))
                    .clickable { onSeekTo(overlay.startTimeMs) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "${overlay.iconEmoji} ${overlay.label}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SubtitleTrackRow(
    subtitles: List<SubtitleClip>,
    msPerDp: Float,
    onSeekTo: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color(0xFF171A24), RoundedCornerShape(4.dp))
            .padding(1.dp)
    ) {
        subtitles.forEach { sub ->
            val startDp = (sub.startMs / msPerDp).dp
            val widthDp = ((sub.endMs - sub.startMs) / msPerDp).dp.coerceAtLeast(24.dp)

            Box(
                modifier = Modifier
                    .offset(x = startDp)
                    .width(widthDp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFB45309))
                    .border(0.5.dp, Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                    .clickable { onSeekTo(sub.startMs) }
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sub.text,
                    color = Color.White,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun AudioTrackRow(
    audioTracks: List<AudioTrack>,
    msPerDp: Float,
    onSeekTo: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color(0xFF171A24), RoundedCornerShape(4.dp))
            .padding(1.dp)
    ) {
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
                    Text(
                        text = "♪ ${audio.name}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = "${(audio.volume * 100).toInt()}%",
                        color = Color(0xFF6EE7B7),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
