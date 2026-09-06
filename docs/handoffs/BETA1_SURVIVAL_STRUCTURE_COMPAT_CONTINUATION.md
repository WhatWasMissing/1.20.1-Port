# Beta 1 survival + structures + AE2/GuideME continuation handoff

Branch target: `testing/main`
Base head: `2ab06166064c36d6496872bddcaeea0cb364fbd1`
References: Matter Overdrive 1.7.10 0.4.2, Matter Overdrive 1.12.2 0.7.1.0, AE2 15.4.10 and GuideME 20.1.15.

## Pass scope

Continued the requested Beta tracks: late-game survival reachability, structure parity/acquisition and optional AE2/GuideME compatibility.

## Survival findings and changes

The port registered a number of late-game components, weapons and machine upgrades but had no normal recipe JSON for them. The 1.12.2 reference JAR does contain source recipes for these items. Restored modern equivalents for the source-backed component, weapon and upgrade chains while keeping higher Isolinear Circuits gated behind the existing Inscriber progression.

Restored: Integration Matrix, ME Conversion Matrix, Forcefield Emitter, H-Compensator, Spacetime Equalizer, Plasma Core, Weapon Handle, Weapon Receiver, Sniper Scope, Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun, Network Flash Drive, Upgrade Base and all seven currently registered machine upgrades.

This closes the largest static recipe-reachability gap after the Replicator without inventing direct shortcuts or changing working machine backends.

## Structure findings

The port already reconstructs the six major structure families and populates structure crates. The 1.12.2 generator selected its main building families with weights Android House 40 / Sand Pit 100 / Crashed Ship 75 / Underwater Base 30 / Cargo Ship 5, but Crashed Ship, Underwater Base and Cargo Ship also used substantial separation/location rules. Do not replace the current 1.20.1 placed-feature rarity values with those weights directly; fresh-world sampling is the safe next gate.

Source parity gap found: the legacy Android House always creates a level-3 legendary ranged Rogue Android in addition to normal defenders. The reconstructed 1.20.1 house currently has normal defenders plus an optional Drone. Restore that in the next entity/structure code pass after the current data/compile gate.

## AE2 / GuideME

No hard dependency or merged network was introduced. Continue to use Forge capabilities for AE2 automation and the reflection bridge for GuideME. The expanded test audit now explicitly requires slot-validity, partial-process, chunk-reload, Pattern Drive NBT and four-way startup-matrix checks.

## Runtime gates

1. Fresh Survival: progress from ores through first FE/matter, Replicator, Inscriber Mk2/Mk3/Mk4, restored components/upgrades, all four energy weapons and reactor construction.
2. Structures: sample fresh chunks, verify layouts, inhabitants, crate persistence and acquisition pacing.
3. AE2: automate every audited public handler through unload/reload with no loss/duplication and no upgrade-slot exposure.
4. GuideME: navigate every page with exact 20.1.15, including Current Features, and verify absent-mod fallback.
5. Startup: neither optional mod / AE2 only / GuideME only / both.

See `docs/testing/BETA1_LATE_GAME_STRUCTURE_COMPAT_AUDIT.md`.
