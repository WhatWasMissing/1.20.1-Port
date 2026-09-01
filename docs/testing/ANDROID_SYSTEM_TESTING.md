# Android System and Weapon HUD Test Checklist

Branch: `feature/android-system-weapon-hud`

## Android conversion and energy

- [ ] Use a Blue Android Pill in Survival. Confirm conversion activates at 25,000 / 100,000 Android FE.
- [ ] Open an Android Station. Confirm it reports Android status, energy, installed parts, station FE, and transfer rate.
- [ ] Power the Android Station from Fusion Reactor IO/cables. While standing within four blocks, confirm Android FE increases gradually.
- [ ] Use a Yellow Android Pill while converted. Confirm it adds up to 25,000 FE without exceeding capacity.
- [ ] Use a Red Android Pill. Confirm conversion ends and all installed bionic parts return to the inventory/drop safely.
- [ ] Relog and die/respawn. Confirm active conversion, energy, and parts persist.

## Bionic parts and abilities

- [ ] While converted, hold each Rogue Android Part and right-click an Android Station to install it.
- [ ] Confirm a second copy of the same part is rejected without consuming it.
- [ ] Head: night vision while Android FE is available.
- [ ] Chest: resistance effect and lower incoming damage while FE is available.
- [ ] Arms: increased melee damage while FE is available.
- [ ] Legs: increased movement speed while FE is available.
- [ ] Drain Android FE to zero and confirm abilities stop rather than becoming free.

## Rogue Android Spawner

- [ ] Power an Android Spawner from the reactor/cable network.
- [ ] After it stores 20,000 FE, confirm it creates a named Rogue Android (Husk) when no previous Rogue Android is nearby.
- [ ] Kill the Rogue Android and confirm it drops one random bionic part.
- [ ] Confirm the spawner does not repeatedly spawn while a tagged Rogue Android is within its local range.

## Weapon HUD

- [ ] Hold each energy weapon in the main hand. Confirm a compact HUD appears at the lower-right.
- [ ] Confirm the charge bar/value changes while firing/reloading/charging.
- [ ] Confirm heat value and bar rise with firing and cool down normally.
- [ ] Confirm the HUD clearly shows `OVERHEATED` and the weapon cannot fire while overheated.
- [ ] Switch to a non-energy item or hide the GUI. Confirm the HUD disappears.
