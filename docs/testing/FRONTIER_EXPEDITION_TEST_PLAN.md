# Frontier Expedition Runtime Test Plan

Branch: `feature/lead-dev-expansion-2026-09-11`

Status: implementation committed; runtime verification pending.

## Build and startup gate

- [ ] Run `gradlew.bat compileJava --no-daemon`.
- [ ] Run the repository resource/documentation sanity tasks used by this branch.
- [ ] Start a clean Forge client and verify no registry, codec, structure-piece, datapack or resource-load errors.
- [ ] Start/load a dedicated server world and verify no structure serialization errors.

## Locate / generation checks

Use a new world. Locate each structure repeatedly so at least three starts (covering the deterministic layout variants where possible) are inspected.

- [ ] `/locate structure matteroverdrive:deep_matter_vault`
- [ ] `/locate structure matteroverdrive:autonomous_drone_foundry`
- [ ] `/locate structure matteroverdrive:anomaly_quarantine_site`
- [ ] `/locate structure matteroverdrive:orbital_recovery_array`

For every generated start:

- [ ] no world-load stall or watchdog warning;
- [ ] no blocks placed outside expected structure bounds;
- [ ] no visible chunk-border shearing caused by unclipped writes;
- [ ] entrance is reachable from terrain without breaking blocks;
- [ ] every required room can be reached from the entrance;
- [ ] corridors visibly connect rather than terminating in solid walls;
- [ ] vertical shafts have a survivable ascent/descent path;
- [ ] player can return to the entrance after reaching the deepest room;
- [ ] machines are placed on usable floor blocks and not inside walls;
- [ ] doors/openings have at least two blocks of player clearance;
- [ ] damaged variants remain traversable;
- [ ] occupied variants place security markers without blocking traversal;
- [ ] roofs/floors do not expose accidental open voids except intentional damage;
- [ ] structures survive save/reload without changing layout.

## Deep Matter Vault

- [ ] Surface/shaft entry leads into security and vault areas.
- [ ] Vault core visibly contains Facility Network Controller, Matter Storage Matrix and red Tritanium cache signature.
- [ ] Archive reads as data/research space and includes Pattern Storage / Matter Analyzer equipment.
- [ ] Refinery reads as matter-processing space and includes processing/storage machinery.
- [ ] Underground placement is not exposed absurdly on normal terrain and does not generate below build height.

## Autonomous Drone Foundry

- [ ] Foundry has a clear industrial entrance, control room, fabrication room, hangar and salvage yard.
- [ ] Control room contains Facility Network Controller and Charging Station signature.
- [ ] Fabrication wing contains Drone Fabricator and Android-related machinery.
- [ ] Hangar has adequate open floor area and does not feel like a standard small room.
- [ ] Salvage yard is visually distinct from the finished manufacturing interior.

## Anomaly Quarantine Site

- [ ] Entry/shaft correctly reaches the buried quarantine complex.
- [ ] Containment room contains Anomaly Containment Unit, Gravitational Stabilizers and Facility Network Controller.
- [ ] Security/decontamination/observation rooms are distinct and logically placed.
- [ ] Containment room remains traversable in damaged state.
- [ ] Dark/security palette clearly differentiates the quarantine site from ordinary research buildings.

## Orbital Recovery Array

- [ ] Main array/mast is visible from outside and has a readable purpose.
- [ ] Control room contains Facility Network Controller + Network Switch.
- [ ] Processing and storage rooms connect logically to the control/array areas.
- [ ] Catwalk has usable width and rails/readable edges.

## Frontier Expedition progression

Test with a fresh player profile / fresh world saved data.

- [ ] Enter any piece of a Deep Matter Vault: system message appears, dossier is awarded, progress becomes 1/4 and 40 XP is awarded.
- [ ] Enter the same vault again after changing chunks: no duplicate reward for the same structure start.
- [ ] Find a second Deep Matter Vault: location is logged, dossier is awarded, only 15 XP is awarded and unique progress remains 1/4.
- [ ] Discover the Drone Foundry, Quarantine Site and Recovery Array in any order: unique progress increases once per class.
- [ ] Fourth unique class awards the completion message, 120 bonus XP and exactly one Parallel Processing Upgrade.
- [ ] Re-enter/reload/restart after completion: completion reward is not duplicated.
- [ ] Inspect dossier NBT: `FrontierArchive` matches the discovered site and `FrontierProgress` matches the unique-site count at award time.
- [ ] With two players, each player maintains independent unique-site progress and rewards.
- [ ] Restart server between discoveries and verify saved progression survives.
- [ ] Stand near an older Quantum Relay Station / Android Command Bunker and confirm it does not trigger Frontier Expedition discovery.

## Facility threat clearance

Use actual hostile mobs or structure-associated Android/drone hostiles and make sure the killing blow belongs to the test player.

- [ ] Deep Matter Vault: first three qualifying hostile kills show action-bar progress; fourth completes the 4-kill target.
- [ ] Autonomous Drone Foundry: completion occurs exactly on hostile kill 6.
- [ ] Anomaly Quarantine Site: completion occurs exactly on hostile kill 5.
- [ ] Orbital Recovery Array: completion occurs exactly on hostile kill 4.
- [ ] Completion awards 60 XP and one `facility_research` dossier with `FrontierSecurityArchive` and `Secured=true`.
- [ ] Additional kills in the same secured structure do not award another completion reward.
- [ ] A hostile killed immediately outside the structure piece does not advance the counter.
- [ ] Passive/creature-category deaths do not advance the counter.
- [ ] Hostiles killed by environment, another player, a drone, or non-player source do not credit the wrong player.
- [ ] Save/reload between kills and verify progress persists.
- [ ] Restart after securing a structure and verify it remains secured.
- [ ] Two players can independently secure the same generated facility and receive their own one-time reward.

## Performance / multiplayer

- [ ] Cross chunk boundaries near a frontier site while watching server tick time; structure-manager discovery lookups should not create sustained spikes.
- [ ] Fly rapidly across unrelated chunks; no repeated chat/reward spam and no obvious continuous scan cost.
- [ ] Two players entering separate frontier sites simultaneously do not interfere with one another's ledger.
- [ ] Repeated unrelated hostile deaths outside Frontier structures do not create noticeable server load.
- [ ] No chunk is force-loaded by generation, discovery or clearance checks.

## Failure evidence to capture

For any failure record: world seed, exact coordinates, structure ID, layout appearance, whether the room is damaged/occupied, screenshots from entrance/core/top-down where useful, and the relevant `latest.log`/`debug.log` excerpt.
