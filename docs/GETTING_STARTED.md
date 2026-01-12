# Getting Started Guide

## Multi-Device Bluetooth Driver with AI Integration

This comprehensive guide will help you set up and deploy the Bluetooth Multi-Device Driver system on both Windows and Android platforms.

---

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Windows Installation](#windows-installation)
3. [Android Installation](#android-installation)
4. [IoT Device Setup](#iot-device-setup)
5. [AI Model Configuration](#ai-model-configuration)
6. [Testing and Verification](#testing-and-verification)
7. [Troubleshooting](#troubleshooting)

---

## System Requirements

### Windows

- **Operating System**: Windows 10 (64-bit) version 1809 or later, or Windows 11
- **Development Tools**:
  - Visual Studio 2022 (Community, Professional, or Enterprise)
  - Windows Driver Kit (WDK) 10
  - .NET 8.0 SDK or later
- **Hardware**:
  - Bluetooth 4.0+ adapter (Bluetooth 5.0+ recommended)
  - 4GB RAM minimum (8GB recommended)
  - 500MB free disk space

### Android

- **Operating System**: Android 8.0 (API 26) or higher
- **Development Tools**:
  - Android Studio Electric Eel (2022.1.1) or later
  - Android SDK 34
  - Android NDK r25+
  - Kotlin 1.9.0+
- **Hardware**:
  - Bluetooth 4.0+ (Bluetooth 5.0+ recommended)
  - 2GB RAM minimum
  - 100MB free storage

---

## Windows Installation

### Step 1: Install Prerequisites

```powershell
# Install Windows Driver Kit
# Download from: https://docs.microsoft.com/en-us/windows-hardware/drivers/download-the-wdk

# Install .NET 8.0 SDK
winget install Microsoft.DotNet.SDK.8

# Install Visual Studio 2022 with C++ and Windows Driver Development components
```

### Step 2: Build the Driver

```powershell
# Navigate to the Windows directory
cd BluetoothMultiDriver/windows

# Build the driver
cd driver
msbuild MultiDeviceBTDriver.vcxproj /p:Configuration=Release /p:Platform=x64

# Verify the build
dir x64\Release\MultiDeviceBTDriver.sys
```

### Step 3: Sign the Driver (Required for Windows 11)

```powershell
# Create a test certificate (for development only)
makecert -r -pe -ss PrivateCertStore -n "CN=MultiDeviceBT Test Certificate" testcert.cer

# Sign the driver
signtool sign /v /s PrivateCertStore /n "MultiDeviceBT Test Certificate" /t http://timestamp.digicert.com MultiDeviceBTDriver.sys

# For production, use a valid code signing certificate from a trusted CA
```

### Step 4: Install the Driver

```powershell
# Enable test signing (development only)
bcdedit /set testsigning on

# Restart the computer
shutdown /r /t 0

# After restart, install the driver
cd installer
.\install-driver.bat

# Verify installation
pnputil /enum-drivers
```

### Step 5: Install and Start the Windows Service

```powershell
# Build the service
cd ..\service
dotnet build -c Release

# Install the service
sc create MultiDeviceBTService binPath= "C:\Path\To\MultiDeviceBTService.exe" start= auto
sc description MultiDeviceBTService "AI-Powered Multi-Device Bluetooth Manager"

# Start the service
sc start MultiDeviceBTService

# Verify service status
sc query MultiDeviceBTService
```

### Step 6: Install the Management API (Optional)

```powershell
# Build the API
cd ..\api
dotnet build -c Release

# Run the API
dotnet run --launch-profile Production

# The API will be available at https://localhost:5001
```

---

## Android Installation

### Step 1: Set Up Development Environment

```bash
# Install Android Studio
# Download from: https://developer.android.com/studio

# Install required SDK components
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
sdkmanager "ndk;25.2.9519653"
```

### Step 2: Build the Application

```bash
# Navigate to the Android directory
cd BluetoothMultiDriver/android

# Build the app
./gradlew assembleDebug

# For release build
./gradlew assembleRelease

# The APK will be in app/build/outputs/apk/
```

### Step 3: Install on Device

```bash
# Connect your Android device via USB
# Enable USB debugging in Developer Options

# Install the app
adb install app/build/outputs/apk/debug/app-debug.apk

# Grant Bluetooth permissions
adb shell pm grant com.multidevicebt android.permission.BLUETOOTH_CONNECT
adb shell pm grant com.multidevicebt android.permission.BLUETOOTH_SCAN
adb shell pm grant com.multidevicebt android.permission.ACCESS_FINE_LOCATION
```

### Step 4: Configure System Service (Requires Root)

```bash
# For rooted devices only - enables system-level integration

# Push service binary
adb root
adb remount
adb push service/multidevicebt-service /system/bin/
adb shell chmod 755 /system/bin/multidevicebt-service

# Create service configuration
adb push service/multidevicebt.rc /system/etc/init/
adb shell chmod 644 /system/etc/init/multidevicebt.rc

# Reboot
adb reboot
```

---

## IoT Device Setup

### Supported IoT Devices

1. **Smart Air Conditioners** (Bluetooth LE)
2. **Smart Refrigerators** (Bluetooth LE)
3. **Android TV / Smart TV** (Bluetooth Classic)
4. **Bluetooth Headphones** (Bluetooth Classic/LE)
5. **Smart Speakers** (Bluetooth Classic)
6. **Generic IoT Devices** (Bluetooth LE with custom GATT profiles)

### Pairing IoT Devices

#### Windows

```powershell
# Open PowerShell as Administrator
# Use the built-in Bluetooth settings or API

# Via API
Invoke-RestMethod -Uri "https://localhost:5001/api/devices/scan" -Method POST
Invoke-RestMethod -Uri "https://localhost:5001/api/devices/pair" -Method POST -Body (@{
    address = "XX:XX:XX:XX:XX:XX"
    deviceType = "IOT_AIR_CONDITIONER"
    pin = "0000"
} | ConvertTo-Json) -ContentType "application/json"
```

#### Android

1. Open the Multi-Device BT Manager app
2. Navigate to "IoT Control" tab
3. Tap "+" to scan for devices
4. Select device and tap "Pair"
5. Configure device-specific settings

### IoT Device Communication Protocol

The system uses custom GATT characteristics for IoT devices:

```
Service UUID: 0000ff00-0000-1000-8000-00805f9b34fb

Characteristics:
- Command (Write): 0000ff01-0000-1000-8000-00805f9b34fb
- Status (Read/Notify): 0000ff02-0000-1000-8000-00805f9b34fb
- Sensor Data (Read/Notify): 0000ff03-0000-1000-8000-00805f9b34fb
- Configuration (Read/Write): 0000ff04-0000-1000-8000-00805f9b34fb
```

---

## AI Model Configuration

### TensorFlow Lite Model

The AI optimization engine uses a custom TensorFlow Lite model trained on Bluetooth connection patterns.

#### Model Architecture

```
Input Layer: 10 features (priority, device type, signal strength, data rate, etc.)
Hidden Layers: 2 layers (32 and 16 neurons) with ReLU activation
Output Layer: 5 optimization recommendations
```

#### Training Your Own Model (Optional)

```python
# See shared/ai-models/training/train_model.py

python train_model.py --data connection_logs.csv --epochs 100 --output bluetooth_optimizer.tflite
```

#### Deploying the Model

**Windows:**
```powershell
# Copy model to service directory
copy shared\ai-models\bluetooth_optimizer.tflite windows\service\models\
```

**Android:**
```bash
# Copy model to assets
cp shared/ai-models/bluetooth_optimizer.tflite android/app/src/main/assets/
```

---

## Testing and Verification

### Windows Testing

```powershell
# Check driver status
pnputil /enum-drivers | Select-String "MultiDeviceBT"

# Check service status
Get-Service MultiDeviceBTService

# View service logs
Get-EventLog -LogName Application -Source MultiDeviceBTService -Newest 50

# Test API endpoints
curl https://localhost:5001/api/devices/status
curl https://localhost:5001/api/ai/statistics
```

### Android Testing

```bash
# View app logs
adb logcat | grep MultiDeviceBT

# Check connected devices
adb shell dumpsys bluetooth_manager

# Monitor AI optimizations
adb logcat | grep AIOptimizationEngine

# Test IoT control
adb shell am broadcast -a com.multidevicebt.TEST_IOT_COMMAND \
  --es device "XX:XX:XX:XX:XX:XX" \
  --es command "TURN_ON"
```

### Performance Benchmarks

Expected performance metrics:

| Metric | Windows | Android |
|--------|---------|---------|
| Max Simultaneous Connections | 7 | 7 |
| Connection Latency | < 500ms | < 800ms |
| AI Optimization Cycle | 30s | 30s |
| Memory Usage | ~50MB | ~80MB |
| CPU Usage (idle) | < 2% | < 3% |
| Battery Impact (Android) | - | ~5% per day |

---

## Troubleshooting

### Windows Issues

#### Driver Installation Fails

```powershell
# Check if test signing is enabled
bcdedit /enum | Select-String "testsigning"

# Verify certificate installation
certutil -viewstore PrivateCertStore

# Check Windows Event Log
Get-EventLog -LogName System -Source "Windows Kernel" -Newest 100 | Where-Object {$_.Message -like "*Bluetooth*"}
```

#### Service Won't Start

```powershell
# Check service dependencies
sc qc MultiDeviceBTService

# View detailed error
Get-EventLog -LogName Application -Source MultiDeviceBTService -EntryType Error -Newest 10

# Restart Bluetooth support service
Restart-Service bthserv
```

### Android Issues

#### Permissions Denied

```bash
# Manually grant all permissions
adb shell pm grant com.multidevicebt android.permission.BLUETOOTH_CONNECT
adb shell pm grant com.multidevicebt android.permission.BLUETOOTH_SCAN
adb shell pm grant com.multidevicebt android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.multidevicebt android.permission.ACCESS_BACKGROUND_LOCATION
```

#### Connection Failures

```bash
# Reset Bluetooth stack
adb shell svc bluetooth disable
sleep 2
adb shell svc bluetooth enable

# Clear app data
adb shell pm clear com.multidevicebt

# Check Bluetooth hardware
adb shell getprop bluetooth.device.class
```

#### AI Model Not Loading

```bash
# Verify model file exists
adb shell ls -la /data/data/com.multidevicebt/assets/

# Check model file size
adb shell du -h /data/data/com.multidevicebt/assets/bluetooth_optimizer.tflite

# View TensorFlow Lite logs
adb logcat | grep -i tensorflow
```

### IoT Device Connection Issues

1. **Device not discovered**:
   - Ensure device is in pairing mode
   - Check Bluetooth is enabled
   - Verify device is in range (< 10 meters)
   - Restart Bluetooth adapter

2. **Connection drops frequently**:
   - Check signal strength
   - Reduce distance to device
   - Remove interference sources
   - Update device firmware

3. **Commands not working**:
   - Verify device protocol compatibility
   - Check GATT characteristic UUIDs
   - Review device documentation
   - Enable debug logging

---

## Advanced Configuration

### Custom Priority Profiles

Edit `config/priority_profiles.json`:

```json
{
  "profiles": {
    "gaming": {
      "headphones": 0,
      "controller": 0,
      "keyboard": 1,
      "mouse": 1,
      "other": 3
    },
    "productivity": {
      "keyboard": 0,
      "mouse": 0,
      "headphones": 1,
      "other": 2
    },
    "home_automation": {
      "iot_critical": 0,
      "iot_sensors": 1,
      "iot_generic": 2,
      "other": 3
    }
  }
}
```

### AI Model Tuning

Adjust AI optimization parameters in `config/ai_config.json`:

```json
{
  "optimization": {
    "interval_seconds": 30,
    "learning_rate": 0.001,
    "enable_predictive_connect": true,
    "enable_bandwidth_optimization": true,
    "enable_power_saving": true,
    "enable_latency_reduction": true
  },
  "thresholds": {
    "weak_signal_rssi": -80,
    "critical_signal_rssi": -90,
    "low_data_rate_bps": 1000,
    "high_data_rate_bps": 1000000
  }
}
```

---

## Support and Documentation

- **GitHub**: https://github.com/yourusername/BluetoothMultiDriver
- **Documentation**: https://docs.multidevicebt.com
- **Issues**: https://github.com/yourusername/BluetoothMultiDriver/issues
- **Discord**: https://discord.gg/multidevicebt

---

## License

MIT License - See LICENSE file for details

---

**Congratulations!** You've successfully set up the Multi-Device Bluetooth Driver system. Enjoy AI-powered Bluetooth connection management across all your devices!
