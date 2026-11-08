@echo off
title SONORA - Universal Audio to Opus Converter (High Power)

:: 1. Locate Python executable
set "PYTHON_CMD="
where python >nul 2>nul
if %ERRORLEVEL% EQU 0 set "PYTHON_CMD=python"
if not defined PYTHON_CMD (
    where py >nul 2>nul
    if %ERRORLEVEL% EQU 0 set "PYTHON_CMD=py"
)
if not defined PYTHON_CMD (
    if exist "%LOCALAPPDATA%\Programs\Python\Python314\python.exe" (
        set "PYTHON_CMD=%LOCALAPPDATA%\Programs\Python\Python314\python.exe"
    )
)
if not defined PYTHON_CMD (
    echo [ERROR] Python was not found on your system!
    echo Please install Python from https://www.python.org/downloads/
    pause
    exit /b 1
)

:: 2. Locate FFmpeg (WinGet Gyan.FFmpeg or system PATH)
where ffmpeg >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "%LOCALAPPDATA%\Microsoft\WinGet\Packages" (
        for /d %%D in ("%LOCALAPPDATA%\Microsoft\WinGet\Packages\Gyan.FFmpeg*") do (
            for /r "%%D" %%i in (ffmpeg.exe) do (
                if exist "%%i" set "PATH=%%~dpi;%PATH%"
            )
        )
    )
)

:: 3. Run Converter
"%PYTHON_CMD%" "%~dp0converter.py" %*

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [!] Process completed or cancelled.
)

echo.
pause

