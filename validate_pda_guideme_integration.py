#!/usr/bin/env python3
"""Static gate for the Matter Overdrive PDA / GuideME responsibility split."""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parent

FILES = {
    "opener": ROOT / "src/main/java/matteroverdrive/client/ClientDataPadOpener.java",
    "screen": ROOT / "src/main/java/matteroverdrive/client/screen/DataPadScreen.java",
    "docs": ROOT / "src/main/java/matteroverdrive/client/ClientDocumentationOpener.java",
    "compat": ROOT / "src/main/java/matteroverdrive/client/GuideMeCompatEvents.java",
    "packet": ROOT / "src/main/java/matteroverdrive/network/DataPadOpenPacket.java",
    "network": ROOT / "src/main/java/matteroverdrive/network/ModNetwork.java",
    "saved": ROOT / "src/main/java/matteroverdrive/world/StructureLoreSavedData.java",
    "catalog": ROOT / "src/main/java/matteroverdrive/world/StructureLoreCatalog.java",
    "events": ROOT / "src/main/java/matteroverdrive/event/StructureLoreEvents.java",
    "item": ROOT / "src/main/java/matteroverdrive/item/DocumentationItem.java",
}

errors = []
text = {}
for name, path in FILES.items():
    if not path.exists():
        errors.append(f"missing required file: {path.relative_to(ROOT)}")
        text[name] = ""
    else:
        text[name] = path.read_text(encoding="utf-8")

# The PDA must never be replaced by GuideME.
if "GuideMeCompat" in text["opener"]:
    errors.append("ClientDataPadOpener still references GuideME; the PDA must always open independently")
if "new DataPadScreen(history, loreMask)" not in text["opener"]:
    errors.append("ClientDataPadOpener does not open the lore-aware PDA screen")

# Server-authoritative lore archive sync.
for needle in ("int loreMask", "writeVarInt(packet.loreMask", "ClientDataPadOpener.open(packet.history, packet.loreMask"):
    if needle not in text["packet"]:
        errors.append(f"DataPadOpenPacket missing lore sync marker: {needle}")
if "StructureLoreSavedData.get" not in text["network"] or ".mask(p.getUUID())" not in text["network"]:
    errors.append("ModNetwork does not populate PDA lore state from StructureLoreSavedData")
if "public int mask(UUID player)" not in text["saved"]:
    errors.append("StructureLoreSavedData does not expose its stable archive mask")

# Canonical, non-linear archive must have exactly sixteen entries.
record_count = len(re.findall(r'\br\("', text["catalog"]))
if record_count != 16:
    errors.append(f"StructureLoreCatalog must contain exactly 16 records; found {record_count}")
for needle in ("archiveIndex", "chapter", "facility", "structureKey", "ALL_RECORDS_MASK"):
    if needle not in text["catalog"]:
        errors.append(f"StructureLoreCatalog missing canonical metadata: {needle}")
if "StructureLoreCatalog.records()" not in text["events"]:
    errors.append("StructureLoreEvents is not using the shared canonical catalog")
if 'putInt("ArchiveIndex"' not in text["events"]:
    errors.append("physical recovered dossiers do not store canonical ArchiveIndex")

# PDA surfaces gameplay state and lore, while documentation remains separate.
for needle in ("DATA BANK", "FACILITIES", "RESEARCH", "OPERATIONS", "CONTRACTS", "SCAN LOG"):
    if needle not in text["screen"]:
        errors.append(f"PDA missing player-facing section: {needle}")
if "StructureLoreCatalog" not in text["screen"]:
    errors.append("PDA does not consume the shared lore catalog")
if "ClientDocumentationOpener.openTechnicalManual()" not in text["screen"]:
    errors.append("PDA lacks the Technical Manual bridge")

# GuideME remains first-class but optional, with bundled fallback.
for needle in ("GuideMeCompatEvents.openGuide()", "new DocumentationScreen(documentId)", "openTechnicalManual"):
    if needle not in text["docs"]:
        errors.append(f"technical manual bridge missing marker: {needle}")
for needle in ("isAvailable()", "openGuide()", "Class.forName(\"guideme.Guide\")"):
    if needle not in text["compat"]:
        errors.append(f"GuideME optional bridge missing marker: {needle}")
if "SYSTEM_GUIDE(2" not in text["item"]:
    errors.append("standalone System Guide item is no longer registered")

# Retired feature must not creep back into the new PDA integration.
for name in ("opener", "screen", "docs", "catalog"):
    if "star map" in text[name].lower():
        errors.append(f"retired Star Map reference found in {FILES[name].relative_to(ROOT)}")

if errors:
    print("PDA / GuideME integration validation: FAIL")
    for error in errors:
        print(f" - {error}")
    sys.exit(1)

print("PDA / GuideME integration validation: PASS")
print(" - PDA remains independent of GuideME")
print(" - 16-record server-authoritative lore archive is synced")
print(" - GuideME remains accessible through Technical Manual with bundled fallback")
print(" - research, operations, contracts and scan history remain in the PDA")
