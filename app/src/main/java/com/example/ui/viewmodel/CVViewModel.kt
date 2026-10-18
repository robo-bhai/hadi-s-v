package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CertificationItem
import com.example.data.model.CVData
import com.example.data.model.Education
import com.example.data.model.Experience
import com.example.data.model.ProjectItem
import com.example.data.model.TemplateCategory
import com.example.data.model.TemplateInfo
import com.example.data.model.TemplateRegistry
import com.example.data.repository.CVRepository
import com.example.pdf.PdfGenerator
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CVViewModel(private val repository: CVRepository) : ViewModel() {

    private val _currentCV = MutableStateFlow(CVData())
    val currentCV: StateFlow<CVData> = _currentCV.asStateFlow()

    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    private val _isPreviewLoading = MutableStateFlow(false)
    val isPreviewLoading: StateFlow<Boolean> = _isPreviewLoading.asStateFlow()

    private val _selectedCategory = MutableStateFlow(TemplateCategory.ALL)
    val selectedCategory: StateFlow<TemplateCategory> = _selectedCategory.asStateFlow()

    private val _templateSearchQuery = MutableStateFlow("")
    val templateSearchQuery: StateFlow<String> = _templateSearchQuery.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _lastGeneratedFile = MutableStateFlow<File?>(null)
    val lastGeneratedFile: StateFlow<File?> = _lastGeneratedFile.asStateFlow()

    fun refreshPreview(context: Context) {
        viewModelScope.launch {
            _isPreviewLoading.value = true
            val bitmap = withContext(Dispatchers.Default) {
                try {
                    val generator = PdfGenerator(context)
                    generator.renderCvToBitmap(_currentCV.value, 1.8f)
                } catch (e: Exception) {
                    null
                }
            }
            _previewBitmap.value = bitmap
            _isPreviewLoading.value = false
        }
    }

    val savedCVs: StateFlow<List<CVData>> = repository.allCVs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setTemplateCategory(category: TemplateCategory) {
        _selectedCategory.value = category
    }

    fun setTemplateSearchQuery(query: String) {
        _templateSearchQuery.value = query
    }

    fun getFilteredTemplates(): List<TemplateInfo> {
        val cat = _selectedCategory.value
        val query = _templateSearchQuery.value.trim().lowercase()

        return TemplateRegistry.templates.filter { t ->
            val matchesCategory = cat == TemplateCategory.ALL || t.category == cat
            val matchesQuery = query.isEmpty() ||
                    t.name.lowercase().contains(query) ||
                    t.description.lowercase().contains(query) ||
                    t.category.title.lowercase().contains(query)
            matchesCategory && matchesQuery
        }
    }

    fun updatePersonalInfo(
        fullName: String? = null,
        jobTitle: String? = null,
        email: String? = null,
        phone: String? = null,
        city: String? = null,
        address: String? = null,
        cnic: String? = null,
        linkedInOrGithub: String? = null,
        photoUri: String? = null
    ) {
        _currentCV.update { current ->
            current.copy(
                fullName = fullName ?: current.fullName,
                jobTitle = jobTitle ?: current.jobTitle,
                email = email ?: current.email,
                phone = phone ?: current.phone,
                city = city ?: current.city,
                address = address ?: current.address,
                cnic = cnic ?: current.cnic,
                linkedInOrGithub = linkedInOrGithub ?: current.linkedInOrGithub,
                photoUri = if (photoUri != null && photoUri == "CLEAR") null else (photoUri ?: current.photoUri)
            )
        }
    }

    fun updateObjective(objective: String) {
        _currentCV.update { it.copy(objective = objective) }
    }

    // Education operations
    fun addEducation() {
        _currentCV.update { current ->
            current.copy(educations = current.educations + Education())
        }
    }

    fun updateEducation(index: Int, education: Education) {
        _currentCV.update { current ->
            val list = current.educations.toMutableList()
            if (index in list.indices) {
                list[index] = education
            }
            current.copy(educations = list)
        }
    }

    fun removeEducation(index: Int) {
        _currentCV.update { current ->
            val list = current.educations.toMutableList()
            if (index in list.indices && list.size > 1) {
                list.removeAt(index)
            }
            current.copy(educations = list)
        }
    }

    // Experience operations
    fun addExperience() {
        _currentCV.update { current ->
            current.copy(experiences = current.experiences + Experience())
        }
    }

    fun updateExperience(index: Int, experience: Experience) {
        _currentCV.update { current ->
            val list = current.experiences.toMutableList()
            if (index in list.indices) {
                list[index] = experience
            }
            current.copy(experiences = list)
        }
    }

    fun removeExperience(index: Int) {
        _currentCV.update { current ->
            val list = current.experiences.toMutableList()
            if (index in list.indices && list.size > 1) {
                list.removeAt(index)
            }
            current.copy(experiences = list)
        }
    }

    fun setFreshGraduate(isFresh: Boolean) {
        _currentCV.update { it.copy(isFreshGraduate = isFresh) }
    }

    // Project operations
    fun addProject() {
        _currentCV.update { current ->
            current.copy(projects = current.projects + ProjectItem())
        }
    }

    fun updateProject(index: Int, project: ProjectItem) {
        _currentCV.update { current ->
            val list = current.projects.toMutableList()
            if (index in list.indices) {
                list[index] = project
            }
            current.copy(projects = list)
        }
    }

    fun removeProject(index: Int) {
        _currentCV.update { current ->
            val list = current.projects.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            current.copy(projects = list)
        }
    }

    // Certification operations
    fun addCertification() {
        _currentCV.update { current ->
            current.copy(certifications = current.certifications + CertificationItem())
        }
    }

    fun updateCertification(index: Int, certification: CertificationItem) {
        _currentCV.update { current ->
            val list = current.certifications.toMutableList()
            if (index in list.indices) {
                list[index] = certification
            }
            current.copy(certifications = list)
        }
    }

    fun removeCertification(index: Int) {
        _currentCV.update { current ->
            val list = current.certifications.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            current.copy(certifications = list)
        }
    }

    // Skills & Languages
    fun addSkill(skill: String) {
        val trimmed = skill.trim()
        if (trimmed.isNotBlank()) {
            _currentCV.update { current ->
                if (!current.skills.contains(trimmed)) {
                    current.copy(skills = current.skills + trimmed)
                } else current
            }
        }
    }

    fun removeSkill(skill: String) {
        _currentCV.update { current ->
            current.copy(skills = current.skills.filter { it != skill })
        }
    }

    fun addLanguage(language: String) {
        val trimmed = language.trim()
        if (trimmed.isNotBlank()) {
            _currentCV.update { current ->
                if (!current.languages.contains(trimmed)) {
                    current.copy(languages = current.languages + trimmed)
                } else current
            }
        }
    }

    fun removeLanguage(language: String) {
        _currentCV.update { current ->
            current.copy(languages = current.languages.filter { it != language })
        }
    }

    fun selectTemplate(templateId: String, context: Context? = null) {
        _currentCV.update { it.copy(templateId = templateId) }
        if (context != null) refreshPreview(context)
    }

    fun setCustomAccent(colorHex: Long?, context: Context? = null) {
        _currentCV.update { it.copy(customAccentHex = colorHex) }
        if (context != null) refreshPreview(context)
    }

    fun saveCurrentCV(onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val cv = _currentCV.value
            val savedId = repository.saveCV(cv)
            _currentCV.update { it.copy(id = savedId) }
            onComplete?.invoke(savedId)
        }
    }

    fun loadCV(cv: CVData, context: Context? = null) {
        _currentCV.value = cv
        if (context != null) refreshPreview(context)
    }

    fun deleteCV(cv: CVData) {
        viewModelScope.launch {
            repository.deleteCV(cv)
        }
    }

    fun resetForm() {
        _currentCV.value = CVData()
        _lastGeneratedFile.value = null
        _previewBitmap.value = null
    }

    fun loadSampleData(context: Context? = null) {
        _currentCV.value = CVData(
            title = "Muhammad Ali - Full Stack Developer",
            fullName = "Muhammad Ali Khan",
            jobTitle = "Full Stack & Mobile Engineer",
            email = "m.ali.khan@gmail.com",
            phone = "+92 300 1234567",
            city = "Lahore",
            address = "DHA Phase 5, Lahore, Pakistan",
            cnic = "35201-1234567-3",
            linkedInOrGithub = "github.com/alikhan-pk | linkedin.com/in/alikhan",
            photoUri = null,
            objective = "Passionate and detail-oriented Software Engineer with strong hands-on expertise in Kotlin, Jetpack Compose, Room Database, and modern scalable system architecture. Proven track record building offline-first apps and high-performance solutions for Pakistani tech leaders and international clients.",
            educations = listOf(
                Education(
                    degree = "BS Computer Science",
                    institute = "FAST - NUCES Lahore",
                    passingYear = "2024",
                    gradeOrCgpa = "CGPA: 3.58 / 4.0"
                ),
                Education(
                    degree = "FSc Pre-Engineering",
                    institute = "Government College University (GCU) Lahore",
                    passingYear = "2020",
                    gradeOrCgpa = "A+ (92%)"
                )
            ),
            experiences = listOf(
                Experience(
                    jobTitle = "Associate Software Engineer",
                    company = "Systems Limited Lahore",
                    duration = "Jan 2024 - Present",
                    description = "Engineered enterprise Android & Compose features with 99.8% crash-free sessions. Implemented Room offline synchronization and automated unit test suites."
                ),
                Experience(
                    jobTitle = "Mobile App Developer Intern",
                    company = "Techlogix Pakistan",
                    duration = "Jun 2023 - Dec 2023",
                    description = "Built reactive UI modules, integrated REST APIs with Retrofit, and cut app load times by 28%."
                )
            ),
            isFreshGraduate = false,
            projects = listOf(
                ProjectItem(
                    title = "Quick CV Maker Android",
                    techStack = "Kotlin, Jetpack Compose, Room, PdfDocument",
                    link = "github.com/alikhan-pk/quick-cv",
                    description = "Offline-first resume builder app featuring 40+ dynamic templates and high-resolution A4 PDF vector generation."
                ),
                ProjectItem(
                    title = "PakAgri Market Portal",
                    techStack = "Android, Spring Boot, PostgreSQL",
                    link = "github.com/alikhan-pk/agri-portal",
                    description = "Connecting 5000+ local Pakistani farmers directly with grain traders, featuring real-time Urdu mandi rates."
                )
            ),
            certifications = listOf(
                CertificationItem(
                    name = "Google Associate Android Developer",
                    issuer = "Google Developers",
                    year = "2024"
                ),
                CertificationItem(
                    name = "Meta Frontend & Mobile Specialization",
                    issuer = "Coursera / Meta",
                    year = "2023"
                )
            ),
            skills = listOf(
                "Kotlin",
                "Jetpack Compose",
                "Android SDK",
                "Room Database",
                "Coroutines & Flow",
                "MVVM & Clean Architecture",
                "RESTful APIs",
                "Git & GitHub",
                "CI/CD Pipelines",
                "Team Leadership"
            ),
            languages = listOf("English (Professional)", "Urdu (Native)", "Punjabi (Conversational)"),
            templateId = "modern_emerald"
        )
        if (context != null) refreshPreview(context)
    }

    fun generatePdf(context: Context, onResult: (Result<File>) -> Unit) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            val generator = PdfGenerator(context)
            val result = generator.generatePdf(_currentCV.value)
            if (result.isSuccess) {
                _lastGeneratedFile.value = result.getOrNull()
            }
            _isGeneratingPdf.value = false
            onResult(result)
        }
    }
}

class CVViewModelFactory(private val repository: CVRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CVViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CVViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
