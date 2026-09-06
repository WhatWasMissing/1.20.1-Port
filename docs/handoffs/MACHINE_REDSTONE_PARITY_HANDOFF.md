# Processing Machine Redstone Pass Handoff

Branch: `testing/main`
Base head: `b136da96f711da560f5b9e128571eeb22a4ecca5`.
Final head: the commit containing this handoff.

## Recovered legacy behavior

The common legacy `MOTileEntityMachine` redstone configuration has three modes:
- LOW: machine work is enabled when unpowered.
- HIGH: machine work is enabled when powered.
- DISABLED: machine work ignores redstone.

## Implemented in this pass

- Added shared `MachineRedstoneMode` helper using the recovered semantics.
- Decomposer, Matter Recycler and Microwave now persist/synchronize a real redstone mode.
- Compatibility default is DISABLED when old saves do not contain the new key.
- Active work pauses without advancing progress or consuming work FE when the selected redstone condition is false.
- Passive FE charging remains live while work is paused; Decomposer stored-matter output remains live.
- Added CONFIG pages and real server-side CYCLE RS controls to all three operator GUIs.
- Physical machine/upgrade slots remain fixed and usable across pages.
- Added reference + runtime/visual testing docs.

## Next machine-configuration stage

Re-fetch exact `testing/main` before editing. Audit and then apply the same recovered LOW/HIGH/DISABLED primitive only where cleanly mappable to:
- Replicator (pause actual replication, do not discard/duplicate queued network requests).
- Inscriber (pause recipe progress, preserve valid partial cycle).
- Matter Analyzer (pause active analysis while preserving scan/pattern state).
- Charging Station (gate active charge transfer without destroying installed items/state).
- Space-Time Accelerator (gate active acceleration effect only).
- Solar Panel if legacy source confirms the generic mode should gate generation rather than storage/output.
- Transporter only if source-backed semantics map safely to dispatch; do not block GUI/destination management.

Do not add a duplicate generic mode to Fusion Reactor or Gravitational Stabilizer because they already expose dedicated source-backed redstone controls.

After the remaining machine config/GUI stage, create another handoff and move to the dedicated authoritative Dimensional Pylon pass.
