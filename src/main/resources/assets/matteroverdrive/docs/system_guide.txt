# Matter Overdrive Alpha Version 3 - Complete System Guide

Made by MVQ1303
Branch: testing/main

This guide explains the current playable systems in the 1.20.1 port. Use the index to jump to a system. The guide remembers the last page you had open.

Status labels:
- PLAYABLE: usable gameplay loop is implemented.
- TESTING: implemented and build-verified, but needs in-world verification.
- PARTIAL: useful implementation exists but legacy parity is not complete.

Status: PARTIAL means a useful implementation exists but legacy parity is not complete.

# Contents
1. First steps and world exploration
2. FE power and batteries
3. Matter Decomposer and Recycler
4. Matter Analyzer and Pattern Drives
5. Pattern Storage / Monitor / Replicator
6. Matter Scanner and Portable Decomposer
7. Matter Containers and Pipes
8. Molecular Inscriber
9. Solar Panel and Charging Station
10. Microwave
11. Space-Time Accelerator
12. Transporter
13. Tritanium Crates
14. Matter Network Router / Switch / Pylons
15. Fusion Reactor
16. Gravitational Anomalies and Stabilizers
17. Energy weapons and Weapon Station
18. Android conversion / HUD / skill tree
19. Rogue Android Spawner squads
20. Drones
21. Scientists / Failed creatures / Cocktail of Ascension
22. Security and Tritanium Wrench
23. Star Map navigation and encounters
24. Star Map fleet combat
25. Star Map colony economy
26. Star Map planet-local ships and transfers
27. Restored structures and salvage
28. Troubleshooting routes

# 1. First steps and world exploration
Status: PLAYABLE / TESTING world content.

## Simplified
- Mine Tritanium and Dilithium.
- Build basic FE and matter machines.
- Explore fresh chunks for Matter Overdrive structures and natural anomalies.
- Salvage crates and fight structure defenders for faster progression.

## Detailed
Tritanium and Dilithium remain the core machine/material progression. Fresh chunks can contain crashed ships, cargo ships, underwater bases, Mad Scientist houses, Android Houses and Sand Pits. These structures contain persisted Tritanium Crate salvage and persistent inhabitants such as Rogue Androids, Ranged Rogue Androids, Drones, Mad Scientists and Failed animals.

Natural Gravitational Anomalies can generate in fresh Overworld chunks at the conservative 1.7-style candidate frequency of roughly 1/200 chunks. Their starting mass is 2,048-10,240 and they immediately use the same pull/event-horizon/mass-growth backend as reactor anomalies.

Important limits: old explored chunks are not retroactively populated, and Android House/Sand Pit geometry is still an approximate modern translation rather than exact old PNG-template parity.

# 2. FE power and batteries
Status: PLAYABLE.

## Simplified
Generate FE with Solar Panels or a Fusion Reactor, move it with Heavy Energy Cable and recharge Batteries/HC Batteries in the Charging Station.

## Detailed
Heavy Energy Cable is the FE transport path. Matter Pipe carries matter and Network Pipe carries network traffic. Normal and HC Batteries store real FE; Energy Packs are consumable emergency weapon energy; Creative Batteries are testing-only unlimited sources.

# 3. Matter Decomposer and Recycler
Status: PLAYABLE.

## Simplified
Put matter-valued items in the Decomposer to create matter. Use the Recycler to recover/refine Matter Dust.

## Detailed
The Decomposer consumes FE and matter-valued items into its internal buffer. Matter Pipe can move that matter into Replicators, Reactor IO and compatible receivers. The intended failure path can create Matter Dust. Recycler processes Matter Dust into refined form while preserving represented matter.

# 4. Matter Analyzer and Pattern Drives
Status: PLAYABLE.

## Simplified
Put a Pattern Drive in the Analyzer and analyze matching items until a pattern reaches 100%.

## Detailed
Normal Pattern Drives store two distinct patterns. Analyzer cycles add progress; completed patterns record item identity and matter cost. Pattern Drives retain data when removed and can be exposed through powered Pattern Storage.

# 5. Pattern Storage / Monitor / Replicator
Status: PLAYABLE.

