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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.ai.ColorGradingResult
import com.example.edit88.ai.GeminiService
import com.example.edit88.ai.HighlightMoment
import com.example.edit88.ai.ScriptToVideoResponse
import com.example.edit88.data.SettingsRepository
import com.example.edit88.model.ColorGrading
import com.example.edit88.model.FilterType
import com.example.edit88.model.MediaClip
import com.example.edit88.model.MediaType
import com.example.edit88.model.Project
import com.example.edit88.model.SubtitleClip
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import kotlinx.coroutines.launch

@Composable
fun AiStudioSheet(
    project: Project,
    settingsRepository: SettingsRepository,
    geminiService: GeminiService,
    onApplySubtitles: (List<SubtitleClip>) -> Unit,
    onApplyScenes: (List<MediaClip>, List<SubtitleClip>) -> Unit,
    onApplyColorGrading: (FilterType, ColorGrading) -> Unit,
    onSeekTo: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit
) {
    val apiKey = settingsRepository.getApiKey()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Auto-Captions", "Script to Video", "AI Color Grade", "Viral Highlights")

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Drag handle
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
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit88 AI Studio",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (apiKey.isBlank()) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF7F1D1D), RoundedCornerShape(8.dp))
                        .clickable { onOpenSettings() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("API Key Missing", color = Color(0xFFFCA5A5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF065F46), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Gemini Active", color = Color(0xFF6EE7B7), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF13151F),
            contentColor = AccentCyan,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        statusMessage = ""
                    },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) AccentCyan else Color(0xFF94A3B8)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        when (selectedTab) {
            0 -> AutoCaptionsTab(
                isLoading = isLoading,
                onGenerate = { lang, hint ->
                    isLoading = true
                    statusMessage = "Generating synchronized $lang auto-subtitles with Gemini AI..."
                    scope.launch {
                        val subs = geminiService.generateAutoSubtitles(hint, lang, apiKey)
                        isLoading = false
                        onApplySubtitles(subs)
                        statusMessage = "✓ Generated ${subs.size} auto-captions synchronized to timeline!"
                    }
                }
            )

            1 -> ScriptToVideoTab(
                isLoading = isLoading,
                onGenerate = { prompt ->
                    isLoading = true
                    statusMessage = "Creating multi-scene script & video clips with Gemini AI..."
                    scope.launch {
                        val response = geminiService.generateScriptToVideo(prompt, apiKey)
                        val newClips = response.scenes.mapIndexed { idx, sc ->
                            val colorHex1 = when (idx % 3) {
                                0 -> 0xFF3B82F6
                                1 -> 0xFF8B5CF6
                                else -> 0xFFEC4899
                            }
                            val colorHex2 = when (idx % 3) {
                                0 -> 0xFF06B6D4
                                1 -> 0xFFA855F7
                                else -> 0xFFF59E0B
                            }
                            MediaClip(
                                title = sc.title,
                                type = MediaType.VIDEO,
                                durationMs = sc.durationSec * 1000L,
                                endTrimMs = sc.durationSec * 1000L,
                                visualTheme = sc.recommendedTheme,
                                primaryColorHex = colorHex1,
                                secondaryColorHex = colorHex2
                            )
                        }

                        // Generate corresponding Urdu subtitles
                        var startOffset = 0L
                        val newSubs = response.scenes.map { sc ->
                            val dur = sc.durationSec * 1000L
                            val s = SubtitleClip(
                                text = sc.urduVoiceover,
                                startMs = startOffset,
                                endMs = startOffset + dur - 500L,
                                isRtl = true
                            )
                            startOffset += dur
                            s
                        }

                        isLoading = false
                        onApplyScenes(newClips, newSubs)
                        statusMessage = "✓ Generated ${response.scenes.size} scenes for \"${response.title}\"!"
                    }
                }
            )

            2 -> AiColorGradingTab(
                isLoading = isLoading,
                onApply = { mood ->
                    isLoading = true
                    statusMessage = "Analyzing mood '$mood' and computing color grading matrix..."
                    scope.launch {
                        val result = geminiService.analyzeColorGrading(mood, apiKey)
                        isLoading = false
                        onApplyColorGrading(result.filterType, result.colorGrading)
                        statusMessage = "✓ Applied ${result.filterType.displayName}: ${result.explanation}"
                    }
                }
            )

            3 -> ViralHighlightsTab(
                isLoading = isLoading,
                projectSummary = project.title,
                geminiService = geminiService,
                apiKey = apiKey,
                onSeekTo = {
                    onSeekTo(it)
                    onClose()
                }
            )
        }

        if (statusMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F2537), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(text = statusMessage, color = Color(0xFFBAE6FD), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun AutoCaptionsTab(
    isLoading: Boolean,
    onGenerate: (String, String) -> Unit
) {
    var selectedLang by remember { mutableStateOf("Urdu (Nastaliq)") }
    var contextPrompt by remember { mutableStateOf("Motivational life advice about patience and victory") }

    val languages = listOf("Urdu (Nastaliq)", "English", "Roman Urdu", "Bilingual")

    Column {
        Text("Target Language:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            languages.forEach { lang ->
                FilterChip(
                    selected = lang == selectedLang,
                    onClick = { selectedLang = lang },
                    label = { Text(lang, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentCyan,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkSurfaceVariant,
                        labelColor = Color(0xFF94A3B8)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contextPrompt,
            onValueChange = { contextPrompt = it },
            label = { Text("Audio or Dialogue Context", color = Color(0xFF94A3B8)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onGenerate(selectedLang, contextPrompt) },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating with Gemini...", color = Color.Black)
            } else {
                Icon(Icons.Default.Hearing, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Auto Subtitles", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ScriptToVideoTab(
    isLoading: Boolean,
    onGenerate: (String) -> Unit
) {
    var topicPrompt by remember { mutableStateOf("How to become 1% better every day with Urdu voiceover") }

    Column {
        Text("AI Video Script Director:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = topicPrompt,
            onValueChange = { topicPrompt = it },
            label = { Text("Video Topic or Concept", color = Color(0xFF94A3B8)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onGenerate(topicPrompt) },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Creating Scenes with AI...")
            } else {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Full Video Project", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AiColorGradingTab(
    isLoading: Boolean,
    onApply: (String) -> Unit
) {
    var moodPrompt by remember { mutableStateOf("Moody cinematic teal and warm sunset glow") }
    val moodPresets = listOf(
        "Cyberpunk Neon Violet",
        "Vintage 90s Golden Film",
        "High Contrast Monochrome Noir",
        "Warm Golden Hour Sunset",
        "Blockbuster Action Teal & Orange"
    )

    Column {
        Text("Describe Visual Mood / Aesthetic:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = moodPrompt,
            onValueChange = { moodPrompt = it },
            label = { Text("Color Mood Prompt", color = Color(0xFF94A3B8)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPink,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            moodPresets.forEach { preset ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E212E), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF32384D), RoundedCornerShape(8.dp))
                        .clickable { moodPrompt = preset }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = preset, color = Color(0xFFE2E8F0), fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onApply(moodPrompt) },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing Lighting & Color...")
            } else {
                Icon(Icons.Default.ColorLens, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply AI Color Grading", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ViralHighlightsTab(
    isLoading: Boolean,
    projectSummary: String,
    geminiService: GeminiService,
    apiKey: String,
    onSeekTo: (Long) -> Unit
) {
    var highlights by remember {
        mutableStateOf(
            listOf(
                HighlightMoment(title = "Viral Intro Hook", startMs = 0L, endMs = 3800L, viralScore = 98, reason = "Immediate emotional hook with strong visual motion"),
                HighlightMoment(title = "Key Climax Reveal", startMs = 4500L, endMs = 9500L, viralScore = 94, reason = "Peak dialogue retention and beat drop"),
                HighlightMoment(title = "Call-To-Action Finale", startMs = 10000L, endMs = 15000L, viralScore = 89, reason = "Urdu Nastaliq outro punchline driving shares")
            )
        )
    }

    Column {
        Text("AI Highlight Detector (Viral Moments):", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        highlights.forEach { h ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color(0xFF191C28), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF2C3247), RoundedCornerShape(10.dp))
                    .clickable { onSeekTo(h.startMs) }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = h.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = h.reason, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "${h.viralScore}% Viral", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${h.startMs / 1000}s - ${h.endMs / 1000}s", color = Color(0xFF64748B), fontSize = 10.sp)
                }
            }
        }
    }
}
