@echo off
echo ========================================================
echo   SONORA - Automated GitHub Push for Cloud Build
echo ========================================================
echo.
echo Target Repository: https://github.com/DiptarkaSamanta/SONORA.git
echo.
echo 1. Adding all files and committing...
git add .
git commit -m "Sonora release - Spotify Importer, Firebase/GDrive Cloud Database & Ultra-Compact Storage"

echo.
echo 2. Setting remote origin...
git remote remove origin 2>nul
git remote add origin https://github.com/DiptarkaSamanta/SONORA.git
git branch -M main

echo.
echo 3. Pushing to GitHub...
git push -u origin main --force

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================================
    echo   SUCCESS! Pushed to https://github.com/DiptarkaSamanta/SONORA
    echo ========================================================
    echo GitHub Actions is now building your APK in the cloud!
    echo.
    echo In 2-3 minutes:
    echo 1. Open: https://github.com/DiptarkaSamanta/SONORA/releases
    echo 2. Download: Sonora-v1.0.0-Redmi12-5G.apk directly onto your phone!
    echo.
) else (
    echo.
    echo [!] If push failed due to authentication, make sure you are signed into GitHub
    echo     or use GitHub CLI / Personal Access Token.
    echo.
)
pause
