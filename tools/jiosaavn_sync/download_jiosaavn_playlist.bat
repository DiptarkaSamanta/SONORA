@echo off
setlocal enabledelayedexpansion

echo ================================================================
echo   SONORA - JioSaavn Ultra-Compact Playlist Downloader
echo ================================================================
echo.
echo Paste any public JioSaavn playlist, album, or track link.
echo It will automatically download all songs and save them in
echo ultra-compact audio (~2 MB each) with full tags and cover art!
echo.

set "URL_INPUT=%~1"
if "%URL_INPUT%"=="" (
    set /p URL_INPUT="Enter JioSaavn Playlist or Track URL: "
)
set "URL_INPUT=%URL_INPUT:"=%"

if "%URL_INPUT%"=="" (
    echo Error: No URL entered!
    pause
    exit /b
)

:: Extract token if full URL was provided
set "TOKEN=%URL_INPUT%"
for %%A in ("%URL_INPUT%") do set "LAST_PART=%%~nxA"
if not "%LAST_PART%"=="" (
    set "TOKEN=%LAST_PART%"
)

echo.
echo [1] 48 kbps  (Space Saver  - ~1.5 MB per song, saves maximum storage)
echo [2] 96 kbps  (Balanced     - ~2.5 MB per song, great clarity) [DEFAULT]
echo [3] 160 kbps (High Quality - ~4.5 MB per song)
echo [4] 320 kbps (Extreme      - ~9.0 MB per song)
echo.
set /p QUALITY_CHOICE="Select Quality (1-4, default is 2): "

set "BITRATE=96"
if "%QUALITY_CHOICE%"=="1" set "BITRATE=48"
if "%QUALITY_CHOICE%"=="2" set "BITRATE=96"
if "%QUALITY_CHOICE%"=="3" set "BITRATE=160"
if "%QUALITY_CHOICE%"=="4" set "BITRATE=320"

echo.
echo Downloading playlist using SONORA Engine...
echo.

node --openssl-legacy-provider "%~dp0download_jiosaavn.js" "%TOKEN%" "%BITRATE%"

if %ERRORLEVEL% NEQ 0 (
    echo Node.js download failed, attempting Python fallback...
    python "%~dp0jiosaavn_to_sonora.py" "%URL_INPUT%" --output "%~dp0jiosaavn_downloaded_songs" --bitrate "%BITRATE%k"
)

echo.
echo ================================================================
echo Done! All songs are ready in:
echo %~dp0jiosaavn_downloaded_songs
echo.
echo You can:
echo 1. Connect Redmi 12 5G and copy the folder to your phone!
echo 2. Or upload to Google Drive / Firebase for 0 MB cloud streaming!
echo ================================================================
pause
