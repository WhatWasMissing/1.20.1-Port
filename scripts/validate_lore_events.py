#!/usr/bin/env python3
"""Validate that Matter Overdrive lore is reachable through player events."""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/matteroverdrive"
RESOURCES = ROOT / "src/main/resources"
errors: list[str] = []


def read(path: Path) -> str:
    if not path.is_file():
        errors.append(f"missing file: {path.relative_to(ROOT)}")
        return ""
    return path.read_text(encoding="utf-8")


def load(path: Path):
    try:
        return json.loads(read(path))
    except json.JSONDecodeError as exc:
        errors.append(f"invalid JSON: {path.relative_to(ROOT)}: {exc}")
        return None


catalog = read(JAVA / "world/StructureLoreCatalog.java")
structure_events = read(JAVA / "event/StructureLoreEvents.java")
technology_events = read(JAVA / "event/TechnologyLoreEvents.java")
discovery_events = read(JAVA / "event/PlayerDiscoveryEvents.java")
fragment_item = read(JAVA / "item/RecoveredLoreFragmentItem.java")
npc_flow = read(JAVA / "quest/NpcAssignmentFlow.java")
field_operations = read(JAVA / "quest/FieldOperations.java")
environmental_events = read(JAVA / "event/EnvironmentalStorytellingEvents.java")
frontier_events = read(JAVA / "event/FrontierExpeditionEvents.java")

record_ids = re.findall(r'\br\("([a-z0-9_]+)"', catalog)
if len(record_ids) != 16 or len(set(record_ids)) != 16:
    errors.append(f"structure lore catalog must contain 16 unique records, found {len(record_ids)}")
record_set = set(record_ids)

# Only inspect the explicit player-event route declarations, not the narrative switch
# statements below them. This prevents a site from passing merely because it is named in
# legacy hazard/voice text.
route_section = structure_events.split("private StructureLoreEvents()", 1)[0]
route_values = set(re.findall(r'Map\.entry\("[^"]+",\s*"([a-z0-9_]+)"\)', route_section))
route_values.update(re.findall(r'Map\.of\([^;]*?"[^"]+",\s*"([a-z0-9_]+)"', route_section, re.DOTALL))
missing_routes = sorted(record_set - route_values)
if missing_routes:
    errors.append("structure lore records without player-event routes: " + ", ".join(missing_routes))

required_markers = {
    "StructureLoreEvents": (
        "discoverFromPlayerEvent", "discoverFromTechnology", "discoverFromEntity",
        "discoverFromBlock", "discoverFromAssignment", "discoverFromFieldOperation",
        "discoverFromAmbientFragment", "discoverRecord", "StructureLoreSavedData",
        "EnvironmentalStorytellingEvents.observeFromPlayerEvent",
        "FrontierExpeditionEvents.discoverFromPlayerEvent",
    ),
    "TechnologyLoreEvents": (
        "@SubscribeEvent", "onCrafted", "onPickup", "onPlaced", "onPlayerTick",
        "StructureLoreEvents.discoverFromTechnology",
    ),
    "PlayerDiscoveryEvents": (
        "crafted", "mined", "killed", "StructureLoreEvents.discoverFromTechnology",
        "StructureLoreEvents.discoverFromBlock", "StructureLoreEvents.discoverFromEntity",
    ),
    "RecoveredLoreFragmentItem": (
        "use(", "AmbientLoreSavedData", "StructureLoreEvents.discoverFromAmbientFragment",
        "MatterOverdriveLoreId",
    ),
    "NpcAssignmentFlow": ("PlayerDiscoveryLog.record", "StructureLoreEvents.discoverFromAssignment"),
    "FieldOperations": ("complete(", "StructureLoreEvents.discoverFromFieldOperation"),
    "EnvironmentalStorytellingEvents": ("observeFromPlayerEvent", "EnvironmentalStorytellingCatalog"),
    "FrontierExpeditionEvents": ("discoverFromPlayerEvent", "FrontierExpeditionSavedData"),
}
sources = {
    "StructureLoreEvents": structure_events,
    "TechnologyLoreEvents": technology_events,
    "PlayerDiscoveryEvents": discovery_events,
    "RecoveredLoreFragmentItem": fragment_item,
    "NpcAssignmentFlow": npc_flow,
    "FieldOperations": field_operations,
    "EnvironmentalStorytellingEvents": environmental_events,
    "FrontierExpeditionEvents": frontier_events,
}
for name, markers in required_markers.items():
    for marker in markers:
        if marker not in sources[name]:
            errors.append(f"{name}: missing player-event lore marker {marker}")

