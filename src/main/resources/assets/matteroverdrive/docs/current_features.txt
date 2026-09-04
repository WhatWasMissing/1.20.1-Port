# Matter Overdrive 1.20.1 - Current Feature Reference

Branch: `testing/alpha`
Legacy reference: MatterOverdrive 1.12.2 `0.7.1.0` jar and recovered source/resources
Build identity: `Alpha Version 3`, made by MVQ1303

This file is the source-of-truth feature summary and is bundled in-game as the **Current Feature Reference** item. Registered legacy IDs are not automatically counted as complete systems.

## Runtime-confirmed in the latest player test

- Rogue Android melee combat works.
- Rogue Android sounds work.
- Failed Cow, Pig, Sheep and Chicken spawn and behave correctly.
- Mad Scientist interaction and the current Puny Humans quest slice work.

The Holo Sign's original cube-model and crouch-programming failures are now runtime-confirmed fixed. The latest screenshots show the remaining renderer issue clearly: holographic text is still camera-billboarded, so it separates from the monitor plane at side/top viewing angles. That transform remains pending before the Holo Sign is visually verified.

## Matter and replication systems

- Matter Decomposer: FE-powered decomposition, matter storage/output, upgrades, failure behaviour and debug telemetry.
- Matter Recycler: FE-powered Matter Dust recycling with persistence and upgrades.
- Matter Analyzer: item analysis and Pattern Drive progression.
- Pattern Drive: two normal pattern entries with persistent progress/data.
- Pattern Storage: six drive slots and network pattern supply.
- Pattern Monitor: network discovery and replication request queue.
- Matter Replicator: FE + matter replication, queueing and intended failure output.
- Molecular Inscriber: Mk2-Mk4 Isolinear Circuit production with FE, upgrades and persistence.
- Matter Scanner: links to powered Pattern Storage and adds pattern progress from matter-valued blocks.
- Portable Decomposer: rechargeable filtered pickup conversion and direct matter transfer.
- Matter Containers and Matter Pipe: portable storage and machine-to-machine matter transfer.

## Power, transport and utility

- Solar Panel daylight generation, storage, export and Power Storage upgrades.
- Charging Station buffered charging for compatible FE items.
- Heavy Energy Cable using the legacy `heavy_matter_pipe` ID for FE transport.
- Microwave food-only powered cooking with upgrades, battery support and persistence.
- Space-Time Accelerator FE + matter extra-tick system with range/upgrades/redstone/debug controls.
- Transporter and Transport Flash Drive for supported same-dimension transport.
- Tritanium Crates: 54-slot portable inventory for base plus colour variants.
- Weapon Station: weapon/module editing, persistence and safe return on break.

## Matter Network logistics

- Network Pipe item/pattern-network connectivity.
- Network Switch route enable/disable and pattern traversal control.
- Powered Network Router with filtering, diagnostics and anti-bounce routing.
- Matching-channel Pylon wireless item-network bridging in the current supported range.

## Fusion Reactor and gravity

- Horizontal Fusion Reactor ring validation and exact fault/overlay diagnostics.
- Controller plus formed Reactor IO shared FE/matter storage.
- Mass/efficiency-scaled generation and proportional matter use.
- Speed, Range, Power Storage and Matter Storage upgrade behaviour.
- Direct IO extraction, long Heavy Energy Cable chains and multiple outputs.
- Internal ring machine power distribution and demand telemetry.
- Persistent RUN/SCRAM, redstone modes, comparator output and Reactor Remote.
- Persistent Gravitational Anomaly mass, pull, event-horizon consumption and environmental destruction.
- Living-entity mass is added once when event-horizon damage kills the entity.
- Space-Time Equalizer immunity.
- Powered Gravitational Stabilizers with clear-beam targeting, suppression, upgrades and redstone modes.

## Weapons

Playable weapons:

- Phaser
- Phaser Rifle
- Ion Sniper
- Plasma Shotgun
- Omni Tool

Current support includes server-authoritative FE payment, heat/overheat, reloads, rechargeable Batteries/HC Batteries, consumable Energy Packs, Weapon Station persistence, current barrel/sight/ricochet/colour modules and Omni Tool powered tool actions.

Remaining weapon parity is mainly renderer presentation and the wider original random/enchantment/module ecosystem.

## Android system

- Persistent Android conversion, FE and HUD.
- Head, Chest, Arms and Legs installation through Android Station.
- Part-gated Cloak, Force Field, Sonic Shockwave and Ender Teleport.
- V to cycle, B to activate and K to open the selectable skill tree.
- Three branches, ten levels and thirty functional perks.
- One point per reached level; perk selections survive level-up, relog and death.
- Individual confirmed refunds and confirmed full reset.
- Android Station charging and real-FE held Battery/HC Battery charging.
- Zero-power CORE OFFLINE state and movement penalty.

The modern tree is extensive and playable, but not every original multi-rank/stat/minimap/team presentation feature is reproduced yet.

