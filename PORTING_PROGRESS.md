# Matter Overdrive 1.20.1 Port Progress

This file is the durable hand-off record for continuing the port in a new ChatGPT conversation.

## Working rules

- Update this file in every commit made during assisted porting work.
- Record what changed, what was verified, known problems, and the exact next step.
- Do not mark runtime behavior as verified until it has been tested in Minecraft.
- Target: Minecraft 1.20.1, Forge 47.4.10, Java 17.
- Current port version: `0.8.0.0-alpha.4.1`.

## Current repository state

- Active work branch: `feature/network-routing-controls`
- Base branch: `main`
- Base commit: `4af59cea15dd6db7c088fa6c5b84130bb8f67666`
- Latest code commit before this checkpoint: `b71ddc9d544a4b6bad7c25e74853ee0094c3a0e4`
- Branch status before this checkpoint: 3 commits ahead of `main`, 0 behind.
- Files changed relative to `main`:
  - `src/main/java/matteroverdrive/blockentity/DecomposerBlockEntity.java`
  - `src/main/java/matteroverdrive/network/MatterNetworkUtil.java`

## Verified milestones

### M1

Verified on a Forge client and dedicated server:

- 75 registered block IDs
- 72 block items
- 98 standalone items
- 57 sound events
- 1.20.1-safe models and resources
- client placement/resource reload
- dedicated-server startup

### M2 alpha foundation

The last recorded verification completed successfully with Java 17 and Gradle 8.1.1. The runtime checks preserved the M1 registry counts and detected no Matter Overdrive missing-texture model warnings or runtime failure marker.

Implemented foundation:

- Matter Decomposer
- Matter Recycler
- Matter Analyzer
- Matter Replicator
- six-slot Pattern Storage
- two patterns per normal Pattern Drive
- Pattern Monitor with an eight-request queue
- Network Pipe pattern/task discovery
- Matter Pipe and Heavy Matter Pipe matter transport
- persistent machine inventory, FE, matter, pattern data, and replication tasks
- direct Pattern Drive fallback
- legacy Matter Container item and machine transfers
- Pattern Monitor request completion acknowledgement

## Current work: matter-pipe routing

Branch `feature/matter-pipe-routing` replaces decomposer-specific routing logic with generic matter-network routing.

Latest known code commit:

- `b71ddc9` — Use generic matter routing from decomposer

The branch currently modifies `DecomposerBlockEntity` and `MatterNetworkUtil`. It has not yet been merged into `main`.

## Verification still required

- `VERIFY_M2_BUILD.bat` passed completely on Windows 10 with JDK 17.0.20.1 and Gradle 8.1.1 on 2026-08-27.
- M1 resources, M2 sources, and clean Forge compilation all passed.
- Generated JAR: `build/libs/matteroverdrive-0.8.0.0-alpha.4.1.jar`.
- Three non-blocking deprecation warnings remain in `ClientModEvents.java`, `MatterOverdrive.java`, and `ModSounds.java`.
- Runtime routing tests passed for direct transfer, Matter Pipe, Heavy Matter Pipe, mixed pipes, turns, junctions, two destinations, full destinations, and broken/reconnected routes.
- No matter loss, duplication, routing lockup, or crash was observed.
- Remaining behaviour note: a routing attempt transfers the full available amount immediately rather than visibly moving matter in smaller batches.
- Recheck client and dedicated-server startup.
- Remove temporary chat/log diagnostics only after the gameplay path is confirmed.

## Exact resume steps

From the repository directory:

```cmd
git fetch origin
git switch feature/matter-pipe-routing
git pull
git status
VERIFY_M2_BUILD.bat
```

If the verification script succeeds, perform the in-game matter-routing tests above. Record the complete build result and gameplay outcome in this file before committing any fix or merge.

## Commit log

### 2026-08-27 — Add durable port progress checkpoint

- Added this hand-off file.
- Captured the verified M1/M2 state, active branch, current routing work, test requirements, and resume commands.

### 2026-08-27 — Verification failure: stale routing source gate

- `VERIFY_M2_BUILD.bat` completed Java compilation successfully.
- Step 3, `:verifyM2Sources`, failed because the gate still searched for the old decomposer call `findMatterTargets`.
- Updated the gate to require the new generic `MatterNetworkUtil.transferMatter` call.
- Runtime routing remains unverified until the complete script and in-game checks pass.

### 2026-08-27 — M2 routing branch build verified

- Pulled source-gate fix `7ebb8e4`.
- `VERIFY_M2_BUILD.bat` passed all four stages.
- M1 resource counts remained blocks=75, blockItems=72, standaloneItems=98, sounds=57, activeJson=404.
- M2 functional source verification passed.
- Clean Forge compilation passed with 10 tasks executed.
- Output JAR: `build/libs/matteroverdrive-0.8.0.0-alpha.4.1.jar`.
- Next step: install this JAR and perform the matter-routing runtime test matrix before merging.

### 2026-08-27 — Matter routing verified in development client

- Launched the source tree with `RUN_M2_CLIENT.bat`.
- Direct Decomposer-to-Replicator transfer passed.
- Normal Matter Pipe, Heavy Matter Pipe, mixed pipes, long routes, and turns passed.
- Two-destination junction routing passed.
- Full-destination handling passed.
- Breaking and reconnecting a route stopped and resumed transfer correctly.
- No loss or duplication was observed.
- Transfer appears instantaneous because the current route operation offers the full stored amount per routing attempt; retain as an accepted alpha behaviour unless a throttled transfer rate is added before merge.

## Current work: network routing controls

Branch `feature/network-routing-controls` starts from merged routing commit `4af59ce`.

Planned scope:

- inspect the registered Network Router and Network Switch placeholders and legacy behavior;
- define a minimal 1.20.1-safe routing-control model;
- allow network paths to be enabled or excluded without breaking existing passive pipes;
- persist and synchronize routing-control state;
- add focused source/build checks and a runtime test matrix;
- keep the already verified matter-routing behavior intact.

Decision: preserve effective legacy behaviour. The current traversal already treats Router and Switch as passive network bridges. Do not add invented redstone or side-filter controls in this milestone.

Exact next step: close this investigation without unnecessary runtime code and begin the machine-upgrade milestone.

### 2026-08-27 — Start network routing controls milestone

- Merged verified generic matter-pipe routing into `main` as `4af59ce`.
- Created `feature/network-routing-controls` from that merge.
- Recorded the next milestone scope before implementation.

### 2026-08-27 — Complete Router/Switch legacy investigation

- Inspected the archived MatterOverdrive Legacy Edition source.
- Legacy `TileEntityMachineNetworkRouter` and `TileEntityMachineNetworkSwitch` both extend the same packet-queue base and accept connections from every side.
- Router used four upgrade slots; Switch used zero. Their completed connection/routing behaviour was otherwise effectively identical.
- Current 1.20.1 `MatterNetworkUtil` already treats Network Pipe, Router, and Switch as traversable network transports.
- Chose legacy fidelity: no invented redstone toggle, side filter, or subnet GUI will be added at this stage.
- No runtime code change was necessary; move next to the missing machine-upgrade system.
