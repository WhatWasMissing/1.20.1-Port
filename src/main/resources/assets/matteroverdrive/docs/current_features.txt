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
Android conversion, FE/HUD, body parts, abilities, V/B/K controls and persistent perk tree are present. Android Spawner restores the six-unit 30% melee / 70% ranged population, persistent ownership, six patrol drives, PATROL/GUARD/HOLD/ESCORT, commander formations, squad colors and coordinated targeting. Rogue and Ranged Rogue Androids, Failed animals, Mad Scientist, Mutant Scientist and Drone are implemented. Linked Drones support FOLLOW/DEFENSIVE/PASSIVE/AGGRESSIVE while unowned Drones remain hostile. Puny Humans and Cocktail of Ascension are implemented.

## Weapons
Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool are playable with FE payment, heat/overheat, reload and current module effects. Weapon Station exposes real Battery/Color/Barrel/Sights/Utility slots and stat/loadout preview. Exact module meshes, recoil, zoom and remaining first-person animation parity remain incomplete.

## Restored legacy world structures
Six legacy structure families use the native Forge 1.20.1 Feature -> configured feature -> placed feature -> biome modifier pipeline: crashed spacecraft, cargo ships, underwater bases, Mad Scientist houses, Android Houses and Sand Pits. They have terrain/rarity guards, persisted structure-specific Tritanium Crate salvage and one-time persistent inhabitants using real current-port Android/Drone/Scientist/Failed-animal entities. Structure Androids are deliberately unowned by Android Spawners and structure Drones begin unowned/hostile. Exact old PNG-template geometry remains incomplete: the authoritative Android House is 21x21 with yOffset -2 and the Sand Pit is 24x24 with yOffset -9, while the current translations are still approximate.

## Natural gravitational anomalies
Natural Gravitational Anomaly worldgen is restored through configured/placed features and a Forge biome modifier. It uses the conservative 1.7 default frequency (~1/200 candidate chunks) and the shared legacy 2,048-10,240 starting-mass range through the normal anomaly block entity. Generated anomalies immediately use the real attraction, event-horizon consumption, mass growth and block-effect systems. The 1.12 `DimensionalRifts` class is a client-side seeded noise sampler rather than another physical worldgen structure and is not treated as missing structure generation.

## Star Map navigation / encounters
Star Map navigation reaches Galaxy -> Quadrant -> Star -> Planet with deterministic planet properties, wheel zoom, drag and right-click back. Server-authoritative console journeys persist current/destination galactic position and timing, validate requests and preserve the legacy 10-per-AU / 8-per-LY timing concepts without inventing FE cost. Longer routes schedule Asteroid Field, Gravitational Slingshot, Rogue Android Intercept, Signal Echo or Hostile Fleet encounters.

## Star Map fleet combat
A persistent command-fleet layer restores useful 1.7 travel-event/attack concepts: commander ownership, 100 hull / 60 shield / 20 firepower baseline, deterministic hostile fleets, validated FIRE/RECHARGE combat, shield-before-hull damage, victory count, preserved travel pause and emergency retreat. Combat state persists in Star Map NBT and menu sync. Scout/Colonizer counts do not silently change combat firepower because the legacy references do not provide justified class combat values.

## Star Map colony / shipyard economy
The planetary economy is real server state rather than GUI-only counters.

