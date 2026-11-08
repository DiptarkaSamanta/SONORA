#!/usr/bin/env python3
"""
SONORA - High-Performance Universal Audio-to-Opus Converter
------------------------------------------------------------
Fast batch converter for ANY video or audio format (MP4, MKV, WEBM, WAV, FLAC, M4A, AAC, MP3, etc.).
Extracts audio only (strips video completely) and compresses into ultra-small Opus 64kbps (~1.2 MB/song).
Stores outputs in 'tools/sonora_opus_music'.
"""

import os
import sys
import glob
import json
import argparse
import subprocess
from pathlib import Path

SUPPORTED_EXTENSIONS = {
    # Video containers (audio extracted, video discarded)
    ".mp4", ".mkv", ".webm", ".avi", ".mov", ".flv", ".m4v", ".ts", ".3gp",
    # Audio formats
    ".wav", ".flac", ".alac", ".aiff", ".mp3", ".m4a", ".aac", ".wma", ".ogg", ".opus"
}

FFMPEG_CMD = "ffmpeg"

def find_ffmpeg():
    global FFMPEG_CMD
    # 1. System PATH
    try:
        subprocess.run(["ffmpeg", "-version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True)
        return True
    except Exception:
        pass

    # 2. Check WinGet Packages directory
    local_app_data = os.environ.get("LOCALAPPDATA", "")
    if local_app_data:
        winget_dir = Path(local_app_data) / "Microsoft" / "WinGet" / "Packages"
        if winget_dir.exists():
            for p in winget_dir.glob("**/ffmpeg.exe"):
                if p.is_file():
                    FFMPEG_CMD = str(p)
                    os.environ["PATH"] = str(p.parent) + os.pathsep + os.environ.get("PATH", "")
                    return True
    return False

def clean_filename(name):
    # Remove problematic characters for storage and Drive
    for ch in ['"', "'", ":", "?", "*", "<", ">", "|"]:
        name = name.replace(ch, "")
    return name.strip()

def convert_single_file(input_file, out_dir, bitrate="64k"):
    inp = Path(input_file)
    safe_name = clean_filename(inp.stem)
    out_file = Path(out_dir) / f"{safe_name}.opus"

    cmd = [
        FFMPEG_CMD, "-y",
        "-i", str(inp),
        "-vn",              # Discard video completely
        "-c:a", "libopus",  # Ultra-small Opus codec
        "-b:a", bitrate,    # 64 kbps (~1.2 MB per song)
        "-vbr", "on",
        str(out_file),
        "-loglevel", "error"
    ]

    try:
        subprocess.run(cmd, check=True)
        if out_file.exists():
            new_size_kb = out_file.stat().st_size // 1024
            orig_size_kb = max(1, inp.stat().st_size // 1024)
            pct = max(0, 100 - int((new_size_kb / orig_size_kb) * 100))
            print(f"  [✓] {inp.name} ({orig_size_kb/1024:.1f} MB)")
            print(f"      -> {out_file.name} ({new_size_kb/1024:.2f} MB) [{pct}% space saved]")
            return {
                "id": f"song_{out_file.stat().st_mtime_ns}_{new_size_kb}",
                "title": safe_name,
                "artist": "Sonora Cloud",
                "filename": out_file.name,
                "size_kb": new_size_kb,
                "codec": "opus",
                "bitrate_kbps": int(bitrate.replace("k", ""))
            }
    except Exception as e:
        print(f"  [X] Failed to convert {inp.name}: {e}")
    return None

def main():
    parser = argparse.ArgumentParser(description="SONORA Universal Audio-to-Opus Converter")
    parser.add_argument("inputs", nargs="*", help="File or folder paths to convert")
    parser.add_argument("--output", "-o", default=None, help="Output folder")
    parser.add_argument("--bitrate", "-b", default="64k", help="Bitrate (default: 64k)")
    parser.add_argument("--clean", action="store_true", help="Clear the output folder")
    args = parser.parse_args()

    script_dir = Path(__file__).parent.resolve()
    out_dir = Path(args.output).resolve() if args.output else (script_dir / "sonora_opus_music")
    out_dir.mkdir(parents=True, exist_ok=True)

    if args.clean:
        for f in out_dir.iterdir():
            if f.is_file():
                f.unlink()
        print(f"[✓] Cleared all files from {out_dir}")
        return

    if not find_ffmpeg():
        print("[!] FFmpeg not found. Please install via: winget install Gyan.FFmpeg")
        sys.exit(1)

    # Gather all file paths from inputs
    raw_inputs = args.inputs
    if not raw_inputs:
        print("Tip: You can drag and drop multiple songs or folders directly onto convert_any_to_sonora.bat!")
        entered = input("Enter file or folder paths (separated by space or quotes): ").strip()
        if not entered:
            print("[!] No paths provided.")
            return
        # Parse inputs respecting quotes
        import shlex
        try:
            raw_inputs = shlex.split(entered)
        except Exception:
            raw_inputs = [entered]

    collected_files = []
    for item in raw_inputs:
        # Clean quotes
        clean_item = item.strip('\'"')
        p = Path(clean_item)
        if p.is_dir():
            for root, _, files in os.walk(p):
                for f in files:
                    fp = Path(root) / f
                    if fp.suffix.lower() in SUPPORTED_EXTENSIONS:
                        collected_files.append(fp)
        elif p.is_file():
            if p.suffix.lower() in SUPPORTED_EXTENSIONS:
                collected_files.append(p)
        else:
            # Try glob
            matched = glob.glob(clean_item)
            for m in matched:
                mp = Path(m)
                if mp.is_file() and mp.suffix.lower() in SUPPORTED_EXTENSIONS:
                    collected_files.append(mp)

    # Remove duplicates
    collected_files = list(dict.fromkeys(collected_files))

    if not collected_files:
        print("[!] No supported media files found in the provided paths.")
        print(f"    Supported formats: MP4, MKV, WEBM, WAV, FLAC, M4A, AAC, MP3, WMA, OGG")
        return

    print("=" * 65)
    print("  SONORA - High-Performance Audio-to-Opus Converter")
    print("=" * 65)
    print(f"Files to convert: {len(collected_files)}")
    print(f"Target format   : OPUS @ {args.bitrate} (~1.2 MB per song)")
    print(f"Output folder   : {out_dir}")
    print("=" * 65 + "\n")

    catalog_entries = []
    total_orig = 0
    total_new = 0

    for i, fp in enumerate(collected_files, 1):
        print(f"[{i}/{len(collected_files)}] Converting: {fp.name}")
        total_orig += fp.stat().st_size
        res = convert_single_file(fp, out_dir, args.bitrate)
        if res:
            total_new += res["size_kb"] * 1024
            catalog_entries.append(res)

    # Update sonora_catalog.json
    if catalog_entries:
        cat_file = out_dir / "sonora_catalog.json"
        cat_data = {
            "catalog_name": "Sonora Google Drive Library",
            "version": 1,
            "total_songs": len(catalog_entries),
            "songs": catalog_entries
        }
        with open(cat_file, "w", encoding="utf-8") as f:
            json.dump(cat_data, f, indent=2, ensure_ascii=False)

    orig_mb = total_orig / (1024 * 1024)
    new_mb = total_new / (1024 * 1024)
    saved_mb = max(0, orig_mb - new_mb)

    print("\n" + "=" * 65)
    print(f"  ✓ DONE: Converted {len(catalog_entries)}/{len(collected_files)} songs into tiny Opus files!")
    print(f"  Original size : {orig_mb:.1f} MB")
    print(f"  New Opus size : {new_mb:.1f} MB")
    print(f"  Storage saved : {saved_mb:.1f} MB ({(saved_mb/max(1,orig_mb))*100:.0f}% space saved!)")
    print(f"  Saved in      : {out_dir}")
    print("=" * 65)
    print("\nNext step: Upload these .opus files into your Google Drive folder:")
    print("https://drive.google.com/drive/folders/1mjeXjTTy-nliOOUG607rprdvdcnlKurP")
    print()

if __name__ == "__main__":
    main()
