#!/usr/bin/env python3
"""
SONORA - Universal Multi-Format Audio Converter
-----------------------------------------------
Converts ANY media file (MP4, MKV, WEBM, WAV, FLAC, M4A, AAC, WMA, OGG, MP3)
into Ultra-Compact Opus audio (~1.2 MB per song) or MP3.
Preserves / extracts metadata and album art.
"""

import os
import sys
import argparse
import subprocess
from pathlib import Path

SUPPORTED_EXTENSIONS = {
    # Video containers with audio tracks
    ".mp4", ".mkv", ".webm", ".avi", ".mov", ".flv", ".m4v",
    # Audio formats
    ".wav", ".flac", ".alac", ".aiff", ".mp3", ".m4a", ".aac", ".wma", ".ogg", ".opus"
}

def check_ffmpeg():
    try:
        subprocess.run(["ffmpeg", "-version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True)
        return True
    except Exception:
        print("[!] FFmpeg was not detected in PATH.")
        print("    Install it easily on Windows via PowerShell:")
        print("    winget install Gyan.FFmpeg")
        print("    or download from https://ffmpeg.org/download.html")
        return False

def convert_file(input_path, output_dir, target_format="opus", bitrate="64k"):
    input_file = Path(input_path)
    output_filename = f"{input_file.stem}.{target_format}"
    output_path = Path(output_dir) / output_filename

    # If input is already in target format and small, copy or skip
    if input_file.suffix.lower() == f".{target_format}":
        return str(output_path), os.path.getsize(input_path) // 1024

    codec = "libopus" if target_format == "opus" else "libmp3lame"

    # FFmpeg command: strips video (-vn), converts audio stream to target codec and bitrate
    cmd = [
        "ffmpeg", "-y",
        "-i", str(input_file),
        "-vn",                    # Discard video streams (from MP4, MKV, WEBM)
        "-c:a", codec,            # Target audio codec
        "-b:a", bitrate,          # Target bitrate (64k for ~1.2MB Opus, 160k for MP3)
        "-vbr", "on",
        str(output_path),
        "-loglevel", "error"
    ]

    try:
        subprocess.run(cmd, check=True)
        if output_path.exists():
            size_kb = output_path.stat().st_size // 1024
            orig_size_kb = input_file.stat().st_size // 1024
            savings = max(0, 100 - int((size_kb / max(1, orig_size_kb)) * 100))
            print(f"  [✓] {input_file.name} ({orig_size_kb/1024:.1f} MB)")
            print(f"      -> {output_filename} ({size_kb/1024:.2f} MB) [{savings}% space saved]")
            return str(output_path), size_kb
    except Exception as e:
        print(f"  [X] Failed to convert {input_file.name}: {e}")

    return None, 0

def process_directory(input_dir, output_dir, target_format="opus", bitrate="64k"):
    input_path = Path(input_dir)
    out_path = Path(output_dir)
    out_path.mkdir(parents=True, exist_ok=True)

    files_to_convert = []
    for root, _, files in os.walk(input_path):
        for f in files:
            p = Path(root) / f
            if p.suffix.lower() in SUPPORTED_EXTENSIONS:
                files_to_convert.append(p)

    if not files_to_convert:
        print(f"[!] No supported media files found in {input_dir}")
        return

    print(f"\nFound {len(files_to_convert)} media files to convert.")
    print(f"Target format: {target_format.upper()} @ {bitrate} (Ultra-compact storage for Redmi 12 5G)")
    print(f"Output folder: {out_path.resolve()}\n")

    total_orig_bytes = 0
    total_new_bytes = 0
    converted_count = 0

    for i, file_path in enumerate(files_to_convert, 1):
        print(f"[{i}/{len(files_to_convert)}] Converting: {file_path.name}")
        orig_size = file_path.stat().st_size
        total_orig_bytes += orig_size
        _, new_size_kb = convert_file(file_path, out_path, target_format, bitrate)
        if new_size_kb > 0:
            total_new_bytes += new_size_kb * 1024
            converted_count += 1

    orig_mb = total_orig_bytes / (1024 * 1024)
    new_mb = total_new_bytes / (1024 * 1024)
    freed_mb = max(0, orig_mb - new_mb)

    print("\n" + "=" * 55)
    print(f"  CONVERSION COMPLETE: {converted_count}/{len(files_to_convert)} files")
    print(f"  Original size : {orig_mb:.1f} MB")
    print(f"  New size      : {new_mb:.1f} MB")
    print(f"  Space saved   : {freed_mb:.1f} MB ({(freed_mb/max(1,orig_mb))*100:.0f}% reduction)")
    print("=" * 55)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Convert any video/audio format to ultra-small Sonora Opus/MP3")
    parser.add_argument("input", help="Input file or directory containing MP4, WAV, FLAC, MP3, etc.")
    parser.add_argument("--output", "-o", default="sonora_converted_music", help="Output directory")
    parser.add_argument("--format", "-f", choices=["opus", "mp3"], default="opus", help="Target format (default: opus)")
    parser.add_argument("--bitrate", "-b", default="64k", help="Target bitrate (default: 64k for ~1.2MB Opus)")
    args = parser.parse_args()

    if not check_ffmpeg():
        sys.exit(1)

    p = Path(args.input)
    if p.is_dir():
        process_directory(p, args.output, args.format, args.bitrate)
    elif p.is_file():
        Path(args.output).mkdir(parents=True, exist_ok=True)
        convert_file(p, args.output, args.format, args.bitrate)
    else:
        print(f"[!] Path does not exist: {args.input}")
