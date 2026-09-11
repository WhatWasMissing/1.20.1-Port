#!/usr/bin/env python3
"""Static topology validator for Matter Overdrive generated structures.

Validates the authored playthrough contract:
terrain -> approach -> entrance -> critical path -> guarded objective -> exit.
Also enforces exploration-first worldgen: structures must not hand out free working
Matter Overdrive infrastructure.
"""
from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
import re
import sys
from typing import Dict, List, Tuple

ROOT = Path(__file__).resolve().parent
WORLDGEN = ROOT / "src/main/java/matteroverdrive/worldgen"
EVENTS = ROOT / "src/main/java/matteroverdrive/event"
LOOT = ROOT / "src/main/resources/data/matteroverdrive/loot_tables/chests/facilities"
REGISTRY = ROOT / "src/main/java/matteroverdrive/registry/ModStructures.java"

@dataclass(frozen=True)
class Site:
    name: str
    source: str
    required: Tuple[str, ...]

SITES: Tuple[Site, ...] = (
    Site("crashed_ship", "LegacyVanillaStructurePiece.java", ("crashedShip", "story_cache", "salvage", "android_spawner")),
    Site("cargo_ship", "LegacyVanillaStructurePiece.java", ("cargoShip", "story_cache", "salvage", "android_spawner")),
    Site("underwater_base", "LegacyVanillaStructurePiece.java", ("underwaterBase", "South airlock", "story_cache")),
    Site("mad_scientist_house", "LegacyVanillaStructurePiece.java", ("madScientistLab", "stairStart", "story_cache")),
    Site("android_house", "LegacyVanillaStructurePiece.java", ("androidSafehouse", "corridorX", "corridorZ", "story_cache")),
    Site("sand_pit", "LegacyVanillaStructurePiece.java", ("excavation", "Continuous L-shaped descent", "story_cache")),
    Site("synthetic_manufacturing_plant", "TechnologyFacilityStructurePiece.java", ("PLANT_ENTRANCE", "SECURITY_CHECKPOINT", "MANUFACTURING_CORE", "SHIPPING_WING")),
    Site("matter_refinery", "TechnologyFacilityStructurePiece.java", ("REFINERY_ENTRANCE", "SECURITY_CHECKPOINT", "REFINERY_CORE", "PROCESSING_WING")),
    Site("quantum_relay_station", "TechnologyFacilityStructurePiece.java", ("RELAY_ENTRANCE", "CONTROL_WING", "QUANTUM_CORE")),
    Site("android_command_bunker", "TechnologyFacilityStructurePiece.java", ("BUNKER_ENTRANCE", "SECURITY_CHECKPOINT", "BUNKER_COMMAND", "ARMORY")),
    Site("fusion_research_complex", "TechnologyFacilityStructurePiece.java", ("FUSION_ENTRANCE", "REACTOR_CONTROL", "FUSION_CORE")),
    Site("black_site", "TechnologyFacilityStructurePiece.java", ("BLACK_ENTRANCE", "BLACK_SECURITY", "BLACK_CORE", "EXCAVATION_SHAFT", "BLACK_VAULT")),
    Site("deep_matter_vault", "FrontierSitePiece.java", ("VAULT_ENTRY", "SHAFT", "VAULT_SECURITY", "VAULT_CORE")),
    Site("autonomous_drone_foundry", "FrontierSitePiece.java", ("FOUNDRY_ENTRY", "FOUNDRY_CONTROL", "FOUNDRY_FABRICATION", "FOUNDRY_HANGAR")),
    Site("anomaly_quarantine_site", "FrontierSitePiece.java", ("QUARANTINE_ENTRY", "SHAFT", "QUARANTINE_DECON", "QUARANTINE_SECURITY", "QUARANTINE_CONTAINMENT")),
    Site("orbital_recovery_array", "FrontierSitePiece.java", ("RECOVERY_ENTRY", "RECOVERY_CONTROL", "RECOVERY_ARRAY")),
)

