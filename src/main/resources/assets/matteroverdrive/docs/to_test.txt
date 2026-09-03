# Consolidated Runtime Testing Checklist

Branch: `testing/main`

Use this after a fresh pull and normal M2 client launch. Record the expected result, actual result, coordinates/orientation and relevant log lines for every failure.

## Build and runtime gate

- [ ] Reach the main menu without registry, datapack, model or menu errors.
- [ ] Create/load a test world successfully.
- [ ] Confirm the runtime marker reports `blocks=75`, `blockItems=72`, `standaloneItems=103`, `sounds=57`, `blockEntities=18` and `menus=15`.
- [ ] Confirm the marker includes `microwave=enabled`, `spacetimeAccelerator=enabled`, `handheldMatterTools=enabled`, `androidAbilities=enabled`, `documentationItems=enabled` and `systemGuide=enabled`.
- [ ] Confirm new handheld recipes appear in the recipe book/JEI and can be crafted in Survival.

## In-game documentation and version identity

- [ ] Obtain the M2 Testing Checklist, Current Feature Reference and Matter Overdrive System Guide items from the Matter Overdrive creative tab or craft them in Survival.
- [ ] Right-click each item and confirm it opens the correct full document in a scrollable screen.
- [ ] Verify mouse wheel, Page Up/Down, Home/End, scrollbar, Done/Escape and multiple GUI scales.
- [ ] Compare several headings and entries with `docs/testing/TO_TEST.md`, `docs/reference/WORKING_FEATURES.md` and `docs/reference/SYSTEM_GUIDE.md`.
- [ ] In the System Guide, sample survival, machine, network, reactor, weapon, Android and Contract sections. Confirm each provides Simplified and Detailed instructions and labels partial systems clearly.
- [ ] Join a single-player world and dedicated server. Confirm chat reports `Matter Overdrive Alpha 0.2 • Made by MVQ1303` once per login.
- [ ] Confirm the documentation packet handles an invalid fourth document safely and causes no dedicated-server client-class loading error.

## Survival resources and Android pills

- [ ] In fresh Overworld chunks, confirm Tritanium generates from Y -32 through 64 and Dilithium from Y -64 through 16.
- [ ] Smelt/blast both ores into their intended resources.
- [ ] Craft and verify Blue, Red and Yellow Android Pills.
- [ ] Save/reload and confirm Android state/FE remains correct.

## Android system and active abilities

- [ ] Install Head, Chest, Arms and Legs parts and confirm they unlock Cloak, Force Field, Sonic Shockwave and Ender Teleport respectively.
- [ ] Use/rebind the default `V` cycle and `B` activate controls; verify selected ability and cooldown on the Android HUD, and confirm enabled Cloak/Force Field indicators remain visible after cycling away.
- [ ] Sneak with a charged normal/HC Battery in either hand. Verify at most 1,024 FE/t transfers, only real battery FE is drained and the battery remains rechargeable.
- [ ] Drain Android FE to zero. Verify the HUD reports core offline, movement is reduced by 50%, and normal speed returns after recharge or deactivation.
- [ ] Verify exact/atomic FE use: insufficient actions leave partial FE untouched and sustained abilities shut down safely.
- [ ] Verify Arms adds 3 damage for 80 FE only to direct melee attacks; projectiles and Sonic Shockwave must not trigger that bonus.
- [ ] Verify Force Field reduces final post-armour/effect damage and shows activation/impact feedback. Verify Shockwave's fixed 4,096 FE cost/target filtering/knockback and test collision-safe Ender Teleport with level, upward and downward aim.
- [ ] Verify the Rogue Android Spawner uses a collision-free candidate and consumes no 20,000 FE charge when spawning fails.
- [ ] Verify ability selection, toggles and cooldowns across relog/death, and Red Pill cleanup.
- [ ] See `ANDROID_SYSTEM_TESTING.md` for the focused checklist.

## Microwave

- [ ] Accept valid food-smelting inputs and reject non-food items.
- [ ] Cook from cable FE and from a charged battery without free processing.
- [ ] Confirm base timing/cost, output blocking, upgrades, persistence, automation and break-safe drops.
- [ ] See `MICROWAVE_TESTING.md` for the focused checklist.

## Space-Time Accelerator

- [ ] Require both FE and matter; verify base usage and 40-tick pulse timing.
- [ ] Accelerate crops/random ticks and several block entities on the same Y level.
- [ ] Verify equal +X/-X/+Z/-Z boundary coverage.
- [ ] Verify redstone disable, every supported upgrade, persistence, break-safe drops and acceptable tick-time impact.
- [ ] See `SPACETIME_ACCELERATOR_TESTING.md` for the focused checklist.

