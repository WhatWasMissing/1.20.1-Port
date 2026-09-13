# Matter Overdrive 1.20.1 - Current Feature Reference

Branch: `main`
Release line: `0.6`
Legacy references:
- Matter Overdrive 1.7.10 `0.4.2`.
- Matter Overdrive 1.12.2 `0.7.1.0`.

The 0.8 alpha jar is not a parity authority.
Build identity: `Matter Overdrive 0.6`, maintained by MVQ1303

This is the source-of-truth feature summary and is mirrored by the GuideME **Current Features** page. Implemented does not mean runtime-confirmed; use the M2 Testing Checklist for verification status.

## Matter / power / machines
Decomposer, Recycler, Matter Analyzer, Pattern Drives, Pattern Storage, Pattern Monitor, Replicator, Inscriber/circuit progression, Matter Scanner, Portable Decomposer, Matter Containers and Matter Pipe are implemented. Solar Panel, Heavy Energy Cable, Microwave, Space-Time Accelerator, Charging Station, Transporter, Tritanium Crates, Weapon Station and Tritanium Wrench are functional. Legacy-inspired Home/Tasks/Config/Upgrades presentation is exposed where real server state exists and physical slots remain visible. Decomposer, Matter Recycler and Microwave use wide HOME/TASKS/UPGRADES operator layouts with persistent access to their real IO and four upgrade slots. Solar Panel uses HOME/GEN/UPGRADES with live generation, sky/light/daylight, buffer and output telemetry while both real upgrade slots remain visible. Pattern Storage now uses HOME/DRIVES/UPGRADES while keeping its energy slot, six real Pattern Drive slots and four upgrade slots visible; Pattern Monitor uses PATTERNS/QUEUE while preserving its 12 clickable ghost-pattern request slots. Replicator, Transporter, Charging Station, Space-Time Accelerator and Inscriber retain their richer real-state operator pages.

### 0.6 matter economy
Matter valuation now resolves through dynamic Matter Dust values, explicit item values, tag bases, recursively derived recipe values and deterministic fallbacks. Recipe resolution includes cycle/depth protection, output-count division and cheapest-positive ingredient alternatives. Analyzer and Decomposer use the level-aware value path, Pattern Drives preserve analyzed values, Replicator consumes those stored values, tooltips expose effective matter values and their source, and startup auditing/diagnostic commands are available for coverage checks. Existing Pattern Drives created before the 0.6 valuation pass may need to be re-analyzed to refresh their stored value.

Fusion Reactor/gravity includes horizontal structure validation, Controller/IO shared storage, anomaly-mass-scaled output, upgrades, cable output, demand telemetry, shared ring power, RUN/SCRAM, redstone/comparator behavior, Reactor Remote, persistent overlay, Gravitational Anomaly mass/pull/event horizon, Equalizer and powered Stabilizers. Gravitational Stabilizer now uses HOME/BEAM/UPGRADES pages backed by its synchronized FE draw, redstone mode, beam-block and anomaly-lock telemetry while the real RS MODE control and four upgrade slots remain available.

## Network
Network Pipe, Network Switch, Network Router and matching-channel Pylon routing are present. Switch state persists and changes routing/appearance. Router supports ordinary filtering, Network Flash Drive destination filtering, four Speed/Hyper-Speed slots, multi-stack item budgets and the 10 FE/item execution limit. Heavy Energy Pipe status now exposes live buffer fill and last-output telemetry without inventing configuration controls. There is no invented standalone Network Controller because the authoritative legacy implementations do not contain one matching that assumption.

## Androids / entities
Android conversion, FE/HUD, body parts, abilities, V/B/K controls and persistent perk tree are present. Android Spawner restores the six-unit 30% melee / 70% ranged population, persistent ownership, six patrol drives, PATROL/GUARD/HOLD/ESCORT, commander formations, squad colors and coordinated targeting. Rogue and Ranged Rogue Androids, Failed animals, Mad Scientist, Mutant Scientist and Drone are implemented. Linked Drones support FOLLOW/DEFENSIVE/PASSIVE/AGGRESSIVE while unowned Drones remain hostile. Puny Humans and Cocktail of Ascension are implemented.

