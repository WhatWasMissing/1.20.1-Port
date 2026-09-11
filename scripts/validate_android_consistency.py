#!/usr/bin/env python3
"""Static contract checks for Android power/progression state.

This intentionally avoids Forge/Gradle so the most failure-prone client/server state
contracts can be checked before Minecraft starts.
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

FILES = {
    "data": ROOT / "src/main/java/matteroverdrive/android/AndroidData.java",
    "chassis": ROOT / "src/main/java/matteroverdrive/android/AndroidChassisData.java",
    "events": ROOT / "src/main/java/matteroverdrive/event/AndroidEvents.java",
    "loadout_events": ROOT / "src/main/java/matteroverdrive/event/AndroidLoadoutEvents.java",
    "loadout_definition": ROOT / "src/main/java/matteroverdrive/android/AndroidLoadout.java",
    "client_state": ROOT / "src/main/java/matteroverdrive/client/AndroidClientState.java",
    "hud": ROOT / "src/main/java/matteroverdrive/client/AndroidHudOverlay.java",
    "tree": ROOT / "src/main/java/matteroverdrive/client/screen/AndroidSkillTreeScreen.java",
    "packet": ROOT / "src/main/java/matteroverdrive/network/AndroidStatePacket.java",
    "network": ROOT / "src/main/java/matteroverdrive/network/ModNetwork.java",
    "station": ROOT / "src/main/java/matteroverdrive/blockentity/AndroidStationBlockEntity.java",
    "artifact": ROOT / "src/main/java/matteroverdrive/item/RecoveredArtifactItem.java",
    "energy_weapon": ROOT / "src/main/java/matteroverdrive/item/weapon/EnergyWeaponItem.java",
    "reactor_remote": ROOT / "src/main/java/matteroverdrive/item/ReactorRemoteItem.java",
    "reactor_controller": ROOT / "src/main/java/matteroverdrive/blockentity/FusionReactorControllerBlockEntity.java",
    "matter_analyzer": ROOT / "src/main/java/matteroverdrive/blockentity/MatterAnalyzerBlockEntity.java",
    "facility_research": ROOT / "src/main/java/matteroverdrive/item/FacilityResearchItem.java",
    "data_pad": ROOT / "src/main/java/matteroverdrive/item/DataPadItem.java",
}

errors: list[str] = []


def read(name: str) -> str:
    path = FILES[name]
    try:
        return path.read_text(encoding="utf-8")
    except OSError as exc:
        errors.append(f"cannot read {path.relative_to(ROOT)}: {exc}")
        return ""


def require(text: str, marker: str, label: str) -> None:
    if marker not in text:
        errors.append(f"missing {label}: {marker}")


def forbid(text: str, marker: str, label: str) -> None:
    if marker in text:
        errors.append(f"forbidden {label}: {marker}")


def main() -> int:
    data = read("data")
    chassis = read("chassis")
    events = read("events")
    loadout = read("loadout_events")
    loadout_definition = read("loadout_definition")
    client = read("client_state")
    hud = read("hud")
    tree = read("tree")
    packet = read("packet")
    network = read("network")
    station = read("station")
    artifact = read("artifact")
    energy_weapon = read("energy_weapon")
    reactor_remote = read("reactor_remote")
    reactor_controller = read("reactor_controller")
    matter_analyzer = read("matter_analyzer")
    facility_research = read("facility_research")
    data_pad = read("data_pad")

    require(data, "getEnergyCapacity(Player player)", "authoritative effective capacity API")
    require(chassis, "CAPACITOR_CORE", "Capacitor Core")
    require(chassis, "capacity += 50_000", "Capacitor Core 50k capacity bonus")
    require(chassis, "clampStoredEnergyToCapacity(player)", "capacity shrink persistence clamp")
    require(chassis, "AndroidData.setEnergy(player, AndroidData.getEnergy(player))", "persisted hidden-energy clamp")

    # Runtime systems that can fill or threshold the Android reserve must not use the
    # base 100k constant once chassis-adjusted capacity exists.
    forbid(events, "AndroidData.ENERGY_CAPACITY", "base-capacity reference in AndroidEvents")
    forbid(loadout, "AndroidData.ENERGY_CAPACITY", "base-capacity reference in AndroidLoadoutEvents")
    require(events, "int capacity = AndroidData.getEnergyCapacity(player);", "handheld charging effective capacity")
    require(loadout, "int capacity = AndroidData.getEnergyCapacity(player);", "loadout effective capacity")

    # Server -> client packet contract.
    require(packet, "int energyCapacity", "energy capacity packet field")
    require(packet, "buffer.writeVarInt(packet.energyCapacity)", "energy capacity packet encode")
    require(client, "energyCapacity()", "client capacity getter")
    require(network, "AndroidData.getEnergyCapacity(p)", "server capacity sync")
    match = re.search(r'PROTOCOL\s*=\s*"(\d+)"', network)
    if not match or int(match.group(1)) < 10:
        errors.append("network protocol was not bumped for the AndroidStatePacket shape change")

    # UI contract: HUD/tree must show authoritative capacity and actual nonlinear
    # progression/point rules instead of historical placeholders.
    require(hud, "AndroidClientState.energyCapacity()", "HUD effective capacity")
    require(hud, "AndroidData.skillPointsForLevel(level)", "HUD skill point formula")
    require(hud, "AndroidData.experienceForLevel(level)", "HUD XP curve")
    forbid(hud, "private static final int CAPACITY = 100_000", "hard-coded HUD capacity")
    forbid(hud, "level - Long.bitCount", "linear HUD skill point formula")
    require(tree, "AndroidClientState.energyCapacity()", "Skill Tree effective capacity")

    # Existing machine-side charging should remain tied to the same authoritative API.
    require(station, "AndroidData.getEnergyCapacity(player)", "Android Station charge capacity")
    require(station, "AndroidData.getEnergyCapacity(viewer)", "Android Station displayed capacity")

    # Clone persistence is a high-risk regression area for this subsystem.
    require(events, "AndroidData.copyTo(event.getOriginal(), event.getEntity())", "Android core clone persistence")

    # Structure artifacts must remain a real server-authoritative reward rather
    # than an inert item with a client-only loadout mutation.
    require(artifact, "RecoveredProtocol", "artifact protocol persistence tag")
    require(artifact, "AndroidLoadout.selectArtifact(server, decoded)", "artifact passive integration")
    require(artifact, "ModNetwork.syncAndroidState(server)", "artifact state synchronization")
    require(artifact, "!AndroidData.isAndroid(server)", "artifact Android eligibility gate")
    require(energy_weapon, "Artifact.THERMAL_LATTICE", "Thermal Lattice weapon integration")
    require(energy_weapon, "added *= 0.65F", "Thermal Lattice shot heat reduction")
    require(energy_weapon, "heat - cooling", "Thermal Lattice passive cooling")
    require(loadout_definition, "REACTOR_SYMBIOTE", "Reactor Symbiote protocol definition")
    require(loadout, "ReactorRemoteItem.findLinkedRunningController(player)", "Reactor Symbiote server-side remote lookup")
    require(loadout, "controller.drawAndroidUplinkEnergy(requested)", "Reactor Symbiote reactor-funded energy draw")
    require(reactor_remote, "findLinkedRunningController(ServerPlayer player)", "bounded Reactor Remote Android lookup")
    require(reactor_remote, "if (!level.hasChunkAt(target)) return null;", "Reactor Remote no forced chunk load")
    require(reactor_controller, "drawAndroidUplinkEnergy(int limit)", "reactor-authoritative Android FE withdrawal")
    require(reactor_controller, "generatedLastTick <= 0", "Reactor Symbiote running-reactor gate")
    # Field dossiers must take the real analysis path, with a server-owned
    # operator identity that survives machine save/reload rather than a direct-use reward.
    require(facility_research, "Matter Analyzer", "field dossier analyzer instruction")
    require(facility_research, "analyzer.queueResearch(server, stack)", "dossier analyzer queue")
    require(facility_research, "archiveAtAnalyzer(ServerPlayer server", "server-side dossier reward")
    require(matter_analyzer, "RESEARCH_ANALYZE_SPEED = 400", "research analysis duration")
    require(matter_analyzer, "RESEARCH_ENERGY_PER_TICK = 256", "research analysis energy cost")
    require(matter_analyzer, "researchOperator", "per-player research operator persistence")
    require(matter_analyzer, "FacilityResearchItem.archiveAtAnalyzer(operator, queuedResearch)", "analyzer research completion")
    encounter_events = (ROOT / "src/main/java/matteroverdrive/event/RogueAndroidDropEvents.java").read_text(encoding="utf-8")
    require(encounter_events, "LivingDeathEvent", "encounter research death hook")
    require(encounter_events, "MatterOverdriveEncounterResearch", "per-player encounter persistence")
    require(encounter_events, "ResearchProgression.unlockEvidence", "encounter progression reward")
    require(data_pad, "MatterOverdriveEncounterResearch", "Data Pad encounter evidence")
    require(data_pad, "Encounter evidence:", "Data Pad encounter evidence summary")

    if errors:
        print(f"ANDROID CONSISTENCY FAILED: {len(errors)} issue(s)")
        for error in errors:
            print(f" - {error}")
        return 1

    print("ANDROID CONSISTENCY PASSED: effective capacity, packet/UI progression and clamp contracts agree")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
