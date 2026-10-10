package com.hadiscare.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hadiscare.app.model.MonitorStatus
import com.hadiscare.app.ui.theme.BabyLoveBorder
import com.hadiscare.app.ui.theme.BabyLoveHeart
import com.hadiscare.app.ui.theme.BabyLoveLullabyGold
import com.hadiscare.app.ui.theme.BabyLoveMilk
import com.hadiscare.app.ui.theme.BabyLovePrimary
import com.hadiscare.app.ui.theme.BabyLoveRose
import com.hadiscare.app.ui.theme.BabyLoveSoftMint

@Composable
fun LiveMonitorCard(
    isServiceRunning: Boolean,
    monitorStatus: MonitorStatus,
    currentDecibels: Float,
    thresholdDb: Float,
    triggerCount: Int,
    dominantFrequencyHz: Float,
    soundClassification: String,
    matchConfidence: Float,
    sample1Similarity: Float,
    strictSample1Matching: Boolean,
    isCalibrating: Boolean,
    calibrationCountdown: Int,
    onToggle: () -> Unit,
    onSimulateTrigger: () -> Unit,
    onCalibrate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val isAdultOrNoise = soundClassification.contains("Adult") || soundClassification.contains("Ambient") || soundClassification.contains("Blocked")
    val isBabyDetected = soundClassification.contains("Baby") || soundClassification.contains("Matched")

    val statusColor by animateColorAsState(
        targetValue = when {
            isCalibrating -> BabyLoveLullabyGold
            monitorStatus == MonitorStatus.PLAYING_RESPONSE -> BabyLoveSoftMint
            monitorStatus == MonitorStatus.TRIGGER_DETECTED -> BabyLoveHeart
            isBabyDetected && isServiceRunning -> BabyLoveHeart
            isAdultOrNoise && isServiceRunning -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            monitorStatus == MonitorStatus.LISTENING -> BabyLovePrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        },
        label = "statusColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_monitor_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, BabyLoveBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with status badge and WakeLock indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                isCalibrating -> Icons.Default.HourglassTop
                                isAdultOrNoise -> Icons.Default.Block
                                else -> Icons.Default.Favorite
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isCalibrating -> "CALIBRATING ROOM (${calibrationCountdown}s)"
                                monitorStatus == MonitorStatus.LISTENING -> if (strictSample1Matching) "STRICT VOICE MATCHING" else "BABY VOICE GUARD ACTIVE"
                                monitorStatus == MonitorStatus.TRIGGER_DETECTED -> "BABY CRY DETECTED 💕"
                                monitorStatus == MonitorStatus.PLAYING_RESPONSE -> "SOOTHING BABY WITH LULLABY 🎵"
                                else -> "MONITOR STANDBY"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }
                }

                if (isServiceRunning) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BabyLoveMilk
                    ) {
                        Text(
                            text = "WakeLock: ON",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = BabyLovePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Central Pulsing Visualizer Ring
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .scale(if (isServiceRunning || isCalibrating) pulseScale else 1.0f),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    statusColor.copy(alpha = if (isServiceRunning || isCalibrating) 0.35f else 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Middle circle with heart/icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            if (isServiceRunning || isCalibrating) statusColor.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            width = 2.dp,
                            color = statusColor.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isCalibrating -> Icons.Default.Tune
                            monitorStatus == MonitorStatus.PLAYING_RESPONSE -> Icons.Default.MusicNote
                            monitorStatus == MonitorStatus.TRIGGER_DETECTED -> Icons.Default.Warning
                            isAdultOrNoise -> Icons.Default.RecordVoiceOver
                            isServiceRunning -> Icons.Default.Hearing
                            else -> Icons.Default.ChildCare
                        },
                        contentDescription = "Monitor status icon",
                        tint = statusColor,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-time Sound Classification Badge
            if (isServiceRunning || isCalibrating) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when {
                        isCalibrating -> BabyLoveMilk
                        isAdultOrNoise -> MaterialTheme.colorScheme.surfaceVariant
                        isBabyDetected -> BabyLoveMilk
                        else -> MaterialTheme.colorScheme.surface
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isBabyDetected || isCalibrating) BabyLovePrimary else BabyLoveBorder.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isCalibrating) "Sampling Room Acoustics (${calibrationCountdown}s)..." else soundClassification,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = when {
                                isCalibrating || isBabyDetected -> BabyLovePrimary
                                isAdultOrNoise -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )

                        if (!isCalibrating && matchConfidence > 0.2f && !isAdultOrNoise) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BabyLovePrimary
                            ) {
                                Text(
                                    text = "${(matchConfidence * 100).toInt()}% Match",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Decibels readout with warm styling
            Text(
                text = if (isServiceRunning || isCalibrating) "${currentDecibels.toInt()} dB" else "-- dB",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (isServiceRunning) {
                    val pitchText = if (dominantFrequencyHz > 0) "Pitch: ${dominantFrequencyHz.toInt()} Hz • " else ""
                    val simText = if (sample1Similarity > 0.2f) "Sample 1: ${(sample1Similarity * 100).toInt()}% • " else ""
                    "$pitchText$simText Threshold: ${thresholdDb.toInt()} dB"
                } else if (isCalibrating) {
                    "Stay quiet while app calculates room noise floor"
                } else {
                    "Tap toggle switch below to start listening"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sound level meter bar
            val progressFraction = (currentDecibels / 100f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { if (isServiceRunning || isCalibrating) progressFraction else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .testTag("sound_level_indicator"),
                color = if (isBabyDetected) BabyLoveHeart else if (isAdultOrNoise) MaterialTheme.colorScheme.outline else BabyLoveRose,
                trackColor = MaterialTheme.colorScheme.surface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Toggle Switch Row with prominent click target
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BabyLoveBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (isServiceRunning) BabyLovePrimary else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Baby Audio Monitor",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (isServiceRunning) "Adult filter: ON • Baby guard: ON" else "Turn ON to protect your baby",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isServiceRunning,
                        onCheckedChange = { onToggle() },
                        modifier = Modifier.testTag("main_toggle_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BabyLovePrimary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: Calibrate Room Noise & Play Lullaby
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCalibrate,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("calibrate_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = BabyLovePrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCalibrating) "Calibrating..." else "Calibrate Room",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                FilledTonalButton(
                    onClick = onSimulateTrigger,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("simulate_cry_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = BabyLoveMilk,
                        contentColor = BabyLovePrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Play Lullaby",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (triggerCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Baby Cry Triggers Today: $triggerCount 💕",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = BabyLoveLullabyGold
                )
            }
        }
    }
}
