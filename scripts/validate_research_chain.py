from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
progression = (ROOT / "src/main/java/matteroverdrive/quest/ResearchProgression.java").read_text(encoding="utf-8")
discovery = (ROOT / "src/main/java/matteroverdrive/world/TechnologySiteDiscoverySavedData.java").read_text(encoding="utf-8")
events = (ROOT / "src/main/java/matteroverdrive/event/ContractEvents.java").read_text(encoding="utf-8")
dossier = (ROOT / "src/main/java/matteroverdrive/item/FacilityResearchItem.java").read_text(encoding="utf-8")
pad = (ROOT / "src/main/java/matteroverdrive/item/DataPadItem.java").read_text(encoding="utf-8")
commands = (ROOT / "src/main/java/matteroverdrive/quest/ResearchCommands.java").read_text(encoding="utf-8")

def require(text: str, needle: str, label: str) -> None:
    if needle not in text:
        raise SystemExit(f"research-chain validation failed: {label}")

for stage in ("MATTER_TECHNOLOGY", "AUTOMATION_DRONES", "FUSION_RESEARCH", "ANOMALY_ENGINEERING"):
    require(progression, stage, f"progression stage {stage}")
require(progression, "unlockEvidence", "ordered evidence unlock")
require(progression, "Player.PERSISTED_NBT_TAG", "per-player persistence")
require(discovery, 'private static final String[] CHAIN = {"android_relay_outpost", "matter_observatory", "anomaly_research_site"}', "deterministic chain")
require(discovery, "MAX_DISCOVERIES", "bounded discovery ledger")
require(events, "discoveries.advanceChain", "chain advancement on discovery")
require(events, 'chainStage == 3', "terminal chain reward")
require(events, 'upgrade_parallel_processing', "terminal chain reward item")
require(dossier, "archiveAtAnalyzer", "server-side dossier analysis")
require(dossier, "isArchived", "duplicate reward guard")
require(dossier, "ResearchProgression.unlockEvidence", "dossier progression integration")
require(pad, "chainStage", "Data Pad chain state")
require(pad, "siteNames", "Data Pad discovery state")
require(commands, 'Commands.literal("research")', "research command root")
require(commands, 'Commands.literal("status")', "read-only research status")
require(commands, 'hasPermission(2)', "operator-only milestone control")
require(commands, "unlockEvidence", "bounded milestone advancement")
print("research-chain validation passed")
