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
- [x] Mad Scientist interaction and Puny Humans quest.
- [x] Holo Sign thin geometry and renamed-item programming.
- [x] Earlier reactor/network/matter baseline through Star Map navigation and wrench fixes.
- [x] Charging Station / Space-Time Accelerator / deeper Star Map code through `7fee056` was GitHub-Actions build verified.

## Consolidation pass - highest priority regression

- [ ] Network Router with no upgrade moves normally and still honours item filters and Network Flash Drive destination filters.
- [ ] Add Speed/Hyper Speed upgrades to the Router. The displayed item budget increases and the router can actually spend more than one stack of that budget in one tick when several source stacks/endpoints are available.
- [ ] Router never moves more items than available FE can pay for at 10 FE/item.
- [ ] Router four upgrade slots accept only Speed/Hyper Speed, persist after reload and return their contents when dismantled.
- [ ] Network Switch toggling changes both routing connectivity and active/inactive block appearance; the state persists after reload.
- [ ] Transporter operates when stored FE is exactly equal to the displayed transport cost, not only when FE is greater than cost.
- [ ] Transporter still caps each completed cycle at 3 living entities and ignores dead/removed entities on the pad.
- [ ] Dismantling a Transporter returns drive, energy item and all upgrades exactly once; no deleted contents and no duplicate drops.
- [ ] Android Station selected-ability frame, ability text, CYCLE/SKILL TREE buttons and inventory no longer overlap at normal GUI scales.

## Android Spawner legacy parity

- [ ] Right-click Android Spawner opens the operator screen with live FE, owned Android count, maximum population and next-spawn time.
- [ ] Powered spawner reaches up to 6 Androids rather than stopping after the first nearby Android.
- [ ] Over repeated spawns, both melee and ranged Rogue Androids appear; intended legacy mix is 30% melee / 70% ranged.
- [ ] A naturally spawned/unrelated Rogue Android beside the machine does not consume one of that spawner's six owned slots.
- [ ] Spawned melee and ranged Androids retain their originating spawner identity through save/reload.
- [ ] `KILL ALL` removes only Androids belonging to the current spawner and does not delete natural Androids or Androids from a different spawner.
- [ ] Destroy/rebuild one spawner near another and verify ownership is not mixed between their populations.
- [ ] Android Spawner GUI has no text/button/inventory overlap at several GUI scales.

## Legacy entities / quest regression

- [ ] Ranged Rogue Android carries a Phaser Rifle or Ion Sniper and performs ranged attacks normally.
- [ ] Mutant Scientist has the restored approximately 1.0 x 2.3 collision dimensions, 256 base health, 0.25 movement speed and 4 base attack damage.
- [ ] Mutant Scientist attacks nearby living entities but does not target another Mutant Scientist.
- [ ] Owned Drone does not attack its owner; two Drones with the same owner treat each other as allies.
- [ ] Unowned hostile Drone continues to attack players normally.
- [ ] Cocktail of Ascension tracks 5 shovel Creeper kills plus 5 gunpowder and 5 red mushrooms.
- [ ] Completing Cocktail successfully spawns the Mutant Scientist, then consumes ingredients and marks the quest complete.
- [ ] If the Mutant Scientist cannot be created/spawned, Cocktail must leave ingredients and quest state intact instead of silently completing.

## Machine GUI / operator parity

- [ ] Decomposer FE, matter and progress presentation updates correctly.
- [ ] Replicator Home/Tasks/Config/Upgrades pages retain replication, queue, timing and failure telemetry.
- [ ] Matter Analyzer Home/Tasks/Config/Upgrades pages retain waveform, pattern progress and server-side redstone cycling.
- [ ] Inscriber Home/Tasks/Upgrades pages show recipe tier, running state, progress, FE/t and total cycle cost live.
- [ ] Charging Station HOME/ANDROID/UPGRADES pages remain aligned with all physical slots.
- [ ] Weapon Station LOADOUT reflects all six real module slots immediately; STATS previews current unpacked modules.
- [ ] Fusion Reactor dual FE/matter rings, fault/status lamps, RUN/SCRAM, redstone mode and live demand/output telemetry remain functional.
- [ ] Resize the game window/UI scale and reopen several machines; no displaced slot hitboxes or invisible clickable slots.

## Charging Station Android parity

- [ ] Nearby Android charges wirelessly without an inserted item.
- [ ] Base wireless range is 8 blocks and close-range baseline can reach 512 FE/t before upgrades.
- [ ] Charge rate falls with distance and multiple Android players can be charged in one tick when FE is available.
- [ ] Rechargeable FE item in the physical slot still charges after Android processing.
- [ ] Range, Power and Power Storage upgrades alter range/rate/capacity and persist.
- [ ] Range stacking respects the legacy x8 cap.

## Transporter destination parity

- [ ] Bound Transport Flash Drive works directly without IMPORT.
- [ ] IMPORT stores a persistent machine-side destination and duplicate coordinates select rather than duplicate it.
- [ ] `<` and `>` cycle several saved destinations; REMOVE clamps selection safely.
- [ ] Machine destinations and selected index persist without the source drive after save/reload.
- [ ] A named Transport Flash Drive keeps its custom name when bound/imported.
- [ ] Same-X/Z targets less than four vertical blocks away are rejected; nearby horizontal targets are not rejected by a fake four-block sphere.
- [ ] Target exactly at current range is rejected because valid distance is strictly less than range.
- [ ] Speed affects charge/cooldown, Power affects FE cost, Range affects max distance and Power Storage affects capacity.

## Space-Time Accelerator / Star Map

- [ ] Accelerator uses the 1.12 `[-radius, radius)` footprint (`2r x 2r`, not `(2r+1) x (2r+1)`).
- [ ] Accelerator Home/Tasks/Upgrades pages show FE, matter, pulse progress, interval, radius and last accelerated count.
- [ ] Star Map reaches Galaxy -> Quadrant -> Star -> Planet and retains wheel zoom, drag pan and right-click back navigation.
- [ ] Planet page shows deterministic type/orbit/habitability/temperature/gravity/moons/atmosphere values.
- [ ] Planet page still clearly reports travel unavailable; no fake travel action occurs.

## Network / persistence

- [ ] Network Flash Drive toggles inventory endpoints, persists CONNECTIONS and filters router destinations.
- [ ] Empty installed Network Flash Drive permits no destinations.
- [ ] Pylon-linked network sections obey matching channel and destination filters.
- [ ] Matter Pipe, Heavy Energy Cable and Network Pipe arms update in all six directions without breaking transfer.
- [ ] Existing machines retain inventories, FE, matter, upgrades, contracts, drives, saved locations and ownership after save/reload.

## Core gameplay regression

- [ ] Matter production/storage/analysis/replication works end-to-end.
- [ ] Reactor ring, IO, anomaly mass, stabilizers, shared ring power, remote and overlay remain functional.
- [ ] Weapons require valid FE, heat/reload correctly and do not drain unrelated weapons.
- [ ] Android HUD, V cycle, B activate, K tree and perk persistence/refunds work.
- [ ] Security Claim/Access/Remove rules still gate normal interaction and wrench dismantling correctly.
