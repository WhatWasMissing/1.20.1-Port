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

### 2026-08-27 — Integrate Replicator upgrades and debug values

- Confirmed Matter Analyzer upgrades, debug timing, direct-drive/network analysis, persistence, block drops, and runtime gate work.
- Added four visible, persistent upgrade slots to the Replicator.
- Replicator accepts Speed, Power, Fail-Safe, Power Storage, Matter Storage, and Hyper Speed; Range is rejected.
- Applied legacy formulas to cycle speed, total energy usage, energy capacity, matter capacity, and pattern-progress-dependent failure chance.
- Added synchronized debug lines for effective cycle ticks/seconds and failure percentage.
- Added shift-click routing, upgrade drops on block break, and a taller non-overlapping GUI.
- Preserved direct Pattern Drive and Pattern Monitor network-task replication paths.
- Runtime behavior remains unverified until the development client test passes.

### 2026-08-27 — Keep Replicator debug section always visible

- Corrected the first Replicator debug implementation, which only rendered while a valid pattern was active.
- Cycle and failure debug lines now remain visible at all times.
- Without a direct pattern or network task, both fields explicitly display `inactive`.
- With an active pattern, the fields display synchronized effective ticks, seconds, and failure percentage.
- Reaffirmed that debug visibility is required for every newly changed runtime mechanic going forward.


### 2026-08-27 — Integrate Pattern Storage upgrades and debug values

- Confirmed the Replicator's always-visible debug values, direct and network replication, upgrade effects, persistence, block drops, GUI layout, and runtime gate all work.
- Added four visible, persistent upgrade slots to Pattern Storage.
- Pattern Storage accepts Power and Power Storage upgrades, matching the legacy machine's declared supported upgrade types; all other upgrades are rejected.
- Power Storage upgrades multiply the 64000 FE capacity and removing them clamps stored energy safely.
- Preserved the legacy quirk that Pattern Storage requires stored energy to stay network-active but consumes no idle FE, so Power upgrades are accepted yet have no operating-cost effect.
- Added always-visible synchronized debug lines for effective capacity and actual idle usage (0 FE/t).
- Added shift-click routing and ensured installed upgrades drop when the block is broken.
- Runtime behavior remains unverified until the development client test passes.
- Exact next step: pull this commit, run `RUN_M2_CLIENT.bat`, and verify Pattern Storage upgrade acceptance, capacity, persistence, drops, layout, network activity, and runtime gate.


### 2026-08-27 — Verify queued replication and close upgrade integration scope

- Pattern Storage rendered its four upgrade slots and always-visible baseline debug values: 64000 FE capacity and 0 FE/t idle use.
- Confirmed that the ordinary Power Upgrade does not change Pattern Storage capacity; this is expected because it affects power usage, while the legacy-compatible storage consumes no idle FE.
- A Pattern Monitor successfully queued three network replication requests.
- The Replicator correctly waited while it had 0 kM despite having full FE and active queued patterns.
- Supplying matter from the Decomposer made the queued requests replicate successfully.
- Clarified the gameplay rule: Network Pipes carry pattern/task discovery, while Matter Pipes or a Matter Container must separately supply replication matter.
- Inspected legacy Pattern Monitor behavior: it constructed four upgrade slots but rejected every upgrade through `isAffectedByUpgrade`; do not add inert upgrade slots to the current monitor.
- Functional machine-upgrade integration scope is complete once the Power Storage-specific Pattern Storage checks pass.
- Exact next step: verify one Power Storage Upgrade shows 128000 FE, test persistence and block drops, close the client for the runtime gate, then run `VERIFY_M2_BUILD.bat` before merging.


### 2026-08-27 — Complete and verify machine-upgrade milestone

