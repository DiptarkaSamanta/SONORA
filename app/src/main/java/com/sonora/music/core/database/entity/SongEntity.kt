package com.sonora.music.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sonora.music.domain.model.Song
import com.sonora.music.domain.model.StorageMode

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["title", "artist"]),
        Index(value = ["audioHash"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isDownloaded"]),
        Index(value = ["playCount"])
    ]
)
data class SongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mediaUri: String,
    val localFilePath: String?,
    val sizeBytes: Long,
    val mimeType: String,
    val bitrateKbps: Int,
    val isFavorite: Boolean,
    val playCount: Int,
    val lastPlayedTimestamp: Long,
    val dateAdded: Long,
    val isDownloaded: Boolean,
    val isLocal: Boolean,
    val storageMode: String, // HIGH_QUALITY, BALANCED, SPACE_SAVER
    val audioHash: String?,
    val artworkUri: String?,
    val lyrics: String?,
    val sourceProvider: String,
    val canDownloadLegally: Boolean
) {
    fun toDomain(): Song = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        mediaUri = mediaUri,
        localFilePath = localFilePath,
        sizeBytes = sizeBytes,
        mimeType = mimeType,
        bitrateKbps = bitrateKbps,
        isFavorite = isFavorite,
        playCount = playCount,
        lastPlayedTimestamp = lastPlayedTimestamp,
        dateAdded = dateAdded,
        isDownloaded = isDownloaded,
        isLocal = isLocal,
        storageMode = runCatching { StorageMode.valueOf(storageMode) }.getOrDefault(StorageMode.BALANCED),
        audioHash = audioHash,
        artworkUri = artworkUri,
        lyrics = lyrics,
        sourceProvider = sourceProvider,
        canDownloadLegally = canDownloadLegally
    )

    companion object {
        fun fromDomain(song: Song): SongEntity = SongEntity(
            id = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            durationMs = song.durationMs,
            mediaUri = song.mediaUri,
            localFilePath = song.localFilePath,
            sizeBytes = song.sizeBytes,
            mimeType = song.mimeType,
            bitrateKbps = song.bitrateKbps,
            isFavorite = song.isFavorite,
            playCount = song.playCount,
            lastPlayedTimestamp = song.lastPlayedTimestamp,
            dateAdded = song.dateAdded,
            isDownloaded = song.isDownloaded,
            isLocal = song.isLocal,
            storageMode = song.storageMode.name,
            audioHash = song.audioHash,
            artworkUri = song.artworkUri,
            lyrics = song.lyrics,
            sourceProvider = song.sourceProvider,
            canDownloadLegally = song.canDownloadLegally
        )
    }
}

