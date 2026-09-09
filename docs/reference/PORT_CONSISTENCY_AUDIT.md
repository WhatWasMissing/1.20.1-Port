# Whole-port consistency audit

`python scripts/validate_port_consistency.py` is the lightweight, Forge-independent consistency gate for the 1.20.1 port.

## Source of truth

Current Java registries are authoritative:

1. `ModBlocks` / `ModItems` and other active registries;
2. active assets and data resources;
3. GuideME and current testing/reference documentation;
4. historical parity inventories and old handoffs only as reference material.

`PORT_INVENTORY.json` is a milestone-era extraction from the legacy porting effort. It is useful for historical comparison, but it is **not** an instruction to register every legacy ID. In particular, its Star Map entries must never be interpreted as active-port requirements.

## Checks

The audit currently covers:

- duplicate active block and standalone-item IDs;
- explicit retired-content guard for the Star Map in active registries and GuideME;
- blockstate, item-model and modern localization coverage warnings for registered content;
- JSON parsing across Matter Overdrive asset/data resources;
- missing Matter Overdrive model references from blockstates/models;
- missing Matter Overdrive texture references from models;
- local GuideME link resolution;
- required native facility StructurePiece registrations and assembly hooks;
- explicit historical-scope documentation for `PORT_INVENTORY.json`.

Model/texture reference failures, malformed JSON, retired-content reactivation, duplicate IDs and loss of the native facility piece architecture are hard failures. Coverage gaps that can have legitimate implementation-specific exceptions are warnings and are written to the report for follow-up.

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
