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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.Project
import com.example.edit88.model.SubtitleClip
import com.example.edit88.model.SubtitleStylePreset
import com.example.edit88.model.TimedWord
import com.example.edit88.model.UrduFontChoice
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GulzarFontFamily
import com.example.ui.theme.LateefFontFamily
import com.example.ui.theme.NotoNastaliqUrduFontFamily

@Composable
fun UrduSubtitleSheet(
    project: Project,
    currentPlayheadMs: Long,
    selectedSubtitleId: String? = null,
    isGeneratingSubtitles: Boolean = false,
    subtitleGenerationStatus: String? = null,
    onAddSubtitle: (SubtitleClip) -> Unit,
    onUpdateSubtitle: (SubtitleClip) -> Unit,
    onDeleteSubtitle: (String) -> Unit,
    onGenerateSubtitles: () -> Unit,
    onClose: () -> Unit
) {
    val existingClip = project.subtitles.firstOrNull { it.id == selectedSubtitleId }

    var selectedTab by remember { mutableIntStateOf(if (existingClip != null) 0 else 0) }

    var subtitleText by remember(existingClip) {
        mutableStateOf(existingClip?.text ?: "نیا عنوان شامل کریں")
    }
    var selectedFont by remember(existingClip) {
        mutableStateOf(existingClip?.fontChoice ?: UrduFontChoice.NOTO_NASTALIQ)
    }
    var selectedPreset by remember(existingClip) {
        mutableStateOf(existingClip?.stylePreset ?: SubtitleStylePreset.KARAOKE)
    }
    var selectedColorHex by remember(existingClip) {
        mutableLongStateOf(existingClip?.textColorHex ?: 0xFFFFFFFF)
    }
    var fontSizeSp by remember(existingClip) {
        mutableFloatStateOf(existingClip?.fontSizeSp ?: 24f)
    }
    var isRtl by remember(existingClip) {
        mutableStateOf(existingClip?.isRtl ?: true)
    }
    var durationSec by remember(existingClip) {
        mutableFloatStateOf(
            if (existingClip != null) ((existingClip.endMs - existingClip.startMs) / 1000f).coerceIn(1f, 15f)
            else 3.5f
        )
    }
    var generatedNotice by remember { mutableStateOf(false) }

    val fontColorPalette = listOf(
        Pair("White", 0xFFFFFFFF),
        Pair("Yellow", 0xFFFACC15),
        Pair("Cyan", 0xFF06B6D4),
        Pair("Hot Pink", 0xFFEC4899),
        Pair("Emerald", 0xFF10B981),
        Pair("Amber", 0xFFF59E0B),
        Pair("Purple", 0xFF8B5CF6),
        Pair("Red", 0xFFEF4444),
        Pair("Orange", 0xFFF97316),
        Pair("Dark", 0xFF1E293B)
    )

    val quickUrduPresets = listOf(
        "نیا عنوان شامل کریں",
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
                    imageVector = Icons.Default.TextFields,
                    contentDescription = null,
                    tint = AccentAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (existingClip != null) "Edit Text Overlay" else "Text & Subtitle Studio",
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

        // Tabs: [Custom Text, Manual Auto-Subtitles]
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurfaceVariant,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AccentAmber
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("✍ Text Overlay", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("✨ Generate Subtitles", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // ================= TAB 0: TEXT OVERLAY EDITING =================
            // Text Input
            OutlinedTextField(
                value = subtitleText,
                onValueChange = { subtitleText = it },
                label = { Text("Text / Caption (Urdu, English, etc.)", color = Color(0xFF94A3B8)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentAmber,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Preset Suggestions
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(quickUrduPresets) { line ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E2230), RoundedCornerShape(8.dp))
                            .border(0.5.dp, Color(0xFF333B50), RoundedCornerShape(8.dp))
                            .clickable { subtitleText = line }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = line, color = Color(0xFFE2E8F0), fontSize = 11.sp, fontFamily = NotoNastaliqUrduFontFamily)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFF0F111A), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF262C3E), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val previewFont = when (selectedFont) {
                    UrduFontChoice.NOTO_NASTALIQ -> NotoNastaliqUrduFontFamily
                    UrduFontChoice.GULZAR -> GulzarFontFamily
                    UrduFontChoice.AMIRI -> AmiriFontFamily
                    UrduFontChoice.LATEEF -> LateefFontFamily
                    UrduFontChoice.SYSTEM_SANS -> FontFamily.Default
                    UrduFontChoice.SERIF -> FontFamily.Serif
                    UrduFontChoice.IMPACT -> FontFamily.SansSerif
                    UrduFontChoice.CURSIVE -> FontFamily.Cursive
                }
                Text(
                    text = subtitleText,
                    fontFamily = previewFont,
                    color = Color(selectedColorHex),
                    fontSize = (fontSizeSp * 0.9f).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. FONT SELECTION
            Text("Select Font:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(UrduFontChoice.values()) { font ->
                    val isSelected = font == selectedFont
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) AccentAmber.copy(alpha = 0.25f) else Color(0xFF1E212D),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) AccentAmber else Color(0xFF2E3345),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFont = font }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = font.label,
                            color = if (isSelected) AccentAmber else Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. FONT COLOR SELECTION
            Text("Font Color:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(fontColorPalette) { (name, hex) ->
                    val isSelected = hex == selectedColorHex
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(hex))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) AccentAmber else Color.White.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .clickable { selectedColorHex = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = name,
                                tint = if (hex == 0xFFFFFFFFL || hex == 0xFFFACC15L) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. FONT SIZE SLIDER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Font Size", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${fontSizeSp.toInt()} sp", color = AccentAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = fontSizeSp,
                onValueChange = { fontSizeSp = it },
                valueRange = 14f..48f,
                colors = SliderDefaults.colors(thumbColor = AccentAmber, activeTrackColor = AccentAmber)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. DURATION SLIDER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Text Duration", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(String.format("%.1f sec", durationSec), color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = durationSec,
                onValueChange = { durationSec = it },
                valueRange = 1f..10f,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. STYLE PRESETS
            Text("Animation & Style Presets:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SubtitleStylePreset.values()) { preset ->
                    val isSelected = preset == selectedPreset
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) AccentPurple.copy(alpha = 0.25f) else Color(0xFF1E212D),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) AccentPurple else Color(0xFF2E3345),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedPreset = preset }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = preset.label,
                            color = if (isSelected) AccentPurple else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ACTION BUTTONS (Save / Add / Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (existingClip != null) {
                    // Delete Button
                    Button(
                        onClick = {
                            onDeleteSubtitle(existingClip.id)
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = Color.White, fontSize = 13.sp)
                    }

                    // Save Changes Button
                    Button(
                        onClick = {
                            val start = existingClip.startMs
                            val end = start + (durationSec * 1000).toLong()
                            val words = subtitleText.trim().split(" ").filter { it.isNotBlank() }
                            val timedWords = if (words.isNotEmpty()) {
                                val dur = (end - start) / words.size
                                words.mapIndexed { idx, w ->
                                    TimedWord(w, start + (idx * dur), start + ((idx + 1) * dur))
                                }
                            } else emptyList()

                            onUpdateSubtitle(
                                existingClip.copy(
                                    text = subtitleText,
                                    endMs = end,
                                    fontChoice = selectedFont,
                                    stylePreset = selectedPreset,
                                    fontSizeSp = fontSizeSp,
                                    textColorHex = selectedColorHex,
                                    isRtl = isRtl,
                                    words = timedWords
                                )
                            )
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Save Changes", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Add New Text Button
                    Button(
                        onClick = {
                            val start = currentPlayheadMs
                            val end = start + (durationSec * 1000).toLong()
                            val words = subtitleText.trim().split(" ").filter { it.isNotBlank() }
                            val timedWords = if (words.isNotEmpty()) {
                                val dur = (end - start) / words.size
                                words.mapIndexed { idx, w ->
                                    TimedWord(w, start + (idx * dur), start + ((idx + 1) * dur))
                                }
                            } else emptyList()

                            val newSub = SubtitleClip(
                                text = subtitleText,
                                startMs = start,
                                endMs = end,
                                fontChoice = selectedFont,
                                stylePreset = selectedPreset,
                                fontSizeSp = fontSizeSp,
                                textColorHex = selectedColorHex,
                                isRtl = isRtl,
                                words = timedWords,
                                posX = 0.5f,
                                posY = 0.8f
                            )
                            onAddSubtitle(newSub)
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Add Text to Video", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // ================= TAB 1: MANUAL GENERATE SUBTITLES =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131724), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF26304A), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AI Audio Subtitle Generation",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Transcribe real video audio & generate Urdu Nastaliq subtitles with Gemini AI",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Font Status Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F291E), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✓", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "4 Urdu Nastaliq Fonts Ready: Noto Nastaliq, Gulzar, Amiri, Lateef",
                            color = Color(0xFFD1FAE5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Project contains ${project.clips.size} clips & ${project.audioTracks.size} audio tracks (${project.totalDurationMs / 1000}s). Tap below to transcribe real audio and generate synchronized karaoke subtitles using Gemini 3.5 Flash.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isGeneratingSubtitles) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C4A6E), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF38BDF8),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = subtitleGenerationStatus ?: "🎙️ Transcribing audio with Gemini 3.5 Flash...",
                                color = Color(0xFFE0F2FE),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    // Big Trigger Button
                    Button(
                        onClick = {
                            onGenerateSubtitles()
                            generatedNotice = true
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✨ Generate Real Subtitles with Gemini",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (generatedNotice && !isGeneratingSubtitles) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF065F46), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = subtitleGenerationStatus ?: "✓ Subtitles generated successfully and placed on timeline! You can drag, resize, rotate, or trim them on canvas.",
                            color = Color(0xFFD1FAE5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
