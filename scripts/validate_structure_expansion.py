#!/usr/bin/env python3
"""Static gate for Matter Overdrive's native multi-chunk facility expansion."""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PIECE = ROOT / "src/main/java/matteroverdrive/worldgen/TechnologyFacilityStructurePiece.java"
INFRA = ROOT / "src/main/java/matteroverdrive/worldgen/FacilityInfrastructurePiece.java"
STRUCTURE = ROOT / "src/main/java/matteroverdrive/worldgen/TechnologyFacilityStructure.java"
MOD_BLOCKS = ROOT / "src/main/java/matteroverdrive/registry/ModBlocks.java"
MOD_STRUCTURES = ROOT / "src/main/java/matteroverdrive/registry/ModStructures.java"
RESTORATION = ROOT / "src/main/java/matteroverdrive/world/FacilityRestorationSavedData.java"
CONTROLLER = ROOT / "src/main/java/matteroverdrive/block/FacilityNetworkControllerBlock.java"
SPAWNER = ROOT / "src/main/java/matteroverdrive/blockentity/AndroidSpawnerBlockEntity.java"
ANDROID = ROOT / "src/main/java/matteroverdrive/entity/RogueAndroidEntity.java"
SECURITY_DOOR = ROOT / "src/main/java/matteroverdrive/block/SecurityDoorBlock.java"
LAYOUT_LAB = ROOT / "scripts/facility_layout_lab.py"

FACILITIES = [
    "synthetic_manufacturing_plant", "matter_refinery", "quantum_relay_station",
    "android_command_bunker", "fusion_research_complex", "black_site",
]
REQUIRED_PIECES = [
    "CORRIDOR_X", "CORRIDOR_Z", "SERVICE_GANTRY_X", "SERVICE_GANTRY_Z",
    "ROOF_PLANT", "RELAY_MAST", "SECURITY_CHECKPOINT", "OBSERVATION_BRIDGE",
    "EXCAVATION_SHAFT", "SALVAGE_YARD",
]
INFRA_KINDS = [
    "SERVICE_SPINE_X", "SERVICE_SPINE_Z", "SERVICE_ACCESS",
    "SERVICE_BRIDGE_LINK_NORTH", "SERVICE_BRIDGE_LINK_SOUTH",
    "SERVICE_CORRIDOR_X", "SERVICE_CORRIDOR_Z", "BLACK_LOWER_CORRIDOR_X",
    "SECURE_GATE_X", "SECURE_GATE_Z",
    "LOWERED_STEP_EAST", "LOWERED_STEP_WEST", "LOWERED_STEP_SOUTH",
    "LADDER_UP_12", "LADDER_DOWN_7", "LADDER_DOWN_12",
    "ENTRANCE_STAIR_NORTH_3", "PLANT_EAST_ENTRANCE_FIX", "BLACK_SURFACE_HATCH",
    "BLACK_VAULT_STAIR", "RESTORATION_TERMINAL",
]
INDUSTRIAL_BLOCKS = [
    "industrial_catwalk", "industrial_railing", "cable_tray",
    "warning_light", "damaged_panel", "security_door",
]
SAFE_ITEM_ONLY_VISUAL_FALLBACKS = {"anomaly_containment_unit"}
errors: list[str] = []


def require(path: Path) -> None:
    if not path.is_file(): errors.append(f"missing file: {path.relative_to(ROOT)}")


def parse_json(path: Path) -> dict | list | None:
    if not path.is_file(): return None
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"invalid JSON: {path.relative_to(ROOT)}: {exc}")
        return None


for p in (PIECE, INFRA, STRUCTURE, MOD_BLOCKS, MOD_STRUCTURES, RESTORATION,
          CONTROLLER, SPAWNER, ANDROID, SECURITY_DOOR, LAYOUT_LAB):
    require(p)

for name in FACILITIES:
    require(ROOT / f"src/main/resources/data/matteroverdrive/worldgen/structure/{name}.json")
    require(ROOT / f"src/main/resources/data/matteroverdrive/worldgen/structure_set/{name}.json")

for folder in ("structure", "structure_set"):
    for name in FACILITIES:
        parse_json(ROOT / f"src/main/resources/data/matteroverdrive/worldgen/{folder}/{name}.json")

