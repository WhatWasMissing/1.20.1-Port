#!/usr/bin/env python3
from __future__ import annotations

from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
ERRORS: list[str] = []


def load(path: str) -> str:
    p = ROOT / path
    if not p.is_file():
        ERRORS.append(f"missing file: {path}")
        return ""
    return p.read_text(encoding="utf-8")


def need(blob: str, owner: str, *tokens: str) -> None:
    for token in tokens:
        if token not in blob:
            ERRORS.append(f"{owner}: missing {token!r}")


def forbid(blob: str, owner: str, *tokens: str) -> None:
    for token in tokens:
        if token in blob:
            ERRORS.append(f"{owner}: forbidden token {token!r}")


dressing = load("src/main/java/matteroverdrive/worldgen/EnvironmentalDressingPiece.java")
registry = load("src/main/java/matteroverdrive/registry/ModStructures.java")
legacy = load("src/main/java/matteroverdrive/worldgen/LegacyNativeStructure.java")
modern = load("src/main/java/matteroverdrive/worldgen/TechnologyFacilityStructure.java")
frontier = load("src/main/java/matteroverdrive/worldgen/FrontierSiteStructure.java")

need(dressing, "EnvironmentalDressingPiece",
     "extends StructurePiece", "ENVIRONMENTAL_DRESSING_PIECE", "addAdditionalSaveData",
     "clip.isInside(pos)", "getBoundingBox().isInside(pos)", "level.getBlockState(pos).isAir()",
     "support.isAir()", "support.getFluidState().isEmpty()", "Math.abs(x) <= 1 || Math.abs(z) <= 1")
need(registry, "ModStructures", "ENVIRONMENTAL_DRESSING_PIECE", '"environmental_dressing"')
need(legacy, "LegacyNativeStructure", "new EnvironmentalDressingPiece(")
need(modern, "TechnologyFacilityStructure", "new EnvironmentalDressingPiece(")
need(frontier, "FrontierSiteStructure", "new EnvironmentalDressingPiece(")

for site in (
    "sand_pit", "deep_matter_vault", "matter_refinery", "cargo_ship",
    "synthetic_manufacturing_plant", "underwater_base", "quantum_relay_station",
    "crashed_ship", "mad_scientist_house", "anomaly_quarantine_site", "android_house",
    "android_command_bunker", "autonomous_drone_foundry", "fusion_research_complex",
    "black_site", "orbital_recovery_array",
):
    need(dressing, "EnvironmentalDressingPiece", f'case "{site}"')

# Dressing is visual evidence only. It must never grow into a second structure/objective system.
forbid(dressing, "EnvironmentalDressingPiece",
       "setChunkForced", "addRegionTicket", "forceChunk", "getChunk(",
       "android_spawner", "tritanium_crate", "LootTable", "seedStructureLoot",
       "transporter", "fusion_reactor_controller", "anomaly_containment_unit",
       "destroyBlock", "spawn", "EntityType", "StructureStart")

# Verify the six modern facilities bias all three local environmental records.
modern_loot = {
    "synthetic_manufacturing_plant.json": "helix_tool_shadow",
    "matter_refinery.json": "kestrel_valve_tag",
    "quantum_relay_station.json": "echo9_clock_stickers",
    "android_command_bunker.json": "bastion_tape_arrow",
    "fusion_research_complex.json": "icarus_scram_paint",
    "black_site.json": "orpheus_bin_labels",
}
for filename, record in modern_loot.items():
    blob = load(f"src/main/resources/data/matteroverdrive/loot_tables/chests/facilities/{filename}")
    need(blob, filename, record)

# Fresh-world serializers remain registered; old serializers are not removed.
need(registry, "ModStructures",
     "LEGACY_NATIVE_PIECE", "LEGACY_VANILLA_PIECE", "TECHNOLOGY_FACILITY_PIECE",
     "FRONTIER_SITE_PIECE", "MODERN_EXPLORATION_PIECE", "FRONTIER_EXPLORATION_PIECE")

if ERRORS:
    print("ENVIRONMENTAL DRESSING VALIDATION FAILED")
    for error in ERRORS:
        print(" -", error)
    sys.exit(1)

print("ENVIRONMENTAL DRESSING VALIDATION PASSED")
print("  canonical_sites=16; structure_families=3")
print("  placement=open-cell + supported + chunk-clipped + off central route")
print("  objectives/machines/loot/entity spawning/force-loading absent from dressing piece")
print("  modern local caches include the new third facility-specific record")
