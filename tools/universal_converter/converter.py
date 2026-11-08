#!/usr/bin/env python3
"""
SONORA - Universal Multi-Format Audio Converter
-----------------------------------------------
Converts ANY media format (MP4, MKV, WEBM, WAV, FLAC, M4A, AAC, WMA, OGG, MP3)
into Ultra-Compact Opus audio (~1.2 MB per song, 64 kbps).
Extracts metadata & generates 'sonora_catalog.json' ready for Google Drive streaming.
"""

import os
import sys
import json
import argparse
import subprocess
from pathlib import Path

SUPPORTED_EXTENSIONS = {
    # Video containers with audio tracks
    ".mp4", ".mkv", ".webm", ".avi", ".mov", ".flv", ".m4v", ".ts", ".3gp",
    # Lossless & Lossy audio formats
    ".wav", ".flac", ".alac", ".aiff", ".mp3", ".m4a", ".aac", ".wma", ".ogg", ".opus"
}

def check_ffmpeg():
    try:
        subprocess.run(["ffmpeg", "-version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True)
        return True
    except Exception:
        print("\n" + "=" * 65)
        print("  [!] FFmpeg was not detected on your system.")
        print("=" * 65)
        print("  To install FFmpeg in 10 seconds on Windows:")
        print("  1. Open PowerShell or Command Prompt.")
        print("  2. Run: winget install Gyan.FFmpeg")
        print("  (Or download directly from https://ffmpeg.org)")
        print("=" * 65 + "\n")
        return False

def extract_metadata(file_path):
    """Attempt to extract title and artist using ffprobe, fallback to filename."""
    cmd = [
        "ffprobe", "-v", "quiet",
        "-print_format", "json",
        "-show_format",
        str(file_path)
    ]
    title = file_path.stem
    artist = "Unknown Artist"
    duration = 210

    try:
        res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, check=True)
        data = json.loads(res.stdout)
        tags = data.get("format", {}).get("tags", {})
        
        # Look for common tag variations
        title = tags.get("title") or tags.get("TITLE") or title
        artist = tags.get("artist") or tags.get("ARTIST") or tags.get("album_artist") or artist
        duration = int(float(data.get("format", {}).get("duration", 210)))
    except Exception:
        # Fallback to parsing "Artist - Title" from filename
        if " - " in file_path.stem:
            parts = file_path.stem.split(" - ", 1)
            artist = parts[0].strip()
            title = parts[1].strip()

    return title, artist, duration

def convert_file(input_path, output_dir, target_format="opus", bitrate="64k"):
    input_file = Path(input_path)
    safe_stem = input_file.stem.replace('"', '').replace("'", "")
    output_filename = f"{safe_stem}.{target_format}"
    output_path = Path(output_dir) / output_filename

    codec = "libopus" if target_format == "opus" else "libmp3lame"

    # FFmpeg command: strips video (-vn), converts audio stream to target codec and bitrate
    cmd = [
        "ffmpeg", "-y",
        "-i", str(input_file),
        "-vn",                    # Discard video streams (from MP4, MKV, etc.)
        "-c:a", codec,            # Target audio codec
        "-b:a", bitrate,          # Target bitrate (64k for Opus = ~1.2MB/song)
        "-vbr", "on",
        str(output_path),
        "-loglevel", "error"
    ]

    try:
        subprocess.run(cmd, check=True)
        if output_path.exists():
            size_kb = output_path.stat().st_size // 1024
            orig_size_kb = max(1, input_file.stat().st_size // 1024)
            savings = max(0, 100 - int((size_kb / orig_size_kb) * 100))
            print(f"  [✓] {input_file.name} ({orig_size_kb/1024:.1f} MB)")
            print(f"      -> {output_filename} ({size_kb/1024:.2f} MB) [{savings}% space saved]")
            
            title, artist, duration = extract_metadata(input_file)
            return {
                "id": f"song_{len(output_filename)}_{size_kb}",
                "title": title,
                "artist": artist,
                "duration_seconds": duration,
                "filename": output_filename,
                "size_kb": size_kb,
                "codec": target_format,
                "bitrate_kbps": int(bitrate.replace("k", ""))
            }
    except Exception as e:
        print(f"  [X] Failed to convert {input_file.name}: {e}")

    return None

def main():
    parser = argparse.ArgumentParser(description="SONORA Universal Multi-Format to Opus Audio Compressor")
    parser.add_argument("inputs", nargs="+", help="Files or folders containing MP4, WAV, FLAC, MP3, etc.")
    parser.add_argument("--output", "-o", default="sonora_opus_music", help="Output directory")
    parser.add_argument("--format", "-f", choices=["opus", "mp3"], default="opus", help="Target format (default: opus)")
    parser.add_argument("--bitrate", "-b", default="64k", help="Target bitrate (default: 64k for ~1.2MB Opus)")
    args = parser.parse_args()

    if not check_ffmpeg():
        sys.exit(1)

    out_path = Path(args.output).resolve()
    out_path.mkdir(parents=True, exist_ok=True)

    # Collect all valid files from inputs (files or folders)
    files_to_convert = []
    for item in args.inputs:
        p = Path(item)
        if p.is_dir():
            for root, _, files in os.walk(p):
                for f in files:
                    fp = Path(root) / f
                    if fp.suffix.lower() in SUPPORTED_EXTENSIONS:
                        files_to_convert.append(fp)
        elif p.is_file():
            if p.suffix.lower() in SUPPORTED_EXTENSIONS:
                files_to_convert.append(p)

    if not files_to_convert:
        print(f"\n[!] No supported media files found in the provided inputs.")
        print(f"    Supported formats: MP4, MKV, WEBM, WAV, FLAC, M4A, AAC, MP3, WMA, OGG")
        return

    print("=" * 65)
    print("  SONORA - Universal Multi-Format Audio Converter")
    print("=" * 65)
    print(f"Files to process : {len(files_to_convert)}")
    print(f"Target format    : {args.format.upper()} @ {args.bitrate} (~1.2 MB per song)")
    print(f"Destination      : {out_path}")
    print("=" * 65 + "\n")

    catalog_entries = []
    total_orig_bytes = 0
    total_new_bytes = 0

    for i, file_path in enumerate(files_to_convert, 1):
        print(f"[{i}/{len(files_to_convert)}] Converting: {file_path.name}")
        total_orig_bytes += file_path.stat().st_size
        track_meta = convert_file(file_path, out_path, args.format, args.bitrate)
        if track_meta:
            total_new_bytes += track_meta["size_kb"] * 1024
            catalog_entries.append(track_meta)

    # Generate / update sonora_catalog.json for Google Drive / Cloud streaming
    if catalog_entries:
        catalog_path = out_path / "sonora_catalog.json"
        existing_songs = []
        if catalog_path.exists():
            try:
                with open(catalog_path, "r", encoding="utf-8") as f:
                    old_data = json.load(f)
                    existing_songs = old_data.get("songs", [])
            except Exception:
                pass

        # Merge new songs with existing without duplicates
        existing_filenames = {s.get("filename") for s in existing_songs}
        for entry in catalog_entries:
            if entry["filename"] not in existing_filenames:
                existing_songs.append(entry)

        catalog_data = {
            "catalog_name": "Sonora Google Drive Library",
            "version": 1,
            "total_songs": len(existing_songs),
            "description": "Ultra-compact Opus audio library for real-time streaming",
            "songs": existing_songs
        }

        with open(catalog_path, "w", encoding="utf-8") as f:
            json.dump(catalog_data, f, indent=2, ensure_ascii=False)

    orig_mb = total_orig_bytes / (1024 * 1024)
    new_mb = total_new_bytes / (1024 * 1024)
    freed_mb = max(0, orig_mb - new_mb)

    print("\n" + "=" * 65)
    print(f"  ✓ CONVERSION COMPLETED: {len(catalog_entries)}/{len(files_to_convert)} files converted!")
    print(f"  Original size : {orig_mb:.1f} MB")
    print(f"  New Opus size : {new_mb:.1f} MB")
    print(f"  Space saved   : {freed_mb:.1f} MB ({(freed_mb/max(1,orig_mb))*100:.0f}% saved!)")
    print(f"  Catalog file  : {out_path / 'sonora_catalog.json'}")
    print("=" * 65)
    print("\nNext step: Upload this folder to Google Drive to stream with 0 MB storage!")

if __name__ == "__main__":
    main()