- Pattern Storage Power Storage Upgrade runtime checks passed: one upgrade showed 128000 FE capacity, installed upgrades persisted across reload, and installed upgrades dropped when the block was broken.
- The final development-client run completed normally.
- Runtime verification passed live registry counts (74 blocks, 71 block items, 97 standalone items, 57 sounds), the M2 framework marker, missing-texture checks, runtime-failure checks, and data-pack tag checks.
- `VERIFY_M2_BUILD.bat` passed with JDK 17.0.20.1 and Gradle 8.1.1.
- M1 resources, M2 sources, and clean Forge compilation all passed; 10 clean-build tasks executed.
- Three existing non-blocking deprecation warnings remain in `ClientModEvents.java`, `MatterOverdrive.java`, and `ModSounds.java`.
- Verified upgrade integration now covers Decomposer, Matter Recycler, Matter Analyzer, Replicator, and Pattern Storage, including synchronized debug values, persistence, shift-clicking, capacity clamping, and block drops.
- Legacy Pattern Monitor rejects every upgrade, so no inert upgrade UI was added.
- Machine-upgrade milestone is complete and ready to fast-forward into `main`.


## Current work: solar power generation

Branch `feature/solar-power` starts from verified machine-upgrade milestone commit `6e83cc1`.

Planned scope:

- replace the registered `solar_panel` placeholder with a functional block entity;
- preserve legacy base values: 8 FE/t peak generation, 64000 FE storage, and 512 FE/t extraction per adjacent receiver;
- generate only with skylight, a clear view of the sky, and sufficient daytime sky light;
- stop generation at night, under cover, during weather-reduced sky light, and in dimensions without skylight;
- push stored FE into adjacent Forge Energy receivers;
- restore the two legacy upgrade slots, accepting only Power Storage upgrades;
- persist energy and upgrades, clamp capacity safely, and drop installed upgrades when broken;
- add a compact GUI with always-visible synchronized debug values for actual generation, sky eligibility, storage capacity, and output limit;
- extend the M2 source gate and provide a focused runtime test matrix.

Legacy findings:

- `TileEntityMachineSolarPanel` used two upgrade slots and accepted only `PowerStorage`.
- Peak generation was `CHARGE_AMOUNT = 8` FE/t, scaled by the legacy daylight cosine and rounded.
- Storage was 64000 FE.
- The panel attempted to send up to 512 FE/t to each adjacent receiver while energy remained.
- The legacy panel could not receive external FE.

Exact next step: implement the block, block entity, menu, screen, registrations, source checks, and debug synchronization without changing unupgraded constants.


### 2026-08-27 — Implement functional Solar Panel

- Replaced the `solar_panel` placeholder with a ticking block entity and interactive GUI.
- Restored the legacy 8 FE/t peak daylight curve, 64000 FE base storage, and 512 FE/t output limit per adjacent receiver.
- Generation requires a skylight dimension, clear sky above the panel, effective sky light 15, and a legacy daylight factor above 0.5.
- The panel cannot receive external FE and automatically pushes stored FE to Forge Energy receivers on all six sides.
- Added two persistent upgrade slots that accept only Power Storage upgrades.
- Removing an upgrade clamps stored FE to the reduced capacity, and breaking the block drops installed upgrades.
- Added always-visible synchronized debug values for actual generated FE/t, total FE sent last tick, sky visibility, effective sky light, daylight factor, and output limit.
- Registered the Solar Panel block entity, menu, and client screen.
- Expanded the M2 source gate to require the Solar Panel implementation, legacy constants, energy flow, upgrade wiring, and seven block entities/menus.
- Build and runtime behavior remain unverified until the Windows development-client test passes.
- Exact next step: pull `feature/solar-power`, run `RUN_M2_CLIENT.bat`, and test daylight, night, cover, weather, dimensions, adjacent output, upgrades, persistence, drops, GUI layout, and runtime logs.


### 2026-08-27 — Verify Solar Panel runtime and fix stale log gate

- The complete Solar Panel gameplay matrix passed in the development client.
- Verified open-sky daytime generation, zero generation at night, zero generation under cover, weather response, adjacent Forge Energy output, and always-visible debug values.
- Verified the 64000 FE base capacity, Power Storage-only upgrade filtering, 128000/256000 FE upgraded capacities, persistence, capacity clamping, and upgrade drops.
- The client build completed successfully in 3m 25s with 10 tasks (3 executed, 7 up-to-date).
- Live registry counts, textures, runtime failures, and data-pack tags remained clean.
- The runtime checker produced error 32 only because it still expected the pre-Solar exact marker with six block entities and menus.
- Updated the gate to require seven block entities, seven menus, and the explicit `solarPanel=enabled` marker without depending on the entire marker line.
- Exact next step: pull this checker fix, rerun `CHECK_M2_RUNTIME.bat` against the existing `run\logs\latest.log`, then run `VERIFY_M2_BUILD.bat`.