## Simplified
Connect powered Pattern Storage, Pattern Monitor and Replicator with Network Pipe, select a completed pattern, then supply FE and matter.

## Detailed
Pattern Storage exposes patterns across enabled network paths. Pattern Monitor queues replication. Replicator consumes real FE and matter and outputs the requested item. Network Pipe, Matter Pipe and Heavy Energy Cable remain separate systems.

# 6. Matter Scanner and Portable Decomposer
Status: PLAYABLE.

## Simplified
Link the Matter Scanner to powered Pattern Storage. Configure Portable Decomposer pickup filtering to convert supported drops into matter.

## Detailed
Scanner adds pattern progress only through a valid linked storage path. Portable Decomposer stores FE/matter, intercepts configured pickups and can transfer stored matter into compatible receivers.

# 7. Matter Containers and Pipes
Status: PLAYABLE.

Matter Containers provide portable matter storage. Matter Pipe discovers compatible matter endpoints through connected runs. Use Heavy Energy Cable for FE and Network Pipe for network traffic.

# 8. Molecular Inscriber
Status: PLAYABLE.

Power the Inscriber and use the current circuit chain: Mk1 + Gold -> Mk2, Mk2 + Diamond -> Mk3, Mk3 + Emerald -> Mk4. Battery, cable FE, upgrades and progress state persist.

# 9. Solar Panel and Charging Station
Status: PLAYABLE.

Solar Panels generate FE under valid daylight. Charging Stations recharge compatible energy items and provide current Android wireless charging behavior in range.

# 10. Microwave
Status: PLAYABLE.

A powered food cooker using valid smelting results. It waits if output is blocked and supports relevant upgrades.

# 11. Space-Time Accelerator
Status: PLAYABLE / performance-sensitive.

Consumes FE and matter to add extra ticks to eligible blocks/block entities in its area. Speed, Range, Power and storage upgrades alter pulse/radius/resource behavior. Large ranges can amplify third-party machine load.

# 12. Transporter
Status: PLAYABLE same-dimension transport.

## Simplified
Bind a Transport Flash Drive, insert it into a powered Transporter and stand on the pad.

## Detailed
Transporter validates destination, range, FE and cooldown before moving eligible entities. Legacy-derived behavior includes 1,024,000 FE capacity, up to three entities/cycle and distance-scaled FE use. Exact-required-FE transport should succeed.

# 13. Tritanium Crates
Status: PLAYABLE.

54-slot storage with persistent dropped-item contents and normal Forge automation support.

# 14. Matter Network Router / Switch / Pylons
Status: PLAYABLE.

Router moves items across Network Pipe using 10 FE per item actually moved, supports filters/Network Flash Drive destinations and four Speed/Hyper-Speed upgrade slots. Switches persist enabled state and alter graph routing. Matching-channel Pylons provide the current local wireless bridge; exact legacy multiblock/glow/interdimensional parity is still incomplete.

# 15. Fusion Reactor
Status: TESTING late-game power system.

## Simplified
Build the controller/ring around a Gravitational Anomaly using the guide/overlay, feed matter through Reactor IO, extract FE through IO and control it with RUN/SCRAM/redstone modes.

## Detailed
The reactor validates its horizontal multiblock, links shared storage, scales output from anomaly mass and efficiency, supports upgrades, shared ring power, cable output and demand telemetry. Reactor Remote and persistent assembly overlay are implemented.

# 16. Gravitational Anomalies and Stabilizers
Status: TESTING hazard system.

Anomalies pull entities/items, consume matter-valued drops and living entities inside the event horizon to gain mass, and become more dangerous as mass grows. Equalizer protects the wearer. Powered Stabilizers reduce effective danger while correctly aimed/controlled.

# 17. Energy weapons and Weapon Station
Status: PLAYABLE combat core / PARTIAL visual parity.

Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun require FE, generate heat and can overheat. Batteries/HC Batteries are rechargeable. Weapon Station exposes Battery/Color/Barrel/Sights/Utility slots and current module effects. Exact module meshes, recoil, zoom and first-person animation remain incomplete.

# 18. Android conversion / HUD / skill tree
Status: PLAYABLE / TESTING progression.

