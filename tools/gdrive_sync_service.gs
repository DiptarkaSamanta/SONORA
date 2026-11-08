/**
 * SONORA - 100% Real-Time Dynamic Google Drive Sync Service
 * ------------------------------------------------------------
 * FREE Google Apps Script Web API (Hosted directly by Google).
 * Scans your Google Drive folder dynamically in real-time.
 * NO JSON files needed! Supports 1,000+ songs automatically!
 *
 * HOW TO DEPLOY (Takes 30 seconds):
 * 1. Open https://script.google.com and click "New project".
 * 2. Paste this entire code into the editor.
 * 3. Click "Deploy" > "New deployment".
 * 4. Select type: "Web app".
 * 5. Set "Execute as": "Me" and "Who has access": "Anyone".
 * 6. Click "Deploy" and copy your Web App URL.
 * 7. Paste that URL into Sonora's Sync settings. Done!
 *
 * Whenever you add, rename, or delete songs in Google Drive,
 * Sonora automatically reflects the changes in real-time!
 */

function doGet(e) {
  const FOLDER_ID = (e && e.parameter && e.parameter.folderId) || "1mjeXjTTy-nliOOUG607rprdvdcnlKurP";

  try {
    const folder = DriveApp.getFolderById(FOLDER_ID);
    const files = folder.getFiles();
    const songs = [];

    while (files.hasNext()) {
      const file = files.next();
      const name = file.getName();
      const mime = file.getMimeType();

      // Filter audio / video or opus files
      if (name.toLowerCase().endsWith('.opus') || mime.startsWith('audio/') || mime.startsWith('video/')) {
        const id = file.getId();
        const sizeBytes = file.getSize();
        const sizeMb = (sizeBytes / (1024 * 1024)).toFixed(1) + " MB";

        // Clean title & artist from filename e.g. "Darkside Alan Walker ft. Au Ra.opus"
        let cleanName = name.replace(/\.[^/.]+$/, ""); // strip extension
        let artist = "Google Drive";
        let title = cleanName;

        if (cleanName.includes(" - ")) {
          const parts = cleanName.split(" - ");
          artist = parts[0].trim();
          title = parts[1].trim();
        }

        songs.push({
          id: "gdrive_" + id,
          drive_id: id,
          title: title,
          artist: artist,
          album: "Sonora Google Drive",
          size: sizeMb,
          size_bytes: sizeBytes,
          format: "OPUS 64k",
          duration: "3:30",
          download_url: "https://drive.google.com/uc?export=download&id=" + id,
          stream_url: "https://drive.google.com/uc?export=download&id=" + id,
          art: "https://picsum.photos/seed/" + id.substring(0, 5) + "/200/200"
        });
      }
    }

    // Sort alphabetically by title
    songs.sort((a, b) => a.title.localeCompare(b.title));

    const response = {
      status: "success",
      folder_id: FOLDER_ID,
      total_songs: songs.length,
      updated_at: new Date().toISOString(),
      songs: songs
    };

    return ContentService.createTextOutput(JSON.stringify(response))
      .setMimeType(ContentService.MimeType.JSON);

  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({
      status: "error",
      message: err.toString()
    })).setMimeType(ContentService.MimeType.JSON);
  }
}
