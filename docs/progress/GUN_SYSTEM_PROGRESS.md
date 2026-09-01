# Current status — 2026-09-01

The current implementation supersedes older Creative-mode references below:

- Weapons must have energy in Survival and Creative; only the explicit Creative Battery module provides infinite energy.
- The Weapon Station module workflow and battery reload path remain implemented.
- First- and third-person transforms for Phaser, Phaser Rifle, Ion Sniper, and Plasma Shotgun remain an open visual runtime check; do not treat the older model note as a final rendering solution.

The remaining historical entries are retained as implementation context.

# Gun System Progress

## Current branch

`feature/easy-parity-systems`

## Latest fixes

- Weapon Station inventories save and reload through block-entity NBT.
- Weapon Station contents mark the block entity dirty when changed.
- Weapon Station shift-click bounds are limited to the slots actually exposed by the menu.
- Weapons can reload from charged FE batteries in the player inventory or off-hand.
- Energy packs remain automatic reload items.
- Shift-right-click reloads non-Phaser weapons in Survival and Creative.
- Other guns are no longer treated as reload batteries.
- Charged weapon batteries are consumed once per reload and no longer drained per shot.
- Gun item models use the original-compatible generated icons; the legacy OBJ assets remain reserved for a later dedicated 1.20.1 renderer.

## Weapon Station module implementation

- The Weapon Station now has one dedicated weapon slot and six typed module slots matching the existing weapon NBT system: battery, colour, barrel, sights, utility, utility.
- Slot validation uses `WeaponSystem.isValidModuleForSlot`, so incompatible modules and unsupported weapon/module combinations are rejected instead of being silently accepted.
- Opening the station unpacks installed modules from the weapon into editable station slots.
- Taking the weapon or closing the GUI packs the selected modules back into the weapon and clears the temporary station slots, preventing module duplication.
- Removing a module while editing cleanly uninstalls it from the weapon when the weapon is packed again.
- The station screen now exposes the weapon/module layout instead of presenting seven unlabeled generic slots.
- Shift-clicking the weapon now packs its modules before the stack is copied into the player inventory, so the fast-transfer path preserves the same configuration as normal pickup.

## Important testing note

Creative mode bypasses weapon energy consumption. Use Survival mode when testing battery reload, energy costs, and empty-weapon behavior.

## Weapon Station test pass

1. Put each energy weapon into the Weapon slot.
2. Verify only compatible batteries/modules enter each corresponding module slot.
3. Install a battery, colour module, barrel module, sights module, and utility modules where supported.
4. Close and reopen the station and confirm every installed module returns to the correct station slot.
5. Remove one module, take the weapon, reopen it, and confirm the removed module stays uninstalled.
6. Take a fully configured weapon directly from the station and confirm its modules still affect firing, colour, range, accuracy, damage, and special effects as applicable.
7. Save and reload the world with a configured weapon left in the station and confirm the weapon and its module state persist.
8. Try shift-clicking weapons/modules into and out of the station and confirm no duplication, deletion, or invalid-slot insertion occurs.

## Next work

- Runtime-test the completed Weapon Station module workflow.
- Add remaining gun recipes once their legacy ingredient mappings are verified rather than guessed.
- Continue with other self-contained parity systems that do not depend on Android capability infrastructure, custom entity AI, or a dedicated legacy OBJ renderer.
