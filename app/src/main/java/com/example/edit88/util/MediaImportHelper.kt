package com.example.edit88.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.edit88.model.AspectRatioType
import com.example.edit88.model.MediaClip
import com.example.edit88.model.MediaType

object MediaImportHelper {

    fun createMediaClipFromUri(context: Context, uri: Uri): MediaClip {
        var fileName = "Video Clip"
        var durationMs = 5000L
        var width = 1080
        var height = 1920
        var rotation = 0

        // 1. Query display name
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex) ?: "Video Clip"
                }
            }
        } catch (_: Exception) {
            fileName = uri.lastPathSegment ?: "Video Clip"
        }

        // 2. Query duration and dimensions via MediaMetadataRetriever
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val dur = durationStr?.toLongOrNull()
            if (dur != null && dur > 0) {
                durationMs = dur
            }

            val wStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val hStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val rotStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)

            val parsedWidth = wStr?.toIntOrNull() ?: 1080
            val parsedHeight = hStr?.toIntOrNull() ?: 1920
            val parsedRot = rotStr?.toIntOrNull() ?: 0

            // If rotation is 90 or 270, swap dimensions
            if (parsedRot == 90 || parsedRot == 270) {
                width = parsedHeight
                height = parsedWidth
            } else {
                width = parsedWidth
                height = parsedHeight
            }
            rotation = parsedRot
            retriever.release()
        } catch (_: Exception) {
            // Keep default duration and resolution
        }

        // Color palettes for thumbnail representation
        val palettes = listOf(
            Pair(0xFF2563EBL, 0xFF7C3AEDL),
            Pair(0xFF059669L, 0xFF0D9488L),
            Pair(0xFFD97706L, 0xFFDC2626L),
            Pair(0xFF4F46E5L, 0xFF9333EAL),
            Pair(0xFF0284C7L, 0xFF2563EBL),
            Pair(0xFFDB2777L, 0xFF9333EAL)
        )
        val palette = palettes[kotlin.math.abs(fileName.hashCode()) % palettes.size]

        // Clean up title
        val cleanTitle = fileName.substringBeforeLast(".")

        return MediaClip(
            title = cleanTitle,
            type = MediaType.VIDEO,
            uriString = uri.toString(),
            videoWidth = width,
            videoHeight = height,
            durationMs = durationMs,
            startTrimMs = 0L,
            endTrimMs = durationMs,
            visualTheme = "Local: $cleanTitle",
            primaryColorHex = palette.first,
            secondaryColorHex = palette.second
        )
    }

    fun detectBestAspectRatio(clips: List<MediaClip>): AspectRatioType {
        if (clips.isEmpty()) return AspectRatioType.RATIO_9_16
        val first = clips.first()
        val ratio = first.videoWidth.toFloat() / first.videoHeight.toFloat().coerceAtLeast(1f)
        return when {
            ratio > 1.3f -> AspectRatioType.RATIO_16_9
            ratio in 0.85f..1.15f -> AspectRatioType.RATIO_1_1
            ratio in 0.7f..0.85f -> AspectRatioType.RATIO_4_5
            else -> AspectRatioType.RATIO_9_16
        }
    }
}