if PIECE.is_file():
    text = PIECE.read_text(encoding="utf-8")
    if "star_map" in text.lower(): errors.append("retired Star Map reference found in modern facility generator")
    if "BoundingBox clip" not in text or "clip.isInside(pos)" not in text:
        errors.append("facility generation no longer enforces chunk-local BoundingBox writes")
    for piece in REQUIRED_PIECES:
        if piece not in text: errors.append(f"modern facility piece missing: {piece}")
    if "damageVariant()" not in text: errors.append("seeded damaged-room variation missing")
    if "assemblePlant" not in text or "assembleBlackSite" not in text:
        errors.append("facility-specific layout assemblers missing")
    if MOD_BLOCKS.is_file():
        block_registry = MOD_BLOCKS.read_text(encoding="utf-8")
        ids = set(re.findall(r'mod\("([^"]+)"', text))
        for block_id in sorted(ids - SAFE_ITEM_ONLY_VISUAL_FALLBACKS):
            if f'"{block_id}"' not in block_registry:
                errors.append(f"facility generator references unregistered block id: {block_id}")

if INFRA.is_file():
    text = INFRA.read_text(encoding="utf-8")
    if "star_map" in text.lower(): errors.append("retired Star Map reference found in infrastructure generator")
    if "clip.isInside(pos)" not in text or "getBoundingBox().isInside(pos)" not in text:
        errors.append("infrastructure generation is not double-clipped to chunk and owning piece")
    for kind in INFRA_KINDS:
        if kind not in text: errors.append(f"facility infrastructure piece missing: {kind}")
    for marker in (
        "Blocks.LADDER", "POLISHED_DEEPSLATE_STAIRS", "security_door", "facility_network_controller",
        "sideExit", "serviceAccess", "loweredTransition", "blackSurfaceHatch", "plantEastEntranceFix",
    ):
        if marker not in text: errors.append(f"facility traversal/restoration infrastructure missing marker: {marker}")
    for marker in (
        "c.offset(-12, 0, 0)", "c.offset(12, 0, 0)", "c.offset(0, 0, 11)",
        "BLACK_LOWER_CORRIDOR_X", "c.offset(-9, -6, 19)",
        "c.offset(0, 12, -27)", "LOWERED_STEP_SOUTH", "ENTRANCE_STAIR_NORTH_3",
    ):
        if marker not in text: errors.append(f"known facility topology fix regressed: {marker}")

if SECURITY_DOOR.is_file():
    text = SECURITY_DOOR.read_text(encoding="utf-8")
    for marker in ("BlockStateProperties.POWERED", "columnPowered", "columnStoredPowered", "syncColumn", "Block.UPDATE_CLIENTS"):
        if marker not in text: errors.append(f"stacked Security Door redstone synchronization missing: {marker}")

security_state = ROOT / "src/main/resources/assets/matteroverdrive/blockstates/security_door.json"
state_json = parse_json(security_state)
if isinstance(state_json, dict) and "multipart" not in state_json:
    errors.append("security_door blockstate must ignore POWERED visually via multipart selectors")

if STRUCTURE.is_file():
    text = STRUCTURE.read_text(encoding="utf-8")
    if "layout = Math.floorMod" not in text: errors.append("stable per-chunk layout variant selection missing")
    if "TechnologyFacilityStructurePiece.assemble(builder, kind, origin, layout)" not in text:
        errors.append("layout variant is not forwarded into primary piece assembly")
    if "FacilityInfrastructurePiece.assemble(builder, kind, origin, layout)" not in text:
        errors.append("infrastructure overlay is not assembled by native structure generation")

if MOD_STRUCTURES.is_file() and "FACILITY_INFRASTRUCTURE_PIECE" not in MOD_STRUCTURES.read_text(encoding="utf-8"):
    errors.append("facility infrastructure StructurePieceType is not registered")

if RESTORATION.is_file():
    text = RESTORATION.read_text(encoding="utf-8")
    for marker in ("STAGE_POWER", "STAGE_REPAIR", "STAGE_RESEARCH", "STAGE_COMPLETE", "SavedData", "getStructureWithPieceAt"):
        if marker not in text: errors.append(f"persistent facility restoration missing marker: {marker}")
if CONTROLLER.is_file():
    text = CONTROLLER.read_text(encoding="utf-8")
    for marker in ("isEmergencyPowerCell", "isRepairCircuit", "matchesResearch", "SECURE ACCESS RELEASED"):
        if marker not in text: errors.append(f"facility controller restoration flow missing marker: {marker}")
if SPAWNER.is_file():
    text = SPAWNER.read_text(encoding="utf-8")
    for marker in ("FacilityRestorationSavedData.isFacilityRestored", "facilityInitialReserve", "applyFacilitySecurityProfile"):
        if marker not in text: errors.append(f"generated security restoration integration missing marker: {marker}")
