# Matter Overdrive 1.20.1 Port

Work-in-progress Forge 1.20.1 source port of Matter Overdrive Legacy Edition.

## Target

- Minecraft 1.20.1
- Forge 47.4.10
- Java 17
- Mod ID: `matteroverdrive`
- Current port version: `0.8.0.0-alpha.4.1`

## Current branch testing

The current parity branch is `feature/easy-parity-systems`.

After pulling it locally:

1. Run `VERIFY_M2_BUILD.bat` for the normal source/build gate.
2. Run `RUN_M2_CLIENT.bat` to launch the development client.
3. Follow [`docs/testing/TO_TEST.md`](docs/testing/TO_TEST.md) for the current branch test pass.
4. Use `PACKAGE_TEST_JAR.bat` when you want a tester JAR under `build/libs`.

## Current functional areas

The port now includes substantial functional implementations for:

- Matter Decomposer, Recycler, Analyzer, Replicator, Pattern Storage and Pattern Monitor.
- Matter/network transport and replication routing.
- Molecular Inscriber production.
- Transporter and bound Transport Flash Drives.
- Fusion Reactor, Reactor IO, Heavy Energy Cables, Gravitational Anomaly and Stabilizers.
- Machine upgrades and debug instrumentation.
- Tritanium tools and armour.
- Energy weapons, weapon batteries/modules, firing behavior and Weapon Station persistence.
- Current parity branch: Weapon Station module installation/uninstallation and typed module slots.

Android systems, Star Maps, advanced entity/world content, dedicated legacy weapon rendering, and other larger legacy systems remain later parity work.

## Repository layout

- `src/` — Forge mod source and resources.
- `docs/` — project documentation, grouped by purpose.
  - `docs/progress/` — feature implementation progress.
  - `docs/testing/` — current and historical runtime test plans.
  - `docs/porting/` — port status, inventory, references and chronological progress.
  - `docs/reference/` — tester/player feature references.
  - `docs/history/` — older milestone notes and changelogs.
- `.github/` — CI workflows.
- Root `.bat` files — local run, verification, runtime check and packaging entry points.
- Gradle files remain at root as expected by the project toolchain.

See [`docs/README.md`](docs/README.md) for the documentation index and [`docs/reference/WORKING_FEATURES.md`](docs/reference/WORKING_FEATURES.md) for the broader working-feature reference.
