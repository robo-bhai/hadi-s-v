package com.example

import com.example.data.db.CVConverters
import com.example.data.db.toDomain
import com.example.data.db.toEntity
import com.example.data.model.CVData
import com.example.data.model.Education
import com.example.data.model.Experience
import com.example.data.model.ProjectItem
import com.example.data.model.TemplateRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CVModelsAndConvertersTest {

    @Test
    fun testTemplatesCountAtLeast40() {
        assertTrue("Template registry must have at least 40 templates", TemplateRegistry.templates.size >= 40)
    }

    @Test
    fun testEducationSerialization() {
        val educations = listOf(
            Education(degree = "BSCS", institute = "FAST-NUCES", passingYear = "2024", gradeOrCgpa = "3.5"),
            Education(degree = "FSc", institute = "GCU Lahore", passingYear = "2020", gradeOrCgpa = "A+")
        )
        val json = CVConverters.educationsToJson(educations)
        val deserialized = CVConverters.jsonToEducations(json)

        assertEquals(2, deserialized.size)
        assertEquals("BSCS", deserialized[0].degree)
        assertEquals("FAST-NUCES", deserialized[0].institute)
        assertEquals("2024", deserialized[0].passingYear)
        assertEquals("GCU Lahore", deserialized[1].institute)
    }

    @Test
    fun testProjectSerialization() {
        val projects = listOf(
            ProjectItem(title = "Quick CV", techStack = "Kotlin, Compose", link = "github.com", description = "PDF maker")
        )
        val json = CVConverters.projectsToJson(projects)
        val deserialized = CVConverters.jsonToProjects(json)

        assertEquals(1, deserialized.size)
        assertEquals("Quick CV", deserialized[0].title)
        assertEquals("Kotlin, Compose", deserialized[0].techStack)
    }

    @Test
    fun testEntityDomainMapping() {
        val cv = CVData(
            id = 42,
            fullName = "Muhammad Ali",
            jobTitle = "Software Engineer",
            email = "ali@example.com",
            phone = "+92 300 1234567",
            city = "Lahore",
            templateId = "classic_harvard"
        )

        val entity = cv.toEntity()
        assertEquals(42L, entity.id)
        assertEquals("Muhammad Ali", entity.fullName)
        assertEquals("classic_harvard", entity.templateId)

        val domain = entity.toDomain()
        assertEquals("Muhammad Ali", domain.fullName)
        assertEquals("classic_harvard", domain.templateId)
        assertEquals("Harvard Traditional", domain.getActiveTemplate().name)
    }
}
