# Matter Overdrive 1.20.1 — Working Features Reference

This is the current functional-feature inventory for the `feature/fusion-reactor` test build. It distinguishes usable gameplay systems from registry/resource shells that are present only to preserve the legacy mod's IDs and assets.

## Installing the test JAR

1. Close Minecraft completely.
2. Run `PACKAGE_TEST_JAR.bat` from the port project.
3. Copy `build\\libs\\matteroverdrive-0.8.0.0-alpha.4.1.jar` into the **mods** folder of a Forge 1.20.1 instance:
   - Official launcher: `%AppData%\\.minecraft\\mods`
   - CurseForge/Prism/other launcher: that instance's own `mods` folder.
4. Remove any older Matter Overdrive 1.20.1 test JAR from that same mods folder, so only one copy is loaded.
5. Start the Forge profile and test in a new world or a backed-up test world.

The JAR is for a normal modded Minecraft installation. Keep the source project separately for continued development.

## Functional machines and storage

| Feature | Working behaviour |
|---|---|
| Matter Decomposer | Converts supported items into matter, consumes FE, supports upgrades, and exposes debug information including failure chance. |
| Matter Recycler | Recycles supported items into matter with energy usage, inventory handling, upgrades, and debug values. |
| Matter Analyzer | Analyses supported items/patterns, uses FE, works with Pattern Storage/Monitor network links, and has debug information. |
| Matter Replicator | Replicates queued patterns using matter and FE; reports cycle/failure data in its debug UI. |
| Pattern Storage | Stores Pattern Drives, accepts supported upgrades, provides pattern capacity/energy debug values, and participates in the pattern network. |
| Pattern Monitor | Discovers Pattern Storage, queues replication work, reports linked storage/pattern counts, and sends work to a Replicator. |
| Molecular Inscriber | Produces Mk2, Mk3, and Mk4 Isolinear Circuits; has an energy-item battery slot, upgrades, persistence, and recipe/cycle debug values. |
| Solar Panel | Produces FE in suitable daylight, has 64,000 FE base storage, exports up to 512 FE/t per side, supports Power Storage upgrades, and reports daylight/generation/debug state. |
| Tritanium Crates | All 17 variants work as portable 54-slot inventories, retain contents in their dropped item NBT, and have a crate GUI. |
| Transporter | Uses a bound Transport Flash Drive and power to move entities above it to a target in the same dimension. It supports Speed, Range, Power, and Power Storage upgrades plus cycle/cost/range debug data. |

## Matter, energy, patterns, and upgrades

| Feature | Working behaviour |
|---|---|
| Matter Container | Stores up to 1,000 kM, transfers matter to/from compatible machines, updates immediately, and reports transfer debug messages. |
| Matter network | Matter Pipes expose matter transport for compatible machines; Heavy Matter Pipe is now reserved for FE transport as the Heavy Energy Cable. |
| Pattern network | Network Pipes, Routers, and Switches provide the current pattern/task transport and routing behaviour for Pattern Storage, Monitor, and Replicator workflows. |
| Creative Battery | Provides infinite FE for machine testing. |
| Pattern Drive | Holds two normal patterns; Creative Pattern Drive operates as the creative variant. |
| Upgrades | Working machines expose their supported slots and show temporary debug output for effective values. Common effects include speed, power cost, failure chance, range, power storage, matter storage, and matter usage. |

## Inscriber recipe chain

| Input | Additional material | Output |
|---|---|---|
| Isolinear Circuit Mk1 | Gold ingot | Isolinear Circuit Mk2 |
| Isolinear Circuit Mk2 | Diamond | Isolinear Circuit Mk3 |
| Isolinear Circuit Mk3 | Emerald | Isolinear Circuit Mk4 |

## Fusion Reactor and power distribution

### Compact Fusion Reactor structure

Build the following:

- Fusion Reactor Controller in the centre.
- Fusion Reactor Coil directly north, south, east, and west.
- Fusion Reactor IO directly above the Controller.
- Gravitational Anomaly within three blocks of the Controller.

The Controller accepts Matter Container transfers when used directly. Its debug UI reports structure validity, exact fault reason, FE/matter, anomaly distance, efficiency, output, and matter drain.

| Reactor value | Current behaviour |
|---|---|
| FE buffer | 100,000,000 FE base capacity |
| Matter buffer | 2,048 kM base capacity |
| Maximum base generation | 2,048 FE/t with a close anomaly |
| Base matter drain | 0.0125 kM/t |
| Reactor upgrades | Speed, Range, Power Storage, Matter Storage |
| Range safety | Anomaly scanning is capped at 16 blocks, preventing prior server-freezing scans |
| Reactor IO | Exports up to 512 FE/t to each adjacent receiving side, except the Controller beneath it |

### Heavy Energy Cable

The existing **Heavy Matter Pipe** item/block is now named **Heavy Energy Cable** for the test build.

- It buffers 8,192 FE.
- It relays up to 1,024 FE/t to each receiving side.
- Multiple cables can be chained.
- Right-click a cable for debug information: stored FE, latest output, and relay behaviour.
- Use it as: `Reactor IO → Heavy Energy Cable(s) → powered machine`.
- Matter Pipe and Network Pipe do **not** carry Forge Energy.

## Verified runtime checks

The test scripts validate:

- Registry/resource counts and active JSON resources.
- Functional M2 source wiring and legacy baseline constants.
- No Matter Overdrive missing-texture warnings.
- No Matter Overdrive runtime-failure marker.
- No Matter Overdrive data-pack tag failures.
- Current runtime marker: 13 block entities and 12 menus, including Fusion Reactor and Heavy Energy Cable.

## Present but not yet functional

Many legacy blocks, items, sounds, and resources are registered so worlds/assets remain complete. They are not automatically gameplay-complete just because they appear in the creative tab. In particular, the large legacy Fusion Reactor exterior/rendering and Gravitational Stabilizer behaviour are still future work.
