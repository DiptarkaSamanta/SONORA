package com.sonora.music.domain.repository

import com.sonora.music.domain.model.LyricsData
import com.sonora.music.domain.model.Playlist
import com.sonora.music.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getFavoriteSongs(): Flow<List<Song>>
    fun getDownloadedSongs(): Flow<List<Song>>
    fun getCloudSongs(): Flow<List<Song>>
    fun getMostPlayedSongs(): Flow<List<Song>>
    fun getRecentlyPlayedSongs(): Flow<List<Song>>
    fun searchLocalSongs(query: String): Flow<List<Song>>

    fun getAlbums(): Flow<List<String>>
    fun getArtists(): Flow<List<String>>
    fun getSongsByAlbum(album: String): Flow<List<Song>>
    fun getSongsByArtist(artist: String): Flow<List<Song>>

    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>
    suspend fun createPlaylist(name: String, description: String = ""): Long
    suspend fun addSongToPlaylist(playlistId: Long, songId: String)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)
    suspend fun deletePlaylist(playlistId: Long)

    suspend fun toggleFavorite(songId: String, currentStatus: Boolean)
    suspend fun recordSongPlayed(songId: String)
    suspend fun getSongById(songId: String): Song?
    suspend fun scanLocalMedia(): Int

    // Online discovery & search
    suspend fun searchOnlineMusic(query: String): Result<List<Song>>
    suspend fun getTrendingOnlineMusic(): Result<List<Song>>
    suspend fun syncCloudMusicCatalog(customUrl: String? = null): Result<Int>
    suspend fun fetchLyrics(title: String, artist: String): LyricsData?
    suspend fun searchLyrics(query: String): Result<List<LyricsData>>
}

