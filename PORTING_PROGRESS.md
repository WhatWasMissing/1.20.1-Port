# Matter Overdrive 1.20.1 Port Progress

This file is the durable hand-off record for continuing the port in a new ChatGPT conversation.

## Working rules

- Update this file in every commit made during assisted porting work.
- Record what changed, what was verified, known problems, and the exact next step.
- Do not mark runtime behavior as verified until it has been tested in Minecraft.
- Add a temporary, clearly labelled debug UI readout for new or changed runtime mechanics so their effective server-side values can be tested directly; remove or convert these only during later UI polish.
- Target: Minecraft 1.20.1, Forge 47.4.10, Java 17.
- Current port version: `0.8.0.0-alpha.4.1`.

## Current repository state

- Active work branch: `feature/machine-upgrades`
- Base branch: `main`
- Base commit: `701be7bfd7bfdd4e6e4aec77fe908ed735c6a866`
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

## Current work: machine upgrades

Branch `feature/machine-upgrades` starts from `701be7b`.

Initial scope:

- inspect registered legacy upgrade items and archived upgrade formulas;
- create a reusable upgrade inventory/effect model;
- implement the smallest useful set first: Speed, Power, Power Storage, Matter Storage, and Fail-Safe;
- integrate upgrades into one machine before applying the model across Decomposer, Recycler, Analyzer, Replicator, and legacy-compatible network machines;
- persist upgrade inventories and synchronize machine GUI data;
- preserve current unupgraded constants and behaviour;
- add verification checks and record runtime tests before merging.

Exact next step: map the archived upgrade API and current item registrations, then select the first machine integration.

### 2026-08-27 — Start machine-upgrade milestone

- Fast-forwarded the completed Router/Switch investigation into `main` at `701be7b`.
- Created `feature/machine-upgrades`.
- Chose machine upgrades as the next missing system because upgrade items are registered but currently have no machine effect.

### 2026-08-27 — Restore typed legacy upgrade items

- Added a shared `MachineUpgradeItem` model for all seven registered upgrade items.
- Restored the original legacy multipliers for Speed, Power, Fail-Safe, Range, Power Storage, Hyper Speed, and Matter Storage.
- Added tooltips that expose every non-neutral multiplier.
- Updated `ModItems` so upgrade IDs create typed upgrade items instead of inert placeholders.
- No machine consumes these effects yet.
- Next step: compile this isolated foundation, then add four upgrade slots to the Decomposer and apply supported multipliers.

### 2026-08-27 — Add upgrade-ready dynamic storage capacity

- Confirmed commit `4f5d7d1` compiles successfully.
- Added runtime capacity setters to `MachineEnergyStorage` and `MachineMatterStorage`.
- Capacity changes clamp existing contents safely, preventing upgraded storage from retaining impossible values when an upgrade is removed.
- No machine capacity is changed yet.
- Next step: compile the storage foundation, then integrate the Decomposer upgrade inventory and effects.

### 2026-08-27 — Fix runtime gate and stale Matter Container block tag

- Runtime regression test passed: all seven upgrade tooltips appeared, the Decomposer operated normally, and default capacities remained 512000 FE and 1024 kM.
- Logs confirmed the M1 marker exists with live counts blocks=74, blockItems=71, standaloneItems=97, sounds=57.
- These live counts intentionally differ from the historical M1 inventory counts after the old Matter Container block and two-state item placeholders were replaced by one functional standalone item.
- Removed stale `matteroverdrive:matter_container` from the pickaxe block tag; it caused a real data-pack load error because Matter Container is no longer a block.
- Reworked `CHECK_M2_RUNTIME.bat` into explicit success/failure labels so a failure cannot be followed by a contradictory pass banner.
- Added detection for Matter Overdrive data-pack tag failures.
- Next step: rerun the development client and confirm the corrected runtime gate passes, then integrate Decomposer upgrade slots.

### 2026-08-27 — Integrate Decomposer upgrade slots and effects

