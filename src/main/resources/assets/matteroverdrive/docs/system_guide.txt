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
28. Playtester troubleshooting

# 1. First steps and world exploration
Status: PLAYABLE / TESTING world content.

## Simplified
- Mine Tritanium and Dilithium.
- Build basic FE and matter machines.
- Explore fresh chunks for Matter Overdrive structures and natural anomalies.
- Salvage crates and fight structure defenders for faster progression.

## Detailed
Tritanium and Dilithium remain the core machine/material progression. Fresh chunks can also contain crashed ships, cargo ships, underwater bases, Mad Scientist houses, Android Houses and Sand Pits. These structures contain persisted Tritanium Crate salvage and persistent inhabitants such as Rogue Androids, Ranged Rogue Androids, Drones, Mad Scientists and Failed animals.

Natural Gravitational Anomalies can generate in fresh Overworld chunks at the conservative 1.7-style candidate frequency of roughly 1/200 chunks. Their starting mass is 2,048-10,240 and they immediately use the same pull/event-horizon/mass-growth backend as reactor anomalies.

## Important limits
- Old explored chunks are not retroactively populated.
- Current structure layouts are modern translations and are not yet exact copies of every legacy PNG template.
- Android House and Sand Pit geometry are known parity targets still needing exact reconstruction.

# 2. FE power and batteries
Status: PLAYABLE.

## Simplified
- Generate FE with Solar Panels or a Fusion Reactor.
- Move FE with Heavy Energy Cable.
- Recharge Batteries/HC Batteries in the Charging Station.

## Detailed
Heavy Energy Cable is the FE transport path. Matter Pipe carries matter, and Network Pipe carries network item/pattern traffic. Normal and HC Batteries store real FE and remain rechargeable after being drained. Energy Packs are consumable emergency weapon energy. Creative Batteries are testing-only unlimited sources.

Cable chains and corners should automatically resume after rebuilding. If a machine is not receiving FE, test a producer -> one cable -> machine path first, then extend the chain.

# 3. Matter Decomposer and Recycler
Status: PLAYABLE.

## Simplified
- Put matter-valued items in the Decomposer to create matter.
- Use the Recycler to recover/refine Matter Dust.

## Detailed
The Decomposer consumes FE and matter-valued items into its internal matter buffer. Matter Pipe can move that matter into Replicators, Reactor IO and compatible receivers. The intended failure path can create Matter Dust.

The Recycler processes Matter Dust into refined form while preserving represented matter. Both machines persist inventories, energy, matter and upgrades.

# 4. Matter Analyzer and Pattern Drives
Status: PLAYABLE.

## Simplified
- Put a Pattern Drive in the Analyzer.
- Analyze matching items until the pattern reaches 100%.

## Detailed
Normal Pattern Drives store two distinct patterns. Analyzer cycles add progress to a matching pattern; completed patterns record the item identity and matter cost used by replication. Pattern Drives retain data when removed.

The Analyzer can write directly to its local drive or use Pattern Storage over the working Network Pipe graph.

# 5. Pattern Storage / Monitor / Replicator
Status: PLAYABLE.

## Simplified
- Put Pattern Drives in powered Pattern Storage.
- Connect Storage, Monitor and Replicator with Network Pipe.
- Choose a completed pattern in Pattern Monitor.
- Supply the Replicator with FE and matter.

## Detailed
Pattern Storage exposes patterns across enabled network paths. Pattern Monitor queues replication requests. Replicator consumes real FE and matter and outputs the requested item. Normal analyzed patterns retain their current failure behavior; failures can produce matter-bearing dust.

Keep resource paths separate: Network Pipe for pattern/network traffic, Matter Pipe for matter, Heavy Energy Cable for FE.

# 6. Matter Scanner and Portable Decomposer
Status: PLAYABLE.

## Matter Scanner
Sneak-use the Scanner on powered Pattern Storage to link it. Hold-use it on a supported block to add pattern progress. Invalid or unavailable linked storage causes the scan to fail safely instead of deleting the target.

## Portable Decomposer
The Portable Decomposer stores FE and matter. Add matter-valued items to its pickup filter, then matching pickups are converted instead of entering inventory when enough FE/capacity exists. Use it on compatible matter receivers to transfer its stored matter.

# 7. Matter Containers and Pipes
Status: PLAYABLE.

Matter Containers provide portable matter storage. Matter Pipe discovers compatible matter endpoints through connected runs and moves matter between them. Use Heavy Energy Cable for FE and Network Pipe for network traffic; they are intentionally separate systems.

A Debug Matter Container with very large capacity exists for controlled testing.

