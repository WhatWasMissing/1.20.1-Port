# Matter Overdrive 1.20.1 — Feature Reference

This is the current source-of-truth feature list for the 1.20.1 port on `testing/main`. It distinguishes systems that are implemented from legacy 1.12.2 content whose registry ID or resource may exist but whose original gameplay has not yet been restored.

A registered item/block is not automatically considered feature-complete. Several legacy IDs intentionally remain compatibility/resource placeholders until their behaviour is ported.

## Implemented core systems

### Matter machines and replication

- Matter Decomposer: FE-powered decomposition into matter, upgrades, failure chance and debug telemetry.
- Matter Recycler: FE-powered recycling into matter, upgrades and persistence.
- Matter Analyzer: analysis/pattern workflow and pattern-network participation.
- Matter Replicator: queued pattern replication using matter and FE, including cycle/failure state.
- Pattern Storage: Pattern Drive storage and network pattern supply.
- Pattern Monitor: discovers network storage/replicators and submits replication work.
- Molecular Inscriber: Mk2-Mk4 Isolinear Circuit production, FE, upgrades and persistence.
- Matter Container: portable matter storage/transfer.
- Matter Pipe: matter transport between compatible endpoints.

### Power and machines

- Solar Panel: daylight FE generation, storage/export and Power Storage upgrades.
- Charging Station: FE buffer and charging for compatible batteries/energy items.
- Microwave: food-only FE-powered cooker using normal Minecraft smelting recipes, with a 512,000 FE base buffer, 1,000 FE nominal cost per cook, 10-tick base cook time, battery charging, Speed/Power/Power Storage/Hyper Speed upgrades, persistence and break-safe inventory return.
- Space-Time Accelerator: FE-and-matter-powered extra ticking for nearby random-tick blocks and block entities, with redstone disable, Speed/Hyper Speed/Power/Power Storage/Matter Storage/Range upgrades, persistence, debug controls and ticker-failure isolation.
- Transporter: Transport Flash Drive binding and same-dimension entity transport with active Speed, Range, Power and Power Storage upgrades.
- Tritanium Crates: 54-slot portable storage across all colour variants with dropped-item NBT retention.
- Weapon Station: weapon/module editing, persistence and safe content return on break.

### Survival resources and world generation

- Tritanium Ore generates naturally in Overworld biomes from Y -32 through 64, using 10 placement attempts per chunk and legacy vein size 6.
- Dilithium Ore generates naturally in Overworld biomes from Y -64 through 16, using 6 placement attempts per chunk and legacy vein size 5.
- Tritanium Ore smelts/blasts into Tritanium Ingots.
- Dilithium Ore smelts/blasts into Dilithium Crystals.
- Blue, Red and Yellow Android Pills have temporary survival crafting recipes so Android conversion/deactivation/recharge can be reached before the Mad Scientist/quest route is restored.

The ore frequencies and vein sizes preserve the original mod's relative balance while the vertical ranges are adapted to the 1.20.1 world height. The pill recipes are a progression bridge, not final legacy-parity quest design.

### Handheld matter tools

- Matter Scanner: links to powered Pattern Storage for 128 FE, scans matter-valued blocks over 60 ticks, consumes only successfully recorded targets and adds 10% Pattern Drive progress per block.
- Portable Decomposer: 128,000 FE rechargeable buffer, 512 kM matter storage, persistent pickup filters, automatic 10% matter conversion with fractional-yield retention and direct transfer into compatible matter receivers.
- Data Pad: seven-page in-game guide plus persistent, deduplicated scan history for up to 16 blocks with registry IDs and matter values.
- M2 Testing Checklist: scrollable in-game copy of the consolidated runtime test document.
- Current Feature Reference: scrollable in-game copy of this implemented/missing feature matrix.
- Matter Overdrive System Guide: scrollable in-game instructions for every currently working system, pairing a simplified route with detailed setup and explicit partial-parity warnings.
- Login identity banner: reports Matter Overdrive Alpha 0.2 and MVQ1303 in chat once per world/server login.

### Matter/network logistics

- Heavy Energy Cable: FE-only cable using the legacy `heavy_matter_pipe` registry ID.
- Network Pipe: item/pattern-network connectivity.
- Network Switch: enables/disables item logistics and pattern traversal.
- Network Router: powered filtered logistics with endpoint diagnostics, anti-bounce sink tracking and one active router executor per connected graph.
- Dimensional Pylon: matching-channel wireless item-network bridge.
- Pattern-network discovery across enabled Pipe/Router/Switch paths.

