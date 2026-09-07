---
navigation:
  title: Current Features
  position: 2
  icon: matteroverdrive:data_pad
---
# Current Features - 0.6

This page is the compact in-game status reference for the current **Matter Overdrive 0.6** `main` build, maintained by MVQ1303. The individual GuideME system pages are the how-to guide. The repository `docs/reference/WORKING_FEATURES.md` remains the detailed source-of-truth reference and this page mirrors its player-facing status.

Implemented does not mean runtime-verified. Use the **M2 Testing Checklist** for the current 0.6 regression plan.

## Matter and machines

Implemented: Decomposer, Recycler, Matter Analyzer, Pattern Drives, Pattern Storage, Pattern Monitor, Replicator, Molecular Inscriber, Matter Scanner, Portable Decomposer, Matter Containers, Matter Pipe, Solar Panel, Heavy Energy Cable, Microwave, Space-Time Accelerator, Charging Station, Transporter, Tritanium Crates, Weapon Station and Tritanium Wrench.

Major machines expose real server-backed operator pages and inventories rather than decorative controls. Machine redstone/configuration parity is restored where a real backend equivalent exists.

### Matter economy in 0.6

Matter values now resolve through dynamic Matter Dust values, explicit values, tag bases, recursive recipe derivation and deterministic fallbacks. Recipe resolution protects against cycles/depth explosions, accounts for recipe output counts and chooses the cheapest valid positive ingredient alternative. Analyzer and Decomposer use the level-aware value path, Pattern Drives preserve the analyzed value, and Replicator consumes that stored value.

Matter tooltips expose the effective value and can show its source. Diagnostic commands are available under `/matteroverdrive matter` for value inspection, auditing and cache clearing. Pattern Drives made before this valuation pass may need to be analyzed again to refresh their stored value.

See [Matter Technology](matter.md) for the normal player workflow.

## Fusion and gravity

Implemented: Fusion Reactor structure validation, Controller/IO shared storage, anomaly-mass-scaled output, upgrades, cable output, demand telemetry, shared ring power, RUN/SCRAM, redstone/comparator behavior, Reactor Remote, persistent overlay, Gravitational Anomaly mass/pull/event horizon, Equalizer and powered Stabilizers.

## Matter Network

Implemented: Network Pipe, Network Switch, Network Router and matching-channel Pylon routing. Router filtering, Network Flash Drive destination filtering, four Speed/Hyper-Speed slots, multi-stack budgets and the 10 FE/item execution limit are present. The Matter Network remains intentionally separate from an AE2 ME network.

## Androids and entities

Implemented: Android conversion, FE/HUD, body parts, abilities, persistent skill tree, Android Spawner ownership/squad behavior, Rogue Android squads, Rogue/Ranged Rogue Androids, Failed creatures, Mad Scientist, Mutant Scientist and Drone. Linked Drones support FOLLOW, DEFENSIVE, PASSIVE and AGGRESSIVE modes.

## Weapons

Implemented: Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool with FE payment, heat/overheat, reload and module effects. Weapon Station exposes its real module slots and live statistics. Exact remaining module mesh placement and full legacy first-person animation choreography still need visual parity work.

## World content

Implemented structure families: crashed spacecraft, cargo ships, underwater bases, Mad Scientist houses, Android Houses and Sand Pits, with persisted salvage and inhabitants. Natural Gravitational Anomalies generate in fresh Overworld chunks. Exact old PNG-template geometry is not yet complete for every structure.

## Star Map

Implemented: Galaxy -> Quadrant -> Star -> Planet navigation, persistent journeys and encounters, fleet combat, planetary economy, colonies, four construction queues, Scout/Colonizer production, recovered legacy capacity rules and planet-local ship transfers.

## Dimensional Pylon

Implemented backend: source-backed 2x3x2 multiblock identity, shared energy/matter state, charge/generation/drain behavior and GUI telemetry, while unformed blocks retain the modern relay compatibility behavior. Exact legacy visual effects remain a visual-testing item.

## Security and quests

Implemented: Empty/Claim/Access/Remove security protocols, security-aware wrench dismantling, contracts, staged objectives, restored legacy scientist quests and the modern research progression from Matter Technology through Anomaly Engineering. Quests guide progression but do not act as mandatory recipe/technology gates. Richer legacy-style cinematic dialogue remains presentation work.

## Optional integrations

When GuideME is installed, this manual is the primary player-facing how-to guide and the Data Pad remains the scanning/contract tool. When Applied Energistics 2 is installed, public Forge item capabilities allow normal Storage Bus/import/export-style automation on audited Matter Overdrive inventories without merging ME and Matter Overdrive networks or inventing AE-to-FE conversion.

## Remaining high-value parity work

- Exact legacy structure templates and deeper structure-specific scripted events.
- Additional source-backed Star Map strategic detail.
- Remaining Drone flying/renderer/equipment polish.
- Remaining machine-specific GUI/detail parity where backed by real state.
- Exact Dimensional Pylon visual effects.
- Remaining weapon model placement and first-person animation choreography.
- Deeper source-backed dispatcher/broadcaster network concepts where they map cleanly to the current routing core.
- Richer dialogue presentation and remaining source-backed quest detail.

For operating instructions, return to the [Matter Overdrive index](index.md) and choose the relevant system page.