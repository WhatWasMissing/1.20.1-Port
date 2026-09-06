# Beta 1 late-game survival, structure and optional-integration audit

Base: `testing/main` at `2ab06166064c36d6496872bddcaeea0cb364fbd1`.

## Survival progression restored

The 1.12.2 reference JAR contains recipes for registered late-game items that were missing from the 1.20.1 recipe directory. This pass restores 1.20.1 equivalents while preserving the existing Inscriber ladder: Mk1 + gold -> Mk2, Mk2 + diamond -> Mk3, Mk3 + emerald -> Mk4.

Restored recipe families in this pass:
- Integration Matrix, ME Conversion Matrix, Forcefield Emitter, H-Compensator and Spacetime Equalizer.
- Plasma Core, Weapon Handle, Weapon Receiver and Sniper Scope.
- Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun.
- Upgrade Base plus Speed, Power, Fail-Safe, Range, Power Storage, Hyper Speed and Matter Storage upgrades.
- Network Flash Drive.

The recipes preserve the legacy ingredient relationships, translated from old metadata/ore-dictionary ingredients to current 1.20.1 registry IDs and vanilla ingredients.

## Structure acquisition audit

The 1.12.2 building selector used source weights Android House 40, Sand Pit 100, Crashed Ship 75, Underwater Base 30 and Cargo Ship 5. Crashed Ships also enforced 256-block separation; Underwater Bases required deep ocean plus 2048-block separation; Cargo Ships enforced 4096-block separation plus an additional 10% generation roll.

The 1.20.1 port uses independent placed-feature rarity filters plus per-feature location validation, so the legacy weights cannot safely be copied directly without changing world density. Runtime sampling is required before changing acquisition frequency.

Current structure loot provides alternate acquisition for circuits, Integration Matrices, Pattern Drives, Android parts, Upgrade Bases and other progression materials. During fresh-world testing record structures encountered, travel distance/chunks generated, crate contents and whether any required progression item is practically unobtainable.

A remaining structure-identity difference is now documented: legacy Android House generation includes a guaranteed level-3 legendary ranged Rogue Android. The reconstructed house currently uses normal defenders plus an optional Drone. Restore that in a dedicated entity/structure code pass after this recipe/data gate.

## AE2 compatibility gate

AE2 remains capability-level interoperability only. Validate:
- Storage Bus insert/extract on Tritanium Crates.
- Decomposer, Recycler, Inscriber, Replicator and Pattern Storage public handlers.
- Input validity is respected and output extraction cannot duplicate processing state.
- Upgrade inventories are not exposed through the public item capability.
- Partial processing and inventories survive save/reload and chunk unload/reload.
- Replicator output extraction does not alter or duplicate Matter Overdrive network tasks.
- Pattern Drive automation does not corrupt drive NBT.
- No AE-to-FE conversion and no ME/Matter Network merge.

Run the startup matrix: neither optional mod, AE2 only, GuideME only, both.

## GuideME gate

With exact GuideME 20.1.15:
- Data Pad exposes GuideME Manual only when GuideME is present.
- Manual opens `matteroverdrive:guide`; Current Features is linked; all section links/icons render.
- Data Pad contracts and scan history still work.
- Without GuideME, compact fallback pages remain usable and there are no missing-class errors.

## Fresh Survival late-game test

Starting from the existing Replicator gate, obtain/craft without commands:
1. Mk2, Mk3 and Mk4 Isolinear Circuits through the Inscriber.
2. Upgrade Base and every restored machine upgrade.
3. Integration Matrix and higher utility components.
4. Plasma Core, Weapon Handle and Weapon Receiver.
5. Phaser, Phaser Rifle, Plasma Shotgun and Ion Sniper.
6. Fusion Reactor Controller, IO and coils using survival-obtained materials.
7. Save/reload between each tier and confirm no machine inventory, FE, matter or Pattern Drive state is lost.