- Planet ownership, Base, Ship Factory, Hangars, Matter Extractors, Power Generators, Residential buildings and **stationed Scout/Colonizer counts** are stored in server-global `StarMapGalaxyData`, keyed by deterministic quadrant/star/planet identity.
- Breaking or replacing a Star Map does not delete planet ownership, buildings or stationed ships.
- The first commander-bound starting planet is bootstrapped as a homeworld with **Base + Ship Factory** so the restored loop can begin without the removed legacy homeworld generator.
- A newly colonized planet begins with a **Base** and no Ship Factory.
- **Scout Ship** build time: **3,600 ticks**.
- **Colonizer Ship** build time: **5,000 ticks**.
- **Ship Factory** build time: **8,000 ticks** and is required for ship production.
- **Ship Hangar** build time: **4,800 ticks**; each Hangar adds the recovered **+2 fleet-space** effect. The current bridge gives a Base two baseline berths.
- **Matter Extractor**: **14,400 ticks**, **+10 matter / -6 energy**.
- **Power Generator**: **14,400 ticks**, **+8 energy / -2 matter**.
- **Residential**: **6,000 ticks**, **+10,000 population / -4 energy / -2 matter / +4 building capacity**.
- Residential happiness follows the recovered positive/negative matter and energy production rule and is synchronized to the Planet GUI.
- Only one colony construction project can run on a Star Map console at once; queue action, target and finish time persist in block-entity NBT and console departure is blocked while construction is active.

### Planet-local ship fleets
Scout and Colonizer ships now **live at planets**, not at the Star Map block that built them.

- Completed ship production adds the ship to the build planet's persistent stationed fleet.
- Planet GUI ship counts always describe the **current planet's stationed ships**.
- Fleet capacity is checked against that planet's Base/Hangar capacity.
- Non-current Planet pages expose **SEND S / SEND C** for independent transfer.
- Dispatch removes one ship from the current planet and creates one server-global persistent per-ship travel event containing owner, source planet, destination planet, ship type, start time and legacy-derived travel duration.
- Multiple independent ship transfers can coexist while the separate command-fleet journey system remains stationary.
- In-transit counts are synchronized to the GUI across the commander's active ship travel events.
- Any loaded Star Map console owned by that commander can settle due arrivals, so a dispatched ship no longer depends on the exact source console surviving.
- A Scout arriving at a friendly colony becomes stationed at that destination. The authoritative 1.7 Scout travel hook is empty, so no fake scouting reward is invented. If there is no friendly berth/capacity, the Scout returns to its origin colony.
- A Colonizer arriving at an unowned planet consumes itself and establishes ownership + Base, matching the recovered 1.7 arrival behavior. If the destination is already a friendly colony and has room, it stations there instead. If it cannot claim or berth, it returns to its origin colony.
- Existing saves from the earlier console-local Scout/Colonizer counter implementation migrate those counts once into the console's current planet when the bound console loads.
- Physical Scout/Colonizer item tokens still materialize on construction with owner/type/planet metadata as a tangible representation. Planet-local SavedData is now authoritative for residency; dispatch opportunistically consumes a matching carried token but does not depend on a token existing, preventing ships stationed on remote colonies from becoming unusable.

This now supports the strategic loop **homeworld -> build ships -> send ships -> colonize/friendly transfer -> develop destination -> build/receive more ships** while keeping command-fleet navigation and hostile-fleet combat as a separate layer.

## Security / GUI
Empty/Claim/Access/Remove protocols and security-aware wrench dismantling are implemented. Dedicated legacy-inspired operator passes exist across major machines, with real slots remaining visible and controls only shown when they have genuine server-side behavior.

## Major remaining parity gaps
1. Exact legacy world-structure templates and deeper structure-specific scripted objectives/events.
2. Star Map additional source-backed ship/building classes, richer economic consequences/events and physical planet dimensions/player arrival.
3. Drone flying navigation/renderer/equipment parity and richer owner-management presentation.
4. Generic legacy machine redstone/configuration modes where backend equivalents are absent.
5. Remaining machine-specific GUI pages that map to real backend state.
6. Exact legacy Pylon multiblock/animated overlay and renderer glow layers.
7. Full weapon module meshes, recoil, zoom and first-person animation parity.
8. Deeper dispatcher/broadcaster network concepts where cleanly mappable to the working Forge routing core.
9. Broader dialog/quest framework.

See the in-game **M2 Testing Checklist** for runtime verification.
