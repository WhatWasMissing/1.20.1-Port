# Matter Overdrive 1.20.1 - Current Feature Reference

Branch: `testing/main`
Legacy references:
- Matter Overdrive 1.7.10 `0.4.2`.
- Matter Overdrive 1.12.2 `0.7.1.0`.

The 0.8 alpha jar is not a parity authority.
Build identity: `Alpha Version 3`, made by MVQ1303

This is the source-of-truth feature summary and is bundled in-game as **Current Feature Reference**. Implemented does not mean runtime-confirmed; use the M2 Testing Checklist for verification status.

## Matter / power / machines
Decomposer, Recycler, Matter Analyzer, Pattern Drives, Pattern Storage, Pattern Monitor, Replicator, Inscriber/circuit progression, Matter Scanner, Portable Decomposer, Matter Containers and Matter Pipe are implemented. Solar Panel, Heavy Energy Cable, Microwave, Space-Time Accelerator, Charging Station, Transporter, Tritanium Crates, Weapon Station and Tritanium Wrench are functional. Legacy-inspired Home/Tasks/Config/Upgrades presentation is exposed where real server state exists and physical slots remain visible. Decomposer, Matter Recycler and Microwave now use wide HOME/TASKS/UPGRADES operator layouts with persistent access to their real IO and four upgrade slots. Solar Panel uses HOME/GEN/UPGRADES with live generation, sky/light/daylight, buffer and output telemetry while both real upgrade slots remain visible. Replicator, Transporter, Charging Station and Space-Time Accelerator retain their richer real-state operator pages.

Fusion Reactor/gravity includes horizontal structure validation, Controller/IO shared storage, anomaly-mass-scaled output, upgrades, cable output, demand telemetry, shared ring power, RUN/SCRAM, redstone/comparator behavior, Reactor Remote, persistent overlay, Gravitational Anomaly mass/pull/event horizon, Equalizer and powered Stabilizers.

## Network
Network Pipe, Network Switch, Network Router and matching-channel Pylon routing are present. Switch state persists and changes routing/appearance. Router supports ordinary filtering, Network Flash Drive destination filtering, four Speed/Hyper-Speed slots, multi-stack item budgets and the 10 FE/item execution limit. There is no invented standalone Network Controller because the authoritative legacy implementations do not contain one matching that assumption.

## Androids / entities
Android conversion, FE/HUD, body parts, abilities, V/B/K controls and persistent perk tree are present. Android Spawner restores the six-unit 30% melee / 70% ranged population, persistent ownership, six patrol drives, PATROL/GUARD/HOLD/ESCORT, commander formations, squad colors and coordinated targeting. Rogue and Ranged Rogue Androids, Failed animals, Mad Scientist, Mutant Scientist and Drone are implemented. Linked Drones support FOLLOW/DEFENSIVE/PASSIVE/AGGRESSIVE while unowned Drones remain hostile. Puny Humans and Cocktail of Ascension are implemented.

## Weapons
Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool are playable with FE payment, heat/overheat, reload and current module effects. Weapon Station retains the seven real Weapon/Battery/Color/Barrel/Sights/Utility slots on every page and now exposes legacy-inspired HOME/MODULES/STATS operator pages with live energy, heat, overheat, module and effective-stat telemetry.

Legacy aiming presentation is now substantially closer to 1.12: Ion Sniper uses the recovered **0.40 base zoom multiplier** while aimed, and the Sniper Scope module uses the recovered **0.85 scope zoom override** through the same scope-first/base-second rule as legacy `EnergyWeapon#getZoomMultiply`. Scope accuracy/range effects remain server-authoritative through the current weapon module backend. Camera recoil now reacts to actual shot timestamps and heat instead of the previous continuous held-weapon wobble; Ion Sniper uses the recovered legacy 3/4-class recoil distinction as the reference point. Exact remaining module mesh placement and full legacy hand/weapon animation choreography remain incomplete.

Weapon energy sourcing remains constrained to the weapon's own stored energy, its installed creative battery, explicit Energy Packs and real `WeaponBatteryItem` inventory/offhand sources; unrelated energy weapons are not treated as reload batteries.

## Restored legacy world structures
Six legacy structure families use the native Forge 1.20.1 Feature -> configured feature -> placed feature -> biome modifier pipeline: crashed spacecraft, cargo ships, underwater bases, Mad Scientist houses, Android Houses and Sand Pits. They have terrain/rarity guards, persisted structure-specific Tritanium Crate salvage and one-time persistent inhabitants using real current-port Android/Drone/Scientist/Failed-animal entities. Structure Androids are deliberately unowned by Android Spawners and structure Drones begin unowned/hostile. Exact old PNG-template geometry remains incomplete: authoritative Android House is 21x21/yOffset -2 and Sand Pit is 24x24/yOffset -9, while current translations are still approximate.

## Natural gravitational anomalies
Natural Gravitational Anomaly worldgen is restored through configured/placed features and a Forge biome modifier. It uses the conservative 1.7 default frequency (~1/200 candidate chunks) and the shared legacy 2,048-10,240 starting-mass range through the normal anomaly block entity. Generated anomalies immediately use the real attraction, event-horizon consumption, mass growth and block-effect systems. The 1.12 `DimensionalRifts` class is a client-side seeded noise sampler rather than another physical worldgen structure.

## Star Map navigation / encounters
Star Map navigation reaches Galaxy -> Quadrant -> Star -> Planet with deterministic planet properties, wheel zoom, drag and right-click back. Server-authoritative console journeys persist current/destination galactic position and timing, validate requests and preserve the legacy 10-per-AU / 8-per-LY timing concepts without inventing FE cost. Longer routes schedule Asteroid Field, Gravitational Slingshot, Rogue Android Intercept, Signal Echo or Hostile Fleet encounters.

