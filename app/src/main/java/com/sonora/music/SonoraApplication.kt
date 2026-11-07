package com.sonora.music

import android.app.Application
import com.sonora.music.core.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SonoraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Eagerly initialize core dependencies
        ServiceLocator.provideDatabase(this)
        ServiceLocator.provideUserPreferences(this)
        ServiceLocator.providePlaybackManager(this)

        // Kick off lightweight background media scan & cloud catalog sync on app launch
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val repository = ServiceLocator.provideMusicRepository(this@SonoraApplication)
                repository.scanLocalMedia()
                repository.syncCloudMusicCatalog()
            }
        }
    }
}

