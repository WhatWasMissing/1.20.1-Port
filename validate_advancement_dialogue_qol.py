#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parent
ERRORS: list[str] = []


def text(path: str) -> str:
    p = ROOT / path
    if not p.is_file():
        ERRORS.append(f"missing file: {path}")
        return ""
    return p.read_text(encoding="utf-8")


def require(blob: str, token: str, owner: str) -> None:
    if token not in blob:
        ERRORS.append(f"{owner}: missing {token!r}")


def forbid(blob: str, token: str, owner: str) -> None:
    if token in blob:
        ERRORS.append(f"{owner}: forbidden {token!r}")


network = text("src/main/java/matteroverdrive/network/ModNetwork.java")
packet = text("src/main/java/matteroverdrive/network/NpcDialoguePacket.java")
choice_packet = text("src/main/java/matteroverdrive/network/DialogueChoicePacket.java")
screen = text("src/main/java/matteroverdrive/client/screen/NpcDialogueScreen.java")
pda_screen = text("src/main/java/matteroverdrive/client/screen/DataPadScreen.java")
catalog = text("src/main/java/matteroverdrive/dialogue/DialogueCatalog.java")
state = text("src/main/java/matteroverdrive/dialogue/DialogueStateSavedData.java")
sessions = text("src/main/java/matteroverdrive/dialogue/DialogueSessionManager.java")
awards = text("src/main/java/matteroverdrive/dialogue/DialogueAdvancementEvents.java")
researcher = text("src/main/java/matteroverdrive/entity/FacilityResearcherEntity.java")
defector = text("src/main/java/matteroverdrive/entity/DefectorAndroidEntity.java")
data_packet = text("src/main/java/matteroverdrive/network/DataPadOpenPacket.java")
voice = text("src/main/java/matteroverdrive/pda/PdaVoiceLineCatalog.java")
audio = text("src/main/java/matteroverdrive/client/PdaEmbeddedAudio.java")
manifest_text = text("src/main/resources/assets/matteroverdrive/pda_voice/voice_bank_manifest.json")
ambient_catalog = text("src/main/java/matteroverdrive/world/AmbientLoreCatalog.java")
ambient_state = text("src/main/java/matteroverdrive/world/AmbientLoreSavedData.java")
ambient_item = text("src/main/java/matteroverdrive/item/RecoveredLoreFragmentItem.java")
data_pad_item = text("src/main/java/matteroverdrive/item/DataPadItem.java")
structure_events = text("src/main/java/matteroverdrive/event/StructureLoreEvents.java")
ambient_loot = text("src/main/resources/data/matteroverdrive/loot_tables/chests/facilities/ambient_lore.json")
story_cache = text("src/main/resources/data/matteroverdrive/loot_tables/chests/facilities/story_cache.json")
salvage = text("src/main/resources/data/matteroverdrive/loot_tables/chests/facilities/salvage.json")

for token in ['PROTOCOL = "17"', "DialogueChoicePacket.class", "openBranchingDialogue", "sendDialogueView", "chooseDialogue"]:
    require(network, token, "ModNetwork")
for token in ["dialogueId", "nodeId", "ChoiceOption", "choices", "Compatibility constructor"]:
    require(packet, token, "NpcDialoguePacket")
for token in ["DialogueSessionManager.choose", "context.getSender()"]:
    require(choice_packet, token, "DialogueChoicePacket")
for token in ["SESSION_LIFETIME_TICKS", "session.dialogueId().equals", "session.nodeId().equals", "PlayerLoggedOutEvent"]:
    require(sessions, token, "DialogueSessionManager")
for token in ["DialogueStateSavedData", "fieldTrust", "syntheticTrust", "archiveInsight", "lastChoice", "matteroverdrive_dialogue_state"]:
    require(state, token, "DialogueStateSavedData")
for token in ["ModNetwork.chooseDialogue", "ChoiceOption", "READ ALOUD", "SYNTHETIC CONTACT", "HologramPortraitRenderer"]:
    require(screen, token, "NpcDialogueScreen")

