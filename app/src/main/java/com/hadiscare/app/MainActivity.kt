package com.hadiscare.app

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hadiscare.app.model.NavigationTab
import com.hadiscare.app.ui.HadiCareViewModel
import com.hadiscare.app.ui.components.AudioSampleCard
import com.hadiscare.app.ui.components.ControlsTabScreen
import com.hadiscare.app.ui.components.FirstLaunchSetupDialog
import com.hadiscare.app.ui.components.HistoryTabScreen
import com.hadiscare.app.ui.components.LiveMonitorCard
import com.hadiscare.app.ui.components.RecordAudioDialog
import com.hadiscare.app.ui.components.SampleManagerDialog
import com.hadiscare.app.ui.theme.BabyLoveBorder
import com.hadiscare.app.ui.theme.BabyLoveMilk
import com.hadiscare.app.ui.theme.BabyLovePrimary
import com.hadiscare.app.ui.theme.HadisCareTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HadiCareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HadisCareTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: HadiCareViewModel) {
    val context = LocalContext.current

    val triggerSamples by viewModel.triggerSamples.collectAsStateWithLifecycle()
    val triggerSample by viewModel.triggerSample.collectAsStateWithLifecycle()
    val responseSample by viewModel.responseSample.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val history by viewModel.triggerHistory.collectAsStateWithLifecycle()
    val isMonitoringActive by viewModel.isMonitoringActive.collectAsStateWithLifecycle()

    val monitorStatus by viewModel.monitorStatus.collectAsStateWithLifecycle()
    val isServiceRunning by viewModel.isServiceRunning.collectAsStateWithLifecycle()
    val currentDecibels by viewModel.currentDecibels.collectAsStateWithLifecycle()
    val dominantFrequency by viewModel.dominantFrequency.collectAsStateWithLifecycle()
    val soundClassification by viewModel.soundClassification.collectAsStateWithLifecycle()
    val matchConfidence by viewModel.matchConfidence.collectAsStateWithLifecycle()
    val sample1Similarity by viewModel.sample1Similarity.collectAsStateWithLifecycle()
    val triggerCount by viewModel.triggerCount.collectAsStateWithLifecycle()
    val isCalibrating by viewModel.isCalibrating.collectAsStateWithLifecycle()
    val calibrationCountdown by viewModel.calibrationCountdown.collectAsStateWithLifecycle()
    val playingPreviewId by viewModel.playingPreviewId.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }
    var recordingDialogSampleNumber by remember { mutableStateOf<Int?>(null) }
    var pickingSampleNumber by remember { mutableStateOf<Int?>(null) }
    var isSampleManagerOpen by remember { mutableStateOf(false) }

    // Auto-start listening on app launch / restart if monitoring toggle was previously ON
    LaunchedEffect(Unit) {
        if (isMonitoringActive && !isServiceRunning) {
            val recordAudioPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            if (recordAudioPerm == PackageManager.PERMISSION_GRANTED) {
                viewModel.startMonitoring(context)
            }
        }
    }

    // Audio file picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = queryFileName(context, it) ?: "audio_file.mp3"
            val isTrigger = pickingSampleNumber == 1
            if (isTrigger) {
                viewModel.addCustomTriggerSampleFromUri(it, fileName)
                Toast.makeText(context, "Added Baby Voice Sample: $fileName 💕", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.saveAudioFileFromPicker(it, false, fileName)
                Toast.makeText(context, "Loaded Response Lullaby: $fileName 🎵", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (recordAudioGranted) {
            viewModel.startMonitoring(context)
            Toast.makeText(context, "Hadi's Care Baby Monitor Activated 💕", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Microphone permission is required to listen for baby sounds", Toast.LENGTH_LONG).show()
        }
    }

    fun checkAndToggleMonitoring() {
        if (isServiceRunning) {
            viewModel.stopMonitoring(context)
        } else {
            val recordAudioPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true

            if (recordAudioPerm == PackageManager.PERMISSION_GRANTED && hasNotifications) {
                viewModel.startMonitoring(context)
            } else {
                val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                permissionLauncher.launch(perms.toTypedArray())
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BabyLoveMilk),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "App Logo",
                                tint = BabyLovePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Hadi's Care",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "💕",
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "Smart Baby Audio Monitor",
                                style = MaterialTheme.typography.labelSmall,
                                color = BabyLovePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    if (isServiceRunning) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BabyLoveMilk,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(BabyLovePrimary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BabyLovePrimary
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.HOME,
                    onClick = { selectedTab = NavigationTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home Tab") },
                    label = { Text("Home", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BabyLovePrimary,
                        selectedTextColor = BabyLovePrimary,
                        indicatorColor = BabyLoveMilk
                    ),
                    modifier = Modifier.testTag("tab_home")
                )

                NavigationBarItem(
                    selected = selectedTab == NavigationTab.CONTROLS,
                    onClick = { selectedTab = NavigationTab.CONTROLS },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Controls Tab") },
                    label = { Text("Controls", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BabyLovePrimary,
                        selectedTextColor = BabyLovePrimary,
                        indicatorColor = BabyLoveMilk
                    ),
                    modifier = Modifier.testTag("tab_controls")
                )

                NavigationBarItem(
                    selected = selectedTab == NavigationTab.HISTORY,
                    onClick = { selectedTab = NavigationTab.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = "History Tab") },
                    label = { Text("History", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BabyLovePrimary,
                        selectedTextColor = BabyLovePrimary,
                        indicatorColor = BabyLoveMilk
                    ),
                    modifier = Modifier.testTag("tab_history")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(max = 640.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    NavigationTab.HOME -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // 1. Clear & Attractive Live Monitor Card
                            LiveMonitorCard(
                                isServiceRunning = isServiceRunning,
                                monitorStatus = monitorStatus,
                                currentDecibels = currentDecibels,
                                thresholdDb = settings.thresholdDb,
                                triggerCount = triggerCount,
                                dominantFrequencyHz = dominantFrequency,
                                soundClassification = soundClassification,
                                matchConfidence = matchConfidence,
                                sample1Similarity = sample1Similarity,
                                strictSample1Matching = settings.strictSample1Matching,
                                isCalibrating = isCalibrating,
                                calibrationCountdown = calibrationCountdown,
                                onToggle = { checkAndToggleMonitoring() },
                                onSimulateTrigger = {
                                    viewModel.simulateTestTrigger(context)
                                    Toast.makeText(context, "Playing response lullaby...", Toast.LENGTH_SHORT).show()
                                },
                                onCalibrate = {
                                    viewModel.startRoomCalibration(context)
                                    Toast.makeText(context, "Calibrating room noise for 5 seconds...", Toast.LENGTH_SHORT).show()
                                }
                            )

                            // 2. Audio Samples Setup Section
                            Text(
                                text = "Audio Samples Setup",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )

                            AudioSampleCard(
                                sampleNumber = 1,
                                title = "Trigger Sound",
                                subtitle = "Baby voice, cry, or recorded audio",
                                icon = Icons.Default.Hearing,
                                sampleInfo = triggerSample,
                                isPlayingPreview = playingPreviewId == triggerSample.id || playingPreviewId == "trigger",
                                sampleCount = triggerSamples.size,
                                onManageSamples = { isSampleManagerOpen = true },
                                onPickFile = {
                                    pickingSampleNumber = 1
                                    filePickerLauncher.launch(arrayOf("audio/*", "application/ogg"))
                                },
                                onRecordAudio = {
                                    recordingDialogSampleNumber = 1
                                },
                                onUsePreset = {
                                    viewModel.addPresetTriggerSample()
                                    Toast.makeText(context, "Loaded Infant Cry Preset", Toast.LENGTH_SHORT).show()
                                },
                                onTogglePreview = {
                                    viewModel.togglePlayPreview(triggerSample.id, triggerSample.filePath)
                                }
                            )

                            AudioSampleCard(
                                sampleNumber = 2,
                                title = "Response Sound",
                                subtitle = "Loving lullaby played on detection",
                                icon = Icons.Default.MusicNote,
                                sampleInfo = responseSample,
                                isPlayingPreview = playingPreviewId == "response",
                                onPickFile = {
                                    pickingSampleNumber = 2
                                    filePickerLauncher.launch(arrayOf("audio/*", "application/ogg"))
                                },
                                onRecordAudio = {
                                    recordingDialogSampleNumber = 2
                                },
                                onUsePreset = {
                                    viewModel.useDefaultResponseSample()
                                    Toast.makeText(context, "Loaded Soothing Lullaby Preset", Toast.LENGTH_SHORT).show()
                                },
                                onTogglePreview = {
                                    viewModel.togglePlayPreview("response", responseSample.filePath)
                                }
                            )
                        }
                    }

                    NavigationTab.CONTROLS -> {
                        ControlsTabScreen(
                            settings = settings,
                            onUpdateThreshold = { viewModel.updateThreshold(it) },
                            onUpdateFrequencyFilter = { viewModel.updateFrequencyFilter(it) },
                            onUpdateStrictSample1 = { viewModel.updateStrictSample1Matching(it) },
                            onUpdateAdultGuard = { viewModel.updateAdultSpeechGuard(it) },
                            onUpdateBabyWhisper = { viewModel.updateBabyWhisperDetector(it) },
                            onUpdateTransientRejector = { viewModel.updateTransientNoiseRejector(it) },
                            onUpdateContinuousLoop = { viewModel.updateContinuousSoothingLoop(it) },
                            onUpdateCooldownEnabled = { viewModel.updateCooldownTimerEnabled(it) },
                            onUpdateCooldownSeconds = { viewModel.updateCooldownDurationSeconds(it) },
                            onUpdateVolume = { viewModel.updateVolume(it) }
                        )
                    }

                    NavigationTab.HISTORY -> {
                        HistoryTabScreen(
                            history = history,
                            onClearHistory = { viewModel.clearTriggerHistory() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // First launch onboarding setup dialog
    if (!settings.isFirstLaunchCompleted) {
        FirstLaunchSetupDialog(
            onQuickStartPresets = {
                viewModel.useDefaultTriggerSample()
                viewModel.useDefaultResponseSample()
                viewModel.markFirstLaunchComplete()
                Toast.makeText(context, "Baby Love setup ready! Ready to monitor.", Toast.LENGTH_SHORT).show()
            },
            onCustomSelection = {
                viewModel.markFirstLaunchComplete()
                pickingSampleNumber = 1
                filePickerLauncher.launch(arrayOf("audio/*", "application/ogg"))
            }
        )
    }

    // Custom in-app recording dialog
    recordingDialogSampleNumber?.let { sampleNum ->
        val isTrigger = sampleNum == 1
        RecordAudioDialog(
            sampleNumber = sampleNum,
            isTrigger = isTrigger,
            onAudioRecorded = { file, title ->
                if (isTrigger) {
                    viewModel.addRecordedTriggerSample(file, title)
                } else {
                    viewModel.saveRecordedAudio(file, false, title)
                }
                Toast.makeText(context, "Saved: $title 💕", Toast.LENGTH_SHORT).show()
                recordingDialogSampleNumber = null
            },
            onDismiss = {
                recordingDialogSampleNumber = null
            }
        )
    }

    // Baby Voice Samples Manager Dialog / Window
    if (isSampleManagerOpen) {
        SampleManagerDialog(
            samples = triggerSamples,
            playingPreviewId = playingPreviewId,
            onPlayPreview = { sampleId, path ->
                viewModel.togglePlayPreview(sampleId, path)
            },
            onDeleteSample = { sampleId ->
                viewModel.deleteTriggerSample(sampleId)
                Toast.makeText(context, "Sample deleted", Toast.LENGTH_SHORT).show()
            },
            onRecordNew = {
                recordingDialogSampleNumber = 1
            },
            onAddFromFile = {
                pickingSampleNumber = 1
                filePickerLauncher.launch(arrayOf("audio/*", "application/ogg"))
            },
            onAddPreset = {
                viewModel.addPresetTriggerSample()
                Toast.makeText(context, "Preset cry sample added 💕", Toast.LENGTH_SHORT).show()
            },
            onPauseListening = {
                viewModel.pauseMonitoringForManager(context)
            },
            onResumeListening = {
                viewModel.resumeMonitoringAfterManager(context)
            },
            onDismiss = {
                isSampleManagerOpen = false
            }
        )
    }
}

private fun queryFileName(context: android.content.Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                cursor.getString(nameIndex)
            } else null
        }
    } catch (e: Exception) {
        null
    }
}
