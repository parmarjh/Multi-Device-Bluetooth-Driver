#!/bin/bash

# Multi-Device Bluetooth Manager Android Installer Script
# Requires adb to be in your PATH

echo "===================================================="
echo "   Multi-Device Bluetooth Manager Installer (Android)   "
echo "===================================================="

# Check if adb is installed
if ! command -v adb &> /dev/null
then
    echo "Error: adb could not be found. Please install Android Platform Tools."
    exit 1
fi

# Check for connected devices
DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device" | wc -l)

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "Error: No Android device detected via ADB."
    echo "Please connect your device and enable USB Debugging."
    exit 1
fi

echo "Found $DEVICE_COUNT device(s)."

# Build path (defaulting to the build output directory)
APK_PATH="../app/build/outputs/apk/debug/app-debug.apk"

if [ ! -f "$APK_PATH" ]; then
    echo "Error: APK not found at $APK_PATH"
    echo "Please run './gradlew assembleDebug' first."
    exit 1
fi

echo "Installing APK: $APK_PATH ..."
adb install -r "$APK_PATH"

if [ $? -eq 0 ]; then
    echo "✅ App installed successfully!"
else
    echo "❌ Installation failed."
    exit 1
fi

echo "Granting Bluetooth and Location permissions..."
adb shell pm grant com.multidevicebt android.permission.BLUETOOTH_CONNECT
adb shell pm grant com.multidevicebt android.permission.BLUETOOTH_SCAN
adb shell pm grant com.multidevicebt android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.multidevicebt android.permission.ACCESS_BACKGROUND_LOCATION

echo "Starting the application..."
adb shell am start -n com.multidevicebt/com.multidevicebt.ui.MainActivity

echo "===================================================="
echo "          Installation Complete! 🎉                 "
echo "===================================================="
