# To Test - Easy Parity Systems

Branch: `feature/easy-parity-systems`

Current implementation commits:

- `a21b915` - Implement Weapon Station module editing
- `11a85d0` - Fix Weapon Station shift-click module packing

## Build gate

- [ ] Pull `feature/easy-parity-systems`.
- [ ] Run the normal development build/client launch.
- [ ] Confirm the game reaches the main menu without registry, data-pack, model, or menu errors.
- [ ] Confirm an existing test world loads successfully.

## Weapon Station - basic UI

- [ ] Place a Weapon Station and open it.
- [ ] Confirm the station opens without a client/server crash.
- [ ] Confirm the layout shows one Weapon slot and six module slots.
- [ ] Confirm the player inventory and hotbar are aligned and usable.

## Weapon slot validation

Test with each currently implemented energy weapon.

- [ ] Phaser can be inserted into the Weapon slot.
- [ ] Phaser Rifle can be inserted into the Weapon slot.
- [ ] Ion Sniper can be inserted into the Weapon slot.
- [ ] Plasma Shotgun can be inserted into the Weapon slot.
- [ ] Normal items cannot be inserted into the Weapon slot.
- [ ] Weapon stacks remain limited to one item.

## Module slot validation

The module slots are ordered as:

1. Battery
2. Colour
3. Barrel
4. Sights
5. Utility
6. Utility

- [ ] A Weapon Battery can enter the Battery slot.
- [ ] Creative Battery can enter the Battery slot.
- [ ] Batteries cannot enter non-battery module slots.
- [ ] Colour modules only enter the Colour slot.
- [ ] Barrel modules only enter the Barrel slot.
- [ ] Sight modules only enter the Sights slot.
- [ ] Utility modules only enter either Utility slot.
- [ ] Random items cannot enter any module slot.
- [ ] Modules unsupported by the inserted weapon are rejected.

## Install modules

For each weapon, install every compatible module type available.

- [ ] Battery installs successfully.
- [ ] Colour module installs successfully.
- [ ] Barrel module installs successfully where supported.
- [ ] Sight module installs successfully where supported.
- [ ] Utility modules install successfully where supported.
- [ ] Close the Weapon Station.
- [ ] Reopen the Weapon Station with the same gun still inside.
- [ ] Confirm every installed module reappears in the correct station slot.

## Remove modules

- [ ] Remove one installed module from its station slot.
- [ ] Take the weapon from the station.
- [ ] Reinsert the weapon.
- [ ] Confirm the removed module stays removed.
- [ ] Confirm all modules that were left installed return correctly.
- [ ] Repeat with a battery module.
- [ ] Repeat with one of the two Utility slots.

## Weapon behaviour after editing

Use Survival mode for energy behaviour tests because Creative bypasses normal weapon energy consumption.

- [ ] Installed battery affects weapon capacity/reload behaviour correctly.
- [ ] Colour module changes the beam colour.
- [ ] Damage module changes damage as expected.
- [ ] Fire module ignites targets and applies its expected damage tradeoff.
- [ ] Explosion module creates its expected impact behaviour.
- [ ] Holo Sights changes weapon accuracy as expected.
- [ ] Sniper Scope changes range/accuracy as expected.
- [ ] Ricochet causes one valid rebound from a block.
- [ ] Any other currently registered compatible module still performs its existing effect.

## Phaser-specific compatibility

The Phaser intentionally has more limited module compatibility than the larger guns.

- [ ] Phaser accepts supported Colour modules.
- [ ] Phaser accepts supported Barrel modules.
- [ ] Phaser rejects unsupported Sights modules.
- [ ] Phaser rejects unsupported Utility modules.
- [ ] Phaser mode switching still works after module installation/removal.
- [ ] Phaser firing, heat, and energy behaviour have not regressed.

## Plasma Shotgun compatibility

- [ ] Plasma Shotgun rejects the Heal module as intended.
- [ ] Other supported modules can still be installed normally.
- [ ] Charge/release shotgun firing still works after station editing.

## Normal pickup test

This specifically tests module packing when taking a weapon normally with the mouse.

- [ ] Put a weapon in the station.
- [ ] Install multiple modules.
- [ ] Pick the weapon up normally with the mouse.
- [ ] Confirm the station module slots clear.
- [ ] Reinsert the weapon.
- [ ] Confirm all installed modules return.
- [ ] Confirm no module is duplicated.
- [ ] Confirm no module is deleted.

## Shift-click regression test

This specifically verifies commit `11a85d0`.

- [ ] Shift-click an unconfigured gun from player inventory into the Weapon Station.
- [ ] Install multiple modules.
- [ ] Shift-click the configured gun back into player inventory.
- [ ] Confirm the station module slots clear.
- [ ] Reinsert the gun.
- [ ] Confirm all modules are still installed.
- [ ] Confirm no module is duplicated.
- [ ] Confirm no module is deleted.
- [ ] Shift-click compatible modules from player inventory into the station and verify correct routing.
- [ ] Shift-click modules back out and confirm they return to the player inventory normally.

## Save/reload persistence

- [ ] Leave a configured weapon inside the Weapon Station.
- [ ] Save and quit the world.
- [ ] Reopen the world.
- [ ] Open the Weapon Station.
- [ ] Confirm the weapon is still present.
- [ ] Confirm all module state is preserved.
- [ ] Take the weapon and reinsert it to verify the configuration still unpacks correctly.

## Block break / unusual state checks

- [ ] Verify normal Weapon Station usage does not duplicate modules after repeatedly opening and closing the GUI.
- [ ] Verify switching between different weapons does not transfer modules from one gun to another unexpectedly.
- [ ] Verify removing a gun before inserting another leaves no stale module state in the station.
- [ ] Verify inserting a gun that already has modules correctly exposes those modules for editing.

## Regression checks

These were working before this branch and should remain working.

- [ ] Weapons still fire normally.
- [ ] Reloading from charged weapon batteries still works.
- [ ] Reloading from Energy Packs still works.
- [ ] Creative Battery behaviour still works.
- [ ] Heat generation/cooling still works.
- [ ] Overheat state still works.
- [ ] Existing weapon sounds still play.
- [ ] Existing weapon models/icons still render.
- [ ] Other Matter Overdrive machines and the Fusion Reactor still load and function normally.

## Pass criteria

This branch is ready to merge into `main` when:

- [ ] The build/client gate passes.
- [ ] Module slot validation passes.
- [ ] Install and uninstall behaviour passes.
- [ ] Normal pickup preserves module state.
- [ ] Shift-click preserves module state.
- [ ] No duplication or deletion is found.
- [ ] Save/reload persistence passes.
- [ ] Existing firing/reload behaviour has not regressed.

## Failure notes

Record any failure with:

- weapon used;
- modules installed;
- Survival or Creative mode;
- normal click or shift-click;
- whether the failure happened before or after closing the GUI;
- whether the world had been saved/reloaded;
- what was expected;
- what actually happened;
- relevant log/crash output if present.
