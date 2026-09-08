---
navigation:
  title: Power and Machines
  parent: index.md
  position: 3
  icon: matteroverdrive:solar_panel
---
# Power and Machines

Matter Overdrive machines use Forge Energy (FE). Early progression can run from Solar Panels and portable batteries; late progression can draw from the Fusion Reactor through Reactor IO and Heavy Energy Pipes.

## Early power

The **Solar Panel** is the simplest early generator. Its legacy baseline is 8 FE/t peak generation, 64,000 FE internal storage and up to 512 FE/t output per side. Actual generation follows the panel's daylight calculation, so night, obstruction and poor sky access reduce output.

A Solar Panel can accept supported power-storage upgrades. If a machine does not appear to be charging, first test it directly adjacent to the panel before adding cables or automation.

## Portable power and Charging Station

The **Charging Station** recharges compatible portable FE items. Its battery/inventory, upgrades, FE state and redstone configuration are serialized across save/reload.

Portable energy items can also be used directly in machine energy slots where that machine accepts an extractable FE capability. The machine pulls a bounded amount each tick rather than instantly consuming the whole item.

## Heavy Energy Pipe / Cable

The existing Heavy Matter Pipe identity also provides the current Heavy Energy Pipe relay behavior. Each cable has:

- 8,192 FE local buffer;
- 1,024 FE/t local transfer limit;
- persistent stored FE;
- routed search through connected cable chains.

In 0.6 branched cable networks rotate among reachable receiver branches instead of repeatedly selecting the first discovered endpoint. The routing sequence is persisted, which reduces first-machine monopolisation after reloads.

For Fusion Reactor output, Reactor IO performs the high-level receiver discovery/fair-share handoff while the cables provide the physical external path.

## Machine energy behavior

Most powered Matter Overdrive machines have their own FE buffer and track recent energy usage for reactor demand telemetry. A machine must have enough FE for its current processing tick; having a connected cable does not guarantee that its internal buffer is currently charged.

Important machines include:

- **Decomposer** - FE + input item -> matter, with failure behavior and upgrades.
- **Matter Analyzer** - FE + item + Pattern Drive -> analysis progress.
- **Replicator** - FE + matter + valid pattern -> replicated output.
- **Matter Recycler** - FE-powered recycling path.
- **Molecular Inscriber** - FE-powered legacy circuit recipes.
- **Transporter** - FE-powered entity transport to configured destinations.
- **Space-Time Accelerator** - powered time-acceleration system.
- **Microwave** - powered processing utility.
- **Weapon Station** - persistent weapon/module configuration inventory.
- **Charging Station** - portable FE charging.
- **Gravitational Stabilizer** - continuous powered anomaly suppression.

## Upgrades

Supported upgrade types include combinations of Speed, Range, Power Usage, Power Storage, Matter Storage and Failure Chance depending on the machine. A machine rejects upgrade types it does not support.

Do not assume a Speed upgrade is free throughput: faster operation can increase the rate at which FE/matter is demanded. Power-storage upgrades increase buffer size rather than creating energy. Matter-storage upgrades increase matter capacity where supported.

## Redstone and persistence

Several modernized machines expose a redstone mode and persist it with their block entity. Machine inventories, upgrade inventories, FE, matter and progress are serialized where those systems exist.

For release-quality testing, do more than return to the title screen: put a partially processed item in a machine, note FE/matter/progress, save, quit Minecraft completely, relaunch and compare the state.

## Reactor demand telemetry

The Fusion Reactor's connected-demand value is based on recent real machine usage rather than total empty buffer space. This prevents a large empty machine buffer from appearing as hundreds of thousands of FE/t of current demand.

When diagnosing reactor output, compare three values separately:

- reactor **potential FE/t**;
- reactor **generated FE/t**;
- connected machine **current demand**.

A low demand figure with a full reactor buffer is not a generation failure.

## Troubleshooting

**One cable works but a longer chain does not:** verify every segment is a Heavy Energy Pipe and every destination exposes FE on the connected side. Break/replacement of middle cables is part of the 0.6 regression test.

**Only one machine gets power on a branch:** this should no longer be permanent in 0.6; both Reactor IO and cable routing have fair/rotating behavior. Report a reproducible topology if one endpoint still monopolises output.

**Machine is connected but idle:** inspect its own FE buffer, input/output slots, redstone mode and required matter/pattern state before blaming the cable.

**Battery will not charge:** verify it exposes a receivable FE capability or is a supported Matter Overdrive battery type, and verify the Charging Station itself has FE.

For late-game generation, continue to [Fusion Reactor](reactor.md). For matter transport, see [Matter Technology](matter.md) and [Matter Network](network.md).
