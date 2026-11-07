package com.sonora.music.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SleepTimerManager(
    private val scope: CoroutineScope,
    private val onTimerExpired: () -> Unit,
    private val onVolumeAdjust: (Float) -> Unit
) {

    private var timerJob: Job? = null

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    fun startTimer(minutes: Int) {
        cancelTimer()
        val totalSeconds = minutes * 60L
        _remainingSeconds.value = totalSeconds
        _isActive.value = true

        timerJob = scope.launch(Dispatchers.Default) {
            var currentSec = totalSeconds
            while (isActive && currentSec > 0) {
                delay(1000)
                currentSec--
                _remainingSeconds.value = currentSec

                // Smooth fade-out in the last 15 seconds
                if (currentSec <= 15) {
                    val volume = (currentSec / 15f).coerceIn(0.1f, 1.0f)
                    onVolumeAdjust(volume)
                }
            }

            if (isActive && currentSec == 0L) {
                _isActive.value = false
                onTimerExpired()
                onVolumeAdjust(1.0f) // restore standard volume
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _remainingSeconds.value = 0L
        _isActive.value = false
        onVolumeAdjust(1.0f)
    }
}

