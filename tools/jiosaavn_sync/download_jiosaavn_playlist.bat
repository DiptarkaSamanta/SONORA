@echo off
setlocal enabledelayedexpansion

echo ================================================================
echo   SONORA - JioSaavn Ultra-Compact Playlist Downloader
echo ================================================================
echo.
echo Paste any public JioSaavn playlist, album, or track link.
echo It will automatically download all songs and compress them into
echo ultra-small Opus audio (~1.2 MB each) with album art and tags.
echo.

where ffmpeg >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [!] FFmpeg is required to compress audio.
    echo     Install it on Windows via PowerShell:
    echo     winget install Gyan.FFmpeg
    echo     or download from https://ffmpeg.org
    echo.
    pause
    exit /b
)

set /p PLAYLIST_URL="Enter JioSaavn Playlist/Album URL: "
set "PLAYLIST_URL=%PLAYLIST_URL:"=%"

if "%PLAYLIST_URL%"=="" (
    echo Error: No URL entered!
    pause
    exit /b
)

echo.
echo Processing playlist and compressing into ultra-small Opus 64k...
echo.

python "%~dp0jiosaavn_to_sonora.py" "%PLAYLIST_URL%" --output "%~dp0jiosaavn_downloaded_songs" --format opus --bitrate 64k

echo.
echo ================================================================
echo Done! All songs are stored in:
echo %~dp0jiosaavn_downloaded_songs
echo.
echo You can:
echo 1. Copy them to your Redmi 12 5G (takes only ~1.2 MB per song).
echo 2. Or upload them to Firebase / Google Drive to stream with 0 MB!
echo ================================================================
pause
