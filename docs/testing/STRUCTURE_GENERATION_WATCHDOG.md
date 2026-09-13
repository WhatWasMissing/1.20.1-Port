# Matter Overdrive 0.6 - Structure Generation Watchdog Test (Historical)

> Superseded for release 0.7: MO-authored structure placement is dormant. Use `docs/testing/TO_TEST.md` to verify that fresh worlds contain no MO-authored structures, and use an older generated save for compatibility checks. The spacing values below are historical design-review data, not active placement configuration.

This historical pass documents the native `Structure` / `StructurePiece` path for the six legacy facilities. In release 0.7 the path is retained for existing-save compatibility but its natural placement is dormant; the old Feature-based multi-chunk placement path remains disabled.

## Historical placement targets

- `matteroverdrive:crashed_ship` - spacing 80, separation 24
- `matteroverdrive:cargo_ship` - spacing 160, separation 48
- `matteroverdrive:underwater_base` - spacing 128, separation 40
- `matteroverdrive:mad_scientist_house` - spacing 96, separation 32
- `matteroverdrive:android_house` - spacing 96, separation 32
- `matteroverdrive:sand_pit` - spacing 80, separation 24

The intentionally conservative spacing is for stability testing. Frequencies can be raised after chunk generation is proven reliable.

## Safety model

The restored structures use `LegacyNativeStructure` and `LegacyNativeStructurePiece`. A structure piece owns the full multi-chunk bounding box, but block placement is clipped to the chunk bounding box supplied by Minecraft. This avoids the old behavior where an ordinary Feature tried to synchronously stamp blocks into neighboring chunks.

The old legacy surface/ocean Feature biome modifiers and the chunk-load crash-debris placement hook must remain disabled during this test pass.

## Watchdog

`ServerStartupWatchdog` now has a continuous runtime heartbeat monitor in addition to the startup and post-login checks.

If the server stops completing ticks for at least five seconds, the log prints:

`M2 CONTINUOUS WATCHDOG: no completed server tick ...`

followed by stack dumps for the server, render, worldgen/ForkJoin and relevant Netty threads. Structure scheduling also logs slow generation-point lookups and logs an explicit `M2 STRUCTURE ERROR` if a native generation-point calculation throws.

A paused integrated-server can intentionally stop ticks, so a watchdog message while sitting in the pause menu is not by itself evidence of a worldgen hang. Reproduce hangs while actively entering/exploring a world.

## Test order

1. Pull `main`, build locally, and install the newly produced JAR.
2. Create a completely new world. Confirm it passes 100% and reaches gameplay.
3. Fly quickly through fresh terrain for several minutes.
4. Run `/locate structure matteroverdrive:crashed_ship`, teleport near the result, and cross the structure's surrounding chunk boundaries.
5. Repeat for `cargo_ship`, `underwater_base`, `mad_scientist_house`, `android_house`, and `sand_pit`.
6. Save, quit, and reload the same world.
7. Watch for blank chunks, chunks that never finish, server `Can't keep up` messages, `M2 STRUCTURE ERROR`, and `M2 CONTINUOUS WATCHDOG` dumps.

If a hang occurs, leave the game frozen for at least 6-10 seconds before closing it so the watchdog has time to emit a stack trace, then provide `latest.log` and `debug.log` from that exact run.

## Current limitation

This historical pass described natural placement scheduling and the existing native structure geometry. Release 0.7 intentionally does not activate that scheduling because the structures are not yet up to the required visual/content standard. Any future placement pass must first complete the missing occupant, loot, decoration and exact-template review.
