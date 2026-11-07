package com.sonora.music.ui.screens.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sonora.music.core.di.ServiceLocator
import com.sonora.music.domain.model.Playlist
import com.sonora.music.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryTab {
    SONGS,
    CLOUD,
    PLAYLISTS,
    ALBUMS,
    ARTISTS
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = ServiceLocator.provideMusicRepository(application)
    private val downloadRepository = ServiceLocator.provideDownloadRepository(application)
    private val storageRepository = ServiceLocator.provideStorageRepository(application)
    private val playbackManager = ServiceLocator.providePlaybackManager(application)

    val allSongs: StateFlow<List<Song>> = musicRepository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cloudSongs: StateFlow<List<Song>> = musicRepository.getCloudSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = musicRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<String>> = musicRepository.getAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<String>> = musicRepository.getArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(LibraryTab.SONGS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    fun setTab(tab: LibraryTab) {
        _selectedTab.value = tab
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

    fun playAll() {
        val songs = allSongs.value
        if (songs.isNotEmpty()) {
            playQueue(songs, 0)
        }
    }

    fun shuffleAll() {
        val songs = allSongs.value
        if (songs.isNotEmpty()) {
            val shuffled = songs.shuffled()
            playQueue(shuffled, 0)
        }
    }

    fun scanMedia() {
        viewModelScope.launch {
            _isScanning.value = true
            musicRepository.scanLocalMedia()
            musicRepository.syncCloudMusicCatalog()
            _isScanning.value = false
        }
    }

    fun syncCloudCatalog(customUrl: String? = null) {
        viewModelScope.launch {
            _isScanning.value = true
            musicRepository.syncCloudMusicCatalog(customUrl)
            _isScanning.value = false
        }
    }

    fun downloadSong(song: Song) {
        viewModelScope.launch {
            val mode = com.sonora.music.domain.model.StorageMode.SPACE_SAVER
            downloadRepository.enqueueDownload(song, mode)
        }
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            musicRepository.createPlaylist(name.trim())
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(song.id, song.isFavorite)
        }
    }
}