### 2026-08-27 — Complete and verify Solar Panel milestone

- The corrected `CHECK_M2_RUNTIME.bat` passed all five gates against the completed Solar Panel development-client log.
- `VERIFY_M2_BUILD.bat` passed all four stages with JDK 17.0.20.1 and Gradle 8.1.1.
- M1 resources, M2 sources, and a clean Forge compilation passed; the full build executed 10 tasks.
- The source gate confirms seven functional block entities and menus, including Solar Panel at 8 FE/t peak, 64000 FE base storage, and 512 FE/t per-side output.
- Three existing non-blocking deprecation warnings remain in `ClientModEvents.java`, `MatterOverdrive.java`, and `ModSounds.java`.
- Solar power generation milestone is complete and ready to fast-forward into `main`.


## Current work: Tritanium Crates

Branch `feature/tritanium-crates` starts from verified Solar Panel milestone commit `9bb0bbb`.

Why this precedes Charging Station:

- legacy Charging Station only charged Android player energy;
- Android capabilities are not yet ported, so adding it now would make a visibly functional but purposeless machine;
- the 17 registered Tritanium Crate variants are self-contained and immediately useful for the existing production/network loop.

Planned scope:

- replace the base Tritanium Crate and all 16 dyed crate placeholders with a shared functional storage block entity;
- restore the legacy 54-slot capacity for every variant;
- provide a GUI, shift-click transfers, persistent storage, Forge item capability access, and drop handling;
- retain the crate contents in its dropped item and restore them when placed again, matching legacy portable-storage behavior;
- expose a visible debug line for used slots and total item count;
- ensure all variants use the same block entity registration and add focused source/runtime verification.

Exact next step: inspect the current block/item/loot setup and implement the shared crate behavior without changing the registered IDs or resources.


### 2026-08-27 — Implement functional Tritanium Crates

- Replaced the base Tritanium Crate and all 16 coloured crate placeholders with one shared functional storage block entity.
- Restored the legacy 54-slot inventory for every variant, with normal shift-click transfers and a Forge item-handler capability for automation.
- Crate contents persist in the block entity, are written into the dropped crate item's NBT, and restore when that crate item is placed again.
- Added a compact 6×9 crate GUI with the required always-visible debug readout: used slots out of 54 and total stored item count.
- Registered one common crate block entity type across all 17 existing block IDs, plus the menu and client screen.
- Updated the M2 source and runtime gates for eight block entities/menus and the explicit crate marker.
- Runtime behavior is unverified until the development-client test passes.
- Exact next step: pull this branch, run `RUN_M2_CLIENT.bat`, test storage, all-colour variants, shift-clicking, portable drops/replacement, debug values, and the runtime/build gates.


### 2026-08-27 — Fix Tritanium Crate item comparison compile error

- The first crate branch compile correctly identified an item/block comparison in portable drop handling: `ItemStack.is` accepts an item, not a block.
- Corrected the comparison to use the crate block's item form, preserving all-colour portable inventory serialization.
- This is a compile-only correction; runtime behavior remains unverified.
- Exact next step: pull the fix, run `RUN_M2_CLIENT.bat`, then perform the crate storage and portable-drop test matrix.


### 2026-08-27 — Verify functional Tritanium Crates

- Confirmed every Tritanium Crate variant opens and provides its functional shared inventory.
- Confirmed the 54-slot crate storage UI works in the development client.
- The portable-drop, shift-click, Forge item capability, and debug readout implementation is now ready for the normal final gate pass.
- Tritanium Crate milestone is complete and ready to fast-forward into `main`.
- Exact next step: merge the verified crate branch, then select the next legacy system that has real gameplay value without depending on unported Android capability infrastructure.


