package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TemplateCategory
import com.example.data.model.TemplateRegistry
import com.example.pdf.PdfGenerator
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.CVViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    viewModel: CVViewModel,
    onBackToForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cvData by viewModel.currentCV.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingPdf.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.templateSearchQuery.collectAsStateWithLifecycle()

    val previewBitmap by viewModel.previewBitmap.collectAsStateWithLifecycle()
    val isPreviewLoading by viewModel.isPreviewLoading.collectAsStateWithLifecycle()

    val currentTemplate = cvData.getActiveTemplate()
    val filteredTemplates = viewModel.getFilteredTemplates()

    var showSuccessDialog by remember { mutableStateOf(false) }
    var generatedFileForDialog by remember { mutableStateOf<File?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }
    var isZoomMode by remember { mutableStateOf(false) }

    // Trigger initial preview generation
    LaunchedEffect(cvData.templateId, cvData.customAccentHex) {
        viewModel.refreshPreview(context)
    }

    val accentPresets = listOf(
        0xFF0F5132L to "Emerald",
        0xFF1A365DL to "Sapphire",
        0xFF7B1113L to "Ruby",
        0xFF4A154BL to "Amethyst",
        0xFF006D77L to "Teal",
        0xFF0E7490L to "Petroleum",
        0xFF1F2421L to "Charcoal",
        0xFFB8860BL to "Gold"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("WYSIWYG A4 Preview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${currentTemplate.name} • 100% Identical to PDF", fontSize = 11.sp, color = EmeraldPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToForm) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to form")
                    }
                },
                actions = {
                    IconButton(onClick = { showColorPicker = !showColorPicker }) {
                        Icon(Icons.Default.Palette, contentDescription = "Customize colors", tint = EmeraldPrimary)
                    }
                    IconButton(onClick = onBackToForm) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit data")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.generatePdf(context) { result ->
                                    result.onSuccess { file ->
                                        generatedFileForDialog = file
                                        showSuccessDialog = true
                                    }.onFailure { error ->
                                        Toast.makeText(context, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("download_pdf_button"),
                            enabled = !isGenerating
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating...")
                            } else {
                                Icon(Icons.Default.Download, contentDescription = "Download")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download Exact PDF", fontWeight = FontWeight.Bold)
                            }
                        }

                        // WhatsApp Direct Share
                        Button(
                            onClick = {
                                viewModel.generatePdf(context) { result ->
                                    result.onSuccess { file ->
                                        val generator = PdfGenerator(context)
                                        val shareIntent = generator.createWhatsAppShareIntent(file)
                                        context.startActivity(shareIntent)
                                    }.onFailure { error ->
                                        Toast.makeText(context, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("share_whatsapp_button"),
                            enabled = !isGenerating
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Color Override Palette
            if (showColorPicker) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Customize Primary Color", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                if (cvData.customAccentHex != null) {
                                    Text(
                                        text = "Reset to Default",
                                        fontSize = 11.sp,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.clickable {
                                            viewModel.setCustomAccent(null, context)
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                accentPresets.forEach { (colorHex, _) ->
                                    val isSelected = cvData.customAccentHex == colorHex
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(colorHex))
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                viewModel.setCustomAccent(colorHex, context)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar & Filter Tabs
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setTemplateSearchQuery(it) },
                    placeholder = { Text("Search 44+ templates (e.g. Emerald, Harvard, Tech)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Category Chips
            item {
                ScrollableTabRow(
                    selectedTabIndex = TemplateCategory.values().indexOf(selectedCategory),
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent
                ) {
                    TemplateCategory.values().forEach { cat ->
                        Tab(
                            selected = selectedCategory == cat,
                            onClick = { viewModel.setTemplateCategory(cat) },
                            text = { Text(cat.title, fontSize = 11.sp, fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }

            // Template Carousel Cards
            item {
                Text(
                    text = "${filteredTemplates.size} Templates Available • Tap to Preview Instantly",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredTemplates, key = { it.id }) { template ->
                        val isSelected = cvData.templateId == template.id
                        ElevatedCard(
                            onClick = {
                                viewModel.selectTemplate(template.id, context)
                            },
                            modifier = Modifier
                                .width(150.dp)
                                .testTag("template_card_${template.id}"),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (isSelected) EmeraldContainer else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.elevatedCardElevation(
                                defaultElevation = if (isSelected) 4.dp else 1.dp
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(template.primaryColorHex))
                                    )

                                    if (template.badge != null) {
                                        Surface(
                                            color = EmeraldPrimary,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = template.badge,
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = template.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = template.layoutStyle.name.replace("_", " "),
                                    fontSize = 9.5.sp,
                                    color = if (isSelected) EmeraldPrimary else Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Live Pixel-Perfect WYSIWYG A4 Preview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Exact A4 Output Preview",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Rendered with real PDF vector canvas",
                            fontSize = 10.5.sp,
                            color = Color.Gray
                        )
                    }

                    // Mode Toggle: Fit to screen vs Zoom
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { isZoomMode = !isZoomMode }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isZoomMode) Icons.Default.FitScreen else Icons.Default.ZoomIn,
                                contentDescription = "Toggle Zoom",
                                modifier = Modifier.size(16.dp),
                                tint = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isZoomMode) "Fit Page" else "Zoom 100%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                // The Paper Sheet Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewBitmap != null) {
                        if (isZoomMode) {
                            // Zoom & Read Mode: horizontal + vertical scrollable at native full crispness
                            val hScrollState = rememberScrollState()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(hScrollState)
                            ) {
                                Image(
                                    bitmap = previewBitmap!!.asImageBitmap(),
                                    contentDescription = "A4 Page Full Detail",
                                    modifier = Modifier
                                        .width(595.dp)
                                        .height(842.dp),
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                        } else {
                            // Fit Page Mode: standard A4 proportion 595:842
                            Image(
                                bitmap = previewBitmap!!.asImageBitmap(),
                                contentDescription = "A4 Page Fitted Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(595f / 842f),
                                    contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        // Loading skeleton with standard A4 ratio
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(595f / 842f)
                                .background(Color(0xFFFAFAFA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = EmeraldPrimary, strokeWidth = 2.5.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Rendering exact A4 layout...", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }

                    if (isPreviewLoading) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.9f),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(18.dp),
                                color = EmeraldPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Success Dialog after PDF Generation
    if (showSuccessDialog && generatedFileForDialog != null) {
        val file = generatedFileForDialog!!
        val generator = PdfGenerator(context)
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Exact A4 PDF Generated!", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Column {
                    Text(
                        text = "Your resume has been saved with the exact design, spacing, and styling you previewed on screen.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = file.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        val shareIntent = generator.createShareIntent(file)
                        context.startActivity(Intent.createChooser(shareIntent, "Share CV via..."))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share PDF")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSuccessDialog = false
                        val viewIntent = generator.createViewIntent(file)
                        try {
                            context.startActivity(viewIntent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "No PDF viewer app found", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = "Open", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open")
                }
            }
        )
    }
}
