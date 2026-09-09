# Project Documentation

The repository root is intentionally kept focused on files needed to build, run, verify, and package the mod locally.

## Current snapshot

Start with [Current Features and Roadmap](progress/CURRENT_FEATURES_AND_ROADMAP.md) for the release-line feature set. Development work on `testing/tech-overhaul` has additional structure/network/runtime gates that are documented below before they are promoted to `main`.

## Progress

Feature-specific implementation notes live in [`progress/`](progress/):

- `CURRENT_FEATURES_AND_ROADMAP.md` — implemented systems, open checks, and next original-mod milestones.
- `GUN_SYSTEM_PROGRESS.md` — weapons and Weapon Station work.
- `REACTOR_PROGRESS.md` — Fusion Reactor, anomaly, stabilizer, and power work.
- `MACHINE_DEBUG_PROGRESS.md` — machine debug instrumentation.
- `TOOLS_ARMOUR_PROGRESS.md` — Tritanium tools and armour.

## Testing

Manual test plans and verification notes live in [`testing/`](testing/).

For `testing/tech-overhaul`, start with [`testing/TO_TEST.md`](testing/TO_TEST.md), then use the facility-specific matrices when testing fresh structures.

Structure QA tooling is documented in [`STRUCTURE_QA_V2.md`](STRUCTURE_QA_V2.md). It covers the 18-layout player-reachability lab, terrain/site-integration pieces, and the limits of static verification.

## Porting

Port-wide status, inventory, source references, and the chronological implementation record live in [`porting/`](porting/).

The root `PORT_INVENTORY.json` is historical reference material rather than an active-registry manifest; see [`reference/PORT_INVENTORY_SCOPE.md`](reference/PORT_INVENTORY_SCOPE.md).

## Reference

Player/tester-facing and development references live in [`reference/`](reference/).

- [`WORKING_FEATURES.md`](reference/WORKING_FEATURES.md) is the implemented-versus-missing parity matrix.
- [`SYSTEM_GUIDE.md`](reference/SYSTEM_GUIDE.md) gives simplified and detailed instructions for every currently working system and labels partial systems explicitly.
- [`PORT_CONSISTENCY_AUDIT.md`](reference/PORT_CONSISTENCY_AUDIT.md) documents the automated registry/resource/model/texture/GuideME/worldgen consistency gate.
- [`PORT_INVENTORY_SCOPE.md`](reference/PORT_INVENTORY_SCOPE.md) explains why legacy inventory entries, especially the retired Star Map, must not be treated as active registration requirements.

## History

Older milestone-specific notes and changelogs live in [`history/`](history/).

## Root files intentionally left at repository root

- `README.md`, `BUILDING.md`, `CHANGELOG.md`, `LICENSE`
- Gradle wrapper/build files
- `RUN_*.bat`, `CHECK_*.bat`, `VERIFY_*.bat`, and `PACKAGE_TEST_JAR.bat`

This keeps the commands normally used after a local pull immediately visible while moving development notes out of the way.
