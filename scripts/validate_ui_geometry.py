"""Conservative static checks for common Forge screen geometry mistakes."""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
SCREEN_DIR = ROOT / "src/main/java/matteroverdrive/client/screen"
BOUNDS = re.compile(
    r"\.bounds\(leftPos\s*\+\s*(\d+)\s*,\s*topPos\s*\+\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)"
)
DIMENSION = re.compile(r"imageWidth\s*=\s*(\d+)|imageHeight\s*=\s*(\d+)")
PAGE_LABELS = re.compile(r"PAGES\s*=\s*\{([^}]+)\}")
TAB_WIDTH = re.compile(
    r"Button\.builder\(Component\.literal\(PAGES\[i\]\).*?\.bounds\([^;]*?,\s*(\d+)\s*,\s*15\s*\)\.build\(\)",
    re.DOTALL,
)

errors = []
checked = 0
for path in sorted(SCREEN_DIR.glob("*.java")):
    text = path.read_text(encoding="utf-8")
    width = height = None
    for match in DIMENSION.finditer(text):
        if match.group(1):
            width = int(match.group(1))
        if match.group(2):
            height = int(match.group(2))
    if width is None or height is None:
        continue
    for match in BOUNDS.finditer(text):
        x, y, w, h = map(int, match.groups())
        checked += 1
        if x < 0 or y < 0 or w <= 0 or h <= 0:
            errors.append(f"{path.name}: invalid widget bounds ({x},{y},{w},{h})")
        if x + w > width or y + h > height:
            errors.append(
                f"{path.name}: widget ({x},{y},{w},{h}) exceeds frame {width}x{height}"
            )

    # Minecraft's default font is variable-width, so use a conservative
    # six-pixel-per-character upper bound plus four pixels of button padding.
    # This catches visibly truncated machine tabs without requiring a client.
    labels_match = PAGE_LABELS.search(text)
    tab_match = TAB_WIDTH.search(text)
    if labels_match and tab_match:
        labels = re.findall(r'"([^"]+)"', labels_match.group(1))
        tab_width = int(tab_match.group(1))
        required = max((len(label) * 6 + 4 for label in labels), default=0)
        if tab_width < required:
            errors.append(
                f"{path.name}: tab width {tab_width}px cannot safely fit "
                f"longest page label ({required}px conservative minimum)"
            )

# Regression guard for the Android inspection defect observed in runtime
# screenshots: descriptions must use a panel-derived, safely clamped width.
android_loadout = (SCREEN_DIR / "AndroidLoadoutScreen.java").read_text(encoding="utf-8")
if "int panelWidth = Math.max(180, width - x - 18);" not in android_loadout:
    errors.append("AndroidLoadoutScreen.java: inspection width is not panel-derived/clamped")
if re.search(r"drawWrapped\([^;]*,\s*230\s*,", android_loadout):
    errors.append("AndroidLoadoutScreen.java: fixed 230px inspection wrapping remains")

android_class = (SCREEN_DIR / "AndroidClassLoadoutScreen.java").read_text(encoding="utf-8")
if "int panelWidth = Math.max(180, width - 21 - x);" not in android_class:
    errors.append("AndroidClassLoadoutScreen.java: inspection width is not derived from its right panel")
if "Math.max(1, mainRight() - x)" in android_class:
    errors.append("AndroidClassLoadoutScreen.java: negative inspection-width regression remains")

if errors:
    print("UI GEOMETRY FAIL")
    print("\n".join(errors))
    sys.exit(1)

print(f"UI geometry checks passed ({checked} literal container-screen widget bounds)")
