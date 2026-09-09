#!/usr/bin/env python3
"""Static packaging gate for the experimental Matter Overdrive tech overhaul."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]

BLOCKS = [
    "grid_capacitor",
    "android_induction_relay",
    "quantum_power_relay",
    "matter_storage_matrix",
    "matter_excavator",
    "holographic_status_panel",
]
ITEMS = [
    "network_diagnostic_probe",
    "matter_storage_cell_64k",
    "matter_storage_cell_256k",
    "matter_storage_cell_1m",
    "matter_storage_cell_4m",
    "upgrade_parallel_processing",
]
JAVA = [
    "src/main/java/matteroverdrive/block/TechMachineBlock.java",
    "src/main/java/matteroverdrive/blockentity/GridCapacitorBlockEntity.java",
    "src/main/java/matteroverdrive/blockentity/AndroidInductionRelayBlockEntity.java",
    "src/main/java/matteroverdrive/blockentity/QuantumPowerRelayBlockEntity.java",
    "src/main/java/matteroverdrive/blockentity/MatterStorageMatrixBlockEntity.java",
    "src/main/java/matteroverdrive/blockentity/MatterExcavatorBlockEntity.java",
    "src/main/java/matteroverdrive/blockentity/HolographicStatusPanelBlockEntity.java",
    "src/main/java/matteroverdrive/item/MatterStorageCellItem.java",
    "src/main/java/matteroverdrive/item/NetworkDiagnosticProbeItem.java",
    "src/main/java/matteroverdrive/machine/MachineSideConfigurationData.java",
    "src/main/java/matteroverdrive/network/FacilityNetworkTelemetry.java",
]
GUIDES = [
    "src/main/resources/assets/matteroverdrive/guides/matteroverdrive/guide/advanced_infrastructure.md",
    "src/main/resources/assets/matteroverdrive/guides/matteroverdrive/guide/network.md",
    "src/main/resources/assets/matteroverdrive/guides/matteroverdrive/guide/power.md",
    "src/main/resources/assets/matteroverdrive/guides/matteroverdrive/guide/matter.md",
    "src/main/resources/assets/matteroverdrive/guides/matteroverdrive/guide/android.md",
    "docs/testing/TECH_OVERHAUL_TEST_PLAN.md",
]

errors = []

def require(path: str):
    p = ROOT / path
    if not p.is_file():
        errors.append(f"missing file: {path}")
    return p

for path in JAVA + GUIDES:
    require(path)

for block in BLOCKS:
    require(f"src/main/resources/assets/matteroverdrive/blockstates/{block}.json")
    require(f"src/main/resources/assets/matteroverdrive/models/block/{block}.json")
    require(f"src/main/resources/assets/matteroverdrive/models/item/{block}.json")
    require(f"src/main/resources/data/matteroverdrive/recipes/{block}.json")
    require(f"src/main/resources/data/matteroverdrive/loot_tables/blocks/{block}.json")

for item in ITEMS:
    require(f"src/main/resources/assets/matteroverdrive/models/item/{item}.json")
    require(f"src/main/resources/data/matteroverdrive/recipes/{item}.json")

# Parse every experimental JSON resource now, before Forge gets involved.
for path in list((ROOT / "src/main/resources/assets/matteroverdrive/blockstates").glob("*.json")) + \
            list((ROOT / "src/main/resources/assets/matteroverdrive/models/block").glob("*.json")) + \
            list((ROOT / "src/main/resources/assets/matteroverdrive/models/item").glob("*.json")) + \
            list((ROOT / "src/main/resources/data/matteroverdrive/recipes").glob("*.json")) + \
            list((ROOT / "src/main/resources/data/matteroverdrive/loot_tables/blocks").glob("*.json")):
    name = path.stem
    if name not in BLOCKS and name not in ITEMS:
        continue
    try:
        json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"invalid JSON: {path.relative_to(ROOT)}: {exc}")

mod_blocks = require("src/main/java/matteroverdrive/registry/ModBlocks.java")
mod_items = require("src/main/java/matteroverdrive/registry/ModItems.java")
extra_be = require("src/main/java/matteroverdrive/registry/ModExtraBlockEntities.java")
client_events = require("src/main/java/matteroverdrive/client/ClientModEvents.java")

if mod_blocks.is_file():
    text = mod_blocks.read_text(encoding="utf-8")
    for block in BLOCKS:
        if f'"{block}"' not in text:
            errors.append(f"ModBlocks does not register {block}")
if mod_items.is_file():
    text = mod_items.read_text(encoding="utf-8")
    for item in ITEMS:
        if f'"{item}"' not in text:
            errors.append(f"ModItems does not register {item}")
if extra_be.is_file():
    text = extra_be.read_text(encoding="utf-8")
    for key in ["GRID_CAPACITOR", "ANDROID_INDUCTION_RELAY", "QUANTUM_POWER_RELAY", "MATTER_STORAGE_MATRIX", "MATTER_EXCAVATOR", "HOLOGRAPHIC_STATUS_PANEL"]:
        if key not in text:
            errors.append(f"ModExtraBlockEntities missing {key}")
if client_events.is_file():
    text = client_events.read_text(encoding="utf-8")
    if "HolographicStatusPanelRenderer" not in text:
        errors.append("Holographic status renderer is not registered client-side")

# Protect the project direction in active GuideME pages.
active_guides = [
    ROOT / "src/main/resources/assets/matteroverdrive/guides/matteroverdrive/guide/index.md",
    ROOT / "src/main/resources/assets/matteroverdrive/guides/matteroverdrive/guide/current_features.md",
]
for guide in active_guides:
    if guide.is_file() and "[Star Map]" in guide.read_text(encoding="utf-8"):
        errors.append(f"retired Star Map remains linked from active guide: {guide.relative_to(ROOT)}")

if errors:
    print("TECH OVERHAUL VALIDATION FAILED")
    for error in errors:
        print(f"  - {error}")
    sys.exit(1)

print("TECH OVERHAUL VALIDATION PASSED")
print(f"  experimental blocks: {len(BLOCKS)}")
print(f"  experimental standalone items/upgrades: {len(ITEMS)}")
print("  recipes/models/blockstates/loot tables: present and JSON-parseable")
print("  GuideME/test-plan coverage: present")
