"""Verify every Matter Overdrive sound entry resolves to a bundled OGG."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/matteroverdrive"
events = json.loads((ASSETS / "sounds.json").read_text(encoding="utf-8"))
missing = []
checked = 0
for event, definition in events.items():
    for sound in definition.get("sounds", []) if isinstance(definition, dict) else []:
        name = sound if isinstance(sound, str) else sound.get("name", "")
        if not name.startswith("matteroverdrive:"):
            continue
        checked += 1
        path = ASSETS / "sounds" / (name.split(":", 1)[1] + ".ogg")
        if not path.is_file():
            missing.append(f"{event}: {path.relative_to(ROOT)}")

if missing:
    print("SOUND RESOURCE VERIFICATION FAILED")
    print("\n".join(f"  - {entry}" for entry in missing))
    raise SystemExit(1)
print(f"SOUND RESOURCE VERIFICATION PASSED: {checked} bundled sound references")
