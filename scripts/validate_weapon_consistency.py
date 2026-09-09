#!/usr/bin/env python3
"""Static regression gate for Matter Overdrive energy-weapon state invariants."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FILES = {
    "weapon": ROOT / "src/main/java/matteroverdrive/item/weapon/EnergyWeaponItem.java",
    "system": ROOT / "src/main/java/matteroverdrive/item/weapon/WeaponSystem.java",
    "station": ROOT / "src/main/java/matteroverdrive/blockentity/WeaponStationBlockEntity.java",
    "menu": ROOT / "src/main/java/matteroverdrive/menu/WeaponStationMenu.java",
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


def main():
    weapon = read("weapon")
    system = read("system")
    station = read("station")
    menu = read("menu")

    need(weapon, "if (!hasEnoughEnergy(weapon, shooter, energyCost) && !tryReload(weapon, shooter, energyCost))", "pre-fire energy gate")
    need(weapon, "drainEnergy(weapon, shooter, energyCost);", "shot energy consumption")
    need(weapon, "candidate == weapon || !(candidate.getItem() instanceof WeaponBatteryItem)", "reload battery type/isolation guard")
    need(weapon, 'candidate.is(ModItems.get("energy_pack").get())', "Energy Pack reload path")
    need(weapon, "Mth.clamp(weapon.getOrCreateTag().getInt(ENERGY_TAG), 0, getCapacity(weapon))", "capacity-aware weapon energy read")

    need(system, "module.getItem() instanceof WeaponBatteryItem", "battery module validation")

    need(station, "gun.setEnergyStored(weapon, gun.getEnergyStored(weapon));", "final packed-capacity clamp")
    need(station, "packModulesIntoCurrentWeapon();", "station close/drop module persistence")
    need(menu, "station.packModulesInto(stack);", "station weapon take persistence")

    if errors:
        print(f"WEAPON CONSISTENCY FAILED: {len(errors)} issue(s)")
        for error in errors:
            print(f" - {error}")
        return 1
    print("WEAPON CONSISTENCY PASSED: firing, reload isolation, module persistence and capacity clamp contracts agree")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
