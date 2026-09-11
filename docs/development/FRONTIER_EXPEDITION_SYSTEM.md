# Frontier Expedition System

Status: implemented in source/datapack on `feature/lead-dev-expansion-2026-09-11`; local compile and runtime verification are still required.

## Purpose

The Frontier Expedition slice extends Matter Overdrive exploration with a second generation family that is intentionally larger and more dungeon-like than the compact field sites. It is built on native `Structure` / `StructurePiece` generation so Minecraft clips each room to the active chunk rather than synchronously stamping neighbouring chunks.

## New structure family

Four new facility classes are registered and placed through `worldgen/structure_set/frontier_expeditions.json`.

### Deep Matter Vault

Underground archive/refinery complex built around a protected Matter storage core. Rooms include a surface/shaft entry, security checkpoint, archive, refinery and central vault. The vault mixes Matter Storage Matrix, Pattern Storage, Matter Analyzer, Decomposer and Matter Excavator technology with security markers and high-value caches.

### Autonomous Drone Foundry

Surface industrial complex built around automated drone production. The foundry includes control, fabrication, hangar and salvage areas. It places Drone Fabricator, Charging Station, Android Station, Inscriber and Android security infrastructure so the site reads as a manufacturing location rather than a generic shell.

### Anomaly Quarantine Site

Partially buried containment installation. The structure includes decontamination, observation, security and a reinforced containment chamber built around an Anomaly Containment Unit and Gravitational Stabilizers. It is intentionally more dangerous-looking and uses darker materials and security markers.

### Orbital Recovery Array

Surface telemetry/salvage site built to process recovered orbital material and signals. It includes control, processing, storage, elevated catwalks and a large recovery mast/array using Quantum Power Relay and network infrastructure.

## Layout and damage model

Each site has three deterministic layout variants derived from its start chunk. Individual rooms also derive deterministic damaged/occupied state from world position, room identity and layout number. This provides visible variation without depending on mutable generation RNG, so serialization and reloads reconstruct the same facility.

Damaged rooms can contain breached walls, cracked debris, cobwebs and inert replacement machinery. Occupied rooms add Android security markers. The system therefore produces intact, damaged and hostile-feeling versions without introducing non-deterministic reload changes.

## Frontier Expedition progression

`FrontierExpeditionEvents` performs a server-authoritative native-structure lookup only when a player enters a new chunk. It asks Minecraft's `StructureManager` whether the player's position is inside one of the four Frontier Expedition structures, so older facilities cannot accidentally satisfy the new expedition and no block-radius scan or chunk forcing is required.

Discoveries are persisted through `FrontierExpeditionSavedData` per player and per structure-start chunk. The ledger separately tracks a four-bit site-class mask so repeated facilities can be logged without duplicating the unique-class milestone.

Rewards:

- new site class: 40 XP and a `facility_research` dossier tagged with `FrontierArchive` and `FrontierProgress`;
- additional location of a known class: 15 XP and another location dossier;
- all four unique site classes: 120 bonus XP plus a Parallel Processing Upgrade.

The completion reward is issued only when the unique-site mask transitions to all four classes.

## Performance and safety contract

- generation uses independent native structure pieces;
- every block write checks the current chunk clipping box;
- no structure code force-loads chunks;
- layout state is serialized with each piece;
- discovery work is one native `StructureManager` membership query per candidate site class, only after a player crosses into a different chunk;
- saved data caps discovery keys and player entries and rejects oversized state growth;
- missing Matter Overdrive decorative/machine blocks fall back to vanilla blocks during generation instead of crashing worldgen.

## Files added/changed

- `src/main/java/matteroverdrive/worldgen/FrontierSiteStructure.java`
- `src/main/java/matteroverdrive/worldgen/FrontierSitePiece.java`
- `src/main/java/matteroverdrive/world/FrontierExpeditionSavedData.java`
- `src/main/java/matteroverdrive/event/FrontierExpeditionEvents.java`
- `src/main/java/matteroverdrive/registry/ModStructures.java`
- four structure JSONs under `data/matteroverdrive/worldgen/structure/`
- `data/matteroverdrive/worldgen/structure_set/frontier_expeditions.json`

## Verification still required

The connector environment can inspect and commit source but cannot run the local Gradle/Forge client used by the project. Before merging, run `gradlew.bat compileJava --no-daemon`, the repository sanity/resource checks, then the runtime test plan in a new world. Any generated layout that has an unreachable room, terrain-sealed entrance, missing machine, unsafe fall, impossible return path or chunk-border artifact should be treated as a blocking structure bug.
