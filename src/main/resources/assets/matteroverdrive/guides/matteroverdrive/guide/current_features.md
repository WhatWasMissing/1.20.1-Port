---
navigation:
  title: Current Features
  position: 2
  icon: matteroverdrive:data_pad
---
# Current Features - 0.7

This page is the compact in-game status reference for the current **Matter Overdrive 0.7** release line, maintained by MVQ1303. The individual GuideME system pages are the how-to guide. Manual verification remains tracked in `docs/testing/TO_TEST.md`.

Implemented does not mean runtime-verified. Use the repository testing documents for the current regression plan.

## Matter and machines

Implemented on the core line: Decomposer, Recycler, Matter Analyzer, Pattern Drives, Pattern Storage, Pattern Monitor, Replicator, Molecular Inscriber, Matter Scanner, Portable Decomposer, Matter Containers, Matter Pipe, Solar Panel, Heavy Energy Cable, Microwave, Space-Time Accelerator, Charging Station, Drone Fabricator, Transporter, Tritanium Crates, Weapon Station and Tritanium Wrench.

Major machines expose server-backed operator pages and inventories rather than decorative controls. Machine redstone/configuration parity is restored where a real backend equivalent exists.

### Matter economy in 0.7

Matter values resolve through dynamic Matter Dust values, explicit values, tag bases, recursive recipe derivation and deterministic fallbacks. Analyzer and Decomposer use the level-aware value path, Pattern Drives preserve the analyzed value, and Replicator consumes that stored value.

Matter tooltips expose the effective value and diagnostic commands are available under `/matteroverdrive matter` for value inspection, auditing and cache clearing.

## 0.7 tech overhaul

The merged 0.7 line adds the following connected systems:

- **Grid Capacitor** - 16,000,000 FE high-throughput grid buffer.
- **Energy Bank** - independent 32,000,000 FE / 1,000,000 Matter reactor buffer with FE and Matter network output.
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

These features use the existing Matter Overdrive registries, FE/Matter capability paths and network policy system. Runtime verification remains deferred to the release test plan.

See [Advanced Infrastructure](advanced_infrastructure.md), [Matter Network](network.md), [Power and Machines](power.md) and [Matter Technology](matter.md) for operating instructions.

## Fusion and gravity

Implemented: Fusion Reactor structure validation, Controller/IO shared storage, anomaly-mass-scaled output, upgrades, cable output, demand telemetry, shared ring power, RUN/SCRAM, redstone/comparator behavior, Reactor Remote, persistent overlay, Gravitational Anomaly mass/pull/event horizon, Equalizer and powered Stabilizers. Recovered Reactor Symbiote protocols let an Android carrying a linked Remote draw up to 1,200 FE/s from that same-dimension loaded reactor only while it is actively generating; the lookup never force-loads its chunk.

The **Anomaly Containment Unit** can capture an anomaly, preserve its mass and redeploy it elsewhere so reactor/base placement is no longer dictated by natural anomaly position.

## Matter Network

Implemented core systems: Network Pipe, Network Switch, Network Router and matching-channel Pylon routing. Router filtering, Network Flash Drive destination filtering, upgrade slots, multi-stack budgets and FE/item execution accounting are present. The craftable Matter Network Terminal provides bounded remote Matter Container push/pull access with aggregate status and comparator output. The Matter Network remains intentionally separate from an AE2 ME network.

The 0.7 line extends this with channels, router priority, per-side resource policies, facility telemetry and the Network Diagnostic Probe.

## Androids and entities

Implemented: Android conversion, FE/HUD, body parts, persistent skill tree, class/loadout systems, five-slot chassis hardware, Android Station equipment management, Android Spawner ownership/squad behavior, Rogue Android squads, Rogue/Ranged Rogue Androids, Failed creatures, Mad Scientist, Mutant Scientist and Drone.

Linked Drones support FOLLOW, HOLD, DEFENSIVE, PASSIVE, AGGRESSIVE and persistent PATROL modes with a dedicated Drone Management screen. Reactor Maintenance drones can top up a nearby Gravitational Stabilizer from their charged reserve while their operator works in the containment room. Logistics Cores can be sneak-configured against any item inventory; a routed Logistics drone flies only to that loaded target, then moves nearby drops into it through the server-side item capability without force-loading chunks. The console displays each route and has a LOGISTICS command for configured drones. A powered Charging Station is also a Drone Bay: its bounded cached service recharges the fleet and repairs up to two nearby loaded drones per second for 400 FE per health, with live repair telemetry in the station screen. The console can safely RECALL loaded linked drones to clear formation positions around their operator, returning them to FOLLOW without allowing another player to move them. The 0.7 release adds Android Induction Relay wireless charging and fixes Capacitor Core capacity so its extra 50,000 FE is usable storage.

