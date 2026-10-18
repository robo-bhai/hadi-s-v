package com.example.data.model

import java.util.UUID

enum class TemplateCategory(val title: String) {
    ALL("All (44)"),
    MODERN("Modern & Creative (10)"),
    CLASSIC("Executive Classic (10)"),
    TECH("IT & Tech (8)"),
    COMPACT("1-Page Compact (8)"),
    PAKISTAN("Pakistan & Industry (8)")
}

enum class LayoutStyle {
    SIDEBAR_LEFT,
    SIDEBAR_RIGHT,
    HEADER_BANNER,
    CLASSIC_CENTERED,
    TWO_COLUMN_GRID,
    TIMELINE_ACCENT,
    MINIMAL_BORDER
}

enum class FontStyleType {
    SANS,
    SERIF,
    MONO
}

data class TemplateInfo(
    val id: String,
    val name: String,
    val category: TemplateCategory,
    val description: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val layoutStyle: LayoutStyle,
    val fontStyle: FontStyleType = FontStyleType.SANS,
    val badge: String? = null,
    val isAtsOptimized: Boolean = true
)

data class Education(
    val id: String = UUID.randomUUID().toString(),
    val degree: String = "",
    val institute: String = "",
    val passingYear: String = "",
    val gradeOrCgpa: String = ""
)

data class Experience(
    val id: String = UUID.randomUUID().toString(),
    val jobTitle: String = "",
    val company: String = "",
    val duration: String = "",
    val description: String = ""
)

data class ProjectItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val techStack: String = "",
    val link: String = "",
    val description: String = ""
)

data class CertificationItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val issuer: String = "",
    val year: String = ""
)

data class CVData(
    val id: Long = 0,
    val title: String = "My Professional CV",
    // Personal Info
    val fullName: String = "",
    val jobTitle: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val address: String = "",
    val cnic: String = "",
    val linkedInOrGithub: String = "",
    val photoUri: String? = null,
    // Content
    val objective: String = "",
    val educations: List<Education> = listOf(Education()),
    val experiences: List<Experience> = listOf(Experience()),
    val isFreshGraduate: Boolean = false,
    val projects: List<ProjectItem> = emptyList(),
    val certifications: List<CertificationItem> = emptyList(),
    val skills: List<String> = listOf("MS Office", "Communication", "Problem Solving"),
    val languages: List<String> = listOf("English (Fluent)", "Urdu (Native)"),
    val referencesText: String = "References available upon request",
    // Template & Customization
    val templateId: String = "modern_emerald",
    val customAccentHex: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getActiveTemplate(): TemplateInfo {
        return TemplateRegistry.getTemplateById(templateId)
    }

    fun calculateAtsScore(): Int {
        var score = 0
        if (fullName.isNotBlank()) score += 15
        if (jobTitle.isNotBlank()) score += 10
        if (email.contains("@") && email.length > 5) score += 15
        if (phone.length >= 10) score += 15
        if (objective.length > 25) score += 15
        if (educations.any { it.degree.isNotBlank() && it.institute.isNotBlank() }) score += 15
        if (skills.size >= 3) score += 10
        if ((!isFreshGraduate && experiences.any { it.jobTitle.isNotBlank() }) || (isFreshGraduate && educations.isNotEmpty())) score += 5
        return score.coerceIn(0, 100)
    }
}

