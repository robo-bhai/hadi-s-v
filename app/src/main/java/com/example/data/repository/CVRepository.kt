package com.example.data.repository

import com.example.data.db.CVDao
import com.example.data.db.toDomain
import com.example.data.db.toEntity
import com.example.data.model.CVData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CVRepository(private val dao: CVDao) {

    val allCVs: Flow<List<CVData>> = dao.getAllProfiles().map { list ->
        list.map { it.toDomain() }
    }

    fun getCVById(id: Long): Flow<CVData?> = dao.getProfileById(id).map { entity ->
        entity?.toDomain()
    }

    suspend fun saveCV(cvData: CVData): Long {
        val entity = cvData.toEntity().copy(updatedAt = System.currentTimeMillis())
        return dao.insertProfile(entity)
    }

    suspend fun deleteCV(cvData: CVData) {
        dao.deleteProfile(cvData.toEntity())
    }

    suspend fun deleteCVById(id: Long) {
        dao.deleteProfileById(id)
    }
}
