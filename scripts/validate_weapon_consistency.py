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
    "native_item": ROOT / "src/main/java/matteroverdrive/item/weapon/NativeDestinyWeaponItem.java",
    "destiny_profile": ROOT / "src/main/java/matteroverdrive/item/weapon/NativeDestinyWeaponProfile.java",
    "destiny_sounds": ROOT / "src/main/java/matteroverdrive/registry/ModDestinySounds.java",
}
ACTUAL_DESTINY_AUDIO = {
    "ace_fire.ogg", "ace_fire_3p.ogg", "ace_reload.ogg",
    "hawkmoon_draw.ogg", "hawkmoon_fire.ogg", "hawkmoon_fire_3p.ogg", "hawkmoon_reload.ogg",
    "khvostov_draw.ogg", "khvostov_fire.ogg", "khvostov_fire_3p.ogg", "khvostov_reload.ogg",
    "last_word_draw.ogg", "last_word_fire.ogg", "last_word_fire_3p.ogg", "last_word_reload.ogg",
    "mida_multi_draw.ogg", "mida_multi_fire.ogg", "mida_multi_fire_3p.ogg", "mida_multi_reload.ogg",
    "montecarlo_fire.ogg", "montecarlo_fire_3p.ogg", "montecarlo_reload.ogg",
    "sim_draw.ogg", "sim_fire.ogg", "sim_fire_3p.ogg", "sim_reload.ogg",
    "s_regime_fire.ogg", "s_regime_fire_3p.ogg", "s_regime_reload.ogg",
    "thorn_draw.ogg", "thorn_fire.ogg", "thorn_fire_3p.ogg", "thorn_reload.ogg",
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


def destiny_profile_event_ids(profile_source):
    """Return sound arguments, excluding each enum entry's item id."""
    event_ids = set()
    for line in profile_source.splitlines():
        if not re.match(r'^\s*[A-Z0-9_]+\("', line):
            continue
        values = re.findall(r'"(destiny_[a-z0-9_]+)"', line)
        event_ids.update(values[1:])
    return event_ids


def main():
    weapon = read("weapon")
    system = read("system")
    station = read("station")
    menu = read("menu")
    client = read("client")
    native_renderer = read("native_renderer")
    native_library = read("native_library")
    native_item = read("native_item")
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
    need(native_library, 'frame.has("post")', "GunPack post-keyframe animation support")
    need(native_item, "thirdPersonFireSound()", "native Destiny third-person fire routing")
    need(native_item, "playFireSound(level, shooter, volume, pitch)", "native Destiny perspective fire audio")
    need(native_item, "supportsModule(WeaponModuleItem module)", "native Destiny module compatibility")
    need(native_item, "WeaponSystem.energyMultiplier(weapon)", "native Destiny energy module scaling")
    need(native_item, "WeaponSystem.damageMultiplier(weapon)", "native Destiny damage module scaling")
    need(native_item, "WeaponSystem.getColor(weapon)", "native Destiny colour module rendering")

    actual_audio_dir = ROOT / "src/main/resources/assets/matteroverdrive/sounds/destiny/actual"
    for filename in ACTUAL_DESTINY_AUDIO:
        path = actual_audio_dir / filename
        if not path.is_file() or path.stat().st_size == 0:
            errors.append(f"missing exact Destiny GunPack audio: {path.relative_to(ROOT)}")

    expanded_audio = sorted(actual_audio_dir.glob("destiny_*.ogg"))
    for path in expanded_audio:
        if path.stat().st_size == 0:
            errors.append(f"empty expanded Destiny GunPack audio: {path.relative_to(ROOT)}")

    profile_ids = re.findall(r'^\s*[A-Z0-9_]+\("([^"\\]+)"', profile, re.MULTILINE)
    if len(profile_ids) != 49:
        errors.append(f"expected 49 imported Destiny weapon profiles, found {len(profile_ids)}")
    for weapon_id in profile_ids:
        for relative in (
                f"src/main/resources/assets/matteroverdrive/native_destiny/geometry/{weapon_id}.geo.json",
                f"src/main/resources/assets/matteroverdrive/native_destiny/animations/{weapon_id}.animation.json",
                f"src/main/resources/assets/matteroverdrive/native_destiny/transforms/{weapon_id}.json",
                f"src/main/resources/assets/matteroverdrive/textures/native_destiny/{weapon_id}.png"):
            if not (ROOT / relative).is_file():
                errors.append(f"missing imported Destiny asset: {relative}")
        item_model = ROOT / "src/main/resources/assets/matteroverdrive/models/item" / f"{weapon_id}.json"
        if not item_model.is_file():
            errors.append(f"missing imported Destiny item model: {item_model.relative_to(ROOT)}")

    recipe_dir = ROOT / "src/main/resources/data/matteroverdrive/recipes"
    destiny_recipes = sorted(recipe_dir.glob("destiny_*.json"))
    if len(destiny_recipes) != 35:
        errors.append(f"expected 35 additional Destiny recipes, found {len(destiny_recipes)}")
    for recipe_path in destiny_recipes:
        try:
            recipe = json.loads(recipe_path.read_text(encoding="utf-8"))
            result_id = recipe.get("result", {}).get("item")
            expected_id = f"matteroverdrive:{recipe_path.stem}"
            if result_id != expected_id or recipe_path.stem not in profile_ids:
                errors.append(f"Destiny recipe result is not connected to its profile: {recipe_path.relative_to(ROOT)}")
        except Exception as exc:
            errors.append(f"cannot parse Destiny recipe {recipe_path.relative_to(ROOT)}: {exc}")

    sounds_path = ROOT / "src/main/resources/assets/matteroverdrive/sounds.json"
    try:
        sounds = json.loads(sounds_path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"cannot parse Destiny sounds resource: {exc}")
        sounds = {}
    registered_ids = set(re.findall(r'register\("([^"\\]+)"\)', destiny_sounds))
    profile_sound_ids = destiny_profile_event_ids(profile)
    for sound_id in registered_ids | profile_sound_ids:
        if sound_id not in sounds:
            errors.append(f"registered Destiny sound has no sounds.json entry: {sound_id}")
    for path in expanded_audio:
        if path.stem not in sounds:
            errors.append(f"expanded Destiny audio has no sounds.json entry: {path.stem}")

    if errors:
        print(f"WEAPON CONSISTENCY FAILED: {len(errors)} issue(s)")
        for error in errors:
            print(f" - {error}")
        return 1
    print("WEAPON CONSISTENCY PASSED: firing, reload isolation, module persistence and capacity clamp contracts agree")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
