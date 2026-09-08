---
navigation:
  title: Android System
  parent: index.md
  position: 4
  icon: matteroverdrive:android_station
---
# Android System

The Android Station manages conversion and Android progression. Androids have persistent progression, body systems, energy and a selectable skill tree. The Android HUD reports combat state in play, while the Class Matrix and skill-tree interfaces configure the build.

## Core abilities

The legacy core cycle remains available alongside subclass abilities. The current 0.6 combat pass intentionally makes these abilities noticeable rather than tiny background bonuses:

- **Cloak** drains 80 FE/t while active, breaks nearby hostile target locks, grants movement on entry and gives an ambush movement/Strength window when you leave cloak. Phase Navigator and related fragments/passives strengthen this further.
- **Force Field** drains 20 FE/t while idle and 48 FE per absorbed damage point before perk scaling. It absorbs 65% of incoming damage by default and 85% with Adamant Chassis, with additional defensive layers available from the loadout.
- **Sonic Shockwave** costs 3,000 FE, has a 4s base cooldown, deals 12 base damage in a 7-block radius and applies strong knockback/control. Shockwave perks and Singularity Lattice substantially increase it.
- **Ender Teleport** costs 2,800 FE, has a 2.5s base cooldown and 12-block base range. It grants post-blink mobility/resistance and disrupts nearby enemies; Phase Navigator, Translocation and Singularity Lattice add real gameplay effects.

## Subclass abilities

Every subclass equips three dedicated actions in addition to the core ability cycle:

- **H** - class ability. Lower-cost movement, defence, sustain or close-combat action.
- **N** - tech ability. Stronger battlefield-control, damage, marking or fleet-support action.
- **G** - subclass ultimate. High-impact ability with the largest FE commitment and cooldown.

The Class Matrix inspection panel reports the exact FE cost and cooldown for H and N, and the descriptions state concrete ranges, durations, damage, healing and effect levels. G ultimates show their FE cost and cooldown as well.

Damaging H/N/G actions are tagged as Android ability damage. This is important: damage Aspects, Fragments and passive protocols such as Vanguard Protocol, Amplitude, Feedback, Corrosive Cloud and Mass Driver now modify those attacks instead of silently missing them.

## HUD

The in-world Android HUD is designed to answer the important combat questions without reopening the Matrix:

- current specialization;
- Android FE and low-energy warning;
- level, XP and unspent progression points;
- equipped Aspect/Fragment counts and passive protocol;
- selected core ability and current state;
- dedicated **H / N / G** slots with ability name, FE cost, level lock, READY state or remaining cooldown;
- a live cooldown progress strip for each subclass slot.

## Aspects, Fragments and passive protocols

Descriptions in the Class Matrix are intended to match the runtime values. The 0.6 combat audit specifically wires previously underimplemented choices such as **Singularity Lattice**, **Reactive Exoshell**, **Phase Navigator** and **Fragment of Translocation** to real effects. Percentage-only bonuses were also raised so a committed build has a noticeable combat identity.

Reactive Exoshell, for example, now triggers a six-second Resistance III + Absorption II defensive window when damaged, with an eight-second retrigger gate. Phase Navigator changes Teleport range/cooldown/post-blink effects and improves cloak exit rather than existing only as descriptive text.

## Drone fleets

Linked drones recognise drones belonging to the same operator, and allied operators, as friendlies. Friendly drones are excluded from target selection and friendly drone damage is rejected as a second safety layer.

Following drones use stable escort formation slots behind and beside their operator rather than all steering toward the same point. Fleet separation keeps nearby drones apart, and long-distance catch-up only repositions a drone into a collision-free slot that is not already occupied by another fleet drone.

Drone Commander progression now materially scales the fleet: damage multipliers, marked-target bonuses, repair, resistance and command radius all have runtime hooks. **Command Authority** extends the active support envelope to 48 blocks, and **Overmind Ascendant** actually boosts drone offence in addition to repair/defence.

## Android Spawner squads

The Android Spawner maintains up to six owned synthetic soldiers with the intended 30% melee / 70% ranged mix and six Transport Flash Drive patrol slots. Squad color, PATROL/GUARD/HOLD/ESCORT mode and commander state persist.

ESCORT now assigns deterministic formation slots by squad membership rather than hash-based positions that can collide. If a squad member falls far behind it may use a collision-checked catch-up position rather than clipping into blocks or another squad member.

Android features are server-authoritative where gameplay state is concerned, so relogging or changing dimensions should not reset legitimate progression.
