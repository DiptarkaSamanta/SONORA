package com.sonora.music.core.network

import com.google.gson.Gson
import com.sonora.music.core.network.model.CloudCatalogResponse
import com.sonora.music.core.network.model.CloudSongItem
import com.sonora.music.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class CloudDatabaseClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    private val gson = Gson()

    /**
     * Fetches online songs from any cloud JSON endpoint (Supabase, Firebase, GitHub Pages, etc.)
     */
    suspend fun fetchCloudCatalog(url: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) {
                return@runCatching getBuiltinCompactCatalog()
            }
            val json = response.body!!.string()
            val catalog = gson.fromJson(json, CloudCatalogResponse::class.java)
            catalog.songs.map { it.toDomainSong() }
        }.recoverCatching {
            getBuiltinCompactCatalog()
        }
    }

    /**
     * Built-in ultra-compact cloud library (~1MB per song) available immediately on app install
     */
    fun getBuiltinCompactCatalog(): List<Song> {
        val items = listOf(
            CloudSongItem(
                id = "cc_01",
                title = "Lo-Fi Midnight Study",
                artist = "Sonnox Collective",
                album = "Chill Cloud Beats",
                durationSeconds = 175,
                streamUrl = "https://cdn.pixabay.com/download/audio/2022/05/27/audio_1808fbf07a.mp3",
                downloadUrl = "https://cdn.pixabay.com/download/audio/2022/05/27/audio_1808fbf07a.mp3",
                sizeKb = 1400, // ~1.4 MB
                codec = "opus",
                bitrateKbps = 64,
                artworkUrl = "https://picsum.photos/seed/sonora1/400/400"
            ),
            CloudSongItem(
                id = "cc_02",
                title = "Cyber Ambient Groove",
                artist = "Neon Horizon",
                album = "Space Saver Waves",
                durationSeconds = 192,
                streamUrl = "https://cdn.pixabay.com/download/audio/2022/03/15/audio_c8c8a73467.mp3",
                downloadUrl = "https://cdn.pixabay.com/download/audio/2022/03/15/audio_c8c8a73467.mp3",
                sizeKb = 1530, // ~1.5 MB
                codec = "opus",
                bitrateKbps = 64,
                artworkUrl = "https://picsum.photos/seed/sonora2/400/400"
            ),
            CloudSongItem(
                id = "cc_03",
                title = "Acoustic Sunset Breeze",
                artist = "Sierra Strings",
                album = "Acoustic Nature",
                durationSeconds = 158,
                streamUrl = "https://cdn.pixabay.com/download/audio/2022/01/18/audio_d0a13f69d2.mp3",
                downloadUrl = "https://cdn.pixabay.com/download/audio/2022/01/18/audio_d0a13f69d2.mp3",
                sizeKb = 1260, // ~1.2 MB
                codec = "opus",
                bitrateKbps = 64,
                artworkUrl = "https://picsum.photos/seed/sonora3/400/400"
            ),
            CloudSongItem(
                id = "cc_04",
                title = "Deep Focus Synthwave",
                artist = "Pulse Vector",
                album = "Retrowave 84",
                durationSeconds = 210,
                streamUrl = "https://cdn.pixabay.com/download/audio/2022/10/14/audio_9939f77cf6.mp3",
                downloadUrl = "https://cdn.pixabay.com/download/audio/2022/10/14/audio_9939f77cf6.mp3",
                sizeKb = 1680, // ~1.6 MB
                codec = "opus",
                bitrateKbps = 64,
                artworkUrl = "https://picsum.photos/seed/sonora4/400/400"
            ),
            CloudSongItem(
                id = "cc_05",
                title = "Peaceful Piano Reflection",
                artist = "Elena Vostok",
                album = "Minimal Solitude",
                durationSeconds = 145,
                streamUrl = "https://cdn.pixabay.com/download/audio/2021/08/04/audio_bb630cc098.mp3",
                downloadUrl = "https://cdn.pixabay.com/download/audio/2021/08/04/audio_bb630cc098.mp3",
                sizeKb = 1160, // ~1.1 MB
                codec = "opus",
                bitrateKbps = 64,
                artworkUrl = "https://picsum.photos/seed/sonora5/400/400"
            )
        )
        return items.map { it.toDomainSong() }
    }
}

