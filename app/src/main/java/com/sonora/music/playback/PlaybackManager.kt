package com.sonora.music.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sonora.music.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope
) {

    val exoPlayer: ExoPlayer by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true) // automatic audio focus management
            .setHandleAudioBecomingNoisy(true) // pause automatically when headphones unplugged
            .build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                shuffleModeEnabled = false
            }
    }

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _audioSessionId = MutableStateFlow(0)
    val audioSessionId: StateFlow<Int> = _audioSessionId.asStateFlow()

    private var originalQueue: List<Song> = emptyList()
    private var progressTrackingJob: Job? = null

    val audioEffectsController = AudioEffectsController()
    val sleepTimerManager = SleepTimerManager(
        scope = scope,
        onTimerExpired = { pause() },
        onVolumeAdjust = { volume -> exoPlayer.volume = volume }
    )

    init {
        setupPlayerListener()
        startProgressTracking()
        registerBecomingNoisyReceiver()
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                        _audioSessionId.value = exoPlayer.audioSessionId
                        audioEffectsController.attachToAudioSession(exoPlayer.audioSessionId)
                    }
                    Player.STATE_ENDED -> {
                        _isPlaying.value = false
                    }
                    else -> Unit
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = exoPlayer.currentMediaItemIndex
                val queueList = _queue.value
                if (index in queueList.indices) {
                    _currentSong.value = queueList[index]
                    _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                }
            }
        })
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        originalQueue = songs
        _queue.value = songs

        val mediaItems = songs.map { song ->
            val uri = Uri.parse(song.mediaUri)
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .setArtworkUri(song.artworkUri?.let { Uri.parse(it) })
                .build()

            MediaItem.Builder()
                .setMediaId(song.id)
                .setUri(uri)
                .setMediaMetadata(metadata)
                .build()
        }

        exoPlayer.setMediaItems(mediaItems, startIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()

        _currentSong.value = songs.getOrNull(startIndex)
        _isPlaying.value = true
    }

    fun playSong(song: Song) {
        val currentQueue = _queue.value
        val existingIndex = currentQueue.indexOfFirst { it.id == song.id }
        if (existingIndex >= 0) {
            exoPlayer.seekTo(existingIndex, 0L)
            exoPlayer.play()
        } else {
            playQueue(listOf(song), 0)
        }
    }

    fun playPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_ENDED) {
                exoPlayer.seekTo(0, 0L)
            }
            exoPlayer.play()
        }
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun resume() {
        exoPlayer.play()
    }

    fun stopPlayback() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _currentSong.value = null
        _isPlaying.value = false
        _currentPositionMs.value = 0L
        _durationMs.value = 0L
    }

    fun skipToNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (_repeatMode.value == Player.REPEAT_MODE_ALL && _queue.value.isNotEmpty()) {
            exoPlayer.seekTo(0, 0L)
            exoPlayer.play()
        }
    }

    fun skipToPrevious() {
        if (exoPlayer.currentPosition > 3000L) {
            // Restart current song if played more than 3 seconds
            exoPlayer.seekTo(0L)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun toggleShuffle() {
        val newShuffle = !_isShuffleEnabled.value
        _isShuffleEnabled.value = newShuffle
        exoPlayer.shuffleModeEnabled = newShuffle
    }

    fun cycleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = nextMode
        exoPlayer.repeatMode = nextMode
    }

    fun setPlaybackSpeed(speed: Float) {
        val safeSpeed = speed.coerceIn(0.5f, 2.0f)
        _playbackSpeed.value = safeSpeed
        exoPlayer.playbackParameters = PlaybackParameters(safeSpeed)
    }

    private fun startProgressTracking() {
        progressTrackingJob?.cancel()
        progressTrackingJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    _currentPositionMs.value = exoPlayer.currentPosition
                    if (exoPlayer.duration > 0) {
                        _durationMs.value = exoPlayer.duration
                    }
                }
                delay(250) // Smooth progress updates at 4Hz
            }
        }
    }

    private fun registerBecomingNoisyReceiver() {
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                    pause()
                }
            }
        }
        runCatching {
            context.registerReceiver(receiver, filter)
        }
    }

    fun release() {
        progressTrackingJob?.cancel()
        sleepTimerManager.cancelTimer()
        audioEffectsController.release()
        exoPlayer.release()
    }
}

