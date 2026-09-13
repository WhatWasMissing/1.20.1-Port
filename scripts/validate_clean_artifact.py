#!/usr/bin/env python3
"""Ensure retired Star Map resources do not survive into the built mod archive."""
from pathlib import Path
import sys
import zipfile

jar = Path(__file__).resolve().parents[1] / "build/libs/matteroverdrive-0.7.jar"
if not jar.is_file():
    print(f"CLEAN ARTIFACT VALIDATION FAILED: missing {jar}")
    sys.exit(1)
with zipfile.ZipFile(jar) as archive:
    names = archive.namelist()
    retired = [name for name in names if name.endswith("guide/starmap.md") or name.endswith("loot_tables/blocks/star_map.json")]
    jei = archive.read("matteroverdrive/compat/jei/MatterOverdriveJeiPlugin.class")
    if retired or b"star_map" in jei or b"starmap" in jei:
        print("CLEAN ARTIFACT VALIDATION FAILED")
        print("\n".join(retired))
        sys.exit(1)
print("CLEAN ARTIFACT VALIDATION PASSED: retired Star Map resources/references absent")