if ANDROID.is_file():
    text = ANDROID.read_text(encoding="utf-8")
    for marker in ("FacilitySecurityProfile", "Assembly Defender", "Relay Sentry", "Command Guard", "Containment Sentry", "Black Site Warden"):
        if marker not in text: errors.append(f"structure-specific Android identity missing marker: {marker}")

for block in INDUSTRIAL_BLOCKS:
    if MOD_BLOCKS.is_file() and f'"{block}"' not in MOD_BLOCKS.read_text(encoding="utf-8"):
        errors.append(f"ModBlocks does not register industrial block: {block}")
    for rel in (
        f"src/main/resources/assets/matteroverdrive/blockstates/{block}.json",
        f"src/main/resources/assets/matteroverdrive/models/item/{block}.json",
        f"src/main/resources/data/matteroverdrive/recipes/{block}.json",
        f"src/main/resources/data/matteroverdrive/loot_tables/blocks/{block}.json",
    ):
        path = ROOT / rel
        require(path)
        parse_json(path)

for model in ("industrial_catwalk", "industrial_railing", "cable_tray", "warning_light", "damaged_panel", "security_door_closed", "security_door_open"):
    path = ROOT / f"src/main/resources/assets/matteroverdrive/models/block/{model}.json"
    require(path)
    parse_json(path)

items = ROOT / "src/main/java/matteroverdrive/registry/ModItems.java"
archive = ROOT / "src/main/java/matteroverdrive/item/FacilityResearchItem.java"
require(items); require(archive)
if items.is_file() and archive.is_file():
    registered = set(re.findall(r'"([a-z0-9_.]+)"', items.read_text(encoding="utf-8")))
    archive_source = archive.read_text(encoding="utf-8")
    for family in FACILITIES + ["salvage"]:
        path = ROOT / f"src/main/resources/data/matteroverdrive/loot_tables/chests/facilities/{family}.json"
        require(path)
        if not path.is_file(): continue
        try:
            table = json.loads(path.read_text(encoding="utf-8"))
            if table.get("type") != "minecraft:chest": errors.append(f"wrong loot context for {family}")
            archives = []
            for pool in table["pools"]:
                for entry in pool["entries"]:
                    namespace, name = entry["name"].split(":", 1)
                    if namespace == "matteroverdrive" and name not in registered:
                        errors.append(f"unknown loot item {entry['name']} in {family}")
                    if name == "facility_research": archives.append(entry)
            if family != "salvage":
                if family.upper() + "(" not in archive_source: errors.append(f"unhandled research family: {family}")
                expected = '{FacilityArchive:"' + family.upper() + '"}'
                if len(archives) != 1 or archives[0]["functions"][0]["tag"] != expected:
                    errors.append(f"missing/mismatched guaranteed dossier: {family}")
        except (ValueError, KeyError, TypeError, IndexError) as exc:
            errors.append(f"invalid facility loot {family}: {exc}")

if LAYOUT_LAB.is_file():
    text = LAYOUT_LAB.read_text(encoding="utf-8")
    for marker in ("FACILITY LAYOUT LAB PASSED: 18 layouts", "qa_core_link_west", "lower_lab_link", "surface_hatch", "lowered_step"):
        if marker not in text: errors.append(f"facility visual QA lab missing regression marker: {marker}")

for doc in (
    ROOT / "docs/STRUCTURE_RESTORATION_PASS.md",
    ROOT / "docs/STRUCTURE_QA_VISUAL_LAB.md",
    ROOT / "docs/testing/FACILITY_RESTORATION_TEST_PLAN.md",
    ROOT / "src/main/resources/assets/matteroverdrive/guides/matteroverdrive/guide/facility_restoration.md",
): require(doc)

if errors:
    print("STRUCTURE EXPANSION VALIDATION FAILED")
    for error in errors: print(f"  - {error}")
    sys.exit(1)

print("STRUCTURE EXPANSION VALIDATION PASSED")
print(f"  native facilities: {len(FACILITIES)}")
print(f"  primary reusable piece types checked: {len(REQUIRED_PIECES)}")
print(f"  infrastructure piece kinds checked: {len(INFRA_KINDS)}")
print(f"  reusable industrial blocks checked: {len(INDUSTRIAL_BLOCKS)}")
print("  structure + structure_set JSON: parseable")
print("  facility loot: 7 tables, registered items, six matching research archives")
print("  restoration/security hooks and stacked-door redstone state: present")
print("  topology regression guards and visual layout lab: present")
print("  traversal infrastructure and double clipping: present")
print("  retired Star Map reference: absent from modern worldgen")
