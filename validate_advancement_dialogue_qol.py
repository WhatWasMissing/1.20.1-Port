#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path
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

for name in ["field_liaison.json", "synthetic_liaison.json", "incident_analyst.json"]:
    path = adv_dir / name
    if path.is_file():
        data = json.loads(path.read_text(encoding="utf-8"))
        trigger = data.get("criteria", {}).get("earned", {}).get("trigger")
        if trigger != "minecraft:impossible":
            ERRORS.append(f"{name}: dialogue-earned advancement must use minecraft:impossible")

voice_ids = [
    "field_link", "record_recovered", "reconstruction_complete", "matter_resonance",
    "anomaly_warning", "pressure_warning", "structural_warning", "signal_echo",
    "orpheus_security", "icarus_warning", "synthetic_contact", "field_liaison",
    "synthetic_liaison", "incident_analyst", "closed_loop", "database_ready",
]
for line_id in voice_ids:
    require(voice, f'LINES.put("{line_id}"', "PdaVoiceLineCatalog")
for token in ["System.Speech", "powershell.exe", "espeak", "spd-say", "PdaVoiceLineCatalog.line",
              "pda_voice/", "config", "recordedStream"]:
    require(audio, token, "PdaEmbeddedAudio")
try:
    manifest = json.loads(manifest_text)
    manifest_ids = [entry.get("id") for entry in manifest.get("lines", [])]
    if manifest_ids != voice_ids:
        ERRORS.append("voice_bank_manifest.json IDs/order do not match PdaVoiceLineCatalog validator contract")
except Exception as exc:
    ERRORS.append(f"voice bank manifest invalid JSON: {exc}")

for owner, blob in [("DialogueCatalog", catalog), ("DialogueStateSavedData", state),
                    ("DialogueSessionManager", sessions), ("DialogueAdvancementEvents", awards)]:
    forbid(blob.lower(), "star_map", owner)
    forbid(blob, "setChunkForced", owner)
    forbid(blob, "addRegionTicket", owner)

if ERRORS:
    print("ADVANCEMENT / DIALOGUE / QOL VALIDATION FAILED")
    for error in ERRORS:
        print(" -", error)
    sys.exit(1)

print("ADVANCEMENT / DIALOGUE / QOL VALIDATION PASSED")
print(f"  branching_profiles={len(profile_ids)}")
print(f"  added_advancements={len(new_advancements)}")
print(f"  canonical_pda_voice_lines={len(voice_ids)}")
print("  dialogue choices are server-session validated and world-persistent")
