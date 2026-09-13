# Frontier Expedition System

Status: registered compatibility/event system in 0.7; natural MO placement is intentionally disabled while visual/content review is pending. Local compile and runtime verification are still required.

## Purpose

The Frontier Expedition slice retains a second-generation family that is intentionally larger and more dungeon-like than the compact field sites. It is built on native `Structure` / `StructurePiece` generation so Minecraft clips each room to the active chunk rather than synchronously stamping neighbouring chunks. The definitions and serializers are dormant in new worlds until their visual/content review is complete.

## New structure family

Four facility classes are registered as dormant compatibility definitions. No `worldgen/structure_set` resource places them in new worlds.

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

`FrontierExpeditionEvents` primarily authenticates a site class from existing player-event evidence routed by technology acquisition, archive-fragment inspection, NPC assignments and Field Operations. It retains a bounded native-structure lookup only as a fallback for structures already generated in older saves; no new placement resource can trigger it in a fresh world.

Discoveries are persisted through `FrontierExpeditionSavedData` per player using the existing ledger format. The ledger separately tracks a four-bit site-class mask so repeated evidence can be logged without duplicating the unique-class milestone.

Rewards:

- new site class: 40 XP and a `facility_research` dossier tagged with `FrontierArchive` and `FrontierProgress`;
- additional compatible evidence of a known class: 15 XP and another archive dossier;
- all four unique site classes: 120 bonus XP plus a Parallel Processing Upgrade.

The completion reward is issued only when the unique-site mask transitions to all four classes.

## Facility threat clearance

`FrontierThreatEvents` adds a second objective layer. Hostile `MobCategory.MONSTER` kills made by a player are counted only when the killed entity is inside a Frontier Expedition structure according to the native `StructureManager`.

Per-player, per-structure progress is stored in `FrontierSecuritySavedData`. Clearance targets are intentionally different by facility role: Deep Matter Vault 4, Autonomous Drone Foundry 6, Anomaly Quarantine Site 5, Orbital Recovery Array 4. Progress is shown through the action bar. Completing a facility awards 60 XP and a `facility_research` dossier tagged with `FrontierSecurityArchive` and `Secured=true`; completion state persists so the same structure cannot be farmed repeatedly by the same player.

This turns occupied variants into an actual gameplay objective while leaving peaceful/damaged sites explorable without requiring combat for the four-site discovery chain.

## Performance and safety contract

- generation uses independent native structure pieces;
- every block write checks the current chunk clipping box;
- no structure code force-loads chunks;
- layout state is serialized with each piece;
- primary discovery work is event-driven; the compatibility fallback performs one native `StructureManager` membership query per candidate site class only after a player crosses into a different chunk;
- threat-clearance lookups run only when a qualifying hostile actually dies;
- saved data caps discovery/security keys and rejects oversized state growth;
- missing Matter Overdrive decorative/machine blocks fall back to vanilla blocks during generation instead of crashing worldgen.

## Files added/changed

- `src/main/java/matteroverdrive/worldgen/FrontierSiteStructure.java`
- `src/main/java/matteroverdrive/worldgen/FrontierSitePiece.java`
- `src/main/java/matteroverdrive/world/FrontierExpeditionSavedData.java`
- `src/main/java/matteroverdrive/world/FrontierSecuritySavedData.java`
- `src/main/java/matteroverdrive/event/FrontierExpeditionEvents.java`
- `src/main/java/matteroverdrive/event/FrontierThreatEvents.java`
- `src/main/java/matteroverdrive/registry/ModStructures.java`
- four structure JSONs under `data/matteroverdrive/worldgen/structure/`
- no active Frontier structure-set resource is shipped in 0.7;

## Verification still required

The connector environment can inspect and commit source but cannot run the local Gradle/Forge client used by the project. Before merging, run `gradlew.bat compileJava --no-daemon`, the repository sanity/resource checks, then test that fresh worlds do not place MO-authored structures and that existing generated content remains compatible. Any retained layout that has an unreachable room, terrain-sealed entrance, missing machine, unsafe fall, impossible return path or chunk-border artifact should be treated as a blocking structure bug before placement is reconsidered.
