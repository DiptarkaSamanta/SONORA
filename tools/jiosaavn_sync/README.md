# 🎵 JioSaavn Playlist to Sonora (Ultra-Low Storage)

You can download and import any public **JioSaavn playlist, album, or song** directly into Sonora.

### Highlights:
- **Ultra-Small Storage**: Every song is compressed into **64 kbps Opus audio (~1.2 MB each)** instead of 8–10 MB MP3s.
- **Embedded Metadata**: Song title, artists, album, and high-resolution cover artwork are automatically embedded into every track.
- **No Ads, No Subscription**: Listen offline or stream from your personal Firebase/Google Drive cloud.

---

## How to Download Your JioSaavn Playlist:

1. In the **JioSaavn app** or website, open your playlist:
   - Tap **Share** ➔ **Copy Link** (e.g. `https://www.jiosaavn.com/featured/...` or `https://www.jiosaavn.com/s/playlist/...`).
2. On your computer, open `D:\projects\song app\tools\jiosaavn_sync\`.
3. Double-click [`download_jiosaavn_playlist.bat`](file:///d:/projects/song%20app/tools/jiosaavn_sync/download_jiosaavn_playlist.bat).
4. Paste your JioSaavn link and press **Enter**.
5. The tool will download and compress each track into `jiosaavn_downloaded_songs/` and generate a `sonora_catalog.json` file.

---

## What to do with the downloaded songs:

- **Option A (Offline on your phone)**:
  Copy the `jiosaavn_downloaded_songs` folder directly to your Redmi 12 5G (Internal Storage or SD Card). Sonora will find and play them offline immediately.
  *100 songs will take only ~120 MB!*

- **Option B (Stream with 0 MB storage from Firebase/GDrive)**:
  Upload the folder and `sonora_catalog.json` to your Firebase Storage or Google Drive. In Sonora, go to **Storage Dashboard** ➔ **Sync Cloud Songs**. Now you can stream all of them without taking up phone storage!