## Current work: Inscriber production bundle

Branch `feature/inscriber-production` starts from the verified Tritanium Crate milestone commit `58ed97c`.

This is intentionally a larger, complete production-system port rather than another isolated placeholder:

- make the Molecular Inscriber a functional FE-powered workstation with its original three inputs/outputs and four upgrade slots;
- restore all three legacy Isolinear Circuit tier-upgrade operations: Mk1 + Gold → Mk2, Mk2 + Diamond → Mk3, and Mk3 + Emerald → Mk4;
- add the missing modern crafting recipes needed to obtain the Mk1 input and the Inscriber itself, then port a focused set of existing machine progression recipes that consume upgraded circuits;
- restore normal item capability automation, recipe-aware slot filtering, shift-clicking, persistence, upgrade drops, and capacity clamping;
- add persistent visible debug values for selected recipe, current progress, effective cycle time, and FE/t;
- extend source/runtime verification for the expanded production loop.

Legacy values to preserve:

- base FE capacity: 512000;
- FE input/output transfer limits: 256 FE/t;
- cycle time and total energy: Mk2 300 ticks / 64000 FE, Mk3 600 / 88000, Mk4 1200 / 114000;
- primary circuit input holds one item, secondary material holds 64, output is extraction-only;
- accepted upgrades: Power, Speed, Power Storage, and Power Transfer (the latter remains documented but has no current transfer-side use until machine IO controls are ported).

Exact next step: implement the complete Inscriber, its recipe model, core recipes, GUI/debug synchronization, registrations, and verification in one feature branch before the next runtime test.


### 2026-08-27 — Implement Inscriber production bundle

- Replaced the Molecular Inscriber placeholder with a functional FE-powered block entity, GUI, and Forge item/energy capabilities.
- Restored the full legacy Isolinear Circuit sequence: Mk1 plus Gold becomes Mk2 at 64000 FE over 300 ticks; Mk2 plus Diamond becomes Mk3 at 88000 FE over 600 ticks; Mk3 plus Emerald becomes Mk4 at 114000 FE over 1200 ticks.
- Restored 512000 FE base storage and 256 FE/t input/output limits, recipe-aware one-slot primary input, 64-slot secondary material input, extraction-only output, shift-click routing, persistence, and block-break drops.
- Added four persistent upgrade slots. They accept Speed, Power, Power Storage, and Hyper Speed, using the shared 1.20.1 upgrade model; legacy Power Transfer has no corresponding registered current upgrade and remains deferred with machine-side IO controls.
- Added always-visible synchronized debug values for selected recipe, running/idle state, effective cycle ticks/seconds, FE/t, and total energy.
- Ported the supporting survival material chain: Tritanium dust/ore smelting, ingot/nugget conversion, Tritanium Plates, Machine Casings, Isolinear Circuit Mk1, and the Inscriber crafting recipe.
- Registered the new block entity and menu, bringing the framework to nine block entities and nine menus, and extended M2 source/runtime gates accordingly.
- Runtime behavior is unverified until the development-client test passes.
- Exact next step: pull this branch, run `RUN_M2_CLIENT.bat`, and test all three circuit recipes, energy input, upgrades, debug values, automation slots, persistence/drops, the new crafting chain, and the standard runtime/build gates.


### 2026-08-27 — Restore Inscriber battery input

- Runtime testing correctly exposed that the first Inscriber implementation had FE capability access but no item battery input slot, unlike the existing M2 machine workflow.
- Added a dedicated battery slot that accepts Forge Energy items, including the Creative Battery, and charges the Inscriber at its restored 256 FE/t receive limit.
- Shift-click now prioritizes compatible energy items into that slot; the GUI includes the visible battery slot alongside the recipe inputs and output.
- Extended the source gate to require battery-charging wiring.
- Exact next step: pull the correction, run the development client, insert a Creative Battery, and verify the Inscriber gains FE and completes the Mk1-to-Mk2 recipe.


### 2026-08-27 — Migrate existing Inscribers to the battery-slot inventory

