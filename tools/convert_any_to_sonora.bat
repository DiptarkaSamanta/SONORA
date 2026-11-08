@echo off
setlocal enabledelayedexpansion

title SONORA - Universal Audio to Opus Converter

:: Check for FFmpeg in PATH or WinGet packages
where ffmpeg >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "%LOCALAPPDATA%\Microsoft\WinGet\Packages" (
        for /r "%LOCALAPPDATA%\Microsoft\WinGet\Packages" %%i in (ffmpeg.exe) do (
            if exist "%%i" set "PATH=%%~dpi;!PATH!"
        )
    )
)

echo ================================================================
echo   SONORA - Universal Audio to Opus Converter (High Power)
echo ================================================================
echo.
echo Converts ANY audio or video files into ultra-small Opus (~1.2 MB).
echo Strips video completely to save maximum storage.
echo Output folder: %~dp0sonora_opus_music
echo.

:: Check if clean argument passed
if "%~1"=="--clean" (
    python "%~dp0converter.py" --clean
    pause
    exit /b
)

:: If items were dragged onto the batch file, use them directly
if not "%~1"=="" (
    echo Processing dragged files/folders...
    python "%~dp0converter.py" %* --bitrate 64k
    goto finished
)

echo Menu Options:
echo [1] Convert Songs (Enter multiple file or folder paths)
echo [2] Clear/Empty 'sonora_opus_music' folder (Run after uploading to GDrive)
echo.
set /p MENU_CHOICE="Select option (1 or 2, default is 1): "

if "%MENU_CHOICE%"=="2" (
    echo.
    echo Clearing 'sonora_opus_music' folder...
    python "%~dp0converter.py" --clean
    echo.
    echo All files in sonora_opus_music have been removed!
    pause
    exit /b
)

echo.
echo Tip: You can paste multiple file/folder paths separated by spaces!
echo.
set /p USER_INPUT="Enter file/folder path(s): "

if "%USER_INPUT%"=="" (
    echo [!] No paths entered. Exiting.
    pause
    exit /b
)

echo.
python "%~dp0converter.py" %USER_INPUT% --bitrate 64k

:finished
echo.
echo ================================================================
echo Upload your new .opus files to Google Drive:
echo https://drive.google.com/drive/folders/1mjeXjTTy-nliOOUG607rprdvdcnlKurP
echo.
set /p DEL_CHOICE="Have you uploaded these songs to Google Drive and want to delete local copies? (Y/N): "
if /i "%DEL_CHOICE%"=="Y" (
    python "%~dp0converter.py" --clean
    echo [✓] Local staging folder cleaned!
)

echo ================================================================
echo.
pause
