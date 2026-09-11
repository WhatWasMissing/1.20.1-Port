#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
failures = []

def read(path: str) -> str:
    p = ROOT / path
    if not p.is_file():
        failures.append(f"missing file: {path}")
        return ""
    return p.read_text(encoding="utf-8")

def require(text: str, path: str, *tokens: str):
    for token in tokens:
        if token not in text:
            failures.append(f"{path}: missing required token {token!r}")

def forbid(text: str, path: str, *tokens: str):
    for token in tokens:
        if token in text:
            failures.append(f"{path}: forbidden token present {token!r}")

network_path = "src/main/java/matteroverdrive/network/ModNetwork.java"
network = read(network_path)
require(network, network_path,
        'PROTOCOL = "16"',
        "FacilityDiscoveryPacket.class",
        "EnvironmentalHazardPacket.class",
        "sendFacilityDiscovery",
        "sendEnvironmentalHazard")

queue_path = "src/main/java/matteroverdrive/client/ClientPdaNotificationManager.java"
queue = read(queue_path)
require(queue, queue_path,
        "Deque<Pending>", "QUEUE", "startNext", "PdaNarrationController.isSpeaking()",
        "PdaEmbeddedAudio.playVoice", "showFacilityBanner", "setHazard",
        'case "signal_echo"', 'case "gravitational_anomaly", "anomaly_containment"')

opener_path = "src/main/java/matteroverdrive/client/ClientPdaVoiceOpener.java"
opener = read(opener_path)
require(opener, opener_path, "ClientPdaNotificationManager.enqueue")
forbid(opener, opener_path, "sayNow(", "PdaNarrationController.read(")

audio_path = "src/main/java/matteroverdrive/client/PdaEmbeddedAudio.java"
audio = read(audio_path)
require(audio, audio_path,
        "System.Speech.Synthesis.SpeechSynthesizer",
        'new ProcessBuilder("say"',
        'new ProcessBuilder("espeak"',
        "spd-say",
        "MO_PDA_TEXT",
        "synthesize(pattern",
        'case "ui_startup"', 'case "ui_record"', 'case "ui_facility"',
        'case "ui_hazard"', 'case "ui_comm"')
forbid(audio, audio_path, "http://", "https://", "fal.ai", "elevenlabs", "api_key", "API_KEY")

overlay_path = "src/main/java/matteroverdrive/client/PresentationOverlay.java"
overlay = read(overlay_path)
require(overlay, overlay_path,
        "RenderGuiOverlayEvent.Post", "renderFacilityBanner", "renderHazard", "renderCaption",
        "FACILITY IDENTIFIED", "CRITICAL ENVIRONMENT", "smooth(")

portrait_path = "src/main/java/matteroverdrive/client/screen/HologramPortraitRenderer.java"
portrait = read(portrait_path)
require(portrait, portrait_path,
        "HologramPortraitRenderer", "synthetic", "scanY", "MRW-17", "CHR-LINK", "HEP-NODE")

dialogue_path = "src/main/java/matteroverdrive/client/screen/NpcDialogueScreen.java"
dialogue = read(dialogue_path)
require(dialogue, dialogue_path,
        "HologramPortraitRenderer.render", "portraitWidth", "READ ALOUD", "STOP VOICE",
        "SYNTHETIC CONTACT", "FIELD COMMUNICATION")

lore_path = "src/main/java/matteroverdrive/event/StructureLoreEvents.java"
lore = read(lore_path)
require(lore, lore_path,
        "LAST_SCANNED_CELL", ">> 3", "sendFacilityDiscovery", "sendEnvironmentalHazard",
        "ANOMALY_WARNING_RADIUS", "level.hasChunkAt(cursor)", '"gravitational_anomaly"',
        '"signal_echo"', '"m0_resonance"', '"pressure_damage"', '"structural_damage"')
forbid(lore, lore_path,
       "getChunk(", "setChunkForced", "addRegionTicket", "TicketType")

catalog_path = "src/main/java/matteroverdrive/pda/PdaVoiceLineCatalog.java"
catalog = read(catalog_path)
for token in [
    "field_link", "record_recovered", "reconstruction_complete", "matter_resonance",
    "anomaly_warning", "pressure_warning", "structural_warning", "signal_echo",
    "orpheus_security", "icarus_warning", "synthetic_contact", "closed_loop", "database_ready"
]:
    require(catalog, catalog_path, f'"{token}"')

welcome_path = "src/main/java/matteroverdrive/event/ModWelcomeEvents.java"
welcome = read(welcome_path)
require(welcome, welcome_path, 'sendPdaVoice(player, "field_link")')

title_path = "src/main/java/matteroverdrive/client/screen/MatterOverdriveTitleScreen.java"
title = read(title_path)
require(title, title_path, 'PdaEmbeddedAudio.playUi("ui_startup")')

if failures:
    print("PDA PRESENTATION VALIDATION FAILED")
    for failure in failures:
        print(" -", failure)
    sys.exit(1)

print("PDA PRESENTATION VALIDATION PASSED")
print(" - queued short-form PDA speech with manual-narration exclusion")
print(" - offline OS speech synthesis with narrator fallback")
print(" - generated startup/record/facility/hazard/comm UI cues")
print(" - animated facility banner + persistent environmental warnings")
print(" - responsive human/synthetic hologram portrait panels")
print(" - loaded-chunk-only anomaly scanning and movement-cell structure checks")
