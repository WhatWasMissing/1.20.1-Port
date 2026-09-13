# Matter Overdrive 1.20.1 - Current Project Direction

## NON-NEGOTIABLE: STAR MAP IS RETIRED

The Star Map system is permanently retired from this port.

Do **not** add, restore, repair, expand, rebalance, polish, port, or otherwise develop Star Map functionality in future passes. Do not list it as a roadmap item, parity gap, regression target, or candidate feature. Remove remaining active item/block, GUI/backend, packet, strategic-simulation, GuideMe and gameplay references when encountered. Other systems must not gain new Star Map dependencies.

Older handoffs that discuss Star Map development are superseded by this file.

## Branch status

`main` is the stable 0.7 development/release line and contains the reviewed source from **`testing/tech-overhaul`**. The tech-overhaul branch remains available for historical comparison, but new changes should land on `main` after the static gates pass. `BUILD_LOCAL.bat` runs `scripts/validate_tech_overhaul.py` before Gradle when that script is present.

No GitHub Actions build is required for this branch. Build and test locally.

## Active development direction

Continue work on the rest of Matter Overdrive: Android progression and Drone Commander, quests/NPCs, matter/network infrastructure, machines and configuration, fusion reactor/anomalies, weapons, GuideMe, transporter/security, dimensional pylon, visual parity and stability. Keep the 0.7 structure policy: MO-authored structures remain dormant until a dedicated visual/content review; natural anomalies, vanilla loot, village contacts and player-event lore stay active.

## Android Chassis and Station

Androids have a persistent physical hardware layer with five slots: Core, Frame, Muscles, Optics and Shell. Current modules include capacitor/overclock cores, lightweight/reinforced frames, agility/siege myomers, hunter/precision optics and stealth/reactive shells.

The Android Station has already been overhauled into the central equipment workbench: body parts and chassis modules can be installed, removed and swapped from the station interface, displaced hardware is returned to the player, and progression/Class Matrix access is available from the same screen. Do not regress this back to right-click-only hardware installation.

On `main`/`testing/tech-overhaul`, the Capacitor Core's additional 50,000 FE is real player capacity and Android charging respects chassis-adjusted capacity. The branch also adds the Android Induction Relay for same-dimension wireless charging at configurable 32/64/96-block ranges.

## Facility and Matter Network overhaul

The Facility Network Controller is the central telemetry foundation. The testing branch extends the surrounding infrastructure with:

- Router/Switch channels 0-15 and deterministic Router priority;
- persistent per-face FE, Matter and Item modes (INPUT / OUTPUT / BOTH / DISABLED);
- Network Diagnostic Probe configuration/inspection;
- aggregate facility alarms and comparator output;
- Holographic Status Panel views;
- Grid Capacitor high-throughput FE buffering;
- Energy Bank dual FE/Matter buffering from Reactor IO;
- Quantum Power Relay loaded-peer wireless FE links;
- Matter Storage Matrix and removable 64k/256k/1M/4M cells;
- Matter Excavator bulk block-to-Matter processing;
- Parallel Processing upgrades for Decomposer and Replicator.

These systems should interoperate with existing Forge capabilities and Matter Overdrive networks rather than becoming isolated duplicate resource systems.

## Anomaly Containment Unit

The Anomaly Containment Unit captures a Gravitational Anomaly, removes it from the world, stores its accumulated mass in item NBT and redeploys the same mass elsewhere. This exists specifically so players do not need to build permanent bases around naturally generated anomalies. Keep this behavior available and preserve anomaly mass across relocation.

## Testing priorities

1. Run `BUILD_LOCAL.bat` on `main`; resolve the static packaging gate and Java/resource compile issues before runtime testing.
2. Follow `docs/testing/TECH_OVERHAUL_TEST_PLAN.md` and the Energy Bank checks in `docs/testing/TO_TEST.md` for Grid Capacitor, Induction Relay, Quantum Relay, Energy Bank, Matter Matrix/Cells, Excavator, channels/priority, per-side configuration, parallel processing and facility telemetry.
3. Re-test Reactor IO -> chained Heavy Energy Cable -> machine, Matter Pipe routing and Pattern Monitor -> Replicator queues after the routing changes.
4. Verify Android Station/chassis equipment and chassis-adjusted charging remain persistent across relog/respawn.
5. Verify no Star Map item/system is reintroduced while implementing or testing any of the above.

## Lore and structure policy

All 16 primary structure archive records and all ambient fragments have player-event routes. Technology acquisition, mapped block mining, hostile encounters, NPC assignments, Field Operations and recovered-fragment inspection authenticate the PDA archive without requiring a generated MO structure. Native structure-manager checks are compatibility fallbacks for old generated saves only.
