#!/usr/bin/env python3
"""Static audit of every retained Matter Overdrive world structure.

This is intentionally independent of Forge: it checks the data-pack contract,
registration coverage, terrain tags and the Java implementation families. In 0.7
the authored structures are dormant compatibility definitions, not active
fresh-world placement. It does not claim to replace an in-game test.
"""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data/matteroverdrive/worldgen"
JAVA = ROOT / "src/main/java/matteroverdrive"
OUT = ROOT / "build/reports/structure_audit.json"

def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")

def load(path: Path):
    return json.loads(read(path))

def main() -> int:
    errors: list[str] = []
    warnings: list[str] = []
    records: list[dict] = []
    structures = sorted((DATA / "structure").glob("*.json"))
    if not structures:
        errors.append("no native structures found")

    registered = read(JAVA / "registry/ModStructures.java")
    feature_registry = read(JAVA / "registry/ModFeatures.java")
    legacy = read(JAVA / "worldgen/LegacyParityStructureFeature.java")
    sites = read(JAVA / "worldgen/TechnologySiteFeature.java")
    discovery = read(JAVA / "world/TechnologySiteDiscoverySavedData.java")
    contracts = read(JAVA / "event/ContractEvents.java")
    datapad = read(JAVA / "item/DataPadItem.java")
    facility_piece = read(JAVA / "worldgen/TechnologyFacilityStructurePiece.java")
    modern_facility = read(JAVA / "worldgen/TechnologyFacilityStructure.java")
    metadata_markers = ("record RoomMetadata", "metadata(Room room)", "MORole", "MORequired", "MOConnectorAxis")
    metadata_contract = {"roles": ["room", "entrance", "security", "connector", "service_connector", "salvage", "exterior"],
                         "serialized": all(marker in facility_piece for marker in metadata_markers)}
    if not metadata_contract["serialized"]:
        errors.append("native facilities: room/connector metadata contract is not serialized")

    for path in structures:
        name = path.stem
        data = load(path)
        # Every MO-authored structure is intentionally dormant. Definitions and
        # serializers are still audited for save compatibility, but placement is
        # supplied by neither structure_set nor biome-modifier resources.
        structure_set = None
        if data.get("biomes") not in ("#minecraft:is_overworld", "#minecraft:is_ocean"):
            errors.append(f"{name}: unsupported biome tag {data.get('biomes')!r}")
        if data.get("step") not in ("surface_structures", "underground_structures"):
            errors.append(f"{name}: invalid generation step {data.get('step')!r}")
        if data.get("biomes") == "#minecraft:is_ocean" and data.get("step") != "surface_structures":
            errors.append(f"{name}: ocean site is not a surface structure")
        if name in {"android_command_bunker", "black_site"} and data.get("step") != "underground_structures":
            errors.append(f"{name}: buried facility must use underground_structures")
        if not re.search(r'(?:register|type|facilityType|frontierType)\("' + re.escape(name) + r'"', registered):
            errors.append(f"{name}: missing native registration")
        records.append({"id": name, "biomes": data.get("biomes"), "step": data.get("step"),
                        "spacing": None,
                        "separation": None,
                        "generation_contract": "dormant_compatibility_definition"})

    compact = {"abandoned_matter_lab", "android_relay_outpost", "anomaly_research_site", "matter_observatory", "field_logistics_depot"}
    for name in compact:
        if f'FEATURES.register("{name}"' not in feature_registry:
            errors.append(f"{name}: missing compact feature registration")
        if not (DATA / "configured_feature" / f"{name}.json").is_file():
            errors.append(f"{name}: missing configured feature")
        if not (DATA / "placed_feature" / f"{name}.json").is_file():
            errors.append(f"{name}: missing placed feature")
    for marker in ("MATTER_OBSERVATORY", "FIELD_LOGISTICS_DEPOT", "suitableSite"):
        if marker not in sites:
            errors.append(f"compact sites: missing implementation marker {marker}")
    for marker in ("TechnologySiteDiscoverySavedData", "discoverTechnologySite", "data_pad", "LAST_DISCOVERY_CHUNK", "chunkPosition", "siteNames", "chainStage"):
        if marker not in discovery + contracts:
            errors.append(f"compact sites: missing persistent discovery hook {marker}")
    for marker in ("UUID player", "player + \":\" + site", "count(UUID player)", "siteNames(UUID player)"):
        if marker not in discovery:
            errors.append(f"compact sites: discovery ledger is not player-scoped ({marker})")
    if "siteNames(player.getUUID())" not in datapad or "replace('_', ' ')" not in datapad:
        errors.append("compact sites: Data Pad does not list discovered site names")
    if "Investigation" not in datapad or "chainStage(player.getUUID())" not in datapad:
        errors.append("compact sites: Data Pad does not expose the ordered investigation lead")
    for marker in ("facility_research", "researchArchive(site)", "FacilityArchive", "research dossier recovered"):
        if marker not in contracts:
            errors.append(f"compact sites: missing research dossier reward hook {marker}")
    if "seedSiteCache" not in sites or sites.count("seedSiteCache") < 5:
        errors.append("compact sites: not every site seeds a lazy salvage cache")
    if not (ROOT / "src/main/resources/data/matteroverdrive/loot_tables/chests/facilities/salvage.json").is_file():
        errors.append("compact sites: shared salvage loot table is missing")
    if "spawnDrone" not in sites or sites.count("spawnDrone(level") < 2:
        errors.append("compact sites: relay/anomaly security encounter hooks are missing")
    for marker in ("getWorldBorder().isWithinBounds", "noCollision(drone)",
                   "MobSpawnType.STRUCTURE", "setPersistenceRequired"):
        if marker not in sites:
            errors.append(f"compact sites: unsafe drone spawn contract missing {marker}")

    # These are hard safety invariants for the two legacy stampers.
    for marker in ("getFluidState", "set(level", "switch (kind)"):
        if marker not in legacy:
            warnings.append(f"legacy feature review: expected terrain/write marker {marker!r} absent")
    if "WORLD_SURFACE_WG" not in sites:
        errors.append("compact sites: no world-surface heightmap selection")
    if 'block("star_map")' in legacy:
        errors.append("android_house: retired Star Map still stamped by legacy worldgen")

    legacy_kinds = {"CRASHED_SHIP", "CARGO_SHIP", "UNDERWATER_BASE", "MAD_SCIENTIST_HOUSE", "ANDROID_HOUSE", "SAND_PIT"}
    for kind in legacy_kinds:
        if kind not in legacy:
            errors.append(f"{kind.lower()}: missing legacy implementation kind")
    for kind in ("SYNTHETIC_MANUFACTURING_PLANT", "MATTER_REFINERY", "QUANTUM_RELAY_STATION",
                 "ANDROID_COMMAND_BUNKER", "FUSION_RESEARCH_COMPLEX", "BLACK_SITE"):
        if kind not in registered:
            errors.append(f"{kind.lower()}: missing facility implementation kind")
    if "terrainSuitable" not in read(JAVA / "worldgen/ModernizedStructureFeature.java"):
        warnings.append("mad_scientist_house: modernized terrain predicate not found")

    report = {"structures": records, "compact_features": sorted(compact),
              "room_metadata_contract": metadata_contract,
              "errors": errors, "warnings": warnings}
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(f"STRUCTURE AUDIT {'PASSED' if not errors else 'FAILED'}: {len(records)} dormant structures; {len(compact)} dormant compact systems")
    for issue in errors + warnings:
        print(f"  - {issue}")
    print(f"  report: {OUT.relative_to(ROOT)}")
    return 1 if errors else 0

if __name__ == "__main__":
    raise SystemExit(main())
