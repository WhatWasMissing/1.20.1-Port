#!/usr/bin/env python3
"""Import native Destiny textures and sounds into the Matter Overdrive resource tree."""
from __future__ import annotations

import argparse
import shutil
from pathlib import Path

NATIVE_IDS = (
    "destiny_aceofspades",
    "destiny_chaosdogma",
    "destiny_eyasluna",
    "destiny_hawkmoon",
    "destiny_khvostov7g02",
    "destiny_marshala1",
    "destiny_midamultitool",
    "destiny_montecarlo",
    "destiny_proximacentauriii",
    "destiny_sleepersimulant",
    "destiny_surosregime",
    "destiny_thelastword",
    "destiny_thorn",
    "destiny_traxcallum1",
)

NATIVE_SOUND_IDS = (
    "destiny_khvostov_charge",
    "destiny_khvostov_draw",
    "destiny_khvostov_first",
    "destiny_khvostov_last",
    "destiny_khvostov_reload",
    "destiny_khvostov_unload",
    "destiny_khvostov7g02",
    "destiny_marshala1",
    "destiny_marshal_unload",
    "destiny_marshal_reload",
    "destiny_marshal_charge",
    "destiny_eyasluna",
    "destiny_hawkmoon",
    "destiny_hawkmoon_close",
    "destiny_hawkmoon_draw",
    "destiny_hawkmoon_eject",
    "destiny_hawkmoon_insert",
    "destiny_hawkmoon_open",
    "destiny_hawkmoon_rest",
    "destiny_hawkmoon_up",
    "destiny_surosregime",
    "destiny_surosregime_charge",
    "destiny_surosregime_load",
    "destiny_montecarlo",
    "destiny_montecarlo_unload",
    "destiny_montecarlo_reload",
    "destiny_montecarlo_charge",
    "destiny_traxmallus1_charge",
    "destiny_traxmallus1_reload",
    "destiny_traxmallus1_unload",
    "destiny_traxcallum1",
    "destiny_proximacentauriii",
    "destiny_midamultitool",
    "destiny_midamultitool_unload",
    "destiny_midamultitool_charge",
    "destiny_chaosdogma",
    "destiny_cd_charge",
    "destiny_cd_unload",
    "destiny_cd_reload",
    "destiny_thelastword",
    "destiny_aceofspades",
    "destiny_aos_spin",
    "destiny_aos_open",
    "destiny_aos_insert",
    "destiny_aos_close",
    "destiny_thorn",
    "destiny_thorn_close",
    "destiny_thorn_open",
    "destiny_thorn_reload",
    "destiny_thorn_unload",
    "destiny_sleepersimulant_draw",
    "destiny_sleepersimulant_charge",
    "destiny_sleepersimulant_fire",
    "destiny_sleepersimulant_hit",
    "destiny_sleepersimulant_reload",
    "destiny_sleepersimulant_unload",
)


def find_named(root: Path, filename: str, preferred: tuple[str, ...]) -> Path | None:
    candidates = sorted(root.rglob(filename), key=lambda path: (
        0 if all(part in path.as_posix().lower() for part in preferred) else 1,
        len(path.parts),
        path.as_posix(),
    ))
    return candidates[0] if candidates else None


def copy_asset(source: Path, destination: Path, filename: str, preferred: tuple[str, ...]) -> bool:
    found = find_named(source, filename, preferred)
    if found is None:
        return False
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(found, destination)
    return True


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, required=True,
                        help="Extracted destiny-ext 0.5.1 directory")
    parser.add_argument(
        "--target",
        type=Path,
        default=Path("src/main/resources/assets/matteroverdrive"),
        help="Matter Overdrive resource root (default: %(default)s)",
    )
    parser.add_argument(
        "--strict",
        action="store_true",
        help="Return a failure if any expected native asset is missing",
    )
    args = parser.parse_args()

    source = args.source.resolve()
    target = args.target
    if not source.is_dir():
        parser.error(f"source directory does not exist: {source}")

    missing: list[str] = []
    texture_count = 0
    sound_count = 0

    for native_id in NATIVE_IDS:
        destination = target / "textures" / "native_destiny" / f"{native_id}.png"
        if copy_asset(source, destination, f"{native_id}.png", ("pointblank", "textures", "item")):
            texture_count += 1
        else:
            missing.append(f"texture:{native_id}.png")

    for sound_id in NATIVE_SOUND_IDS:
        destination = target / "sounds" / f"{sound_id}.ogg"
        if copy_asset(source, destination, f"{sound_id}.ogg", ("pointblank", "sounds")):
            sound_count += 1
        else:
            missing.append(f"sound:{sound_id}.ogg")

    print(f"Imported {texture_count}/{len(NATIVE_IDS)} native Destiny textures")
    print(f"Imported {sound_count}/{len(NATIVE_SOUND_IDS)} native Destiny sounds")
    if missing:
        print("Missing native assets:")
        for item in missing:
            print(f" - {item}")
    if missing and args.strict:
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
