#!/usr/bin/env python3
"""Static consistency audit for the Matter Overdrive 1.20.1 port.

The active Java registries are the source of truth. Historical parity inventories are
reference material only. The audit is intentionally Forge-independent so it can run
before Gradle and produce a useful report even when the game cannot be launched.
"""
from __future__ import annotations

import json
import hashlib
import re
import struct
from dataclasses import dataclass, asdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/matteroverdrive"
DATA = ROOT / "src/main/resources/data/matteroverdrive"
REPORT_DIR = ROOT / "build/reports"
BLOCKS_JAVA = ROOT / "src/main/java/matteroverdrive/registry/ModBlocks.java"
OVERHAUL_BLOCKS_JAVA = ROOT / "src/main/java/matteroverdrive/registry/OverhaulContent.java"
ITEMS_JAVA = ROOT / "src/main/java/matteroverdrive/registry/ModItems.java"
EXOTIC_ITEMS_JAVA = ROOT / "src/main/java/matteroverdrive/registry/ModExoticItems.java"
DESTINY_PROFILES_JAVA = ROOT / "src/main/java/matteroverdrive/item/weapon/NativeDestinyWeaponProfile.java"
STRUCTURES_JAVA = ROOT / "src/main/java/matteroverdrive/registry/ModStructures.java"
STRUCTURE_JAVA = ROOT / "src/main/java/matteroverdrive/worldgen/TechnologyFacilityStructure.java"
INVENTORY_JSON = ROOT / "PORT_INVENTORY.json"
LANG_JSON = ASSETS / "lang/en_us.json"
GUIDE_ROOT = ASSETS / "guides/matteroverdrive/guide"
RETIRED_ACTIVE_IDS = {"star_map"}
FACILITY_RELICS = {
    "synthetic_manufacturing_plant": {"OVERCLOCKED_RELAY"},
    "android_command_bunker": {"SWARM_BEACON", "AEGIS_PRISM"},
    "black_site": {"HUNTER_LENS"},
    "matter_refinery": {"NANITE_CROWN"},
    "quantum_relay_station": {"CAPACITOR_HEART"},
    "fusion_research_complex": {"PHASE_ANCHOR"},
}
RELIC_MODEL_DATA = {
    "OVERCLOCKED_RELAY": 1001,
    "SWARM_BEACON": 1002,
    "AEGIS_PRISM": 1003,
    "HUNTER_LENS": 1004,
    "NANITE_CROWN": 1005,
    "CAPACITOR_HEART": 1006,
    "PHASE_ANCHOR": 1007,
}
VANILLA_LOOT_TARGETS = {
    "chests/simple_dungeon", "chests/abandoned_mineshaft", "chests/shipwreck_supply",
    "chests/desert_pyramid", "chests/pillager_outpost", "chests/nether_bridge",
    "chests/stronghold_corridor", "chests/stronghold_crossing", "chests/stronghold_library",
    "chests/jungle_temple", "chests/bastion_treasure", "chests/woodland_mansion",
    "chests/buried_treasure", "chests/end_city_treasure", "chests/ancient_city",
}
NATURAL_SPAWN_MODIFIERS = {
    "rogue_android_spawns.json": "matteroverdrive:rogue_android",
    "ranged_rogue_android_spawns.json": "matteroverdrive:ranged_rogue_android",
    "drone_spawns.json": "matteroverdrive:drone",
    "mutant_scientist_spawns.json": "matteroverdrive:mutant_scientist",
    "assimilator_spawns.json": "matteroverdrive:assimilator",
    "phase_stalker_spawns.json": "matteroverdrive:phase_stalker",
}

@dataclass
class Finding:
    severity: str
    category: str
    message: str

findings: list[Finding] = []
ACTIVE_BLOCK_IDS: set[str] = set()
ACTIVE_BLOCK_ITEM_IDS: set[str] = set()
ACTIVE_ITEM_IDS: set[str] = set()

def add(severity: str, category: str, message: str) -> None:
    findings.append(Finding(severity, category, message))

