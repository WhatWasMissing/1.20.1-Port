#!/usr/bin/env python3
"""Verify runtime-critical Native Destiny classes survive the final JAR packaging step."""
from __future__ import annotations

import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JARS = sorted((ROOT / "build" / "libs").glob("matteroverdrive-*.jar"),
              key=lambda path: path.stat().st_mtime, reverse=True)

if not JARS:
    print("WEAPON PACKAGING VALIDATION FAILED: no built Matter Overdrive JAR found")
    raise SystemExit(1)

required = {
    "matteroverdrive/item/weapon/NativeDestinyWeaponItem.class",
    # `reloadAnimation` uses the compiler-generated enum-switch helper. Its
    # absence caused a client crash while reloading in the September test run.
    "matteroverdrive/item/weapon/NativeDestinyWeaponItem$2.class",
}

with zipfile.ZipFile(JARS[0]) as archive:
    contents = set(archive.namelist())

missing = sorted(required - contents)
if missing:
    print("WEAPON PACKAGING VALIDATION FAILED:")
    for name in missing:
        print(f"  missing runtime class: {name}")
    raise SystemExit(1)

print(f"WEAPON PACKAGING VALIDATION PASSED: {JARS[0].name} contains reload runtime classes")
