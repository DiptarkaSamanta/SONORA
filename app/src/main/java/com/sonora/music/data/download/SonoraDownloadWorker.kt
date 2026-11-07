package com.sonora.music.data.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.sonora.music.R
import com.sonora.music.core.database.SonoraDatabase
import com.sonora.music.core.database.entity.DownloadStatus
import com.sonora.music.core.database.entity.SongEntity
import com.sonora.music.domain.model.StorageMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class SonoraDownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = SonoraDatabase.getInstance(context)
    private val downloadDao = db.downloadDao()
    private val songDao = db.songDao()
    private val okHttpClient = OkHttpClient()

    companion object {
        const val KEY_SONG_ID = "key_song_id"
        const val KEY_TITLE = "key_title"
        const val KEY_ARTIST = "key_artist"
        const val KEY_URL = "key_url"
        const val KEY_STORAGE_MODE = "key_storage_mode"
        const val KEY_ARTWORK_URL = "key_artwork_url"

        private const val CHANNEL_ID = "sonora_download_channel"
        private const val NOTIFICATION_ID_BASE = 4000
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val songId = inputData.getString(KEY_SONG_ID) ?: return@withContext Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: "Unknown Track"
        val artist = inputData.getString(KEY_ARTIST) ?: "Unknown Artist"
        val downloadUrl = inputData.getString(KEY_URL) ?: return@withContext Result.failure()
        val modeStr = inputData.getString(KEY_STORAGE_MODE) ?: StorageMode.BALANCED.name
        val storageMode = runCatching { StorageMode.valueOf(modeStr) }.getOrDefault(StorageMode.BALANCED)
        val artworkUrl = inputData.getString(KEY_ARTWORK_URL)

        val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            ?: context.filesDir
        val safeFileName = "${title.replace(Regex("[^a-zA-Z0-9.-]"), "_")}_${System.currentTimeMillis()}.mp3"
        val targetFile = File(musicDir, safeFileName)

        downloadDao.updateStatus(songId, DownloadStatus.DOWNLOADING)

        try {
            val request = Request.Builder().url(downloadUrl).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful || response.body == null) {
                downloadDao.updateStatus(songId, DownloadStatus.FAILED, "HTTP error: ${response.code}")
                return@withContext Result.failure()
            }

            val body = response.body!!
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            createNotificationChannel()
            val notificationId = NOTIFICATION_ID_BASE + (songId.hashCode() and 0x7FFF)

            body.byteStream().use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var lastUpdate = System.currentTimeMillis()

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (System.currentTimeMillis() - lastUpdate > 800) {
                            lastUpdate = System.currentTimeMillis()
                            downloadDao.updateProgress(songId, downloadedBytes, totalBytes)
                            setProgress(workDataOf("progress" to (downloadedBytes.toFloat() / totalBytes)))
                        }
                    }
                    output.flush()
                }
            }

            // Calculate fast hash for duplicate detection
            val md5 = MessageDigest.getInstance("MD5")
            val hash = runCatching {
                val header = ByteArray(65536)
                targetFile.inputStream().use {
                    val read = it.read(header)
                    if (read > 0) md5.update(header, 0, read)
                }
                md5.digest().joinToString("") { "%02x".format(it) }
            }.getOrNull()

            // Insert into Sonora song library
            val songEntity = SongEntity(
                id = songId,
                title = title,
                artist = artist,
                album = "Sonora Downloads",
                durationMs = 0L, // will be auto-updated on playback
                mediaUri = targetFile.toURI().toString(),
                localFilePath = targetFile.absolutePath,
                sizeBytes = targetFile.length(),
                mimeType = "audio/mpeg",
                bitrateKbps = storageMode.targetBitrateKbps,
                isFavorite = false,
                playCount = 0,
                lastPlayedTimestamp = 0L,
                dateAdded = System.currentTimeMillis(),
                isDownloaded = true,
                isLocal = true,
                storageMode = storageMode.name,
                audioHash = hash,
                artworkUri = artworkUrl,
                lyrics = null,
                sourceProvider = "Authorized Download",
                canDownloadLegally = false
            )
            songDao.insertSong(songEntity)
            downloadDao.updateStatus(songId, DownloadStatus.COMPLETED)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            targetFile.delete()
            downloadDao.updateStatus(songId, DownloadStatus.FAILED, e.localizedMessage)
            Result.failure()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sonora Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

