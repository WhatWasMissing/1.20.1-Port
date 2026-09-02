# Matter Overdrive Alpha 0.2 — Complete System Guide

Made by MVQ1303

This guide explains how to use every currently working gameplay system on testing/main. Each system has a simplified version for getting started and a detailed version for setup, limits and useful combinations.

Status labels:

- PLAYABLE: the described 1.20.1 system works as a usable gameplay loop.
- PARTIAL: the described slice works, but some original 1.12.2 features are not restored.
- TESTING: implemented and compiled, but the latest changes still require an in-world pass.
- PLACEHOLDER: registered content with no restored gameplay is not presented as a working system.

# Contents

1. Survival resources and first steps
2. FE power, Batteries and cables
3. Matter Decomposer
4. Matter Recycler
5. Matter Analyzer and Pattern Drives
6. Pattern Storage
7. Pattern Monitor and Matter Replicator
8. Matter Scanner
9. Portable Decomposer
10. Matter Containers and Matter Pipes
11. Molecular Inscriber
12. Solar Panel and Charging Station
13. Microwave
14. Space-Time Accelerator
15. Transporter
16. Tritanium Crates
17. Matter Network item logistics
18. Fusion Reactor
19. Gravitational Anomaly and Stabilizers
20. Energy weapons and Weapon Station
21. Android conversion, parts and abilities
22. Rogue Android Spawner
23. Contracts and Star Map
24. Tritanium tools and armour
25. Data Pad
26. Documentation and testing utilities
27. Decorative and visual content
28. Troubleshooting routes

# 1. Survival resources and first steps

Status: PLAYABLE world-resource foundation. PARTIAL original progression because the Mad Scientist quest route is not restored.

## Simplified

- Explore fresh Overworld chunks for Tritanium Ore and Dilithium Ore.
- Smelt or blast the ores into Tritanium Ingots and Dilithium Crystals.
- Use these resources for machines, tools, armour and the temporary Android Pill recipes.

## Detailed

- Tritanium Ore generates from Y -32 through 64, with up to six ore blocks per vein and ten placement attempts per chunk.
- Dilithium Ore generates from Y -64 through 16, with up to five ore blocks per vein and six placement attempts per chunk.
- Only fresh chunks receive newly generated ores. Existing explored chunks are not retroactively changed.
- Furnace and Blast Furnace recipes turn both ores into their intended resources.
- Blue, Red and Yellow Android Pills are craftable as a temporary survival bridge.

## Limitations

- Crashed ships, underwater bases, Android houses, anomaly events and the original wider mob-spawn layer are not yet generated.
- Android Pills will eventually return to a quest/NPC progression route.

# 2. FE power, Batteries and Heavy Energy Cables

Status: PLAYABLE.

## Simplified

- Generate FE with a Solar Panel or Fusion Reactor.
- Connect machines with Heavy Energy Cable.
- Charge normal and HC Batteries in a Charging Station and carry them to machines or weapons.

## Detailed

- Heavy Energy Cable uses the legacy heavy_matter_pipe block ID but carries FE only.
- Connect the producing block or Reactor IO to one or more cables, then connect the final cable to a compatible machine.
- Cable chains, corners and rebuilt links should resume automatically.
- Normal and HC Batteries store actual FE. When used by weapons or an Android, they remain as drained rechargeable items.
- Energy Packs provide 32,000 FE to weapons and are consumed.
- Creative Batteries are testing items with effectively unlimited FE.
- Matter Pipe does not carry FE, and Network Pipe does not carry FE.

## Limitations

- The old all-purpose Heavy Matter Pipe behavior was deliberately separated. Use the pipe that matches the resource: Heavy Energy Cable for FE, Matter Pipe for matter, Network Pipe for items and pattern data.

# 3. Matter Decomposer

Status: PLAYABLE.

## Simplified

- Power the Decomposer.
- Insert an item with a matter value.
- Collect or pipe the generated matter into another matter-capable machine.

## Detailed

- Place the Matter Decomposer and provide FE by cable or a compatible charged energy item.
- Insert a supported item into its input slot. Dirt is the easiest test and produces 1 kM.
- The machine converts items into its internal matter buffer.
- Connect a Matter Pipe to transfer matter into a Replicator, Fusion Reactor IO or another compatible receiver.
- The machine supports upgrades and shows effective values in its debug information.
- Decomposition includes its intended failure behavior. Use the GUI/debug state to compare input, FE, matter and failure results.
- Inventory, FE, matter and upgrades persist across save/reload and return safely when the block is broken.

