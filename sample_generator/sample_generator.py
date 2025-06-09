import json
import random
import os

output_dir = "vm_updates"
os.makedirs(output_dir, exist_ok=True)

# Lehetséges usage szintek
usage_levels = [0.1, 0.3, 0.5, 0.7, 0.9]
total_files = 1440
min_step = 30
max_step = 80


# Lépcsőzetes usage_base generálása: külön sorozat mindkét VM-hez
def generate_usage_sequence():
    sequence = []
    while len(sequence) < total_files:
        level = random.choice(usage_levels)
        length = random.randint(min_step, max_step)
        sequence.extend([level] * length)
    return sequence[:total_files]


usage_pattern_vm0 = generate_usage_sequence()
usage_pattern_vm1 = generate_usage_sequence()


# VM generáló függvény
def generate_vm(name, cpu, core_power, startup, price, vm_type, traffic, usage_base):
    return {
        "name": name,
        "cpu": cpu,
        "ram": 4294967296,
        "coreProcessingPower": core_power,
        "startupProcess": startup,
        "reqDisk": 1073741824,
        "pricePerTick": price,
        "status": "RUNNING",
        "type": vm_type,
        "networkTraffic": traffic,
        "usage": round(min(max(random.gauss(usage_base, 0.03), 0.0), 1.0), 2),
        "dataSinceLastSave": random.randint(400, 900)
    }


# Teljes JSON generálása
def generate_update(usage_base_vm0, usage_base_vm1):
    return {
        "requestType": "UPDATE",
        "vmsCount": 2,
        "vmData": [
            generate_vm("VM0", 4, 0.01, 100, 0.204, "BACKEND", 400, usage_base_vm0),
            generate_vm("VM1", 2, 0.005, 400, 0.102, "FRONTEND", 250, usage_base_vm1)
        ]
    }


# JSON fájlok létrehozása
for i in range(total_files):
    update = generate_update(usage_pattern_vm0[i], usage_pattern_vm1[i])
    filename = os.path.join(output_dir, f"update_{i + 1:03}.json")
    with open(filename, "w") as f:
        json.dump(update, f, indent=2)
