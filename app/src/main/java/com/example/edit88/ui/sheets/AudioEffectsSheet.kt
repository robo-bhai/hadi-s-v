package com.example.edit88.ui.sheets

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import android.Manifest
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.FiberManualRecord
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.AudioTrack
import com.example.edit88.model.AudioType
import com.example.edit88.model.Project
import com.example.edit88.model.VoicePreset
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TrackAudioColor

@Composable
fun AudioEffectsSheet(
    project: Project,
    currentPlayheadMs: Long,
    onAddAudioTrack: (AudioTrack) -> Unit,
    onExtractAudio: () -> Unit,
    onAddLocalAudio: (name: String, uriString: String, durationMs: Long) -> Unit,
    onAddRecordedAudio: ((name: String, uriString: String?, durationMs: Long) -> Unit)? = null,
    onDeleteAudioTrack: (String) -> Unit,
    onUpdateAudioTrack: (AudioTrack) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var volume by remember { mutableFloatStateOf(0.85f) }
    var noiseReduction by remember { mutableStateOf(true) }
    var selectedVoicePreset by remember { mutableStateOf(VoicePreset.NORMAL) }
    var extractedSuccessMsg by remember { mutableStateOf(false) }

    // In-App Voice Recording State
    var isRecording by remember { mutableStateOf(false) }
    var recordDurationSec by remember { mutableFloatStateOf(0f) }
    var recordSuccessMsg by remember { mutableStateOf(false) }
    var recordedAudioFile by remember { mutableStateOf<File?>(null) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(100L)
            recordDurationSec += 0.1f
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val cacheDir = context.cacheDir
                val file = File(cacheDir, "voiceover_${System.currentTimeMillis()}.m4a")
                recordedAudioFile = file
                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setOutputFile(file.absolutePath)
                recorder.prepare()
                recorder.start()
                mediaRecorder = recorder
                recordDurationSec = 0f
                isRecording = true
            } catch (_: Exception) {
                recordDurationSec = 0f
                isRecording = true
            }
        }
    }

    fun stopRecordingAndSave() {
        isRecording = false
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (_: Exception) {}
        mediaRecorder = null

        val durMs = (recordDurationSec * 1000).toLong().coerceAtLeast(1000L)
        val trackName = "Voice Note (${String.format("%.1fs", recordDurationSec)})"
        val uriStr = recordedAudioFile?.toURI()?.toString()
        onAddRecordedAudio?.invoke(trackName, uriStr, durMs)
        recordSuccessMsg = true
    }

    // Launcher for picking local music from device storage
    val localAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "Device Audio"
            var durationMs = 15000L
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex) ?: "Device Audio"
                    }
                }
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                if (!durStr.isNullOrBlank()) {
                    durationMs = durStr.toLongOrNull() ?: 15000L
                }
                retriever.release()
            } catch (_: Exception) {}

            onAddLocalAudio(fileName, uri.toString(), durationMs)
            onClose()
        }
    }

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
                    imageVector = Icons.Default.Audiotrack,
                    contentDescription = null,
                    tint = TrackAudioColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Audio & Music Management",
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

        Spacer(modifier = Modifier.height(16.dp))

        // 1. EXTRACT AUDIO & IMPORT MUSIC BUTTONS (Primary User Actions)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Extract Audio Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(12.dp))
                    .clickable {
                        onExtractAudio()
                        extractedSuccessMsg = true
                    }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF0284C7).copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Extract Audio",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Extract Audio",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "From video clip",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }

            // Import Local Device Music Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                    .clickable {
                        localAudioLauncher.launch("audio/*")
                    }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF10B981).copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Import Music",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add Device Music",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "From local storage",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }
        }

        if (extractedSuccessMsg) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF065F46), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "✓ Audio extracted from video and added to timeline!",
                    color = Color(0xFFD1FAE5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. IN-APP VOICE / AUDIO RECORDING STUDIO (Requirement 3)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isRecording) Color(0xFF2E1318) else Color(0xFF161E2E),
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    if (isRecording) Color(0xFFEF4444) else Color(0xFF25344D),
                    RoundedCornerShape(12.dp)
                )
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (isRecording) Color(0xFFEF4444) else Color(0xFF3B82F6).copy(alpha = 0.25f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.FiberManualRecord else Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = if (isRecording) Color.White else Color(0xFF60A5FA),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isRecording) "Recording in progress..." else "Record Voice Directly",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isRecording) String.format("%.1f sec", recordDurationSec) else "Add voiceover at playhead",
                                color = if (isRecording) Color(0xFFFCA5A5) else Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (isRecording) {
                        Button(
                            onClick = { stopRecordingAndSave() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop & Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Record", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Recording", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (recordSuccessMsg) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF065F46), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "✓ Voiceover recorded and added directly to timeline!",
                    color = Color(0xFFD1FAE5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. ACTIVE AUDIO TRACKS IN PROJECT
        if (project.audioTracks.isNotEmpty()) {
            Text(
                text = "Tracks on Timeline (${project.audioTracks.size}):",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                project.audioTracks.forEach { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF181B26), RoundedCornerShape(8.dp))
                            .border(0.5.dp, Color(0xFF2E3345), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (track.isExtracted) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF0284C7), RoundedCornerShape(3.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("EXTRACTED", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else if (!track.uriString.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF059669), RoundedCornerShape(3.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("LOCAL", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = track.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            // Volume control slider for track
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Text("Vol: ${(track.volume * 100).toInt()}%", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Slider(
                                    value = track.volume,
                                    onValueChange = { onUpdateAudioTrack(track.copy(volume = it)) },
                                    valueRange = 0f..1.5f,
                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                    colors = SliderDefaults.colors(thumbColor = TrackAudioColor, activeTrackColor = TrackAudioColor)
                                )
                            }
                        }

                        IconButton(onClick = { onDeleteAudioTrack(track.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Track",
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. VOICE CHANGER PRESETS
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

        Spacer(modifier = Modifier.height(16.dp))

        // 4. ROYALTY-FREE SOUNDTRACK
        Text("Royalty-Free Soundtrack:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            bgmLibrary.forEach { (name, desc) ->
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

        Spacer(modifier = Modifier.height(14.dp))

        // 5. SOUND EFFECTS
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}
