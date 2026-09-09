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

The **Decomposer** converts supported items into Matter. The **Matter Analyzer** and **Matter Scanner** discover item information used by the pattern system. **Pattern Drives** and **Pattern Storage** preserve patterns. The **Pattern Monitor** exposes available patterns and requests. The **Replicator** consumes stored Matter and Forge Energy to manufacture the selected pattern. **Matter Pipes** and **Matter Containers** move/store Matter where their exposed interfaces allow it.

On the tech-overhaul branch, the Decomposer and Replicator can accept **Parallel Processing Upgrades**. Each installed module adds another work lane. More lanes can complete more operations over the same period, but they also increase peak FE/Matter demand and pressure on output space.

## Matter Storage Matrix

The **Matter Storage Matrix** is modular bulk Matter storage. It accepts up to four removable cells:

- **64k Matter Cell** - 64,000 Matter;
- **256k Matter Cell** - 256,000 Matter;
- **1M Matter Cell** - 1,000,000 Matter;
- **4M Matter Cell** - 4,000,000 Matter.

Capacity is the sum of installed cells. The Matrix refuses a cell removal if the remaining capacity would be smaller than the Matter already stored, preventing a configuration change from silently deleting Matter.

Connect it to ordinary Matter Transport Pipe. It exposes the same Matter capability model used by the rest of the port rather than creating a second incompatible storage system.

## Matter Excavator

The **Matter Excavator** extends decomposition to a configured world volume. Select a target block and working radius; the Excavator searches for matching blocks, consumes FE, removes them and stores their resolved Matter value.

Available range modes are **8 / 16 / 24 blocks**. This is destructive automation, so use the Network Diagnostic Probe's Range mode to visualize the operating volume before enabling a large job.

The Excavator is deliberately not a magical ore generator. It turns actual blocks into the same Matter economy used by the Decomposer and Replicator.

## Per-side Matter policy

Supported machines on the tech-overhaul branch persist a Matter policy for each face: **INPUT / OUTPUT / BOTH / DISABLED**. Matter Pipe routing reads those policies. If storage exists but transfer stalls, inspect the exact side the pipe touches before changing the rest of the network.

For automation, bulk infrastructure and routing beyond the basic matter chain, continue to [Matter Network](network.md) and [Advanced Infrastructure](advanced_infrastructure.md).
