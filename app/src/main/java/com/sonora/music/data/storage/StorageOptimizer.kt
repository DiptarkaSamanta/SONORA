package com.sonora.music.data.storage

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.core.content.ContextCompat
import com.sonora.music.core.database.dao.SongDao
import com.sonora.music.domain.model.DuplicateGroup
import com.sonora.music.domain.model.Song
import com.sonora.music.domain.model.StorageMode
import com.sonora.music.domain.model.StorageStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StorageOptimizer(
    private val context: Context,
    private val songDao: SongDao,
    private val duplicateDetector: DuplicateDetector
) {

    suspend fun getStorageStats(activeMode: StorageMode): StorageStats = withContext(Dispatchers.IO) {
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalDeviceBytes = stat.totalBytes
        val freeDeviceBytes = stat.availableBytes

        // Music files in Sonora private/shared folder
        val musicDirs = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_MUSIC)
        var sonoraMusicBytes = 0L
        musicDirs.filterNotNull().forEach { dir ->
            sonoraMusicBytes += calculateDirectorySize(dir)
        }

        // Cache files (Glide/Coil image cache, temp chunks)
        var cacheBytes = calculateDirectorySize(context.cacheDir)
        context.externalCacheDir?.let { cacheBytes += calculateDirectorySize(it) }

        val allSongs = songDao.getAllSongsSync().map { it.toDomain() }
        val duplicates = duplicateDetector.findDuplicates(allSongs)
        val potentialSavings = duplicates.sumOf { it.totalWastedBytes }

        StorageStats(
            totalDeviceStorageBytes = totalDeviceBytes,
            freeDeviceStorageBytes = freeDeviceBytes,
            sonoraMusicBytes = sonoraMusicBytes,
            sonoraCacheBytes = cacheBytes,
            totalSongCount = allSongs.size,
            downloadedSongCount = allSongs.count { it.isDownloaded },
            duplicateGroupCount = duplicates.size,
            potentialDuplicateSavingsBytes = potentialSavings,
            activeStorageMode = activeMode
        )
    }

    suspend fun detectDuplicates(): List<DuplicateGroup> = withContext(Dispatchers.IO) {
        val allSongs = songDao.getAllSongsSync().map { it.toDomain() }
        duplicateDetector.findDuplicates(allSongs)
    }

    suspend fun deleteDuplicateSongs(songsToDelete: List<Song>): Long = withContext(Dispatchers.IO) {
        var freedBytes = 0L
        val idsToDelete = mutableListOf<String>()

        for (song in songsToDelete) {
            song.localFilePath?.let { path ->
                val file = File(path)
                if (file.exists() && file.delete()) {
                    freedBytes += song.sizeBytes
                }
            }
            idsToDelete.add(song.id)
        }

        if (idsToDelete.isNotEmpty()) {
            songDao.deleteSongsByIds(idsToDelete)
        }
        freedBytes
    }

    suspend fun cleanTemporaryCache(): Long = withContext(Dispatchers.IO) {
        var freedBytes = 0L

        fun deleteRecursively(file: File) {
            if (file.isDirectory) {
                file.listFiles()?.forEach { deleteRecursively(it) }
            }
            val length = file.length()
            if (file.delete()) {
                freedBytes += length
            }
        }

        context.cacheDir.listFiles()?.forEach { deleteRecursively(it) }
        context.externalCacheDir?.listFiles()?.forEach { deleteRecursively(it) }

        freedBytes
    }

    private fun calculateDirectorySize(dir: File): Long {
        if (!dir.exists()) return 0L
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (file in files) {
            size += if (file.isDirectory) calculateDirectorySize(file) else file.length()
        }
        return size
    }
}

