@echo off
setlocal enabledelayedexpansion

title SONORA - Universal Audio to Opus Converter

echo ================================================================
echo   SONORA - Universal Multi-Format Audio to Opus Converter
echo ================================================================
echo.
echo Converts ANY video or audio file (MP4, MKV, WAV, FLAC, M4A, MP3, etc.)
echo into Ultra-Compact Opus audio (~1.2 MB per song).
echo.

:: Check for FFmpeg
where ffmpeg >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [!] FFmpeg is required to compress and convert media.
    echo.
    set /p AUTO_INSTALL="Would you like to automatically install FFmpeg now using winget? (Y/N): "
    if /i "!AUTO_INSTALL!"=="Y" (
        echo.
        echo Installing FFmpeg via winget, please wait...
        winget install -e --id Gyan.FFmpeg --accept-package-agreements --accept-source-agreements
        echo.
        echo [!] Installation complete. Please close and re-open this script.
        pause
        exit /b
    ) else (
        echo.
        echo You can install it manually by running in PowerShell:
        echo winget install Gyan.FFmpeg
        echo or downloading from: https://ffmpeg.org
        echo.
        pause
        exit /b
    )
)

:: Get input files or folders
set "INPUT_ITEMS=%*"

if "%INPUT_ITEMS%"=="" (
    echo Tip: You can drag and drop your music files or folders directly onto this window!
    echo.
    set /p INPUT_ITEMS="Enter file/folder path (or drag and drop here): "
)

:: Strip surrounding quotes from single input if present
set "INPUT_ITEMS=%INPUT_ITEMS:"=%"

if "%INPUT_ITEMS%"=="" (
    echo Error: No input specified!
    pause
    exit /b
)

echo.
echo Choose Compression Quality:
echo [1] Opus 64 kbps (Recommended: ~1.2 MB per song, crystal clear audio)
echo [2] Opus 48 kbps (Ultra Space Saver: ~0.9 MB per song)
echo [3] Opus 96 kbps (High Fidelity: ~1.8 MB per song)
echo.
set /p QUALITY_SELECT="Select option (1-3, default is 1): "

set "BITRATE=64k"
if "%QUALITY_SELECT%"=="2" set "BITRATE=48k"
if "%QUALITY_SELECT%"=="3" set "BITRATE=96k"

echo.
echo Converting files to Ultra-Compact Opus (%BITRATE%)...
echo Output will be stored in: %~dp0sonora_opus_music
echo.

python "%~dp0converter.py" "%INPUT_ITEMS%" --output "%~dp0sonora_opus_music" --format opus --bitrate %BITRATE%

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [!] Python conversion failed. Trying direct FFmpeg batch conversion...
    if not exist "%~dp0sonora_opus_music" mkdir "%~dp0sonora_opus_music"
    
    if exist "%INPUT_ITEMS%\*" (
        for %%F in ("%INPUT_ITEMS%\*.*") do (
            echo Converting: %%~nxF
            ffmpeg -y -i "%%F" -vn -c:a libopus -b:a %BITRATE% -vbr on "%~dp0sonora_opus_music\%%~nF.opus" -loglevel error
        )
    ) else (
        for %%F in (%INPUT_ITEMS%) do (
            echo Converting: %%~nxF
            ffmpeg -y -i "%%F" -vn -c:a libopus -b:a %BITRATE% -vbr on "%~dp0sonora_opus_music\%%~nF.opus" -loglevel error
        )
    )
    echo Direct FFmpeg conversion finished!
)

echo.
echo ================================================================
echo Done! All converted .opus files are saved in:
echo %~dp0sonora_opus_music
echo.
echo Next Steps:
echo 1. Upload the 'sonora_opus_music' folder to Google Drive.
echo 2. SONORA streams directly from Drive with ZERO phone storage used!
echo ================================================================
echo.
pause
