package com.sonora.music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sonora.music.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC")
    suspend fun getAllSongsSync(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: String): SongEntity?

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    fun getSongByIdFlow(id: String): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY lastPlayedTimestamp DESC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isDownloaded = 1 ORDER BY dateAdded DESC")
    fun getDownloadedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isLocal = 0 ORDER BY dateAdded DESC")
    fun getCloudSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT 30")
    fun getMostPlayedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT 30")
    fun getRecentlyPlayedSongs(): Flow<List<SongEntity>>

    @Query("""
        SELECT * FROM songs 
        WHERE title LIKE '%' || :query || '%' 
           OR artist LIKE '%' || :query || '%' 
           OR album LIKE '%' || :query || '%' 
           OR lyrics LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("SELECT DISTINCT album FROM songs WHERE album != '' ORDER BY album ASC")
    fun getDistinctAlbums(): Flow<List<String>>

    @Query("SELECT DISTINCT artist FROM songs WHERE artist != '' ORDER BY artist ASC")
    fun getDistinctArtists(): Flow<List<String>>

    @Query("SELECT * FROM songs WHERE album = :album ORDER BY title ASC")
    fun getSongsByAlbum(album: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY title ASC")
    fun getSongsByArtist(artist: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE audioHash IS NOT NULL")
    suspend fun getSongsWithHash(): List<SongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE songs SET lyrics = :lyrics WHERE id = :id")
    suspend fun updateLyrics(id: String, lyrics: String)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: String)

    @Query("DELETE FROM songs WHERE isLocal = 0 AND id NOT IN (:activeIds)")
    suspend fun pruneDeletedCloudSongs(activeIds: List<String>)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteSongsByIds(ids: List<String>)

    @Query("SELECT COUNT(*) FROM songs")
    fun getSongCountFlow(): Flow<Int>

    @Query("SELECT SUM(sizeBytes) FROM songs WHERE isDownloaded = 1")
    fun getTotalDownloadedSizeBytes(): Flow<Long?>
}

