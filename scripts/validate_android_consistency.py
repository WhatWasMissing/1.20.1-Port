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
    "client_state": ROOT / "src/main/java/matteroverdrive/client/AndroidClientState.java",
    "hud": ROOT / "src/main/java/matteroverdrive/client/AndroidHudOverlay.java",
    "tree": ROOT / "src/main/java/matteroverdrive/client/screen/AndroidSkillTreeScreen.java",
    "packet": ROOT / "src/main/java/matteroverdrive/network/AndroidStatePacket.java",
    "network": ROOT / "src/main/java/matteroverdrive/network/ModNetwork.java",
    "station": ROOT / "src/main/java/matteroverdrive/blockentity/AndroidStationBlockEntity.java",
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
    client = read("client_state")
    hud = read("hud")
    tree = read("tree")
    packet = read("packet")
    network = read("network")
    station = read("station")

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

    if errors:
        print(f"ANDROID CONSISTENCY FAILED: {len(errors)} issue(s)")
        for error in errors:
            print(f" - {error}")
        return 1

    print("ANDROID CONSISTENCY PASSED: effective capacity, packet/UI progression and clamp contracts agree")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
