package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.CertificationItem
import com.example.data.model.CVData
import com.example.data.model.Education
import com.example.data.model.Experience
import com.example.data.model.ProjectItem
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldTertiary
import com.example.ui.viewmodel.CVViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormScreen(
    viewModel: CVViewModel,
    cvData: CVData,
    onNavigateToPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Personal", "Objective", "Education", "Experience", "Projects", "Skills & Langs")
    val atsScore = cvData.calculateAtsScore()

    Column(modifier = modifier.fillMaxSize()) {
        // ATS Compatibility Header Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "ATS Score",
                            tint = if (atsScore >= 80) EmeraldPrimary else GoldTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ATS Resume Strength",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "$atsScore% Complete",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (atsScore >= 80) EmeraldPrimary else GoldTertiary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { atsScore / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (atsScore >= 80) EmeraldPrimary else GoldTertiary,
                    trackColor = Color.LightGray.copy(alpha = 0.4f)
                )
            }
        }

        // Tab Bar
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = "${index + 1}. $title",
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Form Body
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> PersonalInfoSection(cvData = cvData, viewModel = viewModel)
                1 -> ObjectiveSection(cvData = cvData, viewModel = viewModel)
                2 -> EducationSection(cvData = cvData, viewModel = viewModel)
                3 -> ExperienceSection(cvData = cvData, viewModel = viewModel)
                4 -> ProjectsCertificationsSection(cvData = cvData, viewModel = viewModel)
                5 -> SkillsLanguagesSection(cvData = cvData, viewModel = viewModel)
            }
        }

        // Bottom Navigation Bar
        HorizontalDivider()
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedTab > 0) {
                    OutlinedButton(
                        onClick = { selectedTab-- },
                        modifier = Modifier.testTag("form_previous_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (selectedTab < tabs.size - 1) {
                    Button(
                        onClick = { selectedTab++ },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.testTag("form_next_button")
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.saveCurrentCV {
                                onNavigateToPreview()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.testTag("save_and_preview_button")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Preview")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Choose Template")
                    }
                }
            }
        }
    }
}