profile_ids = [
    "researcher.field", "researcher.salvager", "researcher.recovery", "researcher.icarus",
    "researcher.janus", "researcher.archivist", "synthetic.morrow", "synthetic.chorus", "synthetic.hephaestus",
]
for profile_id in profile_ids:
    require(catalog, f'add("{profile_id}"', "DialogueCatalog")
for token in ["recordChoice", "ask_more", "corroborative context", "fieldTrust", "syntheticTrust", "archiveInsight"]:
    require(catalog, token, "DialogueCatalog")
for token in ["researcher.field", "researcher.salvager", "researcher.recovery", "researcher.icarus", "researcher.janus", "researcher.archivist"]:
    require(researcher, token, "FacilityResearcherEntity")
for token in ["synthetic.morrow", "synthetic.chorus", "synthetic.hephaestus"]:
    require(defector, token, "DefectorAndroidEntity")
for token in ["field_liaison", "synthetic_liaison", "incident_analyst", "fieldTrust", "syntheticTrust", "archiveInsight", "sendPdaVoice"]:
    require(awards, token, "DialogueAdvancementEvents")
for token in ["fieldTrust", "syntheticTrust", "archiveInsight"]:
    require(data_packet, token, "DataPadOpenPacket")

adv_dir = ROOT / "src/main/resources/data/matteroverdrive/advancements/campaign"
new_advancements = {
    "pattern_architect.json": "matteroverdrive:campaign/matter_age",
    "network_specialist.json": "matteroverdrive:campaign/automation",
    "energy_arms_specialist.json": "matteroverdrive:campaign/automation",
    "containment_engineer.json": "matteroverdrive:campaign/fusion_age",
    "industrialist.json": "matteroverdrive:campaign/automation",
    "field_liaison.json": "matteroverdrive:campaign/first_contact",
    "synthetic_liaison.json": "matteroverdrive:campaign/android_path",
    "incident_analyst.json": "matteroverdrive:campaign/frontier_expedition",
    "full_spectrum_engineer.json": "matteroverdrive:campaign/anomaly_engineer",
    "field_archivist.json": "matteroverdrive:campaign/frontier_expedition",
    "every_scrap_matters.json": "matteroverdrive:campaign/field_archivist",
    "anomaly_field_observer.json": "matteroverdrive:campaign/anomaly_engineer",
    "coalition_builder.json": "matteroverdrive:campaign/incident_analyst",
    "network_of_trust.json": "matteroverdrive:campaign/coalition_builder",
    "field_veteran.json": "matteroverdrive:campaign/automation",
}
for name, parent in new_advancements.items():
    path = adv_dir / name
    if not path.is_file():
        ERRORS.append(f"missing advancement: {name}")
        continue
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        ERRORS.append(f"invalid JSON {name}: {exc}")
        continue
    if data.get("parent") != parent:
        ERRORS.append(f"{name}: expected parent {parent}, got {data.get('parent')}")
    if not data.get("criteria"):
        ERRORS.append(f"{name}: has no criteria")
    display = data.get("display", {})
    if not display.get("title") or not display.get("description"):
        ERRORS.append(f"{name}: missing display title/description")

for name in ["field_liaison.json", "synthetic_liaison.json", "incident_analyst.json",
             "field_archivist.json", "every_scrap_matters.json", "anomaly_field_observer.json",
             "coalition_builder.json", "network_of_trust.json", "field_veteran.json"]:
    path = adv_dir / name
    if path.is_file():
        data = json.loads(path.read_text(encoding="utf-8"))
        criteria = data.get("criteria", {})
        triggers = [value.get("trigger") for value in criteria.values() if isinstance(value, dict)]
        if not triggers or any(trigger != "minecraft:impossible" for trigger in triggers):
            ERRORS.append(f"{name}: server-earned advancement must use minecraft:impossible")

voice_ids = re.findall(r'LINES\.put\("([^"]+)"', voice)
if len(voice_ids) < 80:
    ERRORS.append(f"PdaVoiceLineCatalog expected at least 80 authored lines, found {len(voice_ids)}")
if len(set(voice_ids)) != len(voice_ids):
    ERRORS.append("PdaVoiceLineCatalog contains duplicate IDs")
