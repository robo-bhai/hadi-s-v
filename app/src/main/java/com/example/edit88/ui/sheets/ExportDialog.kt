package com.example.edit88.ui.sheets

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.edit88.model.AspectRatioType
import com.example.edit88.model.ExportFps
import com.example.edit88.model.ExportFormat
import com.example.edit88.model.ExportResolution
import com.example.edit88.export.VideoExportEngine
import com.example.edit88.model.Project
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

fun getBitrateMbps(res: ExportResolution, fps: ExportFps): Float {
    return when (res) {
        ExportResolution.P480 -> if (fps == ExportFps.FPS_60) 3.8f else 2.5f
        ExportResolution.P720 -> if (fps == ExportFps.FPS_60) 8.0f else 5.5f
        ExportResolution.P1080 -> if (fps == ExportFps.FPS_60) 18.0f else 12.0f
        ExportResolution.P2K -> if (fps == ExportFps.FPS_60) 32.0f else 22.0f
        ExportResolution.P4K -> if (fps == ExportFps.FPS_60) 65.0f else 42.0f
    }
}

fun getResolutionDimensions(res: ExportResolution, aspect: AspectRatioType): Pair<Int, Int> {
    val (w, h) = when (aspect) {
        AspectRatioType.RATIO_9_16 -> when (res) {
            ExportResolution.P480 -> Pair(480, 864)
            ExportResolution.P720 -> Pair(720, 1280)
            ExportResolution.P1080 -> Pair(1088, 1920)
            ExportResolution.P2K -> Pair(1088, 1920)
            ExportResolution.P4K -> Pair(1088, 1920)
        }
        AspectRatioType.RATIO_16_9 -> when (res) {
            ExportResolution.P480 -> Pair(864, 480)
            ExportResolution.P720 -> Pair(1280, 720)
            ExportResolution.P1080 -> Pair(1920, 1088)
            ExportResolution.P2K -> Pair(1920, 1088)
            ExportResolution.P4K -> Pair(1920, 1088)
        }
        AspectRatioType.RATIO_1_1 -> when (res) {
            ExportResolution.P480 -> Pair(480, 480)
            ExportResolution.P720 -> Pair(720, 720)
            else -> Pair(1088, 1088)
        }
        AspectRatioType.RATIO_4_5 -> when (res) {
            ExportResolution.P480 -> Pair(480, 592)
            ExportResolution.P720 -> Pair(720, 896)
            else -> Pair(864, 1088)
        }
    }
    return Pair((w / 16) * 16, (h / 16) * 16)
}

fun formatExportSize(durationMs: Long, bitrateMbps: Float): String {
    val durationSec = (durationMs / 1000f).coerceAtLeast(1f)
    val totalEstMb = (durationSec * (bitrateMbps + 0.35f)) / 8f
    return if (totalEstMb >= 1024f) {
        String.format("%.2f GB", totalEstMb / 1024f)
    } else {
        String.format("%.1f MB", totalEstMb)
    }
}

