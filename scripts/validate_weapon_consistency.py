#!/usr/bin/env python3
"""Static regression gate for Matter Overdrive energy-weapon state invariants."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FILES = {
    "weapon": ROOT / "src/main/java/matteroverdrive/item/weapon/EnergyWeaponItem.java",
    "system": ROOT / "src/main/java/matteroverdrive/item/weapon/WeaponSystem.java",
    "station": ROOT / "src/main/java/matteroverdrive/blockentity/WeaponStationBlockEntity.java",
    "menu": ROOT / "src/main/java/matteroverdrive/menu/WeaponStationMenu.java",
    "client": ROOT / "src/main/java/matteroverdrive/client/WeaponClientEffects.java",
    "native_renderer": ROOT / "src/main/java/matteroverdrive/client/NativeDestinyWeaponRenderer.java",
    "native_library": ROOT / "src/main/java/matteroverdrive/client/NativeDestinyVisualLibrary.java",
    "destiny_profile": ROOT / "src/main/java/matteroverdrive/item/weapon/NativeDestinyWeaponProfile.java",
    "destiny_sounds": ROOT / "src/main/java/matteroverdrive/registry/ModDestinySounds.java",
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
    client = read("client")
    native_renderer = read("native_renderer")
    native_library = read("native_library")
    profile = read("destiny_profile")
    destiny_sounds = read("destiny_sounds")

    need(weapon, "if (!hasEnoughEnergy(weapon, shooter, energyCost) && !tryReload(weapon, shooter, energyCost))", "pre-fire energy gate")
    need(weapon, "drainEnergy(weapon, shooter, energyCost);", "shot energy consumption")
    need(weapon, "candidate == weapon || !(candidate.getItem() instanceof WeaponBatteryItem)", "reload battery type/isolation guard")
    need(weapon, 'candidate.is(ModItems.get("energy_pack").get())', "Energy Pack reload path")
    need(weapon, "Mth.clamp(weapon.getOrCreateTag().getInt(ENERGY_TAG), 0, getCapacity(weapon))", "capacity-aware weapon energy read")

    need(system, "module.getItem() instanceof WeaponBatteryItem", "battery module validation")

    need(station, "gun.setEnergyStored(weapon, gun.getEnergyStored(weapon));", "final packed-capacity clamp")
    need(station, "packModulesIntoCurrentWeapon();", "station close/drop module persistence")
    need(menu, "station.packModulesInto(stack);", "station weapon take persistence")

    need(client, "RenderPlayerEvent.Pre", "third-person weapon render hook")
    need(client, "HumanoidModel.ArmPose.EMPTY", "third-person generic use-pose suppression")
    need(client, "-0.105D * aimed", "bounded first-person aim offset")
    need(native_renderer, "applyDisplayTransform(poseStack, displayContext, visual.displayTransform())", "native Destiny display transform")
    need(native_renderer, "poseStack.scale(transform.scale(), transform.scale(), transform.scale())",
         "native Destiny display scale")
    need(native_library, "native_destiny/geometry/", "native Destiny geometry resource loader")
    need(native_library, "native_destiny/animations/", "native Destiny animation resource loader")
    need(native_library, 'geometryRoot.getAsJsonArray("minecraft:geometry")', "Bedrock geometry parser")

    profile_ids = re.findall(r'^\s*[A-Z0-9_]+\("([^"\\]+)"', profile, re.MULTILINE)
    if len(profile_ids) != 14:
        errors.append(f"expected 14 imported Destiny weapon profiles, found {len(profile_ids)}")
    for weapon_id in profile_ids:
        for relative in (
                f"src/main/resources/assets/matteroverdrive/native_destiny/geometry/{weapon_id}.geo.json",
                f"src/main/resources/assets/matteroverdrive/native_destiny/animations/{weapon_id}.animation.json",
                f"src/main/resources/assets/matteroverdrive/native_destiny/transforms/{weapon_id}.json",
                f"src/main/resources/assets/matteroverdrive/textures/native_destiny/{weapon_id}.png"):
            if not (ROOT / relative).is_file():
                errors.append(f"missing imported Destiny asset: {relative}")

    sounds_path = ROOT / "src/main/resources/assets/matteroverdrive/sounds.json"
    try:
        sounds = json.loads(sounds_path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"cannot parse Destiny sounds resource: {exc}")
        sounds = {}
    for sound_id in re.findall(r'register\("([^"\\]+)"\)', destiny_sounds):
        if sound_id not in sounds:
            errors.append(f"registered Destiny sound has no sounds.json entry: {sound_id}")

    if errors:
        print(f"WEAPON CONSISTENCY FAILED: {len(errors)} issue(s)")
        for error in errors:
            print(f" - {error}")
        return 1
    print("WEAPON CONSISTENCY PASSED: firing, reload isolation, module persistence and capacity clamp contracts agree")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
