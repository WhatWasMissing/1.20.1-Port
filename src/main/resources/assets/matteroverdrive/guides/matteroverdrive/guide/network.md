---
navigation:
  title: Matter Network
  parent: index.md
  position: 7
  icon: matteroverdrive:network_router
---
# Matter Network

Matter Overdrive uses several different connection types. They look related, but they move different resources. Keeping that distinction clear makes most network problems much easier to diagnose.

## Transport layers

### Heavy Energy Cable

Heavy Energy Cable moves **Forge Energy (FE)**. Use it between generators, batteries and machines that expose an FE capability. The current cable implementation has an **8,192 FE internal buffer** and can transfer up to **1,024 FE/t per side**.

On the tech-overhaul branch, configured FE faces matter. If the cable reaches a machine but the selected face is configured as OUTPUT or DISABLED, that machine will not accept FE from that side.

A Fusion Reactor's internal ring power bus is special: blocks that belong to the reactor ring can share reactor FE internally. Once power leaves that structure, use Reactor IO and energy cable for ordinary external consumers.

### Matter Transport Pipe

Matter Transport Pipe moves **Matter Plasma**, not items or FE. Connect it to machines that expose Matter storage/capability, such as the matter-processing chain, Matter Storage Matrix and supported Reactor IO connections.

The tech-overhaul branch adds six-face Matter policies. INPUT, OUTPUT, BOTH and DISABLED are enforced by real Matter routing. If a pipe appears correctly connected but no Matter moves, inspect the exact machine face touched by the pipe.

### Hybrid Conduit

The **Hybrid Conduit** is a higher-tier physical cable that carries **FE and Matter Plasma through the same run**. It participates in the Heavy Energy Cable FE graph while also being recognised as a Matter Transport Pipe by Matter routing.

This means one Hybrid Conduit line can feed a machine both of its ordinary infrastructure resources. It can join an existing Heavy Energy Cable run for FE and an existing Matter Pipe run for Matter. It does not carry Pattern Monitor/Router/Switch jobs; logical task routing still uses Matter Network Cable.

### Matter Network Cable

The **Network Pipe / Matter Network Cable** belongs to the task/item routing layer. Routers, Switches, Pattern Monitor, Pattern Storage and Replicator use this layer to discover destinations and move replication work. It is not a replacement for FE cable, Matter Pipe or Hybrid Conduit.

## Channels

Routers and Switches can be assigned to **channels 0-15**. A routing traversal only crosses matching configured Router/Switch nodes, allowing logical subnetworks to share nearby physical infrastructure without every device participating in the same route graph.

Channel changes persist. When diagnosing an apparently disconnected network, verify channel configuration before replacing cables.

## Router priority

A Matter Network can contain multiple Routers. The tech-overhaul branch adds Router priority so one active executor can be selected predictably. The highest-priority powered Router on the channel becomes the executor; ties are resolved deterministically by position.

This allows a primary/backup design instead of relying on placement order. Existing destination filtering, Network Flash Drive routing, speed upgrades, route history and FE-per-item accounting remain part of the router backend.

## Quantum Power Relay links

Quantum Power Relays no longer create a shared broadcast pool. Wireless energy only crosses **explicit player-created links**.

Use a **Quantum Linker** on one relay and then on a second relay. This creates a bidirectional pair. A relay supports up to **8 links**, links are same-dimension, maximum link distance is **256 blocks**, and unloaded destinations are not force-loaded. Sneak-use the Linker on a relay to clear its links.

An unlinked relay near a working pair should receive **zero** wireless FE from them.

## Facility Network Controller

The **Facility Network Controller** is the central monitoring console for the logical Matter Network. Place it against Network Pipe, a Router, or another reachable section of the data network and use it to perform a live topology scan.

A normal use reports connected nodes, FE-capable endpoints, Matter-capable endpoints, aggregate FE and Matter storage, and alarm state. **Sneak + use** prints a per-device inventory grouped by machine type. Comparator output reflects current alarm severity, so the controller can drive redstone warning systems.

The controller follows logical network traversal. It does not magically scan every block in the base. If a machine is not reachable through the network topology, the controller should not report it.

## Network Diagnostic Probe

The **Network Diagnostic Probe** is the operator tool for the upgraded network backend. Cycle its modes to work with Diagnostic summary, FE faces, Matter faces, Item faces, Network channels/priority and Range visualization.

Use it on a configured machine face to cycle **INPUT / OUTPUT / BOTH / DISABLED**. In Network mode it alters Router/Switch configuration and Router priority. Quantum Relay pairing is deliberately handled by the dedicated Quantum Linker so wireless connections are explicit rather than accidental.

The probe changes server-backed state. It is not a cosmetic overlay.

## Network Switch

The **Network Switch** can enable or isolate a local section and now also carries a channel. A disabled Switch blocks traversal regardless of channel. An enabled Switch on the wrong channel also isolates that path for the active traversal.

## Network Flash Drive

The **Network Flash Drive** stores network connection/filter information. It is used when a machine needs a specific destination rather than any compatible device on the network.

Treat the drive as configuration data: keep it with the network it belongs to, and re-check its destination if a machine was moved or replaced.

## Pattern Storage and Pattern Monitor

**Pattern Storage** holds up to **six Pattern Drives**. A normal Pattern Drive stores **two item patterns**, giving one fully populated storage block twelve normal pattern slots.

The **Pattern Monitor** is the task front end. It searches the connected pattern network and queues replication requests; its current queue capacity is **8 tasks**. The Replicator then performs the actual matter/energy-consuming manufacture.

A useful troubleshooting order is:

1. Verify the desired pattern exists in Pattern Storage.
2. Verify Network Cable connects the Pattern Monitor, Pattern Storage and Replicator.
3. Verify Router/Switch channels match.
4. Verify no Switch has isolated part of the network.
5. Verify configured Item/Matter/FE faces permit the intended flow.
6. Verify the Replicator has enough FE and Matter.
7. Verify the Replicator output is not blocked.

## Replicator network bridge

The Replicator can accept local work and network-queued replication work. Network connectivity does **not** provide free resources: the machine still requires the same FE, Matter and valid pattern it would need when used directly.

On the tech-overhaul branch the Replicator can also use **Parallel Processing Upgrades**. Multiple lanes increase throughput when resources and output space exist, but also raise instantaneous FE/Matter demand.

## Applied Energistics 2

Matter Overdrive's task graph remains separate from AE2. Do not splice an ME cable directly into a Matter Network Cable and expect one combined graph. Where both mods expose ordinary inventories/capabilities, bridge them at the inventory/resource boundary instead. See [Applied Energistics 2](ae2.md) for the dedicated compatibility notes.

## Fast fault finding

**Machine has no FE:** inspect the configured FE face, then follow Heavy Energy Cable or Hybrid Conduit back toward the source.

**Machine has FE but no Matter:** inspect the Matter face policy and the Matter Pipe/Hybrid Conduit route.

**Quantum relay has power but remote relay does not:** confirm the pair was created with the Quantum Linker, both chunks are loaded and distance is within 256 blocks.

**Replicator has resources but no queued job:** inspect Pattern Storage, Pattern Monitor, Router/Switch channel and Switch state.

**Controller reports zero nodes:** place it directly against a live Network Pipe/Router/Switch path and verify channel/isolation state.

For the larger experimental infrastructure built on top of this network, continue to [Advanced Infrastructure](advanced_infrastructure.md).
