# To Test - Easy Parity Systems

Branch: `feature/easy-parity-systems`

Current implementation commits:

- `a21b915` - Implement Weapon Station module editing
- `11a85d0` - Fix Weapon Station shift-click module packing
- `94f6d58` - Polish machine UIs and restore legacy textures

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

## Module workflow

The module slots are ordered as Battery, Colour, Barrel, Sights, Utility, Utility.

- [ ] Phaser, Phaser Rifle, Ion Sniper, and Plasma Shotgun can enter the Weapon slot.
- [ ] Normal items cannot enter the Weapon slot.
- [ ] Weapon Battery and Creative Battery can enter the Battery slot only.
- [ ] Colour, Barrel, Sights, and Utility modules only enter their matching slots.
- [ ] Random items cannot enter module slots.
- [ ] Modules unsupported by the inserted weapon are rejected.
- [ ] Install every compatible module type available on each weapon.
- [ ] Close/reopen the station and confirm installed modules return to the correct slots.
- [ ] Remove a module, take/reinsert the weapon, and confirm the removed module stays uninstalled.
- [ ] Confirm modules left installed remain installed.

## Weapon behaviour after editing

Use Survival mode for energy behaviour tests because Creative bypasses normal weapon energy consumption.

- [ ] Installed battery affects capacity/reload behaviour correctly.
- [ ] Colour module changes beam colour.
- [ ] Damage module changes damage as expected.
- [ ] Fire module ignites targets and applies its expected damage tradeoff.
- [ ] Explosion module creates its expected impact behaviour.
- [ ] Holo Sights changes weapon accuracy as expected.
- [ ] Sniper Scope changes range/accuracy as expected.
- [ ] Ricochet causes one valid rebound from a block.
- [ ] Phaser accepts supported Colour/Barrel modules and rejects unsupported Sights/Utility modules.
- [ ] Phaser mode switching, heat, firing, and energy behaviour still work.
- [ ] Plasma Shotgun rejects the Heal module and its charge/release firing still works.

## Pickup, shift-click, and persistence

- [ ] Configure a weapon and pick it up normally with the mouse.
- [ ] Confirm station module slots clear and no module duplicates/deletes.
- [ ] Reinsert it and confirm all installed modules return.
- [ ] Shift-click an unconfigured gun into the Weapon Station.
- [ ] Configure it, then shift-click it back to player inventory.
- [ ] Reinsert it and confirm all modules are still installed with no duplication/deletion.
- [ ] Shift-click compatible modules into/out of the station and confirm correct routing.
- [ ] Leave a configured weapon in the station, save/quit, reopen, and confirm weapon/module state persists.
- [ ] Switch between different weapons and confirm modules never leak from one weapon to another.

## UI polish - machine screens

Open all seven polished screens:

- [ ] Matter Decomposer.
- [ ] Matter Recycler.
- [ ] Matter Analyzer.
- [ ] Matter Replicator.
- [ ] Solar Panel.
- [ ] Molecular Inscriber.
- [ ] Transporter.

For each screen:

- [ ] The dark Matter Overdrive-style frame renders correctly with no vanilla-grey placeholder panel left behind.
- [ ] Every visible slot frame lines up with the real clickable slot.
- [ ] Player inventory/hotbar slots remain aligned and usable.
- [ ] Title, energy/matter values, progress/status text, and debug information remain readable.
- [ ] No text overlaps the `INF FE` button or another label.
- [ ] Progress/energy/matter bars fill in the expected direction and remain inside their frames.
- [ ] `INF FE` still toggles the machine debug energy state where supported.
- [ ] Live values continue updating while the machine works.
- [ ] Repeat at the normal GUI scale and at least one different GUI scale to check clipping/alignment.

## Texture/model polish

Inspect these blocks from every side in-world and in inventory/JEI where applicable:

- [ ] Decomposer uses its dedicated top artwork and machine-base body faces.
- [ ] Matter Analyzer shows analyzer front artwork on model north and analyzer top artwork on top instead of repeating the front on every face.
- [ ] Matter Recycler uses recycler side artwork on side faces and machine-base artwork on top/bottom.
- [ ] Replicator shows its dedicated front face instead of repeating its body texture everywhere.
- [ ] Transporter shows separate front, side, and top artwork.
- [ ] Solar Panel uses the solar artwork on top and machine-base artwork on body faces.
- [ ] None of the changed blocks show purple/black missing textures.
- [ ] No model-bake/resource errors for these blocks appear in the log.
- [ ] Save/reload the world and confirm the visual changes do not affect machine inventories, energy, matter, upgrades, or operation.

Known limitation for this pass: machines without a horizontal-facing blockstate have their restored front artwork fixed to model north. Rotation/state work is intentionally deferred rather than changing placed-world blockstate behaviour during a cosmetic pass.

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
- [ ] Decomposer, Recycler, Analyzer, Replicator, Solar Panel, Inscriber, and Transporter still perform their existing gameplay functions.
- [ ] Fusion Reactor, Reactor IO, Heavy Energy Cable, anomaly, stabilizers, and reactor ring power behaviour still load and function normally.

## Pass criteria

This branch is ready to merge into `main` when:

- [ ] The build/client gate passes.
- [ ] Module slot validation passes.
- [ ] Install/uninstall, normal pickup, shift-click, and persistence pass without duplication/deletion.
- [ ] All seven polished machine screens have correct slot alignment and readable live/debug data.
- [ ] All six corrected block models load with their expected existing legacy textures and no model/resource errors.
- [ ] Existing machine, weapon, and reactor functionality has not regressed.

## Failure notes

Record any failure with:

- machine or weapon used;
- screen/block involved;
- installed modules/upgrades if relevant;
- Survival or Creative mode;
- GUI scale if the problem is visual;
- normal click or shift-click if inventory-related;
- whether the failure happened before or after closing the GUI;
- whether the world had been saved/reloaded;
- expected result;
- actual result;
- relevant log/crash output if present.
