---
navigation:
  title: Android System
  parent: index.md
  position: 4
  icon: matteroverdrive:android_station
---
# Android System

The **Android Station is now the central Android workbench**. Use it to manage body parts, chassis hardware, energy and progression without repeatedly closing the GUI and right-clicking loose components.

## Android Station workflow

After conversion, open an Android Station while carrying the hardware you want to use.

### Body systems

The station presents four body controls: **Head, Torso, Arms and Legs**.

- If the part is missing, clicking its control searches your inventory for the matching Android part and installs one.
- If the part is already installed, clicking it removes the part and returns it to your inventory.
- Removing Head or Torso automatically disables Cloak or Force Field if that system depended on the removed part.
- Installed body controls are visibly marked in the station.

You no longer need to install body components by manually using each loose part outside the station.

### Chassis hardware

Below the body controls are five persistent chassis slots: **Core, Frame, Muscles, Optics and Shell**. Each row shows the currently supported alternatives for that slot.

- Click a module you have in your inventory to equip it.
- Click the currently equipped module again to remove it.
- Click the other choice in the same row to swap modules; the old module is returned automatically.
- The equipped choice is marked directly in the station UI.

Current hardware choices are:

- **Capacitor Core** - passive FE recovery and a larger Android energy reserve.
- **Overclock Core** - spends FE for stronger combat output.
- **Lightweight Frame** - mobility and jump control at a defensive cost.
- **Reinforced Frame** - substantially reduces incoming damage.
- **Agility Myomers** - improves movement speed.
- **Siege Myomers** - increases attack output at a mobility cost.
- **Hunter Optics** - periodically highlights nearby hostiles.
- **Precision Optics** - increases outgoing combat damage.
- **Stealth Shell** - crouching consumes FE to cloak.
- **Reactive Shell** - hardens the chassis below half health.

The 0.7 release fixes the **Capacitor Core** so its extra 50,000 FE is real storage capacity rather than tooltip-only capacity. Charging systems, the HUD and the Skill Tree use the chassis-adjusted maximum, and percentage-based loadout effects use that same effective reserve. A normal Android therefore evaluates 50%/75% effects at 50,000/75,000 FE; a Capacitor-Core Android evaluates them at 75,000/112,500 FE. Removing or replacing a capacity-granting core permanently clamps stored FE to the new maximum, so excess charge cannot remain hidden in player data and reappear when the core is reinstalled.

The station also provides direct buttons for **Core Ability Cycle**, **Skill Tree**, and **Class Matrix**, so normal Android configuration can start from one machine.

## Android charging

The Android Station still charges nearby Androids when supplied with FE. For larger bases, the 0.7 release adds the **Android Induction Relay**:

- stores **2,000,000 FE**;
- accepts up to **16,384 FE/t**;
- wirelessly distributes up to **8,192 FE/t**;
- supports **32 / 64 / 96 block** ranges;
- charges only Android players in the same dimension;
- splits available transfer between multiple Androids rather than duplicating FE.

Sneak-use the relay to cycle its range. It does not create energy and does not force-load or charge players in other dimensions.

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

The legacy core cycle remains available alongside subclass abilities:

- **Cloak** drains FE while active and breaks nearby hostile target locks.
- **Force Field** drains FE and absorbs incoming damage.
- **Sonic Shockwave** is the close-range kinetic control attack.
- **Ender Teleport** is the mobility core ability.

Body parts and level requirements still determine whether a core ability is available. The Android Station shows the selected core ability and makes cycling it available without leaving the workbench.

## Subclass abilities

Every subclass equips three dedicated actions in addition to the core ability cycle:

- **H** - class ability.
- **N** - tech ability.
- **G** - subclass ultimate.

The **Class Matrix** reports the exact FE cost, cooldown and effects for the currently selected class/subclass equipment. The Android Station links directly to it.

## HUD

The in-world Android HUD reports current specialization, chassis-adjusted FE reserve and low-energy state, level/nonlinear XP progress, true available Ascension Points, equipped Aspect/Fragment counts, selected core ability, and dedicated H/N/G slots with cooldowns.

## Skill tree and build points

Android level progression is persistent. The skill tree grants **one Ascension Point every two Android levels**, up to five points at level 10, so it creates a build rather than unlocking everything simultaneously. Higher tiers require investment in the same branch. The Android Station links directly to the skill tree.

## Aspects, Fragments and passive protocols

Aspects, Fragments and passive protocols sit above the physical body/chassis layer. Chassis choices therefore do not replace subclass choices: a Drone Commander can still run a Reinforced Frame, while a Precision Frame build can use Siege Myomers or a Stealth Shell.

### Legendary relics

Rare facility caches can contain **Legendary Relics**. Right-click a relic as an Android to install its existing passive protocol; only one protocol can be active at a time, and relics are not a separate currency or progression tree.

- **Overclocked Relay** - Synthetic Manufacturing Plant; installs Overclock Protocol.
- **Swarm Beacon** or **Aegis Prism** - Android Command Bunker; installs Swarm Support or Aegis Protocol.
- **Hunter Lens** - Black Site; installs Hunter Protocol.
- **Nanite Crown** - Matter Refinery; installs Nanite Recovery.
- **Capacitor Heart** - Quantum Relay Station; installs Capacitor Feedback.
- **Phase Anchor** - Fusion Research Complex; installs Phase Stability.

Relics are consumed when installed and are rejected safely by non-Android players, so finding one gives an Android build a meaningful facility-linked choice without replacing the normal loadout controls.

## Linking and commanding drones

Interact with an unowned drone to link it. Linked drones remember their owner and command mode. Sneak-interacting with your own drone releases the link.

Normal interaction cycles:

1. **FOLLOW** - escort the operator.
2. **HOLD** - remain at the current position.
3. **DEFENSIVE** - escort and retaliate.
4. **PASSIVE** - escort without acquiring combat targets.
5. **AGGRESSIVE** - escort and actively engage hostiles.

**HOLD is persistent.** A held drone does not resume following merely because you walk away.

## Drone Management screen

Press **M** while Android systems are active to open the operator console. It reports loaded linked drones within **192 blocks**, including name, command mode, health and distance. Fleet-wide FOLLOW, HOLD, DEFEND, PASSIVE and AGGRESSIVE commands are available so you do not have to physically catch a following drone.

## Android Spawner squads

The Android Spawner maintains owned synthetic soldiers with persistent squad mode and commander state. ESCORT uses formation slots and catch-up handling rather than stacking every unit into the same position.

Android features are server-authoritative where gameplay state is concerned, so legitimate progression and equipment should survive relogging and respawn cloning.

## Encounter research

The first defeated Android from each encounter faction is logged as field evidence for that player. The log is stored in the player's persistent research data, so repeated kills do not duplicate the reward. Standard facility factions grant 25 XP and contribute to Automation & Drones clearance; a Black Site encounter contributes to Anomaly Engineering. Black Site Androids may also yield a recovered protocol artifact with a stable protocol identity.

For Grid Capacitors, Quantum Power Relays, facility monitoring and the rest of the experimental infrastructure, see [Advanced Infrastructure](advanced_infrastructure.md).