FORBIDDEN_PATTERNS = {
    "LegacyNativeStructure.java": (
        (r"LegacyNativeStructurePiece\(kind, origin\)", "New legacy starts still use the retired pre-standard generator."),
        (r"surfaceY \+ 24", "Legacy structure placement still contains elevated non-vanilla boarding."),
    ),
    "TechnologyFacilityStructure.java": (
        (r"FacilityInfrastructurePiece\.assemble", "Modern facilities still assemble stale pre-redesign infrastructure overlays."),
        (r"FacilityTerrainPiece\.assemble", "Modern facilities still assemble stale pre-redesign terrain overlays."),
    ),
    "FrontierSiteStructure.java": (
        (r"DEEP_MATTER_VAULT -> Math\.max\(minimum, surfaceY - 22\)", "Deep Matter Vault entry is still authored from the buried pre-repair height."),
    ),
}


def read(path: Path) -> str:
    if not path.exists():
        raise FileNotFoundError(path)
    return path.read_text(encoding="utf-8")


def main() -> int:
    errors: List[str] = []
    warnings: List[str] = []
    cache: Dict[str, str] = {}

    def src(name: str) -> str:
        if name not in cache:
            cache[name] = read(WORLDGEN / name)
        return cache[name]

    for site in SITES:
        s = src(site.source)
        missing = [token for token in site.required if token not in s]
        if missing:
            errors.append(f"{site.name}: missing required topology/policy token(s): {', '.join(missing)}")

    for filename, patterns in FORBIDDEN_PATTERNS.items():
        s = src(filename)
        for pattern, message in patterns:
            if re.search(pattern, s):
                errors.append(message)

    legacy_start = src("LegacyNativeStructure.java")
    if "new LegacyVanillaStructurePiece(kind, origin)" not in legacy_start:
        errors.append("LegacyNativeStructure is not wired to LegacyVanillaStructurePiece.")

    registry = read(REGISTRY)
    if "LEGACY_VANILLA_PIECE" not in registry or "LegacyVanillaStructurePiece::new" not in registry:
        errors.append("Legacy vanilla piece serializer is not registered.")
    if "LEGACY_NATIVE_PIECE" not in registry:
        errors.append("Old legacy serializer was removed; existing-world compatibility would be broken.")

    # Chunk-safe generation and force-loading prohibition.
    active_piece_files = ("LegacyVanillaStructurePiece.java", "TechnologyFacilityStructurePiece.java", "FrontierSitePiece.java")
    for filename in active_piece_files:
        s = src(filename)
        if "clip.isInside" not in s:
            errors.append(f"{filename}: no active chunk clip guard found")
        for token in ("getChunk(", "setChunkForced", "getChunkSource().addRegionTicket", "TicketType"):
            if token in s:
                errors.append(f"{filename}: forbidden force-load/synchronous chunk token present: {token}")

    # Legacy exploration-only source must not directly place core functional machines.
    legacy = src("LegacyVanillaStructurePiece.java")
    forbidden_machine_ids = (
        '"matter_analyzer"', '"decomposer"', '"replicator"', '"inscriber"',
        '"network_router"', '"network_switch"', '"fusion_reactor_controller"',
        '"drone_fabricator"', '"android_station"', '"matter_storage_matrix"'
    )
    for token in forbidden_machine_ids:
        if token in legacy:
            errors.append(f"Legacy exploration generator contains free functional machine id: {token}")

    sanitizer_path = EVENTS / "StructureExplorationSanitizer.java"
    if not sanitizer_path.exists():
        errors.append("StructureExplorationSanitizer.java is missing.")
    else:
        sanitizer = read(sanitizer_path)
        for token in ("FUNCTIONAL_BLOCKS", "getStructureWithPieceAt", "story_cache", "ensureGuard", "android_spawner"):
            if token not in sanitizer:
                errors.append(f"Structure exploration sanitizer missing token: {token}")

    if not (LOOT / "story_cache.json").exists():
        errors.append("Guarded structure story cache loot table is missing.")

    print("Matter Overdrive structure topology validator")
    print(f"Checked {len(SITES)} structure families")
    for warning in warnings:
        print(f"WARN: {warning}")
    for error in errors:
        print(f"FAIL: {error}")
    if errors:
        print(f"RESULT: FAIL ({len(errors)} blocking issue(s))")
        return 1
    print("RESULT: PASS")
    return 0

if __name__ == "__main__":
    sys.exit(main())
