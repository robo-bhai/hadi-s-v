package com.hadiscare.app.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.hadiscare.app.audio.AudioRecorderHelper
import com.hadiscare.app.ui.theme.BabyLoveBorder
import com.hadiscare.app.ui.theme.BabyLoveHeart
import com.hadiscare.app.ui.theme.BabyLoveMilk
import com.hadiscare.app.ui.theme.BabyLovePrimary
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun RecordAudioDialog(
    sampleNumber: Int,
    isTrigger: Boolean,
    onAudioRecorded: (file: File, title: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recorderHelper = remember { AudioRecorderHelper(context) }

    var isRecording by remember { mutableStateOf(false) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            elapsedSeconds = 0
            while (isRecording) {
                delay(1000)
                elapsedSeconds++
                if (elapsedSeconds >= 30) {
                    val file = recorderHelper.stopRecording()
                    isRecording = false
                    recordedFile = file
                    break
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (isRecording) {
                recorderHelper.stopRecording()
            }
            onDismiss()
        },
        modifier = modifier.testTag("record_audio_dialog"),
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = if (isTrigger) "Record Sample 1: Baby Cry / Voice" else "Record Sample 2: Loving Lullaby Voice",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isTrigger)
                        "Record 3-5 seconds of your baby's cry, weeping, or vocal sounds so the app can recognize it."
                    else
                        "Record your own comforting voice (\"Shhh baby, mommy & daddy love you, sleep peacefully...\").",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Big mic record button
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRecording) BabyLoveHeart.copy(alpha = 0.2f)
                            else BabyLoveMilk
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (!isRecording) {
                                val targetFileName = if (isTrigger) "recorded_cry_sample.mp4" else "recorded_response_sample.mp4"
                                recordedFile = recorderHelper.startRecording(targetFileName)
                                isRecording = true
                            } else {
                                val file = recorderHelper.stopRecording()
                                isRecording = false
                                recordedFile = file
                            }
                        },
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("mic_record_toggle_button"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isRecording) BabyLoveHeart else BabyLovePrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isRecording) "Recording: ${elapsedSeconds}s (tap to stop)"
                    else if (recordedFile != null) "Recording complete (${elapsedSeconds}s) ready to save 💕"
                    else "Tap microphone to record",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = if (isRecording) BabyLoveHeart else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    recordedFile?.let { file ->
                        val defaultTitle = if (isTrigger) "Recorded Baby Voice (${elapsedSeconds}s)" else "Recorded Loving Lullaby (${elapsedSeconds}s)"
                        onAudioRecorded(file, defaultTitle)
                    }
                    onDismiss()
                },
                enabled = recordedFile != null && !isRecording,
                modifier = Modifier.testTag("save_recording_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BabyLovePrimary
                )
            ) {
                Text("Save as Sample $sampleNumber")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    if (isRecording) recorderHelper.stopRecording()
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
