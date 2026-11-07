package com.sonora.music.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val songCount: Int = 0,
    val totalDurationMs: Long = 0L,
    val coverArtUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