## Security Protocol and ownership

Restored from the original jar:

- Empty, Claim, Access and Remove protocol states.
- Sneak-use binding and Claim -> Access -> Remove cycling.
- Persistent owner UUID.
- Machine/block-entity claiming and removal.
- Owner/Creative/matching-Access use and break permission checks.
- Broad integration across Matter Overdrive block-entity machines, including Holo Sign.

This is build-verified and still needs a focused multiplayer runtime pass.

## Holo Sign

- Dedicated Holo Sign block entity with persistent `Text`.
- Legacy thin monitor geometry and horizontal facing.
- Full-bright holographic text renderer with long-text scaling; current runtime feedback shows its camera-facing transform still needs to be replaced with monitor-face anchoring.
- Renamed-item sneak-use programming intercepted before a held BlockItem can place itself.
- Empty-hand sneak-use clearing.
- Client update/chunk sync and a 256-character cap.
- Security Protocol integration.

The original text-entry GUI is not yet recreated; the current programming route uses an anvil-renamed item.

## Restored legacy entities and quest slice

### Rogue Android

- Real `matteroverdrive:rogue_android` entity replaces new tagged-Husk spawns.
- Levels 0-3, Legendary state, persistent combat state, legacy-inspired health/damage/speed/follow range.
- No sunlight burning and potion-effect immunity.
- Original Rogue Android sounds and bionic-part drops.
- Android Spawner creates the real entity after successful placement/FE checks.
- Natural spawn logic is present and remains balance-sensitive.

### Failed animals

Real registered entity types and legacy resources:

- Failed Cow
- Failed Pig
- Failed Sheep
- Failed Chicken

### Mad Scientist / Puny Humans

- Real Mad Scientist NPC with persistent normal/Junkie state.
- Puny Humans starts from NPC interaction, persists on the player and completes after Android conversion.
- Current reward bundle is one Battery, one Blue Android Pill and five Yellow Android Pills.
- Start/completion feedback and one-time completion are implemented.

Deeper Cocktail of Ascension / Mutant Scientist progression remains future work.

## Survival and progression foundation

- Natural Tritanium and Dilithium ore generation in fresh Overworld chunks.
- Furnace/blast recipes for intended resources.
- Tritanium tools and armour plus configured full-set behaviour.
- Contract Market collect/hunt contracts, exact progress tracking, one-time redemption and refresh persistence.
- Data Pad guide and persistent scan history.
- Star Map currently displays viewer-specific contract status; it is not yet the original galaxy simulation.

## Source-faithful texture/model restoration

The current alpha visual pass deliberately reuses the original jar resources where practical instead of approximating them with generic cubes.

Restored or refined in this pass:

- Holo Sign: original thin monitor geometry.
- Android Station: original stepped platform geometry and texture assignment.
- Weapon Station: original stepped platform geometry and texture assignment.
- Star Map: original station geometry with Star Map side artwork.
- Contract Market: original thin monitor with Holo Monitor front, Network Port back and Base edges.
- Matter Analyzer: original detailed model/UV layout and directional placement.
- Decomposer and Matter Recycler: restored original face texture mapping.
- Microwave: original compact model/front/back/side mapping plus horizontal facing.
- Pattern Monitor: original thin monitor geometry plus horizontal facing.
- Pattern Storage: restored original legacy OBJ/MTL mesh and horizontal facing.
- Replicator: restored legacy model geometry/UVs and directional mapping.
- Charging Station: restored original tall OBJ/MTL mesh and horizontal facing.
- Space-Time Accelerator: restored original narrow three-stage column model.
- Solar Panel: restored half-height visual model.
- Existing earlier restorations remain in place for Tritanium Crate OBJ, Inscriber OBJ, armour textures, gun transforms and translucent render types.

These visual changes are source/build checked but require the runtime checklist before being marked visually verified.

## Major remaining parity gaps

1. Full Cocktail of Ascension / Mutant Scientist and wider dialog/quest framework.
2. Ranged Rogue Androids, drones and richer original entity AI/equipment/team systems.
3. Crashed/cargo ships, underwater bases, Mad Scientist houses and other legacy structures/world events.
4. Full Star Map galaxy/star/planet simulation, ownership, buildings, ships and travel/events.
5. Connected pipe geometry matching the original centre-plus-six-connection models.
6. Full Pylon legacy multi-block OBJ/overlay presentation.
7. Remaining emissive/overlay/connected-texture presentation not expressible by the current simple model pass.
8. Complete weapon module meshes, recoil/zoom/hand animation and random/enchantment ecosystem.
9. Deeper generic/network flash-drive configuration.
10. Optional old 1.12 integrations, which need modern equivalents rather than direct API copies.

## Verification

Open the in-game **M2 Testing Checklist** or `docs/testing/TO_TEST.md` for the exact current runtime pass. GitHub Actions compilation is necessary, but visual orientation, model baking, persistence, multiplayer ownership and balance must still be checked in Minecraft.
