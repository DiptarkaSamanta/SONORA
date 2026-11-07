package com.sonora.music.data.repository

import com.sonora.music.core.datastore.UserPreferences
import com.sonora.music.data.storage.StorageOptimizer
import com.sonora.music.domain.model.DuplicateGroup
import com.sonora.music.domain.model.Song
import com.sonora.music.domain.model.StorageMode
import com.sonora.music.domain.model.StorageStats
import com.sonora.music.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class StorageRepositoryImpl(
    private val storageOptimizer: StorageOptimizer,
    private val userPreferences: UserPreferences
) : StorageRepository {

    override fun getStorageStatsFlow(): Flow<StorageStats> = flow {
        val currentMode = userPreferences.storageModeFlow.first()
        emit(storageOptimizer.getStorageStats(currentMode))
    }

    override suspend fun getStorageStats(): StorageStats {
        val currentMode = userPreferences.storageModeFlow.first()
        return storageOptimizer.getStorageStats(currentMode)
    }

    override suspend fun detectDuplicates(): List<DuplicateGroup> =
        storageOptimizer.detectDuplicates()

    override suspend fun removeDuplicateSongs(songsToDelete: List<Song>): Long =
        storageOptimizer.deleteDuplicateSongs(songsToDelete)

    override suspend fun cleanTemporaryCache(): Long =
        storageOptimizer.cleanTemporaryCache()

    override suspend fun setPreferredStorageMode(mode: StorageMode) {
        userPreferences.setStorageMode(mode)
    }

    override fun getPreferredStorageMode(): Flow<StorageMode> =
        userPreferences.storageModeFlow

    override suspend fun switchStorageLocation(useExternalSdCard: Boolean): Boolean {
        userPreferences.setUseExternalStorage(useExternalSdCard)
        return true
    }
}

