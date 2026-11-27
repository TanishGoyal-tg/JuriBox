@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

title Court Case Scheduling Simulator (JuriBox)
chcp 65001 >nul
cd /d "%~dp0"

echo ============================================================
echo       Court Case Scheduling Simulator (JuriBox)
echo ============================================================
echo.

where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java is not installed or not found in system PATH.
    echo Please install Java JDK 8 or higher from https://adoptium.net/
    echo.
    pause
    exit /b 1
)

if not exist bin (
    mkdir bin
)

echo [1/2] Compiling Java source files...
javac -encoding UTF-8 -cp "lib/*" -d bin src\juribox\exception\*.java src\juribox\model\*.java src\juribox\service\*.java src\juribox\app\*.java
set COMPILE_STATUS=%ERRORLEVEL%

if %COMPILE_STATUS% neq 0 (
    echo [ERROR] Compilation failed. Please inspect the error messages above.
    pause
    exit /b %COMPILE_STATUS%
)

echo [2/2] Launching JuriBox application...
echo.
java -cp "bin;lib/*" juribox.app.Main

echo.
echo ============================================================
echo Application session finished.
echo ============================================================
pause
