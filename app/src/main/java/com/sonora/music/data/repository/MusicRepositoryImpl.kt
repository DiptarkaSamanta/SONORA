package com.sonora.music.data.repository

import com.sonora.music.core.database.dao.PlaylistDao
import com.sonora.music.core.database.dao.SongDao
import com.sonora.music.core.database.entity.PlaylistEntity
import com.sonora.music.core.database.entity.PlaylistSongCrossRef
import com.sonora.music.core.database.entity.SongEntity
import com.sonora.music.core.network.CloudDatabaseClient
import com.sonora.music.core.network.NetworkClient
import com.sonora.music.data.scanner.LocalMediaScanner
import com.sonora.music.domain.model.LyricsData
import com.sonora.music.domain.model.Playlist
import com.sonora.music.domain.model.Song
import com.sonora.music.domain.model.StorageMode
import com.sonora.music.domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MusicRepositoryImpl(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val mediaScanner: LocalMediaScanner
) : MusicRepository {

    override fun getAllSongs(): Flow<List<Song>> =
        songDao.getAllSongs().map { list -> list.map { it.toDomain() } }

    override fun getFavoriteSongs(): Flow<List<Song>> =
        songDao.getFavoriteSongs().map { list -> list.map { it.toDomain() } }

    override fun getDownloadedSongs(): Flow<List<Song>> =
        songDao.getDownloadedSongs().map { list -> list.map { it.toDomain() } }

    override fun getCloudSongs(): Flow<List<Song>> =
        songDao.getCloudSongs().map { list -> list.map { it.toDomain() } }

    override fun getMostPlayedSongs(): Flow<List<Song>> =
        songDao.getMostPlayedSongs().map { list -> list.map { it.toDomain() } }

    override fun getRecentlyPlayedSongs(): Flow<List<Song>> =
        songDao.getRecentlyPlayedSongs().map { list -> list.map { it.toDomain() } }

    override fun searchLocalSongs(query: String): Flow<List<Song>> =
        songDao.searchSongs(query).map { list -> list.map { it.toDomain() } }

    override fun getAlbums(): Flow<List<String>> = songDao.getDistinctAlbums()

    override fun getArtists(): Flow<List<String>> = songDao.getDistinctArtists()

    override fun getSongsByAlbum(album: String): Flow<List<Song>> =
        songDao.getSongsByAlbum(album).map { list -> list.map { it.toDomain() } }

    override fun getSongsByArtist(artist: String): Flow<List<Song>> =
        songDao.getSongsByArtist(artist).map { list -> list.map { it.toDomain() } }

    override fun getAllPlaylists(): Flow<List<Playlist>> =
        playlistDao.getAllPlaylists().map { list -> list.map { it.toDomain() } }

    override fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> =
        playlistDao.getSongsInPlaylist(playlistId).map { list -> list.map { it.toDomain() } }

    override suspend fun createPlaylist(name: String, description: String): Long =
        playlistDao.insertPlaylist(PlaylistEntity(name = name, description = description))

    override suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun toggleFavorite(songId: String, currentStatus: Boolean) {
        songDao.updateFavorite(songId, !currentStatus)
    }

    override suspend fun recordSongPlayed(songId: String) {
        songDao.incrementPlayCount(songId)
    }

    override suspend fun getSongById(songId: String): Song? =
        songDao.getSongById(songId)?.toDomain()

    override suspend fun scanLocalMedia(): Int = withContext(Dispatchers.IO) {
        val scanned = mediaScanner.scanDeviceAudio()
        if (scanned.isNotEmpty()) {
            songDao.insertSongs(scanned)
        }
        scanned.size
    }

    override suspend fun searchOnlineMusic(query: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = NetworkClient.jamendoApi.searchTracks(query = query)
            response.results?.map { it.toDomainSong(StorageMode.BALANCED) } ?: emptyList()
        }
    }

    override suspend fun getTrendingOnlineMusic(): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = NetworkClient.jamendoApi.getTrendingTracks()
            response.results?.map { it.toDomainSong(StorageMode.BALANCED) } ?: emptyList()
        }
    }

    override suspend fun fetchLyrics(title: String, artist: String): LyricsData? = withContext(Dispatchers.IO) {
        runCatching {
            val cleanTitle = title.replace(Regex("\\(.*?\\)|\\[.*?\\]"), "").trim()
            val primaryArtist = artist.split("/", ",", "ft.", "feat.", "-")[0].trim()

            // 1. Try exact lookup
            val exact = try {
                NetworkClient.lrclibApi.getLyrics(trackName = cleanTitle, artistName = primaryArtist)
            } catch (e: Exception) {
                null
            }

            if (exact != null && (!exact.syncedLyrics.isNullOrBlank() || !exact.plainLyrics.isNullOrBlank())) {
                val data = exact.toDomainLyrics()
                data.plainLyrics?.let { songDao.updateLyrics(title, it) }
                return@runCatching data
            }

            // 2. Robust fallback to search query
            val searchResults = NetworkClient.lrclibApi.searchLyrics("$cleanTitle $primaryArtist")
            val best = searchResults.firstOrNull { !it.syncedLyrics.isNullOrBlank() }
                ?: searchResults.firstOrNull { !it.plainLyrics.isNullOrBlank() }

            val data = best?.toDomainLyrics()
            data?.plainLyrics?.let { songDao.updateLyrics(title, it) }
            data
        }.getOrNull()
    }

    private val cloudClient = CloudDatabaseClient()

    override suspend fun syncCloudMusicCatalog(customUrl: String?): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val songs = if (customUrl.isNullOrBlank()) {
                cloudClient.getBuiltinCompactCatalog()
            } else {
                cloudClient.fetchCloudCatalog(customUrl).getOrDefault(cloudClient.getBuiltinCompactCatalog())
            }

            val entities = songs.map { SongEntity.fromDomain(it) }
            songDao.insertSongs(entities)
            songs.size
        }
    }

    override suspend fun searchLyrics(query: String): Result<List<LyricsData>> = withContext(Dispatchers.IO) {
        runCatching {
            val results = NetworkClient.lrclibApi.searchLyrics(query)
            results.map { it.toDomainLyrics() }
        }
    }
}