// =========================================================================
// SECTION 1: PERSONAL INFO
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonalInfoSection(
    cvData: CVData,
    viewModel: CVViewModel
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updatePersonalInfo(photoUri = uri.toString())
        }
    }

    val pakistaniCities = listOf("Lahore", "Karachi", "Islamabad", "Rawalpindi", "Faisalabad", "Peshawar", "Multan", "Quetta", "Sialkot", "Gujranwala")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(EmeraldContainer)
                            .border(2.dp, EmeraldPrimary, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (cvData.photoUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(cvData.photoUri),
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Add Photo",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text("Add Photo", fontSize = 10.sp, color = EmeraldPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Professional Photo",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Formal passport size photo (optional)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.testTag("upload_photo_button")
                            ) {
                                Text(if (cvData.photoUri == null) "Select Photo" else "Change")
                            }
                            if (cvData.photoUri != null) {
                                IconButton(onClick = { viewModel.updatePersonalInfo(photoUri = "CLEAR") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = cvData.fullName,
                onValueChange = { viewModel.updatePersonalInfo(fullName = it) },
                label = { Text("Full Name *") },
                placeholder = { Text("e.g. Muhammad Ali Khan") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("full_name_input"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = cvData.jobTitle,
                onValueChange = { viewModel.updatePersonalInfo(jobTitle = it) },
                label = { Text("Job Title / Target Role *") },
                placeholder = { Text("e.g. Android Developer / Fresh Graduate") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("job_title_input"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = cvData.email,
                onValueChange = { viewModel.updatePersonalInfo(email = it) },
                label = { Text("Email Address *") },
                placeholder = { Text("e.g. m.ali@gmail.com") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = cvData.phone,
                onValueChange = { viewModel.updatePersonalInfo(phone = it) },
                label = { Text("Phone Number (+92) *") },
                placeholder = { Text("e.g. +92 300 1234567") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phone_input"),
                singleLine = true
            )
        }

        item {
            Column {
                Text(
                    text = "City in Pakistan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pakistaniCities.forEach { city ->
                        FilterChip(
                            selected = cvData.city.equals(city, ignoreCase = true),
                            onClick = { viewModel.updatePersonalInfo(city = city) },
                            label = { Text(city, fontSize = 12.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = cvData.city,
                    onValueChange = { viewModel.updatePersonalInfo(city = it) },
                    label = { Text("City / Town") },
                    placeholder = { Text("e.g. Lahore") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        item {
            OutlinedTextField(
                value = cvData.address,
                onValueChange = { viewModel.updatePersonalInfo(address = it) },
                label = { Text("Residential Address") },
                placeholder = { Text("e.g. Gulberg III, Lahore") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = cvData.cnic,
                onValueChange = { viewModel.updatePersonalInfo(cnic = it) },
                label = { Text("CNIC Number (Optional)") },
                placeholder = { Text("e.g. 35201-1234567-1") },
                supportingText = { Text("Standard Pakistani identity number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = cvData.linkedInOrGithub,
                onValueChange = { viewModel.updatePersonalInfo(linkedInOrGithub = it) },
                label = { Text("LinkedIn / GitHub / Portfolio Link") },
                placeholder = { Text("e.g. linkedin.com/in/username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

// =========================================================================
// SECTION 2: OBJECTIVE & SUMMARY
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ObjectiveSection(
    cvData: CVData,
    viewModel: CVViewModel
) {
    val templates = listOf(
        "Fresh Graduate (CS/IT)" to "Motivated Computer Science graduate from a recognized Pakistani university with solid foundation in OOP, Android, and Web development. Seeking an entry-level software engineering role to build robust applications.",
        "Experienced Developer" to "Results-driven Software Engineer with proven hands-on experience developing and deploying high-performance mobile and web solutions. Committed to clean code, scalable architecture, and continuous learning.",
        "Business & Marketing" to "Ambitious Business Administration graduate with expertise in marketing strategies, customer relationship management, and data-driven market research in Pakistan's evolving commercial sector.",
        "Banking & Finance" to "Detail-oriented Finance professional skilled in financial analysis, account reconciliation, and risk assessment adhering to State Bank of Pakistan compliance standards.",
        "Customer Support" to "Customer-centric professional with fluent English and Urdu communication skills, adept at handling customer queries, client retention, and technical support in 24/7 environments."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Professional Objective / About Me",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Tap a preset below to instantly populate your summary or customize your own:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                templates.forEach { (label, text) ->
                    FilterChip(
                        selected = cvData.objective == text,
                        onClick = { viewModel.updateObjective(text) },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = cvData.objective,
                onValueChange = { viewModel.updateObjective(it) },
                label = { Text("Career Objective / Summary") },
                placeholder = { Text("Write 2-4 sentences summarizing your strengths, experience, and career goals...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag("objective_input"),
                maxLines = 8
            )
        }
    }
}

// =========================================================================
// SECTION 3: EDUCATION
// =========================================================================
@Composable
fun EducationSection(
    cvData: CVData,
    viewModel: CVViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Education Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Add degrees, universities & colleges",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { viewModel.addEducation() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.testTag("add_education_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }

        itemsIndexed(cvData.educations, key = { _, edu -> edu.id }) { index, edu ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Education #${index + 1}", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }

                        if (cvData.educations.size > 1) {
                            IconButton(
                                onClick = { viewModel.removeEducation(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = edu.degree,
                        onValueChange = { viewModel.updateEducation(index, edu.copy(degree = it)) },
                        label = { Text("Degree / Certificate *") },
                        placeholder = { Text("e.g. BS Computer Science, FSc") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = edu.institute,
                        onValueChange = { viewModel.updateEducation(index, edu.copy(institute = it)) },
                        label = { Text("Institute / Board / University *") },
                        placeholder = { Text("e.g. FAST-NUCES, NUST, Punjab University") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = edu.passingYear,
                            onValueChange = { viewModel.updateEducation(index, edu.copy(passingYear = it)) },
                            label = { Text("Year") },
                            placeholder = { Text("e.g. 2024") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = edu.gradeOrCgpa,
                            onValueChange = { viewModel.updateEducation(index, edu.copy(gradeOrCgpa = it)) },
                            label = { Text("CGPA / Grade") },
                            placeholder = { Text("e.g. 3.5 CGPA / A+") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// SECTION 4: WORK EXPERIENCE
// =========================================================================
@Composable
fun ExperienceSection(
    cvData: CVData,
    viewModel: CVViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = cvData.isFreshGraduate,
                        onCheckedChange = { viewModel.setFreshGraduate(it) },
                        modifier = Modifier.testTag("fresh_graduate_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Fresh Graduate / No Job Experience Yet",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Skips experience section and highlights education, projects & skills",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (!cvData.isFreshGraduate) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Work Experience",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Jobs, internships & freelancing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { viewModel.addExperience() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.testTag("add_experience_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }

            itemsIndexed(cvData.experiences, key = { _, exp -> exp.id }) { index, exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Work, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Experience #${index + 1}", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }

                            if (cvData.experiences.size > 1) {
                                IconButton(
                                    onClick = { viewModel.removeExperience(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = exp.jobTitle,
                            onValueChange = { viewModel.updateExperience(index, exp.copy(jobTitle = it)) },
                            label = { Text("Job Title *") },
                            placeholder = { Text("e.g. Software Engineer / Intern") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = exp.company,
                            onValueChange = { viewModel.updateExperience(index, exp.copy(company = it)) },
                            label = { Text("Company / Organization *") },
                            placeholder = { Text("e.g. Systems Limited, Jazz, Netsol") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = exp.duration,
                            onValueChange = { viewModel.updateExperience(index, exp.copy(duration = it)) },
                            label = { Text("Duration") },
                            placeholder = { Text("e.g. Jan 2023 - Present or 6 Months") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = exp.description,
                            onValueChange = { viewModel.updateExperience(index, exp.copy(description = it)) },
                            label = { Text("Key Responsibilities & Achievements") },
                            placeholder = { Text("• Developed features\n• Improved app performance by 20%") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            maxLines = 4
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// SECTION 5: PROJECTS & CERTIFICATIONS
// =========================================================================
@Composable
fun ProjectsCertificationsSection(
    cvData: CVData,
    viewModel: CVViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Projects Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Projects & Portfolio",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Showcase FYP, GitHub repositories & apps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { viewModel.addProject() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Project")
                }
            }
        }

        if (cvData.projects.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No projects added yet. Click 'Add Project' to highlight your university or personal work.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        itemsIndexed(cvData.projects, key = { _, p -> p.id }) { index, p ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Project #${index + 1}", fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { viewModel.removeProject(index) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = p.title,
                        onValueChange = { viewModel.updateProject(index, p.copy(title = it)) },
                        label = { Text("Project Title") },
                        placeholder = { Text("e.g. E-Commerce Android App") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = p.techStack,
                        onValueChange = { viewModel.updateProject(index, p.copy(techStack = it)) },
                        label = { Text("Tech Stack / Tools") },
                        placeholder = { Text("e.g. Kotlin, Compose, Firebase, Room") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = p.description,
                        onValueChange = { viewModel.updateProject(index, p.copy(description = it)) },
                        label = { Text("Project Description") },
                        placeholder = { Text("Brief description of features & achievements...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        }

        item { HorizontalDivider() }

        // Certifications Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Certifications & Badges",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Google, Meta, Coursera, PEC or Microsoft",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { viewModel.addCertification() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Cert")
                }
            }
        }

        itemsIndexed(cvData.certifications, key = { _, c -> c.id }) { index, c ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Certification #${index + 1}", fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { viewModel.removeCertification(index) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = c.name,
                        onValueChange = { viewModel.updateCertification(index, c.copy(name = it)) },
                        label = { Text("Certificate Name") },
                        placeholder = { Text("e.g. Associate Android Developer") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = c.issuer,
                            onValueChange = { viewModel.updateCertification(index, c.copy(issuer = it)) },
                            label = { Text("Issuer / Academy") },
                            placeholder = { Text("e.g. Google") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = c.year,
                            onValueChange = { viewModel.updateCertification(index, c.copy(year = it)) },
                            label = { Text("Year") },
                            placeholder = { Text("2024") },
                            modifier = Modifier.weight(0.5f),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// SECTION 6: SKILLS & LANGUAGES
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsLanguagesSection(
    cvData: CVData,
    viewModel: CVViewModel
) {
    var newSkillText by remember { mutableStateOf("") }
    var newLangText by remember { mutableStateOf("") }

    val commonPakistaniSkills = listOf(
        "MS Office / Excel", "Kotlin", "Java", "Python", "Android SDK",
        "React", "Node.js", "Graphic Design", "Digital Marketing",
        "Accounting & QuickBooks", "Communication Skills", "Teamwork",
        "Problem Solving", "Time Management", "Project Management"
    )

    val commonLanguages = listOf("Urdu (Native)", "English (Fluent)", "Punjabi", "Sindhi", "Pashto", "Balochi", "Arabic")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Key Skills & Tools",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Tap to add popular skills or type your own below:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                commonPakistaniSkills.forEach { skill ->
                    val isSelected = cvData.skills.contains(skill)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) viewModel.removeSkill(skill)
                            else viewModel.addSkill(skill)
                        },
                        label = { Text(skill, fontSize = 12.sp) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newSkillText,
                    onValueChange = { newSkillText = it },
                    label = { Text("Add Custom Skill") },
                    placeholder = { Text("e.g. Flutter, SEO") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (newSkillText.isNotBlank()) {
                            viewModel.addSkill(newSkillText)
                            newSkillText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Add")
                }
            }
        }

        if (cvData.skills.isNotEmpty()) {
            item {
                Text(
                    text = "Selected Skills (${cvData.skills.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cvData.skills.forEach { skill ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = EmeraldContainer,
                            modifier = Modifier.clickable { viewModel.removeSkill(skill) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(skill, fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        item { HorizontalDivider() }

        item {
            Text(
                text = "Languages",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                commonLanguages.forEach { lang ->
                    val isSelected = cvData.languages.contains(lang)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) viewModel.removeLanguage(lang)
                            else viewModel.addLanguage(lang)
                        },
                        label = { Text(lang, fontSize = 12.sp) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newLangText,
                    onValueChange = { newLangText = it },
                    label = { Text("Add Custom Language") },
                    placeholder = { Text("e.g. German, French") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (newLangText.isNotBlank()) {
                            viewModel.addLanguage(newLangText)
                            newLangText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Add")
                }
            }
        }
    }
}
