@echo off
setlocal enabledelayedexpansion

echo ================================================================
echo   SONORA - Universal Multi-Format Audio Compressor
echo ================================================================
echo.
echo Converts ANY media file (MP4, MKV, WEBM, WAV, FLAC, M4A, AAC,
echo WMA, OGG, MP3) into tiny, crystal-clear Opus audio (~1.2 MB each).
echo.

where ffmpeg >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [!] FFmpeg is required to compress audio.
    echo     Install it on Windows by opening PowerShell and running:
    echo     winget install Gyan.FFmpeg
    echo     or download it from: https://ffmpeg.org
    echo.
    pause
    exit /b
)

set "INPUT_PATH=%~1"
if "%INPUT_PATH%"=="" (
    set /p INPUT_PATH="Enter the folder path or file (or drag and drop it here): "
)
set "INPUT_PATH=%INPUT_PATH:"=%"

if not exist "%INPUT_PATH%" (
    echo Error: "%INPUT_PATH%" does not exist!
    pause
    exit /b
)

python "%~dp0converter.py" "%INPUT_PATH%" --format opus --bitrate 64k

echo.
pause
