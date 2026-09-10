#!/usr/bin/env python3
"""Static regression gate for Weapon Renderer 2.0 first-person contracts."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FILES = {
    "effects": ROOT / "src/main/java/matteroverdrive/client/WeaponClientEffects.java",
    "renderer": ROOT / "src/main/java/matteroverdrive/client/WeaponItemRenderer.java",
    "profile": ROOT / "src/main/java/matteroverdrive/client/WeaponRenderProfile.java",
    "weapon": ROOT / "src/main/java/matteroverdrive/item/weapon/EnergyWeaponItem.java",
    "native_renderer": ROOT / "src/main/java/matteroverdrive/client/NativeDestinyWeaponRenderer.java",
    "native_library": ROOT / "src/main/java/matteroverdrive/client/NativeDestinyVisualLibrary.java",
    "native_item": ROOT / "src/main/java/matteroverdrive/item/weapon/NativeDestinyWeaponItem.java",
    "destiny_items": ROOT / "src/main/java/matteroverdrive/registry/ModDestinyItems.java",
}
MODEL_IDS = ("phaser", "phaser_rifle", "ion_sniper", "plasma_shotgun")
NATIVE_IDS = (
    "destiny_aceofspades", "destiny_chaosdogma", "destiny_eyasluna",
    "destiny_hawkmoon", "destiny_khvostov7g02", "destiny_marshala1",
    "destiny_midamultitool", "destiny_montecarlo", "destiny_proximacentauriii",
    "destiny_sleepersimulant", "destiny_surosregime", "destiny_thelastword",
    "destiny_thorn", "destiny_traxcallum1",
)
errors: list[str] = []


def read(name: str) -> str:
    path = FILES[name]
    try:
        return path.read_text(encoding="utf-8")
    except OSError as exc:
        errors.append(f"cannot read {path.relative_to(ROOT)}: {exc}")
        return ""


def need(text: str, marker: str, label: str) -> None:
    if marker not in text:
        errors.append(f"missing {label}: {marker}")


def check_models() -> None:
    for model_id in MODEL_IDS:
        path = ROOT / f"src/main/resources/assets/matteroverdrive/models/item/{model_id}.json"
        try:
            obj = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError) as exc:
            errors.append(f"cannot parse {path.relative_to(ROOT)}: {exc}")
            continue
        if obj.get("loader") != "forge:obj":
            errors.append(f"{model_id} no longer uses the expected Forge OBJ loader")
        display = obj.get("display", {})
        fixed = display.get("fixed", {})
        if fixed.get("rotation") != [0, 180, 0]:
            errors.append(f"{model_id} FIXED context must retain [0, 180, 0] for Renderer 2.0")
        first = display.get("firstperson_righthand", {})
        if first.get("rotation") != [3, 185, 0]:
            errors.append(f"{model_id} lost recovered legacy first-person rotation")
        if first.get("translation") != [2.08, -2.88, -8.8]:
            errors.append(f"{model_id} lost recovered legacy first-person translation")


def main() -> int:
    effects = read("effects")
    renderer = read("renderer")
    profile = read("profile")
    weapon = read("weapon")
    native_renderer = read("native_renderer")
    native_library = read("native_library")
    native_item = read("native_item")
    destiny_items = read("destiny_items")

    need(effects, "event.getHand() == InteractionHand.OFF_HAND", "off-hand suppression")
    need(effects, "event.setCanceled(true);", "owned first-person hand cancellation")
    need(effects, "firstPersonRenderer().renderFirstPerson", "dedicated first-person renderer dispatch")
    need(effects, "pose.translate(0.13D, -0.18D, -0.55D);", "recovered hip translation")
    need(effects, "pose.translate(-0.13D * aimed, 0.04D * aimed, -0.30D * aimed);", "recovered ADS delta")
    need(effects, "previousCharge", "interpolated charge state")
    need(effects, "event.getEquipProgress()", "equip animation input")
    need(effects, "event.getSwingProgress()", "swing animation input")

    need(renderer, "public void renderFirstPerson", "first-person renderer entry point")
    need(renderer, "ItemDisplayContext.FIXED", "neutral fixed-context view-model render")
    need(renderer, "WeaponSystem.SIGHTS_SLOT", "mounted optic render path")
    need(renderer, "HOLO_SIGHTS", "Holo Sights render support")
    need(renderer, "SNIPER_SCOPE", "Sniper Scope render support")

    need(profile, "moveBob", "per-weapon movement bob tuning")
    need(profile, "chargeBack", "per-weapon charge tuning")
    need(profile, "recoilPitch", "per-weapon recoil tuning")

    need(weapon, "new matteroverdrive.client.WeaponItemRenderer()", "EnergyWeaponItem client renderer registration")
    need(renderer, "nativeDestinyRenderer().renderNative", "native Destiny first-person renderer dispatch")
    need(renderer, "nativeDestinyRenderer().renderByItem", "native Destiny item-context renderer dispatch")
    need(native_renderer, "NativeDestinyVisualLibrary.get", "native visual lookup")
    need(native_library, "weapons_00.b64", "staged native geometry bundle part 00")
    need(native_library, "weapons_01.b64", "staged native geometry bundle part 01")
    need(native_item, "transferReloadEnergy", "native weapon battery/energy-pack reload path")
    need(destiny_items, "NativeDestinyWeaponProfile.values()", "all native Destiny profiles registered")
    for native_id in NATIVE_IDS:
        path = ROOT / ("src/main/resources/assets/matteroverdrive/models/item/" + native_id + ".json")
        try:
            obj = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError) as exc:
            errors.append(f"cannot parse {path.relative_to(ROOT)}: {exc}")
            continue
        if obj.get("parent") != "builtin/entity":
            errors.append(f"{native_id} must use builtin/entity for the native BEWLR")
    check_models()

    if errors:
        print(f"WEAPON RENDERER CONSISTENCY FAILED: {len(errors)} issue(s)")
        for error in errors:
            print(f" - {error}")
        return 1
    print("WEAPON RENDERER CONSISTENCY PASSED: dedicated first-person ownership, legacy pose, optics and animation inputs agree")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
