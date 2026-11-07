# 🎵 Transferring 1,000 Spotify Songs to SONORA Cloud (Ultra-Low Storage)

This guide shows you how to bring your **1,000 Spotify songs** into Sonora so they play:
- **Without ads or subscription fees**
- **Without delay or buffering**
- **Taking minimal storage space**: 1,000 songs take only **~1.2 GB** in Opus format instead of 10–12 GB in MP3!
- **Stored safely in the Cloud (Firebase / Google Drive)**: So whenever the app updates or changes, you never have to re-download or lose anything.

---

## Step 1: Export your 1,000 Spotify Songs list

You can export your Spotify Liked Songs or Playlists using any free metadata tool:
- **Option A (Instant Web tool)**: Go to [Spotlistr](https://www.spotlistr.com/export/spotify-playlist) or [TuneMyMusic](https://www.tunemymusic.com) -> Login with Spotify -> Click **Export to CSV**.
- Save the file as `my_spotify_songs.csv`.

---

## Step 2: Convert to Ultra-Compact Opus Audio (~1.2 MB each)

1. Open PowerShell or Command Prompt in `D:\projects\song app\tools\spotify_sync`:
   ```powershell
   pip install -r requirements.txt
   ```
2. Run the conversion script:
   ```powershell
   python spotify_to_sonora.py --csv my_spotify_songs.csv --output_dir sonora_music_cloud
   ```
   *Every song will be converted to crystal-clear 64kbps Opus audio (~1.2 MB per song) with album art and tags automatically embedded.*
   *A `sonora_catalog.json` file will be generated.*

---

## Step 3: Host the songs on Firebase Storage or Google Drive (Free)

### Using Firebase Storage (5 GB Free):
1. Go to [Firebase Console](https://console.firebase.google.com/) -> Create a project.
2. In the left menu, click **Storage** -> Click **Get Started** -> Select "Test mode" (allows read access).
3. Upload your `sonora_music_cloud` audio files and `sonora_catalog.json`.
4. Copy the public link to your `sonora_catalog.json`.

### Using Google Drive (15 GB Free):
1. Create a folder in Google Drive named `Sonora Songs`.
2. Upload the audio files and `sonora_catalog.json`.
3. Set the folder sharing to **"Anyone with the link can view"**.

---

## Step 4: Link your Cloud Catalog in SONORA

1. Open **SONORA** on your phone.
2. Go to **Storage Dashboard** -> **Cloud Music Database Sync**.
3. Paste the URL of your `sonora_catalog.json`.
4. Tap **Sync Now**.
5. All 1,000 songs will immediately appear in your **Library -> Cloud (~1MB)** tab!
   - You can stream any song on-demand with **0 MB storage used** on your phone.
   - Or tap **Download** on any song to save the tiny ~1.2 MB file for offline listening on planes or trains.