## Limitations

- Matter values are defined by the current port data. Items without a matter value cannot be decomposed.

# 4. Matter Recycler

Status: PLAYABLE.

## Simplified

- Power the Recycler.
- Insert raw Matter Dust.
- Collect the refined output.

## Detailed

- Place and power the Matter Recycler.
- Insert Matter Dust carrying a stored matter value.
- The Recycler spends FE and processing time to create refined Matter Dust while preserving the represented matter amount.
- Higher-value dust takes longer according to the current legacy-derived processing formula.
- Upgrades affect supported machine values and persist with the machine.
- Use the Recycler when a failed or other process produces raw Matter Dust that you want to recover into the production loop.

## Limitations

- The Recycler is focused on the current Matter Dust recovery workflow rather than every old cross-mod recycling integration.

# 5. Matter Analyzer and Pattern Drives

Status: PLAYABLE.

## Simplified

- Power the Analyzer.
- Insert a Pattern Drive and five matching items.
- Wait for the pattern to reach 100%.

## Detailed

- A normal Pattern Drive stores two distinct patterns.
- Insert a charged energy item or cable power, a Pattern Drive and the item to analyze.
- Each completed Analyzer cycle adds 20% progress for the matching item, so five items complete a normal pattern.
- A Creative Pattern Drive is useful for testing.
- The Analyzer can write directly to its inserted drive.
- For a network setup, leave the Analyzer drive slot empty and connect it by Network Pipe to powered Pattern Storage containing a drive.
- Completed patterns store the item identity and matter cost used by the Replicator.

## Limitations

- The broader original scan/research presentation is simplified. Pattern acquisition itself is functional.

# 6. Pattern Storage

Status: PLAYABLE.

## Simplified

- Power Pattern Storage.
- Insert Pattern Drives.
- Connect it to Analyzer, Pattern Monitor and Replicator with Network Pipe.

## Detailed

- Pattern Storage has six drive slots.
- Each normal Pattern Drive holds two distinct patterns, giving twelve normal entries in a fully populated storage.
- An Analyzer on the same enabled Network Pipe graph can add pattern progress.
- A Matter Scanner can link directly to the storage if it has a drive and at least 128 FE.
- Pattern Monitor discovers completed patterns through the network.
- Removing a drive preserves its pattern data in the item.
- Drive contents, storage inventory and power state persist through save/reload.

## Limitations

- Generic Flash Drive and Network Flash Drive configuration are not restored. Pattern Drives are the working pattern-storage items.

# 7. Pattern Monitor and Matter Replicator

Status: PLAYABLE core replication loop.

## Simplified

- Connect Pattern Storage, Pattern Monitor and Replicator with Network Pipe.
- Open Pattern Monitor and select a completed pattern.
- Give the Replicator FE and enough matter, then collect the output.

## Detailed

- The Pattern Monitor discovers completed patterns on the same enabled network.
- Select a pattern to submit a replication request. The monitor supports a queue of up to eight requests.
- A connected Replicator accepts the work and displays the selected network pattern.
- Feed matter to the Replicator through Matter Pipe from a Decomposer or portable/machine source.
- Supply FE separately by cable or battery.
- When FE and matter are sufficient, the Replicator completes its cycle and places the requested item in its main output.
- A normal fully analyzed pattern retains its small intended failure chance. A failure creates raw Matter Dust carrying the relevant matter value in the secondary output.
- The Replicator can also read a locally inserted Pattern Drive as a fallback.
- Accepted work, FE, matter, inventory and pattern state persist.

## Limitations

- Pattern data uses Network Pipe, matter uses Matter Pipe and FE uses Heavy Energy Cable. Combining the wrong pipe types will not work.

# 8. Matter Scanner

Status: PLAYABLE handheld pattern acquisition.

## Simplified

- Sneak-use the Scanner on powered Pattern Storage containing a Pattern Drive.
- Hold-use it on a matter-valued block for three seconds.
- Ten successful scans of the same block complete its pattern.

## Detailed

