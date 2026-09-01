# Current Features and Roadmap

Status: `main`, updated 2026-09-01.

This is the current playable snapshot of the 1.20.1 port. “Implemented” means it is present in the current code/build; it does not replace the focused in-game checks in [TO_TEST.md](../testing/TO_TEST.md).

## Implemented systems

- Matter machines: Decomposer, Recycler, Analyzer, Replicator, Molecular Inscriber, Pattern Storage and Pattern Monitor.
- Matter and energy transport: Matter Pipe, Heavy Matter Pipe, Energy Pipe, Heavy Energy Cable, and Fusion Reactor IO.
- Fusion Reactor: structure guide and overlay, reactor controller/IO, anomaly interaction, matter input routes, power output, and powered Gravitational Stabilizers.
- Gravitational anomaly: item consumption and visible living-entity mass contribution; anomaly size is capped.
- Gravitational Stabilizer: targeting/beam checks, reactor-FE requirement, four Power Upgrade slots, GUI telemetry, and no redstone activation.
- Charging Station: GUI, one rechargeable-item slot, buffered FE input, gradual charging, persistence, and recovery of stored items when broken.
- Weapons: Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun, Weapon Station module editing, energy checks, battery reloads, and compatible module validation.
- Storage and utility: Tritanium Crate, Transporter, Solar Panel, Reactor Assembly Guide, and the existing diagnostic/UI tooling.
- Resources and presentation: repaired transparent-block rendering, restored crate/Inscriber models, several corrected item textures, and recipes for all 72 placeable blocks. Technical no-item blocks remain intentionally uncraftable: Bounding Box, Matter Plasma, and Molten Tritanium.

## Current verification targets

- The four energy-weapon hand transforms still need visual tuning in first- and third-person; treat them as an open runtime issue.
- Confirm anomaly feeding in a fresh test world: an item or entity entering the horizon must visibly increase mass.
- Confirm Charging Station and Stabilizer FE routes using a live Fusion Reactor, Reactor IO, and cable connection.
- Run the normal client/build gate after pulling main, then use the focused checklist in [TO_TEST.md](../testing/TO_TEST.md).

## Original-mod implementation order

1. **Android core** — Android Station GUI and workflow, Android upgrades/abilities, power/charging integration, then Android Spawner.
2. **Matter Network** — make Network Pipe, Router, Switch, and Pylon form usable networks with discoverable endpoints and a clear GUI.
3. **Progression content** — Contract Market, contracts, Star Map, and their rewards/requirements.
4. **Advanced technology** — Microwave and Spacetime Accelerator, using the finished energy/matter network as their foundation.
5. **Refinement pass** — resolve weapon transforms, complete texture/model review, balance reactor/anomaly values, and broaden survival-playthrough testing.

The recommended next slice is **Android Station + Android power/charging**, because it restores one of Matter Overdrive’s defining gameplay loops and reuses the newly completed charging and energy systems.
