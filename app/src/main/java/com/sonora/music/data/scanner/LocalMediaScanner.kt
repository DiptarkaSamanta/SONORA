package com.sonora.music.data.scanner

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.sonora.music.core.database.entity.SongEntity
import com.sonora.music.domain.model.StorageMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

class LocalMediaScanner(private val context: Context) {

    suspend fun scanDeviceAudio(): List<SongEntity> = withContext(Dispatchers.IO) {
        val songList = mutableListOf<SongEntity>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.ALBUM_ID
        )

        // Only query real music files greater than 20 seconds and 300KB
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 20000"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val duration = cursor.getLong(durationCol)
                    val path = cursor.getString(dataCol)
                    val size = cursor.getLong(sizeCol)
                    val mime = cursor.getString(mimeCol) ?: "audio/mpeg"
                    val dateAdded = cursor.getLong(dateCol) * 1000
                    val albumId = cursor.getLong(albumIdCol)

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()
                    val albumArtUri = "content://media/external/audio/albumart/$albumId"

                    val (bitrate, hash) = extractMetadataFast(path, size)

                    val storageMode = StorageMode.fromBitrate(bitrate)

                    songList.add(
                        SongEntity(
                            id = "local_$id",
                            title = title,
                            artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                            album = if (album == "<unknown>") "Unknown Album" else album,
                            durationMs = duration,
                            mediaUri = contentUri,
                            localFilePath = path,
                            sizeBytes = size,
                            mimeType = mime,
                            bitrateKbps = bitrate,
                            isFavorite = false,
                            playCount = 0,
                            lastPlayedTimestamp = 0L,
                            dateAdded = dateAdded,
                            isDownloaded = true,
                            isLocal = true,
                            storageMode = storageMode.name,
                            audioHash = hash,
                            artworkUri = albumArtUri,
                            lyrics = null,
                            sourceProvider = "Device Storage",
                            canDownloadLegally = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        songList
    }

    /**
     * Computes a fast partial hash (first 64KB + size) to identify duplicate audio files rapidly
     * without thrashing device RAM or I/O.
     */
    private fun extractMetadataFast(filePath: String?, sizeBytes: Long): Pair<Int, String?> {
        if (filePath == null) return Pair(160, null)
        val file = File(filePath)
        if (!file.exists() || !file.canRead()) return Pair(160, null)

        var bitrate = 160
        var hash: String? = null

        try {
            // Fast partial MD5 of first 64KB
            val buffer = ByteArray(65536)
            file.inputStream().use { input ->
                val read = input.read(buffer)
                if (read > 0) {
                    val md = MessageDigest.getInstance("MD5")
                    md.update(buffer, 0, read)
                    // mix in length to ensure distinct files with identical headers don't collide
                    md.update(sizeBytes.toString().toByteArray())
                    hash = md.digest().joinToString("") { "%02x".format(it) }
                }
            }

            // Estimate bitrate if duration is available: (sizeBytes * 8) / (durationSec * 1000)
            // Or use retriever
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            if (!bitrateStr.isNullOrEmpty()) {
                val b = bitrateStr.toIntOrNull()
                if (b != null && b > 0) {
                    bitrate = b / 1000
                }
            }
            retriever.release()
        } catch (e: Exception) {
            // Fallback gracefully
        }

        return Pair(bitrate, hash)
    }
}

