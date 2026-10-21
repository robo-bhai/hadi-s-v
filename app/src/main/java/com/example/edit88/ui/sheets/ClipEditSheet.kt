package com.example.edit88.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.MediaClip
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkSurface

@Composable
fun ClipEditSheet(
    clip: MediaClip,
    currentPlayheadMs: Long,
    onUpdateTrim: (clipId: String, startTrimMs: Long, endTrimMs: Long) -> Unit,
    onUpdateSpeed: (clipId: String, speed: Float) -> Unit,
    onSplitAtPlayhead: () -> Unit,
    onExtractAudio: () -> Unit,
    onDuplicateClip: () -> Unit,
    onDeleteClip: () -> Unit,
    onOpenFilters: () -> Unit,
    onOpenSpeedCurves: () -> Unit,
    onClose: () -> Unit
) {
    var startTrimSec by remember(clip.id, clip.startTrimMs) {
        mutableFloatStateOf(clip.startTrimMs / 1000f)
    }
    var endTrimSec by remember(clip.id, clip.endTrimMs) {
        mutableFloatStateOf(clip.endTrimMs / 1000f)
    }
    val totalClipDurSec = (clip.durationMs / 1000f).coerceAtLeast(1f)

    val speedPresets = listOf(0.2f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f, 5.0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Handle bar
        Box(
            modifier = Modifier
                .size(40.dp, 4.dp)
                .background(Color(0xFF334155), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Clip Editing & Trim",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = clip.title,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
            IconButton(onClick = onDeleteClip) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Clip", tint = Color(0xFFF87171))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Clip Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1E2436), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF2C344E), RoundedCornerShape(8.dp))
                    .clickable { onSplitAtPlayhead() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Split", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1E2436), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF2C344E), RoundedCornerShape(8.dp))
                    .clickable { onOpenFilters() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ColorLens, contentDescription = null, tint = AccentPink, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filters", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1E2436), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF2C344E), RoundedCornerShape(8.dp))
                    .clickable { onExtractAudio() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Extract", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1E2436), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF2C344E), RoundedCornerShape(8.dp))
                    .clickable { onDuplicateClip() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clone", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. TRIM & DURATION CONTROLS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161925), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF282E40), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContentCut, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Trim Range & Duration",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Reset",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        modifier = Modifier.clickable {
                            startTrimSec = 0f
                            endTrimSec = totalClipDurSec
                            onUpdateTrim(clip.id, 0L, clip.durationMs)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Effective Duration Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F121C), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Current Timeline Duration:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(
                        text = String.format("%.2f s", clip.effectiveDurationMs / 1000f),
                        color = Color(0xFFFACC15),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Start Trim Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Trim Start (in-point):", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(String.format("%.2f s", startTrimSec), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = startTrimSec,
                    onValueChange = { newStart ->
                        startTrimSec = newStart.coerceIn(0f, endTrimSec - 0.3f)
                        onUpdateTrim(clip.id, (startTrimSec * 1000).toLong(), (endTrimSec * 1000).toLong())
                    },
                    valueRange = 0f..totalClipDurSec,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFACC15), activeTrackColor = Color(0xFFFACC15))
                )

                // End Trim Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Trim End (out-point):", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(String.format("%.2f s", endTrimSec), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = endTrimSec,
                    onValueChange = { newEnd ->
                        endTrimSec = newEnd.coerceIn(startTrimSec + 0.3f, totalClipDurSec)
                        onUpdateTrim(clip.id, (startTrimSec * 1000).toLong(), (endTrimSec * 1000).toLong())
                    },
                    valueRange = 0f..totalClipDurSec,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFACC15), activeTrackColor = Color(0xFFFACC15))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. SPEED CONTROL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161925), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF282E40), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Playback Speed: ${clip.speed}x",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Curve Editor →",
                        color = AccentPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onOpenSpeedCurves() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Speed Preset Pills
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(speedPresets) { spd ->
                        val isSelected = clip.speed == spd
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) AccentPurple else Color(0xFF22283A),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onUpdateSpeed(clip.id, spd) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${spd}x",
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom Speed Slider
                Slider(
                    value = clip.speed,
                    onValueChange = { onUpdateSpeed(clip.id, it) },
                    valueRange = 0.2f..5.0f,
                    colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
