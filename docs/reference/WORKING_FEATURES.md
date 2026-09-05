# Matter Overdrive 1.20.1 - Current Feature Reference

Branch: `testing/main`
Legacy references:
- Matter Overdrive 1.7.10 `0.4.2`.
- Matter Overdrive 1.12.2 `0.7.1.0`.

The 0.8 alpha jar is not a parity authority.
Build identity: `Alpha Version 3`, made by MVQ1303

This is the source-of-truth feature summary and is bundled in-game as **Current Feature Reference**. Implemented does not mean runtime-confirmed; use the M2 Testing Checklist for verification status.

## Matter / power / machines
Decomposer, Recycler, Matter Analyzer, Pattern Drives, Pattern Storage, Pattern Monitor, Replicator, Inscriber/circuit progression, Matter Scanner, Portable Decomposer, Matter Containers and Matter Pipe are implemented. Solar Panel, Heavy Energy Cable, Microwave, Space-Time Accelerator, Charging Station, Transporter, Tritanium Crates, Weapon Station and Tritanium Wrench are functional. Legacy-inspired Home/Tasks/Config/Upgrades presentation is exposed where real server state exists and physical slots remain visible.

Fusion Reactor/gravity includes horizontal structure validation, Controller/IO shared storage, anomaly-mass-scaled output, upgrades, cable output, demand telemetry, shared ring power, RUN/SCRAM, redstone/comparator behavior, Reactor Remote, persistent overlay, Gravitational Anomaly mass/pull/event horizon, Equalizer and powered Stabilizers.

## Network
Network Pipe, Network Switch, Network Router and matching-channel Pylon routing are present. Switch state persists and changes routing/appearance. Router supports ordinary filtering, Network Flash Drive destination filtering, four Speed/Hyper-Speed slots, multi-stack item budgets and the 10 FE/item execution limit. There is no invented standalone Network Controller because the authoritative legacy implementations do not contain one matching that assumption.

## Androids / entities
Android player conversion, FE/HUD, body parts, abilities, V/B/K controls and persistent selectable perk tree are present. Android Spawner restores a six-unit 30% melee / 70% ranged population, persistent ownership, six Transport Flash Drive patrol slots and PATROL/GUARD/HOLD squad modes with eight colors. Same-Spawner Androids are allies and squad changes propagate to loaded/future units.

Rogue Androids, Ranged Rogue Androids, Failed animals, Mad Scientist, Mutant Scientist and Drone are implemented. Owned Drones persist owner UUID, follow their loaded owner, inherit owner team/allies and do not automatically target unrelated players; unowned Drones remain hostile ranged mobs. Puny Humans and Cocktail of Ascension are implemented, including transactional mutant transformation.

## Weapons
Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool are playable with FE payment, heat/overheat, reload and current module effects. Weapon Station exposes real Battery/Color/Barrel/Sights/Utility slots and stat/loadout preview. Exact module meshes, recoil, zoom and remaining first-person animation parity remain incomplete.

## Restored legacy world structures
Six legacy structure families are now registered through the native Forge 1.20.1 **Feature -> configured feature -> placed feature -> biome modifier** pipeline rather than obsolete chunk hooks.

- **Crashed spacecraft**: rare dry-Overworld wrecks with a damaged tritanium silhouette, striped hull details, bridge glass, crate and Holo Sign. The 1.12 reference used an 11 x 35 image-driven wreck and a 256-block separation concept; the modern placed feature preserves that as the comparatively common restored ship tier using a 1/256 placement rarity.
- **Cargo ships**: much larger intact-ish cargo silhouettes with long deck/hull, upper window rails, lamps, Holo Sign and occasional crates. The legacy cargo generator was dramatically rarer and larger; the modern port retains the rarity hierarchy with a 1/4096 placement rarity.
- **Underwater bases**: ocean-floor-only circular tritanium installations with an Industrial Glass band/dome, deliberately cleared dry interior, Matter Analyzer and blue Tritanium Crate. The legacy underwater base was a 43 x 43 deep-ocean structure with a 2048 separation concept; the modern feature uses ocean biomes, ocean-floor placement and 1/2048 rarity.
- **Mad Scientist houses**: uncommon dry-Overworld white laboratory buildings with glass windows, support beams and real Inscriber, Decomposer and Tritanium Crate set dressing. They use a 1/768 modern placement rarity.
- **Android houses**: restored as rare 19 x 19 machine-built shelters, matching the recovered legacy footprint scale. The modern translation uses a sealed tritanium/white-plate shell, Industrial Glass window bands, support beams, lamps, a divided interior, Holo Sign, blue/standard crates and functioning Matter Analyzer plus Inscriber/Decomposer set dressing. They use a 1/1536 placement rarity.
- **Sand pits**: restored as roughly 32 x 32 desert excavation/crater features. Generation is self-filtering: the feature only commits on sand/red-sand/sandstone terrain, cuts a stepped bowl down to a sandstone floor and exposes a small damaged tritanium wreck with stripe detail, crate and Holo Sign at the bottom. They use a 1/384 candidate rarity before terrain rejection.

