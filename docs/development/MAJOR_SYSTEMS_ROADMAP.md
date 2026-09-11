# Matter Overdrive 1.20.1 Major Systems Roadmap

This roadmap tracks player-visible expansion work by maturity. Runtime verification is recorded separately from source/build evidence.

## Drone Roles Alpha — Functional, runtime verification required

### What the player can now do

- Craft a Configurable Drone Core.
- Sneak-right-click the core to select Combat, Repair, Logistics, Survey, or Reactor Maintenance.
- Right-click to deploy a persistent drone already linked to the player.
- Reprogram an owned deployed drone by interacting with it while holding another core.
- Command individual drones or the whole loaded fleet through the existing Drone Management console.
- Read each drone's role, mode, health, energy, and distance in the console.
- Order one drone or the nearest 24-drone fleet to PATROL; each drone records its current position as a persistent guard anchor.
- Read a patrolling drone's anchor coordinates directly in the management console.
- Use RECALL ALL to return loaded linked drones to collision-safe formation positions and restore FOLLOW mode.

### Role behavior

- **Combat:** spends internal energy on ranged attacks.
- **Repair:** periodically repairs the operator and nearby owned drones.
- **Logistics:** transfers nearby dropped items into the owner's inventory without duplicating partial insertions. A configured Logistics Core can instead route the drone to a loaded target inventory, where it gathers nearby drops and inserts them server-side without force-loading chunks.
- **Survey:** periodically marks nearby hostile mobs with Glowing for fleet visibility.
- **Reactor Maintenance:** while near its operator, services a nearby controller with bounded containment pulses (300 drone FE cools 40 heat and restores 30 stability, clearing thermal runaway only below the safe threshold), then falls back to feeding one nearby Gravitational Stabilizer.

### State and multiplayer model

Role, command mode, patrol anchor, owner UUID, and internal energy are stored in entity NBT. Deployment, patrol targeting, role work, and recall placement run on the logical server. Commands validate ownership and operate on the nearest 24 loaded owned drones within the bounded management range. Recall tests several collision-free formation positions around the operator and does not act across dimensions. Status snapshots use the same 24-drone cap.

### Performance contract

Role work uses interval timers and bounded entity searches: 6 blocks for logistics, 8 for repairs, and 18 for survey. Patrol targeting is confined to a 16-block envelope. Charging Stations cache at most 32 nearby linked drones and refresh the bounded search once per second; per-tick service is round-robin and capped at 4,096 FE aggregate.

### Test checklist

1. Craft or obtain `matteroverdrive:drone_deployment_core`.
2. Sneak-right-click through all five role labels and inspect the tooltip.
3. Deploy one drone of each role and verify ownership/custom names.
4. Damage the player and a linked drone; verify Repair consumes FE and heals both.
5. Drop mixed item stacks near Logistics with a partially full inventory; verify transfer without duplication. Then configure a Logistics Core by sneak-using a target inventory, deploy/reprogram a Logistics drone, select LOGISTICS and verify it flies to that loaded target and inserts nearby drops without duplication.
6. Place a hostile mob near Survey; verify it receives Glowing and drone FE decreases.
7. Put Combat in Defensive/Aggressive mode; verify ranged attacks consume FE.
8. Press the drone-management key and verify role, mode, HP, FE, and distance.
9. Set PATROL and verify the drone orbits its recorded anchor, engages only hostiles inside 16 blocks, and reports the anchor coordinates.
10. Move the owner away; verify Combat guards the anchor, Repair supports nearby owned drones, Survey marks local hostiles, and Logistics waits for its operator inventory.
11. Put multiple drones on PATROL/HOLD, press RECALL ALL, and verify each returns to a clear position near the operator in FOLLOW mode without clipping into blocks or another drone.
12. Save/reload and restart the server; verify owner, role, mode, patrol anchor, health, and FE persist.
13. With two players, verify neither can command, recall, or reprogram the other's drones.

### Known limitations / next integration