# 8. Molecular Inscriber
Status: PLAYABLE.

Power the Inscriber and use the current circuit chain:
- Mk1 + Gold -> Mk2
- Mk2 + Diamond -> Mk3
- Mk3 + Emerald -> Mk4

The machine supports its battery slot, cable FE, upgrades, progress telemetry and persistent inventory/state.

# 9. Solar Panel and Charging Station
Status: PLAYABLE.

Solar Panels generate FE under valid daylight and export it to adjacent receivers/cables. Charging Stations accept FE and recharge compatible energy items. The Android Station is separate: it can provide wireless energy to converted Android players in range.

# 10. Microwave
Status: PLAYABLE.

The Microwave is a powered food cooker. Give it FE and a food item with a valid smelting result. It waits if the output is blocked and supports relevant machine upgrades.

# 11. Space-Time Accelerator
Status: PLAYABLE / performance-sensitive.

The Accelerator consumes FE and matter to add extra ticks to eligible blocks/block entities in its horizontal area. Speed/Hyper Speed change pulse rate; Range changes radius; Power/Storage upgrades change resource behavior. Start with a small range because accelerated third-party machines can expose their own bugs or create server load.

# 12. Transporter
Status: PLAYABLE same-dimension transport.

## Simplified
- Bind a Transport Flash Drive to a destination.
- Insert it into a powered Transporter.
- Stand on the pad and let the cycle complete.

## Detailed
The Transporter validates destination, range, FE and cooldown before moving eligible entities. Legacy-derived behavior includes a 1,024,000 FE capacity, up to three entities per cycle, distance-scaled FE cost and Speed/Range/Power/Power Storage upgrade effects. Exact-required-FE transport should succeed.

# 13. Tritanium Crates
Status: PLAYABLE.

Tritanium Crates and colour variants provide 54-slot storage. Their contents persist into the dropped crate item when safely dismantled/broken and restore when placed again. Automation uses the normal Forge item handler.

# 14. Matter Network Router / Switch / Pylons
Status: PLAYABLE.

## Router
A powered Router moves items across its Network Pipe graph. It uses 10 FE per item actually moved. Speed and Hyper Speed upgrades increase the item budget across multiple transfers. Ordinary item filters and Network Flash Drive destination filtering are supported. Four persistent upgrade slots are available.

## Switch
Right-click to enable/disable a Network Switch. Disabled switches break routing/discovery across that point and their state persists.

## Pylons
Matching-channel Pylons bridge nearby separate network graphs. The current implementation is a practical local wireless bridge; exact legacy multiblock/glow/interdimensional behavior is still incomplete.

# 15. Fusion Reactor
Status: TESTING late-game power system.

## Simplified
- Place the controller/ring around a Gravitational Anomaly using the Reactor Assembly Guide/overlay.
- Feed matter through a formed Reactor IO.
- Extract FE through Reactor IO and Heavy Energy Cable.
- Use RUN/SCRAM and RS MODE to control operation.

## Detailed
The reactor validates its horizontal multiblock, links Controller/IO storage, scales output from anomaly mass and efficiency, consumes matter, supports upgrades, shares power around the ring and reports connected demand. The overlay persists and can show exact required block positions. Reactor Remote opens a linked controller remotely.

Stabilizers can suppress anomaly hazard while the reactor continues using the unsuppressed generation mass model implemented by the port. SCRAM stops generation but does not remove the anomaly.

# 16. Gravitational Anomalies and Stabilizers
Status: TESTING hazard system.

Anomalies pull entities/items, consume matter-valued drops to gain mass and can damage/consume living entities inside the event horizon. Living-entity event-horizon deaths add mass once. Higher mass increases hazard range/strength and reactor potential.

A Space-Time Equalizer worn on the chest protects the wearer from anomaly pull/event-horizon damage. Powered Stabilizers aimed at the anomaly reduce its effective dangerous strength. Blocked, unpowered or incorrectly controlled stabilizers stop refreshing suppression.

Natural anomalies in fresh chunks use this exact runtime backend.

# 17. Energy weapons and Weapon Station
Status: PLAYABLE combat core / PARTIAL visual parity.

Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun require valid FE, generate heat and can overheat. Batteries/HC Batteries provide rechargeable FE; Energy Packs are consumable. Weapon Station exposes real Battery/Color/Barrel/Sights/Utility slots and current module effects.

Known parity gaps include exact module meshes, recoil, zoom and some first-person holding/animation fidelity.

# 18. Android conversion / HUD / skill tree
Status: PLAYABLE / TESTING progression.