object TemplateRegistry {
    val templates: List<TemplateInfo> = listOf(
        // Modern & Creative (1-10)
        TemplateInfo(
            id = "modern_emerald",
            name = "Emerald Elite",
            category = TemplateCategory.MODERN,
            description = "Sleek Pakistani emerald sidebar with golden bullet points and photo badge.",
            primaryColorHex = 0xFF0F5132,
            secondaryColorHex = 0xFF198754,
            accentColorHex = 0xFFD4AF37,
            layoutStyle = LayoutStyle.SIDEBAR_LEFT,
            badge = "Most Popular"
        ),
        TemplateInfo(
            id = "modern_sapphire",
            name = "Sapphire Corporate",
            category = TemplateCategory.MODERN,
            description = "Executive deep royal navy sidebar with warm amber highlights.",
            primaryColorHex = 0xFF1A365D,
            secondaryColorHex = 0xFF2B6CB0,
            accentColorHex = 0xFFDD6B20,
            layoutStyle = LayoutStyle.SIDEBAR_LEFT
        ),
        TemplateInfo(
            id = "modern_ruby",
            name = "Ruby Executive",
            category = TemplateCategory.MODERN,
            description = "Distinguished deep crimson wine sidebar with elegant contrast.",
            primaryColorHex = 0xFF7B1113,
            secondaryColorHex = 0xFF9E2A2B,
            accentColorHex = 0xFFE09F3E,
            layoutStyle = LayoutStyle.SIDEBAR_LEFT
        ),
        TemplateInfo(
            id = "modern_amethyst",
            name = "Amethyst Studio",
            category = TemplateCategory.MODERN,
            description = "Modern creative agency theme in royal plum violet.",
            primaryColorHex = 0xFF4A154B,
            secondaryColorHex = 0xFF6B2D5C,
            accentColorHex = 0xFFE3A857,
            layoutStyle = LayoutStyle.SIDEBAR_LEFT
        ),
        TemplateInfo(
            id = "modern_teal",
            name = "Teal Horizon",
            category = TemplateCategory.MODERN,
            description = "Clean top banner in deep ocean teal with pill tags.",
            primaryColorHex = 0xFF006D77,
            secondaryColorHex = 0xFF83C5BE,
            accentColorHex = 0xFFE29578,
            layoutStyle = LayoutStyle.HEADER_BANNER
        ),
        TemplateInfo(
            id = "modern_nordic",
            name = "Nordic Slate",
            category = TemplateCategory.MODERN,
            description = "Scandinavian minimalist palette with steel blue accents.",
            primaryColorHex = 0xFF2B2D42,
            secondaryColorHex = 0xFF4A4E69,
            accentColorHex = 0xFF457B9D,
            layoutStyle = LayoutStyle.SIDEBAR_LEFT
        ),
        TemplateInfo(
            id = "modern_charcoal",
            name = "Charcoal Tech",
            category = TemplateCategory.MODERN,
            description = "High-contrast dark charcoal with electric cyan accents.",
            primaryColorHex = 0xFF1F2421,
            secondaryColorHex = 0xFF212529,
            accentColorHex = 0xFF00B4D8,
            layoutStyle = LayoutStyle.SIDEBAR_LEFT
        ),
        TemplateInfo(
            id = "modern_rosegold",
            name = "Rose Gold Studio",
            category = TemplateCategory.MODERN,
            description = "Fashion & media aesthetic in dusty mauve and warm champagne.",
            primaryColorHex = 0xFF4A3E4D,
            secondaryColorHex = 0xFF6D597A,
            accentColorHex = 0xFFDDA15E,
            layoutStyle = LayoutStyle.HEADER_BANNER
        ),
        TemplateInfo(
            id = "modern_coral",
            name = "Creative Flare",
            category = TemplateCategory.MODERN,
            description = "Vibrant warm coral and slate for design and UI/UX roles.",
            primaryColorHex = 0xFFE76F51,
            secondaryColorHex = 0xFFF4A261,
            accentColorHex = 0xFF264653,
            layoutStyle = LayoutStyle.HEADER_BANNER
        ),
        TemplateInfo(
            id = "modern_midnight",
            name = "Midnight Indigo",
            category = TemplateCategory.MODERN,
            description = "Deep night sky blue with cobalt timeline nodes.",
            primaryColorHex = 0xFF0B132B,
            secondaryColorHex = 0xFF1C2541,
            accentColorHex = 0xFF3A86FF,
            layoutStyle = LayoutStyle.SIDEBAR_LEFT
        ),

        // Classic & Professional (11-20)
        TemplateInfo(
            id = "classic_harvard",
            name = "Harvard Traditional",
            category = TemplateCategory.CLASSIC,
            description = "Ivy League centered formal header with serif typography and crisp rules.",
            primaryColorHex = 0xFF1E293B,
            secondaryColorHex = 0xFFA51C30,
            accentColorHex = 0xFFA51C30,
            layoutStyle = LayoutStyle.CLASSIC_CENTERED,
            fontStyle = FontStyleType.SERIF,
            badge = "ATS Top Pick"
        ),
        TemplateInfo(
            id = "classic_oxford",
            name = "Oxford Academic",
            category = TemplateCategory.CLASSIC,
            description = "Traditional British academic layout with Oxford Navy rules.",
            primaryColorHex = 0xFF002147,
            secondaryColorHex = 0xFF003366,
            accentColorHex = 0xFFB38F4D,
            layoutStyle = LayoutStyle.CLASSIC_CENTERED,
            fontStyle = FontStyleType.SERIF
        ),
        TemplateInfo(
            id = "classic_wallstreet",
            name = "Wall Street Corporate",
            category = TemplateCategory.CLASSIC,
            description = "Formal black and white investment banking standard.",
            primaryColorHex = 0xFF111827,
            secondaryColorHex = 0xFF374151,
            accentColorHex = 0xFF059669,
            layoutStyle = LayoutStyle.CLASSIC_CENTERED,
            fontStyle = FontStyleType.SERIF
        ),
        TemplateInfo(
            id = "classic_legal",
            name = "Legal Counsel",
            category = TemplateCategory.CLASSIC,
            description = "Designed for lawyers, advocates, and corporate counsel.",
            primaryColorHex = 0xFF4A0E17,
            secondaryColorHex = 0xFF6A1B29,
            accentColorHex = 0xFFC5A059,
            layoutStyle = LayoutStyle.CLASSIC_CENTERED,
            fontStyle = FontStyleType.SERIF
        ),
        TemplateInfo(
            id = "classic_serif_navy",
            name = "Diplomat Navy",
            category = TemplateCategory.CLASSIC,
            description = "Clean ambassadorial layout with double divider lines.",
            primaryColorHex = 0xFF0F2027,
            secondaryColorHex = 0xFF203A43,
            accentColorHex = 0xFFD4AF37,
            layoutStyle = LayoutStyle.CLASSIC_CENTERED,
            fontStyle = FontStyleType.SERIF
        ),
        TemplateInfo(
            id = "classic_herald",
            name = "Imperial Herald",
            category = TemplateCategory.CLASSIC,
            description = "Distinguished bronze & espresso header for senior directors.",
            primaryColorHex = 0xFF3E2723,
            secondaryColorHex = 0xFF4E342E,
            accentColorHex = 0xFFD7CCC8,
            layoutStyle = LayoutStyle.HEADER_BANNER,
            fontStyle = FontStyleType.SERIF
        ),
        TemplateInfo(
            id = "classic_monogram",
            name = "Monogram Executive",
            category = TemplateCategory.CLASSIC,
            description = "Features candidate initials in a stately golden monogram crest.",
            primaryColorHex = 0xFF1B3B2B,
            secondaryColorHex = 0xFF2C5E43,
            accentColorHex = 0xFFD4AF37,
            layoutStyle = LayoutStyle.CLASSIC_CENTERED,
            fontStyle = FontStyleType.SERIF
        ),
        TemplateInfo(
            id = "classic_chronicle",
            name = "Times Chronicle",
            category = TemplateCategory.CLASSIC,
            description = "Standard newspaper chronicle format with clean columns.",
            primaryColorHex = 0xFF1A1A1A,
            secondaryColorHex = 0xFF404040,
            accentColorHex = 0xFF737373,
            layoutStyle = LayoutStyle.CLASSIC_CENTERED,
            fontStyle = FontStyleType.SERIF
        ),
        TemplateInfo(
            id = "classic_prestige",
            name = "Prestige Scholar",
            category = TemplateCategory.CLASSIC,
            description = "Deep indigo with clean silver rules for researchers & professors.",
            primaryColorHex = 0xFF1E1B4B,
            secondaryColorHex = 0xFF312E81,
            accentColorHex = 0xFF94A3B8,
            layoutStyle = LayoutStyle.CLASSIC_CENTERED,
            fontStyle = FontStyleType.SERIF
        ),
        TemplateInfo(
            id = "classic_cambridge",
            name = "Cambridge Maroon",
            category = TemplateCategory.CLASSIC,
            description = "Heritage royal maroon with subtle section separators.",
            primaryColorHex = 0xFF500000,
            secondaryColorHex = 0xFF700000,
            accentColorHex = 0xFFC5A059,
            layoutStyle = LayoutStyle.CLASSIC_CENTERED,
            fontStyle = FontStyleType.SERIF
        ),

        // IT & Tech / Developer (21-28)
        TemplateInfo(
            id = "tech_terminal",
            name = "Terminal Matrix",
            category = TemplateCategory.TECH,
            description = "Dark IDE theme with matrix green prompts and monospace styling.",
            primaryColorHex = 0xFF1E293B,
            secondaryColorHex = 0xFF0F172A,
            accentColorHex = 0xFF10B981,
            layoutStyle = LayoutStyle.SIDEBAR_LEFT,
            fontStyle = FontStyleType.MONO,
            badge = "Dev Favorite"
        ),
        TemplateInfo(
            id = "tech_github",
            name = "Octo Dev Clean",
            category = TemplateCategory.TECH,
            description = "Inspired by GitHub profiles with repo link tags and badge styling.",
            primaryColorHex = 0xFF24292E,
            secondaryColorHex = 0xFF57606A,
            accentColorHex = 0xFF0969DA,
            layoutStyle = LayoutStyle.HEADER_BANNER,
            fontStyle = FontStyleType.MONO
        ),
        TemplateInfo(
            id = "tech_cyber",
            name = "Cyber Matrix",
            category = TemplateCategory.TECH,
            description = "Futuristic timeline with neon cyan milestone nodes.",
            primaryColorHex = 0xFF0F172A,
            secondaryColorHex = 0xFF1E293B,
            accentColorHex = 0xFF06B6D4,
            layoutStyle = LayoutStyle.TIMELINE_ACCENT
        ),
        TemplateInfo(
            id = "tech_devops",
            name = "DevOps Cloud",
            category = TemplateCategory.TECH,
            description = "Structured dual columns optimized for cloud & infra certifications.",
            primaryColorHex = 0xFF1E3A8A,
            secondaryColorHex = 0xFF2563EB,
            accentColorHex = 0xFFEA580C,
            layoutStyle = LayoutStyle.TWO_COLUMN_GRID
        ),
        TemplateInfo(
            id = "tech_data_scientist",
            name = "Data Scientist Grid",
            category = TemplateCategory.TECH,
            description = "Tailored for AI, Machine Learning, and Python Data Analysts.",
            primaryColorHex = 0xFF1D3557,
            secondaryColorHex = 0xFF457B9D,
            accentColorHex = 0xFFE63946,
            layoutStyle = LayoutStyle.TWO_COLUMN_GRID
        ),
        TemplateInfo(
            id = "tech_fullstack",
            name = "Full Stack Modular",
            category = TemplateCategory.TECH,
            description = "Deep violet sidebar with distinct frontend and backend chip blocks.",
            primaryColorHex = 0xFF312E81,
            secondaryColorHex = 0xFF4338CA,
            accentColorHex = 0xFF059669,
            layoutStyle = LayoutStyle.SIDEBAR_LEFT
        ),
        TemplateInfo(
            id = "tech_minimal_mono",
            name = "Clean Code Mono",
            category = TemplateCategory.TECH,
            description = "Minimalist monospace structure with zero fluff and maximum signal.",
            primaryColorHex = 0xFF18181B,
            secondaryColorHex = 0xFF27272A,
            accentColorHex = 0xFF3B82F6,
            layoutStyle = LayoutStyle.MINIMAL_BORDER,
            fontStyle = FontStyleType.MONO
        ),
        TemplateInfo(
            id = "tech_mobile_dev",
            name = "Mobile App Architect",
            category = TemplateCategory.TECH,
            description = "Android Kotlin & Flutter developer theme with teal & purple accents.",
            primaryColorHex = 0xFF4C1D95,
            secondaryColorHex = 0xFF6D28D9,
            accentColorHex = 0xFF14B8A6,
            layoutStyle = LayoutStyle.TIMELINE_ACCENT
        ),

        // Compact & 1-Page Minimal (29-36)
        TemplateInfo(
            id = "compact_swiss",
            name = "Swiss Clean Grid",
            category = TemplateCategory.COMPACT,
            description = "Strict Bauhaus-inspired typography with crisp dual-column density.",
            primaryColorHex = 0xFF111827,
            secondaryColorHex = 0xFF374151,
            accentColorHex = 0xFFDC2626,
            layoutStyle = LayoutStyle.TWO_COLUMN_GRID,
            badge = "1-Page Best"
        ),
        TemplateInfo(
            id = "compact_metro",
            name = "Metro Dense",
            category = TemplateCategory.COMPACT,
            description = "Streamlined modern block header with condensed line heights.",
            primaryColorHex = 0xFF0E7490,
            secondaryColorHex = 0xFF155E75,
            accentColorHex = 0xFF0891B2,
            layoutStyle = LayoutStyle.HEADER_BANNER
        ),
        TemplateInfo(
            id = "compact_fasttrack",
            name = "1-Page FastTrack",
            category = TemplateCategory.COMPACT,
            description = "Emerald dual column layout maximizing single-page capacity.",
            primaryColorHex = 0xFF047857,
            secondaryColorHex = 0xFF065F46,
            accentColorHex = 0xFF10B981,
            layoutStyle = LayoutStyle.TWO_COLUMN_GRID
        ),
        TemplateInfo(
            id = "compact_micro",
            name = "Micro Bullets",
            category = TemplateCategory.COMPACT,
            description = "Dense bullet formatting ideal for extensive skill and tooling lists.",
            primaryColorHex = 0xFF334155,
            secondaryColorHex = 0xFF475569,
            accentColorHex = 0xFF0284C7,
            layoutStyle = LayoutStyle.MINIMAL_BORDER
        ),
        TemplateInfo(
            id = "compact_split",
            name = "Dual Column Split",
            category = TemplateCategory.COMPACT,
            description = "50/50 balanced page division with silver borders.",
            primaryColorHex = 0xFF000000,
            secondaryColorHex = 0xFF4B5563,
            accentColorHex = 0xFF6B7280,
            layoutStyle = LayoutStyle.TWO_COLUMN_GRID
        ),
        TemplateInfo(
            id = "compact_student",
            name = "Student Pocket CV",
            category = TemplateCategory.COMPACT,
            description = "Clean youthful theme for matric, intermediate, and undergrad students.",
            primaryColorHex = 0xFF2563EB,
            secondaryColorHex = 0xFF1D4ED8,
            accentColorHex = 0xFFF59E0B,
            layoutStyle = LayoutStyle.HEADER_BANNER
        ),
        TemplateInfo(
            id = "compact_condensed",
            name = "Ultra Condensed Pro",
            category = TemplateCategory.COMPACT,
            description = "Maximum content density fitting 5+ years of experience onto 1 page.",
            primaryColorHex = 0xFF374151,
            secondaryColorHex = 0xFF4B5563,
            accentColorHex = 0xFF4B5563,
            layoutStyle = LayoutStyle.MINIMAL_BORDER
        ),
        TemplateInfo(
            id = "compact_zenith",
            name = "Zenith Minimal",
            category = TemplateCategory.COMPACT,
            description = "Subtle neutral ash header with pure content focus.",
            primaryColorHex = 0xFF475569,
            secondaryColorHex = 0xFF64748B,
            accentColorHex = 0xFF94A3B8,
            layoutStyle = LayoutStyle.CLASSIC_CENTERED
        ),

        // Pakistan & Overseas Industry Specials (37-44)
        TemplateInfo(
            id = "pk_govt_ppsc",
            name = "PPSC / FPSC Govt Special",
            category = TemplateCategory.PAKISTAN,
            description = "Format tailored for Pakistani Civil Services, CSS, PPSC, and FPSC tests.",
            primaryColorHex = 0xFF01411C,
            secondaryColorHex = 0xFF036626,
            accentColorHex = 0xFFD4AF37,
            layoutStyle = LayoutStyle.CLASSIC_CENTERED,
            fontStyle = FontStyleType.SERIF,
            badge = "Govt Jobs"
        ),
        TemplateInfo(
            id = "pk_banking_sbp",
            name = "Habib Banking & Finance",
            category = TemplateCategory.PAKISTAN,
            description = "Executive emerald format for HBL, Meezan, UBL, and SBP compliance.",
            primaryColorHex = 0xFF004D40,
            secondaryColorHex = 0xFF00695C,
            accentColorHex = 0xFFD4AF37,
            layoutStyle = LayoutStyle.SIDEBAR_LEFT,
            badge = "Bank Sector"
        ),
        TemplateInfo(
            id = "pk_techlogix",
            name = "Lahore Tech Hub",
            category = TemplateCategory.PAKISTAN,
            description = "Tailored for Pakistani tech exporters (Systems, Netsol, Techlogix, Arbisoft).",
            primaryColorHex = 0xFF3730A3,
            secondaryColorHex = 0xFF4338CA,
            accentColorHex = 0xFF10B981,
            layoutStyle = LayoutStyle.SIDEBAR_LEFT
        ),
        TemplateInfo(
            id = "pk_gulf_overseas",
            name = "Gulf & UAE Overseas",
            category = TemplateCategory.PAKISTAN,
            description = "Optimized for Saudi Arabia, Dubai, Qatar & Oman job placement agencies.",
            primaryColorHex = 0xFF0F172A,
            secondaryColorHex = 0xFF1E293B,
            accentColorHex = 0xFFEAB308,
            layoutStyle = LayoutStyle.HEADER_BANNER,
            badge = "Gulf Visa"
        ),
        TemplateInfo(
            id = "pk_medical_doctor",
            name = "Healthcare PMDC Pro",
            category = TemplateCategory.PAKISTAN,
            description = "Formulated for Pakistani Doctors, Surgeons, Dentists & Nurses.",
            primaryColorHex = 0xFF0369A1,
            secondaryColorHex = 0xFF0284C7,
            accentColorHex = 0xFF0D9488,
            layoutStyle = LayoutStyle.HEADER_BANNER
        ),
        TemplateInfo(
            id = "pk_academic_lecturer",
            name = "HEC Lecturer & Faculty",
            category = TemplateCategory.PAKISTAN,
            description = "Designed for university lecturers, research fellows, and PhD candidates.",
            primaryColorHex = 0xFF881337,
            secondaryColorHex = 0xFF9F1239,
            accentColorHex = 0xFFD4AF37,
            layoutStyle = LayoutStyle.CLASSIC_CENTERED,
            fontStyle = FontStyleType.SERIF
        ),
        TemplateInfo(
            id = "pk_corporate_sales",
            name = "FMCG Corporate Sales",
            category = TemplateCategory.PAKISTAN,
            description = "Dynamic right-sidebar layout for sales officers, distributors & BDM.",
            primaryColorHex = 0xFFC2410C,
            secondaryColorHex = 0xFFEA580C,
            accentColorHex = 0xFF1E293B,
            layoutStyle = LayoutStyle.SIDEBAR_RIGHT
        ),
        TemplateInfo(
            id = "pk_fresh_grad",
            name = "Fast Graduate Entry",
            category = TemplateCategory.PAKISTAN,
            description = "Highlights university degree, final year project, GPA & core competencies.",
            primaryColorHex = 0xFF15803D,
            secondaryColorHex = 0xFF16A34A,
            accentColorHex = 0xFFEAB308,
            layoutStyle = LayoutStyle.SIDEBAR_LEFT,
            badge = "Fresh Grad"
        )
    )

    fun getTemplateById(id: String): TemplateInfo {
        return templates.find { it.id == id } ?: templates[0]
    }
}