- Linking costs 128 FE from the Pattern Storage.
- The Scanner remembers the storage dimension and coordinates.
- Hold-use on a valid target for 60 ticks without moving out of range or changing the block.
- A successful scan consumes the block and adds 10% pattern progress.
- The target is not destroyed if the storage is missing, unloaded, unpowered, in another dimension, has no drive, is full or refuses the pattern.
- Releasing early or replacing the target cancels safely.
- Scanner-created patterns are usable by Pattern Monitor and Replicator.
- The Scanner link and last status persist in item data.

## Limitations

- This is a direct storage-linked implementation rather than the full original scanner presentation.

# 9. Portable Decomposer

Status: PLAYABLE handheld matter collection.

## Simplified

- Charge the Portable Decomposer.
- Sneak-use it with a matter-valued offhand item to add that item to its filter.
- Pick up matching items to convert them automatically into stored matter.

## Detailed

- The item stores up to 128,000 FE and 512 kM.
- Sneak-use in air with a supported item in the other hand to add or remove that item from the pickup filter.
- A filtered pickup is intercepted before entering the inventory.
- Each processed item spends FE equal to its base matter value and produces 10% of that value as stored matter.
- Fractional matter is retained so low-value items cannot silently lose every fraction or create free matter.
- Unmatched items, zero-matter items, pickups with insufficient FE and pickups while the matter buffer is full enter the inventory normally.
- Use the Portable Decomposer on a compatible matter receiver to transfer stored matter directly.
- FE, matter, filters and fractional remainder persist.

## Limitations

- The current loop is filter-driven and does not reproduce every old portable GUI behavior.

# 10. Matter Containers and Matter Pipes

Status: PLAYABLE.

## Simplified

- Store matter in a Matter Container.
- Use Matter Pipe between a matter source and receiver.
- Use machine/container interactions to transfer exact matter amounts.

## Detailed

- Matter Containers are portable matter storage and transfer items.
- Matter Pipe discovers compatible endpoints and moves matter through connected runs, corners and junctions.
- Typical routes include Decomposer to Replicator and Decomposer to Reactor IO.
- Matter does not travel through Heavy Energy Cable or ordinary Network Pipe.
- Machine matter buffers and container contents persist.
- The Debug Matter Container stores up to 1,000,000 kM for controlled testing.

## Limitations

- Visual moving-fluid presentation is not the focus of the current implementation; transfers may appear immediate.

# 11. Molecular Inscriber

Status: PLAYABLE.

## Simplified

- Power the Inscriber.
- Combine Mk1 Circuit plus Gold for Mk2, Mk2 plus Diamond for Mk3, or Mk3 plus Emerald for Mk4.
- Take the upgraded circuit from the output.

## Detailed

- Insert the source Isolinear Circuit in the primary slot and the required material in the secondary slot.
- Mk1 plus Gold produces Mk2.
- Mk2 plus Diamond produces Mk3.
- Mk3 plus Emerald produces Mk4.
- The machine has a battery slot and accepts cable FE.
- Speed, Power, Power Storage and Hyper Speed upgrades use the shared machine-upgrade system.
- The GUI/debug information shows the selected recipe, progress, effective cycle duration, FE/t and total energy.
- Automation uses recipe-aware inputs and an extraction-only output.
- Old three-slot machine saves migrate safely to the newer battery-slot inventory.

## Limitations

- The currently registered upgrade set does not include the original separate Power Transfer upgrade.

# 12. Solar Panel and Charging Station

Status: PLAYABLE.

## Simplified

- Put a Solar Panel under open daylight and cable it to a machine or Charging Station.
- Insert a rechargeable FE item into the Charging Station.
- Converted Androids can also stand within four blocks of a powered Android Station to charge.

## Detailed

- Solar Panels generate FE during suitable daylight, store it internally and export to adjacent compatible receivers.
- Power Storage upgrades increase Solar Panel capacity.
- The Charging Station buffers incoming FE and charges compatible energy items, including normal/HC Batteries and the Portable Decomposer.
- A disconnected station stops once its buffer empties; it does not generate free FE.
- The Android Station is a separate block. It accepts FE and shares up to 2,000 FE/t fairly between converted players within four blocks.
- Converted Androids can also sneak while holding a charged normal or HC Battery in either hand. Up to 1,024 FE/t is transferred from real battery storage.

## Limitations

- Solar generation depends on normal world daylight conditions. The Android Station is for player-core charging; the Charging Station is for energy items.

# 13. Microwave

Status: PLAYABLE.

