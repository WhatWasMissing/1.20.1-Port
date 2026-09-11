#!/usr/bin/env python3
"""Normalize the supplied Point Blank Destiny geo/animation JSON into the native loader bundle."""
from __future__ import annotations

import argparse
import base64
import gzip
import json
from pathlib import Path

IDS = (
    "destiny_aceofspades", "destiny_chaosdogma", "destiny_eyasluna", "destiny_hawkmoon",
    "destiny_khvostov7g02", "destiny_marshala1", "destiny_midamultitool", "destiny_montecarlo",
    "destiny_proximacentauriii", "destiny_sleepersimulant", "destiny_surosregime",
    "destiny_thelastword", "destiny_thorn", "destiny_traxcallum1",
)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("target", type=Path)
    args = parser.parse_args()
    geo_root = args.source / "assets/pointblank/geo/item"
    anim_root = args.source / "assets/pointblank/animations/item"
    weapons = {}
    for weapon_id in IDS:
        geo = json.loads((geo_root / f"{weapon_id}.geo.json").read_text(encoding="utf-8"))
        animations = json.loads((anim_root / f"{weapon_id}.animation.json").read_text(encoding="utf-8")).get("animations", {})
        geometries = geo.get("minecraft:geometry", [])
        if len(geometries) != 1:
            raise ValueError(f"{weapon_id}: expected one geometry, found {len(geometries)}")
        weapons[weapon_id] = {"geometry": geometries[0], "animations": animations}
    payload = json.dumps({"weapons": weapons}, separators=(",", ":")).encode("utf-8")
    encoded = base64.b64encode(gzip.compress(payload, compresslevel=9)).decode("ascii")
    args.target.parent.mkdir(parents=True, exist_ok=True)
    args.target.write_text(encoded + "\n", encoding="ascii")
    print(f"Regenerated {len(weapons)}/{len(IDS)} native Destiny visuals")
    print(f"Payload: {len(payload)} bytes; Base64: {len(encoded)} characters")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
