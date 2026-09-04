# Matter Overdrive 1.20.1 - Current Feature Reference

Branch: `testing/main`
Legacy references:
- Original Matter Overdrive 1.7.10 source branch `simeonradivoev/MatterOverdrive@1.7.10` (`0.4.2`).
- Matter Overdrive 1.12.2 `0.7.1.0` jar and recovered source/resources.
Build identity: `Alpha Version 3`, made by MVQ1303

This is the source-of-truth feature summary and is bundled in-game as **Current Feature Reference**.

## Latest runtime-confirmed results

- Rogue Android combat and sounds work.
- Failed Cow, Pig, Sheep and Chicken spawn and behave correctly.
- Mad Scientist interaction and the current Puny Humans quest slice work.
- Holo Sign thin geometry and renamed-item programming work.
- Restored machine-model pass runtime-tested successfully apart from the specifically tracked visual regressions now patched below.

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
- Tritanium Wrench rotates blocks on normal use and now restores the original 1.7 sneak-dismantle mode for Matter Overdrive blocks through the normal server break path.

## Fusion Reactor / gravity

- Horizontal reactor validation, Controller/IO shared storage, mass-scaled generation, upgrades, long cable output and demand telemetry.
- Ring power sharing, RUN/SCRAM, redstone/comparator modes, Reactor Remote and placement overlay.
- Persistent Gravitational Anomaly mass/pull/event horizon and living-entity mass contribution.
- Space-Time Equalizer and powered Gravitational Stabilizers.

## Weapons

Playable Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool with FE payment, heat/overheat, reloads, Batteries/HC Batteries, Energy Packs and current module effects. Base transforms are restored; complete legacy module meshes, recoil, zoom and remaining first-person presentation remain deeper parity work.

## Android

Persistent conversion, FE/HUD, four body-part slots, part-gated abilities, V/B/K controls and the modern selectable 30-perk tree with level persistence and refund/reset flows.

## Security

Empty/Claim/Access/Remove protocols, owner binding, machine ownership, matching Access permission and matching Remove clearing are implemented across Matter Overdrive block entities. Wrench dismantling uses the normal server destruction path so the same break-security event can deny unauthorized removal.

## Legacy entities / quests

- Real Rogue Android with levels, legendary state, sounds, drops and Spawner integration.
- Real Failed Cow/Pig/Sheep/Chicken.
- Mad Scientist normal/Junkie persistence.
- Puny Humans quest and one-time Battery + Blue Pill + five Yellow Pill reward.

Cocktail of Ascension, Mutant Scientist, ranged Androids/drones and the broader legacy dialog framework remain future parity work.

## Source-faithful visuals

Restored machine/display models include Holo Sign, Android Station, Weapon Station, Star Map, Contract Market, Matter Analyzer, Decomposer, Recycler, Microwave, Pattern Monitor, Pattern Storage, Replicator, Charging Station, Space-Time Accelerator, Solar Panel, Tritanium Crates and Inscriber.

Further restored/fixed:
- Matter Pipe, Heavy Energy Cable and Network Pipe centre-plus-directional-arm models/states.
- Original source texture assignments for Reactor Coil, Controller, IO, Gravitational Stabilizer and several decorative blocks.
- Pattern Drive empty/partial/full icons and Matter Scanner offline/online icons.
- Shared original Matter Overdrive GUI slot artwork.
- Pattern Monitor, Space-Time Accelerator and Pattern Storage are non-occluding so adjacent block faces remain visible.
- Holo Sign text is anchored to the physical screen and the latest pass flips the text onto the correct readable side.
- Pylon model was rebuilt within valid modern model-bake bounds after the previous oversized JSON produced a purple/black missing model.
- Industrial Glass suppresses shared internal faces between adjacent glass blocks, restoring the core connected-glass behaviour from the original 1.7 ForceGlass implementation.

## 1.7.10 parity work now incorporated

The original 1.7.10 source is now a first-class reference alongside 1.12.2. The first comparison pass restored:
- ForceGlass-style connected internal-face suppression for Industrial Glass.
- Navigable Star Map presentation: mouse-wheel zoom and click-drag pan are restored as a first step toward the original astronomical UI.
- Tritanium Wrench sneak-dismantle for Matter Overdrive blocks, routed through normal server destruction so Security Protocol break checks remain active.
- Original source behaviour is now being used to audit machine redstone modes, network drives, Android abilities and weapon presentation rather than relying on the later 1.12 build alone.

The original machine framework also included generic redstone modes (none/high/low); the current port has reactor-specific redstone handling but not yet the complete generic per-machine system.

## Major remaining parity gaps

1. Cocktail of Ascension, Mutant Scientist and deeper quest/dialog framework.
2. Ranged Rogue Androids, drones and richer entity AI/team/equipment systems.
3. Crashed/cargo ships, underwater bases, Mad Scientist houses and remaining world events.
4. Full 1.7/1.12 Star Map galaxy/star/planet data model, selection, travel and events beyond the newly restored zoom/pan UI.
5. Generic legacy machine redstone modes and additional machine configuration controls.
6. Exact legacy Pylon multi-block/animated overlay and other renderer-specific glow layers.
7. Full weapon module meshes, recoil, zoom and remaining hand animations.
8. Deeper Network Flash Drive configuration and optional legacy mod integrations.
9. Exact old per-machine GUI backgrounds where modern menu coordinates differ.

See the in-game **M2 Testing Checklist** for the current runtime pass.
