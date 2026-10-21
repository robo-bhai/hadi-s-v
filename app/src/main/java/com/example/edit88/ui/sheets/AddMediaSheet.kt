package com.example.edit88.ui.sheets

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.MediaClip
import com.example.edit88.model.MediaType
import com.example.edit88.util.MediaImportHelper
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun AddMediaSheet(
    onAddClip: (MediaClip) -> Unit,
    onAddClips: ((List<MediaClip>) -> Unit)? = null,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    // Modern Android Photo/Video Picker for Multiple Clips
    val multipleMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val clips = uris.map { uri ->
                MediaImportHelper.createMediaClipFromUri(context, uri)
            }
            if (onAddClips != null) {
                onAddClips(clips)
            } else {
                clips.forEach { onAddClip(it) }
            }
            onClose()
        }
    }

    // Modern Android Photo/Video Picker for a Single Clip
    val singleMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val clip = MediaImportHelper.createMediaClipFromUri(context, uri)
            onAddClip(clip)
            onClose()
        }
    }

    val stockScenes = listOf(
        StockSceneItem("Neon Cyber City", "Deep neon violet & cyan aerial motion", 0xFF2563EB, 0xFF8B5CF6, "Neon City"),
        StockSceneItem("Mountain Sunrise", "Golden warm hour mountain clouds", 0xFFB45309, 0xFFF59E0B, "Mountain Sunrise"),
        StockSceneItem("Cyberpunk Glitch", "High tech neon glitch grid", 0xFF059669, 0xFF06B6D4, "Cyberpunk Glitch"),
        StockSceneItem("Tech Studio", "Minimal clean creator studio lighting", 0xFF1E293B, 0xFF334155, "Tech Studio"),
        StockSceneItem("Crimson Sunset", "Dramatic red sunset silhouette", 0xFF7F1D1D, 0xFFEF4444, "Sunset Ocean")
    )

    var durationSec by remember { mutableIntStateOf(5) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp, 4.dp)
                .background(Color(0xFF334155), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Add Media to Timeline", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("Import from phone storage or pick stock clips", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Device Storage Video Import Hero Card (High Priority)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF1E1435), Color(0xFF0F1E36))
                    )
                )
                .border(
                    1.5.dp,
                    Brush.linearGradient(listOf(AccentPurple, AccentCyan)),
                    RoundedCornerShape(14.dp)
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AccentCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Local Device Storage",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Gallery, Downloads & Camera Videos",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(AccentEmerald.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("CapCut Style", color = AccentEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Import Multiple Clips Button
                    Button(
                        onClick = {
                            multipleMediaLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Default.PermMedia, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Multiple Clips", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Import Single Video Button
                    OutlinedButton(
                        onClick = {
                            singleMediaLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(
                            1.dp,
                            Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF64748B)))
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Single Video", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stock Visual Presets section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Stock Scenes / Color Backgrounds", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

            // Duration selector for stock clips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(3, 5, 8, 10).forEach { sec ->
                    val isSel = sec == durationSec
                    Box(
                        modifier = Modifier
                            .background(if (isSel) AccentPurple else Color(0xFF1E212D), RoundedCornerShape(6.dp))
                            .clickable { durationSec = sec }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${sec}s", color = if (isSel) Color.White else Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.height(180.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stockScenes) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF181B26), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF262C3D), RoundedCornerShape(10.dp))
                        .clickable {
                            val clip = MediaClip(
                                title = item.title,
                                type = MediaType.VIDEO,
                                durationMs = durationSec * 1000L,
                                endTrimMs = durationSec * 1000L,
                                visualTheme = item.theme,
                                primaryColorHex = item.color1,
                                secondaryColorHex = item.color2
                            )
                            onAddClip(clip)
                            onClose()
                        }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Brush.linearGradient(listOf(Color(item.color1), Color(item.color2))),
                                    RoundedCornerShape(6.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(item.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(item.desc, color = Color(0xFF94A3B8), fontSize = 10.sp)
                        }
                    }

                    Icon(Icons.Default.Add, contentDescription = "Add", tint = AccentCyan)
                }
            }
        }
    }
}

private data class StockSceneItem(
    val title: String,
    val desc: String,
    val color1: Long,
    val color2: Long,
    val theme: String
)
