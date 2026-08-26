# alpha.4.1 - Pattern Monitor network discovery fix

This revision fixes the alpha.4 failure where Pattern Storage could contain a valid analyzed pattern but Pattern Monitor still displayed `Patterns: 0`.

## Cause

The alpha.4 data-network search traversed only `Network Pipe` blocks. Machines were treated as terminal endpoints, so some valid-looking branches were split into separate searches.

## Fix

The data network now traverses:

- Network Pipe
- Network Router
- Network Switch
- Matter Analyzer
- Pattern Storage
- Pattern Monitor
- Replicator

Matter Pipe / Heavy Matter Pipe remain a separate matter-only graph.

## Retest

You do not need to re-analyze Dirt if your existing Pattern Drive already contains `Dirt [100%]`.

1. Build alpha.4.1 with `VERIFY_M2_BUILD.bat`.
2. Run `RUN_M2_CLIENT.bat`.
3. Put a powered Pattern Storage containing the 100% Dirt Pattern Drive on the same data network as Pattern Monitor.
4. Open Pattern Monitor.
5. Expected: `Patterns: 1`, with a Dirt icon visible.
6. Click Dirt once. Queue should briefly become `1/8`, then return to `0/8` once a connected Replicator accepts it.
7. The Replicator should show the network Dirt pattern even with its local Pattern Drive slot empty.
