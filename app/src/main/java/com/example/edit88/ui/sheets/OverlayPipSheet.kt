package com.example.edit88.ui.sheets

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.OverlayClip
import com.example.edit88.model.Project
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkSurface
import kotlin.math.roundToInt

@Composable
fun OverlayPipSheet(
    project: Project,
    currentPlayheadMs: Long,
    selectedOverlayId: String? = null,
    onAddOverlay: (OverlayClip) -> Unit,
    onUpdateOverlay: (OverlayClip) -> Unit,
    onDeleteOverlay: (String) -> Unit,
    onSelectOverlay: (String) -> Unit,
    onClose: () -> Unit
) {
    val activeOverlay = project.overlays.firstOrNull { it.id == selectedOverlayId }
        ?: project.overlays.firstOrNull()

    // Photo/Video picker for Picture-in-Picture
    val visualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val newOverlay = OverlayClip(
                label = "PiP Media",
                iconEmoji = "🎬",
                uriString = uri.toString(),
                isVideo = true,
                startTimeMs = currentPlayheadMs,
                durationMs = 5000L,
                posX = 0.72f,
                posY = 0.28f,
                scale = 1.0f,
                rotation = 0f,
                chromaKeyEnabled = false
            )
            onAddOverlay(newOverlay)
        }
    }

    val chromaPresets = listOf(
        Pair("Green", 0xFF00FF00),
        Pair("Blue", 0xFF0044FF),
        Pair("Black", 0xFF000000),
        Pair("Magenta", 0xFFFF00FF),
        Pair("White", 0xFFFFFFFF)
    )

    val sampleStickers = listOf(
        Pair("🔥 Fire Effect", "🔥"),
        Pair("✨ Sparkle PiP", "✨"),
        Pair("🎮 Gamer Cam", "🎮"),
        Pair("🔴 LIVE Tag", "🔴"),
        Pair("💬 Speech Pop", "💬"),
        Pair("🏆 Trophy", "🏆")
    )

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

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = AccentPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Overlay & Picture-in-Picture",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (activeOverlay != null) {
                IconButton(onClick = { onDeleteOverlay(activeOverlay.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF87171))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. ADD NEW OVERLAY BUTTONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    visualMediaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ Import PiP", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val defaultPip = OverlayClip(
                        label = "Reaction PiP",
                        iconEmoji = "🎭",
                        startTimeMs = currentPlayheadMs,
                        durationMs = 4500L,
                        posX = 0.72f,
                        posY = 0.28f,
                        scale = 1.0f,
                        rotation = 0f,
                        chromaKeyEnabled = false
                    )
                    onAddOverlay(defaultPip)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2433)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sticker PiP", color = Color.White, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Preset Overlay Stickers
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sampleStickers) { (label, emoji) ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1A1D28), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF2E3345), RoundedCornerShape(8.dp))
                        .clickable {
                            val clip = OverlayClip(
                                label = label,
                                iconEmoji = emoji,
                                startTimeMs = currentPlayheadMs,
                                durationMs = 4000L,
                                posX = 0.5f,
                                posY = 0.4f,
                                scale = 1.1f
                            )
                            onAddOverlay(clip)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = label, color = Color(0xFFE2E8F0), fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. OVERLAY SELECTION LIST (if multiple)
        if (project.overlays.isNotEmpty()) {
            Text(
                text = "Active Overlays (${project.overlays.size}):",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(project.overlays) { ov ->
                    val isSelected = ov.id == activeOverlay?.id
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) AccentPurple.copy(alpha = 0.35f) else Color(0xFF1E212D),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.5.dp,
                                if (isSelected) AccentPurple else Color(0xFF2E3345),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectOverlay(ov.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = ov.iconEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = ov.label,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (ov.chromaKeyEnabled) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(AccentEmerald, RoundedCornerShape(2.dp))
                                        .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                ) {
                                    Text("CHROMA", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. CAPCUT-STYLE CHROMA KEY SECTION
        if (activeOverlay != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161A26), RoundedCornerShape(12.dp))
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
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Chroma Key (Green Screen Removal)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = activeOverlay.chromaKeyEnabled,
                            onCheckedChange = { isEnabled ->
                                onUpdateOverlay(activeOverlay.copy(chromaKeyEnabled = isEnabled))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentEmerald
                            )
                        )
                    }

                    if (activeOverlay.chromaKeyEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Target Background Color:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(chromaPresets) { (name, hex) ->
                                val isSelected = hex == activeOverlay.chromaKeyColorHex
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(hex))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) AccentEmerald else Color.White.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onUpdateOverlay(activeOverlay.copy(chromaKeyColorHex = hex))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = name,
                                            tint = if (hex == 0xFF00FF00L || hex == 0xFFFFFFFFL) Color.Black else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Similarity Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Similarity (Cutout intensity)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("${(activeOverlay.chromaSimilarity * 100).toInt()}%", color = AccentEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = activeOverlay.chromaSimilarity,
                            onValueChange = {
                                onUpdateOverlay(activeOverlay.copy(chromaSimilarity = it))
                            },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = AccentEmerald, activeTrackColor = AccentEmerald)
                        )

                        // Smoothness Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Smoothness (Edge feathering)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("${(activeOverlay.chromaSmoothness * 100).toInt()}%", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = activeOverlay.chromaSmoothness,
                            onValueChange = {
                                onUpdateOverlay(activeOverlay.copy(chromaSmoothness = it))
                            },
                            valueRange = 0.0f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. RESIZE & ROTATE CONTROLS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161A26), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF282E40), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "Transform: Resize, Rotate & Position",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scale Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Scale / Size", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(String.format("%.2fx", activeOverlay.scale), color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = activeOverlay.scale,
                        onValueChange = {
                            onUpdateOverlay(activeOverlay.copy(scale = it))
                        },
                        valueRange = 0.3f..3.0f,
                        colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rotation Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rotation Angle", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${activeOverlay.rotation.roundToInt()}°", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reset",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                modifier = Modifier.clickable {
                                    onUpdateOverlay(activeOverlay.copy(rotation = 0f))
                                }
                            )
                        }
                    }
                    Slider(
                        value = activeOverlay.rotation,
                        onValueChange = {
                            onUpdateOverlay(activeOverlay.copy(rotation = it))
                        },
                        valueRange = -180f..180f,
                        colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Layout Presets for Picture-in-Picture
                    Text("Quick PiP Placement:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF22283A), RoundedCornerShape(6.dp))
                                .clickable {
                                    onUpdateOverlay(activeOverlay.copy(posX = 0.75f, posY = 0.25f, scale = 0.85f))
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Top Right", color = Color.White, fontSize = 10.sp)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF22283A), RoundedCornerShape(6.dp))
                                .clickable {
                                    onUpdateOverlay(activeOverlay.copy(posX = 0.75f, posY = 0.75f, scale = 0.85f))
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Bottom Right", color = Color.White, fontSize = 10.sp)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF22283A), RoundedCornerShape(6.dp))
                                .clickable {
                                    onUpdateOverlay(activeOverlay.copy(posX = 0.5f, posY = 0.5f, scale = 1.0f, rotation = 0f))
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Center", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. TIMELINE DURATION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Overlay Duration on Timeline", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text(String.format("%.1f sec", activeOverlay.durationMs / 1000f), color = AccentPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = (activeOverlay.durationMs / 1000f).coerceIn(1f, 20f),
                onValueChange = {
                    onUpdateOverlay(activeOverlay.copy(durationMs = (it * 1000).toLong()))
                },
                valueRange = 1f..20f,
                colors = SliderDefaults.colors(thumbColor = AccentPink, activeTrackColor = AccentPink)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Done", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
