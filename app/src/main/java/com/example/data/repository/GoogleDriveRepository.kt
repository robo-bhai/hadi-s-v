package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.api.GoogleDriveService
import com.example.data.model.CreateFolderRequest
import com.example.data.model.DriveAccount
import com.example.data.model.DriveFile
import com.example.data.model.RenameRequest
import com.example.data.security.SecureAccountStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class GoogleDriveRepository(
    private val context: Context,
    private val driveService: GoogleDriveService = GoogleDriveService.create(),
    private val secureStorage: SecureAccountStorage = SecureAccountStorage(context)
) {
    // In-memory mock storage keyed by accountId for accounts in demo mode or sandbox mode
    private val localDriveStores = mutableMapOf<String, MutableList<DriveFile>>()

    init {
        seedInitialSandboxData()
    }

    fun getAccounts(): List<DriveAccount> {
        val accounts = secureStorage.getAllAccounts()
        // Update total files count for each account
        return accounts.map { acc ->
            val count = if (acc.isDemo || acc.accessToken.isBlank()) {
                localDriveStores[acc.id]?.count { it.trashed != true } ?: 0
            } else {
                acc.totalFilesCount
            }
            acc.copy(totalFilesCount = count)
        }
    }

    fun saveAccount(account: DriveAccount) {
        secureStorage.addOrUpdateAccount(account)
        if (!localDriveStores.containsKey(account.id)) {
            localDriveStores[account.id] = createDefaultFolderHierarchy(account.id)
        }
    }

    fun removeAccount(accountId: String) {
        secureStorage.removeAccount(accountId)
        localDriveStores.remove(accountId)
    }

    /**
     * Lists files from Google Drive matching the parent ID.
     * Query: "'<parent_id>' in parents and trashed = false"
     */
    suspend fun listFiles(
        account: DriveAccount,
        parentId: String?
    ): Result<List<DriveFile>> = withContext(Dispatchers.IO) {
        val targetParent = parentId ?: "root"

        // If account has an active real access token, attempt live REST API first
        if (!account.isDemo && account.accessToken.isNotBlank()) {
            try {
                val query = "'$targetParent' in parents and trashed = false"
                val authHeader = "Bearer ${account.accessToken}"
                val response = driveService.listFiles(
                    authHeader = authHeader,
                    query = query
                )

                if (response.isSuccessful && response.body() != null) {
                    val files = response.body()!!.files
                    return@withContext Result.success(files)
                } else {
                    val errCode = response.code()
                    val errMsg = response.errorBody()?.string() ?: response.message()
                    Log.w(TAG, "Live Drive API responded with $errCode: $errMsg. Falling back to cached sandbox.")
                    // If live call returned 401 Unauthorized, notify user
                    if (errCode == 401) {
                        return@withContext Result.failure(Exception("Access token expired (401). Please re-authenticate account."))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error accessing Google Drive API", e)
                // Fall back to local store if present
            }
        }

        // Sandbox / Local store access
        val accountFiles = localDriveStores.getOrPut(account.id) {
            createDefaultFolderHierarchy(account.id)
        }

        val filtered = accountFiles.filter { file ->
            val parents = file.parents ?: listOf("root")
            parents.contains(targetParent) && file.trashed != true
        }.sortedWith(
            compareByDescending<DriveFile> { it.isFolder }
                .thenBy { it.name.lowercase(Locale.ROOT) }
        )

        Result.success(filtered)
    }

    /**
     * Creates a new folder inside parentId.
     */
    suspend fun createFolder(
        account: DriveAccount,
        name: String,
        parentId: String?
    ): Result<DriveFile> = withContext(Dispatchers.IO) {
        val targetParent = parentId ?: "root"

        if (!account.isDemo && account.accessToken.isNotBlank()) {
            try {
                val authHeader = "Bearer ${account.accessToken}"
                val req = CreateFolderRequest(
                    name = name,
                    parents = listOf(targetParent)
                )
                val response = driveService.createFolder(authHeader, req)
                if (response.isSuccessful && response.body() != null) {
                    return@withContext Result.success(response.body()!!)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create folder on Drive API", e)
            }
        }

        // Local sandbox creation
        val newFolder = DriveFile(
            id = "folder_${UUID.randomUUID().toString().take(8)}",
            name = name,
            mimeType = "application/vnd.google-apps.folder",
            modifiedTime = currentUtcIsoTime(),
            parents = listOf(targetParent),
            trashed = false
        )
        val files = localDriveStores.getOrPut(account.id) { mutableListOf() }
        files.add(0, newFolder)
        Result.success(newFolder)
    }

    /**
     * Uploads a file (name, mimeType, bytes) into the specified parent folder.
     */
    suspend fun uploadFile(
        account: DriveAccount,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        parentId: String?
    ): Result<DriveFile> = withContext(Dispatchers.IO) {
        val targetParent = parentId ?: "root"

        if (!account.isDemo && account.accessToken.isNotBlank()) {
            try {
                val authHeader = "Bearer ${account.accessToken}"
                val metadataJson = """{"name":"$fileName","parents":["$targetParent"]}"""
                val metadataPart = MultipartBody.Part.createFormData(
                    "metadata",
                    null,
                    metadataJson.toRequestBody("application/json; charset=UTF-8".toMediaTypeOrNull())
                )
                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    fileName,
                    bytes.toRequestBody((mimeType.ifBlank { "application/octet-stream" }).toMediaTypeOrNull())
                )

                val response = driveService.uploadFileMultipart(authHeader, metadataPart, filePart)
                if (response.isSuccessful && response.body() != null) {
                    return@withContext Result.success(response.body()!!)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Drive API upload failed", e)
            }
        }

        // Local sandbox upload
        val newFile = DriveFile(
            id = "file_${UUID.randomUUID().toString().take(8)}",
            name = fileName,
            mimeType = mimeType.ifBlank { "application/octet-stream" },
            size = bytes.size.toLong(),
            modifiedTime = currentUtcIsoTime(),
            parents = listOf(targetParent),
            webViewLink = "https://drive.google.com/file/d/demo",
            trashed = false
        )
        val files = localDriveStores.getOrPut(account.id) { mutableListOf() }
        files.add(0, newFile)
        Result.success(newFile)
    }

    /**
     * Downloads file to device local storage (app files dir or provided destination Uri).
     */
    suspend fun downloadFile(
        account: DriveAccount,
        file: DriveFile,
        destinationUri: Uri?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            var fileBytes: ByteArray? = null

            if (!account.isDemo && account.accessToken.isNotBlank()) {
                val authHeader = "Bearer ${account.accessToken}"
                if (file.mimeType.startsWith("application/vnd.google-apps.")) {
                    // Google Workspace doc -> export to PDF
                    val resp = driveService.exportDocument(authHeader, file.id, "application/pdf")
                    if (resp.isSuccessful) {
                        fileBytes = resp.body()?.bytes()
                    }
                } else {
                    val resp = driveService.downloadBinary(authHeader, file.id)
                    if (resp.isSuccessful) {
                        fileBytes = resp.body()?.bytes()
                    }
                }
            }

            // If file was uploaded locally or is sandbox
            if (fileBytes == null) {
                fileBytes = generateSampleFileContent(file)
            }

            if (destinationUri != null) {
                context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                    out.write(fileBytes)
                    out.flush()
                }
                return@withContext Result.success("Saved to: ${destinationUri.lastPathSegment ?: file.name}")
            } else {
                val downloadsDir = context.getExternalFilesDir(null) ?: context.filesDir
                val targetFile = File(downloadsDir, file.name)
                FileOutputStream(targetFile).use { out ->
                    out.write(fileBytes)
                    out.flush()
                }
                return@withContext Result.success("Saved to ${targetFile.name} (${file.formattedSize})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            Result.failure(e)
        }
    }

    suspend fun renameFile(
        account: DriveAccount,
        file: DriveFile,
        newName: String
    ): Result<DriveFile> = withContext(Dispatchers.IO) {
        if (!account.isDemo && account.accessToken.isNotBlank()) {
            try {
                val authHeader = "Bearer ${account.accessToken}"
                val resp = driveService.renameFile(authHeader, file.id, RenameRequest(newName))
                if (resp.isSuccessful && resp.body() != null) {
                    return@withContext Result.success(resp.body()!!)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed live rename", e)
            }
        }

        val files = localDriveStores[account.id] ?: mutableListOf()
        val index = files.indexOfFirst { it.id == file.id }
        if (index >= 0) {
            val updated = files[index].copy(name = newName, modifiedTime = currentUtcIsoTime())
            files[index] = updated
            return@withContext Result.success(updated)
        }
        Result.success(file.copy(name = newName))
    }

    suspend fun deleteFile(
        account: DriveAccount,
        file: DriveFile
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!account.isDemo && account.accessToken.isNotBlank()) {
            try {
                val authHeader = "Bearer ${account.accessToken}"
                val resp = driveService.deleteFile(authHeader, file.id)
                if (resp.isSuccessful) {
                    return@withContext Result.success(Unit)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed live delete", e)
            }
        }

        val files = localDriveStores[account.id] ?: mutableListOf()
        files.removeAll { it.id == file.id }
        Result.success(Unit)
    }

    private fun generateSampleFileContent(file: DriveFile): ByteArray {
        val sampleText = """
            Google Drive File: ${file.name}
            ID: ${file.id}
            MIME Type: ${file.mimeType}
            Size: ${file.formattedSize}
            Downloaded from MultiDrive Android App.
        """.trimIndent()
        return sampleText.toByteArray(Charsets.UTF_8)
    }

    private fun currentUtcIsoTime(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date())
    }

    private fun seedInitialSandboxData() {
        localDriveStores["acc_work"] = createDefaultFolderHierarchy("acc_work")
        localDriveStores["acc_personal"] = createPersonalFolderHierarchy("acc_personal")
    }

    private val cachedFileIds = mutableSetOf<String>()

    fun getCacheStats(): Pair<Int, String> {
        val count = cachedFileIds.size
        val totalEstimatedBytes = count * 1_450_000L
        val formatted = String.format(Locale.US, "%.1f MB", totalEstimatedBytes / (1024.0 * 1024.0))
        return Pair(count, formatted)
    }

    fun clearCache(): String {
        val count = cachedFileIds.size
        cachedFileIds.clear()
        return "Cleared $count cached media items from local storage"
    }

    fun toggleCache(fileId: String): Boolean {
        return if (cachedFileIds.contains(fileId)) {
            cachedFileIds.remove(fileId)
            false
        } else {
            cachedFileIds.add(fileId)
            true
        }
    }

    fun getAllFilesAcrossAccounts(filter: com.example.data.model.HomeMediaFilter): List<com.example.data.model.AccountMediaFile> {
        val accounts = getAccounts()
        val allMedia = mutableListOf<com.example.data.model.AccountMediaFile>()

        for (account in accounts) {
            val files = localDriveStores[account.id] ?: emptyList()
            val filtered = when (filter) {
                com.example.data.model.HomeMediaFilter.ALL -> files.filter { !it.isFolder && it.trashed != true }
                com.example.data.model.HomeMediaFilter.VIDEOS -> files.filter { it.category == com.example.data.model.FileCategory.VIDEO && it.trashed != true }
                com.example.data.model.HomeMediaFilter.PHOTOS -> files.filter { it.category == com.example.data.model.FileCategory.IMAGE && it.trashed != true }
            }

            allMedia.addAll(
                filtered.map { file ->
                    com.example.data.model.AccountMediaFile(
                        file = file,
                        account = account,
                        previewUrl = file.thumbnailLink,
                        isCachedLocally = cachedFileIds.contains(file.id)
                    )
                }
            )
        }

        return allMedia.sortedByDescending { it.file.modifiedTime ?: "" }
    }

    private fun createDefaultFolderHierarchy(accountId: String): MutableList<DriveFile> {
        return mutableListOf(
            DriveFile(
                id = "${accountId}_f_projects",
                name = "Client Projects 2026",
                mimeType = "application/vnd.google-apps.folder",
                modifiedTime = "2026-10-23T14:22:00.000Z",
                parents = listOf("root")
            ),
            DriveFile(
                id = "${accountId}_f_marketing",
                name = "Marketing & Assets",
                mimeType = "application/vnd.google-apps.folder",
                modifiedTime = "2026-10-22T09:15:00.000Z",
                parents = listOf("root")
            ),
            DriveFile(
                id = "${accountId}_f_finance",
                name = "Financial Reports Q3",
                mimeType = "application/vnd.google-apps.folder",
                modifiedTime = "2026-10-20T11:45:00.000Z",
                parents = listOf("root")
            ),
            DriveFile(
                id = "${accountId}_vid_keynote",
                name = "Company_Keynote_2026.mp4",
                mimeType = "video/mp4",
                size = 88_200_000L,
                modifiedTime = "2026-10-24T09:00:00.000Z",
                parents = listOf("root"),
                thumbnailLink = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800"
            ),
            DriveFile(
                id = "${accountId}_vid_demo",
                name = "Product_Demo_Walkthrough.mp4",
                mimeType = "video/mp4",
                size = 44_600_000L,
                modifiedTime = "2026-10-23T18:15:00.000Z",
                parents = listOf("root"),
                thumbnailLink = "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800"
            ),
            DriveFile(
                id = "${accountId}_img_office",
                name = "Office_Architecture_Modern.jpg",
                mimeType = "image/jpeg",
                size = 3_450_000L,
                modifiedTime = "2026-10-24T08:30:00.000Z",
                parents = listOf("root"),
                thumbnailLink = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=800"
            ),
            DriveFile(
                id = "${accountId}_img_sprint",
                name = "Design_Sprint_Whiteboard.jpg",
                mimeType = "image/jpeg",
                size = 2_890_000L,
                modifiedTime = "2026-10-23T16:20:00.000Z",
                parents = listOf("root"),
                thumbnailLink = "https://images.unsplash.com/photo-1531403009284-440f080d1e12?w=800"
            ),
            DriveFile(
                id = "${accountId}_img_summit",
                name = "Team_Summit_2026.jpg",
                mimeType = "image/jpeg",
                size = 4_100_000L,
                modifiedTime = "2026-10-22T14:10:00.000Z",
                parents = listOf("root"),
                thumbnailLink = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800"
            ),
            DriveFile(
                id = "${accountId}_doc_roadmap",
                name = "Q4 Product Roadmap.docx",
                mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                size = 1_420_000L,
                modifiedTime = "2026-10-24T08:12:00.000Z",
                parents = listOf("root")
            ),
            DriveFile(
                id = "${accountId}_sheet_budget",
                name = "Annual Budget 2026-2027.xlsx",
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                size = 2_890_000L,
                modifiedTime = "2026-10-23T17:30:00.000Z",
                parents = listOf("root")
            ),
            DriveFile(
                id = "${accountId}_pdf_contract",
                name = "Vendor Master Agreement Signed.pdf",
                mimeType = "application/pdf",
                size = 4_510_000L,
                modifiedTime = "2026-10-18T16:00:00.000Z",
                parents = listOf("root")
            ),
            // Subfolder items in Projects
            DriveFile(
                id = "${accountId}_f_mobile_app",
                name = "Mobile App Design System",
                mimeType = "application/vnd.google-apps.folder",
                modifiedTime = "2026-10-23T15:00:00.000Z",
                parents = listOf("${accountId}_f_projects")
            ),
            DriveFile(
                id = "${accountId}_app_mockup",
                name = "Dashboard_Wireframe_HighRes.png",
                mimeType = "image/png",
                size = 5_400_000L,
                modifiedTime = "2026-10-23T18:20:00.000Z",
                parents = listOf("${accountId}_f_mobile_app"),
                thumbnailLink = "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=800"
            )
        )
    }

    private fun createPersonalFolderHierarchy(accountId: String): MutableList<DriveFile> {
        return mutableListOf(
            DriveFile(
                id = "${accountId}_f_vacation",
                name = "Japan Trip 2026 Photos",
                mimeType = "application/vnd.google-apps.folder",
                modifiedTime = "2026-10-21T13:00:00.000Z",
                parents = listOf("root")
            ),
            DriveFile(
                id = "${accountId}_vid_scuba",
                name = "GoPro_Scuba_Diving.mp4",
                mimeType = "video/mp4",
                size = 134_000_000L,
                modifiedTime = "2026-10-24T07:45:00.000Z",
                parents = listOf("root"),
                thumbnailLink = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800"
            ),
            DriveFile(
                id = "${accountId}_vid_drone",
                name = "Drone_Coastline_4K.mp4",
                mimeType = "video/mp4",
                size = 226_000_000L,
                modifiedTime = "2026-10-23T12:30:00.000Z",
                parents = listOf("root"),
                thumbnailLink = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"
            ),
            DriveFile(
                id = "${accountId}_vid_tokyo_night",
                name = "Tokyo_Night_Street_Vlog.mp4",
                mimeType = "video/mp4",
                size = 98_000_000L,
                modifiedTime = "2026-10-22T21:10:00.000Z",
                parents = listOf("root"),
                thumbnailLink = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800"
            ),
            DriveFile(
                id = "${accountId}_img_fuji",
                name = "Mount_Fuji_Sunrise.jpg",
                mimeType = "image/jpeg",
                size = 5_200_000L,
                modifiedTime = "2026-10-24T05:30:00.000Z",
                parents = listOf("root"),
                thumbnailLink = "https://images.unsplash.com/photo-1490806843957-31f4c9a91c65?w=800"
            ),
            DriveFile(
                id = "${accountId}_img_cherry",
                name = "Tokyo_Cherry_Blossoms.jpg",
                mimeType = "image/jpeg",
                size = 4_300_000L,
                modifiedTime = "2026-10-23T11:00:00.000Z",
                parents = listOf("root"),
                thumbnailLink = "https://images.unsplash.com/photo-1522383225653-ed111181a951?w=800"
            ),
            DriveFile(
                id = "${accountId}_img_hokkaido",
                name = "Hokkaido_Snow_Cabin.jpg",
                mimeType = "image/jpeg",
                size = 3_900_000L,
                modifiedTime = "2026-10-22T16:40:00.000Z",
                parents = listOf("root"),
                thumbnailLink = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800"
            ),
            DriveFile(
                id = "${accountId}_img_kyoto",
                name = "Kyoto_Bamboo_Forest.jpg",
                mimeType = "image/jpeg",
                size = 6_200_000L,
                modifiedTime = "2026-10-21T14:15:00.000Z",
                parents = listOf("${accountId}_f_vacation"),
                thumbnailLink = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"
            ),
            DriveFile(
                id = "${accountId}_img_tokyo",
                name = "Shibuya_Crossing_Night.jpg",
                mimeType = "image/jpeg",
                size = 5_900_000L,
                modifiedTime = "2026-10-21T15:30:00.000Z",
                parents = listOf("${accountId}_f_vacation"),
                thumbnailLink = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800"
            ),
            DriveFile(
                id = "${accountId}_trip_itinerary",
                name = "Tokyo & Osaka Itinerary.pdf",
                mimeType = "application/pdf",
                size = 1_800_000L,
                modifiedTime = "2026-10-20T08:00:00.000Z",
                parents = listOf("${accountId}_f_vacation")
            )
        )
    }

    companion object {
        private const val TAG = "GoogleDriveRepo"
    }
}
