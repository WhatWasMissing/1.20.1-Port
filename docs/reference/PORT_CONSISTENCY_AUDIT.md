# Whole-port consistency audit

`python scripts/validate_port_consistency.py` is the lightweight, Forge-independent consistency gate for the 1.20.1 port.

## Source of truth

Current Java registries are authoritative:

1. `ModBlocks` / `ModItems`, `OverhaulContent`, exotic-item registrations, and the native Destiny profile registry;
2. active assets and data resources;
3. GuideME and current testing/reference documentation;
4. historical parity inventories and old handoffs only as reference material.

`PORT_INVENTORY.json` is a milestone-era extraction from the legacy porting effort. It is useful for historical comparison, but it is **not** an instruction to register every legacy ID. In particular, its Star Map entries must never be interpreted as active-port requirements.

## Checks

The audit currently covers:

- duplicate IDs and active registry coverage for blocks, block items, standalone items, exotic items, and native Destiny profiles;
- blockstates, block/item models, block loot tables, recipes, tags, and English localization for active content;
- cross-reference validation for recipe, loot-table, and tag item/block IDs;
- JSON parsing across Matter Overdrive asset/data resources, plus model bounds and model-to-texture references;
- GuideME page links, block-entry descriptions, item links, recipe links, and referenced model/recipe files;
- vanilla loot injection, natural hostile spawning, village NPC spawning, PDA/contract/discovery hooks, relic registration/loadout behavior, and GuideME registration markers;
- required native facility `StructurePiece` registrations, assembly hooks, facility loot, and the absence of retired `structure_set` resources;
- explicit historical-scope documentation for `PORT_INVENTORY.json`.

The retired-content guard scans active Java, data, GuideME, and bundled current documentation. Historical inventories, parity records, and retired-content tests may still mention the Star Map so they can document or enforce its retired status; those references are not active implementation.

Malformed JSON, missing active resource references, invalid active IDs, missing block loot, incomplete GuideME entries, retired-content reactivation, duplicate IDs, and loss of the native facility piece architecture are hard failures. The native Destiny item registrations currently have valid item models and localization, but their optional native geometry/texture bundle is absent; that implementation-specific gap is reported as one warning for later asset work.

## Reports

Every run writes:

- `build/reports/m2-port-consistency.json`
- `build/reports/m2-port-consistency.md`

The JSON report is intended for tooling. The Markdown report is the useful file to attach or paste into ChatGPT after a local verification run.

## Integrated local gate

`VERIFY_M2_BUILD.bat` now runs, in order:

1. facility player-reachability QA;
2. native structure expansion validation;
3. whole-port consistency audit;
4. Gradle bootstrap;
5. M1 resource gate;
6. M2 source gate;
7. clean Forge build.

The old hard-coded alpha JAR filename check was removed. The gate now verifies that the current build produced a JAR under `build/libs`, so the script does not become invalid merely because the project version changes.
