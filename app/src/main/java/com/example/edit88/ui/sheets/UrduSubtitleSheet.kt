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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.edit88.model.SubtitleClip
import com.example.edit88.model.SubtitleStylePreset
import com.example.edit88.model.TimedWord
import com.example.edit88.model.UrduFontChoice
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GulzarFontFamily
import com.example.ui.theme.NotoNastaliqUrduFontFamily

@Composable
fun UrduSubtitleSheet(
    currentPlayheadMs: Long,
    onAddSubtitle: (SubtitleClip) -> Unit,
    onClose: () -> Unit
) {
    var subtitleText by remember { mutableStateOf("خوابوں کو حقیقت بنانے کے لیے") }
    var selectedFont by remember { mutableStateOf(UrduFontChoice.NOTO_NASTALIQ) }
    var selectedPreset by remember { mutableStateOf(SubtitleStylePreset.KARAOKE) }
    var isRtl by remember { mutableStateOf(true) }
    var durationSec by remember { mutableFloatStateOf(3f) }
    var fontSizeSp by remember { mutableFloatStateOf(24f) }

    val quickUrduPresets = listOf(
        "خوابوں کو حقیقت بنانے کے لیے",
        "وقت سب سے قیمتی سرمایہ ہے",
        "کامیابی کا راز مستقل مزاجی ہے",
        "ہر دن ایک نئی امید لاتا ہے",
        "خود پر بھروسہ رکھیں اور آگے بڑھیں"
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
                    imageVector = Icons.Default.TextFields,
                    contentDescription = null,
                    tint = AccentAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Urdu Subtitles & Typography",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("RTL", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = isRtl,
                    onCheckedChange = { isRtl = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentAmber
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle text field
        OutlinedTextField(
            value = subtitleText,
            onValueChange = { subtitleText = it },
            label = { Text("Subtitle Caption Text (Urdu or English)", color = Color(0xFF94A3B8)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentAmber,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Preset Chips
        Text("Quick Urdu Lines:", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickUrduPresets.forEach { line ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E2333), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF333A4D), RoundedCornerShape(8.dp))
                        .clickable { subtitleText = line }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = line, color = Color(0xFFE2E8F0), fontSize = 12.sp, fontFamily = NotoNastaliqUrduFontFamily)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Font Family Selector
        Text("Choose Typography Font:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UrduFontChoice.values().forEach { font ->
                val isSelected = font == selectedFont
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) AccentPurple.copy(alpha = 0.25f) else Color(0xFF1E212D),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.5.dp,
                            if (isSelected) AccentPurple else Color(0xFF2E3345),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedFont = font }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = font.label,
                        color = if (isSelected) Color(0xFFC084FC) else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subtitle Style Preset
        Text("Animation & Visual Preset:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubtitleStylePreset.values().forEach { preset ->
                val isSelected = preset == selectedPreset
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedPreset = preset },
                    label = { Text(preset.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentAmber,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkSurfaceVariant,
                        labelColor = Color(0xFF94A3B8)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Font Size & Duration sliders
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Font Size: ${fontSizeSp.toInt()}sp", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Slider(
                    value = fontSizeSp,
                    onValueChange = { fontSizeSp = it },
                    valueRange = 16f..36f,
                    colors = SliderDefaults.colors(thumbColor = AccentAmber, activeTrackColor = AccentAmber)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Duration: ${"%.1f".format(durationSec)}s", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Slider(
                    value = durationSec,
                    onValueChange = { durationSec = it },
                    valueRange = 1f..8f,
                    colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add to Timeline Button
        Button(
            onClick = {
                val startMs = currentPlayheadMs
                val endMs = currentPlayheadMs + (durationSec * 1000).toLong()
                val rawWords = subtitleText.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
                val step = if (rawWords.isNotEmpty()) (endMs - startMs) / rawWords.size else 0L

                val timedWords = rawWords.mapIndexed { idx, w ->
                    val wStart = startMs + (idx * step)
                    val wEnd = wStart + step
                    TimedWord(w, wStart, wEnd)
                }

                val clip = SubtitleClip(
                    text = subtitleText,
                    startMs = startMs,
                    endMs = endMs,
                    fontChoice = selectedFont,
                    stylePreset = selectedPreset,
                    fontSizeSp = fontSizeSp,
                    isRtl = isRtl,
                    words = timedWords
                )
                onAddSubtitle(clip)
                onClose()
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Insert Caption at Playhead", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