The current item router remembers inventories that have received routed items as sinks until they empty. This prevents an undirected two-inventory network from repeatedly moving the same stack A -> B -> A while consuming FE.

## Fusion Reactor and gravitational systems

- Large ring Fusion Reactor structure with Reactor Assembly Guide/overlay.
- Reactor Controller and Reactor IO blocks.
- 100,000,000 FE base controller storage and 2,048 kM base matter storage.
- Matter-driven FE generation scaled by anomaly mass and reactor efficiency.
- Internal ring power distribution and connected-machine FE usage diagnostics.
- Reactor Remote support.
- Power Storage and Matter Storage upgrades.
- Speed upgrades increase generation rate and matter consumption proportionally.
- Range upgrades extend vertical anomaly search from the base three-block distance, capped at 16 blocks.
- Gravitational Anomaly persistent mass, entity/item pull and event-horizon consumption.
- Living-entity mass is added once when the event horizon kills the entity.
- Space-Time Equalizer immunity to anomaly pull/event-horizon effects.
- Gravitational Stabilizers with FE use, facing/beam checks, suppression and upgrades.

## Weapons

Implemented energy weapons:

- Phaser
- Phaser Rifle
- Ion Sniper
- Plasma Shotgun

Implemented weapon support includes server-authoritative shot energy checks, heat/overheat state, battery/Energy Pack reloads, Weapon Station installation and the current barrel/sight/ricochet/colour module system.

Normal and HC batteries transfer only the FE they actually contain and remain as drained rechargeable items. Energy Packs contribute their defined 32,000 FE and are consumed.

Weapon held transforms and some original visual/recoil presentation are still below legacy parity and remain a client-side refinement target.

## Android system

Implemented:

- Blue Pill conversion to Android state.
- Persistent Android FE/state.
- Red Pill deactivation and installed-part return.
- Yellow Pill Android-FE recharge.
- Sneak-held normal/HC Battery charging from either hand at up to 1,024 FE/t, transferring only energy actually stored in the battery.
- Legacy-style zero-power state: the Android HUD reports the offline core and movement speed is reduced by 50% until the core is recharged or conversion is removed.
- Head, Chest, Arms and Legs bionic-part installation through the Android Station.
- Android HUD energy/state synchronisation. Persistent Android XP/level progression foundation with a four-node, level-gated skill tree (Cloak L1, Force Field L2, Sonic Shockwave L3, Ender Teleport L4) (levels 1-10, XP from conversion, part installation and successful active abilities) with server-synchronised HUD display.
- Part-gated active abilities with server-authoritative input: Head/Cloak, Chest/Force Field, Arms/Sonic Shockwave and Legs/Ender Teleport.
- Atomic Android FE spending: actions that cannot afford their full cost fail without silently draining a partial remainder.
- Arms damage is restricted to direct melee attacks; projectiles and Sonic Shockwave cannot inherit the melee bonus or its additional FE cost.
- Persistent ability selection, toggle state and cooldowns, with FE costs and an expanded Android HUD.
- Android Station charging within four blocks, shared fairly across nearby converted players.
- Simplified Rogue Android Spawner using a tagged hostile Husk and bionic-part drops. It searches collision-free spawn candidates and charges 20,000 FE only after the entity is accepted by the world.

The Chest part uses vanilla Resistance while powered and does not stack a hidden second damage multiplier.

## Contracts and Star Map

Implemented current progression slice:

- Contract Market with collect/hunt Contract items.
- Persistent target, goal, progress and reward data.
- Exact post-pickup collect tracking and player-kill hunt tracking.
- One action advances one matching contract rather than every duplicate contract.
- Completed-contract redemption at the Contract Market.
- 1,200-tick market refresh delay after the final offer is taken.
- Star Map screen reports the viewing player's active/completed contracts with viewer-specific data.

This is a simplified replacement for part of the original quest/Star Map ecosystem and is not full legacy parity.

## Armour, tools and presentation

- Tritanium tool set and armour set are functional.
- Full Tritanium armour applies the configured defensive set bonus.
- Equipped armour uses the restored Tritanium armour textures.
- Industrial Glass, Bounding Box, Matter Plasma and Molten Tritanium are assigned translucent client render layers.
- Legacy decorative blocks, resources and recipes are substantially registered/restored.

Visual runtime checks are still required for transparency, crate/Inscriber UV alignment, equipped armour and held weapon transforms.

