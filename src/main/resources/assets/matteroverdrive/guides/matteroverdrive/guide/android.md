---
navigation:
  title: Android System
  parent: index.md
  position: 4
  icon: matteroverdrive:android_station
---
# Android System

The Android Station manages conversion, installed body systems and Android progression. The 0.6 Android system is split into four layers: core abilities, Ascension perks, a specialization loadout, and H/N/G subclass abilities. Gameplay state is server-authoritative and designed to persist across normal saves, relogs and death/respawn.

## Progression and Ascension Points

Android level is driven by the backend XP curve rather than a flat 100 XP per level. The HUD now reads those real thresholds. Ascension Points are also not one point per level: you earn a total of five by level 10, with the available total derived from `level / 2`.

Perks are selected in branching paths. Higher-tier perks require prior investment in the same branch, so a level-10 capstone cannot be selected without building toward it first. The selected perk mask is preserved when XP changes and has a backup copy to protect against accidental progression loss.

## Core abilities

Cycle the installed core abilities, then activate the selected one. Each core ability requires its associated bionic part and minimum Android level.

- **Cloak**: 80 FE/t base drain while active. It grants entry mobility/defence, repeatedly breaks nearby hostile target locks and gives an ambush Strength/mobility window when disabled. Ghost Protocol and Silent Cloak reduce the FE burden; Phase Navigator improves the exit payoff.
- **Force Field**: 20 FE/t idle drain plus 48 FE per absorbed damage point before perk scaling. It mitigates 65% of incoming damage by default and **75% with Adamant Chassis**. The field also gives activation feedback/absorption and collapses automatically when FE can no longer sustain it.
- **Sonic Shockwave**: 3,000 FE base cost, 4-second base cooldown, 12 base damage and 7-block base radius. It applies knockback, Slowness and Weakness. Wideband Pulse, Resonant/Overcharged Pulse, Shock Momentum, Singularity Lattice and cooldown/efficiency perks can materially alter the result.
- **Ender Teleport**: 2,800 FE base cost, 2.5-second base cooldown and 12-block base range. It searches backward along the view ray for a collision-free destination, resets fall distance and grants post-blink mobility/resistance. Phase Navigator, Translocation, Singularity Lattice and blink perks extend or modify the move.

If Teleport reports that no safe destination exists, aim at a more open area rather than repeatedly activating into a solid wall.

## Classes and specializations

The three top-level Android identities are:

- **Strider**: mobility, cloak, target acquisition and precision positioning.
- **Juggernaut**: shockwave pressure, force fields, powered melee and durability.
- **Architect**: energy recursion, nanites, battlefield control and drone command.

Each class contains three specializations, for nine current specializations total:

- Strider: **Singularity Breaker / Utility**, **Hunter-Killer**, **Precision Frame**.
- Juggernaut: **Citadel / Chassis**, **Singularity Breaker / Assault**, **Siege Frame**.
- Architect: **Drone Commander**, **Nanite Weaver**, **Gravity Core**.

The Class Matrix is the best place to inspect the currently selected specialization, its Aspect/Fragment loadout, passive protocol and the exact H/N/G ability descriptions.

## H / N / G abilities

Every specialization equips three dedicated actions in addition to the core cycle:

- **H** - class ability: generally the lower-cost movement, defence, sustain or close-combat tool.
- **N** - tech ability: stronger battlefield control, marking, damage or fleet support.
- **G** - ultimate: the highest-impact action with the largest FE commitment and cooldown.

The HUD shows each slot separately with ability name, FE cost, level lock, READY state or remaining cooldown. Damaging H/N/G actions are tagged as Android ability damage, allowing combat Aspects, Fragments and passive protocols to modify them consistently.

A 0.6 safety rule prevents abilities that require a real target/fleet effect from spending FE and starting cooldown when their primary effect cannot occur. For example, **Drone Commander Swarm Surge requires at least one linked drone within 24 blocks**.

## Aspects, Fragments and passive protocols

These are intended to change real runtime behavior, not only text descriptions.

- **Singularity Lattice** strengthens Shockwave and gives Teleport a damaging/control arrival burst.
- **Reactive Exoshell** provides a timed Resistance/Absorption response when damaged and has its own retrigger timer.
- **Phase Navigator** modifies Teleport range/cooldown/post-blink effects and improves the cloak-exit window.
- **Fragment of Translocation** increases the post-Teleport mobility package.
- **Feedback / Harmonics / Recursive Core** return FE from tagged ability damage under their configured conditions.
- **Corrosive Cloud** adds poison/weakness pressure to qualifying ability damage.
- **Mass Driver** increases ability damage/knockback behavior.
- Defensive fragments and protocols stack with the normal Android mitigation calculation rather than replacing Force Field.

## HUD

The in-world Android HUD reports:

- active specialization;
- Android FE and low-energy warning;
- real level and XP progress using the backend XP thresholds;
- real unspent Ascension Points;
- equipped Aspect/Fragment counts and passive protocol;
- selected core ability and active/cooldown state;
- distinct H/N/G slots with FE cost, lock state and cooldown progress.

If the HUD appears stale after changing the build, close the configuration screen and perform a normal ability selection/action so the server state syncs again.

## Linked drone fleet

Linked drones store their owner UUID, fleet slot/count and command mode. FOLLOW, PASSIVE, DEFENSIVE and AGGRESSIVE are persistent commands.

Friendly-fire protection exists at more than one layer. Drones consider the owner and same-owner drones allied, and drone-origin direct or indirect projectile damage to the owner/fleet is cancelled. This prevents a ranged drone arrow already in flight from bypassing the entity-level ally check.

FOLLOW uses deterministic formation positions plus separation so multiple drones do not deliberately stack into one location. Catch-up positions are collision checked. If the owner is offline or cannot be resolved, linked drones now damp residual movement instead of drifting indefinitely.

### Drone Commander

Drone Commander progression can affect fleet damage, repairs, resistance, marked-target bonuses and command radius. **Command Authority** extends the support envelope to 48 blocks. **Swarm Surge** buffs linked drones in its operating radius and will refuse activation with no eligible drone. **Overmind Ascendant** restores/overclocks nearby owned drones and improves offence as well as defence/sustain.

## Android Spawner squads

The Android Spawner maintains up to six owned synthetic soldiers, with the intended population weighted roughly 30% melee / 70% ranged. It provides six Transport Flash Drive patrol slots.

Persistent spawner state includes squad color, PATROL/GUARD/HOLD/ESCORT mode, commander identity, patrol drives and the owned-unit list. ESCORT uses stable formation slots and collision-checked catch-up behavior. GUARD pulls units back toward the spawner, HOLD stops idle navigation, and Dismiss Squad should only remove Androids owned by that spawner.

## Persistence checklist

For a serious progression test, configure a specialization, perks, Aspects, Fragments and passive protocol; put H/N/G on cooldown; deploy linked drones; then save and quit Minecraft completely. After relaunch, verify the loadout, progression, drone modes and cooldown state. Repeat once across player death/respawn. The 0.6 persistence layer explicitly preserves the class/tech/ultimate cooldown timestamps and Reactive Exoshell timer in addition to the main Android/loadout roots.
