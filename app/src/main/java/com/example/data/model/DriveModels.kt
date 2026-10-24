package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Represents a connected Google Account.
 */
data class DriveAccount(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val accessToken: String = "",
    val tokenExpiryEpochMs: Long = 0L,
    val isDemo: Boolean = false,
    val storageUsedBytes: Long = 0L,
    val storageTotalBytes: Long = 0L,
    val totalFilesCount: Int = 0
) {
    val formattedStorageUsage: String
        get() {
            if (storageTotalBytes <= 0) return "Cloud Storage"
            val usedGb = storageUsedBytes.toDouble() / (1024 * 1024 * 1024)
            val totalGb = storageTotalBytes.toDouble() / (1024 * 1024 * 1024)
            return String.format(Locale.US, "%.1f GB of %.0f GB used", usedGb, totalGb)
        }

    val initials: String
        get() = displayName.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
            .ifEmpty { email.take(1).uppercase() }
}

/**
 * Represents a Google Drive file or folder from the Drive v3 API.
 */
@JsonClass(generateAdapter = true)
data class DriveFile(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "size") val size: Long? = null,
    @Json(name = "modifiedTime") val modifiedTime: String? = null,
    @Json(name = "parents") val parents: List<String>? = null,
    @Json(name = "iconLink") val iconLink: String? = null,
    @Json(name = "thumbnailLink") val thumbnailLink: String? = null,
    @Json(name = "webViewLink") val webViewLink: String? = null,
    @Json(name = "webContentLink") val webContentLink: String? = null,
    @Json(name = "trashed") val trashed: Boolean? = false
) {
    val isFolder: Boolean
        get() = mimeType == "application/vnd.google-apps.folder"

    val category: FileCategory
        get() = when {
            isFolder -> FileCategory.FOLDER
            mimeType.contains("document") || mimeType == "application/pdf" || mimeType.contains("text/") -> FileCategory.DOCUMENT
            mimeType.contains("spreadsheet") || mimeType.contains("csv") || mimeType.contains("excel") -> FileCategory.SPREADSHEET
            mimeType.contains("presentation") || mimeType.contains("powerpoint") -> FileCategory.PRESENTATION
            mimeType.startsWith("image/") -> FileCategory.IMAGE
            mimeType.startsWith("video/") -> FileCategory.VIDEO
            mimeType.startsWith("audio/") -> FileCategory.AUDIO
            mimeType.contains("zip") || mimeType.contains("tar") || mimeType.contains("rar") -> FileCategory.ARCHIVE
            else -> FileCategory.OTHER
        }

    val formattedSize: String
        get() {
            if (isFolder) return "--"
            val bytes = size ?: return "--"
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
                bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
                else -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
            }
        }

    val formattedModifiedDate: String
        get() {
            val dateStr = modifiedTime ?: return "Unknown date"
            return try {
                // ISO 8601 format "2026-10-24T08:30:00.000Z"
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = try {
                    isoFormat.parse(dateStr)
                } catch (e: Exception) {
                    val fallbackIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    fallbackIso.parse(dateStr)
                }
                if (date != null) {
                    val outFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                    outFormat.format(date)
                } else {
                    dateStr.take(10)
                }
            } catch (e: Exception) {
                dateStr.take(10)
            }
        }
}

enum class FileCategory(val label: String) {
    ALL("All"),
    FOLDER("Folders"),
    DOCUMENT("Docs"),
    SPREADSHEET("Sheets"),
    PRESENTATION("Slides"),
    IMAGE("Images"),
    VIDEO("Videos"),
    AUDIO("Audio"),
    ARCHIVE("Archives"),
    OTHER("Other")
}

enum class ViewMode {
    LIST,
    GRID
}

enum class SortOption(val title: String) {
    NAME_ASC("Name (A to Z)"),
    NAME_DESC("Name (Z to A)"),
    DATE_DESC("Newest modified"),
    DATE_ASC("Oldest modified"),
    SIZE_DESC("Largest size")
}

@JsonClass(generateAdapter = true)
data class DriveFileListResponse(
    @Json(name = "files") val files: List<DriveFile> = emptyList(),
    @Json(name = "nextPageToken") val nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateFolderRequest(
    @Json(name = "name") val name: String,
    @Json(name = "mimeType") val mimeType: String = "application/vnd.google-apps.folder",
    @Json(name = "parents") val parents: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class RenameRequest(
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class DriveAboutResponse(
    @Json(name = "user") val user: DriveUser? = null,
    @Json(name = "storageQuota") val storageQuota: StorageQuota? = null
)

@JsonClass(generateAdapter = true)
data class DriveUser(
    @Json(name = "displayName") val displayName: String? = null,
    @Json(name = "emailAddress") val emailAddress: String? = null,
    @Json(name = "photoLink") val photoLink: String? = null
)

@JsonClass(generateAdapter = true)
data class StorageQuota(
    @Json(name = "limit") val limit: Long? = null,
    @Json(name = "usage") val usage: Long? = null,
    @Json(name = "usageInDrive") val usageInDrive: Long? = null
)
