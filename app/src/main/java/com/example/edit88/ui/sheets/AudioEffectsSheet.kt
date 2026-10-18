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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.edit88.model.AudioTrack
import com.example.edit88.model.AudioType
import com.example.edit88.model.VoicePreset
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TrackAudioColor

@Composable
fun AudioEffectsSheet(
    currentPlayheadMs: Long,
    onAddAudioTrack: (AudioTrack) -> Unit,
    onClose: () -> Unit
) {
    var volume by remember { mutableFloatStateOf(0.85f) }
    var noiseReduction by remember { mutableStateOf(true) }
    var selectedVoicePreset by remember { mutableStateOf(VoicePreset.NORMAL) }

    val bgmLibrary = listOf(
        Pair("Cinematic Beat", "Epic cinematic percussion and ambient synth"),
        Pair("Cyberpunk Synth", "Fast paced 80s synthwave neon beat"),
        Pair("Lofi Chillhop", "Relaxing warm lofi study beats"),
        Pair("Dramatic Trailer", "Rising orchestral strings and deep sub bass")
    )

    val sfxLibrary = listOf(
        Pair("Cinematic Woosh", "0.8s"),
        Pair("Camera Shutter", "0.5s"),
        Pair("Neon Pop", "0.3s"),
        Pair("Applause Crowd", "2.5s"),
        Pair("Retro Laser", "0.6s")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(20.dp)
    ) {
        // Handle bar
        Box(
            modifier = Modifier
                .size(40.dp, 4.dp)
                .background(Color(0xFF334155), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Audiotrack,
                    contentDescription = null,
                    tint = TrackAudioColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Audio & SFX Studio",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("AI De-Noise", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = noiseReduction,
                    onCheckedChange = { noiseReduction = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TrackAudioColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Volume control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Track Volume", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("${(volume * 100).toInt()}%", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = volume,
            onValueChange = { volume = it },
            valueRange = 0f..1.5f,
            colors = SliderDefaults.colors(thumbColor = TrackAudioColor, activeTrackColor = TrackAudioColor)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Voice Changer Presets
        Text("AI Voice Changer Presets:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(VoicePreset.values()) { preset ->
                val isSelected = preset == selectedVoicePreset
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) TrackAudioColor.copy(alpha = 0.25f) else Color(0xFF1E212D),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) TrackAudioColor else Color(0xFF2E3345),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedVoicePreset = preset }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = preset.label,
                        color = if (isSelected) Color(0xFF34D399) else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Background Music Presets
        Text("Royalty-Free Soundtrack:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            bgmLibrary.take(2).forEach { (name, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF181B26), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF262C3E), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(desc, color = Color(0xFF64748B), fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            val track = AudioTrack(
                                name = name,
                                type = AudioType.BGM,
                                startTimeMs = currentPlayheadMs,
                                durationMs = 12000L,
                                volume = volume,
                                noiseReduction = noiseReduction,
                                voicePreset = selectedVoicePreset
                            )
                            onAddAudioTrack(track)
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TrackAudioColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sound Effects
        Text("Sound Effects (SFX):", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sfxLibrary) { (name, dur) ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E212D), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF333A4D), RoundedCornerShape(8.dp))
                        .clickable {
                            val track = AudioTrack(
                                name = name,
                                type = AudioType.SFX,
                                startTimeMs = currentPlayheadMs,
                                durationMs = 1000L,
                                volume = volume,
                                noiseReduction = false
                            )
                            onAddAudioTrack(track)
                            onClose()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$name ($dur)", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
