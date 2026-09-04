# Matter Overdrive 1.20.1 - Current Feature Reference

Branch: `testing/main`
Legacy reference: MatterOverdrive 1.12.2 `0.7.1.0` jar and recovered source/resources
Build identity: `Alpha Version 3`, made by MVQ1303

This is the source-of-truth feature summary and is bundled in-game as **Current Feature Reference**.

## Latest runtime-confirmed results

- Rogue Android combat and sounds work.
- Failed Cow, Pig, Sheep and Chicken spawn and behave correctly.
- Mad Scientist interaction and the current Puny Humans quest slice work.
- Holo Sign thin geometry and renamed-item programming work.
- The first large source-model pass was runtime-tested successfully: restored stations/displays/machines worked, with only neighbour-face culling reported beside Pattern Monitor and Space-Time Accelerator.

## Matter / replication

- Decomposer, Recycler, Analyzer, Pattern Drives, Pattern Storage, Pattern Monitor and Replicator.
- Inscriber and circuit progression.
- Matter Scanner, Portable Decomposer and Matter Containers.
- Matter Pipe routing and storage integration.

## Power / logistics / utility

- Solar Panel, Charging Station, Heavy Energy Cable, Microwave and Space-Time Accelerator.
- Network Pipe, Network Switch, Network Router and matching-channel Pylon routing.
- Transporter / Transport Flash Drive.
- Tritanium Crates and Weapon Station.

## Fusion Reactor / gravity

- Horizontal reactor validation, Controller/IO shared storage, mass-scaled generation, upgrades, long cable output and demand telemetry.
- Ring power sharing, RUN/SCRAM, redstone/comparator modes, Reactor Remote and placement overlay.
- Persistent Gravitational Anomaly mass/pull/event horizon and living-entity mass contribution.
- Space-Time Equalizer and powered Gravitational Stabilizers.

## Weapons

Playable Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool with FE payment, heat/overheat, reloads, Batteries/HC Batteries, Energy Packs and current module effects. Base transforms are restored; the complete legacy module-mesh/recoil/zoom renderer remains deeper parity work.

## Android

Persistent conversion, FE/HUD, four body-part slots, part-gated abilities, V/B/K controls and the modern selectable 30-perk tree with level persistence and refund/reset flows.

## Security

Empty/Claim/Access/Remove protocols, owner binding, machine ownership, matching Access permission and matching Remove clearing are implemented across Matter Overdrive block entities.

## Legacy entities / quests

- Real Rogue Android with levels, legendary state, sounds, drops and Spawner integration.
- Real Failed Cow/Pig/Sheep/Chicken.
- Mad Scientist normal/Junkie persistence.
- Puny Humans quest and one-time Battery + Blue Pill + five Yellow Pill reward.

Cocktail of Ascension, Mutant Scientist, ranged Androids/drones and the broader legacy dialog framework remain future parity work.

## Source-faithful visuals now in testing/main

Previously restored and runtime-tested: Holo Sign monitor body, Android Station, Weapon Station, Star Map, Contract Market, Matter Analyzer, Decomposer, Recycler, Microwave, Pattern Monitor, Pattern Storage OBJ, Replicator, Charging Station OBJ, Space-Time Accelerator, Solar Panel, Tritanium Crates, Inscriber, armour and weapon base transforms.

Second pass now build-verified:

- Pattern Monitor and Space-Time Accelerator no-occlusion correction for neighbouring faces.
- Holo Sign text anchored to the monitor FACING plane rather than camera-billboarded.
- Matter Pipe, Heavy Energy Cable and Network Pipe centre-plus-directional-arm models/states and outlines.
- Original source texture assignments restored for Reactor Coil, Controller, IO, Gravitational Stabilizer, bright/dark vents, Holo Matrix, striped Tritanium Plate and Decorative Clean.
- Gravitational Stabilizer rotations corrected to match original front-direction convention.
- Pylon upgraded from cube placeholder to a source-proportioned tall multi-part silhouette; exact obsolete CTM/animated OBJ overlay remains separate.
- Original Pattern Drive empty/partial/full icons are driven by stored capacity.
- Original Matter Scanner offline/online icons are driven by link state.
- Shared machine slots use the original `slot_small.png` GUI element without changing modern menu coordinates.

The asset audit found that corresponding original block/item/GUI PNGs already present in the port match the recovered 1.12.2 PNGs; most remaining visual errors were model/state/render usage rather than damaged art.

## Major remaining parity gaps

1. Cocktail of Ascension, Mutant Scientist and deeper quest/dialog framework.
2. Ranged Rogue Androids, drones and richer entity AI/team/equipment systems.
3. Crashed/cargo ships, underwater bases, Mad Scientist houses and remaining world events.
4. Full Star Map galaxy/star/planet simulation and travel/events.
5. Exact legacy Pylon multi-block OBJ/animated CTM overlay and other renderer-specific glow/overlay effects.
6. Full weapon module meshes, recoil, zoom and remaining hand animations/random ecosystem.
7. Exact old per-machine GUI background recreation where modern menu coordinates differ; shared source slot art is restored safely now.
8. Deeper network/flash-drive configuration and optional legacy integrations.

See the in-game **M2 Testing Checklist** for the current runtime pass.
