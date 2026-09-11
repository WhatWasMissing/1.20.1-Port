#!/usr/bin/env python3
"""Static validation for the Matter Overdrive structure-lore campaign."""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parent
CATALOG = ROOT / "src/main/java/matteroverdrive/world/StructureLoreCatalog.java"
EVENT = ROOT / "src/main/java/matteroverdrive/event/StructureLoreEvents.java"
DATA = ROOT / "src/main/java/matteroverdrive/world/StructureLoreSavedData.java"
PDA = ROOT / "src/main/java/matteroverdrive/client/screen/DataPadScreen.java"
ADV = ROOT / "src/main/resources/data/matteroverdrive/advancements/campaign/reconstruct_overdrive_incident.json"
RARE = ROOT / "src/main/java/matteroverdrive/worldgen/RareArchiveMezzaninePiece.java"
MODERN_START = ROOT / "src/main/java/matteroverdrive/worldgen/TechnologyFacilityStructure.java"
FRONTIER_START = ROOT / "src/main/java/matteroverdrive/worldgen/FrontierSiteStructure.java"
REGISTRY = ROOT / "src/main/java/matteroverdrive/registry/ModStructures.java"

SITES = (
    "crashed_ship", "cargo_ship", "underwater_base", "mad_scientist_house", "android_house", "sand_pit",
    "synthetic_manufacturing_plant", "matter_refinery", "quantum_relay_station", "android_command_bunker",
    "fusion_research_complex", "black_site", "deep_matter_vault", "autonomous_drone_foundry",
    "anomaly_quarantine_site", "orbital_recovery_array",
)

NARRATIVE_MARKERS = (
    "Halcyon-7", "Atlas Freight 12", "NEREID", "Voss", "MORROW", "DUSTWELL",
    "HELIX", "KESTREL", "ECHO-9", "GLASS KNIFE", "ICARUS", "ORPHEUS",
    "MNEMOSYNE", "HEPHAESTUS", "JANUS", "LAGRANGE",
)

RECONSTRUCTIONS = (
    "matter_chain", "impossible_signal", "synthetic_schism", "project_overdrive", "closed_loop",
)


def read(path):
    if not path.exists():
        raise FileNotFoundError(path)
    return path.read_text(encoding="utf-8")


def main():
    errors = []
    catalog = read(CATALOG)
    event = read(EVENT)
    data = read(DATA)
    pda = read(PDA)
    adv = read(ADV)

    for site in SITES:
        if f'r("{site}"' not in catalog:
            errors.append(f"missing structure lore record: {site}")
    for marker in NARRATIVE_MARKERS:
        if marker not in catalog:
            errors.append(f"missing narrative marker: {marker}")

    records = re.findall(r'r\("([^"]+)",\s*(\d+),\s*(\d+),', catalog)
    if len(records) != 16:
        errors.append(f"expected exactly 16 primary lore records, got {len(records)}")
    bits = [int(bit) for _, bit, _ in records]
    archive = [int(index) for _, _, index in records]
    if sorted(bits) != list(range(16)):
        errors.append(f"record bit allocation must be exactly 0..15, got {sorted(bits)}")
    if sorted(archive) != list(range(1, 17)):
        errors.append(f"archive chronology must be exactly 1..16, got {sorted(archive)}")

    for reconstruction in RECONSTRUCTIONS:
        if f'reconstruction("{reconstruction}"' not in catalog:
            errors.append(f"missing incident reconstruction: {reconstruction}")
    reconstruction_count = len(re.findall(r'reconstruction\("[^"]+"', catalog))
    if reconstruction_count != 5:
        errors.append(f"expected exactly 5 incident reconstructions, got {reconstruction_count}")

    for token in (
        "String timestamp", "String classification", "String sitePurpose", "String excerpt",
        "String analysis", "String implication", "archiveRecords()", "byArchiveIndex(int archiveIndex)",
        "reconstructionUnlocked", "unlockedReconstructionCount", "DO NOT COMPLETE THE LOOP",
    ):
        if token not in catalog:
            errors.append(f"expanded lore catalog missing required token: {token}")

    # The final answer must remain gated by all sixteen records, not a subset.
    closed_loop = catalog[catalog.find('reconstruction("closed_loop"'):]
    if "ALL_RECORDS_MASK" not in closed_loop[:500]:
        errors.append("THE CLOSED LOOP reconstruction must require ALL_RECORDS_MASK")

    for token in (
        'LoreArc", "OVERDRIVE_INCIDENT"', "LoreTimestamp", "LoreClassification", "LoreSitePurpose",
        "LoreExcerpt", "LoreAnalysis", "LoreImplication", "Closed Loop Artifact",
        "OVERDRIVE_INCIDENT_COMPLETE", "announceNewReconstructions", "data.complete",
        "getStructureWithPieceAt", "LAST_SCANNED_CHUNK",
    ):
        if token not in event:
            errors.append(f"lore event missing required token: {token}")

    for token in (
        "discover(UUID player, int bit)", "mask(UUID player)", "complete(UUID player)", "setDirty()",
        "level.getServer().overworld()",
    ):
        if token not in data:
            errors.append(f"saved data missing required token: {token}")

    for token in (
        'INCIDENT("INCIDENT")', "ARCHIVE_SECTIONS = 3", "renderIncident", "byArchiveIndex",
        "RECOVERED EXCERPT", "PDA FORENSIC ANALYSIS", "INCIDENT IMPLICATION",
        "REQUIRED SOURCE CHAIN", "DO NOT COMPLETE THE LOOP",
    ):
        if token not in pda:
            errors.append(f"PDA missing expanded lore presentation token: {token}")

    for token in ("matteroverdrive:artifact", "OVERDRIVE_INCIDENT_COMPLETE", "challenge", "experience"):
        if token not in adv:
            errors.append(f"completion advancement missing token: {token}")

    # Rare deterministic layout must add an optional, traversable archive room without
    # replacing the normal main route.
    if not RARE.exists():
        errors.append("RareArchiveMezzaninePiece.java is missing")
    else:
        rare = read(RARE)
        for token in ("RARE_ARCHIVE_MEZZANINE_PIECE", "story_cache", "android_spawner", "LECTERN", "clip.isInside"):
            if token not in rare:
                errors.append(f"rare archive piece missing token: {token}")
        for bad in ("getChunk(", "setChunkForced", "addRegionTicket", "TicketType"):
            if bad in rare:
                errors.append(f"rare archive piece contains forbidden force-loading token: {bad}")

    modern_start = read(MODERN_START)
    frontier_start = read(FRONTIER_START)
    registry = read(REGISTRY)
    for name, src in (("modern", modern_start), ("frontier", frontier_start)):
        if "if(layout==2)" not in src or "new RareArchiveMezzaninePiece" not in src:
            errors.append(f"{name} rare layout is not wired to RareArchiveMezzaninePiece")
    for token in ("RARE_ARCHIVE_MEZZANINE_PIECE", "RareArchiveMezzaninePiece::new"):
        if token not in registry:
            errors.append(f"structure registry missing rare archive token: {token}")

    print("Matter Overdrive structure lore validator")
    print("Checked 16 records, chronology, 5 reconstructions, PDA presentation, persistence, rare archives, reward and advancement wiring")
    for error in errors:
        print("FAIL:", error)
    if errors:
        print(f"RESULT: FAIL ({len(errors)} blocking issue(s))")
        return 1
    print("RESULT: PASS")
    return 0


if __name__ == "__main__":
    sys.exit(main())
