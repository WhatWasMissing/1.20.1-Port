# Matter Overdrive 1.20.1 Port

A Forge 1.20.1 port of Matter Overdrive Legacy Edition, maintained by MVQ1303.

## Current release

- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17
- Mod ID: `matteroverdrive`
- Current version: `0.4`
- Development baseline: `main`

Release builds are published from `main` after the normal build and release workflows complete successfully.

## Running locally

After pulling `main`:

1. Run `VERIFY_M2_BUILD.bat` for the normal source and build checks.
2. Run `RUN_M2_CLIENT.bat` to launch the development client.
3. Follow [`docs/testing/TO_TEST.md`](docs/testing/TO_TEST.md) for the current runtime test pass.
4. Use `PACKAGE_TEST_JAR.bat` when you want a local tester JAR under `build/libs`.

## Current feature set

Matter Overdrive 0.4 includes a broad playable implementation of the original mod's major systems, with additional progression and quality of life work for the 1.20.1 port.

### Matter processing and machines

- Matter Decomposer, Recycler, Analyzer, Replicator, Pattern Storage and Pattern Monitor.
- Molecular Inscriber production.
- Matter containers and matter transport routing.
- Replication queues, pattern handling and failure behavior.
- Storage crates and machine persistence.
- Machine upgrades and debug instrumentation.

### Power and reactor systems

- Fusion Reactor structure validation and runtime operation.
- Reactor IO and heavy energy cable transfer.
- Gravitational Anomaly interaction and mass handling.
- Stabilizers with orientation, obstruction and power state checks.
- Reactor network demand reporting and debug controls.
- Shared reactor ring power behavior and stabilizer power support.
- Battery charging and energy storage support.

### Android system

- Android conversion and body part progression.
- Three top level Android classes: Strider, Juggernaut and Architect.
- Three subclasses per class for a full 3 by 3 subclass structure.
- Unique class ability, tech ability and Ultimate kits across the subclass matrix.
- Nine distinct Ultimates.
- Selectable Aspects and Fragments.
- Selectable Passive Protocols with meaningful combat, mobility, defense, energy and drone benefits.
- Android Mastery progression and permanent perks.
- Drone Matrix progression through level 10.
- Android HUD, keybinds, cooldown synchronization and loadout UI.
- Persistent progression and loadout data.

### Weapons and combat

- Energy weapons, weapon batteries and weapon modules.
- Weapon heat and reload behavior.
- Weapon Station persistence and typed module slots.
- Native weapon integration work alongside Point Blank compatibility support.
- Android combat hooks for subclass abilities, passives and drone support.

### Quests and world content

- Mad Scientist Android conversion flow.
- Puny Humans progression.
- Cocktail of Ascension progression.
- Legacy contract and conversation support.
- Active quest tracker HUD for current contracts and special quest states.
- Transporter and bound Transport Flash Drives.

### Materials and equipment

- Tritanium and Dilithium world materials.
- Correct pickaxe mining behavior for Matter Overdrive ores.
- Correct ore drop behavior when mined with an appropriate tool.
- Tritanium tools and armour.

### Guides and integration

- In game guide content for implemented and partially implemented systems.
- Paged guides with index navigation and last page persistence.
- GuideME integration content for Matter Overdrive systems.
- JEI development and runtime integration support.
- Player and tester documentation under `docs/`.

## Testing status

The port is still under active testing. A system being listed above means it has an implementation in the current source tree, not that every interaction or balance value is final.

For the most useful current references, see:

- [`docs/testing/TO_TEST.md`](docs/testing/TO_TEST.md)
- [`docs/reference/WORKING_FEATURES.md`](docs/reference/WORKING_FEATURES.md)
- [`docs/README.md`](docs/README.md)

## Repository layout

- `src/` contains Forge mod source and resources.
- `docs/` contains progress notes, testing plans, porting references, handoffs and feature documentation.
- `.github/` contains CI and release workflows.
- Root `.bat` files provide local verification, runtime and packaging entry points.
- Gradle files remain at the repository root as expected by the project toolchain.

## Credits

Matter Overdrive was originally created by its respective original developers and contributors. This repository is a community port targeting Minecraft Forge 1.20.1 and is maintained by MVQ1303.
