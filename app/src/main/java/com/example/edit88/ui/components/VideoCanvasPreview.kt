package com.example.edit88.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.AspectRatioType
import com.example.edit88.model.ColorGrading
import com.example.edit88.model.FilterType
import com.example.edit88.model.MediaClip
import com.example.edit88.model.OverlayClip
import com.example.edit88.model.Project
import com.example.edit88.model.SubtitleClip
import com.example.edit88.typography.UrduSubtitleRenderer
import kotlin.math.sin

@Composable
fun VideoCanvasPreview(
    project: Project,
    currentPlayheadMs: Long,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine active clip at playhead
    var accumulatedMs = 0L
    var activeClip: MediaClip? = null
    var clipLocalOffsetMs = 0L

    for (clip in project.clips) {
        val dur = clip.effectiveDurationMs
        if (currentPlayheadMs in accumulatedMs until (accumulatedMs + dur)) {
            activeClip = clip
            clipLocalOffsetMs = currentPlayheadMs - accumulatedMs
            break
        }
        accumulatedMs += dur
    }
    if (activeClip == null && project.clips.isNotEmpty()) {
        activeClip = project.clips.last()
        clipLocalOffsetMs = activeClip.effectiveDurationMs
    }

    // Active subtitles at playhead
    val activeSubtitle = project.subtitles.firstOrNull {
        currentPlayheadMs in it.startMs..it.endMs
    }

    // Active overlays at playhead
    val activeOverlays = project.overlays.filter {
        currentPlayheadMs in it.startTimeMs..(it.startTimeMs + it.durationMs)
    }

    // Continuous visual motion animation
    val infiniteTransition = rememberInfiniteTransition(label = "canvas_anim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0B0C10)),
        contentAlignment = Alignment.Center
    ) {
        // Container with aspect ratio bounding box
        BoxWithConstraints(
            modifier = Modifier
                .padding(12.dp)
                .wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            val aspect = project.aspectRatio.ratio
            val canvasModifier = Modifier
                .aspectRatio(aspect)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF232635), RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTogglePlay
                )

            Box(
                modifier = canvasModifier,
                contentAlignment = Alignment.Center
            ) {
                // Procedural Live Video Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val primaryColor = Color(activeClip?.primaryColorHex ?: 0xFF3B82F6)
                    val secondaryColor = Color(activeClip?.secondaryColorHex ?: 0xFF8B5CF6)
                    val filter = activeClip?.filter ?: FilterType.NONE
                    val grading = activeClip?.colorGrading ?: ColorGrading()

                    // Background Gradient
                    val bgBrush = when (filter) {
                        FilterType.CYBERPUNK -> Brush.verticalGradient(
                            listOf(Color(0xFF0F051D), Color(0xFF2E0854), Color(0xFF042F2E))
                        )
                        FilterType.CINEMATIC -> Brush.linearGradient(
                            listOf(Color(0xFF041C24), Color(0xFF134E4A), Color(0xFF78350F))
                        )
                        FilterType.RETRO -> Brush.verticalGradient(
                            listOf(Color(0xFF2C1810), Color(0xFF5A3825), Color(0xFF78593E))
                        )
                        FilterType.MONO -> Brush.linearGradient(
                            listOf(Color(0xFF111111), Color(0xFF333333), Color(0xFF000000))
                        )
                        FilterType.WARM -> Brush.verticalGradient(
                            listOf(Color(0xFF3B1E08), Color(0xFF854D0E), Color(0xFFCA8A04))
                        )
                        FilterType.COLD -> Brush.verticalGradient(
                            listOf(Color(0xFF082F49), Color(0xFF0284C7), Color(0xFF0C4A6E))
                        )
                        else -> Brush.radialGradient(
                            colors = listOf(secondaryColor.copy(alpha = 0.8f), primaryColor.copy(alpha = 0.9f), Color(0xFF0F172A)),
                            center = Offset(w / 2, h / 2),
                            radius = maxOf(w, h)
                        )
                    }

                    drawRect(brush = bgBrush)

                    // Cinematic Dynamic Waveform & Motion Elements
                    val numBars = 24
                    val barWidth = w / (numBars * 2f)
                    for (i in 0 until numBars) {
                        val x = (i * 2 + 1) * barWidth
                        val barHeightFactor = (sin(wavePhase + i * 0.4f) + 1f) / 2f
                        val barH = (h * 0.25f) * barHeightFactor + 10f

                        val barBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF38BDF8).copy(alpha = 0.7f),
                                Color(0xFFA855F7).copy(alpha = 0.9f)
                            ),
                            startY = h / 2 - barH,
                            endY = h / 2 + barH
                        )

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(x, h / 2 - barH / 2),
                            size = androidx.compose.ui.geometry.Size(barWidth * 0.8f, barH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                    }

                    // Ambient Central Ring Glow
                    drawCircle(
                        color = Color.White.copy(alpha = 0.12f),
                        radius = (w * 0.28f) + (sin(wavePhase) * 12f),
                        center = Offset(w / 2, h / 2),
                        style = Stroke(width = 3f)
                    )

                    // Vignette Mask
                    if (grading.vignette > 0.05f) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = grading.vignette.coerceIn(0f, 0.85f))),
                                center = Offset(w / 2, h / 2),
                                radius = maxOf(w, h) * 0.7f
                            )
                        )
                    }
                }

                // Scene Title Watermark
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .background(Color(0x99000000), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = activeClip?.visualTheme ?: "Edit88",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Aspect Ratio indicator badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .background(Color(0x99000000), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = project.aspectRatio.label,
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Overlays
                activeOverlays.forEach { overlay ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset {
                                IntOffset(
                                    (overlay.posX * 120).toInt(),
                                    (overlay.posY * 120).toInt()
                                )
                            }
                            .scale(overlay.scale)
                            .background(Color(0x77000000), CircleShape)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = overlay.iconEmoji,
                            fontSize = 32.sp
                        )
                    }
                }

                // Active Subtitle / Urdu Captions
                if (activeSubtitle != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 28.dp)
                    ) {
                        UrduSubtitleRenderer(
                            clip = activeSubtitle,
                            currentPlayheadMs = currentPlayheadMs
                        )
                    }
                }

                // Play / Pause Overlay Icon when paused
                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0x99000000), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}
