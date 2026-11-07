package com.sonora.music.domain.repository

import com.sonora.music.domain.model.DuplicateGroup
import com.sonora.music.domain.model.Song
import com.sonora.music.domain.model.StorageMode
import com.sonora.music.domain.model.StorageStats
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    fun getStorageStatsFlow(): Flow<StorageStats>
    suspend fun getStorageStats(): StorageStats
    suspend fun detectDuplicates(): List<DuplicateGroup>
    suspend fun removeDuplicateSongs(songsToDelete: List<Song>): Long // returns freed bytes
    suspend fun cleanTemporaryCache(): Long // returns freed bytes
    suspend fun setPreferredStorageMode(mode: StorageMode)
    fun getPreferredStorageMode(): Flow<StorageMode>
    suspend fun switchStorageLocation(useExternalSdCard: Boolean): Boolean
}