Install HEAD/CHEST/ARMS/LEGS parts at Android Station. V cycles abilities, B activates selected ability, K opens the skill tree. Android state stores FE, parts, level/XP, selected ability and persistent perk choices/refunds.

# 19. Rogue Android Spawner squads
Status: TESTING full squad-management layer.

Powered Spawner maintains up to six owned Androids with roughly 30% melee / 70% ranged composition. Six Transport Flash Drive slots define patrol points. Modes PATROL/GUARD/HOLD/ESCORT, squad colors, commander formation, shared targeting and KILL OWNED are implemented. Natural/structure Androids do not consume the owned cap.

# 20. Drones
Status: PLAYABLE command layer / PARTIAL flying parity.

Right-click unowned Drone to link it. Owner right-click cycles FOLLOW -> DEFENSIVE -> PASSIVE -> AGGRESSIVE; shift-right-click releases. Owned Drones protect owner/allies and reject friendly targets. Legacy flying navigation/render/equipment parity remains incomplete.

# 21. Scientists / Failed creatures / Cocktail of Ascension
Status: PLAYABLE restored entity slice.

Mad Scientists, Failed Cow/Pig/Sheep/Chicken and Mutant Scientist exist. Cocktail of Ascension progression only consumes/commits its transformation after the replacement Mutant successfully spawns.

# 22. Security and Tritanium Wrench
Status: PLAYABLE.

Claim/Access/Remove protocols enforce ownership/access. Tritanium Wrench dismantling is security-aware and returns supported state/contents rather than bypassing claims.

# 23. Star Map navigation and encounters
Status: TESTING strategic layer.

## Simplified
Open Star Map, navigate Galaxy -> Quadrant -> Star -> Planet, use wheel/drag/RMB navigation and TRAVEL to move the command fleet.

## Detailed
Planet identities/properties are deterministic. Whole-fleet travel is server-authoritative and persists current/destination/timing. Same-system and interstellar timing preserve the legacy 10-per-AU and 8-per-LY concepts without invented FE cost. Longer routes can trigger Asteroid Field, Gravitational Slingshot, Android Intercept, Signal Echo or Hostile Fleet.

# 24. Star Map fleet combat
Status: TESTING.

Command fleet starts at Hull 100 / Shield 60 / Firepower 20. Hostile Fleet pauses arrival. FIRE obeys cooldown, return fire drains shield before hull, victory resumes preserved travel time and defeat returns to last safe planet at 35 hull / 0 shield. Scout/Colonizer counts do not alter firepower without source-backed values.

# 25. Star Map colony economy
Status: TESTING late-game strategy.

## Simplified
- Every planet has its own persistent colony state.
- `Q n/4` shows how many of that planet's four construction slots are occupied.
- `B used/cap` shows building usage/capacity.
- Ship count/capacity is separate and depends on planet type plus Hangars.
- Queue industry, leave with the command fleet and construction continues.

## Detailed
The 1.7 `Planet` object owned buildings, fleet, ownership, production and a four-slot construction inventory. The port now keeps those concepts in persistent `StarMapGalaxyData` rather than the Star Map block.

Recovered 1.7 **base building / fleet capacities**:
- legacy Normal: **6 / 6**.
- Gas Giant: **2 / 8**.
- Dwarf: **4 / 4**.
- Homeworld override: **8 / 10**.

Port mapping is Terrestrial/Oceanic -> legacy Normal, Gas Giant -> Gas Giant, Dwarf -> Dwarf.

Recovered modifiers:
- Base: **+2 building capacity**.
- Residential: **+4 building capacity**.
- Ship Hangar: **+2 fleet capacity**.

Therefore, before extra Residential/Hangars:
- Terrestrial/Oceanic colony with Base: **8 building / 6 fleet**.
- Gas Giant colony with Base: **4 / 8**.
- Dwarf colony with Base: **6 / 4**.
- Homeworld with Base: **10 / 10**.

This is intentional parity behavior: Gas Giants have little building room but high fleet capacity; Dwarfs have tighter fleet capacity; Normal/Oceanic planets are balanced.

The port now enforces building admission using **completed buildings + queued building projects < effective building capacity**, matching the shape of 1.7 `Planet.canBuild(IBuilding...)`. A Base is required. Queued ship jobs separately reserve fleet capacity. Parallel queues cannot overbook either limit.

