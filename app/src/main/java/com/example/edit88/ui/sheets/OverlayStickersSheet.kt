package com.example.edit88.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.edit88.model.OverlayClip
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NotoNastaliqUrduFontFamily

@Composable
fun OverlayStickersSheet(
    currentPlayheadMs: Long,
    onAddOverlay: (OverlayClip) -> Unit,
    onClose: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Urdu Badges") }
    val categories = listOf("Urdu Badges", "Social Icons", "Glow Emojis")

    var selectedEmoji by remember { mutableStateOf("✨") }
    var selectedLabel by remember { mutableStateOf("Glow Spark") }
    var scale by remember { mutableFloatStateOf(1.2f) }
    var opacity by remember { mutableFloatStateOf(0.95f) }
    var durationSec by remember { mutableFloatStateOf(4f) }

    val urduBadges = listOf(
        Pair("ماشاءاللہ", "سبحان اللہ"),
        Pair("پاکستان", "زندہ باد"),
        Pair("شاعری", "اردو شاعری"),
        Pair("محبت", "دل سے"),
        Pair("کامیابی", "جدوجہد"),
        Pair("حوصلہ", "امید")
    )

    val socialBadges = listOf(
        Pair("🔔", "Subscribe"),
        Pair("❤️", "Double Tap"),
        Pair("🚀", "Viral Reel"),
        Pair("🔊", "Sound On"),
        Pair("⭐", "Top Rated"),
        Pair("🔥", "Trending")
    )

    val glowEmojis = listOf(
        Pair("✨", "Magic Spark"),
        Pair("⚡", "Electric Bolt"),
        Pair("🎯", "Target Hit"),
        Pair("💡", "Idea Spark"),
        Pair("🎬", "Movie Scene"),
        Pair("💎", "Diamond Gem")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp, 4.dp)
                .background(Color(0xFF334155), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = null,
                tint = AccentPink,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Stickers & PiP Overlays",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = cat == selectedCategory,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentPink,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurfaceVariant,
                        labelColor = Color(0xFF94A3B8)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of items
        val currentItems = when (selectedCategory) {
            "Urdu Badges" -> urduBadges
            "Social Icons" -> socialBadges
            else -> glowEmojis
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(140.dp)
        ) {
            items(currentItems) { (iconOrText, label) ->
                val isSel = label == selectedLabel
                Box(
                    modifier = Modifier
                        .background(
                            if (isSel) AccentPink.copy(alpha = 0.25f) else DarkSurfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSel) AccentPink else Color(0xFF2C3247),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            selectedEmoji = iconOrText
                            selectedLabel = label
                        }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = iconOrText,
                            fontSize = if (selectedCategory == "Urdu Badges") 14.sp else 22.sp,
                            fontFamily = if (selectedCategory == "Urdu Badges") NotoNastaliqUrduFontFamily else null,
                            color = Color.White
                        )
                        Text(
                            text = label,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sliders: Scale & Duration
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Scale: ${"%.1f".format(scale)}x", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Slider(
                    value = scale,
                    onValueChange = { scale = it },
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = AccentPink, activeTrackColor = AccentPink)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Duration: ${durationSec.toInt()}s", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Slider(
                    value = durationSec,
                    onValueChange = { durationSec = it },
                    valueRange = 1f..10f,
                    colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                val overlay = OverlayClip(
                    label = selectedLabel,
                    iconEmoji = selectedEmoji,
                    startTimeMs = currentPlayheadMs,
                    durationMs = (durationSec * 1000).toLong(),
                    scale = scale,
                    opacity = opacity
                )
                onAddOverlay(overlay)
                onClose()
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Insert Sticker at Playhead", fontWeight = FontWeight.Bold)
        }
    }
}
