package com.example.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.data.model.CVData
import com.example.data.model.FontStyleType
import com.example.data.model.LayoutStyle
import com.example.data.model.TemplateInfo
import com.example.data.model.TemplateRegistry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfGenerator(private val context: Context) {

    companion object {
        const val PAGE_WIDTH = 595 // Standard A4 width in points (72 DPI)
        const val PAGE_HEIGHT = 842 // Standard A4 height in points
    }

    /**
     * Generates a standard A4 PDF document based on user input and selected template.
     */
    fun generatePdf(cv: CVData): Result<File> {
        return runCatching {
            val template = cv.getActiveTemplate()
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Render according to the template's layout style
            when (template.layoutStyle) {
                LayoutStyle.SIDEBAR_LEFT -> drawSidebarLeftTemplate(canvas, cv, template)
                LayoutStyle.SIDEBAR_RIGHT -> drawSidebarRightTemplate(canvas, cv, template)
                LayoutStyle.HEADER_BANNER -> drawHeaderBannerTemplate(canvas, cv, template)
                LayoutStyle.CLASSIC_CENTERED -> drawClassicCenteredTemplate(canvas, cv, template)
                LayoutStyle.TWO_COLUMN_GRID -> drawTwoColumnGridTemplate(canvas, cv, template)
                LayoutStyle.TIMELINE_ACCENT -> drawTimelineAccentTemplate(canvas, cv, template)
                LayoutStyle.MINIMAL_BORDER -> drawMinimalBorderTemplate(canvas, cv, template)
            }

            document.finishPage(page)

            // Save to app external documents directory
            val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            if (!docsDir.exists()) docsDir.mkdirs()

            val sanitizedName = cv.fullName.trim().replace(Regex("[^a-zA-Z0-9]"), "_")
                .ifBlank { "Resume" }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(docsDir, "CV_${sanitizedName}_${template.id}_$timestamp.pdf")

            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()

            file
        }
    }

    // =========================================================================
    // COLOR & TYPEFACE HELPERS
    // =========================================================================
    private fun getPrimaryColor(cv: CVData, template: TemplateInfo): Int {
        return cv.customAccentHex?.toInt() ?: template.primaryColorHex.toInt()
    }

    private fun getSecondaryColor(cv: CVData, template: TemplateInfo): Int {
        return template.secondaryColorHex.toInt()
    }

    private fun getAccentColor(cv: CVData, template: TemplateInfo): Int {
        return template.accentColorHex.toInt()
    }

    private fun getTypeface(template: TemplateInfo, isBold: Boolean = false, isItalic: Boolean = false): Typeface {
        val style = when {
            isBold && isItalic -> Typeface.BOLD_ITALIC
            isBold -> Typeface.BOLD
            isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        val family = when (template.fontStyle) {
            FontStyleType.SERIF -> Typeface.SERIF
            FontStyleType.MONO -> Typeface.MONOSPACE
            FontStyleType.SANS -> Typeface.SANS_SERIF
        }
        return Typeface.create(family, style)
    }

    // =========================================================================
    // 1. SIDEBAR LEFT (Emerald, Sapphire, Ruby, Amethyst, Terminal, etc.)
    // =========================================================================
    private fun drawSidebarLeftTemplate(canvas: Canvas, cv: CVData, template: TemplateInfo) {
        val sidebarWidth = 180f
        val margin = 18f
        val primaryColor = getPrimaryColor(cv, template)
        val accentColor = getAccentColor(cv, template)

        // Sidebar Background
        val sidebarPaint = Paint().apply {
            color = primaryColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, sidebarWidth, PAGE_HEIGHT.toFloat(), sidebarPaint)

        // Profile Photo or Monogram
        var sideY = 38f
        val photoBitmap = loadProfileBitmap(cv.photoUri, 110)
        if (photoBitmap != null) {
            val photoCenterX = sidebarWidth / 2f
            val photoCenterY = sideY + 42f
            drawCircularBitmap(canvas, photoBitmap, photoCenterX, photoCenterY, 38f)
            sideY += 92f
        } else {
            val initials = getInitials(cv.fullName)
            val badgePaint = Paint().apply {
                color = Color.argb(60, 255, 255, 255)
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(sidebarWidth / 2f, sideY + 35f, 34f, badgePaint)
            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 20f
                typeface = getTypeface(template, isBold = true)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(initials, sidebarWidth / 2f, sideY + 42f, textPaint)
            sideY += 86f
        }

        // Contact Section
        sideY = drawSidebarHeader(canvas, "CONTACT", sideY, sidebarWidth, accentColor, template)
        val sideTextPaint = Paint().apply {
            color = Color.rgb(238, 242, 246)
            textSize = 8.5f
            typeface = getTypeface(template, isBold = false)
            isAntiAlias = true
        }

        if (cv.phone.isNotBlank()) {
            sideY = drawWrappedText(canvas, "Phone:\n${cv.phone}", margin, sideY, sidebarWidth - margin * 2, sideTextPaint, 11f) + 5f
        }
        if (cv.email.isNotBlank()) {
            sideY = drawWrappedText(canvas, "Email:\n${cv.email}", margin, sideY, sidebarWidth - margin * 2, sideTextPaint, 11f) + 5f
        }
        if (cv.city.isNotBlank() || cv.address.isNotBlank()) {
            val loc = listOf(cv.city, cv.address).filter { it.isNotBlank() }.joinToString(", ")
            sideY = drawWrappedText(canvas, "Location:\n$loc", margin, sideY, sidebarWidth - margin * 2, sideTextPaint, 11f) + 5f
        }
        if (cv.cnic.isNotBlank()) {
            sideY = drawWrappedText(canvas, "CNIC:\n${cv.cnic}", margin, sideY, sidebarWidth - margin * 2, sideTextPaint, 11f) + 5f
        }
        if (cv.linkedInOrGithub.isNotBlank()) {
            sideY = drawWrappedText(canvas, "Links:\n${cv.linkedInOrGithub}", margin, sideY, sidebarWidth - margin * 2, sideTextPaint, 11f) + 5f
        }

        // Skills Section
        if (cv.skills.isNotEmpty()) {
            sideY += 8f
            sideY = drawSidebarHeader(canvas, "KEY SKILLS", sideY, sidebarWidth, accentColor, template)
            val bulletPaint = Paint().apply {
                color = accentColor
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            for (skill in cv.skills) {
                if (skill.isBlank()) continue
                canvas.drawCircle(margin + 3f, sideY - 3f, 2.2f, bulletPaint)
                sideY = drawWrappedText(canvas, skill, margin + 11f, sideY, sidebarWidth - margin * 2 - 11f, sideTextPaint, 11.5f) + 3f
                if (sideY > PAGE_HEIGHT - 100) break
            }
        }

        // Languages Section
        if (cv.languages.isNotEmpty() && sideY < PAGE_HEIGHT - 70) {
            sideY += 8f
            sideY = drawSidebarHeader(canvas, "LANGUAGES", sideY, sidebarWidth, accentColor, template)
            for (lang in cv.languages) {
                if (lang.isBlank()) continue
                sideY = drawWrappedText(canvas, "•  $lang", margin, sideY, sidebarWidth - margin * 2, sideTextPaint, 11.5f) + 3f
                if (sideY > PAGE_HEIGHT - 30) break
            }
        }

        // Main Body (Right Column)
        val mainLeft = sidebarWidth + 20f
        val mainWidth = PAGE_WIDTH - mainLeft - 20f
        var mainY = 46f

        // Name & Title
        val namePaint = Paint().apply {
            color = primaryColor
            textSize = 22f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(cv.fullName.ifBlank { "Full Name" }, mainLeft, mainY, namePaint)
        mainY += 18f

        val titlePaint = Paint().apply {
            color = Color.rgb(80, 90, 100)
            textSize = 11f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(cv.jobTitle.ifBlank { "Professional" }.uppercase(Locale.getDefault()), mainLeft, mainY, titlePaint)
        mainY += 12f

        val accentLinePaint = Paint().apply {
            color = accentColor
            strokeWidth = 2.5f
        }
        canvas.drawLine(mainLeft, mainY, mainLeft + 70f, mainY, accentLinePaint)
        mainY += 20f

        // Objective
        if (cv.objective.isNotBlank()) {
            mainY = drawMainSectionHeader(canvas, "PROFILE SUMMARY", mainLeft, mainY, mainWidth, primaryColor, template)
            val bodyPaint = Paint().apply {
                color = Color.rgb(33, 37, 41)
                textSize = 9f
                typeface = getTypeface(template, isBold = false)
                isAntiAlias = true
            }
            mainY = drawWrappedText(canvas, cv.objective, mainLeft, mainY, mainWidth, bodyPaint, 13f) + 14f
        }

        // Experience
        if (!cv.isFreshGraduate && cv.experiences.any { it.jobTitle.isNotBlank() }) {
            mainY = drawMainSectionHeader(canvas, "WORK EXPERIENCE", mainLeft, mainY, mainWidth, primaryColor, template)
            for (exp in cv.experiences) {
                if (exp.jobTitle.isBlank() && exp.company.isBlank()) continue
                if (mainY > PAGE_HEIGHT - 55) break

                val expJobPaint = Paint().apply {
                    color = primaryColor
                    textSize = 10f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText(exp.jobTitle, mainLeft, mainY, expJobPaint)

                if (exp.duration.isNotBlank()) {
                    val datePaint = Paint().apply {
                        color = Color.rgb(108, 117, 125)
                        textSize = 8.5f
                        typeface = getTypeface(template, isItalic = true)
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    canvas.drawText(exp.duration, mainLeft + mainWidth, mainY, datePaint)
                }
                mainY += 12f

                if (exp.company.isNotBlank()) {
                    val compPaint = Paint().apply {
                        color = Color.rgb(73, 80, 87)
                        textSize = 9f
                        typeface = getTypeface(template, isBold = true)
                        isAntiAlias = true
                    }
                    canvas.drawText(exp.company, mainLeft, mainY, compPaint)
                    mainY += 12f
                }

                if (exp.description.isNotBlank()) {
                    val descPaint = Paint().apply {
                        color = Color.rgb(55, 65, 81)
                        textSize = 8.5f
                        typeface = getTypeface(template, isBold = false)
                        isAntiAlias = true
                    }
                    mainY = drawWrappedText(canvas, exp.description, mainLeft, mainY, mainWidth, descPaint, 12f) + 8f
                } else {
                    mainY += 4f
                }
            }
        }

        // Projects
        if (cv.projects.isNotEmpty() && mainY < PAGE_HEIGHT - 70) {
            mainY = drawMainSectionHeader(canvas, "PROJECTS & PORTFOLIO", mainLeft, mainY, mainWidth, primaryColor, template)
            for (proj in cv.projects) {
                if (proj.title.isBlank()) continue
                if (mainY > PAGE_HEIGHT - 50) break

                val projTitlePaint = Paint().apply {
                    color = primaryColor
                    textSize = 9.5f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText(proj.title, mainLeft, mainY, projTitlePaint)
                mainY += 11f

                if (proj.techStack.isNotBlank()) {
                    val techPaint = Paint().apply {
                        color = Color.rgb(100, 116, 139)
                        textSize = 8f
                        typeface = getTypeface(template, isItalic = true)
                        isAntiAlias = true
                    }
                    canvas.drawText("Tech: ${proj.techStack}", mainLeft, mainY, techPaint)
                    mainY += 11f
                }

                if (proj.description.isNotBlank()) {
                    val descPaint = Paint().apply {
                        color = Color.rgb(55, 65, 81)
                        textSize = 8.5f
                        isAntiAlias = true
                    }
                    mainY = drawWrappedText(canvas, proj.description, mainLeft, mainY, mainWidth, descPaint, 11.5f) + 6f
                }
            }
        }

        // Education
        if (cv.educations.any { it.degree.isNotBlank() } && mainY < PAGE_HEIGHT - 50) {
            mainY = drawMainSectionHeader(canvas, "EDUCATION & QUALIFICATIONS", mainLeft, mainY, mainWidth, primaryColor, template)
            for (edu in cv.educations) {
                if (edu.degree.isBlank() && edu.institute.isBlank()) continue
                if (mainY > PAGE_HEIGHT - 40) break

                val eduTitlePaint = Paint().apply {
                    color = primaryColor
                    textSize = 10f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText(edu.degree, mainLeft, mainY, eduTitlePaint)

                if (edu.passingYear.isNotBlank()) {
                    val yearPaint = Paint().apply {
                        color = Color.rgb(108, 117, 125)
                        textSize = 8.5f
                        typeface = getTypeface(template, isItalic = true)
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    canvas.drawText(edu.passingYear, mainLeft + mainWidth, mainY, yearPaint)
                }
                mainY += 12f

                val eduInstPaint = Paint().apply {
                    color = Color.rgb(73, 80, 87)
                    textSize = 9f
                    isAntiAlias = true
                }
                val instDetails = buildString {
                    append(edu.institute)
                    if (edu.gradeOrCgpa.isNotBlank()) append("  •  Grade: ${edu.gradeOrCgpa}")
                }
                mainY = drawWrappedText(canvas, instDetails, mainLeft, mainY, mainWidth, eduInstPaint, 12f) + 6f
            }
        }
    }

    // =========================================================================
    // 2. SIDEBAR RIGHT (Sales Corporate, Modern Inverted, etc.)
    // =========================================================================
    private fun drawSidebarRightTemplate(canvas: Canvas, cv: CVData, template: TemplateInfo) {
        val sidebarWidth = 180f
        val sidebarLeft = PAGE_WIDTH - sidebarWidth
        val primaryColor = getPrimaryColor(cv, template)
        val accentColor = getAccentColor(cv, template)

        // Right Sidebar Background
        val sidebarPaint = Paint().apply {
            color = Color.rgb(245, 247, 250)
            style = Paint.Style.FILL
        }
        canvas.drawRect(sidebarLeft, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), sidebarPaint)

        // Left Main Body
        val mainLeft = 24f
        val mainWidth = sidebarLeft - 44f
        var mainY = 46f

        val namePaint = Paint().apply {
            color = primaryColor
            textSize = 24f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(cv.fullName.ifBlank { "Full Name" }, mainLeft, mainY, namePaint)
        mainY += 18f

        val titlePaint = Paint().apply {
            color = accentColor
            textSize = 11.5f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(cv.jobTitle.ifBlank { "Professional" }, mainLeft, mainY, titlePaint)
        mainY += 18f

        if (cv.objective.isNotBlank()) {
            mainY = drawMainSectionHeader(canvas, "SUMMARY", mainLeft, mainY, mainWidth, primaryColor, template)
            val bodyPaint = Paint().apply {
                color = Color.rgb(40, 40, 40)
                textSize = 9f
                isAntiAlias = true
            }
            mainY = drawWrappedText(canvas, cv.objective, mainLeft, mainY, mainWidth, bodyPaint, 13f) + 14f
        }

        if (!cv.isFreshGraduate && cv.experiences.any { it.jobTitle.isNotBlank() }) {
            mainY = drawMainSectionHeader(canvas, "EXPERIENCE", mainLeft, mainY, mainWidth, primaryColor, template)
            for (exp in cv.experiences) {
                if (exp.jobTitle.isBlank()) continue
                if (mainY > PAGE_HEIGHT - 60) break
                val jobPaint = Paint().apply {
                    color = primaryColor
                    textSize = 10f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText(exp.jobTitle, mainLeft, mainY, jobPaint)
                mainY += 12f
                val compPaint = Paint().apply {
                    color = Color.rgb(80, 80, 80)
                    textSize = 9f
                    isAntiAlias = true
                }
                canvas.drawText("${exp.company} (${exp.duration})", mainLeft, mainY, compPaint)
                mainY += 12f
                if (exp.description.isNotBlank()) {
                    mainY = drawWrappedText(canvas, exp.description, mainLeft, mainY, mainWidth, compPaint, 12f) + 8f
                }
            }
        }

        if (cv.educations.any { it.degree.isNotBlank() } && mainY < PAGE_HEIGHT - 50) {
            mainY = drawMainSectionHeader(canvas, "EDUCATION", mainLeft, mainY, mainWidth, primaryColor, template)
            for (edu in cv.educations) {
                if (edu.degree.isBlank()) continue
                val eduPaint = Paint().apply {
                    color = primaryColor
                    textSize = 9.5f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText(edu.degree, mainLeft, mainY, eduPaint)
                mainY += 11f
                val instPaint = Paint().apply {
                    color = Color.rgb(80, 80, 80)
                    textSize = 8.5f
                    isAntiAlias = true
                }
                canvas.drawText("${edu.institute} - ${edu.passingYear}", mainLeft, mainY, instPaint)
                mainY += 14f
            }
        }

        // Right Sidebar Content
        var sideY = 46f
        val sideInnerLeft = sidebarLeft + 16f
        val sideInnerWidth = sidebarWidth - 32f

        sideY = drawSidebarHeader(canvas, "CONTACT", sideY, sidebarWidth, primaryColor, template, isDarkBackground = false, startX = sideInnerLeft)
        val sidePaint = Paint().apply {
            color = Color.rgb(50, 50, 50)
            textSize = 8.5f
            isAntiAlias = true
        }
        if (cv.phone.isNotBlank()) sideY = drawWrappedText(canvas, "Phone:\n${cv.phone}", sideInnerLeft, sideY, sideInnerWidth, sidePaint, 11f) + 6f
        if (cv.email.isNotBlank()) sideY = drawWrappedText(canvas, "Email:\n${cv.email}", sideInnerLeft, sideY, sideInnerWidth, sidePaint, 11f) + 6f
        if (cv.city.isNotBlank()) sideY = drawWrappedText(canvas, "City:\n${cv.city}", sideInnerLeft, sideY, sideInnerWidth, sidePaint, 11f) + 6f

        if (cv.skills.isNotEmpty()) {
            sideY += 8f
            sideY = drawSidebarHeader(canvas, "SKILLS", sideY, sidebarWidth, primaryColor, template, isDarkBackground = false, startX = sideInnerLeft)
            for (skill in cv.skills) {
                sideY = drawWrappedText(canvas, "• $skill", sideInnerLeft, sideY, sideInnerWidth, sidePaint, 11f) + 3f
                if (sideY > PAGE_HEIGHT - 60) break
            }
        }

        if (cv.languages.isNotEmpty() && sideY < PAGE_HEIGHT - 50) {
            sideY += 8f
            sideY = drawSidebarHeader(canvas, "LANGUAGES", sideY, sidebarWidth, primaryColor, template, isDarkBackground = false, startX = sideInnerLeft)
            for (lang in cv.languages) {
                sideY = drawWrappedText(canvas, "• $lang", sideInnerLeft, sideY, sideInnerWidth, sidePaint, 11f) + 3f
            }
        }
    }

    // =========================================================================
    // 3. HEADER BANNER (Teal Horizon, Gulf Overseas, PMDC Medical, Octo Dev)
    // =========================================================================
    private fun drawHeaderBannerTemplate(canvas: Canvas, cv: CVData, template: TemplateInfo) {
        val bannerHeight = 110f
        val margin = 26f
        val contentWidth = PAGE_WIDTH - (margin * 2)
        val primaryColor = getPrimaryColor(cv, template)
        val accentColor = getAccentColor(cv, template)

        // Draw top banner
        val bannerPaint = Paint().apply {
            color = primaryColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), bannerHeight, bannerPaint)

        // Candidate Name & Title inside banner
        val namePaint = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(cv.fullName.ifBlank { "Candidate Name" }, margin, 42f, namePaint)

        val titlePaint = Paint().apply {
            color = Color.rgb(235, 240, 245)
            textSize = 11f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(cv.jobTitle.ifBlank { "Professional" }.uppercase(Locale.getDefault()), margin, 60f, titlePaint)

        // Contact info line inside banner
        val contactItems = listOfNotNull(
            cv.phone.takeIf { it.isNotBlank() },
            cv.email.takeIf { it.isNotBlank() },
            cv.city.takeIf { it.isNotBlank() },
            cv.cnic.takeIf { it.isNotBlank() }?.let { "CNIC: $it" }
        )
        val contactLine = contactItems.joinToString("   •   ")
        val contactPaint = Paint().apply {
            color = Color.rgb(220, 230, 240)
            textSize = 8.5f
            isAntiAlias = true
        }
        canvas.drawText(contactLine, margin, 80f, contactPaint)

        // Accent strip under banner
        val stripPaint = Paint().apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, bannerHeight, PAGE_WIDTH.toFloat(), bannerHeight + 4f, stripPaint)

        var y = bannerHeight + 24f

        // Profile Summary
        if (cv.objective.isNotBlank()) {
            y = drawMainSectionHeader(canvas, "PROFILE SUMMARY", margin, y, contentWidth, primaryColor, template)
            val bodyPaint = Paint().apply {
                color = Color.rgb(40, 40, 40)
                textSize = 9f
                isAntiAlias = true
            }
            y = drawWrappedText(canvas, cv.objective, margin, y, contentWidth, bodyPaint, 13f) + 14f
        }

        // Work Experience
        if (!cv.isFreshGraduate && cv.experiences.any { it.jobTitle.isNotBlank() }) {
            y = drawMainSectionHeader(canvas, "WORK EXPERIENCE", margin, y, contentWidth, primaryColor, template)
            for (exp in cv.experiences) {
                if (exp.jobTitle.isBlank()) continue
                if (y > PAGE_HEIGHT - 60) break
                val jobPaint = Paint().apply {
                    color = primaryColor
                    textSize = 10f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText(exp.jobTitle, margin, y, jobPaint)
                if (exp.duration.isNotBlank()) {
                    val datePaint = Paint().apply {
                        color = Color.rgb(100, 100, 100)
                        textSize = 8.5f
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    canvas.drawText(exp.duration, PAGE_WIDTH - margin, y, datePaint)
                }
                y += 12f
                val compPaint = Paint().apply {
                    color = Color.rgb(70, 70, 70)
                    textSize = 9f
                    isAntiAlias = true
                }
                canvas.drawText(exp.company, margin, y, compPaint)
                y += 12f
                if (exp.description.isNotBlank()) {
                    y = drawWrappedText(canvas, exp.description, margin, y, contentWidth, compPaint, 12f) + 8f
                }
            }
        }

        // Education
        if (cv.educations.any { it.degree.isNotBlank() } && y < PAGE_HEIGHT - 60) {
            y = drawMainSectionHeader(canvas, "EDUCATION", margin, y, contentWidth, primaryColor, template)
            for (edu in cv.educations) {
                if (edu.degree.isBlank()) continue
                if (y > PAGE_HEIGHT - 45) break
                val eduPaint = Paint().apply {
                    color = primaryColor
                    textSize = 9.5f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText(edu.degree, margin, y, eduPaint)
                y += 11f
                val instPaint = Paint().apply {
                    color = Color.rgb(70, 70, 70)
                    textSize = 8.5f
                    isAntiAlias = true
                }
                canvas.drawText("${edu.institute} — ${edu.passingYear} (${edu.gradeOrCgpa})", margin, y, instPaint)
                y += 12f
            }
        }

        // Skills & Languages footer tags
        if (cv.skills.isNotEmpty() && y < PAGE_HEIGHT - 40) {
            y = drawMainSectionHeader(canvas, "SKILLS & EXPERTISE", margin, y, contentWidth, primaryColor, template)
            val skillsText = cv.skills.joinToString("   •   ")
            val skillPaint = Paint().apply {
                color = Color.rgb(50, 50, 50)
                textSize = 8.5f
                isAntiAlias = true
            }
            y = drawWrappedText(canvas, skillsText, margin, y, contentWidth, skillPaint, 12f) + 6f
        }
    }

    // =========================================================================
    // 4. CLASSIC CENTERED (Harvard, Oxford, PPSC Govt Special, Legal, etc.)
    // =========================================================================
    private fun drawClassicCenteredTemplate(canvas: Canvas, cv: CVData, template: TemplateInfo) {
        val margin = 36f
        val contentWidth = PAGE_WIDTH - (margin * 2)
        val primaryColor = getPrimaryColor(cv, template)
        val accentColor = getAccentColor(cv, template)
        var y = 46f

        // Centered Name
        val namePaint = Paint().apply {
            color = primaryColor
            textSize = 22f
            typeface = getTypeface(template, isBold = true)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(cv.fullName.ifBlank { "Full Name" }.uppercase(Locale.getDefault()), PAGE_WIDTH / 2f, y, namePaint)
        y += 16f

        // Centered Job Title
        val titlePaint = Paint().apply {
            color = accentColor
            textSize = 11f
            typeface = getTypeface(template, isBold = true)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(cv.jobTitle.ifBlank { "Curriculum Vitae" }, PAGE_WIDTH / 2f, y, titlePaint)
        y += 14f

        // Centered Contact Line
        val contactList = listOfNotNull(
            cv.phone.takeIf { it.isNotBlank() },
            cv.email.takeIf { it.isNotBlank() },
            cv.city.takeIf { it.isNotBlank() },
            cv.cnic.takeIf { it.isNotBlank() }?.let { "CNIC: $it" }
        )
        val contactStr = contactList.joinToString("   |   ")
        val contactPaint = Paint().apply {
            color = Color.rgb(80, 85, 90)
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(contactStr, PAGE_WIDTH / 2f, y, contactPaint)
        y += 12f

        // Double Horizontal Separator
        val rulePaint = Paint().apply {
            color = primaryColor
            strokeWidth = 1.2f
        }
        canvas.drawLine(margin, y, PAGE_WIDTH - margin, y, rulePaint)
        y += 2.5f
        rulePaint.strokeWidth = 0.5f
        canvas.drawLine(margin, y, PAGE_WIDTH - margin, y, rulePaint)
        y += 18f

        // Summary
        if (cv.objective.isNotBlank()) {
            y = drawClassicSectionHeader(canvas, "PROFESSIONAL SUMMARY", margin, y, contentWidth, primaryColor, template)
            val bodyPaint = Paint().apply {
                color = Color.rgb(35, 35, 35)
                textSize = 9f
                isAntiAlias = true
            }
            y = drawWrappedText(canvas, cv.objective, margin, y, contentWidth, bodyPaint, 13f) + 14f
        }

        // Experience
        if (!cv.isFreshGraduate && cv.experiences.any { it.jobTitle.isNotBlank() }) {
            y = drawClassicSectionHeader(canvas, "PROFESSIONAL EXPERIENCE", margin, y, contentWidth, primaryColor, template)
            for (exp in cv.experiences) {
                if (exp.jobTitle.isBlank()) continue
                if (y > PAGE_HEIGHT - 60) break

                val jobPaint = Paint().apply {
                    color = Color.rgb(20, 20, 20)
                    textSize = 10f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText(exp.jobTitle, margin, y, jobPaint)

                if (exp.duration.isNotBlank()) {
                    val datePaint = Paint().apply {
                        color = Color.rgb(90, 90, 90)
                        textSize = 8.5f
                        typeface = getTypeface(template, isItalic = true)
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    canvas.drawText(exp.duration, PAGE_WIDTH - margin, y, datePaint)
                }
                y += 12f

                if (exp.company.isNotBlank()) {
                    val compPaint = Paint().apply {
                        color = primaryColor
                        textSize = 9f
                        typeface = getTypeface(template, isBold = true)
                        isAntiAlias = true
                    }
                    canvas.drawText(exp.company, margin, y, compPaint)
                    y += 12f
                }

                if (exp.description.isNotBlank()) {
                    val descPaint = Paint().apply {
                        color = Color.rgb(50, 50, 50)
                        textSize = 8.5f
                        isAntiAlias = true
                    }
                    y = drawWrappedText(canvas, exp.description, margin, y, contentWidth, descPaint, 12f) + 8f
                }
            }
        }

        // Education
        if (cv.educations.any { it.degree.isNotBlank() } && y < PAGE_HEIGHT - 60) {
            y = drawClassicSectionHeader(canvas, "EDUCATION & CREDENTIALS", margin, y, contentWidth, primaryColor, template)
            for (edu in cv.educations) {
                if (edu.degree.isBlank()) continue
                if (y > PAGE_HEIGHT - 45) break
                val eduPaint = Paint().apply {
                    color = Color.rgb(20, 20, 20)
                    textSize = 9.5f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText(edu.degree, margin, y, eduPaint)
                if (edu.passingYear.isNotBlank()) {
                    val yearPaint = Paint().apply {
                        color = Color.rgb(90, 90, 90)
                        textSize = 8.5f
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    canvas.drawText(edu.passingYear, PAGE_WIDTH - margin, y, yearPaint)
                }
                y += 12f
                val instPaint = Paint().apply {
                    color = Color.rgb(70, 70, 70)
                    textSize = 8.5f
                    isAntiAlias = true
                }
                canvas.drawText("${edu.institute} — Grade/CGPA: ${edu.gradeOrCgpa}", margin, y, instPaint)
                y += 12f
            }
        }

        // Skills & Languages
        if (cv.skills.isNotEmpty() && y < PAGE_HEIGHT - 45) {
            y = drawClassicSectionHeader(canvas, "CORE COMPETENCIES", margin, y, contentWidth, primaryColor, template)
            val skillPaint = Paint().apply {
                color = Color.rgb(40, 40, 40)
                textSize = 8.5f
                isAntiAlias = true
            }
            y = drawWrappedText(canvas, "Skills: " + cv.skills.joinToString("  •  "), margin, y, contentWidth, skillPaint, 12f) + 6f
            if (cv.languages.isNotEmpty()) {
                y = drawWrappedText(canvas, "Languages: " + cv.languages.joinToString("  •  "), margin, y, contentWidth, skillPaint, 12f) + 6f
            }
        }
    }

    // =========================================================================
    // 5. TWO COLUMN GRID (Swiss Grid, DevOps Cloud, FastTrack, Data Scientist)
    // =========================================================================
    private fun drawTwoColumnGridTemplate(canvas: Canvas, cv: CVData, template: TemplateInfo) {
        val margin = 26f
        val primaryColor = getPrimaryColor(cv, template)
        val accentColor = getAccentColor(cv, template)
        var y = 38f

        // Top Header
        val namePaint = Paint().apply {
            color = primaryColor
            textSize = 20f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(cv.fullName.ifBlank { "Full Name" }, margin, y, namePaint)
        y += 15f

        val titlePaint = Paint().apply {
            color = accentColor
            textSize = 10.5f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(cv.jobTitle.ifBlank { "Candidate" }.uppercase(Locale.getDefault()), margin, y, titlePaint)
        y += 13f

        val contactStr = listOfNotNull(cv.phone, cv.email, cv.city).filter { it.isNotBlank() }.joinToString("   |   ")
        val contactPaint = Paint().apply {
            color = Color.rgb(100, 100, 100)
            textSize = 8.5f
            isAntiAlias = true
        }
        canvas.drawText(contactStr, margin, y, contactPaint)
        y += 10f

        // Divider
        val linePaint = Paint().apply {
            color = Color.rgb(220, 225, 230)
            strokeWidth = 1f
        }
        canvas.drawLine(margin, y, PAGE_WIDTH - margin, y, linePaint)
        y += 16f

        // Two Column Split
        val colGap = 16f
        val leftWidth = (PAGE_WIDTH - margin * 2 - colGap) * 0.58f
        val rightWidth = (PAGE_WIDTH - margin * 2 - colGap) * 0.42f
        val leftX = margin
        val rightX = margin + leftWidth + colGap

        var leftY = y
        var rightY = y

        // Left Column: Summary + Experience
        if (cv.objective.isNotBlank()) {
            leftY = drawCompactSectionHeader(canvas, "SUMMARY", leftX, leftY, leftWidth, primaryColor, template)
            val bodyPaint = Paint().apply {
                color = Color.rgb(40, 40, 40)
                textSize = 8.5f
                isAntiAlias = true
            }
            leftY = drawWrappedText(canvas, cv.objective, leftX, leftY, leftWidth, bodyPaint, 11.5f) + 12f
        }

        if (!cv.isFreshGraduate && cv.experiences.any { it.jobTitle.isNotBlank() }) {
            leftY = drawCompactSectionHeader(canvas, "EXPERIENCE", leftX, leftY, leftWidth, primaryColor, template)
            for (exp in cv.experiences) {
                if (exp.jobTitle.isBlank()) continue
                if (leftY > PAGE_HEIGHT - 40) break
                val jobPaint = Paint().apply {
                    color = primaryColor
                    textSize = 9.5f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText(exp.jobTitle, leftX, leftY, jobPaint)
                leftY += 11f
                val compPaint = Paint().apply {
                    color = Color.rgb(90, 90, 90)
                    textSize = 8f
                    isAntiAlias = true
                }
                canvas.drawText("${exp.company} (${exp.duration})", leftX, leftY, compPaint)
                leftY += 11f
                if (exp.description.isNotBlank()) {
                    leftY = drawWrappedText(canvas, exp.description, leftX, leftY, leftWidth, compPaint, 11f) + 6f
                }
            }
        }

        // Right Column: Education + Skills + Certifications
        if (cv.educations.any { it.degree.isNotBlank() }) {
            rightY = drawCompactSectionHeader(canvas, "EDUCATION", rightX, rightY, rightWidth, primaryColor, template)
            for (edu in cv.educations) {
                if (edu.degree.isBlank()) continue
                val degPaint = Paint().apply {
                    color = primaryColor
                    textSize = 9f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText(edu.degree, rightX, rightY, degPaint)
                rightY += 10f
                val instPaint = Paint().apply {
                    color = Color.rgb(80, 80, 80)
                    textSize = 8f
                    isAntiAlias = true
                }
                canvas.drawText("${edu.institute} - ${edu.passingYear}", rightX, rightY, instPaint)
                rightY += 12f
            }
        }

        if (cv.skills.isNotEmpty()) {
            rightY = drawCompactSectionHeader(canvas, "SKILLS", rightX, rightY, rightWidth, primaryColor, template)
            val skillPaint = Paint().apply {
                color = Color.rgb(40, 40, 40)
                textSize = 8f
                isAntiAlias = true
            }
            for (skill in cv.skills) {
                rightY = drawWrappedText(canvas, "▪ $skill", rightX, rightY, rightWidth, skillPaint, 10.5f) + 2f
                if (rightY > PAGE_HEIGHT - 40) break
            }
            rightY += 8f
        }

        if (cv.languages.isNotEmpty() && rightY < PAGE_HEIGHT - 30) {
            rightY = drawCompactSectionHeader(canvas, "LANGUAGES", rightX, rightY, rightWidth, primaryColor, template)
            val langPaint = Paint().apply {
                color = Color.rgb(40, 40, 40)
                textSize = 8f
                isAntiAlias = true
            }
            for (lang in cv.languages) {
                rightY = drawWrappedText(canvas, "▪ $lang", rightX, rightY, rightWidth, langPaint, 10.5f) + 2f
            }
        }
    }

    // =========================================================================
    // 6. TIMELINE ACCENT (Cyber Matrix, Mobile App Dev, etc.)
    // =========================================================================
    private fun drawTimelineAccentTemplate(canvas: Canvas, cv: CVData, template: TemplateInfo) {
        val margin = 28f
        val contentWidth = PAGE_WIDTH - margin * 2
        val primaryColor = getPrimaryColor(cv, template)
        val accentColor = getAccentColor(cv, template)
        var y = 44f

        val namePaint = Paint().apply {
            color = primaryColor
            textSize = 24f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(cv.fullName.ifBlank { "Candidate Name" }, margin, y, namePaint)
        y += 18f

        val titlePaint = Paint().apply {
            color = accentColor
            textSize = 11.5f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(cv.jobTitle.ifBlank { "Developer" }, margin, y, titlePaint)
        y += 14f

        val contactLine = listOfNotNull(cv.phone, cv.email, cv.city, cv.linkedInOrGithub).filter { it.isNotBlank() }.joinToString("   •   ")
        val contactPaint = Paint().apply {
            color = Color.rgb(90, 90, 90)
            textSize = 8.5f
            isAntiAlias = true
        }
        canvas.drawText(contactLine, margin, y, contactPaint)
        y += 18f

        // Timeline Spine
        val timelineX = margin + 8f
        val timelineContentX = timelineX + 16f
        val timelineContentWidth = PAGE_WIDTH - timelineContentX - margin

        if (cv.objective.isNotBlank()) {
            y = drawMainSectionHeader(canvas, "OVERVIEW", margin, y, contentWidth, primaryColor, template)
            val bodyPaint = Paint().apply {
                color = Color.rgb(40, 40, 40)
                textSize = 9f
                isAntiAlias = true
            }
            y = drawWrappedText(canvas, cv.objective, margin, y, contentWidth, bodyPaint, 13f) + 16f
        }

        if (!cv.isFreshGraduate && cv.experiences.any { it.jobTitle.isNotBlank() }) {
            y = drawMainSectionHeader(canvas, "CAREER TIMELINE", margin, y, contentWidth, primaryColor, template)
            for (exp in cv.experiences) {
                if (exp.jobTitle.isBlank()) continue
                if (y > PAGE_HEIGHT - 60) break

                // Node Circle
                val nodePaint = Paint().apply {
                    color = accentColor
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawCircle(timelineX, y - 4f, 4f, nodePaint)

                val jobPaint = Paint().apply {
                    color = primaryColor
                    textSize = 10f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText("${exp.jobTitle} — ${exp.company}", timelineContentX, y, jobPaint)
                if (exp.duration.isNotBlank()) {
                    val datePaint = Paint().apply {
                        color = Color.rgb(100, 100, 100)
                        textSize = 8.5f
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    canvas.drawText(exp.duration, PAGE_WIDTH - margin, y, datePaint)
                }
                y += 12f

                if (exp.description.isNotBlank()) {
                    val descPaint = Paint().apply {
                        color = Color.rgb(55, 65, 81)
                        textSize = 8.5f
                        isAntiAlias = true
                    }
                    y = drawWrappedText(canvas, exp.description, timelineContentX, y, timelineContentWidth, descPaint, 12f) + 8f
                }
            }
        }

        // Education
        if (cv.educations.any { it.degree.isNotBlank() } && y < PAGE_HEIGHT - 60) {
            y = drawMainSectionHeader(canvas, "ACADEMIC BACKGROUND", margin, y, contentWidth, primaryColor, template)
            for (edu in cv.educations) {
                if (edu.degree.isBlank()) continue
                val nodePaint = Paint().apply {
                    color = primaryColor
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawCircle(timelineX, y - 4f, 3.5f, nodePaint)
                val eduPaint = Paint().apply {
                    color = primaryColor
                    textSize = 9.5f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText("${edu.degree} — ${edu.institute} (${edu.passingYear})", timelineContentX, y, eduPaint)
                y += 14f
            }
        }

        if (cv.skills.isNotEmpty() && y < PAGE_HEIGHT - 40) {
            y = drawMainSectionHeader(canvas, "SKILLS & TECHNOLOGIES", margin, y, contentWidth, primaryColor, template)
            val skillPaint = Paint().apply {
                color = Color.rgb(40, 40, 40)
                textSize = 8.5f
                isAntiAlias = true
            }
            y = drawWrappedText(canvas, cv.skills.joinToString("   •   "), margin, y, contentWidth, skillPaint, 12f) + 6f
        }
    }

    // =========================================================================
    // 7. MINIMAL BORDER (Clean Code Mono, Ultra Condensed, Micro Bullets)
    // =========================================================================
    private fun drawMinimalBorderTemplate(canvas: Canvas, cv: CVData, template: TemplateInfo) {
        val margin = 24f
        val contentWidth = PAGE_WIDTH - margin * 2
        val primaryColor = getPrimaryColor(cv, template)
        val accentColor = getAccentColor(cv, template)

        // Outer Page Border
        val borderPaint = Paint().apply {
            color = Color.rgb(210, 215, 220)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(margin, margin, PAGE_WIDTH - margin, PAGE_HEIGHT - margin, borderPaint)

        var y = margin + 28f
        val innerMargin = margin + 14f
        val innerWidth = contentWidth - 28f

        val namePaint = Paint().apply {
            color = primaryColor
            textSize = 20f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(cv.fullName.ifBlank { "Full Name" }, innerMargin, y, namePaint)
        y += 14f

        val titlePaint = Paint().apply {
            color = accentColor
            textSize = 10f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(cv.jobTitle.ifBlank { "Professional" }, innerMargin, y, titlePaint)
        y += 12f

        val contactStr = listOfNotNull(cv.phone, cv.email, cv.city, cv.cnic).filter { it.isNotBlank() }.joinToString(" | ")
        val contactPaint = Paint().apply {
            color = Color.rgb(80, 80, 80)
            textSize = 8f
            isAntiAlias = true
        }
        canvas.drawText(contactStr, innerMargin, y, contactPaint)
        y += 14f

        val rulePaint = Paint().apply {
            color = Color.rgb(220, 220, 220)
            strokeWidth = 0.8f
        }
        canvas.drawLine(innerMargin, y, innerMargin + innerWidth, y, rulePaint)
        y += 14f

        if (cv.objective.isNotBlank()) {
            y = drawCompactSectionHeader(canvas, "PROFILE", innerMargin, y, innerWidth, primaryColor, template)
            val bodyPaint = Paint().apply {
                color = Color.rgb(40, 40, 40)
                textSize = 8.5f
                isAntiAlias = true
            }
            y = drawWrappedText(canvas, cv.objective, innerMargin, y, innerWidth, bodyPaint, 11.5f) + 10f
        }

        if (!cv.isFreshGraduate && cv.experiences.any { it.jobTitle.isNotBlank() }) {
            y = drawCompactSectionHeader(canvas, "EXPERIENCE", innerMargin, y, innerWidth, primaryColor, template)
            for (exp in cv.experiences) {
                if (exp.jobTitle.isBlank()) continue
                if (y > PAGE_HEIGHT - 60) break
                val jobPaint = Paint().apply {
                    color = primaryColor
                    textSize = 9.5f
                    typeface = getTypeface(template, isBold = true)
                    isAntiAlias = true
                }
                canvas.drawText("${exp.jobTitle} - ${exp.company}", innerMargin, y, jobPaint)
                y += 11f
                if (exp.description.isNotBlank()) {
                    val descPaint = Paint().apply {
                        color = Color.rgb(70, 70, 70)
                        textSize = 8f
                        isAntiAlias = true
                    }
                    y = drawWrappedText(canvas, exp.description, innerMargin, y, innerWidth, descPaint, 11f) + 6f
                }
            }
        }

        if (cv.educations.any { it.degree.isNotBlank() } && y < PAGE_HEIGHT - 50) {
            y = drawCompactSectionHeader(canvas, "EDUCATION", innerMargin, y, innerWidth, primaryColor, template)
            for (edu in cv.educations) {
                if (edu.degree.isBlank()) continue
                val eduPaint = Paint().apply {
                    color = primaryColor
                    textSize = 9f
                    isAntiAlias = true
                }
                canvas.drawText("${edu.degree} - ${edu.institute} (${edu.passingYear})", innerMargin, y, eduPaint)
                y += 12f
            }
        }

        if (cv.skills.isNotEmpty() && y < PAGE_HEIGHT - 40) {
            y = drawCompactSectionHeader(canvas, "SKILLS", innerMargin, y, innerWidth, primaryColor, template)
            val skillPaint = Paint().apply {
                color = Color.rgb(40, 40, 40)
                textSize = 8f
                isAntiAlias = true
            }
            y = drawWrappedText(canvas, cv.skills.joinToString(", "), innerMargin, y, innerWidth, skillPaint, 11f) + 6f
        }
    }

    // =========================================================================
    // GENERAL DRAWING UTILITIES
    // =========================================================================

    private fun drawSidebarHeader(
        canvas: Canvas,
        title: String,
        y: Float,
        width: Float,
        color: Int,
        template: TemplateInfo,
        isDarkBackground: Boolean = true,
        startX: Float = 18f
    ): Float {
        val paint = Paint().apply {
            this.color = color
            textSize = 9.5f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(title, startX, y, paint)
        val linePaint = Paint().apply {
            this.color = if (isDarkBackground) Color.argb(90, 255, 255, 255) else Color.rgb(210, 215, 220)
            strokeWidth = 0.8f
        }
        canvas.drawLine(startX, y + 4f, if (startX > 30) startX + width - 36f else width - 18f, y + 4f, linePaint)
        return y + 14f
    }

    private fun drawMainSectionHeader(
        canvas: Canvas,
        title: String,
        x: Float,
        y: Float,
        width: Float,
        color: Int,
        template: TemplateInfo
    ): Float {
        val paint = Paint().apply {
            this.color = color
            textSize = 10.5f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(title, x, y, paint)
        val linePaint = Paint().apply {
            this.color = Color.rgb(220, 225, 230)
            strokeWidth = 1f
        }
        canvas.drawLine(x, y + 3.5f, x + width, y + 3.5f, linePaint)
        return y + 15f
    }

    private fun drawClassicSectionHeader(
        canvas: Canvas,
        title: String,
        x: Float,
        y: Float,
        width: Float,
        color: Int,
        template: TemplateInfo
    ): Float {
        val paint = Paint().apply {
            this.color = color
            textSize = 10.5f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(title, x, y, paint)
        val linePaint = Paint().apply {
            this.color = color
            strokeWidth = 1.1f
        }
        canvas.drawLine(x, y + 3f, x + width, y + 3f, linePaint)
        return y + 15f
    }

    private fun drawCompactSectionHeader(
        canvas: Canvas,
        title: String,
        x: Float,
        y: Float,
        width: Float,
        color: Int,
        template: TemplateInfo
    ): Float {
        val paint = Paint().apply {
            this.color = color
            textSize = 9.5f
            typeface = getTypeface(template, isBold = true)
            isAntiAlias = true
        }
        canvas.drawText(title, x, y, paint)
        val linePaint = Paint().apply {
            this.color = Color.rgb(210, 215, 220)
            strokeWidth = 0.8f
        }
        canvas.drawLine(x, y + 2.5f, x + width, y + 2.5f, linePaint)
        return y + 12f
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineSpacing: Float
    ): Float {
        var currentY = startY
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            val line = StringBuilder()
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(testLine) <= maxWidth) {
                    line.clear()
                    line.append(testLine)
                } else {
                    canvas.drawText(line.toString(), x, currentY, paint)
                    currentY += lineSpacing
                    line.clear()
                    line.append(word)
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line.toString(), x, currentY, paint)
                currentY += lineSpacing
            }
        }
        return currentY
    }

    private fun loadProfileBitmap(uriString: String?, targetSize: Int): Bitmap? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val original = BitmapFactory.decodeStream(stream) ?: return null
                Bitmap.createScaledBitmap(original, targetSize, targetSize, true)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun drawCircularBitmap(canvas: Canvas, bitmap: Bitmap, cx: Float, cy: Float, radius: Float) {
        val path = Path().apply {
            addCircle(cx, cy, radius, Path.Direction.CCW)
        }
        canvas.save()
        canvas.clipPath(path)
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        canvas.drawBitmap(bitmap, null, rect, null)
        canvas.restore()

        val ringPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawCircle(cx, cy, radius, ringPaint)
    }

    private fun getInitials(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "CV"
            parts.size == 1 -> parts[0].take(2).uppercase(Locale.getDefault())
            else -> ("" + parts[0].first() + parts[1].first()).uppercase(Locale.getDefault())
        }
    }

    // =========================================================================
    // INTENT DISPATCHERS
    // =========================================================================

    fun getFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun createShareIntent(file: File): Intent {
        val uri = getFileUri(file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Resume - ${file.name}")
            putExtra(Intent.EXTRA_TEXT, "Please find attached my Curriculum Vitae created with Quick CV Maker.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createWhatsAppShareIntent(file: File): Intent {
        val uri = getFileUri(file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Assalam-o-Alaikum, please find attached my CV created with Quick CV Maker.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.whatsapp")
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            intent
        } else {
            createShareIntent(file)
        }
    }

    fun createViewIntent(file: File): Intent {
        val uri = getFileUri(file)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
