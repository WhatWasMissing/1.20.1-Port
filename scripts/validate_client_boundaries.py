#!/usr/bin/env python3
"""Guard client-only event subscribers from dedicated-server class loading."""
from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1] / "src/main/java/matteroverdrive/client"
bad = []
markers = ("RenderGuiOverlayEvent", "RenderLevelStageEvent", "GuiGraphics", "KeyMapping")
for source in root.rglob("*.java"):
    text = source.read_text(encoding="utf-8")
    if any(marker in text for marker in markers) and "EventBusSubscriber" in text:
        if "value = Dist.CLIENT" not in text:
            bad.append(str(source.relative_to(root)))
if bad:
    print("CLIENT BOUNDARY VALIDATION FAILED")
    print("\n".join(bad))
    sys.exit(1)
print("CLIENT BOUNDARY VALIDATION PASSED: GUI/render/input subscribers are client-only")
