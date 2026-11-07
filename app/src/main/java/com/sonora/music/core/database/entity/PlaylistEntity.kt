package com.sonora.music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sonora.music.domain.model.Playlist

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val coverArtUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(songCount: Int = 0, totalDurationMs: Long = 0L): Playlist = Playlist(
        id = id,
        name = name,
        description = description,
        songCount = songCount,
        totalDurationMs = totalDurationMs,
        coverArtUri = coverArtUri,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(playlist: Playlist): PlaylistEntity = PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverArtUri = playlist.coverArtUri,
            createdAt = playlist.createdAt
        )
    }
}

