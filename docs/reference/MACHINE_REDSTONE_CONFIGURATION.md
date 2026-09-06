# Machine Redstone Configuration Parity

Authoritative legacy behavior: generic Matter Overdrive machines expose LOW, HIGH and NONE/DISABLED redstone modes.

- LOW: active when no neighboring redstone signal is present.
- HIGH: active when a neighboring redstone signal is present.
- NONE / DISABLED: ignore redstone and run whenever normal machine requirements are met.

## 1.20 compatibility rules

- Existing machine saves default to DISABLED unless the machine already had a source-backed historical behavior that must be preserved.
- Space-Time Accelerator is the exception: the existing 1.20 implementation already behaved as hard-wired LOW, so old saves migrate/default to LOW.
- Redstone gating pauses active work; it does not discard progress, queued network requests, stored FE/matter, or passive input capabilities.
- Charging Station may continue receiving FE while redstone-paused; outgoing item/Android charging is gated.
- Matter Analyzer already had a complete enum-backed redstone implementation and CONFIG page and is retained without replacement.

## Current coverage

- Decomposer: configurable LOW/HIGH/DISABLED.
- Microwave: configurable LOW/HIGH/DISABLED.
- Matter Analyzer: existing configurable LOW/HIGH/NONE retained.
- Charging Station: configurable LOW/HIGH/DISABLED; only outgoing charging is gated.
- Space-Time Accelerator: configurable LOW/HIGH/DISABLED with old-save LOW compatibility.

## Deferred from this pass

Matter Recycler, Replicator and Inscriber remain on their prior behavior until their implementations can be changed without tripping the repository's verifier-sensitive legacy constant checks. Their legacy constants and functional behavior are not being altered merely to make the verifier accept formatting changes.
