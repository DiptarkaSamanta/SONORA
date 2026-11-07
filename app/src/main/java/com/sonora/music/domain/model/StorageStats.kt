package com.sonora.music.domain.model

data class StorageStats(
    val totalDeviceStorageBytes: Long = 0L,
    val freeDeviceStorageBytes: Long = 0L,
    val sonoraMusicBytes: Long = 0L,
    val sonoraCacheBytes: Long = 0L,
    val totalSongCount: Int = 0,
    val downloadedSongCount: Int = 0,
    val duplicateGroupCount: Int = 0,
    val potentialDuplicateSavingsBytes: Long = 0L,
    val activeStorageMode: StorageMode = StorageMode.BALANCED
) {
    val freeStorageFormatted: String
        get() = formatBytes(freeDeviceStorageBytes)

    val musicStorageFormatted: String
        get() = formatBytes(sonoraMusicBytes)

    val cacheStorageFormatted: String
        get() = formatBytes(sonoraCacheBytes)

    val duplicateSavingsFormatted: String
        get() = formatBytes(potentialDuplicateSavingsBytes)

    val deviceUsagePercent: Float
        get() {
            if (totalDeviceStorageBytes <= 0) return 0f
            val used = totalDeviceStorageBytes - freeDeviceStorageBytes
            return (used.toFloat() / totalDeviceStorageBytes.toFloat()).coerceIn(0f, 1f)
        }

    private fun formatBytes(bytes: Long): String {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return if (gb >= 1.0) {
            String.format("%.2f GB", gb)
        } else {
            val mb = bytes / (1024.0 * 1024.0)
            String.format("%.1f MB", mb)
        }
    }
}

data class DuplicateGroup(
    val title: String,
    val artist: String,
    val primarySong: Song,
    val duplicates: List<Song>,
    val totalWastedBytes: Long
)

