# Multi-Device Bluetooth Driver - Quick Reference

## Quick Commands

### Windows

#### Check Status
```powershell
# Driver status
pnputil /enum-drivers | Select-String "MultiDeviceBT"

# Service status
Get-Service MultiDeviceBTService

# View logs
Get-EventLog -LogName Application -Source MultiDeviceBTService -Newest 20
```

#### Control Service
```powershell
# Start
Start-Service MultiDeviceBTService

# Stop
Stop-Service MultiDeviceBTService

# Restart
Restart-Service MultiDeviceBTService
```

#### API Commands
```powershell
# Get connected devices (requires API running)
Invoke-RestMethod -Uri "https://localhost:5001/api/devices" -Method GET

# Get statistics
Invoke-RestMethod -Uri "https://localhost:5001/api/statistics" -Method GET

# Get AI insights
Invoke-RestMethod -Uri "https://localhost:5001/api/ai/insights" -Method GET
```

### Android

#### ADB Commands
```bash
# Install app
adb install -r app-debug.apk

# Grant permissions
adb shell pm grant com.multidevicebt android.permission.BLUETOOTH_CONNECT
adb shell pm grant com.multidevicebt android.permission.BLUETOOTH_SCAN
adb shell pm grant com.multidevicebt android.permission.ACCESS_FINE_LOCATION

# View logs
adb logcat | grep "MultiDeviceBT"

# Clear app data
adb shell pm clear com.multidevicebt
```

## Connection Priorities

| Priority | Value | Use Case | Examples |
|----------|-------|----------|----------|
| CRITICAL | 0 | Real-time audio/data | Headphones, game controllers |
| HIGH | 1 | Input devices | Keyboard, mouse, wearables |
| MEDIUM | 2 | IoT devices | Smart home devices |
| LOW | 3 | Background sync | File transfers, backups |

## IoT Device Commands

### Air Conditioner
```json
{
  "command": "SET_TEMPERATURE",
  "parameters": {
    "temperature": 24,
    "unit": "celsius",
    "mode": "cool"
  }
}
```

### Refrigerator
```json
{
  "command": "GET_STATUS",
  "parameters": {}
}
```

### Smart TV
```json
{
  "command": "SET_VOLUME",
  "parameters": {
    "level": 50
  }
}
```

## Supported Device Types

| Code | Device Type | Protocol | Max Range |
|------|-------------|----------|-----------|
| 0x01 | Air Conditioner | BLE | 10m |
| 0x02 | Refrigerator | BLE | 10m |
| 0x03 | Smart TV | Classic | 30m |
| 0x04 | Headphones | Classic/BLE | 10-30m |
| 0x05 | Mobile Phone | Classic | 30m |
| 0x06 | Android TV | Classic | 30m |
| 0xFF | Generic IoT | BLE | 10m |

## AI Optimization Features

### Bandwidth Allocation
- Automatically adjusts bandwidth based on device priority
- High-priority devices get 60-80% allocation
- IoT devices get 20-40% allocation

### Power Management
- Aggressive mode: >60% power saving for idle devices
- Moderate mode: 30-60% for IoT devices
- Low mode: <20% for critical devices

### Latency Reduction
- Ultra-low latency: <50ms for priority 0
- Low latency: <100ms for priority 1
- Standard: <200ms for priority 2+

### Anomaly Detection
- Unusual data rate patterns
- Signal strength degradation
- Unexpected disconnections
- Protocol violations

## Troubleshooting Checklist

### Windows

- [ ] Test signing enabled: `bcdedit /enum | Select-String "testsigning"`
- [ ] Driver loaded: `pnputil /enum-drivers`
- [ ] Service running: `Get-Service MultiDeviceBTService`
- [ ] Bluetooth enabled: `Get-Service bthserv`
- [ ] No errors in Event Log: `Get-EventLog -LogName Application -Source MultiDeviceBTService -EntryType Error -Newest 10`

### Android

- [ ] Bluetooth enabled: `adb shell settings get global bluetooth_on`
- [ ] Location enabled: `adb shell settings get secure location_mode`
- [ ] Permissions granted: Check Settings → Apps → Multi-Device BT → Permissions
- [ ] Service running: `adb shell dumpsys activity services | grep MultiDeviceBT`
- [ ] No crashes: `adb logcat | grep AndroidRuntime`

## Performance Tuning

### Windows Registry Settings
```
HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\MultiDeviceBTService

MaxConnections: DWORD (default: 7)
OptimizationInterval: DWORD (default: 30000ms)
EnableAI: DWORD (default: 1)
LogLevel: DWORD (0=Error, 1=Warning, 2=Info, 3=Debug)
```

### Android SharedPreferences
```xml
<!-- /data/data/com.multidevicebt/shared_prefs/settings.xml -->
<map>
    <int name="max_connections" value="7" />
    <int name="optimization_interval" value="30000" />
    <boolean name="enable_ai" value="true" />
    <boolean name="background_scanning" value="true" />
    <boolean name="auto_reconnect" value="true" />
</map>
```

## Signal Strength Reference

| RSSI (dBm) | Quality | Range | Action |
|------------|---------|-------|--------|
| -30 to -50 | Excellent | <1m | None |
| -51 to -60 | Good | 1-5m | None |
| -61 to -70 | Fair | 5-10m | Monitor |
| -71 to -80 | Weak | 10-20m | Optimize |
| -81 to -90 | Very Weak | 20-30m | Boost/Reconnect |
| < -90 | Poor | >30m | Disconnect |

## Common Error Codes

| Code | Description | Solution |
|------|-------------|----------|
| 0x0001 | Driver not loaded | Check test signing, reinstall driver |
| 0x0002 | Service start failed | Check Event Log, verify .NET installation |
| 0x0003 | AI model not found | Copy model to correct directory |
| 0x0004 | Max connections reached | Disconnect low-priority device |
| 0x0005 | Permission denied | Grant Bluetooth permissions |
| 0x0006 | Device not found | Ensure device is in range and paired |
| 0x0007 | Connection timeout | Check signal strength, retry |
| 0x0008 | IoT protocol error | Verify device compatibility |

## Resource Usage Guidelines

| Component | CPU (Idle) | CPU (Active) | Memory | Disk I/O |
|-----------|------------|--------------|--------|----------|
| Windows Driver | <1% | 2-5% | 10MB | Minimal |
| Windows Service | <2% | 5-10% | 50MB | Low |
| Android App | <1% | 3-8% | 80MB | Low |
| AI Engine | 0% | 10-20% | 30MB | None |

## Support Resources

- **Documentation**: `/docs/`
- **Examples**: `/examples/`
- **API Reference**: `/docs/API.md`
- **FAQ**: `/docs/FAQ.md`
- **GitHub Issues**: `https://github.com/yourusername/BluetoothMultiDriver/issues`

---

**Pro Tip**: Use the AI optimization dashboard to monitor connection health and receive real-time recommendations for improving performance!
