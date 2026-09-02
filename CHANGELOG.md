# Changelog

All notable changes to the Matter Overdrive 1.20.1 port are documented here.

## Unreleased — Android system audit

### Added

- Craftable Matter Overdrive System Guide item with a 28-section bundled manual, simplified and detailed instructions, explicit partial-system labels and troubleshooting routes.
- Sneak-held normal/HC Battery charging for converted Androids at up to 1,024 FE/t.
- Legacy-style 50% movement penalty and HUD warning while the Android core has no FE.
- Collision-safe fallback positions for the simplified Rogue Android Spawner.

### Fixed

- Failed Android actions no longer drain an unaffordable partial FE remainder.
- The Arms damage bonus now applies only to direct melee attacks.
- Sonic Shockwave no longer inherits the Arms melee bonus or extra per-target FE drain.
- Rogue Android spawning no longer consumes 20,000 FE when the world rejects the entity.

### Testing status

Compilation, in-game System Guide rendering/navigation and focused client/dedicated-server Android verification are required before this pass is marked runtime-verified.

## 0.8.0.0-alpha.4.1 — M2 functional systems test build

### Added

- Functional Matter Decomposer, Matter Recycler, Matter Analyzer, Matter Replicator, Pattern Storage, Pattern Monitor, and their network/matter transport links.
- Machine upgrades with persistent debug readouts for the effective values used by each upgraded machine.
- Solar Panel power generation and Tritanium Crates with 54-slot portable inventories across all colour variants.
- Molecular Inscriber production chain, including Mk2–Mk4 Isolinear Circuit recipes and battery-slot support.
- Transporter with bound Transport Flash Drives, entity movement, range/cost/cooldown handling, and debug status.
- Fusion Reactor bundle:
  - Controller, four-coil compact structure, Reactor IO, and Gravitational Anomaly validation.
  - Legacy baseline capacity/output/matter-drain values.
  - Persistent debug UI for structure validity, anomaly distance, efficiency, generation, FE, and matter.
  - Speed, Range, Power Storage, and Matter Storage upgrade support.
- Heavy Energy Cable, using the existing Heavy Matter Pipe block resource:
  - 8,192 FE buffer.
  - Relays up to 1,024 FE/t to each receiving side.
  - Debug UI showing stored energy and latest output.
  - Supports chains: `Reactor IO → Heavy Energy Cable(s) → powered machine`.

### Fixed

- Fusion Reactor screen compilation.
- Fusion Controller shift-click inventory crash.
- Fusion Range upgrades can no longer create a server-freezing anomaly search.
- Fusion Speed upgrades now increase output rather than reducing it.
- Reactor IO output now uses the tested simulation/extract/refund transfer flow.

### Testing status

The M2 machines, inscriber recipes, transporter, Fusion Reactor, and Heavy Energy Cable have been tested in the development client. Run `VERIFY_M2_BUILD.bat` before distributing a build beyond testing.
