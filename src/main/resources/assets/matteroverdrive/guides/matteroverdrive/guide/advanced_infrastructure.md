---
navigation:
  title: Advanced Infrastructure
  parent: index.md
  position: 8
  icon: matteroverdrive:facility_network_controller
---
# Advanced Infrastructure

The **0.7 release** adds a set of late-game infrastructure systems that extend Matter Overdrive's existing FE, Matter and Matter Network mechanics. Their runtime verification remains tracked in the release testing checklist.

## Facility Network Controller

Attach the Facility Network Controller to a reachable Matter Network path. It aggregates connected machine telemetry instead of scanning the entire base by radius. Use it to inspect connected device count, combined FE storage/capacity, combined Matter storage/capacity and facility alarms. Sneak-use displays the connected-device inventory. A redstone comparator can read the controller's current alarm severity.

## Network channels and priority

Routers and Switches support **channels 0-15**. Matching channels form logical subnetworks over the same physical infrastructure. A Router's priority decides which available router becomes the active executor for that channel; the highest priority wins and position order provides a deterministic tie break.

Use the **Network Diagnostic Probe** to inspect and configure this system without opening every machine manually. The probe has modes for diagnostic summary, FE, Matter, Items, Network and Range. It can inspect configured faces, change Router/Switch channels, adjust router priority and visualize supported working ranges.

## Per-side machine configuration

Supported machines can persist a separate policy for each of the six block faces. FE, Matter and Item capabilities use the modes **INPUT**, **OUTPUT**, **BOTH** and **DISABLED**. The routing backend respects those policies rather than treating every exposed face as equivalent.

This is intentionally server-authoritative. If automation behaves unexpectedly, inspect the exact machine face touched by the pipe or cable.

## Grid Capacitor

The Grid Capacitor is a high-throughput FE buffer intended to absorb generation/load spikes. It stores **16,000,000 FE** and can move up to **32,768 FE/t**. Place it between a high-output source and a demanding machine network. Comparator output reports its fill level.

## Energy Bank

The **Energy Bank** is a separate late-game buffer that stores reactor **Forge Energy** and **Matter** at the same time without combining their units. It holds **32,000,000 FE** and **1,000,000 Matter**, accepts both from Reactor IO, and emits FE to adjacent Energy Cable/Hybrid Conduit or compatible machines while its Matter output joins the existing Matter Pipe/Hybrid Conduit routing. Its output excludes the reactor loop, so stored resources remain available until another consumer can accept them.

## Android Induction Relay

The Android Induction Relay wirelessly charges converted Android players in the same dimension. It stores **2,000,000 FE**, accepts up to **16,384 FE/t**, supplies up to **8,192 FE/t**, and supports **32 / 64 / 96 block** operating ranges. Sneak-use cycles the range.

This does not perform cross-dimensional charging and does not create energy. It must remain connected to a real FE source.

## Quantum Power Relay

Quantum Power Relays use **explicit point-to-point links** rather than automatically sharing energy with every compatible relay. This prevents unrelated bases or machines from silently joining the same wireless power pool.

Use a **Quantum Linker** on the first relay, then use it on the second relay. The pair is stored as a bidirectional link. Each relay can hold up to **8 explicit links** and the maximum distance between linked relays is **256 blocks**. Both relays must be in the same dimension, and both source/destination chunks must be loaded for energy to move.

The relay stores **1,000,000 FE** and can move up to **16,384 FE/t**. Sneak-use the Quantum Linker on a relay to clear all of that relay's links. Relays do not force-load destinations.

## Hybrid Conduit

The **Hybrid Conduit** is a higher-tier cable that carries **both Forge Energy and Matter Plasma** through the same physical cable run.

