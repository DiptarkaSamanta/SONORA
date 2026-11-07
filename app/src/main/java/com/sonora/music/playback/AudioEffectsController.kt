package com.sonora.music.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import com.sonora.music.domain.model.EqualizerBand
import com.sonora.music.domain.model.EqualizerPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioEffectsController {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _bands = MutableStateFlow<List<EqualizerBand>>(emptyList())
    val bands: StateFlow<List<EqualizerBand>> = _bands.asStateFlow()

    private val _currentPreset = MutableStateFlow<String>("Flat")
    val currentPreset: StateFlow<String> = _currentPreset.asStateFlow()

    private val _bassStrength = MutableStateFlow(0)
    val bassStrength: StateFlow<Int> = _bassStrength.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(0)
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()

    fun attachToAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        release()

        runCatching {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _isEnabled.value
            }

            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _isEnabled.value
                if (strengthSupported) {
                    setStrength(_bassStrength.value.toShort())
                }
            }

            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _isEnabled.value
                if (strengthSupported) {
                    setStrength(_virtualizerStrength.value.toShort())
                }
            }

            loadBands()
        }
    }

    private fun loadBands() {
        val eq = equalizer ?: return
        val count = eq.numberOfBands.toInt()
        val list = mutableListOf<EqualizerBand>()

        for (i in 0 until count) {
            val centerFreq = eq.getCenterFreq(i.toShort()) / 1000 // mHz to Hz
            val gain = eq.getBandLevel(i.toShort()) / 100f // mB to dB
            list.add(EqualizerBand(i, centerFreq, gain))
        }
        _bands.value = list
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        runCatching {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
        }
    }

    fun setBandGain(bandIndex: Int, gainDb: Float) {
        val eq = equalizer ?: return
        runCatching {
            val levelMb = (gainDb * 100).toInt().coerceIn(
                eq.bandLevelRange[0].toInt(),
                eq.bandLevelRange[1].toInt()
            ).toShort()
            eq.setBandLevel(bandIndex.toShort(), levelMb)

            _bands.value = _bands.value.map { band ->
                if (band.index == bandIndex) band.copy(gainDb = gainDb) else band
            }
            _currentPreset.value = "Custom"
        }
    }

    fun applyPreset(preset: EqualizerPreset) {
        _currentPreset.value = preset.name
        val eq = equalizer ?: return
        runCatching {
            val numBands = eq.numberOfBands.toInt().coerceAtMost(preset.bandGainsDb.size)
            for (i in 0 until numBands) {
                val gain = preset.bandGainsDb[i]
                val levelMb = (gain * 100).toInt().toShort()
                eq.setBandLevel(i.toShort(), levelMb)
            }
            loadBands()
        }
    }

    fun setBassBoost(strength: Int) {
        _bassStrength.value = strength.coerceIn(0, 1000)
        runCatching {
            bassBoost?.let {
                if (it.strengthSupported) {
                    it.setStrength(_bassStrength.value.toShort())
                }
            }
        }
    }

    fun setVirtualizer(strength: Int) {
        _virtualizerStrength.value = strength.coerceIn(0, 1000)
        runCatching {
            virtualizer?.let {
                if (it.strengthSupported) {
                    it.setStrength(_virtualizerStrength.value.toShort())
                }
            }
        }
    }

    fun release() {
        runCatching {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}

