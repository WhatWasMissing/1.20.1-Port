# Drone Commander Patrol and Charging Runtime Test Plan

Status: source compilation and static contract checks are available; the gameplay checks below are not complete until recorded against the dedicated Matter Overdrive test profile.

## Player-visible result

An operator can deploy differentiated drones, open the fleet console with `M`, and assign `PATROL` to one drone or the nearest 24 linked drones. Each patrolling drone guards a persistent 16-block envelope around the position where it received the order. Powered Charging Stations service a nearby fleet using cached discovery and round-robin FE delivery.

## Fast setup

1. Start the dedicated Forge 1.20.1 Matter Overdrive profile with the newly built JAR.
2. Enter a disposable creative test world.
3. Run `/give @s matteroverdrive:drone_deployment_core 8`.
4. Sneak-right-click a core to select a role; normal right-click deploys it.
5. Deploy Combat, Repair, Logistics, and Survey drones, then press `M`.

## Patrol behavior

1. Position a Combat drone at an obvious marker and choose `PATROL` for that row.
2. Confirm the console reports `PATROL` and the marker's anchor coordinates.
3. Walk at least 30 blocks away. The drone should remain around its anchor instead of following.
4. Spawn one hostile inside the 16-block guard envelope and another clearly outside it.
5. Confirm the Combat drone engages the inside hostile and does not pursue the outside hostile beyond the envelope.
6. Cycle the drone to `FOLLOW`; confirm it abandons the anchor and rejoins the operator.
7. Reissue `PATROL` at a new marker; confirm the displayed anchor changes.

## Stationary support roles

1. Put Repair and Survey drones on `PATROL` near the first anchor.
2. Damage an owned drone. Confirm Repair restores it while the operator is farther than 24 blocks away and consumes drone FE.
3. Spawn a hostile near Survey. Confirm Glowing is applied and Survey FE decreases.
4. Put a Logistics drone on `PATROL`, move away, and drop items near it. Confirm it does not remotely insert into the distant operator inventory.
5. Return within 24 blocks. Confirm Logistics can then collect into the operator inventory without duplication.
6. Set a support drone to `HOLD`; confirm it stays stationary but still trickle-charges and performs its local role.

## Persistence

1. Record the PATROL anchor shown in the `M` console and the drone's FE value.
2. Save and quit to title, reload the world, and reopen the console.
3. Confirm owner, role, PATROL mode, anchor, health, and FE survived the reload.
4. Fully stop and restart the dedicated profile, reload once more, and repeat the check.

## Charging Station integration

1. Supply a Charging Station from an existing FE source and set its redstone mode to operate.
2. Drain several linked drones through role work, then move them inside the station range.
3. Confirm the Android page reports both drone count and aggregate drone FE/t, and drone FE rises in the `M` console.
4. Place more than one drained drone in range. Confirm service rotates rather than permanently starving later drones.
5. Disable the station with its redstone mode. Confirm drone transfer telemetry becomes zero and FE stops rising except for the drone's slow autonomous recovery.
6. Re-enable it and confirm charging resumes within one second, matching the cache refresh interval.
7. Move a cached drone outside range. Confirm it receives no station FE even before the next cache refresh.
8. Confirm aggregate station-to-drone transfer never exceeds 4,096 FE in one tick.

## Drone Fabricator integration

1. Craft or give `/give @s matteroverdrive:drone_fabricator`, place it, and connect a known FE source.
2. Insert one Plasma Core, two Isolinear Circuit Mk2s, and four Tritanium Plates into the three left input slots.
3. Press `ROLE` until the desired role is displayed, then confirm a 240-tick assembly starts only while the selected redstone mode permits work.
4. Interrupt FE partway through; confirm progress and inputs remain intact, then resume FE and confirm production continues.
5. On completion, confirm the output is exactly one `drone_deployment_core`, its tooltip shows the selected role, and the input counts fell only then.
6. Remove the completed core, cycle to a different role, and repeat. Save/reload during a partial cycle and confirm progress, selection, completed count, and stored FE persist.
7. Place a chest or other item-handler inventory adjacent to the Fabricator and stock the three recipe ingredients. Leave its input slots empty and confirm the machine pulls only the missing recipe quantities (1/2/4), at no more than 16 items per tick, without creating or force-loading chunks; verify unrelated items stay put.
8. Begin with a fresh player and confirm the core cycles only through the currently cleared role. Analyze facility dossiers through the Matter Analyzer until the relevant research stage is reached; confirm Repair, Logistics/Survey, and Reactor roles unlock in sequence and that an old/traded locked core is refused at deployment and reprogramming.
9. Insert one Speed and one Power upgrade in the two Fabricator upgrade slots. Confirm Speed shortens the 240-tick cycle, Power increases FE/t, both effects persist across save/reload, and the output/ingredient accounting remains exact.

## Multiplayer authority

1. Join with two players and give each player one linked drone.
2. Confirm each console lists only the operator's drones.
3. Confirm one player cannot reprogram, release, or command the other player's drone.
4. Issue `PATROL ALL`; confirm it affects at most the issuing player's nearest 24 loaded drones.

## Evidence to record

Runtime evidence must name the world used. Do not treat the legacy `ore test` save as valid until its `level.dat` is restored from an external backup; use a fresh test world for Fabricator and persistence checks.

- Dedicated-profile `latest.log` covering startup, world load, and clean shutdown.
- Screenshots of PATROL status/anchor, a guarded encounter, and Charging Station drone telemetry.
- Save/reload result and two-player ownership result.
- Any deviation, including exact reproduction steps and whether it is code, balance, or presentation related.