These are source-faithful **modern structural translations**, not yet pixel-exact reproductions of every old image-worker template. The Android House and Sand Pit families are no longer missing. Dedicated structure loot filling, structure-specific mob population, exact legacy image/NBT geometry and additional world events remain incomplete. Structure generation requires new chunks and is not retroactive to already-generated terrain.

## Star Map / journey / encounters
Star Map navigation reaches Galaxy -> Quadrant -> Star -> Planet with deterministic planet properties, wheel zoom, drag and right-click back. A server-authoritative journey layer persists current/destination galactic position and timing. Travel requests are validated against the open menu, machine position, player range and catalog destination. Same-system travel preserves the useful legacy 10-per-AU concept and interstellar travel the 8-per-LY concept without inventing FE cost that the later Star Map did not have.

Longer routes schedule one deterministic event: Asteroid Field, Gravitational Slingshot, Rogue Android Intercept, Signal Echo, or the newly restored **Hostile Fleet** encounter. Existing route modifiers and Android boarding parties remain functional.

## Star Map fleet combat
A persistent fleet layer now restores the useful 1.7 concepts that travel events carried a ship state and had a dedicated server attack action, without pretending the removed legacy item/build framework already exists.

- The first player to successfully launch from a Star Map binds that machine's persistent fleet commander UUID; another player cannot take over travel or fire the fleet.
- Baseline fleet state is **100 hull / 60 shields / 20 firepower** plus a persistent victory count.
- Hostile Fleet is a real fifth route encounter. It pauses arrival and generates a destination-derived enemy threat with 60-120 hull and 8-17 firepower.
- The Planet travel control becomes a real `FIRE` / `RECHARGE` combat control backed by a validated C2S packet. Attack cadence is 20 ticks.
- Player volleys damage enemy hull by fleet firepower. Enemy return fire drains shields first and then hull.
- Victory increments the persistent counter and resumes travel while restoring the route time spent paused in combat.
- Defeat performs an emergency Star Map retreat to the previous safe galactic location and leaves the fleet damaged at 35 hull / 0 shields; it does not physically teleport the player.
- New journeys restore shields; successful arrivals repair some hull and restore shields.
- Commander, fleet stats, enemy state and active combat persist in block-entity NBT and synchronize through the Star Map menu.

This is now genuine fleet-combat gameplay, but it is not the complete old ship economy. **Scout/Colonizer item production, ship factories/build queues, multiple ship classes in a composed fleet, Colonizer planet ownership/building creation, physical planet dimensions and player arrival/teleportation remain incomplete.**

## Security / GUI
Empty/Claim/Access/Remove protocols and security-aware wrench dismantling are implemented. Dedicated legacy-inspired operator passes exist across the major machines, with the rule that real slots remain visible and controls are only shown when they have genuine server-side behavior.

## Major remaining parity gaps
1. Exact legacy world-structure templates, dedicated structure loot/encounter population and remaining world events.
2. Star Map ship production/build queues, Scout/Colonizer economy, multi-ship fleet composition, colonization/planet ownership, richer star/planet events and physical arrival gameplay.
3. Deeper Android squad behavior such as follow-player/escort orders, formations and coordinated targeting.
4. Richer Drone command/owner-management UI and remaining flight/render/equipment details.
5. Generic legacy machine redstone/configuration modes where backend equivalents are still absent.
6. Remaining machine-specific GUI pages that map to real backend state.
7. Exact legacy Pylon multiblock/animated overlay and renderer glow layers.
8. Full weapon module meshes, recoil, zoom and first-person animation parity.
9. Deeper dispatcher/broadcaster network concepts where they can be mapped without replacing the working Forge routing core.
10. Broader dialog/quest framework.

See the in-game **M2 Testing Checklist** for runtime verification.