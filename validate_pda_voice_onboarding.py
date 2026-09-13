from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
errors = []


def read(path):
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing file: {path}")
        return ""
    return p.read_text(encoding="utf-8")


def require(text, needle, label):
    if needle not in text:
        errors.append(f"{label}: missing {needle!r}")


network = read("src/main/java/matteroverdrive/network/ModNetwork.java")
voice_packet = read("src/main/java/matteroverdrive/network/PdaVoicePacket.java")
welcome_packet = read("src/main/java/matteroverdrive/network/WelcomeBriefingPacket.java")
voice_catalog = read("src/main/java/matteroverdrive/pda/PdaVoiceLineCatalog.java")
welcome_data = read("src/main/java/matteroverdrive/world/WorldOnboardingSavedData.java")
welcome_events = read("src/main/java/matteroverdrive/event/ModWelcomeEvents.java")
lore_events = read("src/main/java/matteroverdrive/event/StructureLoreEvents.java")
dialogue = read("src/main/java/matteroverdrive/client/screen/NpcDialogueScreen.java")
title_events = read("src/main/java/matteroverdrive/client/TitleScreenEvents.java")
title_screen = read("src/main/java/matteroverdrive/client/screen/MatterOverdriveTitleScreen.java")
briefing = read("src/main/java/matteroverdrive/client/screen/FirstLoginBriefingScreen.java")
researcher = read("src/main/java/matteroverdrive/entity/FacilityResearcherEntity.java")
defector = read("src/main/java/matteroverdrive/entity/DefectorAndroidEntity.java")
voice_settings = read("src/main/java/matteroverdrive/client/PdaVoiceSettings.java")
voice_keys = read("src/main/java/matteroverdrive/client/PdaKeyMappings.java")
pda_screen = read("src/main/java/matteroverdrive/client/screen/DataPadScreen.java")

for token in ["PdaVoicePacket.class", "WelcomeBriefingPacket.class", 'PROTOCOL = "17"']:
    require(network, token, "network")

for line_id in [
    "field_link", "record_recovered", "reconstruction_complete", "matter_resonance",
    "anomaly_warning", "orpheus_security", "icarus_warning", "synthetic_contact",
    "closed_loop", "database_ready"
]:
    require(voice_catalog, f'LINES.put("{line_id}"', "voice catalog")

require(voice_packet, "ClientPdaVoiceOpener.play", "voice packet")
require(welcome_packet, "openFirstWorldBriefing()", "welcome packet")
require(welcome_data, "markBriefed", "world onboarding")
require(welcome_data, "matteroverdrive_world_onboarding", "world onboarding")
require(welcome_events, "BRIEFING_DELAY_TICKS = 40", "welcome event")
require(welcome_events, "openWelcomeBriefing", "welcome event")

for token in ["voiceLineFor", "reconstruction_complete", "closed_loop", "legacy_security", "icarus_containment"]:
    require(lore_events, token, "lore voice integration")

for token in ["READ ALOUD", "STOP VOICE", "linesPerPage", "syntheticSpeaker", "PdaNarrationController"]:
    require(dialogue, token, "dialogue screen")
require(researcher, "ModNetwork.openBranchingDialogue", "researcher dialogue")
require(defector, "ModNetwork.openBranchingDialogue", "defector dialogue")

for token in ["ScreenEvent.Init.Post", "TitleScreen", "MATTER OVERDRIVE", "MatterOverdriveTitleScreen"]:
    require(title_events, token, "title screen hook")
for token in ["THE OVERDRIVE INCIDENT", "SYSTEM PAGE", "EXPLORE / RECOVER / DECIDE"]:
    require(title_screen, token, "title screen")
for token in ["BRIEFING %02d / 03", "FIELD LINK ESTABLISHED", "THE OVERDRIVE INCIDENT", "This briefing appears once"]:
    require(briefing, token, "first login briefing")

for token in ["matteroverdrive_pda_voice.dat", "enabled = true", "PdaNarrationController.stop"]:
    require(voice_settings, token, "PDA voice settings")
for token in ["TOGGLE_VOICE", "GLFW.GLFW_KEY_P", "RegisterKeyMappingsEvent"]:
    require(voice_keys, token, "PDA voice keybind")
for token in ["PdaVoiceSettings.buttonLabel()", "toggleVoice()", "PdaVoiceSettings"]:
    require(pda_screen, token, "PDA voice control")

# Presentation systems must not force-load worldgen chunks.
combined = welcome_events + lore_events + title_events + briefing + dialogue
for forbidden in ["setChunkForced", "getChunk(", "forceChunk", "TicketType"]:
    if forbidden in combined:
        errors.append(f"presentation layer contains force-loading API token {forbidden!r}")

if errors:
    print("PDA VOICE / ONBOARDING VALIDATION FAILED")
    for error in errors:
        print(" -", error)
    sys.exit(1)

print("PDA VOICE / ONBOARDING VALIDATION PASSED")
print(" - contextual original PDA voice-line catalog present")
print(" - first-world briefing persistence and delayed open present")
print(" - title-screen entry point and dedicated landing screen present")
print(" - researcher and synthetic dialogue use responsive narratable screen")
print(" - archive discoveries and reconstructions trigger contextual callouts")
print(" - no force-loading API tokens detected in presentation layer")
