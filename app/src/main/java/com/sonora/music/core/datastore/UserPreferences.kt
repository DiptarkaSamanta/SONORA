package com.sonora.music.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sonora.music.domain.model.StorageMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sonora_preferences")

class UserPreferences(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val KEY_STORAGE_MODE = stringPreferencesKey("preferred_storage_mode")
        val KEY_WIFI_ONLY = booleanPreferencesKey("wifi_only_downloads")
        val KEY_AUTO_DUP_CHECK = booleanPreferencesKey("auto_duplicate_check")
        val KEY_EQ_ENABLED = booleanPreferencesKey("equalizer_enabled")
        val KEY_EQ_PRESET = stringPreferencesKey("equalizer_preset")
        val KEY_BASS_STRENGTH = intPreferencesKey("bass_boost_strength")
        val KEY_VIRTUALIZER_STRENGTH = intPreferencesKey("virtualizer_strength")
        val KEY_PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val KEY_LAST_SONG_ID = stringPreferencesKey("last_played_song_id")
        val KEY_LAST_POSITION = longPreferencesKey("last_playback_position")
        val KEY_OFFLINE_MODE = booleanPreferencesKey("offline_only_mode")
        val KEY_USE_EXTERNAL_STORAGE = booleanPreferencesKey("use_external_storage")
    }

    val storageModeFlow: Flow<StorageMode> = dataStore.data.map { prefs ->
        val name = prefs[KEY_STORAGE_MODE] ?: StorageMode.BALANCED.name
        runCatching { StorageMode.valueOf(name) }.getOrDefault(StorageMode.BALANCED)
    }

    suspend fun setStorageMode(mode: StorageMode) {
        dataStore.edit { it[KEY_STORAGE_MODE] = mode.name }
    }

    val isWifiOnlyFlow: Flow<Boolean> = dataStore.data.map { it[KEY_WIFI_ONLY] ?: true }

    suspend fun setWifiOnly(enabled: Boolean) {
        dataStore.edit { it[KEY_WIFI_ONLY] = enabled }
    }

    val autoDuplicateCheckFlow: Flow<Boolean> = dataStore.data.map { it[KEY_AUTO_DUP_CHECK] ?: true }

    suspend fun setAutoDuplicateCheck(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_DUP_CHECK] = enabled }
    }

    val equalizerEnabledFlow: Flow<Boolean> = dataStore.data.map { it[KEY_EQ_ENABLED] ?: false }

    suspend fun setEqualizerEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_EQ_ENABLED] = enabled }
    }

    val equalizerPresetFlow: Flow<String> = dataStore.data.map { it[KEY_EQ_PRESET] ?: "Flat" }

    suspend fun setEqualizerPreset(presetName: String) {
        dataStore.edit { it[KEY_EQ_PRESET] = presetName }
    }

    val bassBoostStrengthFlow: Flow<Int> = dataStore.data.map { it[KEY_BASS_STRENGTH] ?: 0 }

    suspend fun setBassBoostStrength(strength: Int) {
        dataStore.edit { it[KEY_BASS_STRENGTH] = strength }
    }

    val virtualizerStrengthFlow: Flow<Int> = dataStore.data.map { it[KEY_VIRTUALIZER_STRENGTH] ?: 0 }

    suspend fun setVirtualizerStrength(strength: Int) {
        dataStore.edit { it[KEY_VIRTUALIZER_STRENGTH] = strength }
    }

    val playbackSpeedFlow: Flow<Float> = dataStore.data.map { it[KEY_PLAYBACK_SPEED] ?: 1.0f }

    suspend fun setPlaybackSpeed(speed: Float) {
        dataStore.edit { it[KEY_PLAYBACK_SPEED] = speed }
    }

    val isOfflineModeFlow: Flow<Boolean> = dataStore.data.map { it[KEY_OFFLINE_MODE] ?: false }

    suspend fun setOfflineMode(enabled: Boolean) {
        dataStore.edit { it[KEY_OFFLINE_MODE] = enabled }
    }

    val useExternalStorageFlow: Flow<Boolean> = dataStore.data.map { it[KEY_USE_EXTERNAL_STORAGE] ?: false }

    suspend fun setUseExternalStorage(useExternal: Boolean) {
        dataStore.edit { it[KEY_USE_EXTERNAL_STORAGE] = useExternal }
    }

    val lastPlayedSongIdFlow: Flow<String?> = dataStore.data.map { it[KEY_LAST_SONG_ID] }

    suspend fun saveLastPlaybackState(songId: String, positionMs: Long) {
        dataStore.edit {
            it[KEY_LAST_SONG_ID] = songId
            it[KEY_LAST_POSITION] = positionMs
        }
    }
}

