@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo   SONORA - Quick MP3 to Opus Audio Compressor
echo ========================================================
echo.
echo This tool compresses large MP3 files (8-10 MB each) into
echo tiny Opus audio files (~1.2 MB each) with crystal clear sound.
echo.

where ffmpeg >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [!] FFmpeg is required to compress audio.
    echo     You can install it quickly by running in PowerShell:
    echo     winget install Gyan.FFmpeg
    echo     or download from https://ffmpeg.org
    echo.
    pause
    exit /b
)

set /p INPUT_FOLDER="Enter the folder path containing your MP3 files (or drag and drop it here): "
set "INPUT_FOLDER=%INPUT_FOLDER:"=%"

if not exist "%INPUT_FOLDER%" (
    echo Error: Folder "%INPUT_FOLDER%" does not exist!
    pause
    exit /b
)

set "OUTPUT_FOLDER=%INPUT_FOLDER%\sonora_compressed_opus"
mkdir "%OUTPUT_FOLDER%" 2>nul

echo.
echo Compressing MP3s into ultra-compact Opus (64 kbps)...
echo Output directory: %OUTPUT_FOLDER%
echo.

set COUNT=0
for %%F in ("%INPUT_FOLDER%\*.mp3") do (
    set /a COUNT+=1
    echo [!COUNT!] Compressing: %%~nxF...
    ffmpeg -y -i "%%F" -c:a libopus -b:a 64k -vbr on "%OUTPUT_FOLDER%\%%~nF.opus" -loglevel error
)

echo.
echo ========================================================
echo   COMPLETED!
echo   Processed %COUNT% files into:
echo   %OUTPUT_FOLDER%
echo ========================================================
pause

