package com.example.edit88.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.VideoLibrary
import com.example.edit88.util.MediaImportHelper
import com.example.edit88.model.MediaClip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.data.TemplatesRepository
import com.example.edit88.model.AspectRatioType
import com.example.edit88.model.Project
import com.example.edit88.model.VideoTemplate
import com.example.edit88.ui.sheets.SettingsSheet
import com.example.edit88.viewmodel.VideoEditorViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NotoNastaliqUrduFontFamily
import kotlinx.coroutines.launch

enum class HomeTab(val title: String, val icon: ImageVector) {
    PROJECTS("Projects", Icons.Default.Movie),
    TEMPLATES("Templates", Icons.Default.ViewCarousel),
    AI_TOOLS("AI Tools", Icons.Default.AutoAwesome),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VideoEditorViewModel,
    onOpenEditor: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(HomeTab.PROJECTS) }
    var showNewProjectDialog by remember { mutableStateOf(false) }

    // Launcher for selecting one or multiple videos from device local storage
    val homeMediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val clips = uris.map { MediaImportHelper.createMediaClipFromUri(context, it) }
            viewModel.createProjectFromImportedClips(clips)
            onOpenEditor()
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                activeTab = selectedTab,
                hasApiKey = viewModel.settingsRepository.getApiKey().isNotBlank(),
                onOpenSettings = { selectedTab = HomeTab.SETTINGS }
            )
        },
        bottomBar = {
            HomeBottomNavigation(
                currentTab = selectedTab,
                onSelectTab = { selectedTab = it }
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                HomeTab.PROJECTS -> ProjectsTabContent(
                    projects = uiState.recentProjects,
                    onNewProject = { showNewProjectDialog = true },
                    onImportVideos = {
                        homeMediaPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    onSelectProject = { proj ->
                        viewModel.loadProject(proj)
                        onOpenEditor()
                    },
                    onDuplicate = { viewModel.duplicateProject(it) },
                    onDelete = { viewModel.deleteProject(it.id) }
                )

                HomeTab.TEMPLATES -> TemplatesTabContent(
                    templates = TemplatesRepository.templates,
                    onUseTemplate = { template ->
                        viewModel.loadTemplate(template)
                        onOpenEditor()
                    }
                )

                HomeTab.AI_TOOLS -> AiToolsTabContent(
                    viewModel = viewModel,
                    onOpenEditor = onOpenEditor,
                    onGoToSettings = { selectedTab = HomeTab.SETTINGS }
                )

                HomeTab.SETTINGS -> SettingsTabContent(
                    viewModel = viewModel
                )
            }
        }
    }

    if (showNewProjectDialog) {
        NewProjectDialog(
            onSelectRatio = { ratio ->
                viewModel.createNewProject(ratio)
                showNewProjectDialog = false
                onOpenEditor()
            },
            onImportVideos = {
                showNewProjectDialog = false
                homeMediaPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            },
            onDismiss = { showNewProjectDialog = false }
        )
    }
}

