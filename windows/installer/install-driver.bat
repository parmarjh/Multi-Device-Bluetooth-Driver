@echo off
REM Multi-Device Bluetooth Driver Installer for Windows
REM Run as Administrator

echo ========================================
echo Multi-Device Bluetooth Driver Installer
echo Version 1.0.0
echo ========================================
echo.

REM Check for administrator privileges
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: This script requires Administrator privileges
    echo Please run as Administrator
    pause
    exit /b 1
)

echo [1/6] Checking system requirements...
echo.

REM Check Windows version
for /f "tokens=4-5 delims=. " %%i in ('ver') do set VERSION=%%i.%%j
if "%VERSION%" LSS "10.0" (
    echo ERROR: Windows 10 or later is required
    pause
    exit /b 1
)
echo Windows version: %VERSION% [OK]

REM Check for WDK
if not exist "C:\Program Files (x86)\Windows Kits\10\bin" (
    echo WARNING: Windows Driver Kit not found
    echo Please install WDK from: https://docs.microsoft.com/en-us/windows-hardware/drivers/download-the-wdk
    pause
)

echo.
echo [2/6] Enabling test signing mode...
echo.

bcdedit /set testsigning on
if %errorlevel% neq 0 (
    echo ERROR: Failed to enable test signing
    echo Please run: bcdedit /set testsigning on
    pause
    exit /b 1
)
echo Test signing enabled [OK]
echo NOTE: System restart will be required

echo.
echo [3/6] Installing driver certificate...
echo.

REM Import test certificate
certutil -addstore Root "..\driver\testcert.cer" >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: Failed to import certificate
    echo Driver may not load without proper certificate
) else (
    echo Certificate installed [OK]
)

echo.
echo [4/6] Installing Bluetooth driver...
echo.

REM Copy driver files
set DRIVER_PATH=%SystemRoot%\System32\drivers\MultiDeviceBTDriver.sys
copy /Y "..\driver\x64\Release\MultiDeviceBTDriver.sys" "%DRIVER_PATH%"
if %errorlevel% neq 0 (
    echo ERROR: Failed to copy driver file
    pause
    exit /b 1
)

REM Install driver using pnputil
pnputil /add-driver "..\driver\MultiDeviceBTDriver.inf" /install
if %errorlevel% neq 0 (
    echo ERROR: Driver installation failed
    echo Check Event Viewer for details
    pause
    exit /b 1
)
echo Driver installed [OK]

echo.
echo [5/6] Installing Windows Service...
echo.

REM Build service
cd ..\service
dotnet build -c Release >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Failed to build service
    echo Make sure .NET 8.0 SDK is installed
    pause
    exit /b 1
)

REM Install service
set SERVICE_PATH=%~dp0..\service\bin\Release\net8.0\MultiDeviceBTService.exe
sc create MultiDeviceBTService binPath= "%SERVICE_PATH%" start= auto DisplayName= "Multi-Device Bluetooth Manager"
if %errorlevel% neq 0 (
    echo WARNING: Service installation failed
    echo Service may already exist
    sc delete MultiDeviceBTService
    sc create MultiDeviceBTService binPath= "%SERVICE_PATH%" start= auto DisplayName= "Multi-Device Bluetooth Manager"
)

sc description MultiDeviceBTService "AI-Powered Multi-Device Bluetooth Connection Manager with IoT Support"
echo Service installed [OK]

echo.
echo [6/6] Starting services...
echo.

REM Start Bluetooth Support Service
sc start bthserv >nul 2>&1

REM Start our service
sc start MultiDeviceBTService
if %errorlevel% neq 0 (
    echo WARNING: Service failed to start
    echo Check Event Viewer for details
    echo You can start it manually after restart
) else (
    echo Service started [OK]
)

echo.
echo ========================================
echo Installation Complete!
echo ========================================
echo.
echo IMPORTANT: Your computer needs to restart for changes to take effect.
echo.
echo After restart:
echo 1. Driver will be loaded automatically
echo 2. Service will start automatically
echo 3. Access management API at: https://localhost:5001
echo.
echo Configuration files location:
echo - Driver: %SystemRoot%\System32\drivers\
echo - Service: C:\Program Files\MultiDeviceBT\
echo - Logs: %ProgramData%\MultiDeviceBT\logs\
echo.
echo Uninstall:
echo - Run: uninstall-driver.bat
echo.

choice /C YN /M "Do you want to restart now"
if %errorlevel% equ 1 (
    echo Restarting in 10 seconds...
    shutdown /r /t 10 /c "Multi-Device Bluetooth Driver installation complete. Restarting..."
) else (
    echo Please restart your computer manually to complete installation.
)

pause
