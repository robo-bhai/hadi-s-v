package com.example.data.db

import com.example.data.model.CertificationItem
import com.example.data.model.CVData
import com.example.data.model.Education
import com.example.data.model.Experience
import com.example.data.model.ProjectItem
import org.json.JSONArray
import org.json.JSONObject

object CVConverters {
    fun educationsToJson(list: List<Education>): String {
        val array = JSONArray()
        list.forEach { edu ->
            val obj = JSONObject().apply {
                put("id", edu.id)
                put("degree", edu.degree)
                put("institute", edu.institute)
                put("passingYear", edu.passingYear)
                put("gradeOrCgpa", edu.gradeOrCgpa)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToEducations(json: String): List<Education> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<Education>()
        return try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Education(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        degree = obj.optString("degree", ""),
                        institute = obj.optString("institute", ""),
                        passingYear = obj.optString("passingYear", ""),
                        gradeOrCgpa = obj.optString("gradeOrCgpa", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun experiencesToJson(list: List<Experience>): String {
        val array = JSONArray()
        list.forEach { exp ->
            val obj = JSONObject().apply {
                put("id", exp.id)
                put("jobTitle", exp.jobTitle)
                put("company", exp.company)
                put("duration", exp.duration)
                put("description", exp.description)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToExperiences(json: String): List<Experience> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<Experience>()
        return try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Experience(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        jobTitle = obj.optString("jobTitle", ""),
                        company = obj.optString("company", ""),
                        duration = obj.optString("duration", ""),
                        description = obj.optString("description", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun projectsToJson(list: List<ProjectItem>): String {
        val array = JSONArray()
        list.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("title", p.title)
                put("techStack", p.techStack)
                put("link", p.link)
                put("description", p.description)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToProjects(json: String): List<ProjectItem> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<ProjectItem>()
        return try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ProjectItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        techStack = obj.optString("techStack", ""),
                        link = obj.optString("link", ""),
                        description = obj.optString("description", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun certificationsToJson(list: List<CertificationItem>): String {
        val array = JSONArray()
        list.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("issuer", c.issuer)
                put("year", c.year)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToCertifications(json: String): List<CertificationItem> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<CertificationItem>()
        return try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CertificationItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = obj.optString("name", ""),
                        issuer = obj.optString("issuer", ""),
                        year = obj.optString("year", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun stringListToJson(list: List<String>): String {
        val array = JSONArray()
        list.forEach { array.put(it) }
        return array.toString()
    }

    fun jsonToStringList(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<String>()
        return try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}

fun CVData.toEntity(): CVProfileEntity {
    return CVProfileEntity(
        id = this.id,
        title = this.title.ifBlank { "My Professional CV" },
        fullName = this.fullName,
        jobTitle = this.jobTitle,
        email = this.email,
        phone = this.phone,
        city = this.city,
        address = this.address,
        cnic = this.cnic,
        linkedInOrGithub = this.linkedInOrGithub,
        photoUri = this.photoUri,
        objective = this.objective,
        educationJson = CVConverters.educationsToJson(this.educations),
        experienceJson = CVConverters.experiencesToJson(this.experiences),
        isFreshGraduate = this.isFreshGraduate,
        projectsJson = CVConverters.projectsToJson(this.projects),
        certificationsJson = CVConverters.certificationsToJson(this.certifications),
        skillsJson = CVConverters.stringListToJson(this.skills),
        languagesJson = CVConverters.stringListToJson(this.languages),
        templateId = this.templateId,
        customAccentHex = this.customAccentHex,
        updatedAt = this.updatedAt
    )
}

fun CVProfileEntity.toDomain(): CVData {
    return CVData(
        id = this.id,
        title = this.title,
        fullName = this.fullName,
        jobTitle = this.jobTitle,
        email = this.email,
        phone = this.phone,
        city = this.city,
        address = this.address,
        cnic = this.cnic,
        linkedInOrGithub = this.linkedInOrGithub,
        photoUri = this.photoUri,
        objective = this.objective,
        educations = CVConverters.jsonToEducations(this.educationJson),
        experiences = CVConverters.jsonToExperiences(this.experienceJson),
        isFreshGraduate = this.isFreshGraduate,
        projects = CVConverters.jsonToProjects(this.projectsJson),
        certifications = CVConverters.jsonToCertifications(this.certificationsJson),
        skills = CVConverters.jsonToStringList(this.skillsJson),
        languages = CVConverters.jsonToStringList(this.languagesJson),
        templateId = this.templateId,
        customAccentHex = this.customAccentHex,
        updatedAt = this.updatedAt
    )
}