ambient_catalog = read(JAVA / "world/AmbientLoreCatalog.java")
ambient_entries = re.findall(r'\be\("([a-z0-9_]+)",\s*"([a-z0-9_]+)"', ambient_catalog)
ambient_ids = {entry_id for entry_id, _ in ambient_entries}
ambient_sites = {site_id for _, site_id in ambient_entries}
if not ambient_entries:
    errors.append("ambient lore catalog has no entries")
if len(ambient_ids) != len(ambient_entries):
    errors.append("ambient lore catalog contains duplicate entry ids")
if not ambient_sites.issubset(record_set):
    errors.append("ambient lore references unknown structure records: "
                  + ", ".join(sorted(ambient_sites - record_set)))

ambient_loot_path = RESOURCES / "data/matteroverdrive/loot_tables/chests/facilities/ambient_lore.json"
ambient_loot = load(ambient_loot_path)
ambient_loot_ids = set(re.findall(r'MatterOverdriveLoreId:\\"([a-z0-9_]+)\\"', json.dumps(ambient_loot))) if ambient_loot else set()
if ambient_ids != ambient_loot_ids:
    errors.append("ambient lore loot/catalog mismatch: "
                  + f"missing loot={sorted(ambient_ids - ambient_loot_ids)}, "
                  + f"unknown loot={sorted(ambient_loot_ids - ambient_ids)}")

# No MO-authored placement data is allowed until the structure/content review is complete.
structure_sets = sorted((RESOURCES / "data/matteroverdrive/worldgen/structure_set").glob("*.json"))
if structure_sets:
    errors.append("active MO structure_set resources remain: "
                  + ", ".join(str(path.relative_to(ROOT)) for path in structure_sets))
technology_sites = RESOURCES / "data/matteroverdrive/forge/biome_modifier/technology_sites.json"
if technology_sites.exists():
    errors.append("technology_sites biome modifier remains active")

# Add-features modifiers are allowed only for the two ores and the natural
# Gravitational Anomaly. This catches a future structure placement route even if
# it is hidden under a newly named biome-modifier file.
allowed_natural_features = {"tritanium_ore", "dilithium_ore", "gravitational_anomaly"}
biome_modifier_dir = RESOURCES / "data/matteroverdrive/forge/biome_modifier"
for path in sorted(biome_modifier_dir.glob("*.json")):
    modifier = load(path)
    if not isinstance(modifier, dict):
        continue
    if modifier.get("type") == "forge:add_structures":
        errors.append(f"active structure biome modifier remains: {path.relative_to(ROOT)}")
    features = modifier.get("features", [])
    if isinstance(features, str):
        features = [features]
    for feature in features if isinstance(features, list) else []:
        if not isinstance(feature, str) or not feature.startswith("matteroverdrive:"):
            continue
        feature_id = feature.split(":", 1)[1]
        if feature_id not in allowed_natural_features:
            errors.append(f"unapproved MO natural feature route: {path.relative_to(ROOT)} -> {feature}")
if "structureManager" in environmental_events and "legacyStructureObservation" not in environmental_events:
    errors.append("environmental storytelling uses an unnamed structure-manager dependency")
if "structureManager" in frontier_events and "legacyStructureScan" not in frontier_events:
    errors.append("Frontier archive uses an unnamed structure-manager dependency")
if "structureManager" in structure_events and "legacyStructureScan" not in structure_events:
    errors.append("structure archive uses an unnamed structure-manager dependency")

if errors:
    print("LORE EVENT VALIDATION FAILED")
    for error in errors:
        print(f"  - {error}")
    sys.exit(1)

print("LORE EVENT VALIDATION PASSED")
print(f"  structure archive records routed through player events: {len(record_ids)}/16")
print(f"  ambient lore fragments catalogued and loot-backed: {len(ambient_entries)}")
print("  technology/craft/pickup/place, mine, combat, NPC assignment, field operation and fragment-use hooks: present")
print("  MO-authored natural structure placement routes: dormant")
