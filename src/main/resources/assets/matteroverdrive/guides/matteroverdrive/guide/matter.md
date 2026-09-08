---
navigation:
  title: Matter Technology
  parent: index.md
  position: 2
  icon: matteroverdrive:decomposer
---
# Matter Technology

The core matter chain is **analyze -> store pattern -> supply matter and FE -> replicate**. In 0.6 the matter-value system is recipe-aware, so crafted items normally derive their value from the subtotal of their components instead of relying on a short hard-coded list.

## Quick start

1. Power a **Matter Analyzer** and analyze the item you want to reproduce.
2. Store the resulting pattern on a **Pattern Drive** or expose it through **Pattern Storage**.
3. Produce matter with a **Decomposer** and move/store it with Matter Containers and Matter Pipes.
4. Give the **Replicator** access to the pattern, enough matter and enough Forge Energy.
5. Use the Pattern Monitor/network request path or a local Pattern Drive and allow replication to complete.

## How 0.6 calculates matter values

Matter Dust can carry its own dynamic value. For ordinary items, a world-aware lookup attempts to resolve a usable recipe before accepting an older explicit/tag/fallback value. This makes crafted outputs follow the cost of their ingredients whenever a valid recipe path exists.

Important rules:

- recipe ingredients use the cheapest valid positive alternative;
- ingredient values are added to form the recipe subtotal;
- if a recipe produces multiple items, the subtotal is divided across the output stack;
- division rounds **down**, so integer rounding cannot manufacture free matter;
- reversible compression chains and other cycles are guarded so they terminate instead of recursively inflating values;
- if no usable recipe resolves, explicit values, tag anchors and deterministic fallbacks still keep the item usable.

Stable raw-material anchors include raw iron/iron ingot at 32, raw gold/gold ingot at 42 and raw copper/copper ingot at 16 matter per item.

## Pattern Drives and old worlds

Pattern Drives still store the matter value captured when analysis occurred, but the **Replicator no longer blindly trusts that stored number**. Local and network replication now re-resolve the pattern stack against the current world recipe manager and use the current recipe-aware value when one is available. The stored value is retained as a compatibility fallback.

This means an old Pattern Drive from before the 0.6 economy pass should automatically adopt the current replication cost instead of permanently preserving an outdated cheap or expensive value.

## Decomposer

The Decomposer resolves the current input against the world-aware matter registry. It consumes one input item when a cycle completes and adds the resolved amount to its matter storage unless the decomposition failure roll occurs. A failure produces Matter Dust carrying that item's matter value instead of successful stored matter.

The machine has real FE storage, matter storage, progress, redstone mode and upgrades. These values are serialized across save/reload.

## Analyzer, Pattern Storage and Pattern Monitor

The Matter Analyzer records analysis progress onto compatible Pattern Drives. Normal drives retain the legacy two-pattern capacity; creative drives have expanded capacity. Pattern Storage provides multiple drive slots so a network can expose more patterns at once.

The Pattern Monitor is the player-facing request/control point for network replication. Network replication tasks retain their pattern, amount, source monitor and machine progress through save/reload.

## Replicator

The Replicator requires all three of the following:

- a valid pattern with analysis progress;
- enough matter for the current live matter cost;
- enough FE to complete processing.

The machine supports local Pattern Drive replication and queued network jobs. Successful replication places the item in the output slot. A failed replication places value-carrying Matter Dust into the failure output. Speed, power-use, storage and failure-rate upgrades modify the machine according to their supported upgrade types.

## Anomalies and matter values

Items swallowed by an existing Gravitational Anomaly also use the **level-aware recipe value**. Feeding a crafted item to an anomaly therefore follows the same 0.6 economy used by the Decomposer/Replicator rather than an unrelated hard-coded value path.

## Tooltips and diagnostics

Matter-capable items can display their effective matter value. Hold **Shift** where indicated to expose the resolver source.

Admin/testing commands:

- `/matteroverdrive matter value` - inspect the held item's effective value and source.
- `/matteroverdrive matter audit` - run the explicit full-registry coverage/outlier audit.
- `/matteroverdrive matter clearcache` - clear cached recipe-derived values.

The expensive full audit is intentionally **not** run automatically during server startup; startup only invalidates the recipe cache. This avoids the old 100%-world-loading stall on large registries.

## Avoiding economy exploits

When testing modded recipes, look closely at reversible compression, multi-output crafting, alternative/tag ingredients, smelting/blasting/stonecutting and machine recipes. A valid 0.6 chain should not let you decompose ingredients, craft an output and gain matter solely because of rounding or a stale Pattern Drive value.

For routing and automation, continue to [Matter Network](network.md). For FE requirements, see [Power and Machines](power.md).
