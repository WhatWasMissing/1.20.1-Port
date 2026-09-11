#!/usr/bin/env python3
"""Static gate for PDA narration, narrative populations, enemies, and Android UV repair."""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parent
PDA = ROOT / "src/main/java/matteroverdrive/client/screen/DataPadScreen.java"
NARRATION = ROOT / "src/main/java/matteroverdrive/client/PdaNarrationController.java"
CLIENT = ROOT / "src/main/java/matteroverdrive/client/LegacyEntityClientEvents.java"
RENDERERS = ROOT / "src/main/java/matteroverdrive/client/LegacyEntityRenderers.java"
ENTITIES = ROOT / "src/main/java/matteroverdrive/registry/ModEntities.java"
ATTRS = ROOT / "src/main/java/matteroverdrive/event/LegacyEntityEvents.java"
POP = ROOT / "src/main/java/matteroverdrive/event/StructurePopulationEvents.java"
POP_DATA = ROOT / "src/main/java/matteroverdrive/world/StructurePopulationSavedData.java"

SITES = (
    "crashed_ship", "cargo_ship", "underwater_base", "mad_scientist_house", "android_house", "sand_pit",
    "synthetic_manufacturing_plant", "matter_refinery", "quantum_relay_station", "android_command_bunker",
    "fusion_research_complex", "black_site", "deep_matter_vault", "autonomous_drone_foundry",
    "anomaly_quarantine_site", "orbital_recovery_array",
)


def read(path: Path) -> str:
    if not path.exists():
        raise FileNotFoundError(path)
    return path.read_text(encoding="utf-8")


def main() -> int:
    errors = []
    pda = read(PDA)
    narration = read(NARRATION)
    client = read(CLIENT)
    renderers = read(RENDERERS)
    entities = read(ENTITIES)
    attrs = read(ATTRS)
    population = read(POP)
    population_data = read(POP_DATA)

    for token in ("READ ALOUD", "STOP", "currentLoreNarration", "PdaNarrationController.read", "PdaNarrationController.stop"):
        if token not in pda:
            errors.append(f"PDA narration wiring missing token: {token}")
    for token in ("StructureLoreCatalog.recovered", "reconstructionUnlocked", 'return ""'):
        if token not in pda:
            errors.append(f"locked-lore narration guard missing token: {token}")
    for token in ("getNarrator().sayNow", "getNarrator().clear", "getNarrator().isActive", "Accessibility settings"):
        if token not in narration:
            errors.append(f"narration controller missing token: {token}")

    # The original textures are 64x32 (normal) and 96x64 (ranged). The repair is
    # model/UV-side; do not 'fix' the PNGs by forcing both onto a generic zombie atlas.
    for token in ("ROGUE_ANDROID_LAYER", "RANGED_ANDROID_LAYER", "64, 32", "96, 64"):
        if token not in client:
            errors.append(f"Android model-layer repair missing token: {token}")
    if "ModelLayers.ZOMBIE" in renderers and ("RogueAndroidRenderer" in renderers or "RangedRogueAndroidRenderer" in renderers):
        errors.append("Android renderers regressed to Minecraft's generic zombie layer")
    for token in ("android.png", "android_ranged.png", "android_colorless.png", "android_holo.png"):
        if token not in renderers:
            errors.append(f"Android renderer missing legacy texture: {token}")

    for entity in ("DEFECTOR_ANDROID", "ORPHEUS_SECURITY", "RESONANT_ANDROID", "FACILITY_RESEARCHER"):
        if entity not in entities:
            errors.append(f"entity registry missing {entity}")
        if entity not in attrs:
            errors.append(f"attribute registration missing {entity}")
        if entity not in client:
            errors.append(f"renderer registration missing {entity}")

    for role in ("FIELD_RESEARCHER", "SALVAGER", "RECOVERY_SPECIALIST", "ICARUS_ENGINEER", "JANUS_MEDIC", "ARCHIVIST",
                 "MORROW_SCOUT", "CHORUS_COURIER", "HEPHAESTUS_LIAISON"):
        if role not in population:
            errors.append(f"structure population missing role: {role}")
    for site in SITES:
        if f'case "{site}"' not in population:
            errors.append(f"structure population missing site package: {site}")

    for token in ("MAX_POSITION_CHECKS", "level.isLoaded", "insideAnyPiece", "LAST_SCANNED_CELL", "StructurePopulationSavedData"):
        if token not in population:
            errors.append(f"bounded population logic missing token: {token}")
    for token in ("matteroverdrive_structure_population", "MAX_KEYS", "setDirty", "Populated"):
        if token not in population_data:
            errors.append(f"population persistence missing token: {token}")

    for bad in ("getChunk(", "setChunkForced", "addRegionTicket", "TicketType"):
        if bad in population:
            errors.append(f"population system contains forbidden force-loading token: {bad}")

    print("Matter Overdrive PDA / population / Android validator")
    print("Checked narration gating, legacy Android atlas layers, 4 new entity types, 9 NPC roles, 16 site packages, and bounded persistence.")
    for error in errors:
        print("FAIL:", error)
    if errors:
        print(f"RESULT: FAIL ({len(errors)} blocking issue(s))")
        return 1
    print("RESULT: PASS")
    return 0

if __name__ == "__main__":
    sys.exit(main())
