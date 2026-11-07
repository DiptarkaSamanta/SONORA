# SONORA – Smart Offline & Online Music Player for Android

**SONORA** is a modern, high-performance, ad-free music player built for Android with an offline-first architecture, legal online discovery, background playback engine, and an intelligent storage management system.

Custom-tailored and specifically optimized for the **Redmi 12 5G** (Snapdragon 4 Gen 2, MIUI 14 / HyperOS, Android 13/14).

---

## Architecture Overview

SONORA is designed following clean architecture and modern Android development best practices:

```
com.sonora.music
├── core
│   ├── database/          # Room DB (SonoraDatabase, Songs, Playlists, Downloads)
│   ├── datastore/         # DataStore preferences (Storage mode, Equalizer, Audio presets)
│   ├── di/                # ServiceLocator dependency injection
│   └── network/           # Retrofit & OkHttp (Jamendo CC Discovery & LRCLIB Synced Lyrics)
├── domain
│   ├── model/             # Domain models (Song, StorageMode, Lyrics, EqualizerPreset)
│   └── repository/        # Clean domain repository contracts
├── data
│   ├── download/          # WorkManager background download worker
│   ├── repository/        # Repository implementations
│   ├── scanner/           # Scoped MediaStore audio scanner & MD5 partial hashing
│   └── storage/           # StorageOptimizer & DuplicateDetector heuristics
├── playback
│   ├── AudioEffectsController.kt # Equalizer, Bass Boost, 3D Virtualizer
│   ├── PlaybackManager.kt        # Media3 ExoPlayer orchestration & AudioFocus
│   ├── SleepTimerManager.kt      # Smooth fade-out sleep timer
│   └── SonoraMediaService.kt     # MediaSessionService & Foreground playback
├── xiaomi
│   └── XiaomiOptimizationHelper.kt # MIUI / HyperOS battery & autostart dispatchers
└── ui
    ├── components/        # MiniPlayer, SongItem, GlassmorphicCard, WaveformVisualizer
    ├── navigation/        # Bottom navigation, SonoraNavGraph, Animated full player
    ├── screens/           # Home, Search, Library, Downloads, Storage, Full Player
    └── theme/             # AMOLED-friendly palette (Black, Purple, Electric Blue, Magenta)
```

---

## Key Features

### 1. Smart Storage Optimization (Highest Priority)
- **Three Audio Storage Modes**:
  - **High Quality**: Lossless FLAC / 320 kbps MP3 (~2.4 MB/min).
  - **Balanced**: 160 kbps AAC / 128 kbps Opus (~1.2 MB/min, 50% storage savings with pristine perceptual audio).
  - **Space Saver**: 96 kbps Opus / AAC (~0.72 MB/min, 70% storage savings for maximum library size).
- **Storage Estimation Engine**: Computes exact file size before starting any download.
- **Intelligent Duplicate Audio Detector**:
  - Fast 64KB header MD5 hashing + length fingerprinting.
  - Heuristic audio matching with normalized track title, artist name, and duration tolerance (+/- 2.5 seconds).
  - Identifies duplicate files and enables one-click cleanup of lower-quality duplicates while preserving the best audio file.
- **Smart Cache Cleaner**: Cleans artwork and network cache without deleting downloaded music files.

### 2. Modern Playback Engine & Background Service
- **Android Media3 ExoPlayer**:
  - Complete `MediaSessionService` implementation with foreground notification (`mediaPlayback`).
  - Lock-screen controls, notification media controls, seekbar, and favorite button.
  - Automatic audio focus management (transient loss, phone calls, volume ducking).
  - `AUDIO_BECOMING_NOISY` receiver: automatically pauses playback when headphones or Bluetooth disconnect.
  - Bluetooth headset and wired earphone button control.
- **Built-in Equalizer & Audio FX**:
  - 5-band frequency sliders (-12 dB to +12 dB).
  - Hardware DSP Bass Boost and 3D Virtualizer.
  - Presets: Flat, Bass Boost, Rock, Pop, Jazz, EDM, Vocal, Acoustic.
- **Sleep Timer**:
  - 15, 30, 45, 60 minutes or custom duration.
  - Automatic 15-second smooth volume fade-out before pausing playback.

### 3. Online Music Discovery & Synchronized Lyrics
- **Jamendo API Integration**:
  - Discover and stream legal Creative Commons and royalty-free music.
  - Authorized legal direct downloads with target storage mode compression.
- **LRCLIB Integration**:
  - Live synchronized (karaoke-style) lyrics with auto-scrolling to the active line.
  - Tap any lyric line to seek directly to that point in the song.
  - Search songs by remembering a lyric fragment.

### 4. Redmi 12 5G & Xiaomi HyperOS / MIUI Specific Optimizations
The Redmi 12 5G features a Qualcomm Snapdragon 4 Gen 2 processor with 4GB/6GB/8GB RAM and MIUI/HyperOS background task killers. Sonora incorporates:
1. **Direct Settings Dispatcher**:
   - `XiaomiOptimizationHelper.openBatterySaverSettings()`: Directly launches the MIUI HiddenAppsConfig screen to select **No restrictions**.
   - `XiaomiOptimizationHelper.openAutostartSettings()`: Directly opens the MIUI Autostart manager so Sonora stays alive when phone is locked or while gaming.
2. **Foreground Service Configuration**:
   - Declares `foregroundServiceType="mediaPlayback"` and `WAKE_LOCK` to prevent CPU throttling when screen turns off.
3. **RAM & Storage Optimization**:
   - Partial file hashing (first 64KB) prevents loading multi-megabyte audio files into RAM during duplicate scans on the 4GB/6GB RAM variant.
   - UFS 2.2 storage friendly buffer streaming.

---

## Build and Installation Instructions

### Prerequisites
- Android Studio Ladybug (2024.2+) or Hedgehog+
- JDK 17 (recommended: Eclipse Temurin 17 or Android Studio Embedded JDK 17)
- Android SDK Platform 35
- Gradle 8.7+

### Step-by-Step Build

1. Open Android Studio.
2. Choose **Open** and select the folder:
   ```
   D:\projects\song app
   ```
3. Allow Gradle to sync the project dependencies.
4. Build the Debug APK using Android Studio terminal:
   ```powershell
   ./gradlew assembleDebug
   ```
5. The generated APK will be located at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Installing on Redmi 12 5G
1. Enable **Developer Options** on your Redmi 12 5G:
   - Go to **Settings** -> **About phone** -> Tap **MIUI version** / **OS version** 7 times.
2. Go to **Settings** -> **Additional settings** -> **Developer options**:
   - Enable **USB debugging**.
   - Enable **Install via USB**.
3. Connect your Redmi 12 5G via USB cable and run:
   ```powershell
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## Redmi 12 5G Background Playback Optimization Guide

To guarantee 100% uninterrupted playback while gaming or screen locked on MIUI / HyperOS:

1. **Battery Saver**:
   - In Sonora, tap the **Redmi 12 5G Optimization** banner on the Home screen or go to:
   - Phone **Settings** -> **Apps** -> **Manage apps** -> **SONORA** -> **Battery saver** -> Choose **No restrictions**.
2. **Autostart**:
   - Tap **Autostart** in the banner or go to:
   - Phone **Security app** -> **Manage apps** -> **Permissions** -> **Autostart** -> Enable **SONORA**.
3. **Lock App in Recent Apps**:
   - Open Sonora.
   - Open Recent Apps screen (swipe up and hold).
   - Press and hold the Sonora card -> Tap the **Padlock icon** to prevent HyperOS from clearing it when freeing RAM.