@Composable
private fun HomeTopBar(
    activeTab: HomeTab,
    hasApiKey: Boolean,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(AccentPurple, AccentCyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "88",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Edit88",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = when (activeTab) {
                        HomeTab.PROJECTS -> "Video Projects & Drafts"
                        HomeTab.TEMPLATES -> "Viral CapCut Templates"
                        HomeTab.AI_TOOLS -> "Gemini AI Creative Suite"
                        HomeTab.SETTINGS -> "API Vault & Configuration"
                    },
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }
        }

        // Quick Key Status
        Row(
            modifier = Modifier
                .background(Color(0xFF1E212E), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF2E3345), RoundedCornerShape(20.dp))
                .clickable(onClick = onOpenSettings)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                tint = if (hasApiKey) AccentEmerald else AccentPink,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (hasApiKey) "Active" else "Add Key",
                color = if (hasApiKey) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HomeBottomNavigation(
    currentTab: HomeTab,
    onSelectTab: (HomeTab) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF11131B),
        tonalElevation = 8.dp
    ) {
        HomeTab.values().forEach { tab ->
            val isSelected = tab == currentTab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentCyan,
                    selectedTextColor = AccentCyan,
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B),
                    indicatorColor = AccentCyan.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
private fun ProjectsTabContent(
    projects: List<Project>,
    onNewProject: () -> Unit,
    onImportVideos: () -> Unit,
    onSelectProject: (Project) -> Unit,
    onDuplicate: (Project) -> Unit,
    onDelete: (Project) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRatioFilter by remember { mutableStateOf<AspectRatioType?>(null) }

    val filteredProjects = projects.filter { proj ->
        (selectedRatioFilter == null || proj.aspectRatio == selectedRatioFilter) &&
                (searchQuery.isBlank() || proj.title.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            HeroNewProjectCard(onNewProject = onNewProject, onImportVideos = onImportVideos)
        }

        // Search Bar & Filter Chips
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search draft projects...", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = Color(0xFF2C3247),
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedRatioFilter == null,
                    onClick = { selectedRatioFilter = null },
                    label = { Text("All Ratios (${projects.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentPurple,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurfaceVariant,
                        labelColor = Color(0xFF94A3B8)
                    )
                )

                AspectRatioType.values().forEach { ratio ->
                    val count = projects.count { it.aspectRatio == ratio }
                    FilterChip(
                        selected = selectedRatioFilter == ratio,
                        onClick = { selectedRatioFilter = ratio },
                        label = { Text("${ratio.label} ($count)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkSurfaceVariant,
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }
        }

        // Projects List
        item {
            Text(
                text = "Draft Timelines",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (filteredProjects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No projects found. Tap 'New Project' above to create one!", color = Color(0xFF64748B), fontSize = 13.sp)
                }
            }
        } else {
            items(filteredProjects) { project ->
                ProjectItemCardWithActions(
                    project = project,
                    onClick = { onSelectProject(project) },
                    onDuplicate = { onDuplicate(project) },
                    onDelete = { onDelete(project) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ProjectItemCardWithActions(
    project: Project,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, Color(0xFF222636), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E293B), Color(0xFF334155))
                            )
                        )
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = project.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.aspectRatio.label,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(" • ", color = Color(0xFF64748B), fontSize = 11.sp)
                        Text(
                            text = "${project.clips.size} clips",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Text(" • ", color = Color(0xFF64748B), fontSize = 11.sp)
                        Text(
                            text = "${project.totalDurationMs / 1000}s",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TemplatesTabContent(
    templates: List<VideoTemplate>,
    onUseTemplate: (VideoTemplate) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Urdu Poetry", "Viral Hooks", "Cinematic")

    val filtered = if (selectedCategory == "All") templates else templates.filter { it.category == selectedCategory }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("CapCut Style Viral Templates", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Pre-timed video tracks, sound beats, and synchronized Urdu Nastaliq captions.", color = Color(0xFF94A3B8), fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))

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
                            selectedContainerColor = AccentPurple,
                            selectedLabelColor = Color.White,
                            containerColor = DarkSurfaceVariant,
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }
        }

        items(filtered) { template ->
            TemplateCard(
                template = template,
                onUse = { onUseTemplate(template) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TemplateCard(
    template: VideoTemplate,
    onUse: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, Color(0xFF2B3042), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color(template.primaryColorHex).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(template.badge, color = Color(template.primaryColorHex), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Text("${template.durationSec}s • ${template.aspectRatio.label}", color = Color(0xFF94A3B8), fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(template.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(template.description, color = Color(0xFF94A3B8), fontSize = 12.sp, lineHeight = 16.sp)

        // Subtitle snippet preview with Nastaliq font
        if (template.sampleSubtitles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1118), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "“${template.sampleSubtitles.first().text}”",
                    color = Color(0xFFFDE047),
                    fontSize = 13.sp,
                    fontFamily = NotoNastaliqUrduFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onUse,
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Use This Template in Editor", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AiToolsTabContent(
    viewModel: VideoEditorViewModel,
    onOpenEditor: () -> Unit,
    onGoToSettings: () -> Unit
) {
    val apiKey = viewModel.settingsRepository.getApiKey()
    val scope = rememberCoroutineScope()

    var scriptPrompt by remember { mutableStateOf("30s viral reel on focus and success with Urdu voiceover") }
    var isGeneratingScript by remember { mutableStateOf(false) }
    var scriptOutputMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Header & Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Creative Suite", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            if (apiKey.isBlank()) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF7F1D1D), RoundedCornerShape(8.dp))
                        .clickable(onClick = onGoToSettings)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Enter API Key", color = Color(0xFFFCA5A5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF065F46), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Gemini Online", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Feature 1: Script to Video Director
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .border(1.dp, Color(0xFF2C3247), RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Text("AI Script-To-Video Director", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Transforms your idea into a 3-scene video timeline with Urdu voiceover.", color = Color(0xFF94A3B8), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = scriptPrompt,
                onValueChange = { scriptPrompt = it },
                label = { Text("Enter Video Topic or Reel Concept", color = Color(0xFF94A3B8)) },
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

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    isGeneratingScript = true
                    scriptOutputMessage = "Generating scenes with Gemini AI..."
                    scope.launch {
                        val response = viewModel.geminiService.generateScriptToVideo(scriptPrompt, apiKey)
                        isGeneratingScript = false
                        viewModel.createNewProject(AspectRatioType.RATIO_9_16)
                        scriptOutputMessage = "✓ Created \"${response.title}\" with ${response.scenes.size} scenes!"
                        onOpenEditor()
                    }
                },
                enabled = !isGeneratingScript,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isGeneratingScript) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Creating Project...")
                } else {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate & Open in Editor", fontWeight = FontWeight.Bold)
                }
            }

            if (scriptOutputMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(scriptOutputMessage, color = Color(0xFF34D399), fontSize = 11.sp)
            }
        }

        // Feature 2: Quick Jump Cards for Subtitles & Color Grading
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, Color(0xFF2C3247), RoundedCornerShape(12.dp))
                    .clickable {
                        viewModel.createNewProject(AspectRatioType.RATIO_9_16)
                        viewModel.openSheet(com.example.edit88.viewmodel.SheetType.SUBTITLES)
                        onOpenEditor()
                    }
                    .padding(14.dp)
            ) {
                Icon(Icons.Default.TextFields, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Urdu Auto-Subs", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Karaoke word glow & Nastaliq fonts", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, Color(0xFF2C3247), RoundedCornerShape(12.dp))
                    .clickable {
                        viewModel.createNewProject(AspectRatioType.RATIO_9_16)
                        viewModel.openSheet(com.example.edit88.viewmodel.SheetType.FILTERS)
                        onOpenEditor()
                    }
                    .padding(14.dp)
            ) {
                Icon(Icons.Default.Whatshot, contentDescription = null, tint = AccentPink, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("AI Color Grading", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Prompt-driven LUT matrix and tone", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun SettingsTabContent(
    viewModel: VideoEditorViewModel
) {
    SettingsSheet(
        settingsRepository = viewModel.settingsRepository,
        geminiService = viewModel.geminiService,
        onClose = {}
    )
}

@Composable
private fun HeroNewProjectCard(
    onNewProject: () -> Unit,
    onImportVideos: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 1. Primary Hero: Device Storage Video Import (CapCut Style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF1E1B4B),
                            Color(0xFF311042),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(
                    1.5.dp,
                    Brush.linearGradient(listOf(AccentCyan, AccentPurple)),
                    RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onImportVideos)
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(AccentCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("CapCut Workflow", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Multi-Clip Support", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Import Device Videos",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Phone ki local storage ya gallery se single ya multiple video clips import karein",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Import",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // 2. Secondary Card: Blank Timeline Project
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF161824))
                .border(1.dp, Color(0xFF282D3E), RoundedCornerShape(14.dp))
                .clickable(onClick = onNewProject)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF232839), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("New Blank Project", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Pick ratio: 9:16 Reels, 16:9 Cinema, 1:1 Square", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }

                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun NewProjectDialog(
    onSelectRatio: (AspectRatioType) -> Unit,
    onImportVideos: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF2C3247), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Create New Project", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                // Import option directly inside dialog
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(AccentPurple.copy(alpha = 0.3f), AccentCyan.copy(alpha = 0.3f))
                            )
                        )
                        .border(1.dp, AccentCyan, RoundedCornerShape(10.dp))
                        .clickable { onImportVideos() }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PermMedia, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Import From Device Storage", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Select video(s) from phone gallery", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AccentCyan)
                }

                Text("Or select aspect ratio for blank timeline:", color = Color(0xFF94A3B8), fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))

                AspectRatioType.values().forEach { ratio ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFF1E212D), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF2E3345), RoundedCornerShape(10.dp))
                            .clickable { onSelectRatio(ratio) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(ratio.label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(ratio.iconDesc, color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AccentCyan)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262C3E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color(0xFFE2E8F0))
                }
            }
        }
    }
}

