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
23. Restored structures and salvage
24. Troubleshooting routes

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

# 23. Restored structures and salvage
Status: TESTING exploration content.

Crashed Ship: salvage plus mixed Rogue Android defenders. Cargo Ship: larger salvage, multiple Android defenders and possible hostile Drone. Underwater Base: salvage plus hostile occupants. Mad Scientist House: circuitry/machine salvage, Mad Scientist and possible Failed animal. Android House: Android/battery/network salvage plus defenders. Sand Pit: lower-tier tritanium/matter salvage with Android guardian and possible Drone.

# 24. Troubleshooting routes

## No FE transfer
Confirm Heavy Energy Cable, not Matter Pipe/Network Pipe. Test a one-cable path first.

## No matter transfer
Use Matter Pipe between compatible matter source/receiver. Reactor matter enters through formed Reactor IO.

## Pattern not visible
Confirm Storage/Monitor/Replicator share an enabled Network Pipe graph and stored pattern is complete.

## Android controls missing
Verify conversion/state, installed parts/perks and V/B/K key bindings. Relog once and report whether HUD/selection persisted.

## Missing/purple textures
Record exact block/item and whether issue is world model, inventory model or equipped model.

## Reporting a bug
Include build commit/JAR name, singleplayer/server, steps to reproduce, expected result, actual result, screenshots if visual, and latest.log/debug.log for crashes or state desync.

Use the separate **Current Feature Reference** for implementation details and **M2 Testing Checklist** for formal verification.
