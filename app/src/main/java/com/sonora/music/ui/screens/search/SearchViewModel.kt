package com.sonora.music.ui.screens.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sonora.music.core.di.ServiceLocator
import com.sonora.music.domain.model.LyricsData
import com.sonora.music.domain.model.Song
import com.sonora.music.domain.model.StorageMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SearchFilter {
    ALL,
    LOCAL,
    ONLINE_DISCOVERY,
    BY_LYRICS
}

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepository = ServiceLocator.provideMusicRepository(application)
    private val downloadRepository = ServiceLocator.provideDownloadRepository(application)
    private val storageRepository = ServiceLocator.provideStorageRepository(application)
    private val playbackManager = ServiceLocator.providePlaybackManager(application)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedFilter = MutableStateFlow(SearchFilter.ALL)
    val selectedFilter: StateFlow<SearchFilter> = _selectedFilter.asStateFlow()

    private val _localResults = MutableStateFlow<List<Song>>(emptyList())
    val localResults: StateFlow<List<Song>> = _localResults.asStateFlow()

    private val _onlineResults = MutableStateFlow<List<Song>>(emptyList())
    val onlineResults: StateFlow<List<Song>> = _onlineResults.asStateFlow()

    private val _lyricsResults = MutableStateFlow<List<LyricsData>>(emptyList())
    val lyricsResults: StateFlow<List<LyricsData>> = _lyricsResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val storageMode: StateFlow<StorageMode> = storageRepository.getPreferredStorageMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StorageMode.BALANCED)

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _localResults.value = emptyList()
            _onlineResults.value = emptyList()
            _lyricsResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(350) // Debounce typing
            performSearch(newQuery)
        }
    }

    fun setFilter(filter: SearchFilter) {
        _selectedFilter.value = filter
        if (_query.value.isNotBlank()) {
            performSearch(_query.value)
        }
    }

    private fun performSearch(q: String) {
        viewModelScope.launch {
            _isSearching.value = true

            // Local search
            if (_selectedFilter.value == SearchFilter.ALL || _selectedFilter.value == SearchFilter.LOCAL) {
                val local = musicRepository.searchLocalSongs(q).first()
                _localResults.value = local
            } else {
                _localResults.value = emptyList()
            }

            // Online Discovery search (Creative Commons & Royalty Free)
            if (_selectedFilter.value == SearchFilter.ALL || _selectedFilter.value == SearchFilter.ONLINE_DISCOVERY) {
                musicRepository.searchOnlineMusic(q).onSuccess {
                    _onlineResults.value = it
                }.onFailure {
                    _onlineResults.value = emptyList()
                }
            } else {
                _onlineResults.value = emptyList()
            }

            // Lyrics search
            if (_selectedFilter.value == SearchFilter.BY_LYRICS) {
                musicRepository.searchLyrics(q).onSuccess {
                    _lyricsResults.value = it
                }.onFailure {
                    _lyricsResults.value = emptyList()
                }
            } else {
                _lyricsResults.value = emptyList()
            }

            _isSearching.value = false
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            playbackManager.playSong(song)
            musicRepository.recordSongPlayed(song.id)
        }
    }

    fun downloadSong(song: Song) {
        viewModelScope.launch {
            downloadRepository.enqueueDownload(song, storageMode.value)
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(song.id, song.isFavorite)
        }
    }
}

