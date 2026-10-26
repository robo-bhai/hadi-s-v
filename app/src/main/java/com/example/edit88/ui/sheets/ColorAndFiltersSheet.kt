package com.example.edit88.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.ColorGrading
import com.example.edit88.model.FilterType
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkSurface

@Composable
fun ColorAndFiltersSheet(
    initialFilter: FilterType,
    initialGrading: ColorGrading,
    onApply: (FilterType, ColorGrading) -> Unit,
    onClose: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(initialFilter) }
    var brightness by remember { mutableFloatStateOf(initialGrading.brightness) }
    var contrast by remember { mutableFloatStateOf(initialGrading.contrast) }
    var saturation by remember { mutableFloatStateOf(initialGrading.saturation) }
    var exposure by remember { mutableFloatStateOf(initialGrading.exposure) }
    var temperature by remember { mutableFloatStateOf(initialGrading.temperature) }
    var tint by remember { mutableFloatStateOf(initialGrading.tint) }
    var highlights by remember { mutableFloatStateOf(initialGrading.highlights) }
    var shadows by remember { mutableFloatStateOf(initialGrading.shadows) }
    var vignette by remember { mutableFloatStateOf(initialGrading.vignette) }
    var hue by remember { mutableFloatStateOf(initialGrading.hue) }

    fun currentGrading() = ColorGrading(
        brightness = brightness,
        contrast = contrast,
        saturation = saturation,
        exposure = exposure,
        temperature = temperature,
        tint = tint,
        highlights = highlights,
        shadows = shadows,
        vignette = vignette,
        hue = hue
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Handle bar
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
                Icon(
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = null,
                    tint = AccentPink,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Filters & Color Grading",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = {
                selectedFilter = FilterType.NONE
                brightness = 0f
                contrast = 1f
                saturation = 1f
                exposure = 0f
                temperature = 0f
                tint = 0f
                highlights = 0f
                shadows = 0f
                vignette = 0f
                hue = 0f
                onApply(FilterType.NONE, ColorGrading())
            }) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = Color(0xFF94A3B8))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Preset LUT Filters
        Text("Cinema LUT Presets:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterType.values().forEach { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) AccentPink.copy(alpha = 0.25f) else Color(0xFF1E212D),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.5.dp,
                            if (isSelected) AccentPink else Color(0xFF2E3345),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            selectedFilter = filter
                            onApply(selectedFilter, currentGrading())
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filter.displayName,
                        color = if (isSelected) Color(0xFFF472B6) else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Manual Sliders
        ColorSliderRow(
            label = "Brightness",
            value = brightness,
            valueRange = -0.5f..0.5f,
            formatValue = { "${(it * 100).toInt()}" },
            onValueChange = {
                brightness = it
                onApply(selectedFilter, currentGrading())
            }
        )

        ColorSliderRow(
            label = "Contrast",
            value = contrast,
            valueRange = 0.5f..2.0f,
            formatValue = { "${(it * 100).toInt()}%" },
            onValueChange = {
                contrast = it
                onApply(selectedFilter, currentGrading())
            }
        )

        ColorSliderRow(
            label = "Saturation",
            value = saturation,
            valueRange = 0.0f..2.0f,
            formatValue = { "${(it * 100).toInt()}%" },
            onValueChange = {
                saturation = it
                onApply(selectedFilter, currentGrading())
            }
        )

        ColorSliderRow(
            label = "Exposure",
            value = exposure,
            valueRange = -1.0f..1.0f,
            formatValue = { if (it > 0) "+${(it * 100).toInt()}" else "${(it * 100).toInt()}" },
            onValueChange = {
                exposure = it
                onApply(selectedFilter, currentGrading())
            }
        )

        ColorSliderRow(
            label = "Warmth / Temperature",
            value = temperature,
            valueRange = -1.0f..1.0f,
            formatValue = { if (it > 0) "+${(it * 100).toInt()} (Warm)" else if (it < 0) "${(it * 100).toInt()} (Cool)" else "Neutral" },
            onValueChange = {
                temperature = it
                onApply(selectedFilter, currentGrading())
            }
        )

        ColorSliderRow(
            label = "Tint (Green ↔ Magenta)",
            value = tint,
            valueRange = -1.0f..1.0f,
            formatValue = { if (it > 0) "+${(it * 100).toInt()} (Magenta)" else if (it < 0) "${(it * 100).toInt()} (Green)" else "Neutral" },
            onValueChange = {
                tint = it
                onApply(selectedFilter, currentGrading())
            }
        )

        ColorSliderRow(
            label = "Highlights",
            value = highlights,
            valueRange = -1.0f..1.0f,
            formatValue = { if (it > 0) "+${(it * 100).toInt()}" else "${(it * 100).toInt()}" },
            onValueChange = {
                highlights = it
                onApply(selectedFilter, currentGrading())
            }
        )

        ColorSliderRow(
            label = "Shadows",
            value = shadows,
            valueRange = -1.0f..1.0f,
            formatValue = { if (it > 0) "+${(it * 100).toInt()} (Lifted)" else if (it < 0) "${(it * 100).toInt()} (Crushed)" else "0" },
            onValueChange = {
                shadows = it
                onApply(selectedFilter, currentGrading())
            }
        )

        ColorSliderRow(
            label = "Vignette Shadow",
            value = vignette,
            valueRange = 0.0f..1.0f,
            formatValue = { "${(it * 100).toInt()}%" },
            onValueChange = {
                vignette = it
                onApply(selectedFilter, currentGrading())
            }
        )

        ColorSliderRow(
            label = "Hue Shift",
            value = hue,
            valueRange = -180f..180f,
            formatValue = { "${it.toInt()}°" },
            onValueChange = {
                hue = it
                onApply(selectedFilter, currentGrading())
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply Color Grading", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ColorSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    formatValue: (Float) -> String,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 11.sp)
        Text(text = formatValue(value), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        colors = SliderDefaults.colors(
            thumbColor = AccentPink,
            activeTrackColor = AccentPink
        )
    )
}
