---
navigation:
  title: Facility Restoration
  parent: index.md
  position: 10
  icon: matteroverdrive:facility_network_controller
---
# Facility Restoration

The six native technology facilities now support an optional recovery loop on newly generated sites. The loop does not gate normal crafting or the Mad Scientist campaign; it turns an abandoned facility into a self-contained exploration objective.

## Recovery sequence

1. Find the **Facility Network Controller**. Quantum Relay Stations and Fusion Research Complexes include an additional accessible recovery terminal so their secure control rooms cannot deadlock progression.
2. Restore **emergency power**. Any connected stored FE satisfies the step. With no network power, use a Battery, High Capacity Battery, Energy Pack or Creative Battery on the controller; Survival consumes one portable power item.
3. Repair **control hardware** by using an Isolinear Circuit Mk1 or better on the controller. Survival consumes one circuit.
4. The facility's **Security Doors** release. Explore the secured wing and recover the matching Recovered Research Dossier from its generated cache.
5. Return the dossier to a Facility Network Controller to mark the site restored. The dossier is not consumed. Unused finite emergency security reserves stand down; defenders already deployed remain in the world.

Sneak-use a controller at any time to print the current recovery objective before the normal device inventory readout.

## Facility security identities

Generated security is still finite, but defenders now keep a facility-specific role across save/reload:

- Manufacturing Plants use faster **Assembly Defenders**.
- Matter Refineries use aggressive **Malfunctioning Refinery Androids**.
- Quantum Relays use longer-range **Relay Sentries**.
- Android Bunkers use armoured **Command Guards**.
- Fusion Complexes use tougher, knockback-resistant **Containment Sentries**.
- Black Sites use the strongest **Black Site Wardens**, with later reserve deployments reaching higher minimum Android levels.

These profiles apply only to generated facility spawners. Player-built Android Spawners keep their FE-powered squad behaviour.

## Traversal and infrastructure

The restoration pass adds native chunk-clipped infrastructure pieces rather than returning to large Feature stamping. Surface sites can receive catwalk/service spines with railings, cable trays, warning lights and failed panels. Refinery excavation shafts gain ladders, bunker and Black Site entrance shafts gain climbable routes, and the Black Site vault receives a six-block descent stair from its approach corridor.

The same **Industrial Catwalk**, **Industrial Railing**, **Cable Tray**, **Warning Light**, **Damaged Panel** and **Security Door** blocks are available to players and have survival recipes. Player-placed Security Doors outside generated facilities can be toggled by use or redstone.

World-generation changes only affect fresh chunks. The retired Star Map is not part of this system.
