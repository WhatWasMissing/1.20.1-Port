---
navigation:
  title: Matter Technology
  parent: index.md
  position: 2
  icon: matteroverdrive:decomposer
---
# Matter Technology

The core matter chain is **analyze -> store pattern -> supply matter and FE -> replicate**.

## Quick start

1. Power a **Matter Analyzer** and analyze the item you want to reproduce.
2. Store the resulting pattern on a **Pattern Drive** / **Pattern Storage** network.
3. Produce matter with a **Decomposer** and move/store it with Matter Containers and Matter Pipes.
4. Give the **Replicator** access to the pattern, enough matter and enough Forge Energy.
5. Select/request the pattern and allow the replication task to complete.

## Matter values in 0.6

Matter Overdrive 0.6 no longer depends only on a short hard-coded item list. The value resolver uses the following priority:

1. Dynamic values carried by Matter Dust.
2. Explicit Matter Overdrive/vanilla values.
3. Supported tag-based base values.
4. Values recursively derived from crafting/processing recipes.
5. A deterministic fallback when no better value can be resolved.

Recipe-derived values account for output count, choose the cheapest valid positive ingredient option and protect against cyclic or excessively deep recipe chains. This lets a much larger part of vanilla and modded item progression participate in the matter economy without assigning arbitrary hand-written values to every item.

The **Matter Analyzer** and **Decomposer** resolve values against the current world/recipe manager. Pattern Drives preserve the matter value that was analyzed, and the Replicator uses that stored value. If a Pattern Drive was created before the 0.6 valuation update, analyze the item again if its stored value appears outdated.

## Tooltips and diagnostics

Matter-capable items can display their effective matter value. Hold **Shift** where indicated to expose the source used by the resolver.

For testing/admin diagnostics:

- `/matteroverdrive matter value` inspects the held item's effective value/source.
- `/matteroverdrive matter audit` summarizes matter-value coverage.
- `/matteroverdrive matter clearcache` clears derived recipe-value caches.

These commands are primarily diagnostics; normal survival play does not require them.

## Machines

The **Decomposer** converts supported items into matter. The **Matter Analyzer** and **Matter Scanner** discover item information used by the pattern system. **Pattern Drives** and **Pattern Storage** preserve patterns. The **Pattern Monitor** exposes available patterns and requests. The **Replicator** consumes stored matter and Forge Energy to manufacture the selected pattern. **Matter Pipes** and **Matter Containers** move/store matter where their exposed interfaces allow it.

For automation and routing beyond the basic matter chain, continue to [Matter Network](network.md).