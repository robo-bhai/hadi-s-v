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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hadiscare.app.model.AudioSampleInfo
import com.hadiscare.app.ui.theme.BabyLoveBorder
import com.hadiscare.app.ui.theme.BabyLoveMilk
import com.hadiscare.app.ui.theme.BabyLovePrimary
import com.hadiscare.app.ui.theme.BabyLoveRose
import com.hadiscare.app.ui.theme.BabyLoveSoftMint

@Composable
fun AudioSampleCard(
    sampleNumber: Int,
    title: String,
    subtitle: String,
    icon: ImageVector,
    sampleInfo: AudioSampleInfo,
    isPlayingPreview: Boolean,
    sampleCount: Int = 1,
    onManageSamples: (() -> Unit)? = null,
    onPickFile: () -> Unit,
    onRecordAudio: () -> Unit,
    onUsePreset: () -> Unit,
    onTogglePreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sample_${sampleNumber}_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, BabyLoveBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title & badge row
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BabyLoveMilk),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = BabyLovePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Sample $sampleNumber: $title",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (sampleNumber == 1 && sampleCount > 1) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BabyLoveMilk
                                ) {
                                    Text(
                                        text = "$sampleCount Active",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = BabyLovePrimary
                                    )
                                }
                            }
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (sampleInfo.isSet) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Configured",
                        tint = BabyLoveSoftMint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current file display container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BabyLoveBorder.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = BabyLovePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = sampleInfo.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1
                            )
                            val sourceLabel = when (sampleInfo.sourceType) {
                                "DEFAULT" -> "Built-in Baby Preset"
                                "RECORDED" -> "Microphone Recording"
                                "CUSTOM_FILE" -> "User Audio File"
                                else -> "Not Configured"
                            }
                            val durationText = if (sampleInfo.durationSec > 0) " (${sampleInfo.durationSec}s)" else ""
                            Text(
                                text = "$sourceLabel$durationText",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Test listen / play preview button
                    IconButton(
                        onClick = onTogglePreview,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("preview_sample_${sampleNumber}_button"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isPlayingPreview) BabyLovePrimary else BabyLoveMilk
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlayingPreview) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlayingPreview) "Pause preview" else "Play preview",
                            tint = if (isPlayingPreview) Color.White else BabyLovePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // If manage samples callback provided (Sample 1), show primary "Manage Voice Samples" button
            if (onManageSamples != null) {
                Button(
                    onClick = onManageSamples,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manage_samples_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BabyLovePrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Manage Voice Samples ($sampleCount)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Action buttons row: Pick file, Record, Use Preset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onPickFile,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pick_sample_${sampleNumber}_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = BabyLoveMilk,
                        contentColor = BabyLovePrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Pick MP3", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onRecordAudio,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("record_sample_${sampleNumber}_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = BabyLovePrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Record", style = MaterialTheme.typography.labelMedium)
                }

                IconButton(
                    onClick = onUsePreset,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = BabyLoveMilk
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset to preset",
                        tint = BabyLovePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
