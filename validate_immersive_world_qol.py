#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parent
ERRORS: list[str] = []


def read(path: str) -> str:
    file = ROOT / path
    if not file.is_file():
        ERRORS.append(f"missing file: {path}")
        return ""
    return file.read_text(encoding="utf-8")


def require(blob: str, token: str, owner: str) -> None:
    if token not in blob:
        ERRORS.append(f"{owner}: missing {token!r}")


def forbid(blob: str, token: str, owner: str) -> None:
    if token in blob:
        ERRORS.append(f"{owner}: forbidden {token!r}")


reputation = read("src/main/java/matteroverdrive/dialogue/FactionReputation.java")
services = read("src/main/java/matteroverdrive/dialogue/ContactQuestServices.java")
researcher = read("src/main/java/matteroverdrive/entity/FacilityResearcherEntity.java")
defector = read("src/main/java/matteroverdrive/entity/DefectorAndroidEntity.java")
anomaly = read("src/main/java/matteroverdrive/event/AnomalyFieldEvents.java")
progression = read("src/main/java/matteroverdrive/event/ImmersiveProgressionAdvancements.java")
data_pad = read("src/main/java/matteroverdrive/item/DataPadItem.java")
voice_profile = read("src/main/java/matteroverdrive/pda/PdaVoiceProfile.java")
audio = read("src/main/java/matteroverdrive/client/PdaEmbeddedAudio.java")
processor = read("tools/pda_voicebank/process_voice_bank.py")
ambient = read("src/main/java/matteroverdrive/world/AmbientLoreCatalog.java")
ambient_loot_text = read("src/main/resources/data/matteroverdrive/loot_tables/chests/facilities/ambient_lore.json")
environment_catalog = read("src/main/java/matteroverdrive/world/EnvironmentalStorytellingCatalog.java")
environment_events = read("src/main/java/matteroverdrive/event/EnvironmentalStorytellingEvents.java")
voice = read("src/main/java/matteroverdrive/pda/PdaVoiceLineCatalog.java")
manifest_text = read("src/main/resources/assets/matteroverdrive/pda_voice/voice_bank_manifest.json")

for token in ["RECOVERY_NETWORK", "MORROW", "CHORUS", "HEPHAESTUS", "ARCHIVE_COUNCIL",
              "DISTRUSTED", "UNVERIFIED", "COOPERATIVE", "TRUSTED", "ALLIED",
              "DialogueStateSavedData", "fieldTrust", "syntheticTrust", "archiveInsight"]:
    require(reputation, token, "FactionReputation")

for token in ["MatterOverdriveContactNetwork", "stageRequirementMet", "FieldOperations.assignNext",
              "AmbientLoreSavedData", "StructureLoreSavedData", "TechnologyLoreSavedData",
              "COOPERATIVE", "Support package authorized"]:
    require(services, token, "ContactQuestServices")
for token in ["isShiftKeyDown", "ContactQuestServices.useHuman", "openBranchingDialogue"]:
    require(researcher, token, "FacilityResearcherEntity")
for token in ["isShiftKeyDown", "ContactQuestServices.useSynthetic", "openBranchingDialogue"]:
    require(defector, token, "DefectorAndroidEntity")

for token in ["SCAN_INTERVAL = 40", "getChunkNow", "GravitationalAnomalyBlockEntity", "Signatures",
              "EventsWitnessed", "spacetime_equalizer", "anomaly_field_observer"]:
    require(anomaly, token, "AnomalyFieldEvents")
for token in ["coalition_builder", "network_of_trust", "field_veteran", "ContactQuestServices.stage"]:
    require(progression, token, "ImmersiveProgressionAdvancements")
for token in ["sendFieldSummary", "ContactQuestServices.questStatus", "FactionReputation.status",
              "AnomalyFieldEvents.status", "HISTORY_CAPACITY = 32"]:
    require(data_pad, token, "DataPadItem")

profiles = ["STANDARD", "HAZARD", "ANOMALY", "ARCHIVE", "ORPHEUS", "SYNTHETIC", "CORRUPTED"]
for token in profiles:
    require(voice_profile, token, "PdaVoiceProfile")
for token in ["PdaVoiceProfile.forLine", 'safeId + "." + profile', "config", "resourceStream", "startOfflineTts"]:
    require(audio, token, "PdaEmbeddedAudio")
