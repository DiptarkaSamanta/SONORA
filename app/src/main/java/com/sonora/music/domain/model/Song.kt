package com.sonora.music.domain.model

data class Song(
    val id: String,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val durationMs: Long = 0L,
    val mediaUri: String,
    val localFilePath: String? = null,
    val sizeBytes: Long = 0L,
    val mimeType: String = "audio/mpeg",
    val bitrateKbps: Int = 160,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val isDownloaded: Boolean = false,
    val isLocal: Boolean = true,
    val storageMode: StorageMode = StorageMode.BALANCED,
    val audioHash: String? = null,
    val artworkUri: String? = null,
    val lyrics: String? = null,
    val sourceProvider: String = "Local Storage",
    val canDownloadLegally: Boolean = false
) {
    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }

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
            return if (sizeBytes > 0) "$sizeFormatted • $durationFormatted" else durationFormatted
        }
}
