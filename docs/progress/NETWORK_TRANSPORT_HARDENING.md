# Network / Transporter hardening pass

Branch: `testing/tech-overhaul`
Release line: `0.6`

## Scope

This pass hardens three runtime-sensitive infrastructure contracts without changing the intended network topology model:

1. Transporter target/range/arrival safety.
2. Router item conservation when simulated and real handler behavior diverge.
3. Lifecycle cleanup for world-persistent per-face machine policies.

The retired Star Map remains absent from the active port and was removed from the active Transporter GuideME page.

## Transporter changes

- The effective range boundary is now inclusive (`distance <= range`), so the displayed 32-block base range accepts a target exactly 32 blocks away.
- The target chunk must already be loaded; the Transporter does not force-load remote chunks.
- At cycle completion, each attempted entity searches for a supported, collision-free arrival position above/around the bound target.
- Search order prefers the target column, then adjacent cells, and checks several vertical positions for usable headroom.
- Transport telemetry records only entities that actually moved.
- If zero entities can arrive safely, the cycle aborts without FE debit or post-transport cooldown.
- A partially successful group transport consumes its normal cycle FE once and reports the actual transported count.

## Router conservation changes

`ItemNetworkUtil.moveOneStack` already simulated destination insertion before extracting from the source. The remaining failure window was a handler whose real insertion accepted less than its simulation promised.

The real insertion result is now authoritative:

- only the count actually inserted is reported as moved;
- any real insertion remainder is returned to the original source slot first;
- other source slots are tried next;
- if the source handler refuses its own extracted remainder, the remainder is dropped at the source position rather than silently deleted;
- Router FE accounting remains based on the reported delivered item count.

This is primarily a conservation guard for unusual/modded `IItemHandler` implementations and race-like state changes.

## Side-policy lifecycle

`MachineSideConfigurationData` stores ENERGY/MATTER/ITEMS face policy by world position. Previously those entries could outlive the block that created them.

`MachineSideConfigurationEvents` now clears the position at LOWEST priority on successful server-side block break and placement events. A fresh block placed at an old machine coordinate therefore starts from the default `BOTH` policy instead of inheriting stale INPUT/OUTPUT/DISABLED configuration.

## Static validation

New gate:

```text
python scripts/validate_network_transport_consistency.py
```

It checks:

- inclusive Transporter range;
- unloaded-target guard;
- collision-aware arrival search;
- zero-success no-charge contract;
- Router remainder restoration/loss fallback;
- side-policy break/place cleanup;
- absence of an active retired Star Map link in the Transporter guide.

`VERIFY_M2_BUILD.bat` now runs this as gate 6 of 10 before Gradle bootstrap/build.

## Runtime priorities

See `docs/testing/TO_TEST.md`. Highest-value checks are:

1. 32-block exact boundary vs 33-block rejection.
2. unloaded destination behavior with no forced chunk loading.
3. fully obstructed destination: no move, no FE debit, no cooldown.
4. partially safe multi-entity destination: actual count only.
5. configure a side policy, break the machine, replace at the same coordinate, verify default BOTH.
6. branch-heavy Router item conservation across save/reload.

Static inspection does not replace a local Forge build/runtime pass.
