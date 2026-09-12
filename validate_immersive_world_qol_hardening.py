#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parent
errors: list[str] = []


def read(path: str) -> str:
    p = ROOT / path
    if not p.is_file():
        errors.append(f"missing file: {path}")
        return ""
    return p.read_text(encoding="utf-8")


def require(blob: str, owner: str, *tokens: str) -> None:
    for token in tokens:
        if token not in blob:
            errors.append(f"{owner}: missing {token!r}")


anomaly = read("src/main/java/matteroverdrive/event/AnomalyFieldEvents.java")
voice = read("src/main/java/matteroverdrive/pda/PdaVoiceLineCatalog.java")
profiles = read("src/main/java/matteroverdrive/pda/PdaVoiceProfile.java")
audio = read("src/main/java/matteroverdrive/client/PdaEmbeddedAudio.java")
processor = read("tools/pda_voicebank/process_voice_bank.py")
ambient = read("src/main/java/matteroverdrive/world/AmbientLoreCatalog.java")
dressing = read("src/main/java/matteroverdrive/worldgen/EnvironmentalDressingPiece.java")
structures = read("src/main/java/matteroverdrive/registry/ModStructures.java")
services = read("src/main/java/matteroverdrive/dialogue/ContactQuestServices.java")
reputation = read("src/main/java/matteroverdrive/dialogue/FactionReputation.java")
data_pad = read("src/main/java/matteroverdrive/item/DataPadItem.java")
progression = read("src/main/java/matteroverdrive/event/ImmersiveProgressionAdvancements.java")

# Every anomaly phenomenon must route through a voice ID that exists in the authored catalogue.
voice_ids = set(re.findall(r'LINES\.put\("([^"]+)"', voice))
anomaly_voice_ids = re.findall(r'sendPdaVoice\(player,\s*"([^"]+)"\)', anomaly)
if len(anomaly_voice_ids) != 4:
    errors.append(f"AnomalyFieldEvents: expected 4 PDA voice sends, found {len(anomaly_voice_ids)}")
for line_id in anomaly_voice_ids:
    if line_id not in voice_ids:
        errors.append(f"AnomalyFieldEvents: unregistered PDA voice ID {line_id}")

require(anomaly, "AnomalyFieldEvents", "getChunkNow", "spacetime_equalizer", "ALL_SIGNATURES = 0b1111",
        "anomaly_field_observer", "NextEvent", "EventsWitnessed")
for forbidden in ("setChunkForced", "addRegionTicket", "forceChunk"):
    if forbidden in anomaly:
        errors.append(f"AnomalyFieldEvents: force-load token found: {forbidden}")

# Context-specific prerecorded audio contract.
for profile in ("STANDARD", "HAZARD", "ANOMALY", "ARCHIVE", "ORPHEUS", "SYNTHETIC", "CORRUPTED"):
    require(profiles, "PdaVoiceProfile", profile)
require(audio, "PdaEmbeddedAudio", "PdaVoiceProfile.forLine", 'safeId + "." + profile + ".wav"',
        "config", "pda_voice", "startOfflineTts")
for name in ("standard", "hazard", "anomaly", "archive", "orpheus", "synthetic", "corrupted"):
    require(processor, "process_voice_bank.py", f'"{name}"')

# Lore depth remains exactly three optional physical records per canonical facility.
records = re.findall(r'e\("([^"]+)",\s*"([^"]+)"', ambient)
if len(records) != 48:
    errors.append(f"AmbientLoreCatalog: expected 48 entries, found {len(records)}")
counts: dict[str, int] = {}
for record_id, site_id in records:
    counts[site_id] = counts.get(site_id, 0) + 1
if len(counts) != 16:
    errors.append(f"AmbientLoreCatalog: expected 16 sites, found {len(counts)}")
for site_id, count in sorted(counts.items()):
    if count != 3:
        errors.append(f"AmbientLoreCatalog: {site_id} has {count} entries, expected 3")

