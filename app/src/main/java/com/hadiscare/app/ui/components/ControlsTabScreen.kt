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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.sp
import com.hadiscare.app.model.MonitorSettings
import com.hadiscare.app.ui.theme.BabyLoveBorder
import com.hadiscare.app.ui.theme.BabyLoveMilk
import com.hadiscare.app.ui.theme.BabyLovePrimary

@Composable
fun ControlsTabScreen(
    settings: MonitorSettings,
    onUpdateThreshold: (Float) -> Unit,
    onUpdateFrequencyFilter: (Boolean) -> Unit,
    onUpdateStrictSample1: (Boolean) -> Unit,
    onUpdateAdultGuard: (Boolean) -> Unit,
    onUpdateBabyWhisper: (Boolean) -> Unit,
    onUpdateTransientRejector: (Boolean) -> Unit,
    onUpdateContinuousLoop: (Boolean) -> Unit,
    onUpdateCooldownEnabled: (Boolean) -> Unit,
    onUpdateCooldownSeconds: (Int) -> Unit,
    onUpdateVolume: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Accuracy & Detection Toggles
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("accuracy_toggles_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, BabyLoveBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(BabyLoveMilk),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = BabyLovePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Accuracy & Recognition Toggles",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Filter unwanted sounds and tune voice precision",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 1. Strict Sample 1 Match Toggle
                ControlSwitchRow(
                    icon = Icons.Default.Fingerprint,
                    title = "Strict Sample 1 Voice Match",
                    subtitle = "Sirf recorded Sample 1 voice aur exact pitch par chale (baqi sab ignore)",
                    checked = settings.strictSample1Matching,
                    onCheckedChange = onUpdateStrictSample1,
                    testTag = "strict_sample1_switch"
                )

                // 2. Adult Speech Guard Toggle
                ControlSwitchRow(
                    icon = Icons.Default.Block,
                    title = "Adult Speech Guard",
                    subtitle = "Baron ki awaz (talking, TV, phone calls) 100% block kare",
                    checked = settings.adultSpeechGuard,
                    onCheckedChange = onUpdateAdultGuard,
                    testTag = "adult_speech_guard_switch"
                )

                // 3. Baby Whimper & Whisper Detector Toggle
                ControlSwitchRow(
                    icon = Icons.Default.Hearing,
                    title = "Baby Whimper & Whisper Detector",
                    subtitle = "Raat ke waqt dheemi siskiyan aur soft weeping bhi capture kare",
                    checked = settings.babyWhisperDetector,
                    onCheckedChange = onUpdateBabyWhisper,
                    testTag = "baby_whisper_switch"
                )

                // 4. Transient Noise & Door Slam Rejector Toggle
                ControlSwitchRow(
                    icon = Icons.Default.Campaign,
                    title = "Door Slam & Clap Rejector",
                    subtitle = "Achanak bartan girna, taali, ya darwaza band hone ki awaz reject kare",
                    checked = settings.transientNoiseRejector,
                    onCheckedChange = onUpdateTransientRejector,
                    testTag = "transient_rejector_switch"
                )

                // 5. Infant Cry Formant Resonance Filter Toggle
                ControlSwitchRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "Infant Cry Tremor & Pitch Model",
                    subtitle = "Baby cry ke khas 400-600 Hz cadence aur wailing envelope ko verify kare",
                    checked = settings.frequencyFilterEnabled,
                    onCheckedChange = onUpdateFrequencyFilter,
                    testTag = "frequency_filter_switch"
                )
            }
        }

        // Section 2: Sensitivity & Room Tuning
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sensitivity_tuning_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, BabyLoveBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(BabyLoveMilk),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = BabyLovePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Acoustic Sensitivity Level",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Calibrated Room Baseline: ${settings.calibratedAmbientDb.toInt()} dB",
                            style = MaterialTheme.typography.bodySmall,
                            color = BabyLovePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets chips row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Pair("Whisper (42 dB)", 42f),
                        Pair("Standard Cry (48 dB)", 48f),
                        Pair("Loud Cry (60 dB)", 60f)
                    ).forEach { (label, dbVal) ->
                        val isSelected = kotlin.math.abs(settings.thresholdDb - dbVal) < 2.5f
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateThreshold(dbVal) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BabyLovePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Threshold Setting:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${settings.thresholdDb.toInt()} dB SPL",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = BabyLovePrimary
                    )
                }

                Slider(
                    value = settings.thresholdDb,
                    onValueChange = { onUpdateThreshold(it) },
                    valueRange = 35f..85f,
                    steps = 50,
                    modifier = Modifier.testTag("threshold_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = BabyLovePrimary,
                        activeTrackColor = BabyLovePrimary
                    )
                )
            }
        }

        // Section 3: Smart Playback & Care Controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("playback_controls_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, BabyLoveBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(BabyLoveMilk),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = BabyLovePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Playback & Soothing Loops",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Lullaby repeat behavior and anti-spam limits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 6. Continuous Soothing Loop Toggle
                ControlSwitchRow(
                    icon = Icons.Default.Repeat,
                    title = "Continuous Soothing Loop",
                    subtitle = "Agar baby abhi bhi ro raha ho to lullaby auto-repeat kare",
                    checked = settings.continuousSoothingLoop,
                    onCheckedChange = onUpdateContinuousLoop,
                    testTag = "continuous_loop_switch"
                )

                // 7. Cooldown Timer Toggle
                ControlSwitchRow(
                    icon = Icons.Default.HourglassBottom,
                    title = "Playback Cooldown Guard",
                    subtitle = "Lullaby bajne ke baad rest interval taake bar bar repetitive trigger na ho",
                    checked = settings.cooldownTimerEnabled,
                    onCheckedChange = onUpdateCooldownEnabled,
                    testTag = "cooldown_guard_switch"
                )

                if (settings.cooldownTimerEnabled) {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rest Interval Duration:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${settings.cooldownDurationSeconds} Seconds",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = BabyLovePrimary
                            )
                        }

                        Slider(
                            value = settings.cooldownDurationSeconds.toFloat(),
                            onValueChange = { onUpdateCooldownSeconds(it.toInt()) },
                            valueRange = 15f..180f,
                            steps = 11,
                            modifier = Modifier.testTag("cooldown_seconds_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = BabyLovePrimary,
                                activeTrackColor = BabyLovePrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Volume Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = BabyLovePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lullaby Volume:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = "${(settings.responseVolume * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = BabyLovePrimary
                    )
                }

                Slider(
                    value = settings.responseVolume,
                    onValueChange = { onUpdateVolume(it) },
                    valueRange = 0.1f..1.0f,
                    modifier = Modifier.testTag("volume_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = BabyLovePrimary,
                        activeTrackColor = BabyLovePrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun ControlSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BabyLoveBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BabyLovePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BabyLovePrimary
                )
            )
        }
    }
}