- Alpha roles share the existing drone model and texture in-game. `tools/blockbench/drone_commander_alpha.bbmodel` is the multi-part Blockbench source for the next renderer/model pass; it needs visual and in-game verification before replacing the current source-backed geometry.
- Internal energy still trickle-recovers slowly, while a powered Charging Station provides the practical fleet recharge path and a bounded maintenance bay: it repairs up to two loaded nearby drones per second for 400 FE per health. The dedicated Drone Fabricator is functional in source/build validation and awaits interactive runtime verification.
- Logistics can route to a configured loaded inventory target and now pulls one arbitrary stack per bounded work poll from the connected channel-0 item network before collecting local drops. The Drone Fabricator pulls its three recipe ingredients from a selected router channel with a bounded per-tick budget; broader filter/priority routing remains planned.
- Runtime testing is still required before this slice advances to Integrated.

## Planned major slices

- Drone Fabricator role modules and further fabrication recipes; the initial powered Fabricator now assembles configured Drone Cores, supports bounded adjacent-inventory ingredient supply, and exposes persisted Speed/Power upgrade choices that alter cycle time versus FE/t.
- Research projects that unlock drone roles from facility data.
- Reactor Maintenance role and emergency containment commands. **Functional:** maintenance drones now provide controller-level containment pulses with safe runaway recovery, plus stabilizer-feed fallback; operators can use bounded `reactor scram`/`reactor reset` controls; runtime stress/persistence checks remain.
- Matter-network Logistics routing and configurable inventory targets.
- Structure encounters and rewards tied to drone manufacturing facilities.

## Verification record — 2026-09-11

### Server authority and persistence hardening — Statically verified

- Privileged debug buttons in Charging Station, Fusion Reactor, Spacetime Accelerator, and Transporter menus now require level-2 operator permission.
- Transporter routing state is bounded to 64 destinations with 64-character names at import and load time.
- Technology-site and facility saved-data loaders cap entry counts and reject oversized keys/invalid UUID entries.
- gradlew.bat compileJava --no-daemon passed after the hardening changes.
- Interactive multiplayer authorization and malformed-save recovery remain **Needs runtime verification**.

### Fusion reactor operating profiles — Functional, runtime verification required

- Added persisted Balanced, Overdrive, and Conservation profiles.
- Overdrive increases output and matter demand; Conservation reduces both for longer-running builds.
- Operating profiles now feed a bounded heat/stability loop: stabilizers cool and recover containment, overheating forces a thermal-runaway shutdown, and scrammed reactors cool before recovery.
- Player verification steps are documented in `docs/testing/REACTOR_OPERATING_PROFILE_TEST_PLAN.md`.
- The profile is server-authoritative, synchronized through the reactor menu, and selectable through the new PROFILE control.
- Static validator and Forge compilation pass; in-world profile switching and balance remain **Needs runtime verification**.

### Research and structure-chain integration — Functional, runtime verification required

- Field discovery detects bounded machine pairings, records per-player site evidence, and issues a Data Pad plus a typed research dossier.
- The relay outpost → matter observatory → anomaly research site chain advances deterministically and awards Parallel Processing once, with duplicate discovery/reward guards.
- Powered Matter Analyzer dossier analysis advances shared research clearance and grants the structure-specific progression item server-side.
- A dedicated validator now checks stage integration, chain ordering, Data Pad state, persistence, and duplicate-reward guards in the full sanity pass.
- Player verification steps are documented in `docs/testing/RESEARCH_STRUCTURE_CHAIN_TEST_PLAN.md`.
- Fresh-world traversal, dossier analysis, save/reload, and two-player reward isolation remain **Needs runtime verification**.

