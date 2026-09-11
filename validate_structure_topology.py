#!/usr/bin/env python3
"""Static topology validator for Matter Overdrive generated structures.

This validates the authored vanilla-style playthrough contract before runtime:
terrain -> approach -> entrance -> readable critical path -> objective -> exit.
It also protects the exploration-first rule that generated structures must not hand
out functional Matter Overdrive infrastructure as free world loot.

Run from repository root:
    python validate_structure_topology.py
or on Windows:
    VALIDATE_STRUCTURE_TOPOLOGY.bat

Exit code is non-zero on any blocking failure.
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

@dataclass(frozen=True)
class Site:
    name: str
    source: str
    entrance: str
    objective: str
    required: Tuple[str, ...]

SITES: Tuple[Site, ...] = (
    Site("crashed_ship", "LegacyNativeStructurePiece.java", "south hull breach", "engineering tail", ("central route", "cockpit", "engineering")),
    Site("cargo_ship", "LegacyNativeStructurePiece.java", "loading", "engineering", ("circulation spine", "cargo bays", "engineering")),
    Site("underwater_base", "LegacyNativeStructurePiece.java", "South airlock", "Central hub", ("airlock", "Central hub", "tube")),
    Site("mad_scientist_house", "LegacyNativeStructurePiece.java", "surface residence", "hidden basement laboratory", ("surface residence", "stairwell", "laboratory")),
    Site("android_house", "LegacyNativeStructurePiece.java", "front entrance", "rear secure", ("atrium", "offset wings", "rear secure")),
    Site("sand_pit", "LegacyNativeStructurePiece.java", "surface", "Buried relic floor", ("terraces", "descent ramp", "Buried relic")),
    Site("synthetic_manufacturing_plant", "TechnologyFacilityStructurePiece.java", "PLANT_ENTRANCE", "MANUFACTURING_CORE", ("PLANT_ENTRANCE", "SECURITY_CHECKPOINT", "MANUFACTURING_CORE", "SHIPPING_WING")),
    Site("matter_refinery", "TechnologyFacilityStructurePiece.java", "REFINERY_ENTRANCE", "REFINERY_CORE", ("REFINERY_ENTRANCE", "SECURITY_CHECKPOINT", "REFINERY_CORE", "PROCESSING_WING")),
    Site("quantum_relay_station", "TechnologyFacilityStructurePiece.java", "RELAY_ENTRANCE", "QUANTUM_CORE", ("RELAY_ENTRANCE", "CONTROL_WING", "QUANTUM_CORE")),
    Site("android_command_bunker", "TechnologyFacilityStructurePiece.java", "BUNKER_ENTRANCE", "BUNKER_COMMAND", ("BUNKER_ENTRANCE", "SECURITY_CHECKPOINT", "BUNKER_COMMAND", "ARMORY")),
    Site("fusion_research_complex", "TechnologyFacilityStructurePiece.java", "FUSION_ENTRANCE", "FUSION_CORE", ("FUSION_ENTRANCE", "REACTOR_CONTROL", "FUSION_CORE")),
    Site("black_site", "TechnologyFacilityStructurePiece.java", "BLACK_ENTRANCE", "BLACK_VAULT", ("BLACK_ENTRANCE", "BLACK_SECURITY", "BLACK_CORE", "EXCAVATION_SHAFT", "BLACK_VAULT")),
    Site("deep_matter_vault", "FrontierSitePiece.java", "VAULT_ENTRY", "VAULT_CORE", ("VAULT_ENTRY", "SHAFT", "VAULT_SECURITY", "VAULT_CORE")),
    Site("autonomous_drone_foundry", "FrontierSitePiece.java", "FOUNDRY_ENTRY", "FOUNDRY_HANGAR", ("FOUNDRY_ENTRY", "FOUNDRY_CONTROL", "FOUNDRY_FABRICATION", "FOUNDRY_HANGAR")),
    Site("anomaly_quarantine_site", "FrontierSitePiece.java", "QUARANTINE_ENTRY", "QUARANTINE_CONTAINMENT", ("QUARANTINE_ENTRY", "SHAFT", "QUARANTINE_DECON", "QUARANTINE_SECURITY", "QUARANTINE_CONTAINMENT")),
    Site("orbital_recovery_array", "FrontierSitePiece.java", "RECOVERY_ENTRY", "RECOVERY_ARRAY", ("RECOVERY_ENTRY", "RECOVERY_CONTROL", "RECOVERY_ARRAY")),
)

FORBIDDEN_PATTERNS = {
    "LegacyNativeStructure.java": (
        (r"case CARGO_SHIP -> Math\.max\(86, surfaceY \+ 24\)", "Cargo Ship still floats above normal terrain without guaranteed boarding."),
    ),
    "LegacyNativeStructurePiece.java": (
        (r"c\.offset\(-7, 0, 3\), true", "Android Safehouse west connector still uses the audited off-by-one coordinate."),
        (r"c\.offset\(7, 0, 3\), true", "Android Safehouse east connector still uses the audited off-by-one coordinate."),
    ),
    "FrontierSiteStructure.java": (
        (r"DEEP_MATTER_VAULT -> Math\.max\(minimum, surfaceY - 22\)", "Deep Matter Vault entry is still buried below terrain."),
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

    # 1) Every structure family must retain its authored waypoints.
    for site in SITES:
        s = src(site.source)
        missing = [token for token in site.required if token not in s]
        if missing:
            errors.append(f"{site.name}: missing required topology token(s): {', '.join(missing)}")
        if site.entrance not in s:
            errors.append(f"{site.name}: entrance marker missing: {site.entrance}")
        if site.objective not in s:
            errors.append(f"{site.name}: objective marker missing: {site.objective}")

    # 2) Known regression signatures discovered by the static audit.
    for filename, patterns in FORBIDDEN_PATTERNS.items():
        s = src(filename)
        for pattern, message in patterns:
            if re.search(pattern, s):
                errors.append(message)

    # 3) Modern redesign is authoritative. Old helper overlays may remain in source
    # for save/registry compatibility, but they must not be assembled into new starts.
    modern = src("TechnologyFacilityStructure.java")
    for forbidden in (
        "FacilityInfrastructurePiece.assemble(builder, kind, origin, layout)",
        "FacilityTerrainPiece.assemble(builder, kind, origin, layout)",
    ):
        if forbidden in modern:
            errors.append("TechnologyFacilityStructure still assembles a stale pre-redesign helper overlay: " + forbidden)

    # 4) Native/chunk-safe generation remains mandatory.
    for filename in ("LegacyNativeStructurePiece.java", "TechnologyFacilityStructurePiece.java", "FrontierSitePiece.java"):
        s = src(filename)
        if "clip.isInside" not in s:
            errors.append(f"{filename}: no active chunk clip guard found")
        force_tokens = ("getChunk(", "setChunkForced", "getChunkSource().addRegionTicket", "TicketType")
        for token in force_tokens:
            if token in s:
                errors.append(f"{filename}: forbidden force-load/synchronous chunk token present: {token}")

    # 5) Exploration-first policy must be wired: generated functional machines are
    # sanitized only when they are members of one of our structure pieces.
    sanitizer_path = EVENTS / "StructureExplorationSanitizer.java"
    if not sanitizer_path.exists():
        errors.append("StructureExplorationSanitizer.java is missing; generated structures can expose free functional infrastructure.")
    else:
        sanitizer = read(sanitizer_path)
        required_policy_tokens = (
            "FUNCTIONAL_BLOCKS",
            "getStructureWithPieceAt",
            "story_cache",
            "ensureGuard",
            "android_spawner",
        )
        for token in required_policy_tokens:
            if token not in sanitizer:
                errors.append(f"Structure exploration policy missing token: {token}")

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
