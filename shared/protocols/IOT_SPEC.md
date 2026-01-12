# Bluetooth LE IoT Protocol Specification

This document defines the custom GATT profiles and data structures used for communication between the Multi-Device Bluetooth Driver and various smart home appliances.

---

## 1. Service UUIDs

All Antigravity AI-Enhanced IoT devices use the following primary service:

| Service | UUID | Description |
|---------|------|-------------|
| **Core IoT Service** | `0000FF00-0000-1000-8000-00805F9B34FB` | Main control and telemetry service |

---

## 2. Characteristics

Inside the Core IoT Service, the following characteristics are defined:

| Name | UUID | Type | Properties | Description |
|------|------|------|------------|-------------|
| **Command** | `0000FF01-...` | Write | Write With Response | Send control commands to the device. |
| **Status** | `0000FF02-...` | Read | Read & Notify | Current power status and error codes. |
| **Sensor Data**| `0000FF03-...` | Read | Read & Notify | Real-time telemetry (temp, humidity, etc.) |
| **Config** | `0000FF04-...` | RW | Read & Write | Device identity and Wi-Fi/BT settings. |

---

## 3. Data Payloads (Binary Format)

To maximize efficiency over Bluetooth LE, all data is sent in compact binary format.

### 3.1 Command Packet (Write)
Size: 4 - 20 Bytes

| Byte | Field | Description |
|------|-------|-------------|
| 0 | Command ID | See Command IDs table below |
| 1 | Target Component | 0=System, 1=Compressor, 2=Fan, 3=Display |
| 2-3 | Parameter 1 | 16-bit integer (Big Endian) |
| 4-5 | Parameter 2 | 16-bit integer (Big Endian) |
| 6+ | Extra Data | Optional |

### 3.2 Sensor Data Packet (Read/Notify)
Size: 12 Bytes

| Byte | Field | Description |
|------|-------|-------------|
| 0 | Device Type | 1=AC, 2=Fridge, 3=TV, etc. |
| 1-2 | Temperature | Int16, scaled by 10 (e.g., 245 = 24.5°C) |
| 3-4 | Humidity | Uint16, scaled by 10 (e.g., 450 = 45.0%) |
| 5-6 | Power Consum. | Uint16, Watts |
| 7 | Fan Speed | 0-100% |
| 8 | Mode | Current operational mode (0=Idle, 1=Cooling, etc.) |
| 9-11 | Reserved | For future use |

---

## 4. Device Specific Command IDs

### 4.1 Air Conditioner (Type 0x01)
- `0x01`: Turn On/Off (Param: 0=Off, 1=On)
- `0x02`: Set Temp (Param: Target temp * 10)
- `0x03`: Set Mode (0=Fan, 1=Cool, 2=Heat, 3=Dry)
- `0x04`: Set Fan Speed (0-100)

### 4.2 Refrigerator (Type 0x02)
- `0x10`: Set Fridge Temp
- `0x11`: Set Freezer Temp
- `0x12`: Turbo Cool (Param: 0=Off, 1=On)
- `0x13`: Holiday Mode

### 4.3 Android TV / Smart TV (Type 0x03)
- `0x20`: Key Code (Param: Standard HID key code)
- `0x21`: Launch App (Data: App Identifier string)
- `0x22`: Volume Adjust (Param: -/+ relative or absolute)

---

## 5. AI Integration Metadata

The driver uses the **Sensor Data** to feed the AI Optimization engine.

1. **Power Correlation**: AI tracks power consumption spikes to predict when a device is under heavy load (e.g., Fridge compressor starting).
2. **Usage Prediction**: AI learns that AC is usually turned on at 6:00 PM and prepares the connection beforehand.
3. **QoS Priority**: Devices with safety sensors (e.g., Fridge temp warning) are automatically given `PRIORITY_CRITICAL` status.