- **Build:** `tools/full-sanity-check.ps1` passed static validation, clean Forge compilation, JAR packaging, and deployed `matteroverdrive-0.6.jar`.
- **Runtime startup:** dedicated Forge 1.20.1 client reached the Matter Overdrive registry and M2 machine initialization markers without Matter Overdrive load, texture, ticking, or data-pack failures.
- **Security-hardening startup:** rebuilt JAR reached the same M1/M2 markers after the expanded operator gates and persistence bounds; no Matter Overdrive load or ticking errors were observed. The dedicated client was then closed cleanly.
- **Latest deployment:** `C:\Users\novel\AppData\Roaming\.minecraft-matteroverdrive-test\mods\matteroverdrive-0.6.jar` SHA-256 `4289C5FE1AB6835254800F72A71BE1C263E13306A490533A20318597E348BBA8`.
- **Reactor profile deployment:** the subsequent profile build was deployed to the same target with SHA-256 `526E89526FEF913EF2FC1C143D5A7762743F802839FE9580D50F6AA0B351FDBF`; fresh startup reached M1/M2 markers and was closed cleanly. Profile switching still needs interactive in-world verification.
- **Final profile-sync deployment:** after correcting the menu/block-entity data-count contract (35 entries on both sides), the JAR was rebuilt and deployed with SHA-256 `0F9FFC22FF6538F44C02F54B6C5E759AB60225E995DAE99A9ADA90298A248A8C`; a fresh startup again reached M1/M2 markers and was closed cleanly.
- **Deployment:** the current successful JAR was copied to the dedicated test profile; source/build evidence remains separate from interactive runtime verification.
- **Runtime environment note:** the profile's legacy `run/saves/ore test/level.dat` is unreadable (`Invalid tag id: 97`) and has no `level.dat_old` backup. Startup verification used the separate fresh `test2` world; the damaged save was not modified.
- **Drone Roles Alpha maturity:** source/static evidence is **Functional**; interactive role, persistence, and two-player ownership testing remains **Needs runtime verification**.
- **Charging integration:** powered Charging Stations now recharge nearby owned drones on the logical server, respecting bounded range, redstone work mode, available FE, and upgraded transfer limits; this is **Partial** pending interactive verification.
- **Patrol integration:** PATROL orders, persistent anchors, guard-envelope targeting, status coordinates, HOLD support work, and capped nearest-fleet commands compile successfully; this remains **Needs runtime verification**.
- **Reactor containment telemetry:** Facility Network Controllers, diagnostic probes, and Holographic Status Panels now consume the same bounded reactor heat/stability snapshot, surface threshold alarms, and show a compact overview readout. Static wiring and startup are verified; loaded multi-reactor threshold fan-out remains **Needs runtime verification**.
- **Latest telemetry deployment:** rebuilt and copied to `C:\Users\novel\AppData\Roaming\.minecraft-matteroverdrive-test\mods\matteroverdrive-0.6.jar` with SHA-256 `7AFBB6603436DE25523C875A3DD9A18054759CCA9D5D4C4251EE9AE54FFC0413`; clean Forge client startup reached M1/M2 markers and was closed cleanly.
- **Matter Network Terminal:** added a craftable remote matter access point with bounded 1,000-Matter push/pull requests, aggregate status/alarm feedback, comparator output, side-policy enforcement, and a network guide entry. Player verification is documented in `docs/testing/MATTER_NETWORK_TERMINAL_TEST_PLAN.md`; static validation and deployment pass, while in-world transfer and multiplayer isolation remain **Needs runtime verification**.
- **Drone Fabricator network supply:** the Fabricator now pulls only missing recipe ingredients from a selected router channel with a 16-item/tick budget, while retaining its adjacent-inventory fallback and completion-time consumption guarantee. Static validation and startup pass; network-fed fabrication remains **Needs runtime verification**.
- **Latest terminal/fabricator deployment:** rebuilt and copied to the same target with SHA-256 `387614FE67C15ADE225BA5F99DF5FE09DDC961958A6A64CFF00206F8C418CA38`; clean Forge startup reached M1/M2 markers and was closed cleanly.
- **Logistics network delivery:** configured Logistics drones now use the target inventory as bounded channel-0 network ingress, persisting a cursor and honoring item side policies before collecting local drops. Static validation and startup remain required before promoting this to Integrated.
