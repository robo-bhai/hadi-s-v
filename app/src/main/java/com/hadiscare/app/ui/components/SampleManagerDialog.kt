package com.hadiscare.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hadiscare.app.model.AudioSampleInfo
import com.hadiscare.app.ui.theme.BabyLoveBorder
import com.hadiscare.app.ui.theme.BabyLoveHeart
import com.hadiscare.app.ui.theme.BabyLoveMilk
import com.hadiscare.app.ui.theme.BabyLovePrimary
import com.hadiscare.app.ui.theme.BabyLoveRose

@Composable
fun SampleManagerDialog(
    samples: List<AudioSampleInfo>,
    playingPreviewId: String?,
    onPlayPreview: (sampleId: String, path: String?) -> Unit,
    onDeleteSample: (sampleId: String) -> Unit,
    onRecordNew: () -> Unit,
    onAddFromFile: () -> Unit,
    onAddPreset: () -> Unit,
    onPauseListening: () -> Unit,
    onResumeListening: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Automatically pause live listening when window opens, and resume when closed
    DisposableEffect(Unit) {
        onPauseListening()
        onDispose {
            onResumeListening()
        }
    }

    var sampleToDelete by remember { mutableStateOf<AudioSampleInfo?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .testTag("sample_manager_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, BabyLoveBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(BabyLoveMilk),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = BabyLovePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Baby Voice Samples",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${samples.size} active voice profile${if (samples.size > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = BabyLovePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_sample_manager_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Paused Notice & Multi-Sample Info Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BabyLoveMilk,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BabyLoveBorder.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏸",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column {
                            Text(
                                text = "Live listening paused during setup",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = BabyLovePrimary
                            )
                            Text(
                                text = "Add multiple samples of your baby's cry or whimper. All samples are auto-matched!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Samples List
                Text(
                    text = "Registered Samples Library",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(samples, key = { it.id }) { sample ->
                        val isPlaying = playingPreviewId == sample.id

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sample_item_${sample.id}"),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BabyLoveBorder.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Play / Pause Preview Button
                                IconButton(
                                    onClick = { onPlayPreview(sample.id, sample.filePath) },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .testTag("play_sample_${sample.id}"),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = if (isPlaying) BabyLovePrimary else BabyLoveMilk
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = if (isPlaying) Color.White else BabyLovePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Sample Info
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = sample.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val sourceText = when (sample.sourceType) {
                                        "DEFAULT" -> "Built-in Preset"
                                        "RECORDED" -> "Microphone Recording"
                                        "CUSTOM_FILE" -> "Audio File"
                                        else -> "User Sample"
                                    }
                                    val durationText = if (sample.durationSec > 0) " • ${sample.durationSec}s" else ""
                                    Text(
                                        text = "$sourceText$durationText",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Delete Button
                                IconButton(
                                    onClick = { sampleToDelete = sample },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("delete_sample_${sample.id}"),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = BabyLoveHeart.copy(alpha = 0.12f)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete sample",
                                        tint = BabyLoveHeart,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Add New & Record New
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRecordNew,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("record_new_sample_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BabyLovePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Record New",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    FilledTonalButton(
                        onClick = onAddFromFile,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_file_sample_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = BabyLoveMilk,
                            contentColor = BabyLovePrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Audio",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary row: Add Preset Cry & Done
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddPreset,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_preset_sample_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BabyLoveBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = BabyLovePrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Preset Cry",
                            style = MaterialTheme.typography.labelMedium,
                            color = BabyLovePrimary
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("done_samples_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(
                            text = "Done & Resume",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog before deleting
    sampleToDelete?.let { sample ->
        AlertDialog(
            onDismissRequest = { sampleToDelete = null },
            title = {
                Text(
                    text = "Delete Baby Sample?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${sample.title}\"? It will no longer be used for baby voice recognition.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSample(sample.id)
                        sampleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BabyLoveHeart,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { sampleToDelete = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(22.dp)
        )
    }
}
