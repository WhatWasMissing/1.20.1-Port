---
navigation:
  title: Power and Machines
  parent: index.md
  position: 3
  icon: matteroverdrive:solar_panel
---
# Power and Machines

Matter Overdrive machines use **Forge Energy (FE)**. Early machines can be run from modest generation, while Matter processing, weapons and the Fusion Reactor ecosystem push you toward larger storage and distribution systems.

## Starting power

The **Solar Panel** is the simplest early generator. Its current restored values are:

- up to **8 FE/t peak generation** in suitable daylight;
- **64,000 FE** internal storage;
- up to **512 FE/t per side** output.

Generation follows daylight rather than producing full power at all times. If a panel appears idle, check sky access and time of day before assuming its cable connection is broken.

## Heavy Energy Cable

Heavy Energy Cable is the normal FE transport line. Each cable currently buffers **8,192 FE** and can move up to **1,024 FE/t per side**.

For diagnosis, inspect a network from the source outward. If the first cable charges but the next one does not, that is a transport problem. If every cable is charged but the machine remains empty, check the machine's accepted side/capability and whether its storage is already full.

## Charging Station

The **Charging Station** is the intended bridge between stationary FE generation and compatible portable Matter Overdrive energy storage. Use it for batteries and supported powered items rather than relying on unrelated modded weapons/items as generic batteries.

Android energy and weapon energy are gameplay resources in their own right, so keeping a charged portable reserve is useful even after you have a large base power system.

## Machine upgrades

Matter Overdrive machines support upgrade types such as Speed, Power, Fail-Safe, Range, Power Storage, Hyper Speed and Matter Storage where the specific machine allows them.

Do not assume that a speed upgrade is free throughput. Faster processing generally means the machine can demand FE and/or Matter more quickly. If a machine becomes inconsistent after upgrading it, compare its resource supply rate against the new processing rate.

**Power Storage** upgrades increase how much energy supported machines can buffer. **Matter Storage** upgrades perform the equivalent role for Matter-capable machines. **Range** affects machines that have a meaningful spatial operating radius.

## Common machine chain

A practical early Matter-processing layout is:

1. Solar Panel or another FE source.
2. Heavy Energy Cable distribution.
3. Charging Station for portable reserves.
4. Matter Recycler/Decomposer for Matter production.
5. Matter Analyzer for patterns.
6. Pattern Storage/Monitor when you want networked pattern handling.
7. Replicator for manufacturing.

The machines do not all share one universal resource pipe. FE uses energy cable, Matter uses Matter Transport Pipe, and logical replication tasks use Matter Network Cable.

## Solar and reactor power are different scales

Solar Panels are appropriate for low-throughput early setups. The **Fusion Reactor** is a late-game source with a much larger internal storage/output model and a multiblock/anomaly requirement. It should not be necessary just to operate one basic machine.

The reactor ring can distribute FE internally to supported ring components. External machines should be fed through **Reactor IO** and ordinary energy transport. See [Fusion Reactor](reactor.md) for assembly and telemetry.

## Debugging power problems

If a machine is not running, check these in order:

1. Does its GUI report stored FE?
2. Does the machine have the other resource it needs, such as Matter or an input item?
3. Is the recipe/pattern valid?
4. Does the adjacent cable contain FE?
5. Does the previous cable/source contain FE?
6. Did an upgrade raise demand above supply?

A machine with a full FE bar that still does nothing usually has a recipe, matter, pattern, output-space or state problem rather than a power-generation problem.
