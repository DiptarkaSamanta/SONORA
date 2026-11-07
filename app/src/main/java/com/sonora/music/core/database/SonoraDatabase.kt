package com.sonora.music.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sonora.music.core.database.dao.DownloadDao
import com.sonora.music.core.database.dao.PlaylistDao
import com.sonora.music.core.database.dao.SongDao
import com.sonora.music.core.database.entity.DownloadEntity
import com.sonora.music.core.database.entity.PlaylistEntity
import com.sonora.music.core.database.entity.PlaylistSongCrossRef
import com.sonora.music.core.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        DownloadEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SonoraDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        private const val DB_NAME = "sonora_music.db"

        @Volatile
        private var instance: SonoraDatabase? = null

        fun getInstance(context: Context): SonoraDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SonoraDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}

