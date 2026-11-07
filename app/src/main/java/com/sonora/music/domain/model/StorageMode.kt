package com.sonora.music.domain.model

/**
 * Storage modes offering intelligent trade-offs between audio quality and storage footprint.
 * Highly optimized for device storage efficiency (e.g. Redmi 12 5G).
 */
enum class StorageMode(
    val title: String,
    val subtitle: String,
    val preferredCodec: String,
    val targetBitrateKbps: Int,
    val compressionRatio: Float, // approximate size multiplier compared to 320kbps MP3
    val estimatedMbPerMinute: Float
) {
    HIGH_QUALITY(
        title = "High Quality",
        subtitle = "Lossless FLAC / 320 kbps MP3 • Studio sound fidelity",
        preferredCodec = "FLAC / MP3",
        targetBitrateKbps = 320,
        compressionRatio = 1.0f,
        estimatedMbPerMinute = 2.4f
    ),
    BALANCED(
        title = "Balanced",
        subtitle = "160 kbps AAC / 128 kbps Opus • 50% storage savings with pristine audio",
        preferredCodec = "Opus / AAC",
        targetBitrateKbps = 160,
        compressionRatio = 0.5f,
        estimatedMbPerMinute = 1.2f
    ),
    SPACE_SAVER(
        title = "Space Saver",
        subtitle = "96 kbps Opus / AAC • Maximum songs, 70% storage savings",
        preferredCodec = "Opus",
        targetBitrateKbps = 96,
        compressionRatio = 0.3f,
        estimatedMbPerMinute = 0.72f
    );

    fun estimateSongSizeBytes(durationMs: Long): Long {
        val minutes = durationMs / 60000f
        return (minutes * estimatedMbPerMinute * 1024 * 1024).toLong()
    }

    companion object {
        fun fromBitrate(bitrateKbps: Int): StorageMode {
            return when {
                bitrateKbps >= 256 -> HIGH_QUALITY
                bitrateKbps >= 128 -> BALANCED
                else -> SPACE_SAVER
            }
        }
    }
}

