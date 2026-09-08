---
navigation:
  title: Matter Network
  parent: index.md
  position: 7
  icon: matteroverdrive:network_router
---
# Matter Network

Matter Overdrive has its own routing/task network. It is separate from both the FE cable network and an Applied Energistics 2 ME network.

## Main components

- **Network Pipe** - carries Matter Overdrive network connectivity and task routing.
- **Network Switch** - enables/disables sections of the network.
- **Network Router** - processes queued item routes and destination filters.
- **Pattern Storage** - exposes Pattern Drives and their stored patterns.
- **Pattern Monitor** - player-facing pattern/request interface.
- **Replicator** - executes queued replication tasks when it has pattern progress, matter and FE.
- **Network Flash Drive** - stores connection/destination information used by supported routing/filter paths.

## Pattern replication flow

A typical network replication path is:

1. Analyze an item onto a Pattern Drive.
2. Place the drive in Pattern Storage or otherwise expose it to the network.
3. Connect a Pattern Monitor and Replicator through the Matter Overdrive network.
4. Ensure the Replicator has enough matter and FE.
5. Request the pattern from the Monitor.
6. The Replicator accepts a network task and processes the requested amount.

Queued Replicator network tasks serialize the pattern stack, current live/fallback matter value, analysis progress, requested amount and source-monitor position. A world reload should therefore resume the job rather than silently forgetting it.

## Matter versus network pipes

Do not confuse the three routing systems:

- **Matter Pipe** transfers the Matter capability between compatible matter storages/machines.
- **Heavy Energy Pipe/Cable** transfers Forge Energy.
- **Network Pipe** carries Matter Overdrive item/task-network connectivity.

A machine may participate in more than one of these systems at the same time, but each connection is responsible for a different resource.

## Router behavior

Routers process queued routes, apply destination filtering and support speed/hyper-speed upgrades. Network Flash Drives can be used as destination filters where the router accepts them. Switches are useful for disabling a network segment without dismantling the pipes.

When diagnosing a route, first prove the source/destination inventories work directly, then add Network Pipes, then filtering/upgrades. This isolates capability problems from routing problems.

## Matter-value consistency

Network replication in 0.6 does not permanently trust a stale Pattern Drive price. The Replicator re-resolves the stored pattern stack using the current world/recipe manager and uses the live recipe-aware matter cost when possible, with the stored pattern value retained only as compatibility fallback.

This is important for old worlds: a pre-0.6 Pattern Drive cannot keep an obsolete cheap replication price simply because the NBT was created before the recipe-economy pass.

## Applied Energistics 2

Matter Overdrive and AE2 intentionally remain separate task graphs. Do not try to merge a Matter Overdrive Network Pipe into an ME cable as if they were the same network.

Where a Matter Overdrive block exposes normal Forge item capabilities, AE2 can automate that inventory through ordinary capability-based integration such as import/export or Storage Bus-style access. No artificial AE-to-FE or ME-to-Matter conversion is implied.

See [Applied Energistics 2](ae2.md) for the compatibility-specific guide.

## Troubleshooting

**Pattern visible but request does nothing:** check Replicator matter, FE, pattern progress and output/failure-slot space.

**Request worked before restart but vanished:** report this as a persistence regression; network task state is serialized in 0.6.

**Matter is not moving:** verify you used a Matter Pipe, not only Network Pipe.

**FE is not moving:** use Heavy Energy Pipe/Cable or a direct FE capability connection.

**Router ignores destination:** inspect the Network Flash Drive/filter state and remove advanced filtering temporarily to prove the base route first.
