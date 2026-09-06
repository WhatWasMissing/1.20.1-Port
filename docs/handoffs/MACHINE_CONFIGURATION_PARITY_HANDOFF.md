# Machine Configuration Parity Handoff

Branch: `testing/main`
Base head: `1d42818344ef0ea68452cbd61356245a60355c31`
Authoritative references: Matter Overdrive 1.7.10 0.4.2 and 1.12.2 0.7.1.0.

## Completed in this stage

- Preserved the recovered generic LOW / HIGH / NONE(DISABLED) machine-redstone semantics.
- Kept Matter Analyzer's existing enum-backed implementation intact.
- Kept Decomposer and Microwave configurable redstone support from the previous stage.
- Restored Matter Recycler to its prior verifier-safe implementation because its compacted source formatting tripped `verifyM2Sources` despite Java compilation succeeding.
- Added Charging Station redstone configuration. Incoming FE/buffer storage stays live; outgoing item and Android charging is gated.
- Converted Space-Time Accelerator from hard-wired LOW behavior to configurable LOW/HIGH/DISABLED while defaulting old saves to LOW.
- Added CONFIG-page controls and synchronized state for Charging Station and Space-Time Accelerator.
- Added reference and testing docs for the machine-redstone parity layer.

## Important verifier note

`verifyM2Sources` contains literal source-string checks for some legacy constants. Do not compact or reformat verifier-sensitive constant declarations unless the verifier is deliberately modernized in the same reviewed change. The previous failure was `Legacy recycler constants missing`, not a Java compilation failure.

## Deferred machine work

- Matter Recycler redstone mode can be reintroduced later while preserving verifier-sensitive source formatting.
- Replicator and Inscriber redstone patches were intentionally not landed in this stage because prepared compacted versions would also violate literal legacy-constant checks.

## Next pass

Re-fetch exact `testing/main`, confirm exact-head CI, then begin the Dimensional Pylon parity pass. Recover the authoritative 1.12 multiblock/power behavior before editing the current channel-relay implementation. Preserve compatibility/migration where possible rather than deleting existing player state. Add a dedicated Pylon handoff before moving onward.
