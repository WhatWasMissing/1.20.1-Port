# Project Documentation

The repository root is intentionally kept focused on files needed to build, run, verify, and package the mod locally.

## Current snapshot

Start with [Current Features and Roadmap](progress/CURRENT_FEATURES_AND_ROADMAP.md) for the current `main` feature set, known runtime checks, and the recommended order for restoring more of the original mod.

## Progress

Feature-specific implementation notes live in [`progress/`](progress/):

- `CURRENT_FEATURES_AND_ROADMAP.md` — current implemented systems, open checks, and next original-mod milestones.
- `GUN_SYSTEM_PROGRESS.md` — weapons and Weapon Station work.
- `REACTOR_PROGRESS.md` — Fusion Reactor, anomaly, stabilizer, and power work.
- `MACHINE_DEBUG_PROGRESS.md` — machine debug instrumentation.
- `TOOLS_ARMOUR_PROGRESS.md` — Tritanium tools and armour.

## Testing

Manual test plans and verification notes live in [`testing/`](testing/).

For the current `main` build, start with [`testing/TO_TEST.md`](testing/TO_TEST.md).

## Porting

Port-wide status, inventory, source references, and the chronological implementation record live in [`porting/`](porting/).

## Reference

Player/tester-facing feature references live in [`reference/`](reference/).

- [`WORKING_FEATURES.md`](reference/WORKING_FEATURES.md) is the implemented-versus-missing parity matrix.
- [`SYSTEM_GUIDE.md`](reference/SYSTEM_GUIDE.md) gives simplified and detailed instructions for every currently working system and labels partial systems explicitly.

## History

Older milestone-specific notes and changelogs live in [`history/`](history/).

## Root files intentionally left at repository root

- `README.md`, `BUILDING.md`, `CHANGELOG.md`, `LICENSE`
- Gradle wrapper/build files
- `RUN_*.bat`, `CHECK_*.bat`, `VERIFY_*.bat`, and `PACKAGE_TEST_JAR.bat`

This keeps the commands normally used after a local pull immediately visible while moving development notes out of the way.
