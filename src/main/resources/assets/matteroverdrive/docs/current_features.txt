# Matter Overdrive 1.20.1 — Feature Reference

Branch: `testing/alpha`
Legacy reference: MatterOverdrive 1.12.2 `0.7.1.0` jar plus recovered source/resources

This is the current source-of-truth feature list for the 1.20.1 port. A registered ID or bundled resource is not automatically considered complete; this document separates working implementations from partial legacy-parity work.

The latest restored-entity/Mad Scientist parity batch has passed the repository GitHub Actions build. Runtime behaviour still needs the focused tests in `docs/testing/TO_TEST.md`.

## Implemented core systems

### Matter machines and replication

- Matter Decomposer: FE-powered decomposition into matter, upgrades, failure chance and debug telemetry.
- Matter Recycler: FE-powered recycling into matter, upgrades and persistence.
- Matter Analyzer: item analysis and Pattern Drive progression.
- Matter Replicator: queued pattern replication using matter and FE, including failure/cycle state.
- Pattern Storage: Pattern Drive storage and network pattern supply.
- Pattern Monitor: discovers network storage/replicators and submits replication work.
- Molecular Inscriber: Mk2-Mk4 Isolinear Circuit production, FE, upgrades and persistence.
- Matter Container: portable matter storage and transfer.
- Matter Pipe: matter transport between compatible endpoints.
- Matter Scanner: links to powered Pattern Storage and adds Pattern Drive progress from successfully scanned matter-valued blocks.
- Portable Decomposer: rechargeable FE buffer, persistent pickup filters, matter conversion and direct matter transfer.

### Power, transport and utility machines

- Solar Panel: daylight FE generation, storage/export and Power Storage upgrades.
- Charging Station: FE buffer and charging for compatible batteries/energy items.
- Microwave: food-only FE-powered cooker with battery support, upgrades, persistence and break-safe inventory return.
- Space-Time Accelerator: FE-and-matter-powered extra ticking with redstone control, upgrades, persistence and debug controls.
- Transporter: Transport Flash Drive binding and same-dimension entity transport with upgrades.
- Tritanium Crates: 54-slot portable storage across colour variants with dropped-item NBT retention.
- Weapon Station: weapon/module editing, persistence and safe content return on break.

### Matter/network logistics

- Heavy Energy Cable uses the legacy `heavy_matter_pipe` registry ID for FE transport.
- Network Pipe provides item/pattern-network connectivity.
- Network Switch enables/disables item logistics and pattern traversal.
- Network Router provides powered filtered logistics, diagnostics and anti-bounce routing.
- Dimensional Pylon provides matching-channel wireless item-network bridging.
- Pattern-network discovery works across enabled Pipe/Router/Switch paths.

### Survival resources and world generation

- Tritanium Ore generates naturally in fresh Overworld chunks and smelts/blasts into Tritanium Ingots.
- Dilithium Ore generates naturally in fresh Overworld chunks and smelts/blasts into Dilithium Crystals.
- Tritanium tools and armour are functional, including the configured full-set bonus.
- Blue, Red and Yellow Android Pills remain craftable as a practical progression/test path while deeper legacy NPC quest progression is still being restored.

## Fusion Reactor and gravitational systems

- Horizontal Fusion Reactor ring validation with controller, hull, coil/flexible and IO positions.
- Reactor Assembly Guide/overlay support.
- Shared controller/formed-IO FE and matter storage.
- Legacy-style generation scaling with anomaly mass and structure efficiency.
- Vertical anomaly offset efficiency curve and Range-upgrade extension.
- Speed upgrades scale generation and matter consumption together; Power Storage and Matter Storage expand buffers.
- Formed IO faces support FE extraction, cable chains and matter transfer.
- Compatible machines inside the ring can receive shared reactor power.
- Persistent RUN/SCRAM and redstone modes.
- Comparator output and Reactor Remote controls.
- Expanded GUI/debug telemetry for output, demand, IO count, ring power, anomaly mass, suppression, pull/horizon ranges and destruction.
- Gravitational Anomaly persistent mass, item/living pull, event-horizon consumption and block/fluid destruction.
- Living-entity mass is added once when the event horizon kills an entity.
- Space-Time Equalizer immunity to anomaly pull/event-horizon effects.
- Gravitational Stabilizers consume FE, search a clear beam, apply suppression, support upgrades and persistent redstone modes.

