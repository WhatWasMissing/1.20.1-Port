#!/usr/bin/env python3
from __future__ import annotations

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


catalog = text("src/main/java/matteroverdrive/world/TechnologyLoreCatalog.java")
state = text("src/main/java/matteroverdrive/world/TechnologyLoreSavedData.java")
events = text("src/main/java/matteroverdrive/event/TechnologyLoreEvents.java")
voice = text("src/main/java/matteroverdrive/pda/PdaVoiceLineCatalog.java")
audio = text("src/main/java/matteroverdrive/client/PdaEmbeddedAudio.java")
data_pad = text("src/main/java/matteroverdrive/item/DataPadItem.java")
screen = text("src/main/java/matteroverdrive/client/screen/DataPadScreen.java")
items = text("src/main/java/matteroverdrive/registry/ModItems.java")
blocks = text("src/main/java/matteroverdrive/registry/ModBlocks.java")
exporter = text("tools/pda_voicebank/export_generation_queue.py")
readme = text("tools/pda_voicebank/README.md")

record_ids = re.findall(r'^\s*add\("([^"]+)"', catalog, re.MULTILINE)
if len(record_ids) < 50:
    ERRORS.append(f"TechnologyLoreCatalog expected broad major-technology coverage, found only {len(record_ids)} canonical records")
if len(record_ids) != len(set(record_ids)):
    ERRORS.append("TechnologyLoreCatalog contains duplicate canonical IDs")

critical = [
    # Matter/pattern/fabrication
    "decomposer", "matter_recycler", "matter_analyzer", "pattern_drive", "pattern_storage",
    "pattern_monitor", "replicator", "inscriber", "matter_storage_matrix", "matter_excavator",
    # Power/utility/network
    "solar_panel", "charging_station", "grid_capacitor", "heavy_matter_pipe", "matter_pipe",
    "network_pipe", "network_switch", "network_router", "pylon", "matter_network_terminal",
    "transporter", "spacetime_accelerator", "microwave",
    # Fusion/anomaly
    "fusion_reactor_controller", "fusion_reactor_io", "fusion_reactor_coil", "gravitational_anomaly",
    "gravitational_stabilizer", "spacetime_equalizer", "anomaly_containment_unit", "reactor_remote",
    # Synthetic/drone
    "android_station", "android_spawner", "android_induction_relay", "drone_fabricator",
    "drone_deployment_core", "rogue_android_part_head", "chassis_core_capacitor",
    # Weapons/field tools
    "weapon_station", "battery", "hc_battery", "energy_pack", "phaser", "phaser_rifle",
    "ion_sniper", "plasma_shotgun", "omni_tool", "weapon_module_barrel_damage",
    "tritanium_wrench", "matter_scanner", "portable_decomposer", "matter_container",
    "transport_flash_drive", "network_flash_drive", "network_diagnostic_probe", "quantum_linker",
    # Progression/storage
    "matter_storage_cell_64k", "upgrade_base", "security_protocol_empty", "facility_research",
    "artifact", "contract", "data_pad",
]
for tech_id in critical:
    if tech_id not in record_ids:
        ERRORS.append(f"TechnologyLoreCatalog missing critical major technology: {tech_id}")

for token in ["TechRecord", "voiceId()", "byItemId", "byVoiceId", "voiceLine", "LOOKUP", "String... aliases"]:
    require(catalog, token, "TechnologyLoreCatalog")
for token in ["matteroverdrive_technology_lore", "LinkedHashSet", "discover", "MAX_RECORDS_PER_PLAYER", "entries"]:
    require(state, token, "TechnologyLoreSavedData")
for token in ["ItemCraftedEvent", "EntityItemPickupEvent", "EntityPlaceEvent", "PlayerTickEvent",
              "INVENTORY_SCAN_INTERVAL", "TechnologyLoreSavedData", "sendPdaVoice", "record.voiceId()"]:
    require(events, token, "TechnologyLoreEvents")
for token in ["TechnologyLoreCatalog.voiceLine", "TechnologyLoreCatalog.byVoiceId"]:
    require(voice, token, "PdaVoiceLineCatalog")
for token in ["PdaVoiceLineCatalog.line", "pda_voice/", "local operating-system speech synthesis", "Minecraft Narrator"]:
    require(audio, token, "PdaEmbeddedAudio")
for token in ["TechnologyLoreSavedData", "--- Technology Codex ---", "@tech:", "Technology codex:"]:
    require(data_pad, token, "DataPadItem")
for token in ["TECHNOLOGY", "renderTechnology", "technologyEntries", "currentTechnologyEntry", "READ ALOUD"]:
    require(screen, token, "DataPadScreen")
for token in ["CORE_MANIFEST", "TECH_CATALOG", "voice_generation_queue.json", "tech_", "duplicate voice IDs"]:
    require(exporter, token, "export_generation_queue.py")
for token in ["EXPORT_PDA_VOICE_QUEUE.bat", "tech_*", "PROCESS_PDA_VOICE_BANK.bat"]:
    require(readme, token, "voice-bank README")

# The discovery system must remain ordinary player-state inspection; it must not force world/chunk activity.
for owner, blob in [("TechnologyLoreCatalog", catalog), ("TechnologyLoreSavedData", state),
                    ("TechnologyLoreEvents", events), ("DataPadScreen", screen)]:
    forbid(blob, "setChunkForced", owner)
    forbid(blob, "addRegionTicket", owner)
    forbid(blob, "forceChunk", owner)
    forbid(blob.lower(), "star_map", owner)

# Major registered content should actually still exist in the live registries.
for registry_id in ["decomposer", "replicator", "fusion_reactor_controller", "gravitational_stabilizer",
                    "android_station", "drone_fabricator", "weapon_station", "network_router", "transporter"]:
    if f'"{registry_id}"' not in blocks:
        ERRORS.append(f"ModBlocks no longer contains expected major technology {registry_id}")
for registry_id in ["data_pad", "matter_scanner", "portable_decomposer", "phaser", "ion_sniper",
                    "plasma_shotgun", "reactor_remote", "network_flash_drive", "tritanium_wrench"]:
    if f'"{registry_id}"' not in items:
        ERRORS.append(f"ModItems no longer contains expected major technology {registry_id}")

if ERRORS:
    print("PDA TECHNOLOGY LORE VALIDATION FAILED")
    for error in ERRORS:
        print(" -", error)
    sys.exit(1)

print("PDA TECHNOLOGY LORE VALIDATION PASSED")
print(f"  canonical_technology_records={len(record_ids)}")
print(f"  required_major_technologies={len(critical)}")
print("  first acquisition/craft/place is world-persistent and queues one authored callout")
print("  PDA TECHNOLOGY tab and READ ALOUD path are present")
print("  voice production queue is generated from the live technology catalogue")
