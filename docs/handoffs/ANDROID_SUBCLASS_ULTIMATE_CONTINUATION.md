# Android subclass / ultimate continuation

This pass converts the Android loadout into a subclass-oriented interface and finishes the server/client plumbing for subclass ultimates.

Planned/implemented in this pass:
- Four selectable subclasses: Assault, Chassis, Utility, Drone Commander.
- One class Ultimate per subclass, unlocked at Android level 4.
- Dedicated Ultimate keybind and server-authoritative cooldown/FE checks.
- Destiny-like subclass loadout screen with subclass selector, Ultimate panel, class-filtered Aspects, Fragment slots, Artifact view and Drone Commander progression view.
- Network sync for specialization and Ultimate cooldown.
- Drone perk dependency cleanup when refunding an earlier node.

Testing:
1. Convert to Android and reach level 4.
2. Open loadout and switch between all four subclasses.
3. Verify only Aspects belonging to the selected subclass are offered.
4. Equip up to two Aspects and verify Fragment capacity follows Aspect slot totals.
5. Activate each Ultimate and verify FE cost, cooldown, effects and client HUD/loadout cooldown display.
6. Verify Drone Commander perks still require sequential progression and removing an earlier node removes higher dependent nodes.
7. Relog/clone and verify subclass/loadout persistence.
