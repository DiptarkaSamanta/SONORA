package com.sonora.music.domain.model

import java.io.File

/**
 * Domain representation of an audio track in Sonora.
 * Supports both local MediaStore tracks and Google Drive cloud streaming tracks.
 */
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val mediaUri: String,
    val artworkUri: String?,
    val isDownloaded: Boolean = false,
    val downloadId: Long? = null,
    val storageMode: StorageMode = StorageMode.CLOUD_STREAM,
    val format: String = "OPUS",
    val bitrateKbps: Int = 64,
    val sizeBytes: Long = 0L,
    val mimeType: String = "audio/opus",
    val dateAdded: Long = System.currentTimeMillis()
) {
    val durationFormatted: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }

    val isCloudStream: Boolean
        get() = storageMode == StorageMode.CLOUD_STREAM

    val isLocal: Boolean
        get() = storageMode == StorageMode.LOCAL_EXACT || storageMode == StorageMode.OFFLINE_COMPACT

    val sizeFormatted: String
        get() {
            if (sizeBytes <= 0) return "0 MB"
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1.0) {
                String.format("%.1f MB", mb)
            } else {
                val kb = sizeBytes / 1024.0
                String.format("%.0f KB", kb)
            }
        }

    val qualityBadge: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            val durationFormatted = String.format("%d:%02d", minutes, seconds)
            return if (sizeBytes > 0) "$sizeFormatted • $durationFormatted" else durationFormatted
        }
}
