# Matter Overdrive 1.20.1 - Alpha Runtime Testing Checklist

Branch: `testing/main`
Legacy references:
- Matter Overdrive 1.7.10 `0.4.2` jar.
- Matter Overdrive 1.12.2 `0.7.1.0` universal jar.

The 0.8 alpha jar is intentionally excluded from parity decisions.
Build identity: `Alpha Version 3`, made by MVQ1303

This file is bundled in-game as the **M2 Testing Checklist**. GitHub Actions compilation is not a substitute for runtime verification.

## Already runtime-passed / build verified

- [x] Rogue Android combat and sounds.
- [x] Failed Cow, Pig, Sheep and Chicken spawning/behaviour.
- [x] Mad Scientist interaction and current Puny Humans quest.
- [x] Holo Sign thin monitor geometry and renamed-item programming.
- [x] Previous 1.7 parity baseline through Star Map navigation, wrench dismantle and visual fixes is GitHub-Actions build verified.
- [x] Charging Station / Space-Time Accelerator / deeper Star Map code through commit `7fee056` is GitHub-Actions build verified.

## Priority 1 - legacy GUI shell wired to current logic

- [ ] Open Decomposer, Replicator and Matter Analyzer and verify legacy presentation remains aligned with current live machine state.
- [ ] Decomposer FE, matter and progress presentation updates correctly.
- [ ] Replicator Home/Tasks/Config/Upgrades pages retain replication, network queue, timing and failure telemetry.
- [ ] Matter Analyzer Home/Tasks/Config/Upgrades pages retain scan waveform, pattern progress and server-side redstone cycling.
- [ ] Inscriber Home/Tasks/Upgrades pages show recipe tier, running state, progress, FE/t and total cycle cost live.
- [ ] Transporter Home/Tasks/Locations/Upgrades pages show destination, range, distance, cost, progress and cooldown live.
- [ ] Original small-slot artwork remains aligned with every clickable slot.
- [ ] INF FE/debug controls and all current FE/matter text remain usable and readable.
- [ ] Resize the game window/UI scale and reopen several machines; no gaps, seams or displaced slot hitboxes.

## Priority 1A - Android / Reactor / Weapon Station front-facing parity

- [ ] Android Station opens without clipped controls or overlapping inventory labels.
- [ ] HEAD/CHEST/ARMS/LEGS use the restored original Android slot icons; missing parts are visibly darkened and installed parts highlighted.
- [ ] Selected Android ability uses the original feature-icon frame and changes presentation between locked, ready and active.
- [ ] `CYCLE` changes to the next unlocked Android ability and never selects an unavailable ability.
- [ ] Android online/offline state, FE, installed-part state, level, XP and perk points update without reopening the GUI.
- [ ] `SKILL TREE` opens the existing selectable perk tree directly from the Android Station.
- [ ] Select/refund a perk, return to the station and confirm state remains synchronized and persists through save/reload.
- [ ] Weapon Station shows the real six module roles: Battery, Color, Barrel, Sights and two Utility slots.
- [ ] Weapon Station LOADOUT page updates immediately when modules are inserted/removed.
- [ ] Weapon Station STATS page previews the current station modules and updates damage, energy, cooldown and range multipliers without requiring the weapon to be removed first.
- [ ] Removing the weapon still packs all six modules into the weapon; reopening the station unpacks them correctly.
- [ ] Fusion Reactor shows the restored dual circular energy/matter presentation; each side moves independently.
- [ ] Reactor structure lamp/text changes between valid and the correct current fault reason.
- [ ] RUN/SCRAM and redstone mode controls still work and the front-page status updates immediately.
- [ ] Efficiency, generated FE/t, connected demand, ring power, matter drain, IO/stabilizer count and anomaly/hazard telemetry update while open.
- [ ] Reactor remains readable at multiple GUI scales and does not cover upgrade slots or player inventory.

## Priority 1B - Charging Station legacy Android parity

- [ ] Power a Charging Station and stand nearby as an Android; Android FE should increase automatically without inserting an item.
- [ ] At close range, the station should deliver up to the legacy 512 FE/t baseline before upgrades; charge rate should fall off with distance and reach zero at the edge of range.
- [ ] Base Android charging range should be 8 blocks.
- [ ] Multiple Android players in range can be charged in the same tick while enough station FE remains; GUI reports Android count and total Android FE/t.
- [ ] A rechargeable FE item in the physical slot still charges after Android charging is processed; item charging remains a supported 1.20.1 extension.
- [ ] Install a Range upgrade and confirm the Android charging radius increases. Range stacking must respect the legacy x8 multiplier cap.
- [ ] Install a Power upgrade and confirm the maximum Android charge rate changes according to power-usage efficiency.
- [ ] Install a Power Storage upgrade and confirm the internal FE capacity increases and persists after save/reload.
- [ ] Only Range, Power and Power Storage upgrades are accepted by the four Charging Station upgrade slots.
- [ ] Break/dismantle the Charging Station with an item and upgrades installed; all five contents must be returned rather than deleting upgrades.
- [ ] HOME / ANDROID / UPGRADES pages switch without hiding or offsetting the real item/upgrade slot hitboxes.
- [ ] INF FE remains functional and powers both Android and item charging for debug testing.