## Simplified

- Power the Microwave.
- Insert raw food that has a normal furnace recipe.
- Collect the cooked food.

## Detailed

- The Microwave accepts food-only vanilla smelting recipes and rejects non-food inputs.
- Base storage is 512,000 FE.
- Nominal base cost is 1,000 FE per cook with a 10-tick base cook time.
- It can receive cable FE or charge from a compatible battery item.
- Speed, Power, Power Storage and Hyper Speed upgrades adjust the supported values shown in the GUI.
- Processing waits safely if the output is blocked.
- Inventory, energy, progress and upgrades persist and drop safely on break.

## Limitations

- It is intentionally a food cooker, not a general powered furnace.

# 14. Space-Time Accelerator

Status: PLAYABLE, but high-impact and still a performance-sensitive system.

## Simplified

- Supply both FE and matter.
- Place crops or machines on the same Y level inside its range.
- Keep redstone off to accelerate them; apply redstone to disable it.

## Detailed

- The Accelerator requires FE and matter at the same time.
- At base settings it pulses every 40 ticks and adds extra ticks to eligible random-tick blocks and block entities.
- Targets must be on the same Y level.
- It does not accelerate itself or another Space-Time Accelerator.
- Speed and Hyper Speed shorten the pulse interval.
- Power modifies FE usage.
- Power Storage and Matter Storage increase capacities.
- Range expands the horizontal radius but is capped at twelve blocks and does not intentionally load unloaded chunks.
- Ticker failures are isolated so one broken target should not crash the whole pulse.
- FE, matter, fractional matter use, pulse progress and upgrades persist.

## Limitations

- Extra ticking can expose bugs in other blocks or increase server load. Start with a small radius and a few targets.

# 15. Transporter

Status: PLAYABLE same-dimension transport.

## Simplified

- Sneak-use a Transport Flash Drive on the destination block.
- Put the bound drive into a powered Transporter.
- Stand on the Transporter and wait for the cycle.

## Detailed

- The Transport Flash Drive stores the target dimension and block position.
- The current Transporter moves eligible entities to a valid target in the same dimension.
- Base range, cycle, delay and energy cost are shown in the machine debug information.
- Speed, Range, Power and Power Storage upgrades are active.
- Supply FE through cable or the battery slot.
- The machine checks range, target validity, FE and cooldown before moving the entity.
- Bound drive, inventory, energy, progress and upgrades persist.

## Limitations

- Cross-dimensional transport and richer legacy effects are not part of the current slice.

# 16. Tritanium Crates

Status: PLAYABLE.

## Simplified

- Place any Tritanium Crate colour.
- Store items in its 54 slots.
- Break and replace the dropped crate to retain its contents.

## Detailed

- The base crate and all sixteen colour variants share the same 54-slot storage behavior.
- Shift-click and Forge item-handler automation are supported.
- Contents are written to the dropped crate item and restored when it is placed again.
- The GUI reports used slots and total stored item count.
- Break handling returns the crate exactly once without duplicating its inventory.

## Limitations

- Always verify portable contents before moving irreplaceable items between mod versions.

# 17. Matter Network item logistics

Status: PLAYABLE. Pattern networking shares the same enabled graph rules.

## Simplified

- Connect inventories with Network Pipe.
- Add and power one Network Router.
- Use a Network Switch to disable a route and matching-channel Pylons to bridge nearby separate graphs.

## Detailed

- A powered Router moves items between connected inventory endpoints.
- It consumes 10 FE per item actually moved.
- Put an item in the Router filter slot to create a whitelist; remove it to allow any item.
- A destination is remembered as a sink until it empties, preventing the same stack bouncing back and forth.
- If multiple powered Routers share one graph, only one executes routing on a tick. Another can take over if the active Router loses power.
- Right-click a Network Switch to enable or disable traffic and pattern discovery across that point.
- Pylons use channels 0 through 15. Shift-use them until two pylons have the same channel.
- Matching-channel pylons within 64 blocks bridge separated graphs wirelessly.
- Pattern Storage, Monitor and Replicator discovery also follows enabled Pipe, Router and Switch paths.

## Limitations

- Pylons are a current local wireless bridge, not the full original interdimensional network-security system.
- Network Flash Drive and security protocol configuration remain placeholders.

# 18. Fusion Reactor

Status: PLAYABLE large multiblock.

