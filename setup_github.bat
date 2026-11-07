@echo off
echo ========================================================
echo   SONORA - Quick GitHub Setup for Automatic Cloud Build
echo ========================================================
echo.
echo This script will initialize Git and help you push Sonora
echo to GitHub so GitHub can build the APK for you in 2 minutes!
echo.
set /p REPO_URL="Enter your GitHub Repository URL (e.g., https://github.com/username/sonora.git): "

if "%REPO_URL%"=="" (
    echo Error: No URL entered. Please create a repository on https://github.com/new and try again.
    pause
    exit /b
)

echo.
echo Initializing Git repository...
git init
git add .
git commit -m "Sonora initial release for Redmi 12 5G"
git branch -M main
git remote add origin %REPO_URL%
git push -u origin main --force

echo.
echo ========================================================
echo   SUCCESS! Pushed to GitHub!
echo ========================================================
echo Now open your repository on your browser:
echo 1. Go to the "Actions" tab or "Releases" tab.
echo 2. Wait 2 minutes for GitHub Cloud to finish building.
echo 3. Click and download "Sonora-v1.0.0-Redmi12-5G.apk" directly to your phone!
echo.
pause