## Simplified
- Convert using the Android progression item/path available in the build.
- Install HEAD/CHEST/ARMS/LEGS parts at an Android Station.
- Press V to cycle installed abilities.
- Press B to activate the selected ability.
- Press K to open the skill tree.

## Detailed
Android state stores FE, installed body parts, level/XP, selected ability and a persistent selectable perk tree. The HUD reports core energy and ability state. Skill choices persist and can be refunded/reset through the implemented system. Android Station provides management and charging.

The current skill tree contains substantially more options than the original early port and is intended to be tested for persistence across level-up and relog.

# 19. Rogue Android Spawner squads
Status: TESTING full squad-management layer.

## Simplified
- Power the Android Spawner.
- It maintains up to six owned Androids.
- Use Transport Flash Drives as patrol points.
- Use COLOR / MODE / COMMAND to configure the squad.

## Detailed
The Spawner targets a six-unit owned population with roughly 30% melee / 70% ranged composition. Spawned Androids persist their origin Spawner and squad state. Unrelated/natural/structure Androids do not consume this cap.

Six Transport Flash Drive slots define same-dimension patrol waypoints. Modes are PATROL, GUARD, HOLD and ESCORT. ESCORT can bind the player as commander and uses deterministic formation positions instead of stacking every unit on one spot. Same-Spawner Androids share targets, remain allied and protect the commander/team. KILL OWNED affects only that Spawner's owned squad.

# 20. Drones
Status: PLAYABLE command layer / PARTIAL flying parity.

Right-click an unowned Drone to link it. Another player cannot silently steal a linked Drone. Owner right-click cycles FOLLOW -> DEFENSIVE -> PASSIVE -> AGGRESSIVE. Owner shift-right-click releases it.

FOLLOW stays near the owner without fighting. DEFENSIVE reacts to attackers of the owner. PASSIVE follows without combat. AGGRESSIVE proactively attacks valid nearby hostile monsters. Owned Drones reject owner/players/allies and same-owner drones as targets. Unowned Drones remain hostile.

Legacy flying navigation/render/equipment parity is still incomplete.

# 21. Scientists / Failed creatures / Cocktail of Ascension
Status: PLAYABLE restored entity slice.

Mad Scientists and Failed Cow/Pig/Sheep/Chicken variants exist and can appear in restored structures. The Mutant Scientist is the tougher transformed enemy.

Cocktail of Ascension progression tracks the implemented ingredients/kill requirements and transforms the relevant Scientist only after the replacement Mutant can successfully spawn; ingredients are not committed before successful transformation.

# 22. Security and Tritanium Wrench
Status: PLAYABLE.

Security Protocols support Claim, Access and Remove behavior. Claimed machines enforce ownership/access rules. Tritanium Wrench dismantling is security-aware and returns supported machine contents/state safely instead of bypassing claims.

# 23. Star Map navigation and encounters
Status: TESTING strategic layer.

## Simplified
- Open the Star Map.
- Navigate Galaxy -> Quadrant -> Star -> Planet.
- Mouse wheel zooms; drag pans; right-click goes back.
- Select a planet and use TRAVEL to move the command fleet.

## Detailed
Planet properties and positions are deterministic. Whole-fleet travel is server-authoritative and persists current/destination/timing state. Same-system timing follows the legacy AU-style factor; interstellar timing follows the legacy LY-style factor. No fake FE travel cost has been added.

Routes long enough can trigger one deterministic encounter: Asteroid Field, Gravitational Slingshot, Android Intercept, Signal Echo or Hostile Fleet. Encounter state survives closing/reopening the GUI.

# 24. Star Map fleet combat
Status: TESTING.

The command fleet starts at Hull 100 / Shield 60 / Firepower 20. A Hostile Fleet encounter pauses arrival. Press FIRE when recharged. Each surviving enemy volley drains shields first, then hull. Victory increments fleet victories and resumes travel without consuming the combat pause. Defeat ends travel and returns navigation to the last safe planet with 35 hull / 0 shields.

Scout/Colonizer ship counts do not automatically alter combat firepower because the reference code does not provide justified combat values for them.

# 25. Star Map colony economy
Status: TESTING late-game strategy.

The first bound commander receives a modern bootstrap homeworld containing Base + Ship Factory. Ownership/buildings are world-persistent planet data rather than properties of one Star Map block.

## Buildings and exact recovered effects
- Ship Factory: 8,000 ticks; required for Scout/Colonizer production.
- Ship Hangar: 4,800 ticks; +2 fleet berths.
- Matter Extractor: 14,400 ticks; +10 matter production / -6 energy production.
- Power Generator: 14,400 ticks; +8 energy production / -2 matter production.
- Residential: 6,000 ticks; +10,000 population / -4 energy / -2 matter / +4 building capacity.

