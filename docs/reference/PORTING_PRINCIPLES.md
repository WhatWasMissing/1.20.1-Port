# Matter Overdrive 1.20.1 Porting Principles

## Core rule

Preserve **legacy identity and functionality**, but prefer **modern 1.20.1 execution** when the old implementation was limited by its Minecraft/Forge era rather than by deliberate game design.

## Preserve from 1.7 / 1.12

- Distinct machine/system roles and progression.
- Recovered capacities, timings, upgrade semantics and interaction rules when they define the original system.
- Recognisable structure footprints, offsets, machine palettes, loot themes and occupants.
- Weapon identities, module behaviour, aiming characteristics and visual language.
- Android, Drone, Reactor, Transporter and Matter Network gameplay concepts.
- Characteristic Matter Overdrive presentation and terminology.

## Modernise for 1.20.1

- Persistence and server authority instead of fragile client/local state.
- Clearer operator GUIs and telemetry while keeping every real slot/control available.
- Modern FE/capability interoperability where it does not erase Matter Overdrive-specific behaviour.
- Collision-safe 3D Drone flight rather than legacy ground/pathing workarounds.
- Terrain-aware structure placement rather than blindly stamping templates into unsuitable terrain.
- Better save/reload behaviour, migration handling and duplicate prevention.
- Useful QoL such as remote views, persistent overlays, indexes, debug/test views and explicit state feedback.
- Sensible balancing adjustments where old values are clearly inappropriate for the current port, while documenting deviations.

## Avoid

- Reintroducing legacy bugs or technical limitations solely for parity.
- Fake GUI controls for backends that do not exist.
- Replacing improved 1.20.1 systems with weaker legacy implementations unless the modern behaviour changes the intended gameplay identity.
- Calling translated/generated structures pixel-perfect legacy copies unless they are actually reconstructed from the source templates.
- Silent parity deviations: intentional modernisations should be documented and testable.

## Decision order

When old and current behaviour conflict, prefer:

1. Matter Overdrive's intended gameplay identity.
2. Source-backed common functionality between the 1.7 and 1.12 references.
3. Existing modern behaviour when it is safer, clearer or more robust without changing that identity.
4. A documented modern extension where neither legacy version provides a suitable implementation.
