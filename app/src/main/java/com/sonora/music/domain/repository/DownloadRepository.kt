package com.sonora.music.domain.repository

import com.sonora.music.core.database.entity.DownloadEntity
import com.sonora.music.domain.model.Song
import com.sonora.music.domain.model.StorageMode
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun getAllDownloads(): Flow<List<DownloadEntity>>
    suspend fun enqueueDownload(song: Song, targetMode: StorageMode)
    suspend fun pauseDownload(songId: String)
    suspend fun resumeDownload(songId: String)
    suspend fun cancelDownload(songId: String)
    suspend fun retryDownload(songId: String)
}

