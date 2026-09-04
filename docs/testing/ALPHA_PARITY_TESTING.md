# Alpha parity pass

Branch: testing/alpha

This branch is isolated from testing/main. It is a reference-driven parity pass against the supplied Matter Overdrive 1.12.2 jar and the official Matter Overdrive documentation.

## What the reference establishes

- Legacy item JSON for weapons was only the inventory icon. First-person position, recoil, zoom, the hand, and visible attachment models came from dedicated client renderers.
- The original renderer mounted only physical optics. Colour, barrel, battery, and utility modules altered weapon behaviour but were not rendered as loose icons.
- Android baseline behaviour includes no hunger, water breathing, immunity to harmful effects, sinking in water, and visual damage glitching. Androids still require power.
- Matter-network work is task based: analyzers broadcast patterns, pattern monitors request replication quotas, and routers/switches distribute tasks.
- Fusion reactors are built around an anomaly, use plasma matter, and scale output with anomaly mass. The port already contains the tested structure/network foundation; major balance changes require in-game measurements.
- The original Star Map was itself documented as incomplete, so it is not being presented as a completed parity target.

## Implemented in this alpha branch

- Android baseline traits now refresh while a converted Android has FE:
  - hunger is maintained;
  - air is restored under water;
  - harmful potion effects are cleared;
  - the player sinks while in water unless flying;
  - damage produces a lightweight electrical glitch pulse.
- Android Station now exposes core level, current XP, unspent perk choices and selected ability alongside its existing body-part and charge status.
- Weapon optics now follow the old renderer's design:
  - only Holo Sights and Sniper Scope are candidate visual attachments;
  - legacy per-weapon mount positions are used for Ion Sniper, Phaser Rifle, and Plasma Shotgun;
  - gameplay-only modules no longer try to render as floating icons.

## Next implementation targets

1. Replace the interim item-transform gun presentation with a true first-person renderer that owns recoil, zoom and hand pose.
2. Make scope and holo sight rendering use their original OBJ assets rather than a flat item fallback.
3. Expand Android Station presentation to expose ability selection, core status and progression in one screen.
4. Audit machine task pages and network status against the legacy queue workflow.
5. Tune reactor matter-to-FE balance only after recording current anomaly-mass test values.

## Test focus

- Convert to Android, power the core, then test hunger, water, a harmful potion, and incoming damage.
- Open Android Station at normal and large GUI scale: verify the added progression status does not overlap the player inventory.
- Test an Ion Sniper with each optic installed in first and third person.
- Confirm barrel, colour, battery and utility modules retain their gameplay effects and do not create floating visuals.
- Confirm the existing testing/main branch is unchanged.