- The battery-slot update revealed an expected save-format edge case: already-placed Inscribers had a serialized three-slot inventory, while the new implementation reads a fourth battery slot.
- Replaced generic inventory deserialization with explicit bounded loading of known slots. Old three-slot saves now retain their inputs/output and receive an empty battery slot; new four-slot saves continue to load normally.
- This prevents ticking old placed Inscribers from crashing the integrated server after an update.
- The source gate now requires the save migration path.
- Exact next step: pull the fix, reopen the existing test world, put the Creative Battery into the new slot, and verify FE charging plus Mk1-to-Mk2 processing.


### 2026-08-27 — Verify Inscriber production bundle

- Runtime testing confirmed all three Molecular Inscriber recipes work: Mk1 plus Gold to Mk2, Mk2 plus Diamond to Mk3, and Mk3 plus Emerald to Mk4.
- The Creative Battery input and the current-world three-slot-to-four-slot inventory migration both worked without a server crash.
- The Inscriber production bundle, including its supporting material/crafting recipes and visible debug values, is complete and ready to fast-forward into `main`.
- Exact next step: merge the verified bundle, then take the next larger self-contained legacy system rather than another individual placeholder.


## Current work: Transporter network bundle

Branch `feature/transporter-network` starts from verified Inscriber production bundle commit `d93116c`.

### 2026-08-27 — Implement Transporter network bundle

- Replaced the Transporter placeholder with a powered functional Transporter, bound Transport Flash Drives, GUI, automation capabilities, five upgrade slots, persistence, drops, and always-visible debug values.
- A Transport Flash Drive is bound by right-clicking the destination block. The Transporter reads that drive from its first slot and teleports entities standing on top of it to the stored same-dimension target.
- Restored legacy base behaviour: 1024000 FE storage, 32-block range, 70-tick transport cycle, 80-tick delay, and 16 FE per travelled block.
- Restored Speed, Power, Range, and Power Storage effects; added a Creative Battery-compatible energy slot.
- Added the linked legacy Transport Flash Drive and Transporter crafting recipes.
- Runtime behavior is unverified until the development-client test passes.
- Exact next step: pull the branch, bind a drive to a destination, insert it and a Creative Battery, stand on the Transporter, and verify target/range/energy/debug/upgrade/persistence behavior.


### 2026-08-27 — Verify Transporter network bundle

- Runtime testing confirmed Flash Drive binding, target detection, Creative Battery charging, transport countdown, same-dimension teleporting, debug values, and the linked Transporter workflow all work.
- Transporter Network is complete and ready to fast-forward into `main`.
- Exact next step: merge the verified network, then begin the larger Fusion Reactor system bundle.


## Current work: Fusion Reactor bundle

Branch `feature/fusion-reactor` starts from verified Transporter Network commit `f90804d`.

This is the next major multi-block system bundle:

- restore the Controller, Coil, and IO block roles as one validated reactor structure;
- restore legacy reactor values: 100000000 FE capacity, 2048 kM matter buffer, 2048 FE/t baseline output, and 1/80 kM/t baseline matter drain;
- port the Gravitational Anomaly requirement and its distance-based efficiency; 
- make IO parts expose the controller's Forge Energy/Matter storage and output power to adjacent receivers;
- include structure-state, fuel/energy, efficiency, output, and fault-reason debug readouts;
- port the linked build recipes and provide an explicit in-game build/test plan.

Legacy design findings:

- the controller checked its 28-part structure every 40 ticks;
- it required a Gravitational Anomaly within three blocks, with closer placement increasing efficiency;
- reactor coils and the controller's IO parts have distinct required positions;
- it only generated while the structure was valid and matter remained; its IO parts sent up to 512 FE/t to adjacent receivers;
- this system is independent of Android capability work, so it is appropriate for the next usable large feature.

Exact next step: map the legacy structure positions into a clear 1.20.1 build validation model, then implement Controller, Coil, IO, anomaly interaction, capabilities, menus, debug data, and recipes as one branch.


## Fusion Reactor implementation — awaiting build and runtime verification

