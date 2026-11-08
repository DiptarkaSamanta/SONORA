package com.sonora.music.ui.screens.storage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sonora.music.core.di.ServiceLocator
import com.sonora.music.domain.model.DuplicateGroup
import com.sonora.music.domain.model.StorageMode
import com.sonora.music.domain.model.StorageStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StorageViewModel(application: Application) : AndroidViewModel(application) {

    private val storageRepository = ServiceLocator.provideStorageRepository(application)
    private val musicRepository = ServiceLocator.provideMusicRepository(application)
    private val userPreferences = ServiceLocator.provideUserPreferences(application)

    private val _storageStats = MutableStateFlow(StorageStats())
    val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

    private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<DuplicateGroup>> = _duplicateGroups.asStateFlow()

    private val _isCleaning = MutableStateFlow(false)
    val isCleaning: StateFlow<Boolean> = _isCleaning.asStateFlow()

    private val _isSyncingCloud = MutableStateFlow(false)
    val isSyncingCloud: StateFlow<Boolean> = _isSyncingCloud.asStateFlow()

    val cloudCatalogUrl: StateFlow<String> = userPreferences.cloudCatalogUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://drive.google.com/drive/folders/1mjeXjTTy-nliOOUG607rprdvdcnlKurP?usp=sharing")

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val currentMode: StateFlow<StorageMode> = storageRepository.getPreferredStorageMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StorageMode.BALANCED)

    init {
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            _storageStats.value = storageRepository.getStorageStats()
            _duplicateGroups.value = storageRepository.detectDuplicates()
        }
    }

    fun selectStorageMode(mode: StorageMode) {
        viewModelScope.launch {
            storageRepository.setPreferredStorageMode(mode)
            refreshStats()
            _statusMessage.value = "Storage mode changed to ${mode.title}"
        }
    }

    fun cleanAllDuplicates() {
        viewModelScope.launch {
            _isCleaning.value = true
            val duplicates = _duplicateGroups.value.flatMap { it.duplicates }
            val freed = storageRepository.removeDuplicateSongs(duplicates)
            val freedMb = freed / (1024 * 1024)
            _statusMessage.value = "Successfully cleaned $freedMb MB of duplicate tracks!"
            refreshStats()
            _isCleaning.value = false
        }
    }

    fun cleanTempCache() {
        viewModelScope.launch {
            _isCleaning.value = true
            val freed = storageRepository.cleanTemporaryCache()
            val freedMb = freed / (1024 * 1024)
            _statusMessage.value = "Cleared $freedMb MB of temporary image & network cache!"
            refreshStats()
            _isCleaning.value = false
        }
    }

    fun syncCloudCatalog(customUrl: String) {
        viewModelScope.launch {
            _isSyncingCloud.value = true
            if (customUrl.isNotBlank()) {
                userPreferences.setCloudCatalogUrl(customUrl.trim())
            }
            val result = musicRepository.syncCloudMusicCatalog(customUrl.trim().ifEmpty { null })
            result.onSuccess { count ->
                _statusMessage.value = "Successfully synced $count songs from cloud database!"
                refreshStats()
            }.onFailure {
                _statusMessage.value = "Failed to sync cloud catalog: ${it.localizedMessage}"
            }
            _isSyncingCloud.value = false
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}