On the FE side it participates in the same graph as Heavy Energy Cable, using the existing **8,192 FE cable buffer** and **1,024 FE/t per-side** transfer behavior. Existing Heavy Energy Cable can feed directly into a Hybrid Conduit and a Hybrid Conduit can feed back out into Heavy Energy Cable. The transition is explicitly recognised by both cable blocks, so their visual arms should meet at the junction as well as the FE graph remaining connected.

On the Matter side it is treated as a Matter Transport Pipe by the routing backend. Existing Matter Transport Pipe can therefore feed directly into a hybrid trunk, and the hybrid trunk can branch back into ordinary Matter Pipe. These Matter-Pipe/Hybrid transitions are also explicitly recognised for connection rendering.

A useful compact factory layout is to bring an existing Heavy Energy Cable line and an existing Matter Transport Pipe line into different points of one Hybrid Conduit trunk. The trunk can then carry both resources to machines that require both FE and Matter. The legacy branches remain resource-specific: Heavy Energy Cable does not gain Matter transport and Matter Transport Pipe does not gain FE transport.

The Hybrid Conduit does **not** merge the logical Matter Network task layer into FE/Matter transport. Network Cable is still required for Pattern Monitor/Pattern Storage/Router/Switch task routing.

## Crafting availability

All player-facing hardware introduced by the recent Android/facility/0.7 work is intended to be craftable in survival. The local 0.7 validator checks that each of these additions has a recipe and that the recipe produces its matching Matter Overdrive registry ID. This includes the Facility Network Controller, Anomaly Containment Unit, Android chassis modules, all new infrastructure blocks, the Network Diagnostic Probe, Quantum Linker, Matter Storage Cells and Parallel Processing Upgrade.

If `BUILD_LOCAL.bat` reports a missing or mismatched new-system recipe, treat that as a packaging regression rather than an optional creative-only item.

## Matter Storage Matrix

The Matter Storage Matrix is a modular Matter bank. Install up to four removable Matter Cells. Current cell capacities are **64k, 256k, 1M and 4M Matter**. A cell can only be removed when the remaining installed capacity can still contain the Matter already stored in the matrix.

Connect Matter Transport Pipe or Hybrid Conduit to use the Matrix as ordinary Matter storage. Comparator output reports fill level.

## Matter Excavator

The Matter Excavator is an industrial-scale decomposer, not a conventional quarry. Configure a target block and a working radius; the machine searches the selected volume, consumes FE, removes matching blocks and converts their matter value into stored Matter. Current radius options are **8 / 16 / 24 blocks**.

Use range visualization before enabling a large excavation area. The machine is intended for bulk Matter production and should be treated as destructive automation.

## Holographic Status Panel

The Holographic Status Panel is a compact remote facility display. It can cycle between **Overview, Energy, Matter and Alarms** views and displays live telemetry from the connected facility network. Its comparator output can be used to drive warning lamps, doors or other redstone responses.

## Parallel Processing

The **Parallel Processing Upgrade** is supported by the Decomposer and Replicator on this branch. Each installed module adds another processing lane. Parallel work is not free: additional lanes increase instantaneous FE demand and can expose weak power or Matter supply networks.

## Recommended test order

1. Build one Grid Capacitor and verify FE in/out and comparator level.
2. Charge an Android through each Induction Relay range.
3. Use a Quantum Linker to pair two Quantum Power Relays and confirm an unlinked third relay receives nothing.
4. Clear the link and confirm wireless transfer stops.
5. Feed Heavy Energy Cable and Matter Transport Pipe into the same Hybrid Conduit trunk and verify both resources reach their consumers.
6. Break/re-place both hybrid junction types and confirm the cable arms and routing reconnect.
7. Install/remove every Matter Cell tier and verify stored Matter is never deleted by shrinking capacity.
8. Configure a small Excavator radius and confirm only the selected target is decomposed.
9. Split a Router/Switch network into two channels and verify routing isolation.
10. Use the Diagnostic Probe to change side policies and confirm real automation follows them.
11. Install Parallel Processing in a Decomposer/Replicator and compare throughput and FE demand.