for required in ["field_link", "closed_loop", "site_dustwell", "site_lagrange",
                 "lore_dustwell_shift", "lore_lagrange_shift", "lore_dustwell_markings",
                 "lore_lagrange_tape"]:
    if required not in voice_ids:
        ERRORS.append(f"PdaVoiceLineCatalog missing required line {required}")
for token in ["System.Speech", "powershell.exe", "espeak", "spd-say", "PdaVoiceLineCatalog.line",
              "pda_voice/", "config", "recordedStream", "PdaVoiceProfile", ".profile"]:
    require(audio, token, "PdaEmbeddedAudio")
try:
    manifest = json.loads(manifest_text)
    manifest_ids = [entry.get("id") for entry in manifest.get("lines", [])]
    if manifest_ids != voice_ids:
        ERRORS.append("voice_bank_manifest.json IDs/order do not match PdaVoiceLineCatalog")
except Exception as exc:
    ERRORS.append(f"voice bank manifest invalid JSON: {exc}")

ambient_ids = re.findall(r'e\("([^"]+)",\s*"([^"]+)"', ambient_catalog)
if len(ambient_ids) != 48:
    ERRORS.append(f"AmbientLoreCatalog expected 48 records, found {len(ambient_ids)}")
ambient_id_only = [pair[0] for pair in ambient_ids]
if len(set(ambient_id_only)) != len(ambient_id_only):
    ERRORS.append("AmbientLoreCatalog contains duplicate IDs")
for ambient_id in ambient_id_only:
    if ambient_id not in ambient_loot:
        ERRORS.append(f"ambient_lore loot table missing {ambient_id}")
for token in ["matteroverdrive_ambient_lore", "LinkedHashSet", "discover", "MAX_RECORDS_PER_PLAYER"]:
    require(ambient_state, token, "AmbientLoreSavedData")
for token in ["MatterOverdriveLoreId", "AmbientLoreSavedData", "sendPdaVoice", "field_archivist", "every_scrap_matters"]:
    require(ambient_item, token, "RecoveredLoreFragmentItem")
for token in ["--- Recovered Logs ---", "@lore:", "AmbientLoreSavedData"]:
    require(data_pad_item, token, "DataPadItem")
for token in ["FIELD_LOGS", "renderFieldLogs", "AmbientLoreCatalog", "currentAmbientEntry"]:
    require(pda_screen, token, "DataPadScreen")
for token in ["ambient_lore", "recovered_lore_fragment"]:
    require(story_cache, token, "story_cache.json")
    require(salvage, token, "salvage.json")
for site_voice in ["site_dustwell", "site_mnemosyne", "site_kestrel", "site_atlas", "site_helix", "site_nereid",
                   "site_echo9", "site_halcyon", "site_voss", "site_janus", "site_morrow", "site_bastion",
                   "site_hephaestus", "site_icarus", "site_orpheus", "site_lagrange"]:
    require(structure_events, f'"{site_voice}"', "StructureLoreEvents")

for owner, blob in [("DialogueCatalog", catalog), ("DialogueStateSavedData", state),
                    ("DialogueSessionManager", sessions), ("DialogueAdvancementEvents", awards),
                    ("AmbientLoreCatalog", ambient_catalog), ("AmbientLoreSavedData", ambient_state),
                    ("RecoveredLoreFragmentItem", ambient_item)]:
    forbid(blob.lower(), "star_map", owner)
    forbid(blob, "setChunkForced", owner)
    forbid(blob, "addRegionTicket", owner)

if ERRORS:
    print("ADVANCEMENT / DIALOGUE / QOL / AMBIENT LORE VALIDATION FAILED")
    for error in ERRORS:
        print(" -", error)
    sys.exit(1)

print("ADVANCEMENT / DIALOGUE / QOL / AMBIENT LORE VALIDATION PASSED")
print(f"  branching_profiles={len(profile_ids)}")
print(f"  tracked_advancements={len(new_advancements)}")
print(f"  pda_voice_lines={len(voice_ids)}")
print(f"  optional_physical_lore_records={len(ambient_id_only)}")
print("  dialogue choices are server-session validated and world-persistent")
print("  physical lore IDs are loot-backed, deduplicated, PDA-browsable and voiced")
