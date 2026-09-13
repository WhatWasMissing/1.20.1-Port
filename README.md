# Matter Overdrive 1.20.1 Port

A Forge 1.20.1 port of Matter Overdrive Legacy Edition, maintained by MVQ1303.

## Current release

- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17
- Mod ID: `matteroverdrive`
- Current version: `0.7`
- Development baseline: `main`

Release builds are published from `main` after the normal build and release workflows complete successfully.

## Running locally

After pulling `main`:

1. Run `VERIFY_M2_BUILD.bat` for the normal source and build checks.
2. Run `RUN_M2_CLIENT.bat` to launch the development client.
3. Follow [`docs/testing/TO_TEST.md`](docs/testing/TO_TEST.md) for the current runtime test pass.
4. Use `PACKAGE_TEST_JAR.bat` when you want a local tester JAR under `build/libs`.

### Full setup and PDA voice bank

For a clean checkout/build walkthrough plus the complete local neural-voice workflow, use:

- **[Setup and PDA Voice Bank Guide](docs/SETUP_AND_PDA_VOICE_GUIDE.md)** — Java/Forge setup, validation, Gradle build, voice-queue export, neural source naming, ICARUS reference handling, synthetic post-processing, config overrides, JAR bundling/injection and runtime smoke testing.

The prerecorded PDA bank is optional at runtime. Missing recordings fall back to local OS speech, then Minecraft Narrator, while captions/text remain available.

## Current feature set

Matter Overdrive 0.7 consolidates the 0.6 campaign/world-content line with the merged tech overhaul, dual-resource Energy Bank, expanded hostile/village population, deterministic loot relics, dormant Frontier archive definitions, player-event lore routing, PDA/GuideME audit coverage and the complete native Destiny GunPack weapon set.

For the current post-Star-Map source inventory, including the newer PDA/lore/dialogue/structure/voice systems, see **[Working Features — 0.7](docs/reference/WORKING_FEATURES.md)**. The dated 0.6 inventory is retained as historical context.

### Matter economy and diagnostics

- Recursive recipe-derived matter values for craftable items.
- Crafted outputs are valued from the subtotal of their resolved ingredients, accounting for recipe output count.
- Explicit legacy/material values remain authoritative base values.
- Tag-based material values support logs, planks, leaves, wool and saplings.
- Conservative fallback valuation prevents ordinary obtainable items from silently resolving to zero matter.
- Matter Analyzer and Decomposer use the same level-aware resolver, keeping newly analysed patterns and decomposition yields consistent.
- Item tooltips show resolved matter values; hold Shift to see whether the source is explicit, tag-based, recipe-derived, dynamic or fallback.
- Startup audit reports registry coverage by matter-value source.
- `/matteroverdrive matter value` reports the held item value and source.
- Operators can use `/matteroverdrive matter audit` and `/matteroverdrive matter clearcache` for economy verification.
- Detailed resolver and test notes live in [`docs/reference/MATTER_VALUE_COVERAGE.md`](docs/reference/MATTER_VALUE_COVERAGE.md).

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
- Selectable Passive Protocols with combat, mobility, defense, energy and drone benefits.
- Android Mastery progression and permanent perks.
- Drone Matrix progression through level 10.
- Android HUD, keybinds, cooldown synchronization and loadout UI.
- Persistent progression and loadout data.

### Weapons and combat

- Energy weapons, weapon batteries and weapon modules.
- Weapon heat and reload behavior.
- Weapon Station persistence and typed module slots.
- Native weapon integration alongside Point Blank compatibility support.
- Android combat hooks for subclass abilities, passives and drone support.
- 49 native Destiny energy-weapon profiles, including 35 additional GunPack conversions with imported geometry, textures, source audio, available animations and display transforms.
- Destiny weapons use the existing battery, barrel, sights, colour, utility and Weapon Station module paths.
- First-person source transforms, bounded ADS/recoil presentation and third-person generic-use pose suppression.

### 0.7 tech overhaul and exploration

- Energy Bank dual FE/Matter storage fed by reactor IO and exposed to compatible FE/Matter networks.
- Quantum Flux Reactor, Environmental Regulator, Wall Terminal, reactor/decorative blocks and hybrid FE+Matter conduit routing.
- Frontier Expedition archive definitions: Deep Matter Vault, Autonomous Drone Foundry, Anomaly Quarantine Site and Orbital Recovery Array; unfinished MO-authored sites are not naturally placed in new worlds.
- Natural hostile spawning for Rogue Androids, Ranged Rogue Androids, Drones, Mutant Scientists, Assimilators and Phase Stalkers.
- Village-associated Field Scientists, Systems Engineers and Mad Scientists with PDA contacts and existing contract flow.
- Matter Overdrive supplies injected into selected vanilla structure loot while preserving vanilla pools, plus seven deterministic Legendary Relic sources and themed facility relic pools.
- GuideME block-reference image/recipe coverage, PDA integration checks and expanded source/resource consistency validators.

### Campaign, quests and world content

- Mad Scientist Android conversion flow.
- Puny Humans progression.
- Cocktail of Ascension progression.
- Legacy contract and conversation support.
- Active quest tracker HUD for current contracts and special quest states.
- Transporter and bound Transport Flash Drives.
- Survival/world-structure compatibility work consolidated into the release baseline, including dormant structure serializers, vanilla loot integration and player-event lore in place of unfinished MO-authored natural generation.

### Materials and equipment

- Tritanium and Dilithium world materials.
- Correct pickaxe mining behavior for Matter Overdrive ores.
- Correct ore drop behavior when mined with an appropriate tool.
- Tritanium tools and armour.

### Guides and integration

- In-game guide content for implemented and partially implemented systems.
- Paged guides with index navigation and last-page persistence.
- GuideME integration content for Matter Overdrive systems.
- JEI development and runtime integration support.
- Player and tester documentation under `docs/`.

## Testing status

The port is still under active testing. A system being listed above means it has an implementation in the current source tree, not that every interaction or balance value is final.

For the most useful current references, see:

- [`docs/reference/CURRENT_FEATURES_2026-09-11.md`](docs/reference/CURRENT_FEATURES_2026-09-11.md)
- [`docs/SETUP_AND_PDA_VOICE_GUIDE.md`](docs/SETUP_AND_PDA_VOICE_GUIDE.md)
- [`docs/testing/TO_TEST.md`](docs/testing/TO_TEST.md)
- [`docs/reference/WORKING_FEATURES.md`](docs/reference/WORKING_FEATURES.md)
- [`docs/reference/MATTER_VALUE_COVERAGE.md`](docs/reference/MATTER_VALUE_COVERAGE.md)
- [`docs/README.md`](docs/README.md)

## Repository layout

- `src/` contains Forge mod source and resources.
- `docs/` contains progress notes, testing plans, porting references, handoffs and feature documentation.
- `.github/` contains CI and release workflows.
- Root `.bat` files provide local verification, runtime and packaging entry points.
- Gradle files remain at the repository root as expected by the project toolchain.

## Credits

Matter Overdrive was originally created by its respective original developers and contributors. This repository is a community port targeting Minecraft Forge 1.20.1 and is maintained by MVQ1303.
