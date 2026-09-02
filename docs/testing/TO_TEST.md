# Consolidated Runtime Testing Checklist

Branch: `feature/handheld-matter-tools`

Use this after a fresh pull and normal M2 client launch. Record the expected result, actual result, coordinates/orientation and relevant log lines for every failure.

## Build and runtime gate

- [ ] Reach the main menu without registry, datapack, model or menu errors.
- [ ] Create/load a test world successfully.
- [ ] Confirm the runtime marker reports `blocks=75`, `blockItems=72`, `standaloneItems=100`, `sounds=57`, `blockEntities=18` and `menus=15`.
- [ ] Confirm the marker includes `microwave=enabled`, `spacetimeAccelerator=enabled` and `handheldMatterTools=enabled`.
- [ ] Confirm new handheld recipes appear in the recipe book/JEI and can be crafted in Survival.

## Survival resources and Android pills

- [ ] In fresh Overworld chunks, confirm Tritanium generates from Y -32 through 64 and Dilithium from Y -64 through 16.
- [ ] Smelt/blast both ores into their intended resources.
- [ ] Craft and verify Blue, Red and Yellow Android Pills.
- [ ] Save/reload and confirm Android state/FE remains correct.

## Microwave

- [ ] Accept valid food-smelting inputs and reject non-food items.
- [ ] Cook from cable FE and from a charged battery without free processing.
- [ ] Confirm base timing/cost, output blocking, upgrades, persistence, automation and break-safe drops.
- [ ] See `MICROWAVE_TESTING.md` for the focused checklist.

## Space-Time Accelerator

- [ ] Require both FE and matter; verify base usage and 40-tick pulse timing.
- [ ] Accelerate crops/random ticks and several block entities on the same Y level.
- [ ] Verify equal +X/-X/+Z/-Z boundary coverage.
- [ ] Verify redstone disable, every supported upgrade, persistence, break-safe drops and acceptable tick-time impact.
- [ ] See `SPACETIME_ACCELERATOR_TESTING.md` for the focused checklist.

## Matter Scanner

- [ ] Sneak-use the Scanner on a powered Pattern Storage containing a Pattern Drive and confirm the link is stored.
- [ ] Hold-use the linked Scanner on a block with a matter value for the full scan time.
- [ ] Confirm the block is destroyed only after a successful scan and the linked drive gains 10% pattern progress.
- [ ] Confirm ten valid blocks complete a normal pattern and a completed/full/unpowered/offline storage refuses further scans without destroying the target.
- [ ] Move dimensions, unload/break the linked storage and confirm scanning fails safely.
- [ ] Save/reload the Scanner and confirm its link and last-scan status persist.

## Portable Decomposer

- [ ] Charge it in the Charging Station and confirm its 128,000 FE capacity persists.
- [ ] Sneak-use with an offhand matter-valued item to add/remove that item from its pickup filter.
- [ ] Pick up matching stacks and confirm they are automatically consumed at the intended 10% matter yield while FE is available.
- [ ] Confirm unmatched, zero-matter, insufficient-FE and full-storage pickups enter the inventory normally.
- [ ] Confirm fractional yield is retained rather than granting free matter or silently losing every low-value item.
- [ ] Transfer stored matter into a compatible machine/container and verify exact values before/after.
- [ ] Save/reload with FE, matter, filter entries and fractional remainder present.

## Data Pad and guide

- [ ] Right-click in air and confirm the guide screen opens.
- [ ] Navigate every guide page and verify text/buttons at multiple GUI scales.
- [ ] Use the Data Pad on supported blocks and confirm scan-history entries record the block/item and matter value.
- [ ] Confirm history is capped, ordered newest-first and persists through save/reload.
- [ ] Confirm unknown/zero-matter blocks are identified without crashing.

## Core regression checks

- [ ] Reactor structure, anomaly mass, IO/cable FE transfer and stabilizers remain correct.
- [ ] Empty weapons cannot fire; real batteries transfer only their stored FE; Energy Packs remain consumable.
- [ ] Contract pickup/kill progress advances only one matching contract.
- [ ] Pattern Analyzer/Storage/Monitor/Replicator still work after Scanner-created patterns.
- [ ] Transparent blocks, crate/Inscriber models, equipped armour and held weapon transforms remain visually correct.
- [ ] Machine inventories/upgrades survive reload and return safely on block break.

## Pass criteria

The branch is ready to consolidate when the runtime gate passes and there are no new crashes, datapack errors, item loss/duplication, free-energy/free-matter exploits, asymmetric Accelerator range, broken pattern progression or handheld-state persistence failures.