#!/usr/bin/env python3
"""Validate Minecraft 1.20.1 block-model element bounds.

Minecraft's vanilla BlockElement deserializer accepts element coordinates only
within [-16, 32]. Invalid values otherwise survive normal JSON parsing and are
reported later during client model baking, which makes them easy to miss in CI.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

MIN_COORD = -16.0
MAX_COORD = 32.0
ROOT = Path(__file__).resolve().parents[1]
MODEL_ROOT = ROOT / "src" / "main" / "resources" / "assets" / "matteroverdrive" / "models" / "block"


def fail(message: str) -> None:
    print(f"MODEL BOUNDS ERROR: {message}", file=sys.stderr)


def main() -> int:
    if not MODEL_ROOT.is_dir():
        fail(f"model directory does not exist: {MODEL_ROOT}")
        return 2

    errors = 0
    checked_models = 0
    checked_elements = 0

    for path in sorted(MODEL_ROOT.rglob("*.json")):
        checked_models += 1
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except Exception as exc:
            fail(f"{path.relative_to(ROOT)}: invalid JSON: {exc}")
            errors += 1
            continue

        elements = data.get("elements", [])
        if not isinstance(elements, list):
            continue

        for index, element in enumerate(elements):
            if not isinstance(element, dict):
                continue
            if "from" not in element or "to" not in element:
                continue

            start = element["from"]
            end = element["to"]
            checked_elements += 1

            if not (
                isinstance(start, list)
                and isinstance(end, list)
                and len(start) == 3
                and len(end) == 3
                and all(isinstance(v, (int, float)) for v in start + end)
            ):
                fail(f"{path.relative_to(ROOT)} element {index}: malformed from/to vectors")
                errors += 1
                continue

            for axis, (lo, hi) in zip("xyz", zip(start, end)):
                if lo < MIN_COORD or lo > MAX_COORD:
                    fail(
                        f"{path.relative_to(ROOT)} element {index} from.{axis}={lo} "
                        f"outside [{MIN_COORD:g}, {MAX_COORD:g}]"
                    )
                    errors += 1
                if hi < MIN_COORD or hi > MAX_COORD:
                    fail(
                        f"{path.relative_to(ROOT)} element {index} to.{axis}={hi} "
                        f"outside [{MIN_COORD:g}, {MAX_COORD:g}]"
                    )
                    errors += 1
                if lo > hi:
                    fail(
                        f"{path.relative_to(ROOT)} element {index} axis {axis}: "
                        f"from={lo} exceeds to={hi}"
                    )
                    errors += 1

    if errors:
        print(
            f"MODEL BOUNDS VERIFICATION FAILED: {errors} problem(s), "
            f"{checked_models} model(s), {checked_elements} element(s)",
            file=sys.stderr,
        )
        return 1

    print(
        f"MODEL BOUNDS VERIFICATION PASSED: {checked_models} model(s), "
        f"{checked_elements} element(s); allowed range [{MIN_COORD:g}, {MAX_COORD:g}]"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
