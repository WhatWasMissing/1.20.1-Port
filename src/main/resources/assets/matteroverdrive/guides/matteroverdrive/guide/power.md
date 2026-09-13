---
navigation:
  title: Power and Machines
  parent: index.md
  position: 3
  icon: matteroverdrive:solar_panel
---
# Power and Machines

Matter Overdrive machines use **Forge Energy (FE)**. Early machines can be run from modest generation, while Matter processing, weapons, Android infrastructure and the Fusion Reactor ecosystem push you toward larger storage and distribution systems.

## Starting power

The **Solar Panel** is the simplest early generator. Its current restored values are:

- up to **8 FE/t peak generation** in suitable daylight;
- **64,000 FE** internal storage;
- up to **512 FE/t per side** output.

Generation follows daylight rather than producing full power at all times. If a panel appears idle, check sky access and time of day before assuming its cable connection is broken.

## Heavy Energy Cable

Heavy Energy Cable is the normal FE transport line. Each cable currently buffers **8,192 FE** and can move up to **1,024 FE/t per side**.

The 0.7 release adds per-side FE policy to supported machines. A valid cable path is not sufficient if the receiving face is configured as OUTPUT or DISABLED. Use the **Network Diagnostic Probe** in Energy mode to inspect/cycle the exact face.

## Grid Capacitor

The **Grid Capacitor** is the high-throughput buffer for larger installations. It stores **16,000,000 FE** and moves up to **32,768 FE/t**. Put it between a large generator/reactor feed and bursty consumers to absorb load spikes rather than forcing every downstream machine to rely on the tiny buffer of an individual cable.

Comparator output reports capacitor fill level.

## Energy Bank

The **Energy Bank** is a late-game dual-resource buffer for reactor installations. It stores **32,000,000 FE** and **1,000,000 Matter** independently, accepts both through its exposed capabilities, and supplies adjacent FE networks plus connected Matter Pipe/Hybrid Conduit networks when those networks have room. It does not convert FE into Matter or Matter into FE. Use the block for reactor output that should remain available after the reactor stops or consumers temporarily back up.

## Charging Station

The **Charging Station** remains the local bridge between stationary FE generation and compatible portable Matter Overdrive energy storage.

For Android bases, the 0.7 release adds an **Android Induction Relay**. It stores **2,000,000 FE**, accepts up to **16,384 FE/t**, wirelessly supplies up to **8,192 FE/t**, and supports **32 / 64 / 96 block** same-dimension charging ranges. It does not create FE and does not charge across dimensions.

Every successful Android charge debits the relay's stored FE. Adjacent FE sources are also checked against their configured output face before power is pulled, so a disabled or input-only source side cannot leak power into the relay.

## Quantum Power Relay

A pair/group of **Quantum Power Relays** can move FE wirelessly between loaded same-dimension locations. Relays use channels **0-15**, have a **256 block** wireless range, store **1,000,000 FE**, and transfer up to **16,384 FE/t**.

Only loaded peers participate. Relays do not force-load destinations. The implementation keeps a registry of loaded relays rather than scanning the entire 256-block world volume every tick.

Adjacent relay transfers respect the target machine's configured input face and the source machine's configured output face. Transfers are simulated before extraction and refund any unaccepted FE, preventing silent energy loss when a side policy changes.

## Machine upgrades

Matter Overdrive machines support Speed, Power, Fail-Safe, Range, Power Storage, Hyper Speed and Matter Storage where appropriate. The 0.7 release also adds **Parallel Processing** for the Decomposer and Replicator.

Do not assume that upgrades are free throughput. Faster or parallel processing can raise instantaneous FE/Matter demand. If a machine becomes inconsistent after upgrading it, compare its supply rate against the new workload.

**Power Storage** increases supported machine buffers. **Matter Storage** performs the equivalent role for Matter-capable machines. **Range** affects machines with a real spatial operating radius. **Parallel Processing** adds additional simultaneous work lanes rather than simply shortening one progress bar.

## Per-side configuration

Supported machines persist six independent face policies for FE, Matter and Items. Each face can be **INPUT**, **OUTPUT**, **BOTH** or **DISABLED** for the relevant capability.

This is functional backend state, not just GUI metadata. Energy Cable, Matter Pipe and item automation should obey the configured face they actually touch.

## Common machine chain

A practical Matter-processing layout is:

1. Solar Panel or another FE source.
2. Grid Capacitor when the installation needs burst capacity.
3. Heavy Energy Cable distribution.
4. Charging Station / Android Induction Relay for portable or Android charging.
5. Matter Recycler/Decomposer for Matter production.
6. Matter Analyzer for patterns.
7. Pattern Storage/Monitor for networked pattern handling.
8. Replicator for manufacturing.
9. Quantum Power Relay for remote loaded facilities when cabling is impractical.

FE uses energy infrastructure, Matter uses Matter Transport Pipe, and logical replication tasks use Matter Network Cable.

## Solar and reactor power are different scales

Solar Panels are appropriate for low-throughput early setups. The **Fusion Reactor** is a late-game source with a much larger internal storage/output model and a multiblock/anomaly requirement.

The reactor ring can distribute FE internally to supported ring components. External machines should be fed through **Reactor IO** and ordinary energy transport. A Grid Capacitor is useful immediately outside the reactor when downstream loads are highly variable.

## Debugging power problems

If a machine is not running, check these in order:

1. Does its GUI report stored FE?
2. Is the touched FE face configured to accept input?
3. Does the machine have the other resource it needs, such as Matter or an input item?
4. Is the recipe/pattern valid?
5. Does the adjacent cable/capacitor/relay contain FE?
6. Does the previous source contain FE?
7. Did speed or parallel upgrades raise demand above supply?

A machine with a full FE bar that still does nothing usually has a recipe, matter, pattern, output-space or state problem rather than a generation problem.

For the experimental Grid Capacitor, wireless relays, Matter Matrix and facility monitoring stack, see [Advanced Infrastructure](advanced_infrastructure.md).
