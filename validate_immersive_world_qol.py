#!/usr/bin/env python3
from __future__ import annotations

import json
from collections import Counter
from pathlib import Path
import re
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


def safe(owner: str, blob: str) -> None:
    if "star_map" in blob.lower():
        ERRORS.append(f"{owner}: retired Star Map token found")
    for token in ("setChunkForced", "addRegionTicket", "forceChunk"):
        if token in blob:
            ERRORS.append(f"{owner}: force-load token found: {token}")


reputation = load("src/main/java/matteroverdrive/dialogue/FactionReputation.java")
services = load("src/main/java/matteroverdrive/dialogue/ContactQuestServices.java")
researcher = load("src/main/java/matteroverdrive/entity/FacilityResearcherEntity.java")
defector = load("src/main/java/matteroverdrive/entity/DefectorAndroidEntity.java")
anomaly = load("src/main/java/matteroverdrive/event/AnomalyFieldEvents.java")
progression = load("src/main/java/matteroverdrive/event/ImmersiveProgressionAdvancements.java")
data_pad = load("src/main/java/matteroverdrive/item/DataPadItem.java")
voice_profile = load("src/main/java/matteroverdrive/pda/PdaVoiceProfile.java")
audio = load("src/main/java/matteroverdrive/client/PdaEmbeddedAudio.java")
processor = load("tools/pda_voicebank/process_voice_bank.py")
ambient = load("src/main/java/matteroverdrive/world/AmbientLoreCatalog.java")
ambient_loot = load("src/main/resources/data/matteroverdrive/loot_tables/chests/facilities/ambient_lore.json")
environment_catalog = load("src/main/java/matteroverdrive/world/EnvironmentalStorytellingCatalog.java")
environment_events = load("src/main/java/matteroverdrive/event/EnvironmentalStorytellingEvents.java")
voice = load("src/main/java/matteroverdrive/pda/PdaVoiceLineCatalog.java")
manifest_text = load("src/main/resources/assets/matteroverdrive/pda_voice/voice_bank_manifest.json")

need(reputation, "FactionReputation", "RECOVERY_NETWORK", "MORROW", "CHORUS", "HEPHAESTUS", "ARCHIVE_COUNCIL",
     "DISTRUSTED", "UNVERIFIED", "COOPERATIVE", "TRUSTED", "ALLIED", "DialogueStateSavedData")
need(services, "ContactQuestServices", "MatterOverdriveContactNetwork", "stageRequirementMet",
     "FieldOperations.assignNext", "AmbientLoreSavedData", "StructureLoreSavedData", "TechnologyLoreSavedData",
     "Support package authorized")
need(researcher, "FacilityResearcherEntity", "isShiftKeyDown", "ContactQuestServices.useHuman", "openBranchingDialogue")
need(defector, "DefectorAndroidEntity", "isShiftKeyDown", "ContactQuestServices.useSynthetic", "openBranchingDialogue")
need(anomaly, "AnomalyFieldEvents", "SCAN_INTERVAL = 40", "getChunkNow", "GravitationalAnomalyBlockEntity",
     "Signatures", "EventsWitnessed", "spacetime_equalizer", "anomaly_field_observer")
need(progression, "ImmersiveProgressionAdvancements", "coalition_builder", "network_of_trust", "field_veteran")
need(data_pad, "DataPadItem", "sendFieldSummary", "ContactQuestServices.questStatus", "FactionReputation.status",
     "AnomalyFieldEvents.status", "HISTORY_CAPACITY = 32")

for profile in ("STANDARD", "HAZARD", "ANOMALY", "ARCHIVE", "ORPHEUS", "SYNTHETIC", "CORRUPTED"):
    need(voice_profile, "PdaVoiceProfile", profile)
need(audio, "PdaEmbeddedAudio", "PdaVoiceProfile.forLine", 'safeId + "." + profile', "resourceStream", "startOfflineTts")
need(processor, "process_voice_bank.py", "profile_for", "PARAMS", '"hazard"', '"anomaly"', '"archive"',
     '"orpheus"', '"synthetic"', '"corrupted"', 'f"{source.stem}.{profile}.wav"')

ambient_pairs = re.findall(r'e\("([^"]+)",\s*"([^"]+)"', ambient)
if len(ambient_pairs) != 48:
    ERRORS.append(f"AmbientLoreCatalog expected 48 records, found {len(ambient_pairs)}")
