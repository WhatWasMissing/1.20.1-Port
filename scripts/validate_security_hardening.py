"""Deterministic source checks for the server-authority hardening slice."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def require(path: str, *needles: str) -> None:
    text = (ROOT / path).read_text(encoding="utf-8")
    missing = [needle for needle in needles if needle not in text]
    if missing:
        raise SystemExit(f"{path}: missing {missing}")

require(
    "src/main/java/matteroverdrive/security/ServerDebugAccess.java",
    "player.hasPermissions(2)",
    "Operator permission required",
)
require(
    "src/main/java/matteroverdrive/blockentity/TransporterBlockEntity.java",
    "MAX_DESTINATIONS = 64",
    "MAX_DESTINATION_NAME_LENGTH = 64",
    "destinations.size() >= MAX_DESTINATIONS",
    "i < MAX_DESTINATIONS",
)
require(
    "src/main/java/matteroverdrive/world/TechnologySiteDiscoverySavedData.java",
    "MAX_DISCOVERIES = 4096",
    "i < MAX_DISCOVERIES",
    "entry.hasUUID",
)
require(
    "src/main/java/matteroverdrive/world/FacilityRestorationSavedData.java",
    "MAX_FACILITIES = 4096",
    "i < MAX_FACILITIES",
    "key.length() <= MAX_KEY_LENGTH",
)
require(
    "src/main/java/matteroverdrive/entity/EncounterFaction.java",
    "SYNTHETIC_SECURITY",
    "RESEARCH_CONTAINMENT",
    "fromFacilityProfile",
)
require(
    "src/main/java/matteroverdrive/entity/RogueAndroidEntity.java",
    "EncounterFaction encounterFaction",
    "tag.putString(\"EncounterFaction\", encounterFaction.id())",
    "EncounterFaction.fromId",
)
require(
    "src/main/java/matteroverdrive/event/RogueAndroidDropEvents.java",
    "EncounterFaction.BLACK_SITE",
    "RecoveredArtifactItem.recovered",
    "LivingDeathEvent",
    "MatterOverdriveEncounterResearch",
    "unlockEvidence",
)
for path in (
    "src/main/java/matteroverdrive/menu/ChargingStationMenu.java",
    "src/main/java/matteroverdrive/menu/DecomposerMenu.java",
    "src/main/java/matteroverdrive/menu/FusionReactorMenu.java",
    "src/main/java/matteroverdrive/menu/InscriberMenu.java",
    "src/main/java/matteroverdrive/menu/MatterAnalyzerMenu.java",
    "src/main/java/matteroverdrive/menu/MatterRecyclerMenu.java",
    "src/main/java/matteroverdrive/menu/MicrowaveMenu.java",
    "src/main/java/matteroverdrive/menu/ReplicatorMenu.java",
    "src/main/java/matteroverdrive/menu/SolarPanelMenu.java",
    "src/main/java/matteroverdrive/menu/SpacetimeAcceleratorMenu.java",
    "src/main/java/matteroverdrive/menu/TransporterMenu.java",
):
    require(path, "ServerDebugAccess.require")
print("security hardening validation: PASS")
