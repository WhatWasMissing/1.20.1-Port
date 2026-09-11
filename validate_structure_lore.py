#!/usr/bin/env python3
"""Static validation for the Matter Overdrive structure-lore campaign."""
from pathlib import Path
import re, sys

ROOT = Path(__file__).resolve().parent
EVENT = ROOT / "src/main/java/matteroverdrive/event/StructureLoreEvents.java"
DATA = ROOT / "src/main/java/matteroverdrive/world/StructureLoreSavedData.java"
ADV = ROOT / "src/main/resources/data/matteroverdrive/advancements/campaign/reconstruct_overdrive_incident.json"

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


def read(path):
    if not path.exists():
        raise FileNotFoundError(path)
    return path.read_text(encoding="utf-8")


def main():
    errors = []
    event = read(EVENT)
    data = read(DATA)
    adv = read(ADV)

    for site in SITES:
        if f'r("{site}"' not in event:
            errors.append(f"missing structure lore record: {site}")
    for marker in NARRATIVE_MARKERS:
        if marker not in event:
            errors.append(f"missing narrative marker: {marker}")

    bits = [int(x) for x in re.findall(r'r\("[^"]+",\s*(\d+),', event)]
    if sorted(bits) != list(range(16)):
        errors.append(f"record bit allocation must be exactly 0..15, got {sorted(bits)}")

    for token in (
        'LoreArc", "OVERDRIVE_INCIDENT"', 'LoreProgress', 'LoreLink', 'LoreAuthor',
        'Closed Loop Artifact', 'OVERDRIVE_INCIDENT_COMPLETE', 'data.complete',
        'getStructureWithPieceAt', 'LAST_SCANNED_CHUNK',
    ):
        if token not in event:
            errors.append(f"lore event missing required token: {token}")

    for token in ("ALL_RECORDS_MASK = 0xFFFF", "discover(UUID player, int bit)", "complete(UUID player)", "setDirty()"):
        if token not in data:
            errors.append(f"saved data missing required token: {token}")

    for token in ("matteroverdrive:artifact", "OVERDRIVE_INCIDENT_COMPLETE", "challenge", "experience"):
        if token not in adv:
            errors.append(f"completion advancement missing token: {token}")

    print("Matter Overdrive structure lore validator")
    print("Checked 16 records, persistence, completion reward, and advancement wiring")
    for error in errors:
        print("FAIL:", error)
    if errors:
        print(f"RESULT: FAIL ({len(errors)} blocking issue(s))")
        return 1
    print("RESULT: PASS")
    return 0

if __name__ == "__main__":
    sys.exit(main())