# Missing or incomplete compared with the original 1.12.2 mod

The following are the major remaining parity gaps. This list tracks gameplay behaviour, not merely registry presence.

## 1. Android RPG / biotic ability system — partial

The first active-ability layer is restored: bionic parts unlock Cloak, Force Field, Sonic Shockwave and Ender Teleport, with server validation, FE costs, cooldowns, persistent selection/toggles and HUD status.

Still missing is the full legacy XP/unlock tree, multi-level stat progression, flash cooling and the richer Android minimap/team presentation. The current part-gated unlock model is a playable foundation rather than final legacy progression parity.

## 2. Full Star Map galaxy simulation — missing

The current Star Map is a contract-status interface. The original system modelled galaxies, stars and planets and developed gameplay around planet statistics, buildings, ships and travel/attack events.

Still missing:

- generated/persistent galaxy, star and planet data;
- galaxy/system/planet navigation UI;
- planet population, happiness, energy and matter-production statistics;
- residential/power/matter-extractor/ship-hangar building gameplay;
- ships, ownership and travel/attack events.

## 3. Full quest/dialog progression system — partial

The current Contract Market implements simple collect/hunt contracts. The original contained a broader quest framework with multiple quest logic types, quest XP/rewards, quest HUD/pages, generated progression and NPC/dialog-driven quests.

Still missing includes the original-style quest stack/multi-quest system, mining/crafting and other quest types, quest XP progression, Mad Scientist dialog/progression and Data Pad quest pages.

## 4. Omni Tool — missing

`omni_tool` is registered but does not yet restore the original hybrid tool/energy-weapon behaviour, firing/beam behaviour and associated presentation.

## 5. Holo Sign and security/ownership gameplay — missing

The Holo Sign is presently a basic registered block rather than the original programmable/security-aware holographic sign. The broader legacy ownership/security layer and security protocol item behaviour are also not restored.

## 6. Additional drive/network configuration items — incomplete

Pattern Drives and Transport Flash Drives are functional, but legacy generic/network flash-drive configuration behaviour is not fully restored. `flash_drive`, `network_flash_drive` and the security protocol items are currently generic items.

## 7. Original mobs and Rogue Android depth — mostly missing

The current Android Spawner deliberately uses a tagged vanilla Husk as a simplified Rogue Android. The original dedicated entity ecosystem is not yet ported, including richer Rogue Android AI/levels/equipment/teams/ranged variants and other legacy entities such as drones, failed animals and scientist NPC/mob content.

## 8. Legacy world generation — partial

Natural Tritanium and Dilithium ore generation is now restored through 1.20.1 configured/placed features and a Forge biome modifier. The remaining worldgen parity gap is the original themed content and world-spawn layer: crashed/cargo ships, underwater bases, anomaly/world events and related mob/NPC spawning still need dedicated modern implementations.

## 9. Remaining weapon parity — partial

The core four weapons and module effects are implemented, but legacy extras remain incomplete, including the old weapon enchantment/random-weapon ecosystem, richer Rogue Android weapon generation/drops and final recoil/model/beam presentation parity.

## 10. Legacy integrations — not ported

Old optional integration layers such as ComputerCraft/Tinkers/other 1.12-era compatibility code have not been recreated. These should be reconsidered individually against modern 1.20.1 equivalents rather than copied directly.

# Recommended parity order

1. Android XP/stat progression and a real Rogue Android entity.
2. Security/Holo Sign/network-drive functionality.
3. Themed world structures, anomaly/world events and legacy mobs/NPCs.
4. Full quest framework and then full Star Map galaxy simulation.
5. Weapon visual/enchantment/random-generation parity and optional mod integrations.

## Verification

Use the checklists under `docs/testing/`, especially:

- `AUDIT_FIXES_TESTING.md`
- `ANDROID_SYSTEM_TESTING.md`
- `MATTER_NETWORK_LOGISTICS_TESTING.md`
- `CONTRACTS_STAR_MAP_TESTING.md`
- `SURVIVAL_PROGRESSION_TESTING.md`
- `MICROWAVE_TESTING.md`
- `SPACETIME_ACCELERATOR_TESTING.md`
- `HANDHELD_MATTER_TOOLS_TESTING.md`
- `TO_TEST.md` for the three in-game documentation items, including the System Guide

A successful GitHub Actions build proves that the Forge project compiles/packages. It does not certify datapack loading, fresh-chunk ore generation, recipes, machine behaviour, in-world behaviour or rendering.