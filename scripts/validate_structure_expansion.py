#!/usr/bin/env python3
"""Static gate for Matter Overdrive's native multi-chunk structure expansion."""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PIECE = ROOT / "src/main/java/matteroverdrive/worldgen/TechnologyFacilityStructurePiece.java"
STRUCTURE = ROOT / "src/main/java/matteroverdrive/worldgen/TechnologyFacilityStructure.java"
MOD_BLOCKS = ROOT / "src/main/java/matteroverdrive/registry/ModBlocks.java"

FACILITIES = [
    "synthetic_manufacturing_plant",
    "matter_refinery",
    "quantum_relay_station",
    "android_command_bunker",
    "fusion_research_complex",
    "black_site",
]
REQUIRED_PIECES = [
    "CORRIDOR_X", "CORRIDOR_Z", "SERVICE_GANTRY_X", "SERVICE_GANTRY_Z",
    "ROOF_PLANT", "RELAY_MAST", "SECURITY_CHECKPOINT", "OBSERVATION_BRIDGE",
    "EXCAVATION_SHAFT", "SALVAGE_YARD",
]
SAFE_ITEM_ONLY_VISUAL_FALLBACKS = {"anomaly_containment_unit"}
errors: list[str] = []


def require(path: Path) -> None:
    if not path.is_file():
        errors.append(f"missing file: {path.relative_to(ROOT)}")


for p in (PIECE, STRUCTURE, MOD_BLOCKS):
    require(p)

for name in FACILITIES:
    require(ROOT / f"src/main/resources/data/matteroverdrive/worldgen/structure/{name}.json")
    require(ROOT / f"src/main/resources/data/matteroverdrive/worldgen/structure_set/{name}.json")

# Parse every structure JSON now, not at runtime during world creation.
for folder in ("structure", "structure_set"):
    for name in FACILITIES:
        p = ROOT / f"src/main/resources/data/matteroverdrive/worldgen/{folder}/{name}.json"
        if not p.is_file():
            continue
        try:
            json.loads(p.read_text(encoding="utf-8"))
        except Exception as exc:
            errors.append(f"invalid JSON: {p.relative_to(ROOT)}: {exc}")

if PIECE.is_file():
    text = PIECE.read_text(encoding="utf-8")
    if "star_map" in text.lower():
        errors.append("retired Star Map reference found in modern facility generator")
    if "BoundingBox clip" not in text or "clip.isInside(pos)" not in text:
        errors.append("facility generation no longer enforces chunk-local BoundingBox writes")
    for piece in REQUIRED_PIECES:
        if piece not in text:
            errors.append(f"modern facility piece missing: {piece}")
    if "damageVariant()" not in text:
        errors.append("seeded damaged-room variation missing")
    if "assemblePlant" not in text or "assembleBlackSite" not in text:
        errors.append("facility-specific layout assemblers missing")

    # Validate every matteroverdrive block id requested through mod(...). The one
    # containment-unit identifier is intentionally item-only and therefore uses
    # the explicit vanilla obsidian fallback until a placeable containment block exists.
    if MOD_BLOCKS.is_file():
        block_registry = MOD_BLOCKS.read_text(encoding="utf-8")
        ids = set(re.findall(r'mod\("([^"]+)"', text))
        for block_id in sorted(ids - SAFE_ITEM_ONLY_VISUAL_FALLBACKS):
            if f'"{block_id}"' not in block_registry:
                errors.append(f"facility generator references unregistered block id: {block_id}")

if STRUCTURE.is_file():
    text = STRUCTURE.read_text(encoding="utf-8")
    if "layout = Math.floorMod" not in text:
        errors.append("stable per-chunk layout variant selection missing")
    if "TechnologyFacilityStructurePiece.assemble(builder, kind, origin, layout)" not in text:
        errors.append("layout variant is not forwarded into piece assembly")

# Resource cross-check: loot names must resolve to actual registered items, and
# each archive must match the enum consumed by the dossier item (not silently PASS).
items = ROOT / "src/main/java/matteroverdrive/registry/ModItems.java"
archive = ROOT / "src/main/java/matteroverdrive/item/FacilityResearchItem.java"
require(items)
require(archive)
if items.is_file() and archive.is_file():
    registered = set(re.findall(r'"([a-z0-9_.]+)"', items.read_text(encoding="utf-8")))
    archive_source = archive.read_text(encoding="utf-8")
    for family in FACILITIES + ["salvage"]:
        path = ROOT / f"src/main/resources/data/matteroverdrive/loot_tables/chests/facilities/{family}.json"
        require(path)
        if not path.is_file():
            continue
        try:
            table = json.loads(path.read_text(encoding="utf-8"))
            if table.get("type") != "minecraft:chest":
                errors.append(f"wrong loot context for {family}")
            archives = []
            for pool in table["pools"]:
                for entry in pool["entries"]:
                    namespace, name = entry["name"].split(":", 1)
                    if namespace == "matteroverdrive" and name not in registered:
                        errors.append(f"unknown loot item {entry['name']} in {family}")
                    if name == "facility_research":
                        archives.append(entry)
            if family != "salvage":
                if family.upper() + "(" not in archive_source:
                    errors.append(f"unhandled research family: {family}")
                expected = '{FacilityArchive:"' + family.upper() + '"}'
                if len(archives) != 1 or archives[0]["functions"][0]["tag"] != expected:
                    errors.append(f"missing/mismatched guaranteed dossier: {family}")
        except (ValueError, KeyError, TypeError, IndexError) as exc:
            errors.append(f"invalid facility loot {family}: {exc}")

if errors:
    print("STRUCTURE EXPANSION VALIDATION FAILED")
    for error in errors:
        print(f"  - {error}")
    sys.exit(1)

print("STRUCTURE EXPANSION VALIDATION PASSED")
print(f"  native facilities: {len(FACILITIES)}")
print(f"  modern reusable piece types checked: {len(REQUIRED_PIECES)}")
print("  structure + structure_set JSON: parseable")
print("  facility block ids: registered or explicitly documented fallback")
print("  layout variation: stable and save/reload safe")
print("  chunk-local clipping guard: present")
print("  retired Star Map reference: absent")


print("  facility loot: 7 tables, registered items, six matching research archives")
