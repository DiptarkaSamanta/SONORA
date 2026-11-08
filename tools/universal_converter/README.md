# SONORA Universal Media to Opus Converter

Converts ANY video or audio file into **Ultra-Compact Opus Audio (64 kbps, ~1.2 MB per song)**.

---

### Supported Input Formats
- **Video Containers** (automatically strips video and extracts audio):
  `MP4`, `MKV`, `WEBM`, `AVI`, `MOV`, `FLV`, `M4V`, `3GP`, `TS`
- **Audio Files**:
  `WAV`, `FLAC`, `ALAC`, `AIFF`, `MP3`, `M4A`, `AAC`, `WMA`, `OGG`

---

### How to Use

#### Method 1: Drag & Drop (Fastest)
1. Select any songs or video files (or an entire folder of music) on your computer.
2. Drag and drop them directly onto [`convert_to_opus.bat`](convert_to_opus.bat).
3. The converter will automatically compress them into `.opus` and save them into the `sonora_opus_music/` folder.

#### Method 2: Double-Click
1. Double-click [`convert_to_opus.bat`](convert_to_opus.bat).
2. Enter the path of your folder or drag-and-drop into the terminal window.
3. Select your desired bitrate:
   - **[1] 64 kbps (Recommended)**: ~1.2 MB per song. Crystal-clear sound.
   - **[2] 48 kbps**: ~0.9 MB per song (maximum storage savings).
   - **[3] 96 kbps**: ~1.8 MB per song (audiophile grade).

---

### Prerequisites
- **FFmpeg**: If FFmpeg is not installed, running `convert_to_opus.bat` will offer to install it automatically via Windows `winget`, or you can run in PowerShell:
  ```powershell
  winget install Gyan.FFmpeg
  ```

---

### Google Drive Zero-Storage Streaming Workflow
1. Run `convert_to_opus.bat` on your downloaded songs.
2. It outputs your `.opus` files and generates `sonora_catalog.json`.
3. Upload the `sonora_opus_music/` folder to your **Google Drive**.
4. Set folder sharing to *"Anyone with the link can view"*.
5. In SONORA, connect your Google Drive:
   - Music streams directly from Google Drive in real-time.
   - Takes **0 MB** storage on your Redmi 12 5G!
   - Full support for **Random / Shuffle** playback or custom queue orders.
