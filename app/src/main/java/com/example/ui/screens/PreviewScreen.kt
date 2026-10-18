package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.CVData
import com.example.data.model.FontStyleType
import com.example.data.model.LayoutStyle
import com.example.data.model.TemplateCategory
import com.example.data.model.TemplateInfo
import com.example.data.model.TemplateRegistry
import com.example.pdf.PdfGenerator
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldTertiary
import com.example.ui.viewmodel.CVViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    val currentTemplate = cvData.getActiveTemplate()
    val filteredTemplates = viewModel.getFilteredTemplates()

    var showSuccessDialog by remember { mutableStateOf(false) }
    var generatedFileForDialog by remember { mutableStateOf<File?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }

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
                        Text("Template Gallery & Preview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${TemplateRegistry.templates.size} World-Class Designs", fontSize = 11.sp, color = Color.Gray)
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
                                Text("Save A4 PDF", fontWeight = FontWeight.Bold)
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
            // Optional Color Override Palette
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
                                        modifier = Modifier.clickable { viewModel.setCustomAccent(null) }
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
                                            .clickable { viewModel.setCustomAccent(colorHex) },
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
                    placeholder = { Text("Search 44+ templates (e.g. Emerald, Harvard, Dev, PPSC)...") },
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
                    text = "${filteredTemplates.size} Templates Available • Tap to Preview",
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
                            onClick = { viewModel.selectTemplate(template.id) },
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

            // Real-Time Scaled Document Preview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live A4 Preview: ${currentTemplate.name}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        color = EmeraldContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "A4 Page (595x842)",
                            color = EmeraldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    when (currentTemplate.layoutStyle) {
                        LayoutStyle.SIDEBAR_LEFT -> UniversalSidebarPreview(cv = cvData, template = currentTemplate, isSidebarLeft = true)
                        LayoutStyle.SIDEBAR_RIGHT -> UniversalSidebarPreview(cv = cvData, template = currentTemplate, isSidebarLeft = false)
                        LayoutStyle.HEADER_BANNER -> UniversalBannerPreview(cv = cvData, template = currentTemplate)
                        LayoutStyle.CLASSIC_CENTERED -> UniversalClassicPreview(cv = cvData, template = currentTemplate)
                        LayoutStyle.TWO_COLUMN_GRID -> UniversalGridPreview(cv = cvData, template = currentTemplate)
                        LayoutStyle.TIMELINE_ACCENT -> UniversalTimelinePreview(cv = cvData, template = currentTemplate)
                        LayoutStyle.MINIMAL_BORDER -> UniversalMinimalBorderPreview(cv = cvData, template = currentTemplate)
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
                Text("A4 PDF Generated Successfully!", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Column {
                    Text(
                        text = "Your resume has been saved using template \"${currentTemplate.name}\" and is ready to share on WhatsApp or print.",
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

// =========================================================================
// REAL-TIME PREVIEW RENDERERS FOR ALL LAYOUT STYLES
// =========================================================================

@Composable
fun UniversalSidebarPreview(cv: CVData, template: TemplateInfo, isSidebarLeft: Boolean) {
    val primaryColor = Color(cv.customAccentHex ?: template.primaryColorHex)
    val accentColor = Color(template.accentColorHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
    ) {
        val sidebarContent = @Composable {
            Column(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxSize()
                    .background(primaryColor)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (cv.photoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(cv.photoUri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cv.fullName.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("CONTACT", color = accentColor, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(3.dp))
                if (cv.phone.isNotBlank()) Text(cv.phone, color = Color.White, fontSize = 6.5.sp)
                if (cv.email.isNotBlank()) Text(cv.email, color = Color.White, fontSize = 6.5.sp, maxLines = 1)
                if (cv.city.isNotBlank()) Text(cv.city, color = Color.White, fontSize = 6.5.sp)
                if (cv.cnic.isNotBlank()) Text(cv.cnic, color = Color.White, fontSize = 6.sp)

                Spacer(modifier = Modifier.height(8.dp))
                if (cv.skills.isNotEmpty()) {
                    Text("SKILLS", color = accentColor, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(3.dp))
                    cv.skills.take(5).forEach { skill ->
                        Text("• $skill", color = Color.White, fontSize = 6.5.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (cv.languages.isNotEmpty()) {
                    Text("LANGUAGES", color = accentColor, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(3.dp))
                    cv.languages.take(3).forEach { lang ->
                        Text("• $lang", color = Color.White, fontSize = 6.5.sp, maxLines = 1)
                    }
                }
            }
        }

        val mainContent = @Composable {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Text(
                    text = cv.fullName.ifBlank { "Candidate Name" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = cv.jobTitle.ifBlank { "Target Title" }.uppercase(),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(accentColor)
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (cv.objective.isNotBlank()) {
                    Text("SUMMARY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(cv.objective, fontSize = 7.sp, color = Color.DarkGray, maxLines = 3)
                    Spacer(modifier = Modifier.height(5.dp))
                }

                if (!cv.isFreshGraduate && cv.experiences.any { it.jobTitle.isNotBlank() }) {
                    Text("EXPERIENCE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(2.dp))
                    cv.experiences.take(2).forEach { exp ->
                        if (exp.jobTitle.isNotBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(exp.jobTitle, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                Text(exp.duration, fontSize = 6.5.sp, color = Color.Gray)
                            }
                            Text(exp.company, fontSize = 7.sp, color = primaryColor)
                            if (exp.description.isNotBlank()) {
                                Text(exp.description, fontSize = 6.5.sp, color = Color.DarkGray, maxLines = 2)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }

                if (cv.projects.isNotEmpty()) {
                    Text("PROJECTS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(2.dp))
                    cv.projects.take(2).forEach { proj ->
                        Text("${proj.title} (${proj.techStack})", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        if (proj.description.isNotBlank()) {
                            Text(proj.description, fontSize = 6.5.sp, color = Color.DarkGray, maxLines = 1)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }

                if (cv.educations.any { it.degree.isNotBlank() }) {
                    Text("EDUCATION", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(2.dp))
                    cv.educations.take(2).forEach { edu ->
                        if (edu.degree.isNotBlank()) {
                            Text("${edu.degree} - ${edu.institute} (${edu.passingYear})", fontSize = 7.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
        }

        if (isSidebarLeft) {
            sidebarContent()
            mainContent()
        } else {
            mainContent()
            sidebarContent()
        }
    }
}

@Composable
fun UniversalBannerPreview(cv: CVData, template: TemplateInfo) {
    val primaryColor = Color(cv.customAccentHex ?: template.primaryColorHex)
    val accentColor = Color(template.accentColorHex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
    ) {
        // Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryColor)
                .padding(10.dp)
        ) {
            Column {
                Text(cv.fullName.ifBlank { "Full Name" }, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(cv.jobTitle.ifBlank { "Professional" }, fontSize = 8.5.sp, color = Color.White.copy(alpha = 0.85f))
                val contacts = listOfNotNull(cv.phone, cv.email, cv.city).filter { it.isNotBlank() }.joinToString(" • ")
                Text(contacts, fontSize = 7.sp, color = Color.White.copy(alpha = 0.75f))
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(2.5.dp).background(accentColor))

        Column(modifier = Modifier.padding(10.dp)) {
            if (cv.objective.isNotBlank()) {
                Text("SUMMARY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                Text(cv.objective, fontSize = 7.sp, color = Color.DarkGray, maxLines = 3)
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (!cv.isFreshGraduate && cv.experiences.any { it.jobTitle.isNotBlank() }) {
                Text("EXPERIENCE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                cv.experiences.take(2).forEach { exp ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(exp.jobTitle, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                        Text(exp.duration, fontSize = 6.5.sp, color = Color.Gray)
                    }
                    Text(exp.company, fontSize = 7.sp, color = primaryColor)
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }

            if (cv.educations.any { it.degree.isNotBlank() }) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("EDUCATION", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                cv.educations.take(2).forEach { edu ->
                    Text("${edu.degree} — ${edu.institute} (${edu.passingYear})", fontSize = 7.sp)
                }
            }

            if (cv.skills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("SKILLS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                Text(cv.skills.joinToString("  •  "), fontSize = 7.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun UniversalClassicPreview(cv: CVData, template: TemplateInfo) {
    val primaryColor = Color(cv.customAccentHex ?: template.primaryColorHex)
    val accentColor = Color(template.accentColorHex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = cv.fullName.ifBlank { "Full Name" }.uppercase(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = primaryColor
            )
            Text(
                text = cv.jobTitle.ifBlank { "Curriculum Vitae" },
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
            val contacts = listOfNotNull(cv.phone, cv.email, cv.city, cv.cnic).filter { it.isNotBlank() }.joinToString(" | ")
            Text(contacts, fontSize = 7.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(3.dp))
            HorizontalDivider(color = primaryColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(1.5.dp))
            HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (cv.objective.isNotBlank()) {
            Text("EXECUTIVE SUMMARY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            HorizontalDivider(color = accentColor, thickness = 0.8.dp)
            Text(cv.objective, fontSize = 7.sp, color = Color.DarkGray, maxLines = 3)
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (!cv.isFreshGraduate && cv.experiences.any { it.jobTitle.isNotBlank() }) {
            Text("EXPERIENCE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            HorizontalDivider(color = accentColor, thickness = 0.8.dp)
            cv.experiences.take(2).forEach { exp ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(exp.jobTitle, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                    Text(exp.duration, fontSize = 6.5.sp, color = Color.Gray)
                }
                Text(exp.company, fontSize = 7.sp, color = primaryColor)
                Spacer(modifier = Modifier.height(3.dp))
            }
        }

        if (cv.educations.any { it.degree.isNotBlank() }) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("EDUCATION", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            HorizontalDivider(color = accentColor, thickness = 0.8.dp)
            cv.educations.take(2).forEach { edu ->
                Text("${edu.degree} — ${edu.institute} (${edu.passingYear})", fontSize = 7.sp)
            }
        }

        if (cv.skills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text("CORE COMPETENCIES", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            HorizontalDivider(color = accentColor, thickness = 0.8.dp)
            Text(cv.skills.joinToString("  •  "), fontSize = 7.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun UniversalGridPreview(cv: CVData, template: TemplateInfo) {
    val primaryColor = Color(cv.customAccentHex ?: template.primaryColorHex)
    val accentColor = Color(template.accentColorHex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(cv.fullName.ifBlank { "Full Name" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                Text(cv.jobTitle.ifBlank { "Professional" }, fontSize = 8.5.sp, color = accentColor, fontWeight = FontWeight.Bold)
            }
            Text(cv.phone + "\n" + cv.email, fontSize = 6.5.sp, color = Color.Gray, textAlign = TextAlign.Right)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFD0D0D0))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(0.58f)) {
                if (cv.objective.isNotBlank()) {
                    Text("SUMMARY", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    Text(cv.objective, fontSize = 6.5.sp, color = Color.DarkGray, maxLines = 4)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                if (!cv.isFreshGraduate && cv.experiences.any { it.jobTitle.isNotBlank() }) {
                    Text("EXPERIENCE", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    cv.experiences.take(2).forEach { exp ->
                        Text(exp.jobTitle, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        Text("${exp.company} (${exp.duration})", fontSize = 6.5.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
            }

            Column(modifier = Modifier.weight(0.42f)) {
                if (cv.educations.any { it.degree.isNotBlank() }) {
                    Text("EDUCATION", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    cv.educations.take(2).forEach { edu ->
                        Text("${edu.degree} - ${edu.institute}", fontSize = 6.5.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                if (cv.skills.isNotEmpty()) {
                    Text("SKILLS", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    cv.skills.take(5).forEach { skill ->
                        Text("▪ $skill", fontSize = 6.5.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
fun UniversalTimelinePreview(cv: CVData, template: TemplateInfo) {
    val primaryColor = Color(cv.customAccentHex ?: template.primaryColorHex)
    val accentColor = Color(template.accentColorHex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .padding(10.dp)
    ) {
        Text(cv.fullName.ifBlank { "Full Name" }, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = primaryColor)
        Text(cv.jobTitle.ifBlank { "Developer" }, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accentColor)
        val contacts = listOfNotNull(cv.phone, cv.email, cv.city).filter { it.isNotBlank() }.joinToString(" • ")
        Text(contacts, fontSize = 7.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(6.dp))

        if (cv.objective.isNotBlank()) {
            Text("PROFILE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Text(cv.objective, fontSize = 7.sp, color = Color.DarkGray, maxLines = 3)
            Spacer(modifier = Modifier.height(6.dp))
        }

        Text("TIMELINE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = primaryColor)
        HorizontalDivider(color = accentColor, thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(4.dp))

        if (!cv.isFreshGraduate && cv.experiences.any { it.jobTitle.isNotBlank() }) {
            cv.experiences.take(3).forEach { exp ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(accentColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${exp.jobTitle} @ ${exp.company} (${exp.duration})", fontSize = 7.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(3.dp))
            }
        }

        if (cv.educations.any { it.degree.isNotBlank() }) {
            cv.educations.take(2).forEach { edu ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(primaryColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${edu.degree} - ${edu.institute} (${edu.passingYear})", fontSize = 7.sp)
                }
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}

@Composable
fun UniversalMinimalBorderPreview(cv: CVData, template: TemplateInfo) {
    val primaryColor = Color(cv.customAccentHex ?: template.primaryColorHex)
    val accentColor = Color(template.accentColorHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(4.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(cv.fullName.ifBlank { "Full Name" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Text(cv.jobTitle.ifBlank { "Candidate" }, fontSize = 8.5.sp, color = accentColor)
            val contacts = listOfNotNull(cv.phone, cv.email, cv.city).filter { it.isNotBlank() }.joinToString(" | ")
            Text(contacts, fontSize = 7.sp, color = Color.Gray)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFE0E0E0))

            if (cv.objective.isNotBlank()) {
                Text("SUMMARY", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                Text(cv.objective, fontSize = 6.5.sp, color = Color.DarkGray, maxLines = 3)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (!cv.isFreshGraduate && cv.experiences.any { it.jobTitle.isNotBlank() }) {
                Text("EXPERIENCE", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                cv.experiences.take(2).forEach { exp ->
                    Text("${exp.jobTitle} - ${exp.company} (${exp.duration})", fontSize = 6.5.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (cv.educations.any { it.degree.isNotBlank() }) {
                Text("EDUCATION", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                cv.educations.take(2).forEach { edu ->
                    Text("${edu.degree} - ${edu.institute} (${edu.passingYear})", fontSize = 6.5.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (cv.skills.isNotEmpty()) {
                Text("SKILLS", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                Text(cv.skills.joinToString(", "), fontSize = 6.5.sp, color = Color.DarkGray)
            }
        }
    }
}