def read(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except OSError as exc:
        add("error", "source", f"cannot read {path.relative_to(ROOT)}: {exc}")
        return ""

def java_list(text: str, name: str, constructor: str = "List") -> list[str]:
    match = re.search(rf"\b{name}\s*=\s*{constructor}\.of\((.*?)\);", text, re.S)
    if not match:
        add("error", "registry", f"could not parse {name}")
        return []
    return re.findall(r'"([^"\\]+)"', match.group(1))

def registered_blocks(text: str) -> list[str]:
    """Parse block registrations whose item/model resources must be audited."""
    return re.findall(
        r'public\s+static\s+final\s+RegistryObject<Block>\s+\w+\s*='
        r'\s*register\("([^"\\]+)"', text)

def registered_extra_items() -> list[str]:
    exotic = re.findall(r'ITEMS\.register\("([^"\\]+)"', read(EXOTIC_ITEMS_JAVA))
    destiny = re.findall(
        r'^\s*[A-Z0-9_]+\("([^"\\]+)"', read(DESTINY_PROFILES_JAVA), re.MULTILINE)
    direct = re.findall(
        r'public\s+static\s+final\s+RegistryObject<Item>\s+\w+\s*=\s*ITEMS\.register\("([^"\\]+)"',
        read(ITEMS_JAVA))
    return list(dict.fromkeys(exotic + destiny + direct))

def json_file(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        add("error", "json", f"invalid JSON {path.relative_to(ROOT)}: {exc}")
        return None

def all_json_files() -> list[Path]:
    roots = [ASSETS, DATA]
    return sorted(p for root in roots if root.exists() for p in root.rglob("*.json"))

def resolve_model(ref: str) -> Path | None:
    if ref.startswith("minecraft:") or ref.startswith("builtin/") or ref.startswith("item/") or ref.startswith("block/"):
        return None
    if ref.startswith("matteroverdrive:"):
        rel = ref.split(":", 1)[1]
        # Forge OBJ loaders use a resource path with an explicit .obj suffix.
        # Treat that as the actual model resource instead of appending .json;
        # the previous audit reported every valid OBJ as missing JSON.
        if rel.endswith(".obj"):
            return ASSETS / rel
        return ASSETS / "models" / f"{rel}.json"
    return None

def walk_models(node):
    if isinstance(node, dict):
        for key, value in node.items():
            if key == "model" and isinstance(value, str):
                yield value
            else:
                yield from walk_models(value)
    elif isinstance(node, list):
        for value in node:
            yield from walk_models(value)

def walk_texture_values(node):
    if isinstance(node, dict):
        textures = node.get("textures")
        if isinstance(textures, dict):
            for value in textures.values():
                if isinstance(value, str):
                    yield value
        for value in node.values():
            yield from walk_texture_values(value)
    elif isinstance(node, list):
        for value in node:
            yield from walk_texture_values(value)

def walk_keyed_values(node, keys: set[str]):
    if isinstance(node, dict):
        for key, value in node.items():
            if key in keys:
                yield key, value
            else:
                yield from walk_keyed_values(value, keys)
    elif isinstance(node, list):
        for value in node:
            yield from walk_keyed_values(value, keys)

def check_active_data_refs(path: Path, obj) -> None:
    """Check data references whose namespace is an active item/block registry id."""
    relative = path.relative_to(DATA).as_posix()
    if relative.startswith("recipes/"):
        keys, valid = {"item"}, ACTIVE_ITEM_IDS
    elif relative.startswith("loot_tables/"):
        keys, valid = {"name"}, ACTIVE_ITEM_IDS
    elif relative.startswith("tags/blocks/"):
        keys, valid = {"values"}, ACTIVE_BLOCK_IDS
    elif relative.startswith("tags/items/"):
        keys, valid = {"values"}, ACTIVE_ITEM_IDS
    else:
        return

    for key, value in walk_keyed_values(obj, keys):
        values = value if isinstance(value, list) else [value]
        for ref in values:
            if not isinstance(ref, str) or not ref.startswith("matteroverdrive:"):
                continue
            identifier = ref.split(":", 1)[1]
            if identifier.startswith("#") or "/" in identifier:
                continue
            if identifier not in valid:
                add("error", "reference", f"{path.relative_to(ROOT)} has inactive {key} reference {ref}")

def check_json_and_model_refs() -> None:
    for path in all_json_files():
        obj = json_file(path)
        if obj is None:
            continue
        if path.is_relative_to(DATA):
            check_active_data_refs(path, obj)
        posix = path.as_posix()
        if "assets/matteroverdrive/blockstates" in posix or "assets/matteroverdrive/models" in posix:
            for ref in walk_models(obj):
                target = resolve_model(ref)
                if target is not None and not target.exists():
                    add("error", "model", f"{path.relative_to(ROOT)} references missing model {ref}")
        if "assets/matteroverdrive/models" in posix:
            for ref in walk_texture_values(obj):
                if ref.startswith("#") or ref.startswith("minecraft:"):
                    continue
                if ref.startswith("matteroverdrive:"):
                    rel = ref.split(":", 1)[1]
                    target = ASSETS / "textures" / f"{rel}.png"
                    if not target.exists():
                        add("error", "texture", f"{path.relative_to(ROOT)} references missing texture {ref}")

def check_texture_resources() -> None:
    """Audit raster headers, animation metadata, OBJ sidecars and duplicate assets."""
    texture_root = ASSETS / "textures"
    hashes: dict[str, list[Path]] = {}
    for path in sorted(texture_root.rglob("*.png")):
        try:
            data = path.read_bytes()
        except OSError as exc:
            add("error", "texture", f"cannot read texture {path.relative_to(ROOT)}: {exc}")
            continue
        if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR":
            add("error", "texture", f"invalid PNG header: {path.relative_to(ROOT)}")
        else:
            width, height = struct.unpack(">II", data[16:24])
            if width <= 0 or height <= 0:
                add("error", "texture", f"PNG has invalid dimensions {width}x{height}: {path.relative_to(ROOT)}")
        hashes.setdefault(hashlib.sha256(data).hexdigest(), []).append(path)

    for group in hashes.values():
        if len(group) > 1:
            names = ", ".join(str(path.relative_to(ROOT)) for path in group)
            add("warning", "texture", f"exact duplicate texture assets: {names}")

    for path in sorted(texture_root.rglob("*.png.mcmeta")):
        try:
            json.loads(path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError) as exc:
            add("error", "texture", f"invalid animation metadata {path.relative_to(ROOT)}: {exc}")

    for obj_path in sorted((ASSETS / "models").rglob("*.obj")):
        try:
            lines = obj_path.read_text(encoding="utf-8", errors="replace").splitlines()
        except OSError as exc:
            add("error", "model", f"cannot read OBJ {obj_path.relative_to(ROOT)}: {exc}")
            continue
        for line in lines:
            if not line.strip().lower().startswith("mtllib "):
                continue
            sidecar = obj_path.parent / line.split(None, 1)[1].strip()
            if not sidecar.exists():
                add("error", "model", f"{obj_path.relative_to(ROOT)} references missing material file {sidecar.name}")

    for mtl_path in sorted((ASSETS / "models").rglob("*.mtl")):
        for line in mtl_path.read_text(encoding="utf-8", errors="replace").splitlines():
            parts = line.split()
            if len(parts) < 2 or parts[0].lower() != "map_kd" or parts[1].startswith("#"):
                continue
            ref = parts[1]
            if ref.startswith("matteroverdrive:"):
                ref = ref.split(":", 1)[1]
            elif ":" in ref:
                continue
            target = texture_root / f"{ref.removesuffix('.png')}.png"
            if not target.exists():
                add("error", "texture", f"{mtl_path.relative_to(ROOT)} references missing material texture {parts[1]}")

    java_texture_re = re.compile(r'["/]textures/([^"\\]+\.png)')
    for java_path in sorted((ROOT / "src/main/java").rglob("*.java")):
        source = read(java_path)
        for ref in java_texture_re.findall(source):
            if not (texture_root / ref).exists():
                add("error", "texture", f"{java_path.relative_to(ROOT)} references missing hard-coded texture textures/{ref}")

def check_ae2_contract() -> None:
    """Keep the optional AE2 bridge connected to public, guarded MO inventories."""
    integration = read(ROOT / "src/main/java/matteroverdrive/integration/Ae2IntegrationEvents.java")
    charging = read(ROOT / "src/main/java/matteroverdrive/blockentity/ChargingStationBlockEntity.java")
    matrix = read(ROOT / "src/main/java/matteroverdrive/blockentity/MatterStorageMatrixBlockEntity.java")
    for marker, label, source in (
        ("ForgeCapabilities.ITEM_HANDLER", "AE2 audit item capability", integration),
        ("ModList.get().isLoaded(\"ae2\")", "optional AE2 detection", integration),
        ("AutomationItemHandler", "Charging Station public battery handler", charging),
        ("AutomationItemHandler", "Matter Storage Matrix public cell handler", matrix),
        ("canExtractCell", "Matter Storage Matrix extraction guard", matrix),
    ):
        if marker not in source:
            add("error", "ae2", f"missing {label} contract marker: {marker}")
    guide = read(ASSETS / "guides/matteroverdrive/guide/ae2.md")
    reference = read(ROOT / "docs/reference/AE2_GUIDEME_INTEGRATION.md")
    for marker in ("Charging Station", "Matter Storage Matrix", "/moae2"):
        if marker not in guide or marker not in reference:
            add("error", "ae2", f"AE2 documentation is missing {marker} coverage")

def check_registry_resources() -> tuple[list[str], list[str]]:
    global ACTIVE_BLOCK_IDS, ACTIVE_BLOCK_ITEM_IDS, ACTIVE_ITEM_IDS
    block_text, overhaul_text, item_text = read(BLOCKS_JAVA), read(OVERHAUL_BLOCKS_JAVA), read(ITEMS_JAVA)
    blocks = list(dict.fromkeys(java_list(block_text, "LEGACY_BLOCK_IDS") + registered_blocks(overhaul_text)))
    no_items = set(java_list(block_text, "NO_BLOCK_ITEM", "Set"))
    items = java_list(item_text, "STANDALONE_ITEM_IDS")
    extra_items = registered_extra_items()
    block_items = [block for block in blocks if block not in no_items]
    all_items = block_items + items + extra_items
    ACTIVE_BLOCK_IDS = set(blocks)
    ACTIVE_BLOCK_ITEM_IDS = set(block_items)
    ACTIVE_ITEM_IDS = set(all_items)
    for label, values in (("block", blocks), ("standalone item", items), ("extra item", extra_items), ("item", all_items)):
        for value in sorted({x for x in values if values.count(x) > 1}):
            add("error", "registry", f"duplicate {label} id: {value}")
    for retired in sorted(RETIRED_ACTIVE_IDS):
        if retired in blocks or retired in items:
            add("error", "retired-content", f"retired id {retired!r} is active in Java registries")
    lang = json_file(LANG_JSON)
    if not isinstance(lang, dict):
        lang = {}
    for block in blocks:
        if not (ASSETS / "blockstates" / f"{block}.json").exists():
            add("warning", "resource", f"active block {block} has no blockstate JSON")
        key = f"block.matteroverdrive.{block}"
        if key not in lang:
            add("warning", "localization", f"active block {block} lacks {key}")
        if block not in no_items and not (ASSETS / "models/item" / f"{block}.json").exists():
            add("warning", "resource", f"block item {block} has no item model")
        recipe_id = block.replace(".", "_")
        if block not in no_items and not (DATA / "recipes" / f"{recipe_id}.json").exists():
            add("warning", "recipe", f"active block {block} has no crafting recipe named {recipe_id}")
        if block not in no_items and not (DATA / "loot_tables/blocks" / f"{block}.json").exists():
            add("error", "loot", f"active block {block} has no block loot table")
    for item in items:
        key = f"item.matteroverdrive.{item}"
        if key not in lang and f"item.matteroverdrive.{item}.name" not in lang:
            add("warning", "localization", f"standalone item {item} has no modern localization key")
        if not (ASSETS / "models/item" / f"{item}.json").exists():
            add("warning", "resource", f"standalone item {item} has no item model")
    for item in extra_items:
        key = f"item.matteroverdrive.{item}"
        if key not in lang and f"item.matteroverdrive.{item}.name" not in lang:
            add("warning", "localization", f"registered extra item {item} has no modern localization key")
        if not (ASSETS / "models/item" / f"{item}.json").exists():
            add("warning", "resource", f"registered extra item {item} has no item model")
    return blocks, items

def check_guides() -> None:
    if not GUIDE_ROOT.exists():
        add("warning", "guideme", "GuideME guide root does not exist")
        return
    link_re = re.compile(r"\[[^\]]*\]\(([^)]+)\)")
    retired_re = re.compile(r"\bstar[ _-]?map\b", re.I)
    for path in sorted(GUIDE_ROOT.rglob("*.md")):
        text = read(path)
        if retired_re.search(text):
            add("error", "retired-content", f"active GuideME page mentions retired Star Map: {path.relative_to(ROOT)}")
        for raw in link_re.findall(text):
            href = raw.strip().split("#", 1)[0]
            if not href or "://" in href or href.startswith("#"):
                continue
            target = GUIDE_ROOT / href.lstrip("/") if href.startswith("/") else (path.parent / href).resolve()
            if target.suffix == "" and target.with_suffix(".md").exists():
                target = target.with_suffix(".md")
            if not target.exists():
                add("warning", "guideme", f"broken local link in {path.relative_to(ROOT)}: {raw}")

    block_page = GUIDE_ROOT / "blocks.md"
    if block_page.exists():
        block_text = read(block_page)
        registered = list(dict.fromkeys(
            java_list(read(BLOCKS_JAVA), "LEGACY_BLOCK_IDS")
            + registered_blocks(read(OVERHAUL_BLOCKS_JAVA))))
        no_items = set(java_list(read(BLOCKS_JAVA), "NO_BLOCK_ITEM", "Set"))
        sections = {
            match.group(1): match.group(2)
            for match in re.finditer(
                r"(?ms)^### `([^`]+)`\n(.*?)(?=^### |^## |\Z)", block_text)
        }
        for block in registered:
            heading = f"### `{block}`"
            if heading not in block_text:
                add("error", "guideme", f"Block Reference is missing active block {block}")
                continue
            section = sections.get(block, "")
            if not re.search(r"(?m)^Description:\s+\S", section):
                add("error", "guideme", f"Block Reference is missing a description for {block}")
            if block in no_items:
                continue
            item_link = f'<ItemLink id="matteroverdrive:{block}" />'
            recipe_id = block.replace(".", "_")
            recipe_link = f'<RecipeFor id="matteroverdrive:{recipe_id}" />'
            if item_link not in section:
                add("error", "guideme", f"Block Reference is missing item image link for {block}")
            if recipe_link not in section:
                add("error", "guideme", f"Block Reference is missing recipe link for {block}")
            if not (ASSETS / "models/item" / f"{block}.json").exists():
                add("error", "guideme", f"Block Reference image model is missing for {block}")
            if not (DATA / "recipes" / f"{recipe_id}.json").exists():
                add("error", "guideme", f"Block Reference recipe resource is missing for {block}")
    else:
        add("error", "guideme", "Block Reference page does not exist")

def check_retired_active_refs() -> None:
    """Keep retired strategic content out of active source and bundled content."""
    retired_re = re.compile(r"\bstar[ _-]?map\b|\bstarmap\b", re.IGNORECASE)
    paths: list[Path] = []
    paths.extend(ROOT.joinpath("src/main/java").rglob("*.java"))
    paths.extend(DATA.rglob("*.json"))
    paths.extend(GUIDE_ROOT.rglob("*.md"))
    paths.extend([
        ASSETS / "docs/current_features.txt",
        ASSETS / "docs/system_guide.txt",
    ])
    for path in paths:
        try:
            text = path.read_text(encoding="utf-8")
        except OSError as exc:
            add("error", "source", f"cannot read active retired-content scan target {path.relative_to(ROOT)}: {exc}")
            continue
        if retired_re.search(text):
            add("error", "retired-content", f"active source/resource mentions retired strategic content: {path.relative_to(ROOT)}")

def check_facility_relics() -> None:
    """Verify themed relic drops resolve to the existing passive-protocol item."""
    found: dict[str, str] = {}
    for table_name, expected in FACILITY_RELICS.items():
        path = DATA / "loot_tables/chests/facilities" / f"{table_name}.json"
        table = json_file(path)
        if not isinstance(table, dict):
            continue
        text = json.dumps(table)
        relic_ids = set(re.findall(r'RelicId:\\"([A-Z_]+)\\"', text))
        for relic_id in sorted(relic_ids - expected):
            add("error", "relic-loot", f"{table_name} contains relic {relic_id} outside its themed drop")
        for relic_id in sorted(relic_ids):
            if relic_id in found and found[relic_id] != table_name:
                add("error", "relic-loot", f"Legendary Relic {relic_id} is duplicated in {found[relic_id]} and {table_name}")
            found[relic_id] = table_name
        missing = expected - relic_ids
        for relic_id in sorted(missing):
            add("error", "relic-loot", f"{table_name} is missing themed Legendary Relic {relic_id}")

    for relic_id in sorted(set(RELIC_MODEL_DATA) - set(found)):
        add("error", "relic-loot", f"Legendary Relic {relic_id} is not present in a main facility loot table")

    artifact_model = json_file(ASSETS / "models/item/artifact.json")
    overrides = artifact_model.get("overrides", []) if isinstance(artifact_model, dict) else []
    override_models = {
        entry.get("model"): entry.get("predicate", {}).get("custom_model_data")
        for entry in overrides if isinstance(entry, dict)
    }
    for relic_id, model_data in RELIC_MODEL_DATA.items():
        model_name = f"matteroverdrive:item/relic_{relic_id.lower()}"
        if override_models.get(model_name) != model_data:
            add("error", "relic-resource", f"artifact item model lacks CustomModelData {model_data} override for {relic_id}")
        model_path = ASSETS / "models/item" / f"relic_{relic_id.lower()}.json"
        texture_path = ASSETS / "textures/item" / f"relic_{relic_id.lower()}.png"
        if not model_path.exists():
            add("error", "relic-resource", f"missing relic model for {relic_id}")
        if not texture_path.exists():
            add("error", "relic-resource", f"missing relic texture for {relic_id}")

def check_vanilla_progression() -> None:
    loot_source = read(ROOT / "src/main/java/matteroverdrive/event/VanillaLootEvents.java")
    if "LootTableLoadEvent" not in loot_source or ".addPool" not in loot_source:
        add("error", "vanilla-loot", "vanilla loot injection hook is missing")
    for target in sorted(VANILLA_LOOT_TARGETS):
        if target not in loot_source:
            add("error", "vanilla-loot", f"vanilla loot target is missing from injection map: minecraft:{target}")

    placement_source = read(ROOT / "src/main/java/matteroverdrive/event/LegacyEntityEvents.java")
    for filename, entity_id in NATURAL_SPAWN_MODIFIERS.items():
        path = DATA / "forge/biome_modifier" / filename
        obj = json_file(path)
        if not isinstance(obj, dict) or obj.get("type") != "forge:add_spawns":
            add("error", "natural-spawn", f"invalid natural spawn modifier: {path.relative_to(ROOT)}")
        elif obj.get("spawners", {}).get("type") != entity_id:
            add("error", "natural-spawn", f"natural spawn modifier {filename} targets the wrong entity")
        if entity_id.rsplit(":", 1)[-1].upper() not in placement_source.upper():
            add("error", "natural-spawn", f"spawn placement registration is missing for {entity_id}")

    npc_source = read(ROOT / "src/main/java/matteroverdrive/event/VillageNpcSpawnEvents.java")
    for entity_id in ("FieldScientistEntity", "SystemsEngineerEntity", "MadScientistEntity"):
        if entity_id not in npc_source:
            add("error", "natural-spawn", f"village NPC population hook is missing {entity_id}")

    overhaul_entity_source = read(ROOT / "src/main/java/matteroverdrive/event/OverhaulEntityEvents.java")
    for entity_id in ("FIELD_SCIENTIST", "SYSTEMS_ENGINEER", "ASSIMILATOR", "PHASE_STALKER"):
        if f"OverhaulContent.{entity_id}.get()" not in overhaul_entity_source:
            add("error", "entity", f"attribute registration is missing {entity_id}")

    for entity_source_path in (
        ROOT / "src/main/java/matteroverdrive/entity/FieldScientistEntity.java",
        ROOT / "src/main/java/matteroverdrive/entity/SystemsEngineerEntity.java",
        ROOT / "src/main/java/matteroverdrive/entity/MadScientistEntity.java",
    ):
        entity_source = read(entity_source_path)
        if "ContractInteractionEvents.recordConversation" not in entity_source:
            add("error", "pda", f"NPC conversation is not connected to the PDA log: {entity_source_path.relative_to(ROOT)}")
    for entity_source_path in (
        ROOT / "src/main/java/matteroverdrive/entity/FieldScientistEntity.java",
        ROOT / "src/main/java/matteroverdrive/entity/SystemsEngineerEntity.java",
    ):
        if "NpcAssignmentFlow.offer" not in read(entity_source_path):
            add("error", "pda", f"NPC assignment flow is missing: {entity_source_path.relative_to(ROOT)}")

    pda_source = read(ROOT / "src/main/java/matteroverdrive/item/DataPadItem.java")
    for marker in ("ModNetwork.openDataPad", "ContractEvents.recordScan", "TechnologyLoreEvents.discoverTechnology"):
        if marker not in pda_source:
            add("error", "pda", f"Data Pad integration is missing {marker}")
    discovery_sources = list((ROOT / "src/main/java/matteroverdrive").rglob("*.java"))
    if not any("PlayerDiscoveryLog.record" in read(path) for path in discovery_sources):
        add("error", "pda", "PDA discovery log has no recording event")
    guide_source = read(ROOT / "src/main/java/matteroverdrive/client/GuideMeCompatEvents.java")
    if "matteroverdrive:guide" not in guide_source and "GUIDE_ID" not in guide_source:
        add("error", "guideme", "GuideME registration hook is missing the Matter Overdrive guide id")

    artifact_source = read(ROOT / "src/main/java/matteroverdrive/item/RecoveredArtifactItem.java")
    loadout_source = read(ROOT / "src/main/java/matteroverdrive/android/AndroidLoadout.java")
    perk_source = read(ROOT / "src/main/java/matteroverdrive/event/AndroidLoadoutEvents.java")
    for marker, source_name, source in (
        ("RelicId", "RecoveredArtifactItem", artifact_source),
        ("AndroidLoadout.selectArtifact", "RecoveredArtifactItem", artifact_source),
        ("enum Artifact", "AndroidLoadout", loadout_source),
        ("AndroidLoadout.hasArtifact", "AndroidLoadoutEvents", perk_source),
    ):
        if marker not in source:
            add("error", "relic", f"relic behavior is missing {marker} in {source_name}")

def check_native_destiny_assets() -> None:
    """Verify every registered native Destiny weapon has its imported visual/audio resources."""
    profile_ids = re.findall(
        r'^\s*[A-Z0-9_]+\("([^"\\]+)"', read(DESTINY_PROFILES_JAVA), re.MULTILINE)
    for weapon_id in profile_ids:
        for kind, path in (
            ("geometry", ASSETS / "native_destiny/geometry" / f"{weapon_id}.geo.json"),
            ("animation", ASSETS / "native_destiny/animations" / f"{weapon_id}.animation.json"),
            ("display transform", ASSETS / "native_destiny/transforms" / f"{weapon_id}.json"),
            ("texture", ASSETS / "textures/native_destiny" / f"{weapon_id}.png"),
            ("item model", ASSETS / "models/item" / f"{weapon_id}.json"),
        ):
            if not path.exists():
                add("error", "destiny-visuals", f"native Destiny {kind} is missing for {weapon_id}: {path.relative_to(ROOT)}")

    sounds = json_file(ASSETS / "sounds.json")
    sound_source = read(ROOT / "src/main/java/matteroverdrive/registry/ModDestinySounds.java")
    sound_ids = re.findall(r'\bregister\("([^"\\]+)"\)', sound_source)
    profile_sound_ids = set()
    for line in read(DESTINY_PROFILES_JAVA).splitlines():
        if not re.match(r'^\s*[A-Z0-9_]+\("', line):
            continue
        values = re.findall(r'"(destiny_[a-z0-9_]+)"', line)
        profile_sound_ids.update(values[1:])
    if len(sound_ids) != len(set(sound_ids)):
        duplicates = sorted({sound_id for sound_id in sound_ids if sound_ids.count(sound_id) > 1})
        add("error", "destiny-audio", f"duplicate native Destiny sound registrations: {', '.join(duplicates)}")
    if not isinstance(sounds, dict):
        return
    for sound_id in set(sound_ids) | profile_sound_ids:
        if sound_id not in sounds:
            add("error", "destiny-audio", f"registered native Destiny sound has no sounds.json entry: {sound_id}")
            continue
        for sound in sounds[sound_id].get("sounds", []) if isinstance(sounds[sound_id], dict) else []:
            name = sound.get("name", "") if isinstance(sound, dict) else ""
            if not name.startswith("matteroverdrive:destiny/"):
                continue
            source = ASSETS / "sounds" / (name.split(":", 1)[1] + ".ogg")
            if not source.exists():
                add("error", "destiny-audio", f"sounds.json entry {sound_id} references missing audio {name}")

def check_structure_architecture() -> None:
    registry = read(STRUCTURES_JAVA)
    structure = read(STRUCTURE_JAVA)
    for marker in ["TECHNOLOGY_FACILITY_PIECE", "FACILITY_INFRASTRUCTURE_PIECE", "FACILITY_TERRAIN_PIECE"]:
        if marker not in registry:
            add("error", "worldgen", f"missing native StructurePiece registration marker: {marker}")
    legacy_assembly = all(marker in structure for marker in (
        "TechnologyFacilityStructurePiece.assemble", "FacilityInfrastructurePiece.assemble",
        "FacilityTerrainPiece.assemble"))
    modern_assembly = all(marker in structure for marker in (
        "ModernExplorationStructurePiece", "ModernTraversalRepairPiece", "EnvironmentalDressingPiece"))
    if not (legacy_assembly or modern_assembly):
        add("error", "worldgen", "TechnologyFacilityStructure has neither the legacy assembly contract nor the modern exploration-native assembly contract")
    if "GenerationStub" not in structure:
        add("error", "worldgen", "technology facilities are no longer using native Structure.GenerationStub")

def check_inventory_scope() -> None:
    inv = json_file(INVENTORY_JSON)
    if not isinstance(inv, dict):
        return
    scope_doc = ROOT / "docs/reference/PORT_INVENTORY_SCOPE.md"
    scope_text = read(scope_doc).lower() if scope_doc.exists() else ""
    explicit_historical = inv.get("inventoryScope") == "historical-legacy-reference" or (
        "historical" in scope_text and "source of truth" in scope_text and "star map" in scope_text
    )
    if not explicit_historical:
        add("warning", "inventory", "PORT_INVENTORY.json is not explicitly documented as historical reference")

def write_reports(blocks: list[str], items: list[str]) -> None:
    REPORT_DIR.mkdir(parents=True, exist_ok=True)
    counts = {level: sum(f.severity == level for f in findings) for level in ("error", "warning", "info")}
    report = {"sourceOfTruth": "active Java registries", "activeBlocks": len(blocks),
              "standaloneItems": len(items), "counts": counts, "findings": [asdict(f) for f in findings]}
    (REPORT_DIR / "m2-port-consistency.json").write_text(json.dumps(report, indent=2), encoding="utf-8")
    lines = ["# Matter Overdrive Port Consistency Report", "",
             "Active Java registries are authoritative; historical inventories are reference-only.", "",
             f"- Active blocks parsed: **{len(blocks)}**", f"- Standalone items parsed: **{len(items)}**",
             f"- Errors: **{counts['error']}**", f"- Warnings: **{counts['warning']}**", ""]
    for severity in ("error", "warning", "info"):
        selected = [f for f in findings if f.severity == severity]
        if selected:
            lines += [f"## {severity.title()}s", ""] + [f"- **{f.category}**: {f.message}" for f in selected] + [""]
    (REPORT_DIR / "m2-port-consistency.md").write_text("\n".join(lines), encoding="utf-8")

def main() -> int:
    blocks, items = check_registry_resources()
    check_json_and_model_refs()
    check_texture_resources()
    check_ae2_contract()
    check_guides()
    check_retired_active_refs()
    check_facility_relics()
    check_vanilla_progression()
    check_native_destiny_assets()
    check_structure_architecture()
    check_inventory_scope()
    write_reports(blocks, items)
    errors = [f for f in findings if f.severity == "error"]
    warnings = [f for f in findings if f.severity == "warning"]
    if errors:
        print(f"M2 PORT CONSISTENCY FAILED: {len(errors)} error(s), {len(warnings)} warning(s)")
        for f in errors[:30]:
            print(f" - [{f.category}] {f.message}")
        return 1
    print(f"M2 PORT CONSISTENCY PASSED: {len(blocks)} blocks, {len(items)} standalone items, {len(warnings)} warning(s)")
    print("Report: build/reports/m2-port-consistency.md")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