## Star Map fleet combat
A persistent command-fleet layer restores useful 1.7 travel-event/attack concepts: commander ownership, 100 hull / 60 shield / 20 firepower baseline, deterministic hostile fleets, validated FIRE/RECHARGE combat, shield-before-hull damage, victory count, preserved travel pause and emergency retreat. Combat state persists in Star Map NBT and menu sync. Scout/Colonizer counts do not silently change combat firepower because the legacy references do not provide justified class combat values.

## Star Map colony / shipyard economy
The planetary economy is real server state rather than GUI-only counters. Planet ownership, Base, Ship Factory, Hangars, Matter Extractors, Power Generators, Residential buildings, stationed Scout/Colonizer counts and four construction slots live in server-global `StarMapGalaxyData` keyed by deterministic planet identity. Breaking/replacing a Star Map does not delete this state.

### Recovered 1.7 planet capacity model
The 1.7 generators set **base building spaces / base fleet spaces** by planet class:
- Normal planet: **6 / 6**.
- Gas Giant: **2 / 8**.
- Dwarf: **4 / 4**.
- Homeworld override: **8 / 10**.

The 1.7 Base contributes **+2 building spaces**, Residential contributes **+4 building spaces**, and each Ship Hangar contributes **+2 fleet spaces**. The port now uses those values. The deterministic port types map as follows: Terrestrial and Oceanic -> legacy Normal, Gas Giant -> Gas Giant, Dwarf -> Dwarf.

Effective examples before extra Residential/Hangars:
- Normal/Oceanic colony with Base: **8 building capacity / 6 fleet capacity**.
- Gas Giant colony with Base: **4 / 8**.
- Dwarf colony with Base: **6 / 4**.
- Homeworld with Base: **10 / 10**.

Building admission now mirrors the intended 1.7 `Planet.canBuild(IBuilding...)` structure: the planet must have a Base and **completed buildings + queued building projects must remain below effective building capacity**. Ship jobs continue to reserve future fleet berth capacity. This prevents four parallel queues from overbooking either buildings or ships.

The first new commander-bound homeworld keeps the port's compatibility **Base + Ship Factory** bridge so existing 1.20.1 progression is not stranded, but it now receives the recovered homeworld capacities and the **legacy starting Scout**. Existing pre-capacity saves at the deterministic bootstrap planet migrate to the homeworld capacity profile without deleting their Factory or other state.

Build times/effects:
- Scout Ship: **3,600 ticks**.
- Colonizer Ship: **5,000 ticks**.
- Ship Factory: **8,000 ticks** and required for ship production.
- Ship Hangar: **4,800 ticks**, +2 fleet spaces.
- Matter Extractor: **14,400 ticks**, +10 matter / -6 energy.
- Power Generator: **14,400 ticks**, +8 energy / -2 matter.
- Residential: **6,000 ticks**, +10,000 population / -4 energy / -2 matter / +4 building spaces.

The authoritative 1.7 `Planet` has four construction inventory slots. The port mirrors this with four independent persistent projects per planet. Construction continues while the command fleet travels elsewhere. Duplicate Factory jobs and configured safety caps are rejected. Saves from the preceding console-local single queue migrate that active job once into the target planet.

### Planet-local ship fleets
Scout and Colonizer ships live at planets, not at the Star Map block that built them. Completed production adds ships to the build planet, and Planet GUI counts show that planet's stationed fleet. Non-current Planet pages expose SEND S / SEND C for independent transfers. Each dispatch creates a persistent per-ship travel event with owner, source, destination, type, start and duration. Multiple transfers can coexist with the separate command-fleet journey.

A Scout arriving at a friendly colony with room stations there. The authoritative 1.7 Scout `onTravel` hook is empty, so no fake scouting reward is invented. A Colonizer arriving at an unowned planet consumes itself and establishes ownership + Base; if it reaches a friendly colony with capacity it stations there, otherwise it returns to origin. Planet SavedData is authoritative; physical ship tokens remain a tangible representation but are not required to command remote stationed ships.

This supports the strategic loop **homeworld -> balance building/fleet capacity -> queue industry -> travel while industry continues -> build ships -> transfer/colonize -> develop destination -> expand again**.

## Security / GUI
Empty/Claim/Access/Remove protocols and security-aware wrench dismantling are implemented. Major operator screens use the legacy Home/Tasks/Configurations/Upgrades vocabulary only where the underlying 1.20.1 machine has real state to expose. Physical machine/player slots stay visible while side pages change, and no decorative control is added for an absent backend. Weapon Station, Decomposer, Matter Recycler, Microwave and Solar Panel now follow the wide operator pattern; Transporter, Replicator, Charging Station and Space-Time Accelerator retain their existing richer operator pages, while Fusion Reactor continues exposing its real RUN/SCRAM/redstone/output/hazard telemetry directly.

## Major remaining parity gaps
1. Exact legacy world-structure templates and deeper structure-specific scripted objectives/events.
2. Star Map additional source-backed ship/building classes, richer economic consequences/events and closer legacy galaxy generation/homeworld setup.
3. Drone flying navigation/renderer/equipment parity and richer owner-management presentation.
4. Generic legacy machine redstone/configuration modes where backend equivalents are absent.
5. Remaining machine-specific GUI pages that map to real backend state.
6. Exact legacy Pylon multiblock/animated overlay and renderer glow layers.
7. Remaining weapon module mesh positioning and full first-person hand/weapon animation choreography.
8. Deeper dispatcher/broadcaster network concepts where cleanly mappable to the working Forge routing core.
9. Broader dialog/quest framework.

See the in-game **M2 Testing Checklist** for runtime verification.