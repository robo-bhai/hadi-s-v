package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cv_profiles")
data class CVProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val fullName: String,
    val jobTitle: String,
    val email: String,
    val phone: String,
    val city: String,
    val address: String,
    val cnic: String,
    val linkedInOrGithub: String = "",
    val photoUri: String?,
    val objective: String,
    val educationJson: String,
    val experienceJson: String,
    val isFreshGraduate: Boolean,
    val projectsJson: String = "[]",
    val certificationsJson: String = "[]",
    val skillsJson: String,
    val languagesJson: String,
    val templateId: String = "modern_emerald",
    val customAccentHex: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
