/**
 * SONORA - High-Speed Zero-Dependency JioSaavn Downloader
 * --------------------------------------------------------
 * Downloads any JioSaavn playlist, album, or song into ultra-compact audio (M4A/AAC).
 * Requires NO external dependencies (uses built-in Node.js fetch, crypto, fs).
 */

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const DES_KEY = Buffer.from('38346591');

function decryptMediaUrl(encUrl) {
  try {
    const decipher = crypto.createDecipheriv('des-ecb', DES_KEY, null);
    decipher.setAutoPadding(true);
    let dec = decipher.update(encUrl.trim(), 'base64', 'utf8');
    dec += decipher.final('utf8');
    return dec;
  } catch (err) {
    console.error('Decryption failed:', err.message);
    return null;
  }
}

function cleanFilename(name) {
  return name.replace(/[\\/:*?"<>|]/g, '_').replace(/\s+/g, ' ').trim();
}

async function fetchPlaylist(token) {
  const url = `https://www.jiosaavn.com/api.php?__call=webapi.get&token=${encodeURIComponent(token)}&type=playlist&p=1&n=100&includeMetaTags=0&ctx=wap6dot0&api_version=4&_format=json&_marker=0`;
  const res = await fetch(url, {
    headers: {
      'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
      'Accept': 'application/json, text/plain, */*'
    }
  });
  if (!res.ok) throw new Error(`HTTP error ${res.status}`);
  return await res.json();
}

async function downloadFile(fileUrl, destPath) {
  const res = await fetch(fileUrl, {
    headers: {
      'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
    }
  });
  if (!res.ok) throw new Error(`Failed to download ${fileUrl}: ${res.statusText}`);
  
  const arrayBuffer = await res.arrayBuffer();
  const buffer = Buffer.from(arrayBuffer);
  fs.writeFileSync(destPath, buffer);
  return buffer.length;
}

async function main() {
  const targetToken = process.argv[2] || 'YMVMKJ9w0D-NE5K-G4If6Q__';
  const bitrate = process.argv[3] || '96'; // '48', '96', '160', '320'
  const outDir = path.resolve(__dirname, 'jiosaavn_downloaded_songs');

  if (!fs.existsSync(outDir)) {
    fs.mkdirSync(outDir, { recursive: true });
  }

  console.log('='.repeat(65));
  console.log('  SONORA - JioSaavn Ultra-Compact Audio Downloader');
  console.log('='.repeat(65));
  console.log(`Playlist Token : ${targetToken}`);
  console.log(`Quality / Rate : ${bitrate} kbps (Super lightweight for Redmi 12 5G)`);
  console.log(`Output Directory: ${outDir}\n`);

  console.log('[*] Fetching playlist metadata from JioSaavn...');
  const data = await fetchPlaylist(targetToken);

  const playlistTitle = data.title || 'JioSaavn Playlist';
  const songs = data.list || [];

  console.log(`[+] Found playlist: "${playlistTitle}" with ${songs.length} tracks.\n`);

  const catalog = {
    catalog_name: `JioSaavn - ${playlistTitle}`,
    version: 1,
    description: `${songs.length} songs imported from JioSaavn`,
    updated_at: new Date().toISOString(),
    songs: []
  };

  let totalBytes = 0;

  for (let i = 0; i < songs.length; i++) {
    const s = songs[i];
    const title = (s.title || 'Unknown').replace(/&amp;/g, '&');
    const artist = (s.more_info?.artistMap?.primary_artists?.length) 
      ? s.more_info.artistMap.primary_artists.map(a => a.name).join(', ')
      : (s.subtitle ? s.subtitle.split('-')[0].trim() : 'Unknown Artist');
    const album = s.more_info?.album ? s.more_info.album.replace(/&amp;/g, '&') : playlistTitle;
    const duration = parseInt(s.more_info?.duration || '180', 10);
    const coverArt = (s.image || '').replace('150x150', '500x500');

    console.log(`[${i + 1}/${songs.length}] "${title}" by ${artist}`);

    let rawMediaUrl = '';
    if (s.more_info?.encrypted_media_url) {
      rawMediaUrl = decryptMediaUrl(s.more_info.encrypted_media_url);
    }

    if (!rawMediaUrl) {
      console.log(`    [!] Could not decrypt stream URL for track ${i + 1}. Skipping.`);
      continue;
    }

    // Set desired bitrate in URL (e.g., _96.mp4 or _48.mp4)
    const targetMediaUrl = rawMediaUrl.replace(/_\d+\.mp4$/, `_${bitrate}.mp4`);
    const filename = cleanFilename(`${artist} - ${title}`) + '.m4a';
    const filePath = path.join(outDir, filename);

    try {
      console.log(`    Downloading stream (${bitrate} kbps)...`);
      const bytes = await downloadFile(targetMediaUrl, filePath);
      totalBytes += bytes;
      const sizeMB = (bytes / (1024 * 1024)).toFixed(2);
      console.log(`    ✓ Saved: ${filename} (${sizeMB} MB)`);

      catalog.songs.push({
        id: `jio_${s.id}`,
        title: title,
        artist: artist,
        album: album,
        duration_seconds: duration,
        filename: filename,
        stream_url: targetMediaUrl,
        size_kb: Math.round(bytes / 1024),
        codec: 'aac/m4a',
        bitrate_kbps: parseInt(bitrate, 10),
        artwork_url: coverArt
      });
    } catch (err) {
      console.error(`    [!] Download error: ${err.message}`);
    }
  }

  // Save sonora_catalog.json for cloud streaming or offline import
  const catalogPath = path.join(outDir, 'sonora_catalog.json');
  fs.writeFileSync(catalogPath, JSON.stringify(catalog, null, 2), 'utf-8');

  console.log('\n' + '='.repeat(65));
  console.log(`  ✓ ALL DONE! Downloaded ${catalog.songs.length} songs.`);
  console.log(`  Total space taken: ${(totalBytes / (1024 * 1024)).toFixed(2)} MB!`);
  console.log(`  Average per song : ${(totalBytes / catalog.songs.length / (1024 * 1024)).toFixed(2)} MB`);
  console.log(`  Catalog generated: ${catalogPath}`);
  console.log('='.repeat(65));
}

main().catch(err => {
  console.error('[!] Fatal error:', err);
  process.exit(1);
});
