# Matter Overdrive 1.20.1 Port

Work-in-progress Forge 1.20.1 source port of Matter Overdrive Legacy Edition.

## Target

- Minecraft 1.20.1
- Forge 47.4.10
- Java 17
- Mod ID `matteroverdrive`
- Current port version `0.8.0.0-alpha.4.1`

## Current main status

`main` contains the merged M2 gameplay foundation, including:

- Matter Decomposer, Recycler, Analyzer and Replicator
- Pattern Storage, Pattern Drives and Pattern Monitor
- Matter and network pipe routing
- Tritanium Crates
- Molecular Inscriber production chain
- Transporter and Transport Flash Drive
- Fusion Reactor, Reactor IO, Gravitational Anomaly and Stabilizers
- Heavy Energy Cable power routing
- machine upgrades and debug readouts
- Tritanium tools and armour
- weapon/gun system foundation

Some legacy systems remain incomplete or intentionally deferred, especially Android gameplay, Star Maps, remaining weapon polish/modules on `main`, selected legacy shell blocks/items, and final visual parity.

## Local development

Start with:

```bat
VERIFY_M2_BUILD.bat
RUN_M2_CLIENT.bat
```

For a test JAR:

```bat
PACKAGE_TEST_JAR.bat
```

The generated JAR is placed under `build\libs`.

## Repository layout

- `src/` - Forge source and resources
- `docs/progress/` - current system progress notes
- `docs/testing/` - verification paths and test checklists
- `docs/porting/` - port inventory/status/history of implementation work
- `docs/reference/` - current working-feature reference
- `docs/history/` - older milestone notes and changelogs
- root `.bat` files - local build, client/server, runtime-check and packaging entry points

See [`docs/README.md`](docs/README.md) for the documentation index and [`docs/reference/WORKING_FEATURES.md`](docs/reference/WORKING_FEATURES.md) for the current functional feature reference.
