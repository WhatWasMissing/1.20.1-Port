---
navigation:
  title: Advanced Infrastructure
  parent: index.md
  position: 8
  icon: matteroverdrive:facility_network_controller
---
# Advanced Infrastructure

The **tech-overhaul testing branch** adds a set of late-game infrastructure systems that extend Matter Overdrive's existing FE, Matter and Matter Network mechanics. These systems are experimental until they have passed runtime testing.

## Facility Network Controller

Attach the Facility Network Controller to a reachable Matter Network path. It aggregates connected machine telemetry instead of scanning the entire base by radius. Use it to inspect connected device count, combined FE storage/capacity, combined Matter storage/capacity and facility alarms. Sneak-use displays the connected-device inventory. A redstone comparator can read the controller's current alarm severity.

## Network channels and priority

Routers and Switches support **channels 0-15**. Matching channels form logical subnetworks over the same physical infrastructure. A Router's priority decides which available router becomes the active executor for that channel; the highest priority wins and position order provides a deterministic tie break.

Use the **Network Diagnostic Probe** to inspect and configure this system without opening every machine manually. The probe has modes for diagnostic summary, FE, Matter, Items, Network and Range. It can inspect configured faces, change Router/Switch/Quantum Relay channels, adjust router priority and visualize supported working ranges.

## Per-side machine configuration

Supported machines can persist a separate policy for each of the six block faces. FE, Matter and Item capabilities use the modes **INPUT**, **OUTPUT**, **BOTH** and **DISABLED**. The routing backend respects those policies rather than treating every exposed face as equivalent.

This is intentionally server-authoritative. If automation behaves unexpectedly, inspect the exact machine face touched by the pipe or cable.

## Grid Capacitor

The Grid Capacitor is a high-throughput FE buffer intended to absorb generation/load spikes. It stores **16,000,000 FE** and can move up to **32,768 FE/t**. Place it between a high-output source and a demanding machine network. Comparator output reports its fill level.

## Android Induction Relay

The Android Induction Relay wirelessly charges converted Android players in the same dimension. It stores **2,000,000 FE**, accepts up to **16,384 FE/t**, supplies up to **8,192 FE/t**, and supports **32 / 64 / 96 block** operating ranges. Sneak-use cycles the range.

This does not perform cross-dimensional charging and does not create energy. It must remain connected to a real FE source.

## Quantum Power Relay

Quantum Power Relays move FE wirelessly between **loaded** same-dimension relays on the same channel. They support channels 0-15, a **256 block** link range, **1,000,000 FE** internal storage and up to **16,384 FE/t** transfer.

Relay discovery uses a loaded-relay registry rather than scanning world volumes, so inactive relays do not perform huge block scans. Unloaded destinations are deliberately not force-loaded.

## Matter Storage Matrix

The Matter Storage Matrix is a modular Matter bank. Install up to four removable Matter Cells. Current cell capacities are **64k, 256k, 1M and 4M Matter**. A cell can only be removed when the remaining installed capacity can still contain the Matter already stored in the matrix.

Connect Matter Transport Pipe to use the Matrix as ordinary Matter storage. Comparator output reports fill level.

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
3. Link two Quantum Relays on the same channel, then verify a different channel isolates them.
4. Install/remove every Matter Cell tier and verify stored Matter is never deleted by shrinking capacity.
5. Configure a small Excavator radius and confirm only the selected target is decomposed.
6. Split a Router/Switch network into two channels and verify routing isolation.
7. Use the Diagnostic Probe to change side policies and confirm real automation follows them.
8. Install Parallel Processing in a Decomposer/Replicator and compare throughput and FE demand.
