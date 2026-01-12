import time
import random
import sys
from datetime import datetime

# --- CONFIGURATION ---
NUM_CYCLES = 20
DELAY = 0.5 # Seconds between updates

# --- MOCK DATA ---
DEVICES = [
    {"name": "Living Room AC", "type": "Air Conditioner", "addr": "B4:3A:92:7C:F5:12", "priority": "MEDIUM", "load": 0},
    {"name": "Kitchen Fridge", "type": "Refrigerator", "addr": "C1:44:8E:20:BC:99", "priority": "MEDIUM", "load": 0},
    {"name": "Sony XM4", "type": "Headphones", "addr": "A8:11:7F:32:01:45", "priority": "HIGH", "load": 0},
    {"name": "Samsung S23", "type": "Mobile Phone", "addr": "D9:92:1C:88:A3:21", "priority": "MEDIUM", "load": 0},
    {"name": "Sony Bravia TV", "type": "Android TV", "addr": "E2:33:00:11:CC:DD", "priority": "LOW", "load": 0}
]

def clear_screen():
    # Attempt to clear the terminal screen
    print("\033[H\033[J", end="")

def print_header():
    print("=" * 100)
    print(f" ANTIGRAVITY AI - MULTI-DEVICE BLUETOOTH MANAGEMENT CONSOLE [VERSION 1.0.0]")
    print(f" Status: ACTIVE | Platform: WINDOWS/SIM | AI Engine: TFLINK-V1")
    print("=" * 100)
    print(f"{'DEVICE':<20} | {'ADDRESS':<18} | {'TYPE':<15} | {'PRIORITY':<10} | {'SIGNAL':<10} | {'TRAFFIC'}")
    print("-" * 100)

def simulate():
    logs = []
    
    for cycle in range(NUM_CYCLES):
        clear_screen()
        print_header()
        
        for dev in DEVICES:
            # Simulate real-time signal flux
            signal = random.randint(-95, -30)
            status_color = "\033[92m" # Green
            if signal < -80: status_color = "\033[91m" # Red
            elif signal < -70: status_color = "\033[33m" # Yellow
            
            # Simulate traffic (KB/s)
            traffic = random.uniform(0.1, 15.5) if dev['priority'] != "LOW" else random.uniform(0.1, 1.2)
            
            # AI Engine Logic
            if signal < -85:
                action = f"AI ACTION: Signal boost for {dev['name']} ({signal}dBm)"
                if action not in logs: logs.append(action)
            
            if dev['type'] == "Headphones" and traffic > 5.0 and dev['priority'] != "CRITICAL":
                dev['priority'] = "CRITICAL"
                action = f"AI ACTION: Elevating Sony XM4 to CRITICAL (High Fidelity Audio stream detected)"
                if action not in logs: logs.append(action)

            # Print device row
            print(f"{dev['name']:<20} | {dev['addr']:<18} | {dev['type']:<15} | {dev['priority']:<10} | {status_color}{signal:>3} dBm\033[0m | {traffic:>5.1f} KB/s")

        print("-" * 100)
        print("\033[1mRECENT AI ENGINE EVENT LOGS:\033[0m")
        # Show last 5 logs
        for log in logs[-5:]:
            print(f" > {log}")
        
        print("\nCycle " + str(cycle + 1) + "/" + str(NUM_CYCLES) + " | System Time: " + datetime.now().strftime('%H:%M:%S'))
        time.sleep(DELAY)

if __name__ == "__main__":
    try:
        simulate()
        print("\nSimulation complete. Press Ctrl+C to exit dashboard view.")
    except KeyboardInterrupt:
        print("\nExiting dashboard...")
