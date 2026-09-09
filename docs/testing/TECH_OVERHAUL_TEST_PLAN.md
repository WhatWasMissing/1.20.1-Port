# Matter Overdrive 0.6 - Tech Overhaul Testing Plan

Branch: `testing/tech-overhaul`

This branch is intentionally isolated from `main` until the systems below pass local build and runtime testing. Do not merge based only on source review.

## Build gate

- Run `BUILD_LOCAL.bat`.
- Confirm `verifyM2Sources` and resource parsing pass.
- Confirm no missing model/blockstate/recipe/loot-table errors appear in `latest.log`.

## Grid Capacitor

- Accept FE from each face.
- Supply FE to adjacent consumers without duplication/loss.
- Confirm 16,000,000 FE capacity and 32,768 FE/t throughput.
- Comparator tracks fill level from 0-15.

## Android Induction Relay

- Converted Android charges at 32, 64 and 96 block modes.
- Non-Android players are ignored.
- Players outside range and in other dimensions are ignored.
- Capacitor Core Androids can charge above 100,000 FE to their chassis-adjusted capacity.
- Multiple Androids receive fair transfer without FE duplication.

## Quantum Power Relay

- Two loaded relays on the same channel transfer FE within 256 blocks.
- Different channels do not cross-connect.
- Unloaded destination does not force-load.
- Changing channel immediately updates the loaded-relay registry.
- Break/re-place/reload does not leave ghost peers.
- Large worlds with many relays do not show full-volume scan stalls.

## Matter Storage Matrix

- All four cell tiers install/remove correctly.
- Capacity is the sum of installed cells.
- Matrix accepts/extracts Matter through Matter Transport Pipe.
- Removing a cell is rejected when stored Matter would exceed remaining capacity.
- Reload preserves cells and Matter.
- Breaking the matrix does not silently delete installed cells or stored Matter.

## Matter Excavator

- Configure target block and 8/16/24 radius modes.
- Only selected target blocks are removed.
- Matter gain matches resolved Matter values.
- FE is consumed for successful work and machine stops cleanly when power/storage is insufficient.
- Redstone pause/state survives reload.
- Range visualization matches actual search volume.
- Protected/unbreakable blocks are never removed.

## Matter Network 2.0

- Router/Switch channels 0-15 persist.
- Highest-priority powered Router becomes executor; tie-break is deterministic.
- Destination filter and Network Flash Drive behavior still work.
- Disabled Switch still isolates its section.
- Routing history/status telemetry remains accurate.

## Per-side configuration

- FE INPUT/OUTPUT/BOTH/DISABLED policies affect the exact touched side.
- Matter policies affect real Matter Pipe movement.
- Item policies affect automation insertion/extraction.
- Save/reload preserves all six faces and all resource types.
- Diagnostic Probe reports the same state the backend obeys.

## Parallel processing

- Decomposer and Replicator accept Parallel Processing Upgrades.
- Multiple lanes increase completed work when resources are available.
- FE demand increases with active lanes.
- Failure rolls/output slots remain consistent per operation.
- Network-queued Replicator work acknowledges every completed result correctly.

## Facility telemetry and Holographic Status Panel

- Controller reports connected nodes, aggregate FE, aggregate Matter and alarms.
- Disconnected machines are absent.
- Status Panel cycles Overview/Energy/Matter/Alarms.
- Panel updates from live network state and comparator alarm output changes appropriately.

## Regression checks

- Reactor IO -> chained Heavy Energy Cable -> machine still transfers FE.
- Matter Pipes still transfer between existing machines.
- Pattern Monitor -> Replicator network queue still works.
- Android Station and chassis equipment remain intact.
- Existing Network Switch GUI still opens and reflects backend state.
- No Star Map item/system is restored by this branch.
