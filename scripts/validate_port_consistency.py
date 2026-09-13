#!/usr/bin/env python3
"""Static consistency audit for the Matter Overdrive 1.20.1 port.

The active Java registries are the source of truth. Historical parity inventories are
reference material only. The audit is intentionally Forge-independent so it can run
before Gradle and produce a useful report even when the game cannot be launched.
"""
from __future__ import annotations

import json
import re
from dataclasses import dataclass, asdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/matteroverdrive"
DATA = ROOT / "src/main/resources/data/matteroverdrive"
REPORT_DIR = ROOT / "build/reports"
BLOCKS_JAVA = ROOT / "src/main/java/matteroverdrive/registry/ModBlocks.java"
OVERHAUL_BLOCKS_JAVA = ROOT / "src/main/java/matteroverdrive/registry/OverhaulContent.java"
ITEMS_JAVA = ROOT / "src/main/java/matteroverdrive/registry/ModItems.java"
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

def check_json_and_model_refs() -> None:
    for path in all_json_files():
        obj = json_file(path)
        if obj is None:
            continue
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

def check_registry_resources() -> tuple[list[str], list[str]]:
    block_text, overhaul_text, item_text = read(BLOCKS_JAVA), read(OVERHAUL_BLOCKS_JAVA), read(ITEMS_JAVA)
    blocks = list(dict.fromkeys(java_list(block_text, "LEGACY_BLOCK_IDS") + registered_blocks(overhaul_text)))
    no_items = set(java_list(block_text, "NO_BLOCK_ITEM", "Set"))
    items = java_list(item_text, "STANDALONE_ITEM_IDS")
    for label, values in (("block", blocks), ("standalone item", items)):
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
    for item in items:
        key = f"item.matteroverdrive.{item}"
        if key not in lang and f"item.matteroverdrive.{item}.name" not in lang:
            add("warning", "localization", f"standalone item {item} has no modern localization key")
        if not (ASSETS / "models/item" / f"{item}.json").exists():
            add("warning", "resource", f"standalone item {item} has no item model")
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
        for block in registered:
            heading = f"### `{block}`"
            if heading not in block_text:
                add("error", "guideme", f"Block Reference is missing active block {block}")
                continue
            if block in no_items:
                continue
            item_link = f'<ItemLink id="matteroverdrive:{block}" />'
            recipe_id = block.replace(".", "_")
            recipe_link = f'<RecipeFor id="matteroverdrive:{recipe_id}" />'
            if item_link not in block_text:
                add("error", "guideme", f"Block Reference is missing item image link for {block}")
            if recipe_link not in block_text:
                add("error", "guideme", f"Block Reference is missing recipe link for {block}")
    else:
        add("error", "guideme", "Block Reference page does not exist")

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

def check_structure_architecture() -> None:
    registry = read(STRUCTURES_JAVA)
    structure = read(STRUCTURE_JAVA)
    for marker in ["TECHNOLOGY_FACILITY_PIECE", "FACILITY_INFRASTRUCTURE_PIECE", "FACILITY_TERRAIN_PIECE"]:
        if marker not in registry:
            add("error", "worldgen", f"missing native StructurePiece registration marker: {marker}")
    for marker in ["TechnologyFacilityStructurePiece.assemble", "FacilityInfrastructurePiece.assemble", "FacilityTerrainPiece.assemble"]:
        if marker not in structure:
            add("error", "worldgen", f"TechnologyFacilityStructure does not assemble {marker.split('.')[0]}")
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
    check_guides()
    check_facility_relics()
    check_vanilla_progression()
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
