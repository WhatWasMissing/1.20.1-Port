---
navigation:
  title: Matter Network
  parent: index.md
  position: 7
  icon: matteroverdrive:network_router
---
# Matter Network

Matter Overdrive uses several different connection types. They look related, but they move different resources. Keeping that distinction clear makes most network problems much easier to diagnose.

## The three transport layers

### Heavy Energy Cable

Heavy Energy Cable moves **Forge Energy (FE)**. Use it between generators, batteries and machines that expose an FE capability. The current cable implementation has an **8,192 FE internal buffer** and can transfer up to **1,024 FE/t per side**.

A Fusion Reactor's internal ring power bus is special: blocks that belong to the reactor ring can share reactor FE internally. Once power leaves that structure, use Reactor IO and energy cable for ordinary external consumers.

### Matter Transport Pipe

Matter Transport Pipe moves **Matter Plasma**, not items or FE. Connect it to machines that expose Matter storage/capability, such as the matter-processing chain and supported Reactor IO connections.

If an FE cable works but a matter pipe does nothing, first check that the receiving block actually accepts Matter on that side and has free Matter capacity.

### Matter Network Cable

The **Network Pipe / Matter Network Cable** belongs to the task/item routing layer. Routers, Switches, Pattern Monitor, Pattern Storage and Replicator use this layer to discover destinations and move replication work. It is not a replacement for FE cable or Matter Transport Pipe.

## Router and Switch

The **Network Router** joins network sections and processes queued routes. Destination filtering can restrict where a route is allowed to go. Supported speed upgrades increase how quickly routing work is processed.

The **Network Switch** can enable or isolate a local section. If a machine disappears from the logical network, check nearby switches before rebuilding the entire cable run.

## Network Flash Drive

The **Network Flash Drive** stores network connection/filter information. It is used when a machine needs a specific destination rather than any compatible device on the network.

Treat the drive as configuration data: keep it with the network it belongs to, and re-check its destination if a machine was moved or replaced.

## Pattern Storage and Pattern Monitor

**Pattern Storage** holds up to **six Pattern Drives**. A normal Pattern Drive stores **two item patterns**, giving one fully populated storage block twelve normal pattern slots.

The **Pattern Monitor** is the task front end. It searches the connected pattern network and queues replication requests; its current queue capacity is **8 tasks**. The Replicator then performs the actual matter/energy-consuming manufacture.

A useful troubleshooting order is:

1. Verify the desired pattern exists in Pattern Storage.
2. Verify Network Cable connects the Pattern Monitor, Pattern Storage and Replicator.
3. Verify no Switch has isolated part of the network.
4. Verify the Replicator has enough FE and Matter.
5. Verify the Replicator output is not blocked.

## Replicator network bridge

The Replicator can accept local work and network-queued replication work. Network connectivity does **not** provide free resources: the machine still requires the same FE, Matter and valid pattern it would need when used directly.

Replication retains its normal failure rules. Networking changes how the job reaches the machine, not the cost or probability model of the job itself.

## Applied Energistics 2

Matter Overdrive's task graph remains separate from AE2. Do not try to splice an ME cable directly into a Matter Network Cable and expect one combined graph. Where both mods expose ordinary inventories/capabilities, bridge them at the inventory/resource boundary instead. See [Applied Energistics 2](ae2.md) for the dedicated compatibility notes.

## Fast fault finding

**Machine has no FE:** follow the Heavy Energy Cable back toward the source and check buffers/output sides.

**Machine has FE but no Matter:** inspect the Matter Pipe path and Matter capacity.

**Replicator has resources but no queued job:** inspect Pattern Storage, Pattern Monitor, Network Cable and Switch state.

**One cable works but a longer route fails:** test the route one segment at a time. The 0.6 line contains explicit chained-cable routing fixes, so a reproducible length/break-replace failure should be reported as a bug rather than treated as intended range behavior.
