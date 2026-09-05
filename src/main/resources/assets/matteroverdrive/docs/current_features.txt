# Matter Overdrive 1.20.1 - Current Feature Reference

Branch: `testing/main`
Legacy references:
- Matter Overdrive 1.7.10 `0.4.2`.
- Matter Overdrive 1.12.2 `0.7.1.0`.

The 0.8 alpha jar is not a parity authority.
Build identity: `Alpha Version 3`, made by MVQ1303

This is the source-of-truth feature summary and is bundled in-game as **Current Feature Reference**. “Implemented” does not mean runtime-confirmed; use the M2 Testing Checklist for verification status.

## Runtime-confirmed baseline

- Rogue Android combat and sounds.
- Failed Cow, Pig, Sheep and Chicken spawning/behaviour.
- Mad Scientist interaction and Puny Humans quest baseline.
- Holo Sign thin geometry and renamed-item programming.
- Earlier matter/reactor/network/wrench/Star Map baseline and the Charging Station / Accelerator / deeper Star Map code through `7fee056` were GitHub-Actions build verified.

## Matter / replication

Implemented Decomposer, Recycler, Matter Analyzer, Pattern Drives, Pattern Storage, Pattern Monitor, Replicator, Inscriber/circuit progression, Matter Scanner, Portable Decomposer, Matter Containers and Matter Pipe integration. Legacy-inspired Home/Tasks/Config/Upgrades presentation is wired where real server-side state exists; real container slots remain visible on every page.

## Power / machines / logistics

- Solar Panel, Heavy Energy Cable, Microwave and Space-Time Accelerator.
- Charging Station restores legacy nearby-Android wireless charging while retaining modern FE-item charging. Base Android range/rate and Range/Power/Power Storage upgrades are implemented.
- Space-Time Accelerator uses the 1.12 `[-radius, radius)` footprint and exposes live task/upgrade telemetry.
- Transporter supports direct Transport Flash Drive use plus persistent machine-side imported destinations, selection/removal, named drive imports, legacy range validity and the 3-entity-per-cycle cap.
- Tritanium Crates and Weapon Station are functional.
- Tritanium Wrench rotates normally and sneak-dismantles through the normal security-aware server break path.

## Item network

- Network Pipe, Network Switch, Network Router and matching-channel Pylon links.
- Network Switch persists enabled state and visibly changes between inactive/active legacy textures.
- Network Router supports ordinary item filtering and 1.7-style Network Flash Drive destination filtering.
- Network Flash Drive stores a persistent `CONNECTIONS` endpoint set; an installed empty drive allows no destinations.
- Router has four real Speed/Hyper-Speed upgrade slots. Speed upgrades increase the per-tick item budget and the executor can spend that budget across multiple stack moves rather than only changing a display number.
- Router telemetry exposes FE, endpoints, nodes, pylons, routing mode, destination count, current item budget and last moved amount.

There is no standalone legacy Network Controller machine in the authoritative 1.7/1.12 implementations; `network_controller.png` is not being treated as evidence for a fake block.

## Fusion Reactor / gravity

Implemented horizontal structure validation, Controller/IO shared storage, anomaly-mass-scaled output, upgrades, cable output, connected demand telemetry, shared ring power, RUN/SCRAM, redstone/comparator behaviour, Reactor Remote and persistent placement/debug overlay. Gravitational Anomaly mass/pull/event horizon, living-entity mass contribution, Space-Time Equalizer and powered Gravitational Stabilizers are present. Reactor GUI exposes legacy-inspired dual FE/matter rings plus current fault/output/hazard telemetry.

## Weapons

Playable Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool with FE payment, heat/overheat, reload, Battery/HC Battery/Energy Pack support and current module effects. Weapon Station exposes the real Battery, Color, Barrel, Sights and two Utility module roles plus live loadout/stat preview. Full legacy module meshes, recoil, zoom and remaining first-person animation parity are still incomplete.

## Android player system

Persistent conversion, FE/HUD, HEAD/CHEST/ARMS/LEGS part state, part/level-gated abilities, V cycle, B activate, K skill tree and a selectable 30-perk tree with persistence/refund flows. Android Station has server-authoritative ability cycling, direct skill-tree access and restored legacy bionic/feature icon presentation.

## Android Spawner / Rogue Androids

- Melee Rogue Androids have levels, legendary state, legacy-scaled health/damage, sounds and drops.
- Ranged Rogue Android entity is implemented and uses Phaser Rifle / Ion Sniper equipment with ranged attacks.
- Android Spawner is FE-powered and restores the 1.7 population model: maximum 6 owned Androids and a 30% melee / 70% ranged spawn mix.
- Spawned Rogue Androids persist their originating spawner position so unrelated/natural Androids do not block a machine’s population cap.
- The six legacy Transport Flash Drive slots are real again. Bound same-dimension drives become patrol waypoints; newly spawned Androids inherit and persist the waypoint list and patrol it only while they have no combat target.
- Android Spawner operator screen reports FE, owned population, max population, next-spawn timing and active patrol target count. `KILL OWNED` removes only Androids owned by that spawner.
- Dismantling the Spawner returns installed patrol drives.
- Legacy team/color configuration remains withheld until equivalent modern squad/team state exists; no fake controls are shown.

