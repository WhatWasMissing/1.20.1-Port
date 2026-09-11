#!/usr/bin/env python3
"""Export the complete authored PDA voice-generation queue.

The packaged voice_bank_manifest.json remains the stable core/facility/field-log bank.
Technology discovery lines are authored in TechnologyLoreCatalog.java so they stay next to
their codex entries. This exporter merges both sources into one JSON queue for local/neural
voice production without duplicating 70+ technology transcripts by hand.
"""
from __future__ import annotations

import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[2]
CORE_MANIFEST = ROOT / "src/main/resources/assets/matteroverdrive/pda_voice/voice_bank_manifest.json"
TECH_CATALOG = ROOT / "src/main/java/matteroverdrive/world/TechnologyLoreCatalog.java"
OUT = Path(__file__).resolve().parent / "voice_generation_queue.json"

STRING = r'"(?:\\.|[^"\\])*"'
ADD_START = re.compile(r'^\s*add\(', re.MULTILINE)
STRING_RE = re.compile(STRING)


def java_string(token: str) -> str:
    # Java strings used by the catalogue intentionally stay within JSON-compatible escapes.
    return json.loads(token)


def technology_lines(source: str) -> list[dict[str, str]]:
    result: list[dict[str, str]] = []
    for match in ADD_START.finditer(source):
        start = match.start()
        end = source.find(");", start)
        if end < 0:
            raise ValueError(f"unterminated add(...) near offset {start}")
        block = source[start:end + 2]
        strings = [java_string(token) for token in STRING_RE.findall(block)]
        if len(strings) < 7:
            raise ValueError(f"technology add(...) has fewer than 7 string arguments: {block[:120]!r}")
        tech_id = strings[0]
        voice_text = strings[6]
        result.append({"id": "tech_" + tech_id.replace(".", "_").replace("-", "_"), "text": voice_text})
    return result


def main() -> int:
    if not CORE_MANIFEST.is_file() or not TECH_CATALOG.is_file():
        print("ERROR: run this script from an intact Matter Overdrive source checkout", file=sys.stderr)
        return 2

    core = json.loads(CORE_MANIFEST.read_text(encoding="utf-8"))
    core_lines = core.get("lines", [])
    tech_lines = technology_lines(TECH_CATALOG.read_text(encoding="utf-8"))
    lines = [*core_lines, *tech_lines]
    ids = [entry.get("id", "") for entry in lines]
    if any(not value for value in ids):
        raise ValueError("voice queue contains an empty ID")
    if len(ids) != len(set(ids)):
        duplicates = sorted({value for value in ids if ids.count(value) > 1})
        raise ValueError("duplicate voice IDs: " + ", ".join(duplicates))

    payload = {
        "version": 1,
        "generated": True,
        "core_manifest": str(CORE_MANIFEST.relative_to(ROOT)).replace("\\", "/"),
        "technology_catalog": str(TECH_CATALOG.relative_to(ROOT)).replace("\\", "/"),
        "processing_reference": core.get("processing_reference", "ICARUS approved synthetic VA preset 2026-09-11"),
        "primary_format": "wav",
        "fallback_order": core.get("fallback_order", ["processed_neural_va", "local_os_tts", "minecraft_narrator", "caption"]),
        "counts": {"core_facility_field_lore": len(core_lines), "technology": len(tech_lines), "total": len(lines)},
        "lines": lines,
    }
    OUT.write_text(json.dumps(payload, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"Wrote {OUT.relative_to(ROOT)}")
    print(f"  core/facility/field-lore={len(core_lines)}")
    print(f"  technology={len(tech_lines)}")
    print(f"  total={len(lines)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
