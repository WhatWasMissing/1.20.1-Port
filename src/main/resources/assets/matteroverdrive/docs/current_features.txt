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
Android conversion, FE/HUD, body parts, abilities, V/B/K controls and persistent perk tree are present. Android Spawner restores the six-unit 30% melee / 70% ranged population, persistent ownership, six patrol drives, PATROL/GUARD/HOLD/ESCORT, commander formations, squad colors and coordinated targeting. Rogue/Ranged Rogue Androids, Failed animals, Mad Scientist, Mutant Scientist and Drone are implemented. Linked Drones support FOLLOW/DEFENSIVE/PASSIVE/AGGRESSIVE while unowned Drones remain hostile. Puny Humans and Cocktail of Ascension are implemented.

## Weapons
Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool are playable with FE payment, heat/overheat, reload and current module effects. Weapon Station exposes real Battery/Color/Barrel/Sights/Utility slots and stat/loadout preview. Exact module meshes, recoil, zoom and remaining first-person animation parity remain incomplete.

## Restored legacy world structures
Six legacy structure families use the native Forge 1.20.1 Feature -> configured feature -> placed feature -> biome modifier pipeline: crashed spacecraft, cargo ships, underwater bases, Mad Scientist houses, Android Houses and Sand Pits. They have terrain/rarity guards, persisted structure-specific Tritanium Crate salvage and one-time persistent inhabitants using real current-port Android/Drone/Scientist/Failed-animal entities. Structure Androids are deliberately unowned by Android Spawners and structure Drones begin unowned/hostile. Exact old PNG-template geometry and deeper site-specific scripted objectives remain incomplete.

## Natural gravitational anomalies
Natural Gravitational Anomaly worldgen is restored through configured/placed features and a Forge biome modifier. It uses the conservative 1.7 default frequency (~1/200 candidate chunks) and the shared legacy 2,048-10,240 starting-mass range through the normal anomaly block entity. Generated anomalies immediately use the real attraction, event-horizon consumption, mass growth and block-effect systems. The 1.12 `DimensionalRifts` class inspected in the reference jar is a client-side seeded noise sampler rather than another physical structure generator, so it is not treated as missing world structure generation.

## Star Map navigation / journey / encounters
Star Map navigation reaches Galaxy -> Quadrant -> Star -> Planet with deterministic planet properties, wheel zoom, drag and right-click back. Server-authoritative journeys persist current/destination galactic position and timing, validate requests and preserve the legacy 10-per-AU / 8-per-LY timing concepts without inventing FE cost. Longer routes schedule Asteroid Field, Gravitational Slingshot, Rogue Android Intercept, Signal Echo or Hostile Fleet encounters.

## Star Map fleet combat
A persistent fleet layer restores useful 1.7 travel-event/attack concepts: commander ownership, 100 hull / 60 shield / 20 firepower baseline, deterministic hostile fleets, validated FIRE/RECHARGE combat, shield-before-hull damage, victory count, preserved travel pause and emergency retreat. Combat state persists in Star Map NBT and menu sync.

## Star Map colony / shipyard economy
A first substantial 1.7-style planetary economy layer is now implemented with real server state rather than GUI-only counters.

- Planet ownership and colony buildings are stored in **server-global `SavedData`** keyed by deterministic quadrant/star/planet identity, so ownership survives save/reload and is not tied to one Star Map block.
- The first commander-bound starting planet is bootstrapped as the fleet's homeworld with a **Base + Ship Factory**, providing an entry point into the legacy loop without the old galaxy-homeworld generator.
- A colonized planet begins with a **Base**. Normal colony construction requires ownership/Base state.
- **Scout Ship** production uses the recovered 1.7 build length of **3,600 ticks**.
- **Colonizer Ship** production uses **5,000 ticks**.
- **Ship Factory** construction uses **8,000 ticks** and is required before Scout/Colonizer production.
- **Ship Hangar** construction uses **4,800 ticks**; each Hangar adds the recovered **+2 fleet-space** effect. The current bridge gives a colonized Base two baseline berths.
- Only one construction project can run on a Star Map fleet at a time. Queue action, finish time, target planet and Scout/Colonizer composition persist in block-entity NBT; departure is blocked during active construction.
- On an unowned current planet, deploying a completed Colonizer consumes exactly one Colonizer, establishes the Base and assigns ownership transactionally. Already-owned planets reject colonization without consuming the ship.
- The current-planet Star Map GUI exposes SCOUT, COLONIZER and contextual COLONIZE / BUILD FACTORY / HANGAR controls with real server-side validation and synchronized queue/ship/capacity telemetry.
- Economy requests use a dedicated validated C2S packet and enforce open-menu, block-position, range and commander checks.

This restores the core 1.7 Base -> Factory/Hangar -> Scout/Colonizer -> new Base gameplay dependency while keeping the later navigation/fleet systems intact. Scout/Colonizer composition is currently persistent fleet state rather than restored physical legacy item stacks, and it does not yet alter fleet-combat firepower because the reference material does not justify inventing class combat stats.

## Security / GUI
Empty/Claim/Access/Remove protocols and security-aware wrench dismantling are implemented. Dedicated legacy-inspired operator passes exist across major machines, with real slots remaining visible and controls only shown when they have genuine server-side behavior.

## Major remaining parity gaps
1. Exact legacy world-structure templates and deeper structure-specific scripted objectives/events.
2. Star Map Matter Extractor/Power Generator/Residential production effects, physical ship items, multiple independently travelling fleets, richer star/planet events and physical arrival/dimensions.
3. Drone flying navigation/renderer/equipment parity and richer owner-management presentation.
4. Generic legacy machine redstone/configuration modes where backend equivalents are absent.
5. Remaining machine-specific GUI pages that map to real backend state.
6. Exact legacy Pylon multiblock/animated overlay and renderer glow layers.
7. Full weapon module meshes, recoil, zoom and first-person animation parity.
8. Deeper dispatcher/broadcaster network concepts where cleanly mappable to the working Forge routing core.
9. Broader dialog/quest framework.

See the in-game **M2 Testing Checklist** for runtime verification.