if len({record for record, _ in ambient_pairs}) != len(ambient_pairs):
    ERRORS.append("AmbientLoreCatalog contains duplicate record IDs")
site_counts = Counter(site for _, site in ambient_pairs)
if len(site_counts) != 16:
    ERRORS.append(f"expected 16 ambient-lore sites, found {len(site_counts)}")
for site, count in sorted(site_counts.items()):
    if count != 3:
        ERRORS.append(f"{site}: expected 3 ambient records, found {count}")
for record, _ in ambient_pairs:
    if record not in ambient_loot:
        ERRORS.append(f"ambient lore loot missing {record}")

canonical_sites = (
    "sand_pit", "deep_matter_vault", "matter_refinery", "cargo_ship", "synthetic_manufacturing_plant",
    "underwater_base", "quantum_relay_station", "crashed_ship", "mad_scientist_house",
    "anomaly_quarantine_site", "android_house", "android_command_bunker", "autonomous_drone_foundry",
    "fusion_research_complex", "black_site", "orbital_recovery_array",
)
for site in canonical_sites:
    need(environment_catalog, "EnvironmentalStorytellingCatalog", f'"{site}"')
need(environment_events, "EnvironmentalStorytellingEvents", "SCAN_INTERVAL = 100", "REPEAT_DELAY",
     "getStructureWithPieceAt", "FIELD OBSERVATION")

voice_ids = re.findall(r'LINES\.put\("([^"]+)"', voice)
if len(voice_ids) != 80:
    ERRORS.append(f"expected exactly 80 static PDA voice lines, found {len(voice_ids)}")
if len(voice_ids) != len(set(voice_ids)):
    ERRORS.append("static PDA voice catalogue contains duplicate IDs")
try:
    manifest = json.loads(manifest_text)
    manifest_ids = [entry.get("id") for entry in manifest.get("lines", [])]
    if manifest_ids != voice_ids:
        ERRORS.append("voice manifest IDs/order do not exactly match Java static voice catalogue")
    if manifest.get("version") != 5:
        ERRORS.append(f"voice manifest expected version 5, got {manifest.get('version')}")
except Exception as exc:
    ERRORS.append(f"invalid voice manifest JSON: {exc}")

adv_dir = ROOT / "src/main/resources/data/matteroverdrive/advancements/campaign"
for name, parent in {
    "anomaly_field_observer.json": "matteroverdrive:campaign/anomaly_engineer",
    "coalition_builder.json": "matteroverdrive:campaign/incident_analyst",
    "network_of_trust.json": "matteroverdrive:campaign/coalition_builder",
    "field_veteran.json": "matteroverdrive:campaign/automation",
}.items():
    path = adv_dir / name
    if not path.is_file():
        ERRORS.append(f"missing advancement {name}")
        continue
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        ERRORS.append(f"invalid advancement JSON {name}: {exc}")
        continue
    if data.get("parent") != parent:
        ERRORS.append(f"{name}: expected parent {parent}, got {data.get('parent')}")
    triggers = [entry.get("trigger") for entry in data.get("criteria", {}).values() if isinstance(entry, dict)]
    if not triggers or any(trigger != "minecraft:impossible" for trigger in triggers):
        ERRORS.append(f"{name}: must be server-earned with minecraft:impossible")

for owner, blob in (
    ("FactionReputation", reputation), ("ContactQuestServices", services), ("AnomalyFieldEvents", anomaly),
    ("ImmersiveProgressionAdvancements", progression), ("EnvironmentalStorytellingEvents", environment_events),
    ("EnvironmentalStorytellingCatalog", environment_catalog),
):
    safe(owner, blob)

if ERRORS:
    print("IMMERSIVE WORLD / QOL VALIDATION FAILED")
    for error in ERRORS:
        print(" -", error)
    sys.exit(1)

print("IMMERSIVE WORLD / QOL VALIDATION PASSED")
print("  factions=5; reputation_tiers=5; contact_chains=5x3")
print("  anomaly_field_signatures=4; new_state_advancements=4")
print("  optional_field_logs=48 across 16 sites")
print("  static_pda_voice_lines=80 plus dynamic technology lines")
print("  contextual_voice_profiles=7; environmental_story_sites=16")
print("  no Star Map / force-load tokens found in new runtime systems")
