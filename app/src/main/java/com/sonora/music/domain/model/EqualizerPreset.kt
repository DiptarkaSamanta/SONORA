package com.sonora.music.domain.model

data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val gainDb: Float
) {
    val frequencyLabel: String
        get() = when {
            centerFreqHz >= 1000 -> "${centerFreqHz / 1000}kHz"
            else -> "${centerFreqHz}Hz"
        }
}

data class EqualizerPreset(
    val name: String,
    val bandGainsDb: List<Float> // 5 bands: 60Hz, 230Hz, 910Hz, 3600Hz, 14000Hz
) {
    companion object {
        val FLAT = EqualizerPreset("Flat", listOf(0f, 0f, 0f, 0f, 0f))
        val BASS_BOOST = EqualizerPreset("Bass Boost", listOf(6f, 4f, 1f, 0f, 0f))
        val ROCK = EqualizerPreset("Rock", listOf(4.5f, 2.5f, -1.0f, 2.0f, 4.0f))
        val POP = EqualizerPreset("Pop", listOf(-1.5f, 1.5f, 4.0f, 2.0f, -1.0f))
        val JAZZ = EqualizerPreset("Jazz", listOf(3.0f, 2.0f, 1.0f, 2.0f, 3.5f))
        val EDM = EqualizerPreset("EDM", listOf(5.0f, 3.5f, 0.0f, 3.0f, 4.5f))
        val VOCAL = EqualizerPreset("Vocal Booster", listOf(-2.0f, 1.0f, 5.0f, 3.0f, 0.0f))
        val ACOUSTIC = EqualizerPreset("Acoustic", listOf(3.5f, 2.0f, 1.5f, 2.5f, 3.0f))

        val ALL_PRESETS = listOf(FLAT, BASS_BOOST, ROCK, POP, JAZZ, EDM, VOCAL, ACOUSTIC)
    }
}

