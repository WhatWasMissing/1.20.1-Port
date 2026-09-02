# Current Features and Roadmap

Status: `testing/main`, updated 2026-09-02.

The detailed source-of-truth feature matrix is [WORKING_FEATURES.md](../reference/WORKING_FEATURES.md). This file is the shorter implementation roadmap.

## Current playable systems

- Matter machines: Decomposer, Recycler, Analyzer, Replicator, Molecular Inscriber, Pattern Storage and Pattern Monitor.
- Power/machine utilities: Microwave, Space-Time Accelerator, Solar Panel, Charging Station and Transporter.
- Matter and FE transport: Matter Pipe, Heavy Energy Cable and Reactor IO.
- Matter Network logistics: Network Pipe, Router, Switch and Pylon, including filtered powered item routing and pattern-network traversal.
- Fusion Reactor: ring multiblock, controller/IO, matter input, FE generation/output, structure diagnostics, Reactor Remote/guide and active Speed/Range/Power Storage/Matter Storage upgrades.
- Gravitational systems: anomaly pull/consumption/mass, Space-Time Equalizer and powered Gravitational Stabilizers.
- Weapons: Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun, rechargeable batteries/Energy Packs, heat/energy checks and Weapon Station modules.
- Android foundation: conversion/deactivation/recharge pills, persistent Android FE, Android Station, four bionic body parts, HUD and simplified Rogue Android Spawner.
- Progression foundation: Contract Market, collect/hunt contracts, redemption/refresh flow and per-viewer Star Map contract summary.
- Survival resource foundation: natural Tritanium and Dilithium ore generation, furnace/blast-furnace processing and temporary craftable Android-pill progression.
- Storage/utility: Tritanium Crates, Tritanium tools/armour and the current debug/testing utilities.

## Major original-mod parity gaps

1. **Android RPG abilities/biotic stats** — teleport, shield/force-field, cloak, night vision, shockwave/flash-cooling style abilities, unlock/cooldown progression and richer minimap/team UI.
2. **Full Star Map simulation** — galaxy/star/planet data, planet stats, buildings, ships and travel/attack events. The current Star Map is only a contract summary.
3. **Full quest/dialog framework** — generated/multi quests, mining/crafting and other quest logic, XP/rewards, quest HUD/Data Pad pages and Mad Scientist progression. Current contracts are a deliberately smaller slice.
4. **Data Pad/guide** — guide pages/history, scanning and quest integration are not implemented.
5. **Matter Scanner** — registered but gameplay is not implemented.
6. **Portable Decomposer** — registered but gameplay is not implemented.
7. **Omni Tool** — registered but original tool/weapon behaviour is not implemented.
8. **Holo Sign/security/ownership** — programmable sign and security protocol/ownership behaviour are missing.
9. **Legacy drive configuration** — generic/network flash-drive behaviour beyond Pattern/Transport drives is incomplete.
10. **Dedicated mobs/entities** — proper Rogue Androids, ranged/levelled variants, drones, failed animals and scientist NPC/mob content are not restored; the current spawner uses a tagged Husk.
11. **World structures/spawn layer** — natural Tritanium/Dilithium ores are restored, but themed structures, legacy anomaly/world events and the wider spawn layer are not.
12. **Remaining weapon depth** — original enchantment/random-weapon ecosystem and final recoil/model/beam parity remain.
13. **Legacy optional integrations** — old ComputerCraft/Tinkers/etc. compatibility has not been recreated for modern equivalents.

## Recommended implementation order

1. Matter Scanner, Portable Decomposer and Data Pad/guide.
2. Android ability tree/biotic stats and dedicated Rogue Android entities.
3. Holo Sign, security/ownership and remaining network-drive behaviour.
4. Themed world structures, anomaly/world events and legacy mobs/NPCs.
5. Full quest framework followed by full Star Map galaxy gameplay.
6. Remaining weapon parity and selected modern mod integrations.

## Current verification note

The Microwave and Space-Time Accelerator are now real machines rather than registry placeholders. The Accelerator consumes FE and matter to pulse extra ticks into nearby random-tick blocks and block entities, supports redstone disable and the current machine-upgrade framework, and isolates failing target tickers. Runtime verification is tracked in `docs/testing/MICROWAVE_TESTING.md`, `docs/testing/SPACETIME_ACCELERATOR_TESTING.md` and `docs/testing/SURVIVAL_PROGRESSION_TESTING.md`. Build success does not replace in-world regression checks for machine behaviour, world generation, recipes, visual rendering, compatibility or gameplay balance.
