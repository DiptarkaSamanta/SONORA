package com.sonora.music.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.sonora.music.core.database.dao.DownloadDao
import com.sonora.music.core.database.entity.DownloadEntity
import com.sonora.music.core.database.entity.DownloadStatus
import com.sonora.music.core.datastore.UserPreferences
import com.sonora.music.data.download.SonoraDownloadWorker
import com.sonora.music.domain.model.Song
import com.sonora.music.domain.model.StorageMode
import com.sonora.music.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class DownloadRepositoryImpl(
    private val context: Context,
    private val downloadDao: DownloadDao,
    private val userPreferences: UserPreferences
) : DownloadRepository {

    private val workManager = WorkManager.getInstance(context)

    override fun getAllDownloads(): Flow<List<DownloadEntity>> =
        downloadDao.getAllDownloads()

    override suspend fun enqueueDownload(song: Song, targetMode: StorageMode) {
        val isWifiOnly = userPreferences.isWifiOnlyFlow.first()

        val downloadEntity = DownloadEntity(
            songId = song.id,
            title = song.title,
            artist = song.artist,
            downloadUrl = song.mediaUri,
            destinationPath = "",
            storageMode = targetMode.name,
            totalBytes = song.sizeBytes,
            downloadedBytes = 0L,
            status = DownloadStatus.PENDING,
            artworkUrl = song.artworkUri
        )
        downloadDao.insertDownload(downloadEntity)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (isWifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SonoraDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    SonoraDownloadWorker.KEY_SONG_ID to song.id,
                    SonoraDownloadWorker.KEY_TITLE to song.title,
                    SonoraDownloadWorker.KEY_ARTIST to song.artist,
                    SonoraDownloadWorker.KEY_URL to song.mediaUri,
                    SonoraDownloadWorker.KEY_STORAGE_MODE to targetMode.name,
                    SonoraDownloadWorker.KEY_ARTWORK_URL to song.artworkUri
                )
            )
            .addTag("song_download_${song.id}")
            .build()

        workManager.enqueue(workRequest)
    }

    override suspend fun pauseDownload(songId: String) {
        workManager.cancelAllWorkByTag("song_download_$songId")
        downloadDao.updateStatus(songId, DownloadStatus.PAUSED)
    }

    override suspend fun resumeDownload(songId: String) {
        val download = downloadDao.getDownload(songId) ?: return
        val targetMode = runCatching { StorageMode.valueOf(download.storageMode) }.getOrDefault(StorageMode.BALANCED)
        val song = Song(
            id = download.songId,
            title = download.title,
            artist = download.artist,
            mediaUri = download.downloadUrl,
            artworkUri = download.artworkUrl,
            sizeBytes = download.totalBytes
        )
        enqueueDownload(song, targetMode)
    }

    override suspend fun cancelDownload(songId: String) {
        workManager.cancelAllWorkByTag("song_download_$songId")
        downloadDao.deleteDownload(songId)
    }

    override suspend fun retryDownload(songId: String) {
        resumeDownload(songId)
    }
}

