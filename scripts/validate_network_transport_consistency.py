#!/usr/bin/env python3
"""Static regression gate for Matter Overdrive network/transporter invariants."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FILES = {
    "transporter": ROOT / "src/main/java/matteroverdrive/blockentity/TransporterBlockEntity.java",
    "items": ROOT / "src/main/java/matteroverdrive/network/ItemNetworkUtil.java",
    "side_data": ROOT / "src/main/java/matteroverdrive/machine/MachineSideConfigurationData.java",
    "side_events": ROOT / "src/main/java/matteroverdrive/event/MachineSideConfigurationEvents.java",
    "guide": ROOT / "src/main/resources/assets/matteroverdrive/guides/matteroverdrive/guide/transporter_security.md",
}
errors = []


def read(name):
    try:
        return FILES[name].read_text(encoding="utf-8")
    except OSError as exc:
        errors.append(f"cannot read {FILES[name].relative_to(ROOT)}: {exc}")
        return ""


def need(text, marker, label):
    if marker not in text:
        errors.append(f"missing {label}: {marker}")


def forbid(text, marker, label):
    if marker in text:
        errors.append(f"forbidden {label}: {marker}")


def main():
    transporter = read("transporter")
    items = read("items")
    side_data = read("side_data")
    side_events = read("side_events")
    guide = read("guide")

    need(transporter, "distance() <= range()", "inclusive transporter range boundary")
    forbid(transporter, "distance() < range()", "exclusive transporter range regression")
    need(transporter, "level.hasChunkAt(target)", "unloaded-target guard")
    need(transporter, "findSafeArrival", "collision-aware arrival search")
    need(transporter, "level.noCollision(entity, moved)", "arrival collision validation")
    need(transporter, "if (transported <= 0)", "zero-success transport abort")
    need(transporter, "energy.consumeEnergy(energyCost(), level.getGameTime());", "successful transport FE debit")

    need(items, "restoreRemainder", "router rollback helper")
    need(items, "Containers.dropItemStack", "router loss-prevention fallback")
    need(items, "int moved = extracted.getCount() - toInsert.getCount();", "actual inserted-count accounting")

    need(side_data, "public void clear(BlockPos pos)", "side-policy clear API")
    need(side_events, "BlockEvent.BreakEvent", "break lifecycle cleanup")
    need(side_events, "BlockEvent.EntityPlaceEvent", "placement lifecycle cleanup")
    need(side_events, "MachineSideConfigurationData.get(level).clear(event.getPos());", "position cleanup action")

    forbid(guide, "[Star Map]", "active retired Star Map link")
    forbid(guide, "starmap.md", "active retired Star Map page reference")

    if errors:
        print(f"NETWORK/TRANSPORT CONSISTENCY FAILED: {len(errors)} issue(s)")
        for error in errors:
            print(f" - {error}")
        return 1
    print("NETWORK/TRANSPORT CONSISTENCY PASSED: range, arrival safety, router conservation and side-policy lifecycle contracts agree")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
