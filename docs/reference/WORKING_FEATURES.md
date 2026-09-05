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
Android player conversion, FE/HUD, body parts, abilities, V/B/K controls and persistent selectable perk tree are present. Android Spawner restores a six-unit 30% melee / 70% ranged population, persistent ownership, six Transport Flash Drive patrol slots and four real squad orders: **PATROL / GUARD / HOLD / ESCORT**, plus eight persisted squad colors. ESCORT binds a persistent commander UUID, places idle squad members into deterministic formation positions around that player and resumes formation after combat. The Spawner GUI exposes COLOR, MODE, COMMAND and KILL controls backed by server state. Same-Spawner Androids are allies, commander/team allies are protected, and a valid target acquired by one squad member propagates to nearby idle squad members from the same Spawner for coordinated combat.

Rogue Androids, Ranged Rogue Androids, Failed animals, Mad Scientist, Mutant Scientist and Drone are implemented. Unowned Drones retain hostile ranged behavior. Right-clicking an unowned Drone links it to the player; linked Drones persist owner UUID and a real command mode. Owner right-click cycles **FOLLOW / DEFENSIVE / PASSIVE / AGGRESSIVE**, while owner shift-right-click releases the link. FOLLOW stays with the owner without acquiring combat targets, DEFENSIVE retaliates against valid mobs that hurt the owner, PASSIVE clears combat state, and AGGRESSIVE proactively targets nearby valid hostile mobs while respecting owner/team/same-owner Drone allies. Puny Humans and Cocktail of Ascension are implemented, including transactional mutant transformation.

## Weapons
Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool are playable with FE payment, heat/overheat, reload and current module effects. Weapon Station exposes real Battery/Color/Barrel/Sights/Utility slots and stat/loadout preview. Exact module meshes, recoil, zoom and remaining first-person animation parity remain incomplete.

## Restored legacy world structures
Six legacy structure families are registered through the native Forge 1.20.1 **Feature -> configured feature -> placed feature -> biome modifier** pipeline rather than obsolete chunk hooks.

- **Crashed spacecraft**: rare dry-Overworld damaged tritanium wrecks with stripe/glass detail, Holo Sign and salvage crate. Modern rarity 1/256.
- **Cargo ships**: much larger long-deck cargo silhouettes with upper window rails, lamps, Holo Sign and multiple possible crates. Modern rarity 1/4096.
- **Underwater bases**: ocean-floor-only circular tritanium installations with Industrial Glass band/dome, deliberately dry interior, Matter Analyzer and blue Tritanium Crate. Modern rarity 1/2048.
- **Mad Scientist houses**: uncommon dry-Overworld white laboratories with glass windows, beams, functioning Inscriber/Decomposer and crate. Modern rarity 1/768.
- **Android houses**: rare approximately 19 x 19 machine-built shelters with tritanium/white shell, glass bands, supports, lamps, divided interior, Holo Sign, blue/standard crates and functioning machine set dressing. Modern rarity 1/1536.
- **Sand pits**: roughly 32 x 32 desert excavation/crater features that only commit on sand/red-sand/sandstone-family terrain and expose a damaged tritanium wreck/cache. Candidate rarity 1/384 before terrain rejection.

### Persisted structure salvage
Generated Tritanium Crates are populated directly through the real 54-slot `TritaniumCrateBlockEntity` inventory, so salvage follows the existing NBT/save and crate-item persistence path. Profiles remain structure-specific: crashed ships favor tritanium/matter/batteries/basic circuitry; cargo ships carry larger tritanium/dilithium/machine stores; underwater bases carry refined matter, Mk2 circuitry and occasional integration/pattern hardware; Scientist labs carry machinery/circuitry with rare artifacts; Android houses carry batteries/circuitry/Android parts with possible pills/network drives; Sand Pits carry tritanium/matter with rarer dilithium/artifacts.

### Persistent structure occupants
The six restored families now also populate real current-port entities during the one-time feature placement pass:
- crashed ships: 1-2 mixed melee/ranged Rogue Android defenders;
- cargo ships: 2-4 mixed Android defenders plus an occasional unowned hostile Drone;
- underwater bases: a persistent unowned hostile Drone plus an occasional ranged Rogue Android inside the dry base;
- Mad Scientist houses: one persistent Mad Scientist plus an occasional Failed animal experiment;
- Android houses: 3-4 mixed Android defenders plus an occasional hostile Drone;
- Sand Pits: one Android guardian plus an occasional hostile Drone.