## Simplified

- Right-click the Reactor Assembly Guide and build its highlighted 11 by 11 horizontal ring.
- Put a Gravitational Anomaly at the centre, feed matter through Reactor IO and extract FE through Heavy Energy Cable.
- Open the Controller and correct every reported structure fault until it shows VALID.

## Detailed

- Place the Controller first. The ring extends in the direction you faced during placement.
- The anomaly centre is five blocks forward from the Controller and may be on the ring Y or up to three blocks above or below it.
- Use Machine Hulls in hull positions and Fusion Reactor Coils or Reactor IO in coil-capable positions.
- The two positions beside the Controller are flexible and may accept supported ring blocks or a Decomposer.
- The Assembly Guide/overlay is the authoritative position reference and avoids guessing the ring coordinates.
- Wait up to two seconds after structure changes for validation.
- Feed matter into linked Reactor IO with Matter Pipe.
- Extract FE from Reactor IO with Heavy Energy Cable.
- The Controller stores 100,000,000 FE and 2,048 kM before upgrades.
- Generation scales with anomaly mass and reactor efficiency. Vertical anomaly distance reduces efficiency.
- Speed upgrades increase generation and matter use together.
- Range upgrades extend vertical anomaly search up to sixteen blocks.
- Power Storage and Matter Storage increase the matching capacities.
- Machines inside the ring on the Controller level can receive shared reactor power without external cable; outside machines require Reactor IO and cable.
- Shift-use a Reactor Remote on the Controller to bind it, then use the remote to reopen the linked controller screen.

## Limitations

- Use the Assembly Guide rather than old compact-reactor instructions. Older documents may describe obsolete layouts.
- Full original exterior animation/presentation is still below legacy parity.

# 19. Gravitational Anomaly, Stabilizers and Equalizer

Status: PLAYABLE gravity/reactor support.

## Simplified

- Drop items or allow living targets to reach the anomaly to increase its mass.
- Aim powered Gravitational Stabilizers at it with a clear beam.
- Equip the Space-Time Equalizer to resist anomaly pull and event-horizon effects.

## Detailed

- The anomaly stores persistent mass and pulls nearby entities/items.
- Items reaching the event horizon are consumed and add mass.
- A living entity adds its mass once when the event horizon kills it.
- Higher usable mass increases Fusion Reactor generation.
- Place a Stabilizer facing the anomaly, provide FE and keep the beam path unobstructed.
- Multiple valid Stabilizers suppress the unsafe mass used for gravity behavior while preserving the reactor's unsuppressed generation mass.
- Stabilizer Speed, Range, Power and Power Storage upgrades affect supported values.
- Use the Stabilizer/controller status text to diagnose no power, wrong facing, blocked beam or missing anomaly.
- The Space-Time Equalizer grants immunity while properly equipped and continues to work after relog.

## Limitations

- The original wider world-event system around natural anomalies is not restored.

# 20. Energy weapons and Weapon Station

Status: PLAYABLE combat core. PARTIAL original visuals and random/enchantment ecosystem.

## Simplified

- Carry a Phaser, Phaser Rifle, Ion Sniper or Plasma Shotgun.
- Carry a charged normal/HC Battery or Energy Pack.
- Fire normally, watch the weapon HUD and stop when overheated.
- Use the Weapon Station to install compatible modules.

## Detailed

- The server checks shot energy, heat, cooldown and installed modules.
- If the weapon lacks internal energy, it can pull real FE from carried Batteries or consume an Energy Pack.
- Empty Batteries cannot provide shots and remain as rechargeable items.
- Non-Phaser weapons can be sneak-used to request a reload.
- Sneak-use the Phaser to cycle its six power modes. It also reloads automatically when a shot needs energy and a valid source is carried.
- Heat rises while firing and cools while the weapon is in inventory. An overheated weapon cannot fire until sufficiently cooled.
- The lower-right HUD shows charge, heat and OVERHEATED state.
- Weapon Station supports current barrel, sight, ricochet and colour modules and safely returns contents on break.
- Barrel modules alter supported effects, sights change aiming behavior, ricochet changes projectile behavior and colour modules change presentation.

## Limitations

- Held transforms, recoil, beam/model polish, old enchantments, random weapons and richer Rogue Android weapon drops are incomplete.

# 21. Android conversion, parts and abilities