## Priority 1C - Transporter legacy destination parity

- [ ] A bound Transport Flash Drive still works directly without first importing its target into the machine.
- [ ] Insert a bound drive and press `IMPORT`; the machine destination count should increase and that imported destination becomes selected.
- [ ] Import the same coordinates twice; it should select the existing entry rather than duplicate it.
- [ ] Import two or more different destinations, then use `<` and `>` to cycle the selected machine-side destination.
- [ ] Remove a selected destination; the remaining selection clamps correctly and transport continues to a remaining location.
- [ ] Save/reload the world and confirm imported machine destinations plus selected index persist without the drive being present.
- [ ] Destination validity matches 1.12: same X/Z destinations less than four vertical blocks away are rejected; a nearby horizontal target is not incorrectly rejected by a four-block sphere check.
- [ ] A destination exactly at the upgraded range boundary is rejected; valid transport distance must be strictly less than current range.
- [ ] Put four or more entities on the transporter pad. One completed cycle must teleport at most three entities, matching the 1.12 cap.
- [ ] A following cycle can transport remaining entities after the normal cooldown.
- [ ] Speed upgrades change both charge-up time and cooldown; Power changes FE cost; Range changes max distance; Power Storage changes FE capacity.
- [ ] Home/Tasks/Locations/Upgrades pages remain aligned with the two item slots and five real upgrade slots at multiple GUI scales.

## Priority 1D - Space-Time Accelerator / Star Map depth

- [ ] Space-Time Accelerator uses the 1.12 `[-radius, radius)` footprint, i.e. a `2r x 2r` horizontal target area rather than `(2r+1) x (2r+1)`.
- [ ] Accelerator Home/Tasks/Upgrades pages show live FE, matter, pulse completion, interval, radius and last accelerated target count.
- [ ] Speed, Hyper Speed, Power, Power Storage, Matter Storage and Range upgrade effects remain functional.
- [ ] Star Map navigation reaches Galaxy -> Quadrant -> Star -> Planet.
- [ ] Selecting a planet highlights it and shows type, orbit, habitability, temperature, gravity, moons and atmosphere values.
- [ ] Mouse-wheel zoom, left-drag pan and right-click back navigation work at every Star Map level.
- [ ] Player inventory stays below the planet information area and no Star Map text overlaps clickable inventory slots.
- [ ] Planet page clearly reports that travel is unavailable until the server travel/event layer is restored; no fake travel action should occur.

## Priority 2 - Network Flash Drive restored from 1.7

- [ ] Hold a Network Flash Drive and right-click an inventory-capability block connected to the item network. Chat should report that destination was added.
- [ ] Right-click the same endpoint again. It should be removed rather than duplicated.
- [ ] Add two or more endpoints and confirm the tooltip lists their coordinates.
- [ ] Put the programmed Network Flash Drive in the Network Router filter slot. Items may route only into positions stored on that drive.
- [ ] Sources do not need to be listed on the drive; the drive filters destinations, matching the original 1.7 role.
- [ ] Install an empty Network Flash Drive. The router should move no items until at least one destination is programmed.
- [ ] Remove the drive and confirm ordinary item-filter routing still works exactly as before.
- [ ] Save/reload with a programmed drive in inventory and in the Router. CONNECTIONS entries must persist.
- [ ] Pylon-linked/remote network sections still obey the programmed destination list when reachable.

## Priority 3 - previous 1.7-derived restorations

- [ ] Holo Sign text is readable on the physical screen-facing side for north/east/south/west placement.
- [ ] Pattern Storage, Pattern Monitor and Space-Time Accelerator do not hide faces of adjacent normal blocks.
- [ ] Pylon renders normally with no purple/black missing model and channel routing still works.
- [ ] Adjacent Industrial Glass suppresses shared internal faces and exposes faces immediately when a neighbour is broken.
- [ ] Star Map mouse-wheel zoom and click-drag pan work while contract counts remain intact.
- [ ] Tritanium Wrench normal-use rotates; sneak-use dismantles Matter Overdrive blocks through normal breaking.
- [ ] A secured block cannot be sneak-wrench dismantled by a player lacking owner/matching Access permission.

## Priority 4 - pipes / visuals / persistence

- [ ] Matter Pipe, Heavy Energy Cable and Network Pipe arms connect/update in all six directions and keep transfer behaviour.
- [ ] Reactor Coil, Controller, IO and Gravitational Stabilizer retain restored source face assignments/orientation.
- [ ] Pattern Drive empty/partial/full and Matter Scanner offline/online item states still update.
- [ ] Existing machines retain inventories, FE, matter, upgrades, contracts, drives and ownership after save/reload.

## Core gameplay regression

- [ ] Matter production/storage/replication works end-to-end.
- [ ] Reactor ring, IO, anomaly mass, stabilizers, shared ring power, RUN/SCRAM, remote and overlay work.
- [ ] Weapons require valid FE, heat/reload correctly and do not drain unrelated guns.
- [ ] Android HUD, V cycle, B activate, K tree and perk persistence/refunds work.
