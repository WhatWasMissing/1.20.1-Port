---
navigation:
  title: Android System
  parent: index.md
  position: 4
  icon: matteroverdrive:android_station
---
# Android System

The Android Station manages conversion and Android progression. Androids have persistent progression, body systems, energy and a selectable skill tree. The Android HUD reports combat state in play, while the Class Matrix and skill-tree interfaces configure the build.

## Quick controls

- **V** - cycle the selected legacy core ability.
- **B** - activate the selected core ability.
- **H** - use the equipped class ability.
- **N** - use the equipped tech ability.
- **G** - use the subclass ultimate.
- **K** - open the Android skill tree.
- **L** - open the Android Class Matrix.
- **M** - open **Drone Management** and request a fresh list of loaded linked drones.

If a key conflicts with another mod, change it in Minecraft's Controls menu.

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

## Skill tree and build points

Android level progression is persistent. The skill tree grants a limited number of points, so it is intended to create a build rather than let one character own every node simultaneously. Branch investment unlocks higher tiers and capstones. Use the inspection pane before spending a point: it reports node requirements, branch investment and whether the node is currently available.

The reset control deliberately has a confirmation step and an FE cost. This keeps respecs available without making build choice meaningless.

## Aspects, Fragments and passive protocols

Descriptions in the Class Matrix are intended to match the runtime values. The 0.6 combat audit specifically wires previously underimplemented choices such as **Singularity Lattice**, **Reactive Exoshell**, **Phase Navigator** and **Fragment of Translocation** to real effects. Percentage-only bonuses were also raised so a committed build has a noticeable combat identity.

Reactive Exoshell, for example, now triggers a six-second Resistance III + Absorption II defensive window when damaged, with an eight-second retrigger gate. Phase Navigator changes Teleport range/cooldown/post-blink effects and improves cloak exit rather than existing only as descriptive text.

## Chassis hardware

Converted Androids can now install a second, physical hardware layer in addition to perks, Aspects and subclass choices. Chassis hardware has five persistent slots: **Core, Frame, Muscles, Optics and Shell**. Right-click a chassis module while converted to install it. Installing a different module in the same slot automatically returns the previous module to your inventory, so changing hardware does not destroy the old component.

Current module choices:

- **Capacitor Core** - slowly recovers Android FE over time.
- **Overclock Core** - spends FE continuously for stronger combat output.
- **Lightweight Frame** - improves mobility and jump control at the cost of slightly greater incoming damage.
- **Reinforced Frame** - substantially reduces incoming damage.
- **Agility Myomers** - improves movement speed.
- **Siege Myomers** - improves attack output but reduces mobility.
- **Hunter Optics** - periodically spends a small amount of FE to highlight nearby hostile targets.
- **Precision Optics** - increases outgoing combat damage.
- **Stealth Shell** - crouching spends FE to suppress the Android's visible signature.
- **Reactive Shell** - hardens automatically below half health.

Chassis modules persist with the player and are copied through player clone/respawn handling. They are intended to create hardware trade-offs that sit underneath the existing specialization system rather than replace it. For example, a Drone Commander can still choose a Reinforced Frame, while a Precision Frame subclass can pair with Siege Myomers or a Stealth Shell.

## Linking and commanding drones

Interact with an unowned drone to link it to your player. Linked drones remember their owner and command mode. Sneak-interacting with your own drone releases the link.

Normal interaction cycles the command state in a useful field order:

1. **FOLLOW** - escort the operator in formation and avoid attacking.
2. **HOLD** - stop escorting and hover at the current position. This is the mode to use when you want a drone to stay behind instead of permanently sitting behind your character.
3. **DEFENSIVE** - escort the operator and retaliate against valid attackers.
4. **PASSIVE** - escort without acquiring combat targets.
5. **AGGRESSIVE** - escort and actively engage valid hostile mobs.

**HOLD is persistent.** A held drone does not resume following merely because you walk away; change its mode back to FOLLOW/DEFENSIVE/PASSIVE/AGGRESSIVE when you want it moving with you again.

## Drone Management screen

Press **M** while Android systems are active to open the operator console. The request is server-authoritative and reports loaded linked drones within **192 blocks**.

The screen shows, for each active drone:

- drone name;
- current command mode;
- current/max health;
- approximate distance from the operator;
- a **MODE** button for cycling that individual drone.

The top row contains fleet-wide **FOLLOW ALL**, **HOLD ALL**, **DEFEND ALL**, **PASSIVE ALL** and **AGGRESSIVE** commands. **REFRESH** requests another server snapshot. This means you no longer need to physically catch a drone flying behind you just to change its command state.

The management list intentionally represents **loaded nearby drones**, not every drone that has ever been linked in unloaded chunks. If a drone is absent, move closer to its area and refresh.

## Drone fleets

Linked drones recognise drones belonging to the same operator, and allied operators, as friendlies. Friendly drones are excluded from target selection and friendly drone damage is rejected as a second safety layer.

Following drones use stable escort formation slots behind and beside their operator rather than all steering toward the same point. Fleet separation keeps nearby drones apart, and long-distance catch-up only repositions a drone into a collision-free slot that is not already occupied by another fleet drone.

Drone Commander progression materially scales the fleet: damage multipliers, marked-target bonuses, repair, resistance and command radius all have runtime hooks. **Command Authority** extends the active support envelope to 48 blocks, and **Overmind Ascendant** boosts drone offence in addition to repair/defence.

## Android Spawner squads

The Android Spawner maintains up to six owned synthetic soldiers with the intended 30% melee / 70% ranged mix and six Transport Flash Drive patrol slots. Squad color, PATROL/GUARD/HOLD/ESCORT mode and commander state persist.

ESCORT assigns deterministic formation slots by squad membership rather than hash-based positions that can collide. If a squad member falls far behind it may use a collision-checked catch-up position rather than clipping into blocks or another squad member.

Android features are server-authoritative where gameplay state is concerned, so relogging or changing dimensions should not reset legitimate progression.
