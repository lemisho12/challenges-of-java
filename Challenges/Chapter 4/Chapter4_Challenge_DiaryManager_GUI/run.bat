@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Personal Diary Manager
echo ========================================

:: Check if Java is installed
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH.
    echo Please install Java 11 or higher from:
    echo https://adoptium.net/
    pause
    exit /b 1
)

:: Check Java version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set version=%%g
)
set version=%version:"=%
for /f "delims=.-_ tokens=1-3" %%a in ("%version%") do (
    set major=%%a
    set minor=%%b
    set patch=%%c
)

:: Remove non-numeric characters from major version
set "major=%major:"=%"
for /f "tokens=1 delims=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" %%i in ("%major%") do set major=%%i

if %major% lss 11 (
    echo Error: Java version %major% is too old.
    echo Please install Java 11 or higher.
    pause
    exit /b 1
)

:: Set JavaFX path
set JAVAFX_PATH=lib\javafx-sdk-17.0.6\lib
if not exist "%JAVAFX_PATH%" (
    echo Error: JavaFX SDK not found at %JAVAFX_PATH%
    echo Please download JavaFX SDK 17 from:
    echo https://gluonhq.com/products/javafx/
    echo and extract it to the 'lib' folder.
    pause
    exit /b 1
)

:: Create data directory if it doesn't exist
if not exist "data" (
    mkdir data
    mkdir data\entries
    echo Created data directories.
)

:: Run the application
echo Starting Personal Diary Manager...
echo Java version: %version%
echo.

java --module-path "%JAVAFX_PATH%" ^
     --add-modules javafx.controls,javafx.fxml,javafx.web ^
     -cp "build/classes/java/main;build/resources/main;lib/*" ^
     com.diary.manager.Main

if %errorlevel% neq 0 (
    echo.
    echo Application exited with error code %errorlevel%
    pause
)

endlocal