package com.sonora.music.core.di

import android.content.Context
import com.sonora.music.core.database.SonoraDatabase
import com.sonora.music.core.datastore.UserPreferences
import com.sonora.music.data.repository.DownloadRepositoryImpl
import com.sonora.music.data.repository.MusicRepositoryImpl
import com.sonora.music.data.repository.StorageRepositoryImpl
import com.sonora.music.data.scanner.LocalMediaScanner
import com.sonora.music.data.storage.DuplicateDetector
import com.sonora.music.data.storage.StorageOptimizer
import com.sonora.music.domain.repository.DownloadRepository
import com.sonora.music.domain.repository.MusicRepository
import com.sonora.music.domain.repository.StorageRepository
import com.sonora.music.playback.PlaybackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object ServiceLocator {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Volatile
    private var database: SonoraDatabase? = null

    @Volatile
    private var userPreferences: UserPreferences? = null

    @Volatile
    private var playbackManager: PlaybackManager? = null

    @Volatile
    private var musicRepository: MusicRepository? = null

    @Volatile
    private var storageRepository: StorageRepository? = null

    @Volatile
    private var downloadRepository: DownloadRepository? = null

    fun provideDatabase(context: Context): SonoraDatabase {
        return database ?: synchronized(this) {
            database ?: SonoraDatabase.getInstance(context).also { database = it }
        }
    }

    fun provideUserPreferences(context: Context): UserPreferences {
        return userPreferences ?: synchronized(this) {
            userPreferences ?: UserPreferences(context.applicationContext).also { userPreferences = it }
        }
    }

    fun providePlaybackManager(context: Context): PlaybackManager {
        return playbackManager ?: synchronized(this) {
            playbackManager ?: PlaybackManager(
                context.applicationContext,
                applicationScope
            ).also { playbackManager = it }
        }
    }

    fun provideMusicRepository(context: Context): MusicRepository {
        return musicRepository ?: synchronized(this) {
            val db = provideDatabase(context)
            val scanner = LocalMediaScanner(context.applicationContext)
            musicRepository ?: MusicRepositoryImpl(
                db.songDao(),
                db.playlistDao(),
                scanner
            ).also { musicRepository = it }
        }
    }

    fun provideStorageRepository(context: Context): StorageRepository {
        return storageRepository ?: synchronized(this) {
            val db = provideDatabase(context)
            val prefs = provideUserPreferences(context)
            val detector = DuplicateDetector()
            val optimizer = StorageOptimizer(context.applicationContext, db.songDao(), detector)
            storageRepository ?: StorageRepositoryImpl(
                optimizer,
                prefs
            ).also { storageRepository = it }
        }
    }

    fun provideDownloadRepository(context: Context): DownloadRepository {
        return downloadRepository ?: synchronized(this) {
            val db = provideDatabase(context)
            val prefs = provideUserPreferences(context)
            downloadRepository ?: DownloadRepositoryImpl(
                context.applicationContext,
                db.downloadDao(),
                prefs
            ).also { downloadRepository = it }
        }
    }
}

