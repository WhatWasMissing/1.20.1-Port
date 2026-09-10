# Structure and Weapon Audit — 2026-09-10

## Audited baseline

Branch: `testing/tech-overhaul`

Audited systems:
- Native modular technology facilities and infrastructure overlays.
- Room bounding boxes, corridor overlap, entrances, loot seeding and Android spawner configuration.
- Energy weapon use animation and third-person rendering path.
- Comparison against `feature/weapon-input-destiny-render` and `feature/weapon-renderer-2.0`.

## Confirmed issue fixed

`EnergyWeaponItem#getUseAnimation` returned `UseAnim.BOW` for every weapon except the phaser. That caused the vanilla draw/use animation to engage while aiming in third person. Energy weapons now return `UseAnim.NONE`; firing and reload behavior remain controlled by the weapon code and custom renderer.

Native Destiny weapons already used `UseAnim.NONE`.

## Structure findings

The facility system is correctly using native `StructurePiece` generation and chunk clipping. The main clarity risks are layout readability rather than a single generation API failure:

- Six facility identities currently share a very similar room grammar, so the entrance, security threshold and reward room are not always visually obvious.
- Android encounter association is encoded in invisible block-entity configuration. The spawner blocks are placed and configured during piece generation, but the player receives little visual indication of which room owns the encounter.
- Several layouts use asymmetric offsets and elevated/subterranean entrances; these need in-game visual inspection with bounding-box and room labels enabled.
- Loot is seeded from facility-specific chest profiles, but the generated container must be checked in-game for every facility/layout combination.
- Damage variants are deterministic per piece and therefore chunk-safe.

## Required runtime audit matrix

For each facility, test layouts 0, 1 and 2:

1. Locate the entrance from outside without breaking blocks.
2. Follow the main route to the core.
3. Confirm every named wing is physically connected.
4. Confirm the security checkpoint is between entrance and restricted rooms.
5. Confirm the expected Android/security spawner is present, configured and produces the associated encounter.
6. Confirm every crate has the intended facility loot table and is not empty due to missing block-entity initialization.
7. Confirm elevated, lowered and subterranean sections have stairs/ladders that connect cleanly.
8. Confirm damaged variants retain a readable route and do not destroy encounter/loot anchors.
9. Confirm generation across chunk borders and after save/reload.

## Next implementation pass

- Add persistent room/encounter signage and debug labels at entrances and security checkpoints.
- Add a structure audit command/report that lists detected facility pieces, room IDs, encounter spawners, configured reserve/ranged values and loot profiles.
- Add layout-specific visual differentiation and route markers.
- Validate and adjust third-person weapon transforms after the custom renderer is tested in-game; the use-pose bug is now removed at the item API level.
