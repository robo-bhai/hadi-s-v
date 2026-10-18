package com.example.edit88.ui.sheets

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.edit88.model.ExportFps
import com.example.edit88.model.ExportFormat
import com.example.edit88.model.ExportResolution
import com.example.edit88.model.Project
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import kotlinx.coroutines.delay

@Composable
fun ExportDialog(
    project: Project,
    onDismiss: () -> Unit
) {
    var selectedRes by remember { mutableStateOf(ExportResolution.P1080) }
    var selectedFps by remember { mutableStateOf(ExportFps.FPS_30) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.MP4) }
    var bitrateMbps by remember { mutableFloatStateOf(16f) }

    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableFloatStateOf(0f) }
    var currentPhase by remember { mutableStateOf("Ready to encode") }
    var isCompleted by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(targetValue = exportProgress, label = "progress")

    // Realistic multi-pass rendering simulation
    LaunchedEffect(isExporting) {
        if (isExporting) {
            exportProgress = 0.05f
            currentPhase = "Analyzing timeline clips & keyframes..."
            delay(400)

            exportProgress = 0.25f
            currentPhase = "Pass 1/3: Mixing multi-track audio & de-noise filters..."
            delay(600)

            exportProgress = 0.55f
            currentPhase = "Pass 2/3: Rendering video canvas frames & shaders..."
            delay(700)

            exportProgress = 0.85f
            currentPhase = "Pass 3/3: Burning Urdu Nastaliq subtitles & karaoke effects..."
            delay(600)

            exportProgress = 1.0f
            currentPhase = "Multiplexing final ${selectedFormat.extension.uppercase()} video container..."
            delay(400)

            isCompleted = true
        }
    }

    Dialog(onDismissRequest = { if (!isExporting) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF2C3247), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!isCompleted) {
                    Text(
                        text = if (isExporting) "Exporting Video..." else "Export Studio",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isExporting) {
                        // Resolution selector
                        Text("Resolution:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExportResolution.values().forEach { res ->
                                val isSel = res == selectedRes
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSel) AccentPurple.copy(alpha = 0.25f) else Color(0xFF1E212D), RoundedCornerShape(8.dp))
                                        .border(1.dp, if (isSel) AccentPurple else Color(0xFF2D3346), RoundedCornerShape(8.dp))
                                        .clickable { selectedRes = res }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(res.label, color = if (isSel) Color(0xFFC084FC) else Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // FPS selector
                        Text("Frame Rate:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExportFps.values().forEach { fps ->
                                val isSel = fps == selectedFps
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSel) AccentCyan.copy(alpha = 0.25f) else Color(0xFF1E212D), RoundedCornerShape(8.dp))
                                        .border(1.dp, if (isSel) AccentCyan else Color(0xFF2D3346), RoundedCornerShape(8.dp))
                                        .clickable { selectedFps = fps }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(fps.label, color = if (isSel) Color(0xFF67E8F9) else Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Format selector
                        Text("Format:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExportFormat.values().forEach { fmt ->
                                val isSel = fmt == selectedFormat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSel) Color(0xFF047857).copy(alpha = 0.3f) else Color(0xFF1E212D), RoundedCornerShape(8.dp))
                                        .border(1.dp, if (isSel) AccentEmerald else Color(0xFF2D3346), RoundedCornerShape(8.dp))
                                        .clickable { selectedFormat = fmt }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(fmt.extension.uppercase(), color = if (isSel) Color(0xFF6EE7B7) else Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Estimated file size calculation
                        val durationSec = (project.totalDurationMs / 1000f).coerceAtLeast(5f)
                        val estMb = "%.1f".format((durationSec * bitrateMbps) / 8f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF141724), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estimated Size:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("~$estMb MB ($bitrateMbps Mbps)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                                Text("Cancel", color = Color(0xFF94A3B8))
                            }
                            Button(
                                onClick = { isExporting = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Start Export", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Exporting progress state
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            color = AccentPurple,
                            trackColor = Color(0xFF1E212D),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentPhase,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    // Export Success State
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = AccentEmerald,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Export Complete!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Edit88_${selectedRes.name}_${selectedFps.fps}fps.${selectedFormat.extension}",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Saved to Gallery", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Reel", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
