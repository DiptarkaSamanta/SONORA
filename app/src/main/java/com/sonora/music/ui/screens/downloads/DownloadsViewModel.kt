package com.sonora.music.ui.screens.downloads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sonora.music.core.database.entity.DownloadEntity
import com.sonora.music.core.database.entity.DownloadStatus
import com.sonora.music.core.di.ServiceLocator
import com.sonora.music.domain.model.Song
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadsViewModel(application: Application) : AndroidViewModel(application) {

    private val downloadRepository = ServiceLocator.provideDownloadRepository(application)
    private val musicRepository = ServiceLocator.provideMusicRepository(application)
    private val playbackManager = ServiceLocator.providePlaybackManager(application)
    private val userPreferences = ServiceLocator.provideUserPreferences(application)

    val allDownloads: StateFlow<List<DownloadEntity>> = downloadRepository.getAllDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDownloads: StateFlow<List<DownloadEntity>> = allDownloads.map { list ->
        list.filter { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PENDING || it.status == DownloadStatus.PAUSED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedSongs: StateFlow<List<Song>> = musicRepository.getDownloadedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isWifiOnly: StateFlow<Boolean> = userPreferences.isWifiOnlyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun pauseDownload(songId: String) {
        viewModelScope.launch {
            downloadRepository.pauseDownload(songId)
        }
    }

    fun resumeDownload(songId: String) {
        viewModelScope.launch {
            downloadRepository.resumeDownload(songId)
        }
    }

    fun cancelDownload(songId: String) {
        viewModelScope.launch {
            downloadRepository.cancelDownload(songId)
        }
    }

    fun toggleWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setWifiOnly(enabled)
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            playbackManager.playSong(song)
            musicRepository.recordSongPlayed(song.id)
        }
    }
}

