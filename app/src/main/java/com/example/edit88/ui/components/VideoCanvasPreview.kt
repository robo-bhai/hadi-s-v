package com.example.edit88.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.ui.viewinterop.AndroidView
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
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun VideoCanvasPreview(
    project: Project,
    currentPlayheadMs: Long,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onUpdateOverlay: ((OverlayClip) -> Unit)? = null,
    onDeleteOverlay: ((String) -> Unit)? = null,
    onUpdateSubtitlePosition: ((String, Float, Float, Float) -> Unit)? = null,
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

    var selectedOverlayId by remember { mutableStateOf<String?>(null) }
    var isSubtitleSelected by remember { mutableStateOf(false) }

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

    val primaryColor = Color(activeClip?.primaryColorHex ?: 0xFF3B82F6)
    val secondaryColor = Color(activeClip?.secondaryColorHex ?: 0xFF8B5CF6)
    val grading = activeClip?.colorGrading ?: ColorGrading()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF090A0E)),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Ambient Blurred Video Background Fill (CapCut style)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryColor.copy(alpha = 0.25f),
                        primaryColor.copy(alpha = 0.18f),
                        Color(0xFF090A0E)
                    ),
                    center = Offset(w / 2, h / 2),
                    radius = maxOf(w, h) * 0.8f
                )
            )
        }

        // Bounded Video Player Canvas
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
                    onClick = {
                        selectedOverlayId = null
                        isSubtitleSelected = false
                        onTogglePlay()
                    }
                )

            Box(
                modifier = canvasModifier,
                contentAlignment = Alignment.Center
            ) {
                if (activeClip?.isLocalVideo == true && !activeClip.uriString.isNullOrBlank()) {
                    // Real device video playback from local storage
                    LocalVideoPlayer(
                        uriString = activeClip.uriString,
                        isPlaying = isPlaying,
                        currentOffsetMs = clipLocalOffsetMs,
                        volume = activeClip.volume,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Real-time Canvas Filters & Color Grading overlay over local video
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val filter = activeClip.filter
                        val brightnessShift = (grading.brightness + grading.exposure * 0.5f).coerceIn(-0.8f, 0.8f)

                        // Filter Tint Layer
                        when (filter) {
                            FilterType.CYBERPUNK -> drawRect(color = Color(0xFF6B21A8).copy(alpha = 0.22f))
                            FilterType.CINEMATIC -> drawRect(color = Color(0xFF0F766E).copy(alpha = 0.20f))
                            FilterType.RETRO -> drawRect(color = Color(0xFF78350F).copy(alpha = 0.24f))
                            FilterType.MONO -> drawRect(color = Color(0xFF1E293B).copy(alpha = 0.25f))
                            FilterType.WARM -> drawRect(color = Color(0xFFF59E0B).copy(alpha = 0.18f))
                            FilterType.COLD -> drawRect(color = Color(0xFF0284C7).copy(alpha = 0.18f))
                            else -> {}
                        }

                        // Temperature Warm / Cool Overlay
                        if (grading.temperature != 0f) {
                            val tempColor = if (grading.temperature > 0f) {
                                Color(0xFFF59E0B).copy(alpha = (grading.temperature * 0.25f).coerceIn(0f, 0.4f))
                            } else {
                                Color(0xFF0284C7).copy(alpha = (-grading.temperature * 0.25f).coerceIn(0f, 0.4f))
                            }
                            drawRect(color = tempColor)
                        }

                        // Brightness adjustment layer
                        if (brightnessShift != 0f) {
                            val bColor = if (brightnessShift > 0f) Color.White.copy(alpha = brightnessShift) else Color.Black.copy(alpha = -brightnessShift)
                            drawRect(color = bColor)
                        }

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
                } else {
                    // Procedural Live Video Canvas with Shaders and Color Matrix
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val filter = activeClip?.filter ?: FilterType.NONE

                        // Apply Brightness & Exposure shifts
                        val brightnessShift = (grading.brightness + grading.exposure * 0.5f).coerceIn(-0.8f, 0.8f)

                        // Base Gradient Shader
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
                                colors = listOf(
                                    secondaryColor.copy(alpha = 0.85f),
                                    primaryColor.copy(alpha = 0.9f),
                                    Color(0xFF0F172A)
                                ),
                                center = Offset(w / 2, h / 2),
                                radius = maxOf(w, h)
                            )
                        }

                        drawRect(brush = bgBrush)

                        // Temperature Warm / Cool Overlay
                        if (grading.temperature != 0f) {
                            val tempColor = if (grading.temperature > 0f) {
                                Color(0xFFF59E0B).copy(alpha = (grading.temperature * 0.25f).coerceIn(0f, 0.4f))
                            } else {
                                Color(0xFF0284C7).copy(alpha = (-grading.temperature * 0.25f).coerceIn(0f, 0.4f))
                            }
                            drawRect(color = tempColor)
                        }

                        // Brightness adjustment layer
                        if (brightnessShift != 0f) {
                            val bColor = if (brightnessShift > 0f) Color.White.copy(alpha = brightnessShift) else Color.Black.copy(alpha = -brightnessShift)
                            drawRect(color = bColor)
                        }

                        // Dynamic Waveform & Motion Elements
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

                        // Central Ring Glow
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

                // Interactive Overlays with On-Canvas Bounding Box
                activeOverlays.forEach { overlay ->
                    val isSelected = overlay.id == selectedOverlayId
                    var currentPosX by remember(overlay.posX) { mutableFloatStateOf(overlay.posX) }
                    var currentPosY by remember(overlay.posY) { mutableFloatStateOf(overlay.posY) }
                    var currentScale by remember(overlay.scale) { mutableFloatStateOf(overlay.scale) }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset {
                                IntOffset(
                                    ((currentPosX - 0.5f) * 260).roundToInt(),
                                    ((currentPosY - 0.5f) * 360).roundToInt()
                                )
                            }
                            .rotate(overlay.rotation)
                            .scale(currentScale)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .border(1.5.dp, Color(0xFF06B6D4), RoundedCornerShape(8.dp))
                                        .background(Color(0x3306B6D4), RoundedCornerShape(8.dp))
                                } else Modifier
                            )
                            .pointerInput(overlay.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    selectedOverlayId = overlay.id
                                    isSubtitleSelected = false
                                    currentPosX = (currentPosX + (dragAmount.x / 400f)).coerceIn(0.05f, 0.95f)
                                    currentPosY = (currentPosY + (dragAmount.y / 500f)).coerceIn(0.05f, 0.95f)
                                    onUpdateOverlay?.invoke(
                                        overlay.copy(posX = currentPosX, posY = currentPosY, scale = currentScale)
                                    )
                                }
                            }
                            .clickable {
                                selectedOverlayId = overlay.id
                                isSubtitleSelected = false
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = overlay.iconEmoji,
                            fontSize = 32.sp
                        )

                        // On-Canvas Gizmo Handles when selected
                        if (isSelected) {
                            // Delete button (Top-Right)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 10.dp, y = (-10).dp)
                                    .size(18.dp)
                                    .background(Color(0xFFEF4444), CircleShape)
                                    .clickable { onDeleteOverlay?.invoke(overlay.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            // Scale Handle (Bottom-Right)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 8.dp, y = 8.dp)
                                    .size(14.dp)
                                    .background(Color(0xFF06B6D4), CircleShape)
                                    .border(1.dp, Color.White, CircleShape)
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val scaleDelta = (dragAmount.x + dragAmount.y) / 100f
                                            currentScale = (currentScale + scaleDelta).coerceIn(0.5f, 3.0f)
                                            onUpdateOverlay?.invoke(
                                                overlay.copy(scale = currentScale)
                                            )
                                        }
                                    }
                            )
                        }
                    }
                }

                // Active Subtitle / Urdu Captions with Interactive Placement
                if (activeSubtitle != null) {
                    var subPosX by remember(activeSubtitle.posX) { mutableFloatStateOf(activeSubtitle.posX) }
                    var subPosY by remember(activeSubtitle.posY) { mutableFloatStateOf(activeSubtitle.posY) }
                    var subScale by remember(activeSubtitle.scale) { mutableFloatStateOf(activeSubtitle.scale) }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset {
                                IntOffset(
                                    ((subPosX - 0.5f) * 260).roundToInt(),
                                    ((subPosY - 0.5f) * 360).roundToInt()
                                )
                            }
                            .scale(subScale)
                            .then(
                                if (isSubtitleSelected) {
                                    Modifier
                                        .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                                        .background(Color(0x22F59E0B), RoundedCornerShape(8.dp))
                                } else Modifier
                            )
                            .pointerInput(activeSubtitle.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    isSubtitleSelected = true
                                    selectedOverlayId = null
                                    subPosX = (subPosX + (dragAmount.x / 400f)).coerceIn(0.05f, 0.95f)
                                    subPosY = (subPosY + (dragAmount.y / 500f)).coerceIn(0.05f, 0.95f)
                                    onUpdateSubtitlePosition?.invoke(
                                        activeSubtitle.id, subPosX, subPosY, subScale
                                    )
                                }
                            }
                            .clickable {
                                isSubtitleSelected = !isSubtitleSelected
                                selectedOverlayId = null
                            }
                            .padding(4.dp)
                    ) {
                        UrduSubtitleRenderer(
                            clip = activeSubtitle,
                            currentPlayheadMs = currentPlayheadMs
                        )

                        // Subtitle scale handle
                        if (isSubtitleSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 6.dp, y = 6.dp)
                                    .size(14.dp)
                                    .background(Color(0xFFF59E0B), CircleShape)
                                    .border(1.dp, Color.White, CircleShape)
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val scaleDelta = (dragAmount.x + dragAmount.y) / 100f
                                            subScale = (subScale + scaleDelta).coerceIn(0.6f, 2.5f)
                                            onUpdateSubtitlePosition?.invoke(
                                                activeSubtitle.id, subPosX, subPosY, subScale
                                            )
                                        }
                                    }
                            )
                        }
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

@Composable
private fun LocalVideoPlayer(
    uriString: String,
    isPlaying: Boolean,
    currentOffsetMs: Long,
    volume: Float,
    modifier: Modifier = Modifier
) {
    val uri = remember(uriString) { Uri.parse(uriString) }

    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(uri)
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    try {
                        mp.setVolume(volume, volume)
                    } catch (_: Exception) {}
                    seekTo(currentOffsetMs.toInt())
                    if (isPlaying) {
                        start()
                    } else {
                        pause()
                    }
                }
                setOnErrorListener { _, _, _ -> true }
            }
        },
        update = { videoView ->
            try {
                if (isPlaying) {
                    if (!videoView.isPlaying) {
                        videoView.seekTo(currentOffsetMs.toInt())
                        videoView.start()
                    }
                } else {
                    if (videoView.isPlaying) {
                        videoView.pause()
                    }
                    val currentPos = videoView.currentPosition
                    if (kotlin.math.abs(currentPos - currentOffsetMs.toInt()) > 300) {
                        videoView.seekTo(currentOffsetMs.toInt())
                    }
                }
            } catch (_: Exception) {}
        },
        modifier = modifier
    )
}