## Weapons
Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool are playable with FE payment, heat/overheat, reload and current module effects. Weapon Station retains the seven real Weapon/Battery/Color/Barrel/Sights/Utility slots on every page and exposes legacy-inspired HOME/MODULES/STATS operator pages with live energy, heat, overheat, module and effective-stat telemetry.

Legacy aiming presentation is substantially closer to 1.12: Ion Sniper uses the recovered **0.40 base zoom multiplier** while aimed, and the Sniper Scope module uses the recovered **0.85 scope zoom override** through the same scope-first/base-second rule as legacy `EnergyWeapon#getZoomMultiply`. Scope accuracy/range effects remain server-authoritative through the current weapon module backend. Camera recoil reacts to actual shot timestamps and heat instead of the previous continuous held-weapon wobble; Ion Sniper uses the recovered legacy 3/4-class recoil distinction as the reference point. The first-person aim delta is bounded so it does not double-apply the OBJ display transform, and the third-person generic held-use arm pose is suppressed while a weapon is active.

Weapon energy sourcing remains constrained to the weapon's own stored energy, its installed creative battery, explicit Energy Packs and real `WeaponBatteryItem` inventory/offhand sources; unrelated energy weapons are not treated as reload batteries.

The native Destiny set contains 14 registered weapons with imported Bedrock geometry, animations, textures and 56 matching source sounds from the supplied `destiny-ext` pack. The native loader converts the raw geometry/animation files into vanilla model parts and applies the source separate first/third-person display transforms. The Vex Mythoclast remains a separate Matter Overdrive exotic and uses its documented fallback sound events because the supplied Destiny pack contains no Mythoclast recording.

## Restored legacy world structures
Six legacy structure families use the native Forge 1.20.1 Feature -> configured feature -> placed feature -> biome modifier pipeline: crashed spacecraft, cargo ships, underwater bases, Mad Scientist houses, Android Houses and Sand Pits. They have terrain/rarity guards, persisted structure-specific Tritanium Crate salvage and one-time persistent inhabitants using real current-port Android/Drone/Scientist/Failed-animal entities. Structure Androids are deliberately unowned by Android Spawners and structure Drones begin unowned/hostile. Exact old PNG-template geometry remains incomplete: authoritative Android House is 21x21/yOffset -2 and Sand Pit is 24x24/yOffset -9, while current translations are still approximate.

## Natural gravitational anomalies
Natural Gravitational Anomaly worldgen is restored through configured/placed features and a Forge biome modifier. It uses the conservative 1.7 default frequency (~1/200 candidate chunks) and the shared legacy 2,048-10,240 starting-mass range through the normal anomaly block entity. Generated anomalies immediately use the real attraction, event-horizon consumption, mass growth and block-effect systems. The 1.12 `DimensionalRifts` class is a client-side seeded noise sampler rather than another physical worldgen structure.

## Security / GUI
Empty/Claim/Access/Remove protocols and security-aware wrench dismantling are implemented. Major operator screens use the legacy Home/Tasks/Configurations/Upgrades vocabulary only where the underlying 1.20.1 machine has real state to expose. Physical machine/player slots stay visible while side pages change, and no decorative control is added for an absent backend. Weapon Station, Decomposer, Matter Recycler, Microwave, Solar Panel, Pattern Storage, Pattern Monitor and Gravitational Stabilizer now follow the operator-page pattern; Transporter, Replicator, Charging Station, Space-Time Accelerator and Inscriber retain their existing richer operator pages. Fusion Reactor continues exposing its real RUN/SCRAM/redstone/output/hazard telemetry directly, while Energy Pipe remains a compact status-only screen because it has no source-backed user configuration backend.

## Major remaining parity gaps
1. Exact legacy world-structure templates and deeper structure-specific scripted objectives/events.
2. Drone flying navigation/renderer/equipment parity and richer owner-management presentation.
3. Generic legacy machine redstone/configuration modes where backend equivalents are absent.
4. Remaining machine-specific GUI pages that map to real backend state.
5. Exact legacy Pylon multiblock/animated overlay and renderer glow layers.
6. Remaining weapon module mesh positioning and full first-person hand/weapon animation choreography.
7. Deeper dispatcher/broadcaster network concepts where cleanly mappable to the working Forge routing core.
8. Richer dialogue presentation and remaining source-backed quest detail.

See the in-game **M2 Testing Checklist** for runtime verification.
