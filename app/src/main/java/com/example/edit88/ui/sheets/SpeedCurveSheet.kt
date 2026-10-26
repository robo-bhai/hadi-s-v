package com.example.edit88.ui.sheets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.SpeedCurvePreset
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun SpeedCurveSheet(
    initialSpeed: Float,
    initialCurve: SpeedCurvePreset,
    onApplySpeed: (Float, SpeedCurvePreset) -> Unit,
    onClose: () -> Unit
) {
    var speed by remember { mutableFloatStateOf(initialSpeed) }
    var selectedCurve by remember { mutableStateOf(initialCurve) }
    var pitchCorrection by remember { mutableStateOf(true) }
    var mode by remember { mutableStateOf("Curve") } // "Normal" or "Curve"

    val speedChips = listOf(0.2f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f, 5.0f, 10.0f)

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
                Icon(Icons.Default.Speed, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Speed & Curve Engine", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // Mode switch: Normal vs Curve
            Row(
                modifier = Modifier
                    .background(Color(0xFF1E212D), RoundedCornerShape(20.dp))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (mode == "Curve") AccentPurple else Color.Transparent)
                        .clickable { mode = "Curve" }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Curve", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (mode == "Normal") AccentPurple else Color.Transparent)
                        .clickable { mode = "Normal" }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Normal", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (mode == "Curve") {
            // Speed Curve Interactive Visual Graph
            Text("Speed Curve Preset: ${selectedCurve.displayName}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(selectedCurve.description, color = Color(0xFF94A3B8), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF0F1118), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF262C3D), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val points = selectedCurve.points

                    // Draw grid line (1.0x baseline)
                    val baseLineY = h * 0.7f
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(0f, baseLineY),
                        end = Offset(w, baseLineY),
                        strokeWidth = 1.5f
                    )

                    // Draw curve line
                    val path = Path()
                    val pointOffsets = mutableListOf<Offset>()
                    val stepX = w / (points.size - 1)

                    points.forEachIndexed { i, p ->
                        val px = i * stepX
                        // Normalize 0.2x to 3.5x
                        val normalized = (p - 0.2f) / (3.5f - 0.2f)
                        val py = h - (normalized * (h * 0.85f))
                        val offset = Offset(px, py)
                        pointOffsets.add(offset)
                        if (i == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
                    }

                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(listOf(Color(0xFF06B6D4), Color(0xFFA855F7))),
                        style = Stroke(width = 4f)
                    )

                    // Draw keyframe nodes
                    pointOffsets.forEach { pt ->
                        drawCircle(color = Color(0xFF06B6D4), radius = 6f, center = pt)
                        drawCircle(color = Color.White, radius = 3f, center = pt)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Curve Presets Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpeedCurvePreset.values().forEach { preset ->
                    val isSel = preset == selectedCurve
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedCurve = preset },
                        label = { Text(preset.displayName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkSurfaceVariant,
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }
        } else {
            // Normal Linear Speed Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Linear Speed", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("${"%.2f".format(speed)}x", color = Color(0xFFFDE047), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Slider(
                value = speed,
                onValueChange = { speed = it },
                valueRange = 0.1f..10.0f,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                speedChips.forEach { spd ->
                    val isSel = speed == spd
                    Box(
                        modifier = Modifier
                            .background(if (isSel) AccentCyan else Color(0xFF1E212D), RoundedCornerShape(8.dp))
                            .clickable { speed = spd }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("${spd}x", color = if (isSel) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pitch Correction Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Pitch Correction", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Prevents chipmunk audio effect when speeding up", color = Color(0xFF94A3B8), fontSize = 10.sp)
            }
            Switch(
                checked = pitchCorrection,
                onCheckedChange = { pitchCorrection = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentCyan
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onApplySpeed(if (mode == "Normal") speed else 1.0f, selectedCurve)
                onClose()
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply Speed Settings", fontWeight = FontWeight.Bold)
        }
    }
}