Important: anomaly block/fluid damage is active. Stabilize and isolate large anomalies before feeding them substantial mass.

## Weapons

Implemented energy weapons:

- Phaser
- Phaser Rifle
- Ion Sniper
- Plasma Shotgun
- Omni Tool

Implemented weapon support includes:

- server-authoritative shot energy checks;
- heat/overheat and reload behaviour;
- normal and HC batteries that transfer only their stored FE and remain rechargeable;
- consumable Energy Packs;
- Weapon Station installation/persistence;
- barrel, sight, ricochet and colour module support;
- Omni Tool ranged fire plus powered pickaxe/axe/shovel behaviour.

Remaining weapon parity is mostly presentation and deeper legacy ecosystem work: first-person positioning, recoil/zoom/hand animation, richer beam/module rendering, random/enchantment weapon generation and ranged Rogue Android weapon variants.

## Android system

Implemented:

- Blue Pill conversion and persistent Android state/FE.
- Red Pill deactivation and installed-part return.
- Yellow Pill recharge.
- Battery/HC Battery charging while sneaking, consuming only actual battery FE.
- Zero-power HUD/offline state with movement penalty until recharge/deactivation.
- Head, Chest, Arms and Legs installation through the Android Station.
- Android HUD synchronisation and persistent XP/level progression.
- Three-branch, ten-level selectable perk tree with thirty functional perks.
- One perk point per reached level, persistent selections and level-up persistence protection.
- Individual 2,500 FE perk refunds and confirmed 25,000 FE full reset.
- Part-gated active abilities: Cloak, Force Field, Sonic Shockwave and Ender Teleport.
- Server-authoritative FE costs/cooldowns and atomic FE spending.
- Android Station nearby-player charging.

The current progression tree is playable but still does not reproduce every old multi-rank/stat/minimap/team feature from the 1.12.2 Android system.

## Security Protocol and machine ownership

Restored from the 1.12.2 jar:

- Security Protocol states: Empty, Claim, Access and Remove.
- Sneak-use in air binds an unbound protocol to the player and cycles bound modes Claim -> Access -> Remove -> Claim.
- Claim protocols store ownership on Matter Overdrive block-entity machines and are consumed when successfully applied.
- Machine owners can use and dismantle their claimed machines normally.
- Other players are denied machine use/break access unless they carry a matching Access protocol or are in Creative mode.
- Matching Remove protocols clear ownership and are consumed on success.
- Protocol owner UUID and machine ownership persist in NBT.
- Security applies broadly across Matter Overdrive block-entity machines rather than being limited to one machine type.

This system is build-verified but still needs multiplayer/runtime verification for all machine interaction paths.

## Holographic Sign

The old placeholder Holo Sign has been replaced by a dedicated 1.20.1 block/block-entity implementation:

- persistent `Text` data;
- client update packets and chunk/reconnect synchronisation;
- full-bright floating holographic text renderer;
- automatic text scaling for long messages;
- renamed-item programming by sneak-use;
- empty-hand sneak-use clearing;
- normal-use status/text feedback;
- 256-character safety cap;
- integration with the restored Security Protocol ownership layer.

The legacy 1.12.2 sign GUI is not reproduced exactly; the modern interaction route intentionally avoids requiring a dedicated text-entry packet/GUI for the initial parity implementation.

## Restored legacy entities

### Rogue Android

The previous tagged-Husk shortcut has been replaced for new spawns by a real `matteroverdrive:rogue_android` entity type.

Current restored behaviour includes:

- dedicated Rogue Android entity registration and renderer/resource path;
- legacy-inspired 0.30 movement speed and 24-block follow range;
- Android levels 0-3;
- normal max health `32 + level * 10`;
- normal melee damage `4 + level`;
- Legendary state with stronger restored combat values;
- persistent level/Legendary state through save/load;
- no sunlight burning;
- immunity to potion effects;
- original Rogue Android ambient/death sounds;
- bionic-part drops;
- Android Spawner now creates the real entity after successful collision checks and FE payment;
- natural-spawn placement logic has been restored and is tracked for balance testing.

Compatibility handling for older tagged-Husk Rogue Android test entities is retained so existing test worlds do not need to be discarded immediately.

### Failed animals

Real entity types now exist for:

- `matteroverdrive:failed_cow`
- `matteroverdrive:failed_pig`
- `matteroverdrive:failed_sheep`
- `matteroverdrive:failed_chicken`

