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
    "hybrid_conduit",
    "matter_storage_matrix",
    "matter_excavator",
    "holographic_status_panel",
]
ITEMS = [
    "network_diagnostic_probe",
    "quantum_linker",
    "matter_storage_cell_64k",
    "matter_storage_cell_256k",
    "matter_storage_cell_1m",
    "matter_storage_cell_4m",
    "upgrade_parallel_processing",
]

# Every player-facing craftable introduced by the recent new-system passes is listed
# here, including systems that landed on main immediately before this testing branch.
# This is intentionally stricter than BLOCKS/ITEMS so future resource refactors cannot
# accidentally leave one of the new progression items creative-only.
NEW_CRAFTABLES = [
    "facility_network_controller",
    "anomaly_containment_unit",
    "chassis_core_capacitor",
    "chassis_core_overclock",
    "chassis_frame_lightweight",
    "chassis_frame_reinforced",
    "chassis_muscles_agility",
    "chassis_muscles_siege",
    "chassis_optics_hunter",
    "chassis_optics_precision",
    "chassis_shell_stealth",
    "chassis_shell_reactive",
    *BLOCKS,
    *ITEMS,
]

JAVA = [
    "src/main/java/matteroverdrive/block/TechMachineBlock.java",
    "src/main/java/matteroverdrive/block/HybridConduitBlock.java",
    "src/main/java/matteroverdrive/blockentity/GridCapacitorBlockEntity.java",
    "src/main/java/matteroverdrive/blockentity/AndroidInductionRelayBlockEntity.java",
    "src/main/java/matteroverdrive/blockentity/QuantumPowerRelayBlockEntity.java",
    "src/main/java/matteroverdrive/blockentity/HybridConduitBlockEntity.java",
    "src/main/java/matteroverdrive/blockentity/MatterStorageMatrixBlockEntity.java",
    "src/main/java/matteroverdrive/blockentity/MatterExcavatorBlockEntity.java",
    "src/main/java/matteroverdrive/blockentity/HolographicStatusPanelBlockEntity.java",
    "src/main/java/matteroverdrive/item/MatterStorageCellItem.java",
    "src/main/java/matteroverdrive/item/NetworkDiagnosticProbeItem.java",
    "src/main/java/matteroverdrive/item/QuantumLinkerItem.java",
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
    if not p.is_file(): errors.append(f"missing file: {path}")
    return p

for path in JAVA + GUIDES: require(path)
for block in BLOCKS:
    require(f"src/main/resources/assets/matteroverdrive/blockstates/{block}.json")
    require(f"src/main/resources/assets/matteroverdrive/models/block/{block}.json")
    require(f"src/main/resources/assets/matteroverdrive/models/item/{block}.json")
    require(f"src/main/resources/data/matteroverdrive/recipes/{block}.json")
    require(f"src/main/resources/data/matteroverdrive/loot_tables/blocks/{block}.json")
for item in ITEMS:
    require(f"src/main/resources/assets/matteroverdrive/models/item/{item}.json")
    require(f"src/main/resources/data/matteroverdrive/recipes/{item}.json")

# All newly invented survival-facing hardware must have a recipe that actually
# produces the matching registry id. Merely having a same-named JSON is not enough.
for craftable in dict.fromkeys(NEW_CRAFTABLES):
    recipe = require(f"src/main/resources/data/matteroverdrive/recipes/{craftable}.json")
    if not recipe.is_file():
        continue
    try:
        data = json.loads(recipe.read_text(encoding="utf-8"))
        result = data.get("result")
        result_item = result.get("item") if isinstance(result, dict) else result if isinstance(result, str) else None
        expected = f"matteroverdrive:{craftable}"
        if result_item != expected:
            errors.append(f"recipe {craftable}.json produces {result_item!r}, expected {expected!r}")
    except Exception as exc:
        errors.append(f"invalid recipe JSON: {recipe.relative_to(ROOT)}: {exc}")

for path in list((ROOT / "src/main/resources/assets/matteroverdrive/blockstates").glob("*.json")) + list((ROOT / "src/main/resources/assets/matteroverdrive/models/block").glob("*.json")) + list((ROOT / "src/main/resources/assets/matteroverdrive/models/item").glob("*.json")) + list((ROOT / "src/main/resources/data/matteroverdrive/recipes").glob("*.json")) + list((ROOT / "src/main/resources/data/matteroverdrive/loot_tables/blocks").glob("*.json")):
    name = path.stem
    if name not in BLOCKS and name not in ITEMS and name not in NEW_CRAFTABLES: continue
    try: json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc: errors.append(f"invalid JSON: {path.relative_to(ROOT)}: {exc}")

mod_blocks = require("src/main/java/matteroverdrive/registry/ModBlocks.java")
mod_items = require("src/main/java/matteroverdrive/registry/ModItems.java")
extra_be = require("src/main/java/matteroverdrive/registry/ModExtraBlockEntities.java")
client_events = require("src/main/java/matteroverdrive/client/ClientModEvents.java")
relay = require("src/main/java/matteroverdrive/blockentity/QuantumPowerRelayBlockEntity.java")
matter_network = require("src/main/java/matteroverdrive/network/MatterNetworkUtil.java")
energy_pipe_be = require("src/main/java/matteroverdrive/blockentity/EnergyPipeBlockEntity.java")
energy_pipe_block = require("src/main/java/matteroverdrive/block/EnergyPipeBlock.java")
visual_pipe_block = require("src/main/java/matteroverdrive/block/VisualPipeBlock.java")
hybrid_block = require("src/main/java/matteroverdrive/block/HybridConduitBlock.java")

if mod_blocks.is_file():
    text = mod_blocks.read_text(encoding="utf-8")
    for block in BLOCKS:
        if f'"{block}"' not in text: errors.append(f"ModBlocks does not register {block}")
if mod_items.is_file():
    text = mod_items.read_text(encoding="utf-8")
    for item in ITEMS:
        if f'"{item}"' not in text: errors.append(f"ModItems does not register {item}")
if extra_be.is_file():
    text = extra_be.read_text(encoding="utf-8")
    for key in ["GRID_CAPACITOR", "ANDROID_INDUCTION_RELAY", "QUANTUM_POWER_RELAY", "HYBRID_CONDUIT", "MATTER_STORAGE_MATRIX", "MATTER_EXCAVATOR", "HOLOGRAPHIC_STATUS_PANEL"]:
        if key not in text: errors.append(f"ModExtraBlockEntities missing {key}")
if client_events.is_file() and "HolographicStatusPanelRenderer" not in client_events.read_text(encoding="utf-8"): errors.append("Holographic status renderer is not registered client-side")
if relay.is_file():
    text = relay.read_text(encoding="utf-8")
    if "Set<BlockPos> links" not in text or "addLink" not in text or "MAX_LINKS" not in text: errors.append("Quantum Power Relay is not using explicit persisted links")
    if "LOADED_RELAYS" in text or "channel" in text.lower(): errors.append("Quantum Power Relay still contains automatic channel-pool linking")

if matter_network.is_file():
    text = matter_network.read_text(encoding="utf-8")
    if 'ModBlocks.get("matter_pipe")' not in text or 'ModBlocks.get("hybrid_conduit")' not in text:
        errors.append("Matter routing does not explicitly join Matter Pipe and Hybrid Conduit networks")
if energy_pipe_be.is_file():
    text = energy_pipe_be.read_text(encoding="utf-8")
    if "neighbor instanceof EnergyPipeBlockEntity" not in text:
        errors.append("Energy routing no longer treats Hybrid Conduit as part of the EnergyPipe graph")
if energy_pipe_block.is_file() and "neighbour.getBlock() instanceof EnergyPipeBlock" not in energy_pipe_block.read_text(encoding="utf-8"):
    errors.append("Heavy Energy Cable does not explicitly render a junction to Hybrid Conduit")
if visual_pipe_block.is_file() and "neighbour.getBlock() instanceof HybridConduitBlock" not in visual_pipe_block.read_text(encoding="utf-8"):
    errors.append("Matter Transport Pipe does not explicitly render a junction to Hybrid Conduit")
if hybrid_block.is_file() and 'ModBlocks.get("matter_pipe")' not in hybrid_block.read_text(encoding="utf-8"):
    errors.append("Hybrid Conduit does not explicitly render a junction back to Matter Transport Pipe")

for guide in [ROOT / "src/main/resources/assets/matteroverdrive/guides/matteroverdrive/guide/index.md", ROOT / "src/main/resources/assets/matteroverdrive/guides/matteroverdrive/guide/current_features.md"]:
    if guide.is_file() and "[Star Map]" in guide.read_text(encoding="utf-8"): errors.append(f"retired Star Map remains linked from active guide: {guide.relative_to(ROOT)}")

if errors:
    print("TECH OVERHAUL VALIDATION FAILED")
    for error in errors: print(f"  - {error}")
    sys.exit(1)

print("TECH OVERHAUL VALIDATION PASSED")
print(f"  experimental blocks: {len(BLOCKS)}")
print(f"  experimental standalone items/upgrades: {len(ITEMS)}")
print(f"  new survival craftables with verified recipes: {len(list(dict.fromkeys(NEW_CRAFTABLES)))}")
print("  explicit quantum relay links: present")
print("  heavy cable <-> hybrid FE junction: present")
print("  matter pipe <-> hybrid Matter junction: present")
print("  hybrid FE+Matter conduit routing: present")
print("  recipes/models/blockstates/loot tables: present and JSON-parseable")
print("  GuideME/test-plan coverage: present")
