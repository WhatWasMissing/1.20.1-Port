#!/usr/bin/env python3
"""Validate local Markdown links in the bundled Matter Overdrive guide."""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/matteroverdrive/guides/matteroverdrive/guide"
LINK = re.compile(r"\[[^\]]+\]\(([^)#\s]+)(?:#[^)\s]+)?\)")
missing = []
for source in sorted(ROOT.glob("*.md")):
    for target in LINK.findall(source.read_text(encoding="utf-8")):
        if target.startswith(("http://", "https://", "mailto:")):
            continue
        resolved = (source.parent / target).resolve()
        if not resolved.is_relative_to(ROOT.resolve()) or not resolved.is_file():
            missing.append(f"{source.name} -> {target}")
if missing:
    print("GUIDE LINK VALIDATION FAILED")
    print("\n".join(missing))
    sys.exit(1)
print(f"GUIDE LINK VALIDATION PASSED: {len(list(ROOT.glob('*.md')))} pages")
