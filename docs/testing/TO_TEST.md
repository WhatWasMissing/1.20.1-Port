# Matter Overdrive 1.20.1 - Alpha Runtime Testing Checklist

Branch: `testing/main`
Legacy references:
- Original Matter Overdrive 1.7.10 source branch `simeonradivoev/MatterOverdrive@1.7.10` (`0.4.2`).
- Matter Overdrive 1.12.2 `0.7.1.0` jar and recovered source/resources.
- Matter Overdrive `0.8.0.0-alpha.4.1` legacy jar.
Build identity: `Alpha Version 3`, made by MVQ1303

This file is bundled in-game as the **M2 Testing Checklist**. GitHub Actions compilation is not a substitute for runtime verification.

## Already runtime-passed / build verified

- [x] Rogue Android combat and sounds.
- [x] Failed Cow, Pig, Sheep and Chicken spawning/behaviour.
- [x] Mad Scientist interaction and current Puny Humans quest.
- [x] Holo Sign thin monitor geometry and renamed-item programming.
- [x] Previous 1.7 parity baseline through Star Map navigation, wrench dismantle and visual fixes is GitHub-Actions build verified.

## Priority 1 - legacy GUI shell wired to current logic

- [ ] Open Decomposer, Replicator and Matter Analyzer. They should use the original Matter Overdrive scalable machine shell rather than the synthetic dark rectangular frame.
- [ ] Decomposer: original FE meter, matter meter and arrow artwork should update from the current live FE/matter/progress values.
- [ ] Replicator: original FE/matter/arrow elements should update while replication, network tasks, cycle timing and failure telemetry remain correct.
- [ ] Matter Analyzer: original FE/arrow elements and waveform should update while current analysis progress/pattern values remain correct.
- [ ] Open the other current machine GUIs. They should inherit the original scalable shell while retaining their current menus, slots and controls.
- [ ] Original small-slot artwork must remain aligned with every clickable slot.
- [ ] INF FE/debug controls and all current FE/matter text remain usable and readable.
- [ ] Resize the game window/UI scale and reopen several machines; the legacy shell must scale cleanly without gaps, seams or stretched slot hitboxes.

## Priority 1A - new front-facing Android/Reactor parity

- [ ] Android Station opens without clipped controls or overlapping inventory labels.
- [ ] `CYCLE` changes to the next unlocked Android ability and never selects an ability whose required part/level is unavailable.
- [ ] The selected ability name updates after cycling and reports `LOCKED`, `READY`, or `ACTIVE` correctly.
- [ ] Android online/offline state, FE, installed-part state, level, XP and perk points update without reopening the GUI.
- [ ] `SKILL TREE` opens the existing selectable perk tree directly from the Android Station.
- [ ] Select/refund a perk, return to the station and confirm level/perk-point state remains synchronized and persists through save/reload.
- [ ] Fusion Reactor shows the restored dual circular energy/matter presentation; each side must move independently with its live storage value.
- [ ] Reactor structure lamp/text changes between valid and the correct current fault reason.
- [ ] RUN/SCRAM and redstone mode controls still work and the front-page status updates immediately.
- [ ] Efficiency, generated FE/t, connected demand, ring power, matter drain, IO/stabilizer count and anomaly/hazard telemetry update while the GUI remains open.
- [ ] The reactor screen remains readable at multiple GUI scales and does not cover the four upgrade slots or player inventory.

## Priority 2 - Network Flash Drive restored from 1.7

- [ ] Hold a Network Flash Drive and right-click an inventory-capability block connected to the item network. Chat should report that destination was added.
- [ ] Right-click the same endpoint again. It should be removed rather than duplicated.
- [ ] Add two or more endpoints and confirm the tooltip lists their coordinates.
- [ ] Put the programmed Network Flash Drive in the Network Router filter slot. Items may route only into positions stored on that drive; non-listed destinations must not receive them.
- [ ] Sources do not need to be listed on the drive; the drive filters destinations, matching the original 1.7 role.
- [ ] Install an empty Network Flash Drive. The router should move no items until at least one destination is programmed.
- [ ] Remove the drive and confirm ordinary item-filter routing still works exactly as before.
- [ ] Save/reload with a programmed drive in inventory and in the Router. CONNECTIONS entries must persist.
- [ ] Pylon-linked/remote network sections still obey the programmed destination list when those endpoints are reachable by the current network scan.

## Priority 3 - previous 1.7-derived restorations

- [ ] Holo Sign text is readable on the physical screen-facing side for north/east/south/west placement.
- [ ] Pattern Storage, Pattern Monitor and Space-Time Accelerator do not hide faces of adjacent normal blocks.
- [ ] Pylon renders normally with no purple/black missing model and channel routing still works.
- [ ] Adjacent Industrial Glass suppresses shared internal faces and exposes faces immediately when a neighbour is broken.
- [ ] Star Map mouse-wheel zoom and click-drag pan work while contract counts remain intact.
- [ ] Tritanium Wrench normal-use rotates; sneak-use dismantles Matter Overdrive blocks through normal breaking.
- [ ] A secured block cannot be sneak-wrench dismantled by a player lacking owner/matching Access permission.

## Priority 4 - pipes / visuals / persistence

- [ ] Matter Pipe, Heavy Energy Cable and Network Pipe arms connect/update in all six directions and keep their transfer behaviour.
- [ ] Reactor Coil, Controller, IO and Gravitational Stabilizer retain restored source face assignments/orientation.
- [ ] Pattern Drive empty/partial/full and Matter Scanner offline/online item states still update.
- [ ] Existing machines retain inventories, FE, matter, upgrades, contracts, drives and ownership after save/reload.

## Core gameplay regression

- [ ] Matter production/storage/replication works end-to-end.
- [ ] Reactor ring, IO, anomaly mass, stabilizers, shared ring power, RUN/SCRAM, remote and overlay work.
- [ ] Weapons require valid FE, heat/reload correctly and do not drain unrelated guns.
- [ ] Android HUD, V cycle, B activate, K tree and perk persistence/refunds work.

## Next parity targets not claimed yet

- Generic per-machine redstone modes (`none/high/low`) beyond currently wired machines.
- Machine-specific original GUI tabs/pages, especially task/configuration and upgrade-page presentation where matching 1.20.1 functionality exists.
- Full Star Map galaxy/star/planet data model, selection, travel and events.
- Cocktail of Ascension / Mutant Scientist and deeper quest/dialog framework.
- Ranged Androids, drones and richer entity AI.
- Crashed/cargo ships, underwater bases, Mad Scientist houses and remaining world events.
- Exact Pylon animated overlay, full weapon module meshes/recoil/scope presentation, and additional legacy network dispatcher/broadcaster depth.

## Pass criteria

Pass this build when the original GUI shell/elements render cleanly without altering current machine logic, Android and reactor front-facing controls remain synchronized with their existing backend systems, Network Flash Drive destination filtering works and persists, the prior 1.7 visual/wrench/Star Map fixes still pass, and the existing core systems remain functional.