Residential happiness follows the recovered sign-based energy/matter rule. Planet GUI shows E, M, P, H and B telemetry. A newly colonized world begins with a Base but must build its own Factory before constructing ships.

# 26. Star Map planet-local ships and transfers
Status: TESTING newest feature set.

Scout and Colonizer ships now belong to **planets**, not to a Star Map console.

## Building ships
- Scout: 3,600 ticks.
- Colonizer: 5,000 ticks.
- Completion adds the ship to the planet where the build occurred.
- A Base has two baseline berths; each Ship Hangar adds two more.
- Physical non-stacking ship tokens also appear with owner/type/planet metadata, but persistent planet fleet data is authoritative.

## Moving ships without moving the command fleet
Open a non-current Planet page while the command fleet is stationary. `SEND S` dispatches a stationed Scout; `SEND C` dispatches a stationed Colonizer. Dispatch removes the ship from the current source planet and creates an independent persistent travel event. Multiple ships can be in transit at the same time.

`TRANSIT S# C#` shows active independent ship events. These transfers do not replace the command fleet's separate TRAVEL journey.

## Arrival behavior
- Scout -> friendly colony with free berth: becomes stationed there.
- Scout -> no friendly berth/capacity: returns to origin.
- Scout travel has no invented reward because the authoritative 1.7 Scout `onTravel` hook is empty.
- Colonizer -> unowned planet: ship is consumed, ownership is assigned and a Base is established.
- Colonizer -> friendly colony with room: ship stations there.
- Colonizer -> cannot claim/berth: returns to origin.

A loaded Star Map owned by the same commander can settle completed arrivals, so the exact block that launched the ship is no longer required to survive. Existing saves from the immediately previous console-counter build migrate their Scout/Colonizer counts once to that console's current planet.

## Recommended strategic loop
1. Develop homeworld power/matter balance.
2. Build Hangars for fleet capacity.
3. Build Colonizer ships.
4. Send a Colonizer to an unowned planet.
5. Wait for independent travel arrival.
6. New colony receives ownership + Base.
7. Build Factory and economy buildings there.
8. Build or transfer Scouts/Colonizers between friendly colonies.
9. Expand again while the command fleet separately handles navigation/encounters/combat.

# 27. Restored structures and salvage
Status: TESTING exploration content.

Crashed Ship: salvage plus 1-2 mixed Rogue Android defenders.
Cargo Ship: larger salvage, 2-4 Android defenders and possible hostile Drone.
Underwater Base: persisted salvage, hostile Drone and possible ranged Android; interior should remain dry.
Mad Scientist House: circuitry/machine salvage, Mad Scientist and possible Failed animal.
Android House: Android/battery/network salvage, several Android defenders and possible Drone.
Sand Pit: lower-tier tritanium/matter salvage with an Android guardian and possible Drone.

Generated structure Androids are deliberately not Android-Spawner-owned, and generated Drones begin unowned/hostile.

# 28. Playtester troubleshooting

## No FE transfer
Confirm you are using Heavy Energy Cable, not Matter Pipe/Network Pipe. Test a one-cable path first.

## No matter transfer
Use Matter Pipe between compatible matter source/receiver. Reactor matter enters through formed Reactor IO.

## Pattern not visible
Confirm Pattern Storage/Monitor/Replicator share an enabled Network Pipe graph and the stored pattern is complete.

## Android controls missing
Verify Android conversion/state, installed parts/perks and key bindings for V/B/K. Relog once and report whether HUD/selection persisted.

## Spawner exceeds six or attacks allies
Record whether Androids came from this exact Spawner, a structure, or natural/debug spawning. Only owned units should count.

## Star Map colony has the wrong ships
Ship counts are planet-local. Check the current Planet page and `TRANSIT` counts. A ship in transit is removed from its source until arrival.

## Star Map old save after updating
The first loaded bound console migrates old console-local Scout/Colonizer counts into its current planet one time. If counts duplicate after a second reload, report it immediately.

## Missing/purple textures
Record the exact block/item and whether the problem is world model, inventory model or equipped model.

## Reporting a bug
Include: build commit/JAR name, singleplayer/server, steps to reproduce, expected result, actual result, screenshots if visual, and latest.log/debug.log for crashes or state desync.

Use the separate **Current Feature Reference** for implementation details and **M2 Testing Checklist** for the formal verification list.