for token in ["profile_for", "PARAMS", '"hazard"', '"anomaly"', '"archive"', '"orpheus"',
              '"synthetic"', '"corrupted"', 'f"{source.stem}.{{profile}}.wav"']:
    require(processor, token, "process_voice_bank.py")

ambient_ids = re.findall(r'e\("([^"]+)",\s*"([^"]+)"', ambient)
if len(ambient_ids) != 48:
    ERRORS.append(f"AmbientLoreCatalog expected 48 records, found {len(ambient_ids)}")
site_counts: dict[str, int] = {}
for lore_id, site in ambient_ids:
    site_counts[site] = site_counts.get(site, 0) + 1
    if lore_id not in ambient_loot_text:
        ERRORS.append(f"ambient lore loot missing {lore_id}")
if len(site_counts) != 16:
    ERRORS.append(f"expected 16 lore sites, found {len(site_counts)}")
for site, count in sorted(site_counts.items()):
    if count != 3:
        ERRORS.append(f"{site}: expected 3 ambient records, found {count}")

for token in ["sand_pit", "deep_matter_vault", "matter_refinery", "cargo_ship",
              "synthetic_manufacturing_plant", "underwater_base", "quantum_relay_station", "crashed_ship",
              "mad_scientist_house", "anomaly_quarantine_site", "android_house", "android_command_bunker",
              "autonomous_drone_foundry", "fusion_research_complex", "black_site", "orbital_recovery_array"]:
    require(environment_catalog, f'"{token}"', "EnvironmentalStorytellingCatalog")
for token in ["SCAN_INTERVAL = 100", "REPEAT_DELAY", "getStructureWithPieceAt", "FIELD OBSERVATION"]:
    require(environment_events, token, "EnvironmentalStorytellingEvents")

voice_ids = re.findall(r'LINES\.put\("([^"]+)"', voice)
if len(voice_ids) != 80:
    ERRORS.append(f"expected exactly 80 static PDA voice lines, found {len(voice_ids)}")
try:
    manifest = json.loads(manifest_text)
    manifest_ids = [entry.get("id") for entry in manifest.get("lines", [])]
    if manifest_ids != voice_ids:
        ERRORS.append("voice manifest IDs/order do not exactly match Java static voice catalogue")
    if manifest.get("version") != 5:
        ERRORS.append(f"voice manifest expected version 5, got {manifest.get('version')}")
except Exception as exc:
    ERRORS.append(f"invalid voice manifest JSON: {exc}")

advancements = {
    "anomaly_field_observer.json": "matteroverdrive:campaign/anomaly_engineer",
    "coalition_builder.json": "matteroverdrive:campaign/incident_analyst",
    "network_of_trust.json": "matteroverdrive:campaign/coalition_builder",
    "field_veteran.json": "matteroverdrive:campaign/automation",
}
adv_dir = ROOT / "src/main/resources/data/matteroverdrive/advancements/campaign"
for name, parent in advancements.items():
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
        ERRORS.append(f"{name}: wrong parent {data.get('parent')}")
    triggers = [entry.get("trigger") for entry in data.get("criteria", {}).values() if isinstance(entry, dict)]
    if not triggers or any(trigger != "minecraft:impossible" for trigger in triggers):
        ERRORS.append(f"{name}: must be server-earned with minecraft:impossible")

for owner, blob in [
    ("FactionReputation", reputation), ("ContactQuestServices", services), ("AnomalyFieldEvents", anomaly),
    ("ImmersiveProgressionAdvancements", progression), ("EnvironmentalStorytellingEvents", environment_events),
    ("EnvironmentalStorytellingCatalog", environment_catalog),
]:
    lower = blob.lower()
    forbid(lower, "star_map", owner)
    forbid(blob, "setChunkForced", owner)
    forbid(blob, "addRegionTicket", owner)
    forbid(blob, "forceChunk", owner)

if ERRORS:
    print("IMMERSIVE WORLD / QOL VALIDATION FAILED")
    for error in ERRORS:
        print(" -", error)
    sys.exit(1)

print("IMMERSIVE WORLD / QOL VALIDATION PASSED")
print("  factions=5")
print("  reputation_tiers=5")
print("  contact_chains=5 x 3 stages")
print("  anomaly_field_signatures=4")
print("  new_state_advancements=4")
print("  optional_field_logs=48 across 16 sites")
print("  static_pda_voice_lines=80 plus dynamic technology lines")
print("  contextual_voice_profiles=7")
print("  environmental_story_sites=16")
print("  no Star Map / force-load tokens found in new runtime systems")