## Legacy entities / quests

- Real Failed Cow/Pig/Sheep/Chicken.
- Mad Scientist normal/Junkie state persists.
- Puny Humans quest and one-time Battery + Blue Pill + five Yellow Pill reward.
- Cocktail of Ascension is implemented: 5 Creepers killed with a shovel, 5 gunpowder and 5 red mushrooms, followed by Junkie Scientist transformation.
- Cocktail completion is transactional: ingredients and completion state are committed only after the Mutant Scientist successfully spawns.
- Mutant Scientist is implemented with 256 HP, 0.25 speed, 4 base damage, restored 1.0 x 2.3 dimensions and broad legacy-style hostility toward living entities except other Mutant Scientists.
- Drone entity is implemented. Owner UUID persists; owned drones do not attack their owner and same-owner drones are allied. Broader owner/team command systems remain incomplete.

## Star Map / contracts

Contract Market and contract counting are present. Star Map navigation reaches **Galaxy -> Quadrant -> Star -> Planet**, with deterministic planet type, orbit, habitability, temperature, gravity, moons and atmosphere data plus wheel zoom, drag pan and right-click back navigation.

A real server-authoritative Star Map journey layer is now implemented. Each Star Map machine persists its current galactic position, requested destination, active-travel flag, start/end times and total duration. The Planet page exposes real `TRAVEL`, `EN ROUTE` and `CURRENT LOCATION` states backed by synchronized server data rather than a decorative action. Travel requests are validated server-side against the player's currently open Star Map menu, the actual block position, interaction distance and catalog destination before state can change.

Travel timing preserves the useful legacy concept found in 1.7: interstellar travel is based on 8 time units per LY and same-system travel on 10 per AU. Because the modern deterministic catalog does not contain the original generated galaxy save format, star coordinates and planet orbit numbers act as stable LY/AU stand-ins for those multipliers. The 1.12 Star Map itself had no machine-energy capacity, so no invented FE travel charge has been added.

This restored journey state is intentionally not represented as physical player teleportation. Full legacy ship/fleet ownership, travel attacks, planet/star events, dimension/planet arrival gameplay and the richer generated astronomical/event backend remain incomplete. The 1.7-only `TravelEvent`/ship-attack machinery is therefore not being transplanted literally over the later 1.12 design.

## Security

Empty/Claim/Access/Remove protocols, owner binding, machine ownership, matching Access permission and matching Remove clearing are implemented across Matter Overdrive block entities. Wrench dismantling uses the same server security path.

## GUI / source-faithful presentation

The port reuses the original scalable machine shell and original slot/progress/FE/matter assets while retaining current 1.20.1 menus and server state. Decomposer, Replicator, Analyzer, Inscriber, Transporter, Charging Station, Accelerator, Reactor, Android Station, Android Spawner and Weapon Station have dedicated legacy-inspired operator passes. The design rule is to never hide a real container slot behind a page and never add a legacy button without a real server-side handler.

Restored/fixed visual systems also include directional Matter/Network/Heavy Energy pipe arms, Reactor component face assignments, Pattern Drive fill states, Matter Scanner linked state, non-occluding Pattern Monitor/Storage/Accelerator, readable Holo Sign face, valid Pylon model bounds, Industrial Glass shared-face suppression and active/inactive Network Switch presentation.

## Major remaining parity gaps

1. Crashed/cargo ships, underwater bases, Mad Scientist houses and remaining legacy world structures/events.
2. Star Map planet/star event gameplay, legacy ship/fleet travel combat and richer generated astronomical data beyond the restored server-authoritative journey state.
3. Android Spawner team/color configuration and richer Android squad/path behaviours beyond the restored waypoint patrol loop.
4. Broader Drone ownership/team/command behaviour and remaining entity equipment/AI details.
5. Generic legacy machine redstone/configuration modes where current machines still lack server-side equivalents.
6. Remaining machine-specific legacy GUI elements/pages that map to real current backend state.
7. Exact legacy Pylon multiblock/animated overlay and renderer-specific glow layers.
8. Full weapon module meshes, recoil, zoom and remaining first-person hand animations.
9. Deeper legacy network dispatcher/broadcaster concepts where they can be mapped without replacing the working modern Forge item-routing core.
10. Broader legacy dialog/quest framework beyond Puny Humans and Cocktail of Ascension.

See the in-game **M2 Testing Checklist** for the current runtime pass.