Status: PLAYABLE foundation and active-ability layer. PARTIAL original Android RPG tree.

## Simplified

- Use a Blue Android Pill.
- Install Head, Chest, Arms and Legs parts by holding each part and right-clicking an Android Station.
- Press V to cycle abilities and B to activate the selected ability.
- Recharge at a powered Android Station, with a Yellow Pill, or by sneaking with a charged Battery.
- Use a Red Pill to deactivate and recover installed parts.

## Detailed

- Blue Pill begins with 25,000 of 100,000 Android FE.
- Head provides Night Vision and unlocks Cloak.
- Chest provides Resistance and unlocks Force Field.
- Arms adds three direct-melee damage for 80 FE and unlocks Sonic Shockwave.
- Legs provide movement speed and unlock Ender Teleport.
- Cloak costs 128 FE/t while enabled.
- Force Field costs 32 FE/t idle and absorbs up to half of incoming damage at 64 FE per absorbed damage point.
- Sonic Shockwave costs exactly 4,096 FE, deals six damage, knocks back non-allied targets within five blocks and has a five-second cooldown.
- Ender Teleport costs 4,096 FE, moves up to eight safe blocks along the view direction and has a three-second cooldown.
- FE spending is atomic: an action that cannot afford its complete cost leaves the remaining partial FE untouched.
- At zero FE the HUD shows CORE OFFLINE and movement is reduced by 50% until recharge or deactivation.
- The Android Station shares up to 2,000 FE/t fairly among converted players within four blocks.
- Sneak-held normal/HC Battery charging transfers up to 1,024 FE/t from either hand.
- Conversion, FE, installed parts, selected ability, toggles and cooldowns persist.
- Red Pill clears Android state and safely returns installed parts.

## Limitations

- The full XP/stat unlock tree, multi-level progression, flash cooling, minimap/team UI and many original biotic stats are not restored.

# 22. Rogue Android Spawner

Status: TESTING simplified enemy source. PARTIAL original entity system.

## Simplified

- Power the Rogue Android Spawner.
- Let it accumulate 20,000 FE.
- Kill the named Rogue Android and collect one random bionic part.

## Detailed

- The current spawner creates a tagged hostile Husk named Rogue Android.
- It searches several nearby collision-free positions and respects the world border.
- FE is charged only after Minecraft accepts the spawned entity.
- A blocked/rejected attempt costs no FE and retries after a delay.
- The spawner will not create another tagged Rogue Android while one remains within its local area.
- Killing the tagged enemy drops one random Head, Chest, Arms or Legs part.

## Limitations

- This is not a dedicated Rogue Android entity. Original levels, teams, melee/ranged variants, equipment, AI, sounds and weapon drops remain future work.

# 23. Contracts and Star Map

Status: PARTIAL progression slice.

## Simplified

- Open a Contract Market and take a collect or hunt Contract.
- Keep the Contract in your inventory while collecting the target item or killing the target mob.
- Return a completed Contract to the Market for its reward.
- Open the Star Map to view active and completed Contract counts.

## Detailed

- The Market offers three Contracts.
- Contract items store type, target, goal, progress and reward.
- Collect progress uses the number of items actually picked up.
- Hunt progress uses player kills of the exact target.
- One action advances one matching Contract, so duplicate Contracts do not multiply progress.
- A completed Contract glints and can be redeemed at the Market.
- After the last offer is removed, the Market waits 1,200 ticks before creating a new set.
- Offer data, refresh delay and Contract progress persist.
- Star Map data is viewer-specific, so multiple players see their own active/completed status.

## Limitations

- Star Map is currently a Contract summary, not a galaxy simulation.
- Generated galaxies, stars, planets, buildings, ships, ownership, travel and attack events are missing.
- The full quest/dialog/Mad Scientist framework and quest XP are missing.

# 24. Tritanium tools and armour

Status: PLAYABLE.

## Simplified

- Craft and use the Tritanium sword, tools and armour.
- Wear the full armour set for its configured full-set defensive bonus.

## Detailed

- Tritanium provides Sword, Pickaxe, Axe, Shovel, Hoe and Wrench equipment.
- The armour set includes Helmet, Chestplate, Leggings and Boots.
- Equipping every armour piece activates the current set bonus.
- Equipped armour uses the restored Tritanium textures.
- The Wrench is used by supported machine interactions where implemented.

## Limitations