The first commander-bound homeworld keeps the current 1.20.1 compatibility **Base + Ship Factory** so existing progression is not stranded, but new homeworlds now also receive the recovered **one starting Scout** and the legacy homeworld 8/10 base capacities. Existing pre-capacity saves at the deterministic bootstrap planet are migrated to that homeworld capacity profile without deleting their current Factory/buildings/ships.

Build times/effects:
- Scout: 3,600 ticks.
- Colonizer: 5,000 ticks.
- Ship Factory: 8,000 ticks.
- Ship Hangar: 4,800 ticks, +2 fleet capacity.
- Matter Extractor: 14,400 ticks, +10 matter / -6 energy.
- Power Generator: 14,400 ticks, +8 energy / -2 matter.
- Residential: 6,000 ticks, +10,000 population / -4 energy / -2 matter / +4 building capacity.

Up to four valid projects can run in parallel. Construction persists independently of the Star Map console and continues while the command fleet travels. A queued Residential still occupies one building space; if a colony is already fully built-out, you cannot queue Residential to escape the cap, matching the legacy admission order.

# 26. Star Map planet-local ships and transfers
Status: TESTING newest feature set.

## Simplified
Scout and Colonizer ships belong to planets. Build them at a colony, then use SEND S / SEND C from a non-current Planet page to transfer them without moving the command fleet.

## Detailed
Completed ship production adds the ship to the planet where it was built and respects that planet's legacy-derived fleet capacity. Ship items may appear locally as tangible tokens, but persistent planet fleet data is authoritative.

Independent dispatch removes one stationed ship from source and creates a persistent per-ship travel event. Multiple events can coexist. Scout arriving at friendly colony with room stations there; the 1.7 Scout travel hook is empty, so no fake reward is added. Colonizer arriving at unowned planet is consumed to establish ownership + Base; if it reaches a friendly colony with room it stations there; otherwise it returns to origin.

This gives the current strategic loop: **homeworld -> choose planet-type capacity strengths -> queue industry -> build/transfer ships -> colonize -> develop destination -> expand again**, while the separate command fleet handles navigation/encounters/combat.

# 27. Restored structures and salvage
Status: TESTING exploration content.

Crashed Ship: salvage plus mixed Rogue Android defenders. Cargo Ship: larger salvage, multiple Android defenders and possible hostile Drone. Underwater Base: salvage plus hostile occupants. Mad Scientist House: circuitry/machine salvage, Mad Scientist and possible Failed animal. Android House: Android/battery/network salvage plus defenders. Sand Pit: lower-tier tritanium/matter salvage with Android guardian and possible Drone.

# 28. Troubleshooting routes

## No FE transfer
Confirm Heavy Energy Cable, not Matter Pipe/Network Pipe. Test a one-cable path first.

## No matter transfer
Use Matter Pipe between compatible matter source/receiver. Reactor matter enters through formed Reactor IO.

## Pattern not visible
Confirm Storage/Monitor/Replicator share an enabled Network Pipe graph and stored pattern is complete.

## Android controls missing
Verify conversion/state, installed parts/perks and V/B/K key bindings. Relog once and report whether HUD/selection persisted.

## Star Map building button will not start
Check `B used/cap`, `Q n/4`, ownership/Base state and whether the requested building already exists or is already queued. Planet type now matters: Gas Giant, Dwarf and Normal/Oceanic colonies intentionally have different legacy-derived capacities.

## Star Map ship will not build/arrive
Check stationed S/C count, fleet capacity and queued Scout/Colonizer reservations. A ship in transit is removed from its source until arrival.

## Star Map old save after updating
The deterministic bootstrap homeworld keeps existing buildings/ships while receiving recovered homeworld capacity rules. Report any duplication or loss immediately.

## Missing/purple textures
Record exact block/item and whether issue is world model, inventory model or equipped model.

## Reporting a bug
Include build commit/JAR name, singleplayer/server, steps to reproduce, expected result, actual result, screenshots if visual, and latest.log/debug.log for crashes or state desync.

Use the separate **Current Feature Reference** for implementation details and **M2 Testing Checklist** for formal verification.
