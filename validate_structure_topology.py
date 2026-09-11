#!/usr/bin/env python3
"""Static topology validator for Matter Overdrive generated structures.

This intentionally validates the authored topology contract, not Minecraft runtime
collision. It catches the class of regressions we have repeatedly hit: stale helper
pieces, disconnected critical rooms, missing surface approaches, inconsistent
vertical transitions, and accidental removal of the objective/return route.

Run from repository root:
    python validate_structure_topology.py

Exit code is non-zero on any blocking failure.
"""
from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path
import re
import sys
from typing import Dict, List, Set, Tuple

ROOT = Path(__file__).resolve().parent
WORLDGEN = ROOT / "src/main/java/matteroverdrive/worldgen"

@dataclass(frozen=True)
class Site:
    name: str
    source: str
    entrance: str
    objective: str
    required: Tuple[str, ...]
    optional: Tuple[str, ...] = ()
    surface_access: bool = True
    notes: str = ""

SITES: Tuple[Site, ...] = (
    Site("crashed_ship", "LegacyNativeStructurePiece.java", "south hull breach", "engineering tail", ("central route", "cockpit/service", "engineering")),
    Site("cargo_ship", "LegacyNativeStructurePiece.java", "loading/boarding access", "bridge or engineering", ("boarding", "circulation spine", "cargo bays", "engineering")),
    Site("underwater_base", "LegacyNativeStructurePiece.java", "south airlock", "central hub", ("airlock", "central hub", "pressure tubes")),
    Site("mad_scientist_house", "LegacyNativeStructurePiece.java", "surface residence", "hidden laboratory", ("surface room", "continuous descent", "laboratory")),
    Site("android_house", "LegacyNativeStructurePiece.java", "front approach", "rear secure room", ("atrium", "west/east wings", "rear secure room")),
    Site("sand_pit", "LegacyNativeStructurePiece.java", "surface excavation", "buried relic floor", ("surface edge", "continuous ramp", "relic floor")),
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

BLOCKING_STALE_HELPERS = {
    "TechnologyFacilityStructure.java": (
        "FacilityInfrastructurePiece.assemble(builder, kind, origin, layout)",
        "FacilityTerrainPiece.assemble(builder, kind, origin, layout)",
    ),
}

# Helper kinds which historically existed solely to repair old layouts. They must not
# silently survive a redesign without an explicit review marker in the helper source.
LEGACY_FIX_TOKENS = (
    "PLANT_EAST_ENTRANCE_FIX",
    "BUNKER_ANDROID_LINK_Z",
    "BLACK_LOWER_CORRIDOR_X",
    "LADDER_DOWN_7",
    "LADDER_DOWN_12",
    "BLACK_VAULT_STAIR",
)

# Known vanilla-standard hazards. The validator verifies these signatures are absent
# after the repair pass, so they cannot regress unnoticed.
FORBIDDEN_PATTERNS = {
    "LegacyNativeStructure.java": (
        (r"case CARGO_SHIP -> Math\.max\(86, surfaceY \+ 24\)", "Cargo Ship is elevated without guaranteed vanilla-style boarding."),
    ),
    "LegacyNativeStructurePiece.java": (
        (r"c\.offset\(-7, 0, 3\), true", "Android Safehouse west connector uses the old off-by-one wall coordinate."),
        (r"c\.offset\(7, 0, 3\), true", "Android Safehouse east connector uses the old off-by-one wall coordinate."),
    ),
    "FrontierSitePiece.java": (),
}


def text(name: str) -> str:
    p = WORLDGEN / name
    if not p.exists():
        raise FileNotFoundError(p)
    return p.read_text(encoding="utf-8")


def main() -> int:
    errors: List[str] = []
    warnings: List[str] = []
    cache: Dict[str, str] = {}

    def src(name: str) -> str:
        if name not in cache:
            cache[name] = text(name)
        return cache[name]

    # 1) Every structure family must still exist in the authored source and retain
    # its required semantic waypoints. This catches accidental room deletion/renames.
    for site in SITES:
        s = src(site.source)
        missing = [token for token in site.required if token not in s]
        if missing:
            errors.append(f"{site.name}: missing required topology tokens: {', '.join(missing)}")
        if site.entrance not in s:
            errors.append(f"{site.name}: entrance token missing: {site.entrance}")
        if site.objective not in s:
            errors.append(f"{site.name}: objective token missing: {site.objective}")

    # 2) Explicit regression signatures discovered by static audits.
    for filename, patterns in FORBIDDEN_PATTERNS.items():
        s = src(filename)
        for pattern, message in patterns:
            if re.search(pattern, s):
                errors.append(message)

    # 3) Infrastructure helpers from pre-redesign layouts require an explicit review
    # marker. We allow the helper classes to exist, but stale fix-only tokens are a
    # blocking failure until they are removed or annotated as reviewed.
    infra = src("FacilityInfrastructurePiece.java")
    if "VANILLA_STANDARD_REVIEWED" not in infra:
        stale = [token for token in LEGACY_FIX_TOKENS if token in infra]
        if stale:
            errors.append("Modern facilities still contain pre-redesign helper fixes without VANILLA_STANDARD_REVIEWED marker: " + ", ".join(stale))

    # 4) Structure assembly must remain native/chunk-safe.
    for filename in ("LegacyNativeStructurePiece.java", "TechnologyFacilityStructurePiece.java", "FrontierSitePiece.java"):
        s = src(filename)
        if "clip.isInside" not in s:
            errors.append(f"{filename}: no active chunk clip guard found")
        if "setBlock" not in s:
            warnings.append(f"{filename}: no direct block placement found; validator assumptions may be stale")

    # 5) Prevent accidental reintroduction of synchronous force-loading APIs in the
    # three structure families.
    force_tokens = ("getChunk(", "setChunkForced", "getChunkSource().addRegionTicket", "TicketType")
    for filename in ("LegacyNativeStructurePiece.java", "TechnologyFacilityStructurePiece.java", "FrontierSitePiece.java"):
        s = src(filename)
        for token in force_tokens:
            if token in s:
                errors.append(f"{filename}: forbidden force-load/synchronous chunk token present: {token}")

    print("Matter Overdrive structure topology validator")
    print(f"Checked {len(SITES)} structure families")
    for w in warnings:
        print(f"WARN: {w}")
    for e in errors:
        print(f"FAIL: {e}")
    if errors:
        print(f"RESULT: FAIL ({len(errors)} blocking issue(s))")
        return 1
    print("RESULT: PASS")
    return 0

if __name__ == "__main__":
    sys.exit(main())
