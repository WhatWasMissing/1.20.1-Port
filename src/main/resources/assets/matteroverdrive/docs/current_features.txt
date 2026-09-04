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
- Previous 1.7 parity baseline through wrench/Star Map/visual fixes is GitHub-Actions build verified. The GUI/network-drive pass below still needs runtime verification.

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
- Tritanium Wrench rotates normally and restores the original 1.7 sneak-dismantle mode through the normal server break path, preserving Security Protocol checks.
- Network Flash Drive restores the original `CONNECTIONS` destination-list concept: right-click inventory endpoints to toggle destinations, then install the drive in a Network Router filter slot to restrict routing destinations. An installed empty drive permits no destinations.

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

Empty/Claim/Access/Remove protocols, owner binding, machine ownership, matching Access permission and matching Remove clearing are implemented across Matter Overdrive block entities. Wrench dismantling goes through the same break-security path.

## Legacy entities / quests

- Real Rogue Android with levels, legendary state, sounds, drops and Spawner integration.
- Real Failed Cow/Pig/Sheep/Chicken.
- Mad Scientist normal/Junkie persistence.
- Puny Humans quest and one-time Battery + Blue Pill + five Yellow Pill reward.

Cocktail of Ascension, Mutant Scientist, ranged Androids/drones and the broader legacy dialog framework remain future parity work.

## Legacy GUI presentation wired to modern logic

The 1.7 GUI framework used a scalable `base_gui_hotbar.png` shell plus reusable elements rather than one fixed background per machine. The port now uses that original scalable shell for modern machine screens while retaining the current 1.20.1 menus, slot coordinates, FE/matter storage, progress calculations, debug buttons and persistence.

- Shared machine screens now render through the original 92x77 nine-slice-style shell instead of the synthetic dark frame.
- Existing original `slot_small.png` remains wired to current menu slots.
- Decomposer uses the original 24x16 progress-arrow element plus original 16x42 FE and matter meters, driven by current live values.
- Replicator uses the same original progress/FE/matter elements, driven by current replication/network state.
- Matter Analyzer uses the original progress/FE elements while retaining modern analysis data and controls.
- Other current machine screens inherit the original scalable shell immediately; machine-specific legacy widgets/background composition will be migrated incrementally where it maps cleanly to the modern menus.

## Source-faithful visuals

Restored machine/display models include Holo Sign, Android Station, Weapon Station, Star Map, Contract Market, Matter Analyzer, Decomposer, Recycler, Microwave, Pattern Monitor, Pattern Storage, Replicator, Charging Station, Space-Time Accelerator, Solar Panel, Tritanium Crates and Inscriber.

Further restored/fixed:
- Matter Pipe, Heavy Energy Cable and Network Pipe centre-plus-directional-arm models/states.
- Original source texture assignments for Reactor Coil, Controller, IO, Gravitational Stabilizer and several decorative blocks.
- Pattern Drive empty/partial/full icons and Matter Scanner offline/online icons.
- Pattern Monitor, Space-Time Accelerator and Pattern Storage are non-occluding so adjacent block faces remain visible.
- Holo Sign text is anchored to the physical screen on its readable side.
- Pylon model is within valid modern model-bake bounds.
- Industrial Glass suppresses shared internal faces, restoring the core 1.7 ForceGlass behaviour.
- Star Map has mouse-wheel zoom and click-drag pan as the first restored astronomical-UI layer.

## Major remaining parity gaps

1. Cocktail of Ascension, Mutant Scientist and deeper quest/dialog framework.
2. Ranged Rogue Androids, drones and richer entity AI/team/equipment systems.
3. Crashed/cargo ships, underwater bases, Mad Scientist houses and remaining world events.
4. Full 1.7/1.12 Star Map galaxy/star/planet data model, selection, travel and events.
5. Generic legacy machine redstone modes (`none/high/low`) and additional machine configuration controls.
6. Machine-specific legacy GUI pages/widgets such as the original Analyzer waveform, task/configuration pages and upgrade-page presentation, while retaining current server logic.
7. Exact legacy Pylon multi-block/animated overlay and other renderer-specific glow layers.
8. Full weapon module meshes, recoil, zoom and remaining hand animations.
9. Additional legacy network dispatcher/broadcaster depth and optional old-mod integrations where modern equivalents are appropriate.

See the in-game **M2 Testing Checklist** for the current runtime pass.
