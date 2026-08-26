# Historical alpha.3 verification path

This file documents the previous direct Pattern Drive bridge. For the current build use `M2_NETWORK_VERIFICATION_PATH.md`.

# M2 alpha.3 functional machines verification

Target: Minecraft 1.20.1 / Forge 47.4.10 / Java 17.

## Gate A - build
Run `VERIFY_M2_BUILD.bat`.
Expected: `[PASS] M2 BUILD GATE PASSED` and `build\libs\matteroverdrive-0.8.0.0-alpha.3.jar`.
Stop and send the complete console output if this fails.

## Gate B - client/runtime
Run `RUN_M2_CLIENT.bat`. Matter Overdrive must load without a Forge error screen.

### Decomposer regression
Confirm the already-verified Decomposer still opens, accepts the Creative Battery, and turns Dirt into 1 kM.

### Recycler
Enable cheats and run:
`/give @s matteroverdrive:matter_dust{Matter:32} 1`
Place Matter Recycler, insert Creative Battery, then the 32 kM raw Matter Dust.
Expected: input is consumed and a refined Matter Dust item with 32 kM appears in the output. The legacy formula is 80 * ln(1+matter)^2 ticks and 1000 FE per matter total.

### Analyzer
Place Matter Analyzer. Insert Creative Battery, a Creative Pattern Drive, and one Dirt.
Expected after 800 ticks (~40 seconds): Dirt is consumed and the drive tooltip/screen shows a Dirt pattern at 100% with 1 kM.
Normal Pattern Drive behavior is 20% per analyzed item, so five matching items are required for 100%.

### Replicator
Place a fresh Decomposer directly next to a Replicator. Put a Creative Battery in both. Put the analyzed Dirt Pattern Drive into the Replicator. Feed one Dirt through the adjacent Decomposer.
Expected: the Decomposer's 1 kM transfers to the Replicator; the Replicator consumes 1 kM + FE and produces Dirt in its main output. A fully analyzed pattern has the legacy 0.5% failure chance; failure creates raw Matter Dust carrying the same matter value in the second output.

Important: alpha.3 uses Pattern Drive NBT as the temporary bridge between Analyzer and Replicator. The original Matter Network / Pattern Storage / Pattern Monitor transport layer is not yet ported.

## Gate C - persistence
Leave FE/items/matter/pattern drives in the machines, save and quit, reload the world, and confirm all state survives.

## Gate D - runtime log
Quit the client normally and run `CHECK_M2_RUNTIME.bat`.
Expected: `[PASS] M2 RUNTIME LOG GATE PASSED`.

## Gate E - dedicated server
Run `RUN_M2_SERVER.bat`. If needed, accept `run\eula.txt`, rerun, and wait for `Done`. Ctrl+C after `Done` is acceptable in the Gradle userdev console.