- Added functional Fusion Reactor Controller and Reactor IO block entities with the legacy baseline values: **100,000,000 FE** storage, **2,048 kM** matter storage, **2,048 FE/t** maximum base generation, and **0.0125 kM/t** base matter drain.
- The first 1.20 structure is deliberately compact and visible: put a **Fusion Reactor Controller** in the centre, a **Fusion Reactor Coil** directly north, south, east, and west, a **Fusion Reactor IO** directly above it, and a **Gravitational Anomaly** within three blocks. The controller’s debug UI states the exact missing condition when invalid.
- Controller accepts Matter Container transfers through the Matter capability. Reactor IO exposes and exports up to **512 FE/t per adjacent side** from the controller buffer.
- Added Controller GUI with four upgrade slots and persistent debug readouts for structure, output, anomaly distance, efficiency, matter drain, stored energy and matter. Supported upgrades: Speed, Range, Power Storage, and Matter Storage.
- Updated M2 source/runtime gates for 12 block entities, 11 menus, and the Fusion Reactor marker.
- This commit is an implementation checkpoint only; run RUN_M2_CLIENT.bat plus the standard M2 checks before marking verified. The large legacy reactor exterior/rendering and Gravitational Stabilizer behavior remain future work.


## Fusion Reactor compile correction — awaiting verification

- Corrected the Fusion Reactor screen render call to pass Minecraft's required `partialTick` parameter to `AbstractContainerScreen`.
- This fixes the compile failure reported by `RUN_M2_CLIENT.bat`; re-run the client and the standard M2 gates after pulling this commit.


## Fusion Reactor menu shift-click correction — awaiting verification

- Fixed Fusion Reactor controller inventory slot boundaries: the menu has four upgrade slots plus 36 player slots (indices 0–39), not 49 slots.
- This resolves the client crash `Index 40 out of bounds for length 40` caused by shift-clicking an item in the controller UI.


## Fusion Reactor range, speed, and IO corrections — awaiting verification

- Capped the effective anomaly scan range at **16 blocks**. Range upgrades still extend the reactor's reach from the 3-block base, but cannot create the runaway cubic scan that locked the integrated server and prevented world interaction/closing.
- Corrected Speed upgrade behaviour: its legacy `0.75x` speed multiplier now increases generation (by reducing the cycle factor) instead of reducing output.
- Reworked Reactor IO transfer to use the proven Solar Panel simulation/extract/refund flow, including a receiver `canReceive` check. IO exports up to 512 FE/t to each adjacent receiving side, excluding the Controller below it.


## Energy Cable implementation — awaiting build and runtime verification

- Converted the existing **Heavy Matter Pipe** block into the functional **Heavy Energy Cable**. This preserves the port's legacy block/resource inventory while giving the player a dedicated FE conduit with no missing texture or registry expansion.
- Each cable buffers **8,192 FE** and relays up to **1,024 FE/t per connected receiving side**. Reactor IO pushes FE into the first cable; chained cables then pass it onward to FE-accepting machines.
- Added a cable GUI with persistent `[DEBUG]` readouts for stored FE, last output, and the six-sided relay behaviour.
- Updated M2 source/runtime gates for 13 block entities, 12 menus, and the Energy Cable marker.
- Test with `Reactor IO -> Heavy Energy Cable(s) -> powered machine`. Matter Pipe and Network Pipe remain matter/task-only.


## Test build packaging — ready for local JAR creation

- Added `CHANGELOG.md` covering the complete M2 functional-systems test build: machines, upgrades, solar, crates, inscriber, transporter, Fusion Reactor, and Heavy Energy Cable.
- Added `PACKAGE_TEST_JAR.bat`, which runs the project’s Gradle `jar` task and prints the generated test JAR path under `build\\libs`.
- The current feature branch is ready for final `VERIFY_M2_BUILD.bat` validation after packaging.


## Working-feature reference — ready for test distribution

- Added `WORKING_FEATURES.md`, an exhaustive current-branch reference for installing the test JAR, functional machines, items, networks, upgrades, Inscriber recipes, Fusion Reactor, Heavy Energy Cable, verified checks, and known non-functional legacy shells.
- The document is deliberately explicit that only listed systems are gameplay-functional; remaining registered legacy content is not claimed as complete.
