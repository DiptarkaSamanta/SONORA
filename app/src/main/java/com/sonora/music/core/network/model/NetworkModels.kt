package com.sonora.music.core.network.model

import com.google.gson.annotations.SerializedName
import com.sonora.music.domain.model.LyricsData
import com.sonora.music.domain.model.Song
import com.sonora.music.domain.model.StorageMode

// Jamendo Legal Music Discovery API Models
data class JamendoResponse(
    @SerializedName("headers") val headers: JamendoHeaders?,
    @SerializedName("results") val results: List<JamendoTrack>?
)

data class JamendoHeaders(
    @SerializedName("status") val status: String,
    @SerializedName("code") val code: Int,
    @SerializedName("results_count") val resultsCount: Int
)

data class JamendoTrack(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("duration") val durationSeconds: Long,
    @SerializedName("artist_id") val artistId: String?,
    @SerializedName("artist_name") val artistName: String,
    @SerializedName("album_name") val albumName: String?,
    @SerializedName("album_id") val albumId: String?,
    @SerializedName("audio") val audioStreamUrl: String,
    @SerializedName("audiodownload") val audioDownloadUrl: String?,
    @SerializedName("audiodownload_allowed") val audioDownloadAllowed: Boolean,
    @SerializedName("image") val imageUrl: String?,
    @SerializedName("license_ccurl") val licenseUrl: String?
) {
    fun toDomainSong(storageMode: StorageMode): Song {
        val durationMs = durationSeconds * 1000
        val estimatedSize = storageMode.estimateSongSizeBytes(durationMs)
        return Song(
            id = "jamendo_$id",
            title = name,
            artist = artistName,
            album = albumName ?: "Jamendo Creative Commons",
            durationMs = durationMs,
            mediaUri = audioStreamUrl,
            localFilePath = null,
            sizeBytes = estimatedSize,
            mimeType = "audio/mp3",
            bitrateKbps = storageMode.targetBitrateKbps,
            isFavorite = false,
            isDownloaded = false,
            isLocal = false,
            storageMode = storageMode,
            artworkUri = imageUrl,
            sourceProvider = "Jamendo (Creative Commons)",
            canDownloadLegally = audioDownloadAllowed && !audioDownloadUrl.isNullOrEmpty()
        )
    }
}

// LRCLIB Lyrics API Response Model
data class LrclibResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val trackName: String,
    @SerializedName("artistName") val artistName: String,
    @SerializedName("albumName") val albumName: String?,
    @SerializedName("duration") val durationSeconds: Double?,
    @SerializedName("instrumental") val instrumental: Boolean,
    @SerializedName("plainLyrics") val plainLyrics: String?,
    @SerializedName("syncedLyrics") val syncedLyrics: String?
) {
    fun toDomainLyrics(): LyricsData {
        return if (!syncedLyrics.isNullOrBlank()) {
            LyricsData.parseLrc(syncedLyrics, trackName, artistName)
        } else {
            LyricsData(
                trackTitle = trackName,
                artistName = artistName,
                plainLyrics = plainLyrics ?: if (instrumental) "[Instrumental]" else "No lyrics available",
                syncedLines = emptyList(),
                isSynced = false
            )
        }
    }
}

