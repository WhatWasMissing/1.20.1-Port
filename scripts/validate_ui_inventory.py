"""Verify that the UI audit documents every discovered client screen and menu."""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
AUDIT = ROOT / "docs/ui/UI_AUDIT.md"
JAVA = ROOT / "src/main/java/matteroverdrive"

screen_files = sorted((JAVA / "client/screen").glob("*.java"))
screens = []
for path in screen_files:
    text = path.read_text(encoding="utf-8")
    if re.search(r"class\s+\w+\s+extends\s+(?:Screen|AbstractContainerScreen)", text):
        screens.append(path.stem)

menus = []
for path in sorted((JAVA / "menu").glob("*.java")):
    if re.search(r"class\s+\w+\s+extends\s+(?:AbstractContainerMenu|Menu)", path.read_text(encoding="utf-8")):
        menus.append(path.stem)

audit = AUDIT.read_text(encoding="utf-8")
missing_screens = [name for name in screens if name not in audit]
missing_menus = [name for name in menus if name not in audit]
if missing_screens or missing_menus:
    print("UI INVENTORY FAIL")
    if missing_screens:
        print("Screens missing from UI_AUDIT.md: " + ", ".join(missing_screens))
    if missing_menus:
        print("Menus missing from UI_AUDIT.md: " + ", ".join(missing_menus))
    sys.exit(1)

print(f"UI inventory passed ({len(screens)} screens, {len(menus)} menus documented)")
