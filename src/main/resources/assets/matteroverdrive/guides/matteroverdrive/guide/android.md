---
navigation:
  title: Android System
  parent: index.md
  position: 4
  icon: matteroverdrive:android_station
---
# Android System

The Android Station manages conversion and Android progression. Androids have persistent progression, body systems, energy and a selectable skill tree. The Android HUD reports relevant state in play, while the class matrix and skill-tree interfaces are used to configure progression choices.

## Subclass abilities

Every subclass equips three dedicated actions in addition to the legacy core ability cycle:

- **H** - class ability. Lower-cost movement, defence, sustain or close-combat action.
- **N** - tech ability. Stronger battlefield-control, damage, marking or fleet-support action.
- **G** - subclass ultimate. High-impact ability with the largest FE commitment and cooldown.

The Class Matrix inspection panel reports the exact FE cost and cooldown for H and N, and their descriptions now state concrete ranges, durations, damage, healing and effect levels. G ultimates also show their FE cost and cooldown in the inspection panel.

The in-world Android HUD displays the currently equipped **H / N / G ability names** at all times, together with **READY**, remaining cooldown, level lock, or the FE requirement. This makes the active subclass kit visible without reopening the Class Matrix.

## Drone fleets

Linked drones recognise drones belonging to the same operator, and allied operators, as friendlies. Friendly drones are excluded from target selection and friendly drone damage is rejected as a second safety layer.

Following drones use stable escort formation slots behind and beside their operator rather than all steering toward the same point. Fleet separation keeps nearby drones apart, and long-distance catch-up only repositions a drone into a collision-free slot that is not already occupied by another fleet drone.

Android features are server-authoritative where gameplay state is concerned so relogging or changing dimensions should not reset legitimate progression.