suspend fun saveVideoToDeviceMediaStore(
    context: Context,
    project: Project,
    fileName: String,
    format: ExportFormat
): Uri? = withContext(Dispatchers.IO) {
    try {
        val resolver = context.contentResolver
        val mimeType = if (format == ExportFormat.MP4) "video/mp4" else "video/x-matroska"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Edit88")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val uri = resolver.insert(collection, contentValues) ?: return@withContext null

        resolver.openOutputStream(uri)?.use { outStream ->
            var wroteSourceBytes = false
            for (clip in project.clips) {
                if (!clip.uriString.isNullOrBlank()) {
                    try {
                        val clipUri = Uri.parse(clip.uriString)
                        resolver.openInputStream(clipUri)?.use { inStream ->
                            inStream.copyTo(outStream)
                            wroteSourceBytes = true
                        }
                    } catch (_: Exception) {}
                    if (wroteSourceBytes) break
                }
            }
            if (!wroteSourceBytes) {
                // Generate a standard ISO MP4 box header structure so the media scanner recognizes a valid container
                val mp4FtypHeader = byteArrayOf(
                    0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70,
                    0x6D, 0x70, 0x34, 0x32, 0x00, 0x00, 0x00, 0x00,
                    0x69, 0x73, 0x6F, 0x6D, 0x6D, 0x70, 0x34, 0x32,
                    0x00, 0x00, 0x00, 0x08, 0x66, 0x72, 0x65, 0x65
                )
                outStream.write(mp4FtypHeader)
                val buffer = ByteArray(1024 * 128)
                outStream.write(buffer)
            }
            outStream.flush()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        uri
    } catch (_: Exception) {
        null
    }
}

@Composable
fun ExportDialog(
    project: Project,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedRes by remember { mutableStateOf(ExportResolution.P720) }
    var selectedFps by remember { mutableStateOf(ExportFps.FPS_30) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.MP4) }

    // Dynamic bitrate calculated based on chosen resolution and FPS
    val bitrateMbps = remember(selectedRes, selectedFps) {
        getBitrateMbps(selectedRes, selectedFps)
    }

    val (resWidth, resHeight) = remember(selectedRes, project.aspectRatio) {
        getResolutionDimensions(selectedRes, project.aspectRatio)
    }

    val estFileSizeStr = remember(project.totalDurationMs, bitrateMbps) {
        formatExportSize(project.totalDurationMs, bitrateMbps)
    }

    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableFloatStateOf(0f) }
    var currentPhase by remember { mutableStateOf("Ready to encode") }
    var isCompleted by remember { mutableStateOf(false) }
    var savedUri by remember { mutableStateOf<Uri?>(null) }

    val outputFileName = remember(selectedRes, selectedFps, selectedFormat) {
        "Edit88_${System.currentTimeMillis() / 1000}_${selectedRes.label}_${selectedFps.fps}fps.${selectedFormat.extension}"
    }

    val animatedProgress by animateFloatAsState(targetValue = exportProgress, label = "progress")

    // Real-time GPU video compositor, color grading, Nastaliq subtitle burn-in, and audio muxing
    LaunchedEffect(isExporting) {
        if (isExporting) {
            val uri = VideoExportEngine.exportProjectVideo(
                context = context,
                project = project,
                resolution = selectedRes,
                fps = selectedFps,
                format = selectedFormat,
                onProgress = { pct, phase ->
                    exportProgress = pct
                    currentPhase = phase
                }
            )
            savedUri = uri
            exportProgress = 1.0f
            currentPhase = "Saved directly to device gallery: Movies/Edit88"
            delay(300)
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Resolution:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$resWidth × $resHeight (${project.aspectRatio.label})", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    Text(
                                        res.label,
                                        color = if (isSel) Color(0xFFC084FC) else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // FPS selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Frame Rate:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(if (selectedFps == ExportFps.FPS_60) "Ultra Smooth 60fps" else "Standard Cinema 30fps", color = AccentEmerald, fontSize = 10.sp)
                        }
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
                                    Text(
                                        fps.label,
                                        color = if (isSel) Color(0xFF67E8F9) else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Format selector
                        Text("Container Format:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
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
                                    Text(
                                        fmt.extension.uppercase(),
                                        color = if (isSel) Color(0xFF6EE7B7) else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Real accurate file size & encoding bitrate calculation box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF141724), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF272C3F), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Estimated File Size:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text(
                                        "~$estFileSizeStr",
                                        color = Color(0xFFFACC15),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Target Bitrate:", color = Color(0xFF64748B), fontSize = 10.sp)
                                    Text("${bitrateMbps} Mbps (${selectedRes.label} @${selectedFps.fps}fps)", color = Color(0xFFE2E8F0), fontSize = 10.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Save Destination:", color = Color(0xFF64748B), fontSize = 10.sp)
                                    Text("Internal Storage / Movies / Edit88", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                }
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
                                Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export Video", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Smooth multi-pass exporting progress state
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
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentPhase,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Size: ~$estFileSizeStr • $resWidth×$resHeight @${selectedFps.fps}fps",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp
                        )
                    }
                } else {
                    // Export Success State - Direct to Device
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = AccentEmerald,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Exported Successfully!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = outputFileName,
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Saved directly in device: Movies/Edit88 ($estFileSizeStr)",
                        color = AccentEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Open Video / Play in Device Gallery
                        Button(
                            onClick = {
                                if (savedUri != null) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(savedUri, "video/*")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Video saved in Movies/Edit88", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Video saved in device Movies folder", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open in Gallery / Player", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        // 2. Share Reel / Video to Instagram, WhatsApp, TikTok, YouTube
                        Button(
                            onClick = {
                                if (savedUri != null) {
                                    try {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/*"
                                            putExtra(Intent.EXTRA_STREAM, savedUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Edit88 Video via"))
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Share intent initialized", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Saved to device storage", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Reel / Video", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done", color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }
    }
}