They use restored Matter Overdrive textures/resources and the bundled failed-animal sounds while retaining stable modern passive-animal foundations.

### Mad Scientist

A real `matteroverdrive:mad_scientist` NPC has been restored using the bundled legacy scientist resources.

Current behaviour includes:

- normal and Junkie states;
- persistent NPC state/name;
- direct player interaction;
- restored `Puny Humans` progression slice.

## Puny Humans quest

The first concrete legacy NPC quest route is restored:

1. A non-Android player interacts with a Mad Scientist.
2. `Puny Humans` starts and asks the player to become an Android.
3. Quest state is stored in player-persistent NBT.
4. After Android conversion, returning to a Mad Scientist completes the quest.
5. Current restored reward bundle: one Battery, one Blue Android Pill and five Yellow Android Pills.
6. Quest start/complete sounds play and rewards are protected against a full inventory by dropping overflow safely.
7. Completion is one-time and persists across relog/death.

This is not yet the entire old Mad Scientist quest/dialogue chain. `Cocktail of Ascension`, Mutant Scientist replacement/progression and the broader generated quest framework remain future parity work.

## Contracts, Data Pad and Star Map

Implemented current progression slice:

- Contract Market with collect/hunt Contract items.
- Persistent target, goal, progress and reward data.
- Exact pickup/kill tracking with one action advancing one matching contract.
- Completed-contract redemption and market refresh timing.
- Data Pad guide plus persistent scan history.
- Star Map screen currently reports viewer-specific active/completed contract state.

The current Star Map is not a recreation of the original galaxy simulation.

## In-game documentation and version identity

- M2 Testing Checklist item opens the bundled `TO_TEST` documentation.
- Current Feature Reference item opens the bundled copy of this document.
- Matter Overdrive System Guide provides simplified and detailed instructions for currently working systems.
- Paged/indexed documentation navigation and last-page persistence are implemented.
- Current alpha chat identity marker is `Alpha Version 3`, made by MVQ1303.

The build verifies that repository documentation and bundled in-game copies are identical for the files configured in `build.gradle`.

# Major remaining parity gaps

## 1. Weapon presentation and legacy weapon ecosystem — partial

Core weapon gameplay works, but dedicated legacy-grade first-person recoil/zoom/hand/module rendering and richer random/enchantment weapon generation remain incomplete.

## 2. Full quest/dialog framework — partial

`Puny Humans` is restored, but the original mod had a much broader quest stack including additional objective types, generated quests, dialog chains, XP/rewards, HUD/pages and deeper Mad Scientist progression.

Still missing includes `Cocktail of Ascension`, Mutant Scientist progression and several NPC-driven quest branches.

## 3. Additional entities/AI — partial

Rogue Android, Mad Scientist and four failed animals now exist as real entity types. Still missing or incomplete are ranged Rogue Android variants, drones, Mutant/Hulking Scientist content and richer legacy AI/team/equipment systems.

## 4. Legacy structures and world events — partial

Ore generation is restored, but themed structures/events remain incomplete: crashed/cargo ships, underwater bases, Mad Scientist houses, anomaly/world events and associated mob/NPC spawn ecosystems.

## 5. Full Star Map galaxy simulation — missing

Still missing are persistent galaxies/stars/planets, navigation UI, planet statistics, population/happiness/production, buildings, ships, ownership and travel/attack events.

## 6. Network/drive depth — incomplete

Pattern Drives and Transport Flash Drives work. Generic/network flash-drive configuration and some advanced legacy network controls still need modern implementations.

## 7. Android deep parity — partial

The modern Android tree is extensive and functional, but exact legacy multi-rank stat progression, flash cooling and richer minimap/team/presentation behaviour remain incomplete.

## 8. Legacy integrations — not ported

Old 1.12-era optional integrations should be reconsidered individually against modern 1.20.1 equivalents rather than copied directly.

# Recommended next parity order

1. Complete in-game testing of Security/Holo Sign and restored entities.
2. Restore ranged Rogue Androids/drones plus Mutant Scientist/Cocktail of Ascension.
3. Expand the quest/dialog framework.
4. Add legacy structures/world events.
5. Finish weapon visual parity.
6. Build out the full Star Map galaxy simulation.

## Verification

Use `docs/testing/TO_TEST.md` as the consolidated runtime checklist. A successful GitHub Actions build proves that the Forge project compiles and packages; it does not certify in-world rendering, balance, networking, persistence, fresh-chunk generation or multiplayer security behaviour.