Worldgen occupants call the normal entity spawn/finalization path, receive persistence-required status, and are collision/world-border checked. Structure Rogue Androids deliberately receive no Android-Spawner origin, so they cannot consume a Spawner's six-unit owned population or be affected by `KILL`. Structure Drones deliberately remain unowned until a player explicitly links them.

These remain source-faithful **modern structural translations**, not pixel-exact reproductions of every old image-worker template. Structure-specific population and persisted salvage are no longer missing. Exact legacy geometry and deeper structure-specific scripted objectives/events remain incomplete. Structure generation requires new chunks and is not retroactive.

## Natural gravitational anomalies
Natural Gravitational Anomaly worldgen is restored through the same proper configured/placed-feature + Forge biome-modifier pipeline. Both authoritative legacy versions used a 2,048-10,240 initial matter range; the current anomaly block entity already initializes in that exact range, so naturally placed anomalies use the normal existing runtime path rather than a second mass implementation.

The references disagree substantially on default frequency: 1.7 used **0.005 per chunk (~1/200)** while 1.12 used **0.05 (~1/20)**. Because the current 1.20.1 anomaly has genuine attraction, event-horizon consumption, mass growth and block destruction, the port uses the conservative 1.7 rate. Candidate Y is sampled from roughly sea level +4 through sea level +63, following the later generator's sea-level-based vertical band. The feature only commits into open, fluid-free air and does not delete terrain to force placement.

## Star Map / journey / encounters
Star Map navigation reaches Galaxy -> Quadrant -> Star -> Planet with deterministic planet properties, wheel zoom, drag and right-click back. A server-authoritative journey layer persists current/destination galactic position and timing. Travel requests are validated against the open menu, machine position, player range and catalog destination. Same-system travel preserves the useful legacy 10-per-AU concept and interstellar travel the 8-per-LY concept without inventing FE cost that the later Star Map did not have.

Longer routes schedule one deterministic event: Asteroid Field, Gravitational Slingshot, Rogue Android Intercept, Signal Echo, or **Hostile Fleet**. Existing route modifiers and Android boarding parties remain functional.

## Star Map fleet combat
A persistent fleet layer restores useful 1.7 travel-event/attack concepts without pretending the removed legacy item/build framework already exists.

- first successful launcher binds as persistent fleet commander;
- baseline fleet: **100 hull / 60 shields / 20 firepower** plus victories;
- Hostile Fleet pauses arrival and generates destination-derived 60-120 hull / 8-17 firepower opposition;
- Planet control becomes validated server-side `FIRE` / `RECHARGE`, with 20-tick cadence;
- volleys damage enemy hull, return fire drains shields before hull;
- victory increments count and resumes preserved travel time;
- defeat retreats Star Map state to previous safe galactic location at 35 hull / 0 shields without physically teleporting the player;
- commander/fleet/enemy/combat state persists in block-entity NBT and menu sync.

The old Scout/Colonizer production queue, multi-ship composed fleet, colonization/building economy, physical planet dimensions and player arrival/teleportation remain incomplete.

## Security / GUI
Empty/Claim/Access/Remove protocols and security-aware wrench dismantling are implemented. Dedicated legacy-inspired operator passes exist across major machines, with real slots remaining visible and controls only shown when they have genuine server-side behavior.

## Major remaining parity gaps
1. Exact legacy world-structure templates and deeper structure-specific objectives/events; Dimensional Rifts and other remaining legacy world events still need restoration.
2. Star Map ship production/build queues, Scout/Colonizer economy, multi-ship fleet composition, colonization/planet ownership, richer star/planet events and physical arrival gameplay.
3. Drone flying navigation/renderer/equipment parity and richer owner-management presentation beyond direct interaction commands.
4. Generic legacy machine redstone/configuration modes where backend equivalents are still absent.
5. Remaining machine-specific GUI pages that map to real backend state.
6. Exact legacy Pylon multiblock/animated overlay and renderer glow layers.
7. Full weapon module meshes, recoil, zoom and first-person animation parity.
8. Deeper dispatcher/broadcaster network concepts where they can be mapped without replacing the working Forge routing core.
9. Broader dialog/quest framework.

See the in-game **M2 Testing Checklist** for runtime verification.