- Confirmed the corrected runtime checker passes all gates and the stale Matter Container tag error is gone.
- Added a reusable four-slot `MachineUpgradeInventory` with one upgrade per slot.
- Added four visible upgrade slots to the Decomposer GUI and adjusted the player inventory layout.
- Added shift-click routing for supported upgrades.
- Decomposer supports Speed, Power, Fail-Safe, Power Storage, Matter Storage, and Hyper Speed upgrades; Range is rejected.
- Applied legacy multipliers to processing speed, energy usage, failure chance, energy capacity, and matter capacity.
- Persisted upgrade inventory contents in block-entity NBT.
- Removing storage upgrades clamps stored contents to the resulting capacity.
- Runtime behavior remains unverified until the development client test passes.

### 2026-08-27 — Correct legacy Decomposer failure multiplier

- Runtime testing confirmed all Decomposer upgrade slots and supported effects work.
- Clarified that a Decomposer failure consumes the input and creates Matter Dust instead of storing matter.
- Base failure chance remains the legacy 0.5 percent, making failures uncommon in short tests.
- Corrected Fail-Safe interaction to match legacy: the combined failure multiplier is squared before applying it to the base chance.
- One Fail-Safe Upgrade therefore reduces 0.5 percent to 0.125 percent.

### 2026-08-27 — Show effective Decomposer failure chance

- Added a synchronized debug failure-chance value to the Decomposer container data.
- The server sends the effective probability as parts per million split across two data words, avoiding Forge menu short truncation.
- Added `[DEBUG] Failure: %.4f%` to the Decomposer GUI.
- Expanded the GUI vertically and moved upgrade/player slots to prevent overlap.
- Expected values: 0.5000 percent with no upgrade and 0.1250 percent with one Fail-Safe Upgrade.
- Next step: compile and visually verify the values and revised layout.

### 2026-08-27 — Integrate Matter Recycler upgrades

- Confirmed the Decomposer failure debug display and all expected upgrade values work in the development client.
- Added four visible, persistent upgrade slots to the Matter Recycler.
- Recycler accepts Speed, Power, Power Storage, and Hyper Speed; it rejects Fail-Safe, Range, and Matter Storage, matching legacy supported effect types.
- Applied upgrade multipliers to processing speed, energy usage, and energy capacity.
- Added shift-click upgrade routing and adjusted the Recycler GUI layout.
- Fixed Decomposer and Recycler block breaking so installed upgrades drop instead of being lost.
- Runtime behavior remains unverified until the development client test passes.

### 2026-08-27 — Integrate Matter Analyzer upgrades

- Confirmed the Matter Recycler, upgrade effects, persistence, shift-clicking, block drops, and runtime gate all work.
- Added four visible, persistent upgrade slots to the Matter Analyzer.
- Analyzer accepts Speed, Power, Power Storage, and Hyper Speed; it rejects Fail-Safe, Range, and Matter Storage, matching legacy supported effect types.
- Applied upgrade multipliers to analysis duration, total energy usage, and energy capacity.
- Added shift-click upgrade routing and adjusted the Analyzer GUI layout.
- Installed Analyzer upgrades drop when the block is broken.
- Existing direct Pattern Drive and networked Pattern Storage analysis paths remain available.
- Runtime behavior remains unverified until the development client test passes.

### 2026-08-27 — Add Analyzer debug cycle readout

- Added a standing workflow rule requiring visible debug readouts for newly changed runtime mechanics.
- Added the Analyzer's synchronized effective cycle duration in ticks and seconds.
- Existing Analyzer readouts already show effective FE per tick, energy capacity, input matter, and pattern progress.
- Expanded the Analyzer layout and moved upgrade/player slots to prevent overlap.
- Expected cycle values: 800 ticks / 40.00 seconds with no upgrade, 600 / 30.00 with Speed, 1200 / 60.00 with Power, and 120 / 6.00 with Hyper Speed.
- Runtime layout and values remain unverified until the development client test passes.
