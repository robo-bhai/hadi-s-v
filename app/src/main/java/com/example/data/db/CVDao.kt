package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CVDao {
    @Query("SELECT * FROM cv_profiles ORDER BY updatedAt DESC")
    fun getAllProfiles(): Flow<List<CVProfileEntity>>

    @Query("SELECT * FROM cv_profiles WHERE id = :id LIMIT 1")
    fun getProfileById(id: Long): Flow<CVProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: CVProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: CVProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: CVProfileEntity)

    @Query("DELETE FROM cv_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)
}