- Check equipped rendering after resource-pack or shader changes. Final visual parity remains an ongoing polish area.

# 25. Data Pad

Status: PLAYABLE guide and scan-history tool. PARTIAL original quest integration.

## Simplified

- Use the Data Pad in air to open its seven-page guide.
- Use it on blocks to record their registry ID and matter value.
- Reopen it to review the newest scan history.

## Detailed

- Pages cover Overview, Matter Replication, Power and Machines, Fusion Reactor, Android System, Survival Progression and Scan History.
- Scan history stores up to sixteen unique blocks.
- Scanning an existing entry moves it to newest rather than duplicating it.
- Matter-valued and zero-matter blocks are both identified safely.
- History persists in the Data Pad item.

## Limitations

- Original quest pages and the complete NPC/dialog system are not connected yet.
- For the most comprehensive current instructions, use the separate Matter Overdrive System Guide item.

# 26. Documentation and testing utilities

Status: PLAYABLE documentation/testing support.

## Simplified

- Right-click M2 Testing Checklist for everything requiring verification.
- Right-click Current Feature Reference for the implemented-versus-missing matrix.
- Right-click Matter Overdrive System Guide for player instructions.

## Detailed

- All three documents are bundled inside the mod JAR and use a scrollable screen.
- Mouse wheel, Page Up, Page Down, Home and End navigate long documents.
- Done or Escape closes the screen.
- Documentation identifies Alpha 0.2 and MVQ1303.
- Debug Matter Container and Debug Matter Block provide controlled matter sources.
- Machine debug views expose effective server-side values.
- Infinite FE controls and Creative Battery are testing tools and should not be treated as survival balance.

## Limitations

- Documentation tracks testing/main. A locally older JAR contains the documentation from that older build.

# 27. Decorative and visual content

Status: PLAYABLE building resources; some presentation remains under visual testing.

## Simplified

- Use restored decorative blocks for sci-fi builds.
- Industrial Glass, Bounding Block, Matter Plasma and Molten Tritanium are intended to render translucently.

## Detailed

- Many legacy decorative block IDs, recipes, models and textures are registered.
- Tritanium Crate colours, Inscriber faces, equipped armour and held weapons use restored resources.
- Transparency requires the client render-layer setup included in the port.

## Limitations

- Shader packs and resource packs may expose sorting or transform issues.
- A registered legacy block is not automatically a restored gameplay system. Holo Sign currently lacks programmable/security behavior.

# 28. Troubleshooting routes

## A machine has no FE

- Confirm you used Heavy Energy Cable rather than Matter Pipe or Network Pipe.
- Inspect the source, every cable link and the machine energy/debug display.
- Confirm the battery actually contains FE.
- Break and replace only the suspected link, then recheck.

## Matter will not move

- Use Matter Pipe between matter-capable endpoints.
- Check source matter, destination free capacity and side connection.
- Heavy Energy Cable cannot transport matter.

## A pattern is missing

- Confirm the Pattern Drive contains a completed pattern.
- Power Pattern Storage and Replicator.
- Check every Network Switch on the route.
- Confirm Pattern Monitor, Storage and Replicator share one enabled Network Pipe graph.

## Replication is queued but idle

- Supply both FE and enough matter for the selected pattern.
- Check blocked output slots and failure output.
- Verify the Replicator accepted the Monitor request.

## Reactor is invalid

- Use the Reactor Assembly Guide overlay.
- Check the Controller-facing direction, every highlighted hull/coil position and anomaly centre.
- Wait up to two seconds for validation.
- Read the exact Controller fault text before moving blocks.

## Android ability fails

- Confirm conversion is active, the required body part is installed, the ability is selected, cooldown is zero and FE meets the full cost.
- Recharge with Android Station, Yellow Pill or sneak-held Battery.
- At zero FE, expect the CORE OFFLINE movement penalty.

## Weapon will not fire

- Check internal energy, carried charged Batteries/Energy Packs, heat and shot cooldown.
- An empty Battery cannot provide a shot.
- Allow an overheated weapon to cool.

## A system is marked PARTIAL

- The instructions above describe the working slice only.
- Do not assume missing original UI, entities, progression or integrations are available because a related registry item exists.

# End of Guide

For verification rather than ordinary play, open the M2 Testing Checklist item. For the exact implemented/missing parity matrix, open the Current Feature Reference item.