The Drone Fabricator is the fleet's survival assembly line. Feed it 1 Plasma Core, 2 Isolinear Circuit Mk2s, and 4 Tritanium Plates; connect FE and select Combat, Repair, Logistics, Survey, or Reactor before the 240-tick assembly cycle completes. It consumes 12,800 FE per core, retains inputs during a power outage, exposes its item inventory for local automation, and pulls only missing recipe ingredients from adjacent item-handler inventories at a bounded 16-item/tick rate. Two upgrade slots accept Speed or Power modules: Speed shortens the cycle while Power increases its FE/t demand. The selected role is written directly into the output Drone Core.

Village Field Scientists, Systems Engineers and Mad Scientists now form the player-facing research cast. Their conversations, issued assignments and first-contact lore are recorded by the existing PDA field log, while objectives remain normal transferable contracts.

Facility caches now have rare, themed **Legendary Relics** that install the existing Android passive protocols: Overclocked Relay, Swarm Beacon, Aegis Prism, Hunter Lens, Nanite Crown, Capacitor Heart and Phase Anchor. Their themed drops connect facility exploration to meaningful Android build choices without adding another progression currency.

Vanilla exploration is also part of the progression route: practical Matter Overdrive supplies are injected into selected vanilla structure chests, while the seven Legendary Relics have rare themed drops in strongholds, temples, bastions, mansions, buried treasure, End Cities and Ancient Cities. Rogue Androids, ranged Rogue Androids, Drones, Assimilators, Phase Stalkers and rare Mutant Scientists can naturally appear in the Overworld; the three progression NPCs remain village-associated.

## Weapons

Implemented: Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool with FE payment, heat/overheat, reload and module effects. The Vex Mythoclast remains a separate Matter Overdrive exotic. The 49-profile native Destiny set includes the retained profiles plus 35 additional GunPack conversions with imported geometry, textures, source audio, available animations and per-weapon display transforms. All Destiny weapons use the existing Energy Weapon and Weapon Station battery/barrel/sights/colour/utility module contracts. First-person rendering applies source transforms with bounded ADS/recoil presentation, while third-person generic held-use posing is suppressed. Recovered Thermal Lattice artifacts let Android users trade rare exploration loot for lower energy-weapon heat and faster cooling. Recovered Reactor Symbiote protocols make a linked, actively generating Fusion Reactor a portable Android charging source through the Reactor Remote. Exact remaining module mesh placement and full first-person hand choreography still need visual parity work.

## World content

Implemented structure families: crashed spacecraft, cargo ships, underwater bases, Mad Scientist houses, Android Houses and Sand Pits, with persisted salvage and inhabitants. Six modern technology facilities and four Frontier Expedition sites use native exploration pieces, stable variants, readable routes and persistent discovery/restoration hooks. Natural Gravitational Anomalies generate in fresh Overworld chunks. Compact Matter Labs, Android Relay Outposts, Anomaly Research Sites, Matter Observatories and Field Logistics Depots add stable-footprint exploration infrastructure, deterministic salvage caches, persistent Data Pad field discoveries and site-specific research dossiers. A dossier must now be secured at a powered Matter Analyzer: the 400-tick, 102,400 FE analysis job persists its operator and progress, pauses safely when its owner is offline, then awards that player’s XP, equipment and research clearance. Recovered Artifacts decode into one-use Android passive protocols directly in the field, so exploration rewards can immediately alter a build. Exact old PNG-template geometry is not yet complete for every structure.

## Dimensional Pylon

Implemented backend: source-backed 2x3x2 multiblock identity, shared energy/matter state, charge/generation/drain behavior and GUI telemetry, while unformed blocks retain the modern relay compatibility behavior. Exact legacy visual effects remain a visual-testing item.

## Security and quests

Implemented: Empty/Claim/Access/Remove security protocols, security-aware wrench dismantling, contracts, staged objectives, restored legacy scientist quests, persistent player-scoped compact-site discoveries and the modern research progression from Matter Technology through Anomaly Engineering. Each player receives their own discovery count, dossier and one-time exploration reward; archiving a dossier also advances the shared research clearance to the earliest tier supported by that facility. Quests guide progression but do not act as mandatory recipe/technology gates. Richer legacy-style cinematic dialogue remains presentation work.

## Optional integrations

When GuideME is installed, this manual is the primary player-facing how-to guide and the Data Pad remains the scanning/contract tool. When Applied Energistics 2 is installed, public Forge item capabilities allow normal Storage Bus/import/export-style automation on audited Matter Overdrive inventories without merging ME and Matter Overdrive networks or inventing AE-to-FE conversion.

## Remaining high-value work

- Runtime-test and harden the merged 0.7 systems using `docs/testing/TO_TEST.md`.
- Exact legacy structure templates and deeper structure-specific scripted events.
- Remaining Drone flying/renderer/equipment polish.
- Remaining machine-specific GUI/detail parity where backed by real state.
- Exact Dimensional Pylon visual effects.
- Remaining weapon model placement and first-person animation choreography.
- Deeper source-backed dispatcher/broadcaster network concepts where they map cleanly to the current routing core.
- Richer dialogue presentation and remaining source-backed quest detail.

For operating instructions, return to the [Matter Overdrive index](index.md) and choose the relevant system page.
