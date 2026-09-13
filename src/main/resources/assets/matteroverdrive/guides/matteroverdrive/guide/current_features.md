---
navigation:
  title: Current Features
  position: 2
  icon: matteroverdrive:data_pad
---
# Current Features - 0.6

This page is the compact in-game status reference for the current **Matter Overdrive 0.6** development line, maintained by MVQ1303. The individual GuideME system pages are the how-to guide. The `testing/tech-overhaul` branch contains additional experimental infrastructure and must be runtime-tested before merge to `main`.

Implemented does not mean runtime-verified. Use the repository testing documents for the current regression plan.

## Matter and machines

Implemented on the core line: Decomposer, Recycler, Matter Analyzer, Pattern Drives, Pattern Storage, Pattern Monitor, Replicator, Molecular Inscriber, Matter Scanner, Portable Decomposer, Matter Containers, Matter Pipe, Solar Panel, Heavy Energy Cable, Microwave, Space-Time Accelerator, Charging Station, Transporter, Tritanium Crates, Weapon Station and Tritanium Wrench.

Major machines expose server-backed operator pages and inventories rather than decorative controls. Machine redstone/configuration parity is restored where a real backend equivalent exists.

### Matter economy in 0.6

Matter values resolve through dynamic Matter Dust values, explicit values, tag bases, recursive recipe derivation and deterministic fallbacks. Analyzer and Decomposer use the level-aware value path, Pattern Drives preserve the analyzed value, and Replicator consumes that stored value.

Matter tooltips expose the effective value and diagnostic commands are available under `/matteroverdrive matter` for value inspection, auditing and cache clearing.

## Tech overhaul testing branch

`testing/tech-overhaul` currently adds the following experimental systems:

- **Grid Capacitor** - 16,000,000 FE high-throughput grid buffer.
- **Android Induction Relay** - same-dimension wireless Android charging with 32/64/96-block range modes.
- **Quantum Power Relay** - 16-channel same-dimension wireless FE links between loaded relays within 256 blocks.
- **Matter Storage Matrix** - modular bulk Matter storage with four removable cell slots.
- **Matter Cells** - 64k, 256k, 1M and 4M capacity tiers.
- **Matter Excavator** - FE-powered configured block decomposition over 8/16/24-block working radii.
- **Holographic Status Panel** - live facility overview/energy/matter/alarm telemetry.
- **Facility Network Controller telemetry expansion** - aggregate FE/Matter/device/alarm reporting with comparator output.
- **Matter Network channels** - Router/Switch channels 0-15.
- **Router priority** - deterministic primary/backup executor selection.
- **Network Diagnostic Probe** - face policy, network/channel/priority and range configuration/inspection.
- **Per-side FE/Matter/Item policies** - INPUT / OUTPUT / BOTH / DISABLED where supported by the backend.
- **Parallel Processing Upgrade** - additional concurrent processing lanes in Decomposer and Replicator.

These features are deliberately isolated from `main` until the test plan in `docs/testing/TECH_OVERHAUL_TEST_PLAN.md` passes.

See [Advanced Infrastructure](advanced_infrastructure.md), [Matter Network](network.md), [Power and Machines](power.md) and [Matter Technology](matter.md) for operating instructions.

## Fusion and gravity

Implemented: Fusion Reactor structure validation, Controller/IO shared storage, anomaly-mass-scaled output, upgrades, cable output, demand telemetry, shared ring power, RUN/SCRAM, redstone/comparator behavior, Reactor Remote, persistent overlay, Gravitational Anomaly mass/pull/event horizon, Equalizer and powered Stabilizers.

The **Anomaly Containment Unit** can capture an anomaly, preserve its mass and redeploy it elsewhere so reactor/base placement is no longer dictated by natural anomaly position.

## Matter Network

Implemented core systems: Network Pipe, Network Switch, Network Router and matching-channel Pylon routing. Router filtering, Network Flash Drive destination filtering, upgrade slots, multi-stack budgets and FE/item execution accounting are present. The Matter Network remains intentionally separate from an AE2 ME network.

The tech-overhaul branch extends this with channels, router priority, per-side resource policies, facility telemetry and the Network Diagnostic Probe.

## Androids and entities

Implemented: Android conversion, FE/HUD, body parts, persistent skill tree, class/loadout systems, five-slot chassis hardware, Android Station equipment management, Android Spawner ownership/squad behavior, Rogue Android squads, Rogue/Ranged Rogue Androids, Failed creatures, Mad Scientist, Mutant Scientist and Drone.

Linked Drones support FOLLOW, HOLD, DEFENSIVE, PASSIVE and AGGRESSIVE modes with a dedicated Drone Management screen. The tech-overhaul branch adds Android Induction Relay wireless charging and fixes Capacitor Core capacity so its extra 50,000 FE is usable storage.

Village Field Scientists, Systems Engineers and Mad Scientists now form the player-facing research cast. Their conversations, issued assignments and first-contact lore are recorded by the existing PDA field log, while objectives remain normal transferable contracts.

Facility caches now have rare, themed **Legendary Relics** that install the existing Android passive protocols: Overclocked Relay, Swarm Beacon, Aegis Prism, Hunter Lens, Nanite Crown, Capacitor Heart and Phase Anchor. Their themed drops connect facility exploration to meaningful Android build choices without adding another progression currency.

## Weapons

Implemented: Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool with FE payment, heat/overheat, reload and module effects. Weapon Station exposes its real module slots and live statistics. Exact remaining module mesh placement and full legacy first-person animation choreography still need visual parity work.

## World content

Implemented structure families: crashed spacecraft, cargo ships, underwater bases, Mad Scientist houses, Android Houses and Sand Pits, with persisted salvage and inhabitants. Natural Gravitational Anomalies generate in fresh Overworld chunks. Exact old PNG-template geometry is not yet complete for every structure.

## Dimensional Pylon

Implemented backend: source-backed 2x3x2 multiblock identity, shared energy/matter state, charge/generation/drain behavior and GUI telemetry, while unformed blocks retain the modern relay compatibility behavior. Exact legacy visual effects remain a visual-testing item.

## Security and quests

Implemented: Empty/Claim/Access/Remove security protocols, security-aware wrench dismantling, contracts, staged objectives, restored legacy scientist quests and the modern research progression from Matter Technology through Anomaly Engineering. Quests guide progression but do not act as mandatory recipe/technology gates. Richer legacy-style cinematic dialogue remains presentation work.

## Optional integrations

When GuideME is installed, this manual is the primary player-facing how-to guide and the Data Pad remains the scanning/contract tool. When Applied Energistics 2 is installed, public Forge item capabilities allow normal Storage Bus/import/export-style automation on audited Matter Overdrive inventories without merging ME and Matter Overdrive networks or inventing AE-to-FE conversion.

## Remaining high-value work

- Runtime-test and harden the systems on `testing/tech-overhaul`.
- Exact legacy structure templates and deeper structure-specific scripted events.
- Remaining Drone flying/renderer/equipment polish.
- Remaining machine-specific GUI/detail parity where backed by real state.
- Exact Dimensional Pylon visual effects.
- Remaining weapon model placement and first-person animation choreography.
- Deeper source-backed dispatcher/broadcaster network concepts where they map cleanly to the current routing core.
- Richer dialogue presentation and remaining source-backed quest detail.

For operating instructions, return to the [Matter Overdrive index](index.md) and choose the relevant system page.
