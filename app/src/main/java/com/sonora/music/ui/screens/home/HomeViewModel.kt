package com.sonora.music.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sonora.music.core.di.ServiceLocator
import com.sonora.music.domain.model.Song
import com.sonora.music.domain.model.StorageMode
import com.sonora.music.xiaomi.XiaomiOptimizationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = ServiceLocator.provideMusicRepository(application)
    private val storageRepository = ServiceLocator.provideStorageRepository(application)
    private val playbackManager = ServiceLocator.providePlaybackManager(application)
    private val userPreferences = ServiceLocator.provideUserPreferences(application)

    val recentlyPlayed: StateFlow<List<Song>> = musicRepository.getRecentlyPlayedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = musicRepository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed: StateFlow<List<Song>> = musicRepository.getMostPlayedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storageMode: StateFlow<StorageMode> = storageRepository.getPreferredStorageMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StorageMode.BALANCED)

    private val _trendingOnline = MutableStateFlow<List<Song>>(emptyList())
    val trendingOnline: StateFlow<List<Song>> = _trendingOnline.asStateFlow()

    private val _isXiaomiNeedsOptimization = MutableStateFlow(false)
    val isXiaomiNeedsOptimization: StateFlow<Boolean> = _isXiaomiNeedsOptimization.asStateFlow()

    init {
        checkXiaomiOptimization()
        loadTrendingOnline()
    }

    private fun checkXiaomiOptimization() {
        if (XiaomiOptimizationHelper.isXiaomiDevice) {
            val ignoring = XiaomiOptimizationHelper.isIgnoringBatteryOptimizations(getApplication())
            _isXiaomiNeedsOptimization.value = !ignoring
        }
    }

    private fun loadTrendingOnline() {
        viewModelScope.launch {
            musicRepository.getTrendingOnlineMusic().onSuccess { songs ->
                _trendingOnline.value = songs
            }
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            playbackManager.playSong(song)
            musicRepository.recordSongPlayed(song.id)
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int) {
        viewModelScope.launch {
            playbackManager.playQueue(songs, startIndex)
            songs.getOrNull(startIndex)?.let { musicRepository.recordSongPlayed(it.id) }
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(song.id, song.isFavorite)
        }
    }

    fun triggerXiaomiBatteryFix() {
        XiaomiOptimizationHelper.openBatterySaverSettings(getApplication())
        _isXiaomiNeedsOptimization.value = false
    }

    fun triggerXiaomiAutostart() {
        XiaomiOptimizationHelper.openAutostartSettings(getApplication())
    }
}

