# Current Features and Roadmap

Status: `testing/alpha`, updated 2026-09-04.
Legacy parity reference: MatterOverdrive 1.12.2 `0.7.1.0` jar plus recovered source/resources.

The detailed feature matrix is [WORKING_FEATURES.md](../reference/WORKING_FEATURES.md). The runtime pass is [TO_TEST.md](../testing/TO_TEST.md).

## Current playable systems

- Matter loop: Decomposer, Recycler, Analyzer, Pattern Drives, Pattern Storage, Pattern Monitor, Replicator, Scanner, Portable Decomposer, Matter Containers and Matter Pipes.
- FE/machines: Solar Panel, Heavy Energy Cable, Charging Station, Microwave, Space-Time Accelerator, Transporter, Molecular Inscriber and the testing-branch Energy Bank reactor buffer.
- Item/pattern logistics: Network Pipe, Router, Switch and Pylon.
- Reactor/gravity: Fusion Reactor ring, IO, shared ring power, mass-scaled output, anomaly hazards, stabilizers, Reactor Remote/guide/overlay and persistent controls.
- Weapons: Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun, Omni Tool and Vex Mythoclast with FE, heat/reload and Weapon Station module support.
- Android: conversion, persistent core FE/HUD, four parts, four active abilities and the 30-node selectable perk tree.
- Storage/resources: portable Tritanium Crates, Tritanium/Dilithium world generation, tools and armour.
- Progression: Contract Market, contracts, Data Pad, real Mad Scientist and Puny Humans quest slice.
- Security/Holo: owner-bound Security Protocol modes and a programmable persistent Holo Sign.
- Legacy entities: real Rogue Android plus Failed Cow/Pig/Sheep/Chicken and Mad Scientist.

## Runtime status from the latest player test

Confirmed in-game:

- Rogue Android combat.
- Rogue Android sounds.
- All four failed animals spawning/behaviour.
- Mad Scientist/Puny Humans quest.

The latest Holo Sign retest confirms the thin monitor model and renamed-item programming path now work. The remaining visual defect is the text transform: the hologram still billboards toward the camera, so side/top views make it appear detached from the monitor face. Front-plane anchoring is still pending.

## Current implementation pass: original visual parity

The active pass replaces generic cube approximations with legacy model geometry and texture mappings from the original jar. It includes Android/Weapon stations, Contract Market, Matter Analyzer, Decomposer, Recycler, Microwave, Pattern Monitor, Pattern Storage, Replicator, Charging Station, Space-Time Accelerator and Solar Panel, while retaining earlier Inscriber/Crate/armour/gun/transparency fixes.

## Highest remaining parity work

1. Runtime-verify the current Holo and full visual/model pass.
2. Cocktail of Ascension + Mutant Scientist and the broader dialog/quest framework.
3. Ranged Rogue Androids, drones and richer entity equipment/AI.
4. Legacy structures/world events and their spawn ecosystems.
5. Connected pipe geometry and full Pylon presentation.
6. Deeper drive/network configuration and selected modern integration equivalents.

## Important visual follow-up

The current pass deliberately does not pretend that every old renderer can be represented by a static 1.20.1 JSON model. Connected pipes, Pylon overlays, some emissive/CTM effects and deeper weapon module rendering need dedicated modern rendering/state work. They remain tracked as explicit follow-up rather than being marked complete because their PNGs exist.
