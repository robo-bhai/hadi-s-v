package com.example.edit88.ui.sheets

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.UrduFontChoice
import com.example.edit88.typography.StoreFontItem
import com.example.edit88.typography.UrduFontManager
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun FontStoreSheet(
    fontManager: UrduFontManager,
    selectedFontChoice: UrduFontChoice,
    onSelectFont: (UrduFontChoice, String?) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val fontStatus by fontManager.fontStatus.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var previewText by remember { mutableStateOf("خوابوں کو حقیقت کا روپ دیں • Viral Reel") }
    var importMessage by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    // Launcher for Custom .ttf / .otf Font Files
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isImporting = true
            var displayName: String? = null
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            displayName = cursor.getString(nameIndex)
                        }
                    }
                }
            } catch (_: Exception) {}

            val imported = fontManager.importCustomFont(uri, displayName)
            isImporting = false
            if (imported != null) {
                importMessage = "✓ '${imported.nameEnglish}' kamyabi se import ho gaya!"
                onSelectFont(UrduFontChoice.CUSTOM_IMPORTED, imported.localFilePath)
            } else {
                importMessage = "Font file read nahi ho saki. Please valid .ttf ya .otf select karein."
            }
        }
    }

    val categories = listOf("All", "Urdu Nastaliq", "Calligraphy", "Modern Sans", "Bold Impact", "Custom Imported")

    val filteredFonts = fontStatus.storeFonts.filter { item ->
        val matchesCategory = (selectedCategory == "All") || (item.category.contains(selectedCategory, true))
        val matchesSearch = searchQuery.isBlank() ||
                item.nameEnglish.contains(searchQuery, true) ||
                item.nameUrdu.contains(searchQuery, true)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(16.dp)
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .size(40.dp, 4.dp)
                .background(Color(0xFF334155), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Header with Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FontDownload,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Font Store & Asset Library",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${fontStatus.fontsReadyCount} fonts ready offline",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Font Upload Button
        Button(
            onClick = {
                fontPickerLauncher.launch("*/*")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (isImporting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importing Font...", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("➕ Add / Upload Custom Font (.ttf / .otf)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (importMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF064E3B), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = importMessage ?: "",
                    color = Color(0xFFA7F3D0),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Urdu Nastaliq, Sans, Impact...", color = Color(0xFF64748B), fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = Color(0xFF262C3E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                        selectedLabelColor = AccentCyan,
                        containerColor = Color(0xFF1E212D),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) AccentCyan else Color(0xFF2E3345)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Font List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredFonts, key = { it.id }) { fontItem ->
                val fontFamily = fontManager.resolveFontFamily(fontItem.fontChoice, fontItem.localFilePath)
                val isCurrentlySelected = (selectedFontChoice == fontItem.fontChoice && !fontItem.isCustom) ||
                        (fontItem.isCustom && fontItem.localFilePath != null)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isCurrentlySelected) Color(0xFF1E293B) else DarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .border(
                            width = if (isCurrentlySelected) 1.5.dp else 0.5.dp,
                            color = if (isCurrentlySelected) AccentCyan else Color(0xFF262C3E),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            onSelectFont(fontItem.fontChoice, fontItem.localFilePath)
                        }
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = fontItem.nameEnglish,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF334155), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(fontItem.category, color = Color(0xFFCBD5E1), fontSize = 9.sp)
                                    }
                                }
                                Text(
                                    text = fontItem.nameUrdu,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }

                            // Download / Installed Badge
                            if (fontItem.isInstalled) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF065F46), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF6EE7B7), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ready", color = Color(0xFFD1FAE5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { fontManager.downloadStoreFont(fontItem.id) },
                                    modifier = Modifier.height(30.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Live Preview Rendered in this font
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0A0D14), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = fontItem.sampleUrdu,
                                fontFamily = fontFamily,
                                color = if (isCurrentlySelected) AccentCyan else Color(0xFFF1F5F9),
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
