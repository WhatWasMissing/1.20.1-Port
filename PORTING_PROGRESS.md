# Matter Overdrive 1.20.1 Port Progress

This file is the durable hand-off record for continuing the port in a new ChatGPT conversation.

## Working rules

- Update this file in every commit made during assisted porting work.
- Record what changed, what was verified, known problems, and the exact next step.
- Do not mark runtime behavior as verified until it has been tested in Minecraft.
- Target: Minecraft 1.20.1, Forge 47.4.10, Java 17.
- Current port version: `0.8.0.0-alpha.4.1`.

## Current repository state

- Active work branch: `feature/matter-pipe-routing`
- Base branch: `main`
- Base commit: `7c67199bd6c038d642f98670e5e492bb0dea65e7`
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

- Java compilation passed on 2026-08-27.
- Rerun the repository's M2 verification script after pulling the source-gate fix.
- Test a decomposer outputting through Matter Pipe and Heavy Matter Pipe.
- Confirm matter reaches a compatible destination across turns and junctions.
- Confirm routing does not loop or duplicate matter.
- Confirm disconnected/full destinations do not delete matter.
- Confirm multiple sources and destinations behave deterministically or fairly.
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
