package com.example.edit88.ui.sheets

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.SubtitleClip
import com.example.edit88.model.toSrtString
import com.example.edit88.model.toVttString
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun SrtExportSheet(
    subtitles: List<SubtitleClip>,
    onClose: () -> Unit
) {
    var format by remember { mutableStateOf("SRT") }
    val context = LocalContext.current

    val rawContent = if (format == "SRT") subtitles.toSrtString() else subtitles.toVttString()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(20.dp)
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
                Icon(Icons.Default.Subtitles, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Subtitles", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = format == "SRT",
                    onClick = { format = "SRT" },
                    label = { Text(".SRT", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentAmber,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkSurfaceVariant,
                        labelColor = Color(0xFF94A3B8)
                    )
                )
                FilterChip(
                    selected = format == "VTT",
                    onClick = { format = "VTT" },
                    label = { Text(".VTT", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentAmber,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkSurfaceVariant,
                        labelColor = Color(0xFF94A3B8)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "${subtitles.size} subtitle cues • Precision millisecond timecodes",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Raw Text Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF0F1118), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF232736), RoundedCornerShape(8.dp))
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (rawContent.isNotBlank()) rawContent else "No subtitle clips found on timeline.",
                color = Color(0xFFE2E8F0),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Subtitles", rawContent)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "$format subtitles copied to clipboard!", Toast.LENGTH_SHORT).show()
                onClose()
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copy $format to Clipboard", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
