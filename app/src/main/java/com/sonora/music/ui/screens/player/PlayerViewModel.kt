package com.sonora.music.ui.screens.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sonora.music.core.di.ServiceLocator
import com.sonora.music.domain.model.LyricsData
import com.sonora.music.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val playbackManager = ServiceLocator.providePlaybackManager(application)
    private val musicRepository = ServiceLocator.provideMusicRepository(application)
    private val storageRepository = ServiceLocator.provideStorageRepository(application)

    val currentSong: StateFlow<Song?> = playbackManager.currentSong
    val isPlaying: StateFlow<Boolean> = playbackManager.isPlaying
    val currentPositionMs: StateFlow<Long> = playbackManager.currentPositionMs
    val durationMs: StateFlow<Long> = playbackManager.durationMs
    val isShuffleEnabled: StateFlow<Boolean> = playbackManager.isShuffleEnabled
    val repeatMode: StateFlow<Int> = playbackManager.repeatMode
    val playbackSpeed: StateFlow<Float> = playbackManager.playbackSpeed
    val queue: StateFlow<List<Song>> = playbackManager.queue

    val audioEffectsController = playbackManager.audioEffectsController
    val sleepTimerManager = playbackManager.sleepTimerManager

    private val _lyrics = MutableStateFlow<LyricsData?>(null)
    val lyrics: StateFlow<LyricsData?> = _lyrics.asStateFlow()

    private val _isLoadingLyrics = MutableStateFlow(false)
    val isLoadingLyrics: StateFlow<Boolean> = _isLoadingLyrics.asStateFlow()

    init {
        // Automatically fetch synced/plain lyrics whenever a new song plays
        viewModelScope.launch {
            currentSong.collectLatest { song ->
                if (song != null) {
                    loadLyricsForSong(song)
                } else {
                    _lyrics.value = null
                }
            }
        }
    }

    private fun loadLyricsForSong(song: Song) {
        viewModelScope.launch {
            _isLoadingLyrics.value = true
            _lyrics.value = null

            // If song already has cached lyrics in DB
            if (!song.lyrics.isNullOrBlank()) {
                _lyrics.value = LyricsData.parseLrc(song.lyrics, song.title, song.artist)
                _isLoadingLyrics.value = false
                return@launch
            }

            // Fetch from LRCLIB API
            val fetched = musicRepository.fetchLyrics(song.title, song.artist)
            _lyrics.value = fetched
            _isLoadingLyrics.value = false
        }
    }

    fun playPause() = playbackManager.playPause()
    fun next() = playbackManager.skipToNext()
    fun previous() = playbackManager.skipToPrevious()
    fun seekTo(positionMs: Long) = playbackManager.seekTo(positionMs)
    fun toggleShuffle() = playbackManager.toggleShuffle()
    fun cycleRepeatMode() = playbackManager.cycleRepeatMode()
    fun setPlaybackSpeed(speed: Float) = playbackManager.setPlaybackSpeed(speed)

    fun toggleFavorite() {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            musicRepository.toggleFavorite(song.id, song.isFavorite)
        }
    }
}

