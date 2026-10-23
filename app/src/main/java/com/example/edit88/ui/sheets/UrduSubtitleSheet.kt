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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.Project
import com.example.edit88.model.SubtitleAnimationPreset
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
    onOpenFontStore: () -> Unit = {},
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
    var selectedAnimation by remember(existingClip) {
        mutableStateOf(existingClip?.animationPreset ?: SubtitleAnimationPreset.POP)
    }
    var selectedColorHex by remember(existingClip) {
        mutableLongStateOf(existingClip?.textColorHex ?: 0xFFFFFFFF)
    }
    var activeColorHex by remember(existingClip) {
        mutableLongStateOf(existingClip?.activeColorHex ?: 0xFFFACC15)
    }
    var bgColorHex by remember(existingClip) {
        mutableLongStateOf(existingClip?.bgColorHex ?: 0xAA000000)
    }
    var bgOpacity by remember(existingClip) {
        mutableFloatStateOf(existingClip?.bgOpacity ?: 0.75f)
    }
    var outlineWidthDp by remember(existingClip) {
        mutableFloatStateOf(existingClip?.outlineWidthDp ?: 2.0f)
    }
    var outlineColorHex by remember(existingClip) {
        mutableLongStateOf(existingClip?.outlineColorHex ?: 0xFF000000)
    }
    var shadowBlurRadius by remember(existingClip) {
        mutableFloatStateOf(existingClip?.shadowBlurRadius ?: 8.0f)
    }
    var glowRadius by remember(existingClip) {
        mutableFloatStateOf(existingClip?.glowRadius ?: 0f)
    }
    var glowColorHex by remember(existingClip) {
        mutableLongStateOf(existingClip?.glowColorHex ?: 0xFF06B6D4)
    }
    var textAlign by remember(existingClip) {
        mutableStateOf(existingClip?.textAlign ?: "CENTER")
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
            else 3.0f
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
        Pair("Dark", 0xFF1E293B)
    )

    val quickUrduPresets = listOf(
        "خوابوں کو حقیقت بنائیں",
        "وقت سب سے قیمتی ہے",
        "کامیابی کا اصل راز",
        "آج ہی شروعات کریں",
        "محنت کبھی رائیگاں نہیں جاتی"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Drag Handle
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
                    text = if (existingClip != null) "Edit Subtitle Inspector" else "Subtitle & Captions Studio",
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

        // Tabs: [Inspector/Style, AI Auto-Subtitles]
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
                text = { Text("✍ Subtitle Inspector", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("✨ AI Auto-Subtitles", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // SUBTITLE INSPECTOR / STYLING TAB
            Text("Subtitle Text (3-4 words recommended):", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = subtitleText,
                onValueChange = { subtitleText = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentAmber,
                    unfocusedBorderColor = Color(0xFF333B50)
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Viral Urdu Presets
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

            // Live Inspector Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .background(Color(0xFF0B0E17), RoundedCornerShape(10.dp))
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
                    UrduFontChoice.CUSTOM_IMPORTED -> NotoNastaliqUrduFontFamily
                }
                Box(
                    modifier = Modifier
                        .background(Color(bgColorHex).copy(alpha = bgOpacity), RoundedCornerShape(8.dp))
                        .border(outlineWidthDp.dp, Color(outlineColorHex), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = subtitleText,
                        fontFamily = previewFont,
                        color = Color(selectedColorHex),
                        fontSize = (fontSizeSp * 0.85f).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = when (textAlign) {
                            "LEFT" -> TextAlign.Start
                            "RIGHT" -> TextAlign.End
                            else -> TextAlign.Center
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. FONT SELECTION & FONT STORE SHORTCUT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Font:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .background(Color(0xFF312E81), RoundedCornerShape(6.dp))
                        .clickable { onOpenFontStore() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FontDownload, contentDescription = null, tint = Color(0xFFA5B4FC), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Font Store & Upload ➔", color = Color(0xFFE0E7FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
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
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = font.label,
                            color = if (isSelected) AccentAmber else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. TEXT ALIGNMENT & ANIMATION PRESET
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alignment
                Column(modifier = Modifier.weight(1f)) {
                    Text("Alignment:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF1E212D), RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        listOf(
                            Triple("LEFT", Icons.Default.FormatAlignLeft, "Left"),
                            Triple("CENTER", Icons.Default.FormatAlignCenter, "Center"),
                            Triple("RIGHT", Icons.Default.FormatAlignRight, "Right")
                        ).forEach { (alignVal, icon, label) ->
                            val isSel = textAlign == alignVal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) AccentAmber else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { textAlign = alignVal }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = label, tint = if (isSel) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Font Size
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Size:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${fontSizeSp.toInt()} sp", color = AccentAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = fontSizeSp,
                        onValueChange = { fontSizeSp = it },
                        valueRange = 14f..42f,
                        colors = SliderDefaults.colors(thumbColor = AccentAmber, activeTrackColor = AccentAmber)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. ANIMATION PRESETS
            Text("Animation Preset:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SubtitleAnimationPreset.values()) { anim ->
                    val isSel = anim == selectedAnimation
                    Box(
                        modifier = Modifier
                            .background(if (isSel) AccentCyan.copy(alpha = 0.25f) else Color(0xFF1E212D), RoundedCornerShape(8.dp))
                            .border(1.dp, if (isSel) AccentCyan else Color(0xFF2E3345), RoundedCornerShape(8.dp))
                            .clickable { selectedAnimation = anim }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(anim.iconEmoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = anim.label,
                                color = if (isSel) AccentCyan else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. COLOR SELECTION (Text & Karaoke Active Color)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Text Color:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(fontColorPalette) { (_, hex) ->
                            val isSelected = hex == selectedColorHex
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .border(if (isSelected) 2.5.dp else 1.dp, if (isSelected) AccentAmber else Color.White.copy(0.3f), CircleShape)
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Karaoke Active Color:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(fontColorPalette) { (_, hex) ->
                            val isSelected = hex == activeColorHex
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .border(if (isSelected) 2.5.dp else 1.dp, if (isSelected) AccentCyan else Color.White.copy(0.3f), CircleShape)
                                    .clickable { activeColorHex = hex }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. BACKGROUND OPACITY & OUTLINE CONTROLS
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Bg Opacity:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${(bgOpacity * 100).toInt()}%", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = bgOpacity,
                        onValueChange = { bgOpacity = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Stroke/Outline:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${outlineWidthDp.toInt()}dp", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = outlineWidthDp,
                        onValueChange = { outlineWidthDp = it },
                        valueRange = 0f..6f,
                        colors = SliderDefaults.colors(thumbColor = AccentPink, activeTrackColor = AccentPink)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. SHADOW & GLOW SLIDERS
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shadow Blur:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${shadowBlurRadius.toInt()}", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = shadowBlurRadius,
                        onValueChange = { shadowBlurRadius = it },
                        valueRange = 0f..20f,
                        colors = SliderDefaults.colors(thumbColor = AccentAmber, activeTrackColor = AccentAmber)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Neon Glow:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${glowRadius.toInt()}", color = Color.White, fontSize = 11.sp)
                    }
                    Slider(
                        value = glowRadius,
                        onValueChange = { glowRadius = it },
                        valueRange = 0f..25f,
                        colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Apply / Save / Delete Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (existingClip != null) {
                    Button(
                        onClick = {
                            onDeleteSubtitle(existingClip.id)
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.weight(0.35f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                }

                Button(
                    onClick = {
                        val words = subtitleText.split(" ").filter { it.isNotBlank() }
                        val durMs = (durationSec * 1000L).toLong()
                        val step = if (words.isNotEmpty()) durMs / words.size else 0L
                        val timedWords = words.mapIndexed { idx, w ->
                            val s = currentPlayheadMs + (idx * step)
                            TimedWord(w, s, s + step)
                        }

                        val updated = (existingClip ?: SubtitleClip(
                            text = subtitleText,
                            startMs = currentPlayheadMs,
                            endMs = currentPlayheadMs + durMs,
                            words = timedWords,
                            posY = 0.82f
                        )).copy(
                            text = subtitleText,
                            fontChoice = selectedFont,
                            stylePreset = selectedPreset,
                            animationPreset = selectedAnimation,
                            fontSizeSp = fontSizeSp,
                            textColorHex = selectedColorHex,
                            activeColorHex = activeColorHex,
                            bgColorHex = bgColorHex,
                            bgOpacity = bgOpacity,
                            outlineWidthDp = outlineWidthDp,
                            outlineColorHex = outlineColorHex,
                            shadowBlurRadius = shadowBlurRadius,
                            glowRadius = glowRadius,
                            glowColorHex = glowColorHex,
                            textAlign = textAlign,
                            isRtl = isRtl,
                            words = if (existingClip != null && existingClip.words.isNotEmpty()) existingClip.words else timedWords
                        )

                        if (existingClip != null) {
                            onUpdateSubtitle(updated)
                        } else {
                            onAddSubtitle(updated)
                        }
                        onClose()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (existingClip != null) "Save Subtitle Changes" else "Add Subtitle to Canvas",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // TAB 1: AI AUTO-SUBTITLES GENERATION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131722), RoundedCornerShape(12.dp))
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
                            text = "AI Audio Subtitle Engine",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Auto-breaks long speech into viral 3-4 word synchronized chunks in lower-third",
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
                            text = subtitleGenerationStatus ?: "✓ Subtitles generated successfully into 3-4 word chunks and placed on timeline! Tap any chunk to edit in Inspector.",
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