# Faction/contact systems must remain derived from authoritative dialogue state.
require(reputation, "FactionReputation", "DialogueStateSavedData", "RECOVERY_NETWORK", "MORROW", "CHORUS",
        "HEPHAESTUS", "ARCHIVE_COUNCIL", "COOPERATIVE", "TRUSTED", "ALLIED")
require(services, "ContactQuestServices", "MatterOverdriveContactNetwork", "FieldOperations.assignNext",
        "stageRequirementMet", "Support package authorized", "COOLDOWN_KEY")
require(data_pad, "DataPadItem", "sendFieldSummary", "FactionReputation.status", "ContactQuestServices.questStatus",
        "AnomalyFieldEvents.status", "HISTORY_CAPACITY = 32")

# Five state-driven mastery advancements, all server-awarded impossible criteria.
advancements = {
    "anomaly_field_observer.json": "matteroverdrive:campaign/anomaly_engineer",
    "coalition_builder.json": "matteroverdrive:campaign/incident_analyst",
    "network_of_trust.json": "matteroverdrive:campaign/coalition_builder",
    "field_veteran.json": "matteroverdrive:campaign/automation",
    "field_historian.json": "matteroverdrive:campaign/field_archivist",
}
adv_dir = ROOT / "src/main/resources/data/matteroverdrive/advancements/campaign"
for filename, parent in advancements.items():
    path = adv_dir / filename
    if not path.is_file():
        errors.append(f"missing advancement: {filename}")
        continue
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"invalid advancement JSON {filename}: {exc}")
        continue
    if data.get("parent") != parent:
        errors.append(f"{filename}: parent {data.get('parent')!r}, expected {parent!r}")
    criteria = data.get("criteria", {})
    if not criteria or any(v.get("trigger") != "minecraft:impossible" for v in criteria.values() if isinstance(v, dict)):
        errors.append(f"{filename}: mastery advancement must use only minecraft:impossible criteria")
for token in ("coalition_builder", "network_of_trust", "field_veteran", "field_historian"):
    require(progression, "ImmersiveProgressionAdvancements", token)
require(anomaly, "AnomalyFieldEvents", "anomaly_field_observer")

# Dressing must stay decorative, chunk-clipped and serializer-backed.
require(structures, "ModStructures", "ENVIRONMENTAL_DRESSING_PIECE", 'register("environmental_dressing"')
require(dressing, "EnvironmentalDressingPiece", "extends StructurePiece", "BoundingBox clip", "clip.isInside(pos)",
        "getBoundingBox().isInside(pos)", "level.getBlockState(pos).isAir()", "support.isAir()")
for forbidden in ("ChestBlock", "SpawnerBlock", "LootTable", "setChunkForced", "addRegionTicket", "forceChunk"):
    if forbidden in dressing:
        errors.append(f"EnvironmentalDressingPiece: forbidden objective/force-load token {forbidden}")

# The retired Star Map may not leak into this new feature layer.
for owner, blob in {
    "anomaly": anomaly, "profiles": profiles, "services": services, "reputation": reputation,
    "dressing": dressing, "progression": progression, "data_pad": data_pad,
}.items():
    if "star_map" in blob.lower():
        errors.append(f"{owner}: retired Star Map token found")

if errors:
    print("IMMERSIVE WORLD / QOL HARDENING FAILED")
    for error in errors:
        print(" -", error)
    sys.exit(1)

print("IMMERSIVE WORLD / QOL HARDENING PASSED")
print("  anomaly phenomena=4 with authored PDA routing")
print("  voice profiles=7 with config/bundled/TTS fallback path")
print("  optional physical lore=48 across 16 facilities")
print("  faction networks=5; contact chains=5; Data Pad summary wired")
print("  state-driven mastery advancements=5")
print("  environmental dressing remains chunk-clipped and non-objective")