## Matter Scanner

- [ ] Sneak-use the Scanner on a powered Pattern Storage containing a Pattern Drive and confirm the link is stored.
- [ ] Hold-use the linked Scanner on a block with a matter value for the full scan time.
- [ ] Confirm the block is destroyed only after a successful scan and the linked drive gains 10% pattern progress.
- [ ] Confirm ten valid blocks complete a normal pattern and a completed/full/unpowered/offline storage refuses further scans without destroying the target.
- [ ] Move dimensions, unload/break the linked storage and confirm scanning fails safely.
- [ ] Save/reload the Scanner and confirm its link and last-scan status persist.

## Portable Decomposer

- [ ] Charge it in the Charging Station and confirm its 128,000 FE capacity persists.
- [ ] Sneak-use with an offhand matter-valued item to add/remove that item from its pickup filter.
- [ ] Pick up matching stacks and confirm they are automatically consumed at the intended 10% matter yield while FE is available.
- [ ] Confirm unmatched, zero-matter, insufficient-FE and full-storage pickups enter the inventory normally.
- [ ] Confirm fractional yield is retained rather than granting free matter or silently losing every low-value item.
- [ ] Transfer stored matter into a compatible machine/container and verify exact values before/after.
- [ ] Save/reload with FE, matter, filter entries and fractional remainder present.

## Data Pad and guide

- [ ] Right-click in air and confirm the guide screen opens.
- [ ] Navigate every guide page and verify text/buttons at multiple GUI scales.
- [ ] Use the Data Pad on supported blocks and confirm scan-history entries record the block/item and matter value.
- [ ] Confirm history is capped, ordered newest-first and persists through save/reload.
- [ ] Confirm unknown/zero-matter blocks are identified without crashing.

## Core regression checks

- [ ] Reactor structure, anomaly mass, IO/cable FE transfer and stabilizers remain correct.
- [ ] Empty weapons cannot fire; real batteries transfer only their stored FE; Energy Packs remain consumable.
- [ ] Contract pickup/kill progress advances only one matching contract.
- [ ] Pattern Analyzer/Storage/Monitor/Replicator still work after Scanner-created patterns.
- [ ] Transparent blocks, crate/Inscriber models, equipped armour and held weapon transforms remain visually correct.
- [ ] Machine inventories/upgrades survive reload and return safely on block break.

## Pass criteria

The branch is ready to consolidate when the runtime gate passes and there are no new crashes, datapack errors, item loss/duplication, free-energy/free-matter exploits, asymmetric Accelerator range, broken pattern progression or handheld-state persistence failures.

## Android skill tree and paged documentation

- [ ] Convert and earn XP; confirm the Android HUD shows Level 1 and progression toward the next node.
- [ ] Confirm abilities require both the matching bionic part and the documented level: Cloak L1, Force Field L2, Sonic Shockwave L3, Ender Teleport L4.
- [ ] Confirm a locked ability reports whether its level or part requirement is missing, and that XP persists through relog/death/deactivation.
- [ ] Open each documentation item and confirm it displays Page N / M with Previous/Next buttons and Left/Right or Page Up/Page Down navigation.
- [ ] Open the reactor sections and confirm the paged guide explains controller, IO, heavy-cable routing, ring sharing, anomaly/stabilizer setup, overlay and current limits.

## Expanded selectable Android perk tree

- [ ] Press K while converted and confirm the three-column, ten-level perk tree opens at multiple GUI scales.
- [ ] Confirm one point is available per reached level and the HUD shows K plus the unspent count.
- [ ] Click a node and confirm it remains pending until Confirm Perk is pressed.
- [ ] Select Assault, Utility and Survival / Mobility perks on different levels; confirm the other two nodes on each completed row lock.
- [ ] Verify all original twenty perk selections remain intact in a world upgraded from the previous tree build.
- [ ] Test Sustained Systems passive drain, Quick Charge rate, Learning Matrix XP and Cooldown Router.
- [ ] Test Reactive Plating, Self Repair, Silent Cloak, Tactical Scan, Emergency Protocol and Synthetic Perfection.
- [ ] Hover every node and confirm its description, availability colour and selection state are accurate.
- [ ] Arm Reset Perks, cancel by choosing another action, then confirm a reset consumes exactly 25,000 FE, refunds every point and disables toggled abilities.
- [ ] Confirm insufficient FE cannot reset perks and malformed/repeated perk packets grant nothing.
- [ ] Verify selections and refunded points persist through screen close, relog, death and Red Pill deactivation/reactivation.
