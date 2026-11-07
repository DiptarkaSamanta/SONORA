package com.sonora.music.core.network.model

import com.google.gson.annotations.SerializedName
import com.sonora.music.domain.model.Song
import com.sonora.music.domain.model.StorageMode

data class CloudCatalogResponse(
    @SerializedName("catalog_name") val catalogName: String,
    @SerializedName("version") val version: Int,
    @SerializedName("description") val description: String?,
    @SerializedName("songs") val songs: List<CloudSongItem>
)

data class CloudSongItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("album") val album: String?,
    @SerializedName("duration_seconds") val durationSeconds: Long,
    @SerializedName("stream_url") val streamUrl: String,
    @SerializedName("download_url") val downloadUrl: String?,
    @SerializedName("size_kb") val sizeKb: Long,
    @SerializedName("codec") val codec: String = "opus", // Ultra-compressed opus or aac
    @SerializedName("bitrate_kbps") val bitrateKbps: Int = 64, // 48-64 kbps gives near CD-quality with tiny size
    @SerializedName("artwork_url") val artworkUrl: String?,
    @SerializedName("lyrics") val lyrics: String? = null
) {
    fun toDomainSong(): Song {
        val durationMs = durationSeconds * 1000L
        val sizeBytes = sizeKb * 1024L
        return Song(
            id = "cloud_$id",
            title = title,
            artist = artist,
            album = album ?: "Cloud Compact Library",
            durationMs = durationMs,
            mediaUri = streamUrl,
            localFilePath = null,
            sizeBytes = sizeBytes,
            mimeType = if (codec.contains("opus", ignoreCase = true)) "audio/opus" else "audio/aac",
            bitrateKbps = bitrateKbps,
            isFavorite = false,
            isDownloaded = false,
            isLocal = false,
            storageMode = StorageMode.SPACE_SAVER,
            artworkUri = artworkUrl,
            lyrics = lyrics,
            sourceProvider = "Online Cloud Database (~${sizeKb / 1024.0}MB)",
            canDownloadLegally = !downloadUrl.isNullOrEmpty()
        )
    }
}

