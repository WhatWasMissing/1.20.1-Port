# To Test - Easy Parity Systems

Branch: `feature/easy-parity-systems`

Current implementation commits include:

- `a21b915` - Implement Weapon Station module editing
- `11a85d0` - Fix Weapon Station shift-click module packing
- `94f6d58` - Polish common machine UIs and restore legacy textures
- `7b7810a` - Polish remaining UIs and Weapon Station model
- current resource-repair commit - restore additional legacy face mappings and reactor-controller rotation

## Build gate

- [ ] Pull `feature/easy-parity-systems`.
- [ ] Run the normal development build/client launch.
- [ ] Confirm the game reaches the main menu without registry, data-pack, model, or menu errors.
- [ ] Confirm an existing test world loads successfully.

## Weapon Station - module workflow

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

## UI polish - all current screens

Open every current screen:

- [ ] Matter Decomposer.
- [ ] Matter Recycler.
- [ ] Matter Analyzer.
- [ ] Matter Replicator.
- [ ] Solar Panel.
- [ ] Molecular Inscriber.
- [ ] Transporter.
- [ ] Pattern Monitor.
- [ ] Pattern Storage.
- [ ] Energy Pipe.
- [ ] Tritanium Crate.
- [ ] Weapon Station.
- [ ] Fusion Reactor Controller.
- [ ] Reactor Assembly Guide.

For each relevant container screen:

- [ ] The dark Matter Overdrive-style frame renders correctly with no vanilla-grey placeholder panel left behind.
- [ ] Every visible slot frame lines up with the real clickable slot.
- [ ] Player inventory/hotbar slots remain aligned and usable.
- [ ] Title, energy/matter values, progress/status text, and debug information remain readable.
- [ ] No text overlaps the `INF FE` button or another label.
- [ ] Progress/energy/matter bars fill in the expected direction and remain inside their frames.
- [ ] `INF FE` still toggles the machine debug energy state where supported.
- [ ] Live values continue updating while the machine works.
- [ ] Repeat at the normal GUI scale and at least one different GUI scale to check clipping/alignment.

Specific screen checks:

- [ ] Pattern Monitor populated ghost slots still request x1 when left-clicked.
- [ ] Pattern Monitor queue and pattern counts remain correct.
- [ ] Pattern Storage drive, energy, and upgrade slots all line up and shift-click routing still works.
- [ ] Energy Pipe stored FE and last-output values update correctly.
- [ ] Tritanium Crate all 54 storage slots line up, including the first and last slots of every row.
- [ ] Tritanium Crate used-slot and total-item counters update correctly.
- [ ] Weapon Station one Weapon slot plus six module slots line up exactly with their clickable positions.
- [ ] Fusion Reactor four upgrade slots line up and its structure/output/usage/anomaly/debug values still update.
- [ ] Reactor Assembly Guide page images render, Previous/Next state is correct, and Done closes the guide.

## Texture/model polish

Inspect these blocks from every side in-world and in inventory/JEI where applicable:

- [ ] Decomposer uses its dedicated top artwork and machine-base body faces.
- [ ] Matter Analyzer shows analyzer front artwork on model north and analyzer top artwork on top instead of repeating the front on every face.
- [ ] Matter Recycler uses recycler side artwork on side faces and machine-base artwork on top/bottom.
- [ ] Replicator shows its dedicated front face instead of repeating its body texture everywhere.
- [ ] Transporter shows separate front, side, and top artwork.
- [ ] Solar Panel uses the solar artwork on top and machine-base artwork on body faces.
- [ ] Weapon Station uses `weapon_station_top` on top, `weapon_station_bottom` underneath, and `weapon_station_side` on body faces.
- [ ] Tritanium Lamp uses `tritanium_lamp_top`, `tritanium_lamp_bottom`, and `tritanium_lamp_sides` on the appropriate faces.
- [ ] Pattern Monitor shows the holographic monitor art on one face, its rear art on the opposite face, and normal machine-base artwork on the other four faces.
- [ ] Place Fusion Reactor Controllers facing north, east, south, and west; confirm the visible screen rotates to the controller-facing side instead of remaining fixed.
- [ ] Fusion Reactor Controller uses the screen texture only on its display face and machine-base texture elsewhere.
- [ ] None of the changed blocks show purple/black missing textures.
- [ ] No model-bake/resource errors for these blocks appear in the log.
- [ ] Save/reload the world and confirm the visual changes do not affect machine inventories, energy, matter, upgrades, or operation.

Known limitation: machines that do not expose a horizontal-facing blockstate still have any restored front artwork fixed to model north. Rotation/state work is intentionally deferred rather than changing placed-world blockstate behaviour during a cosmetic pass.

## Regression checks

- [ ] Weapons still fire/reload and heat/cooling still works.
- [ ] Existing weapon sounds/models/icons still render.
- [ ] All previously working machines still perform their existing gameplay functions.
- [ ] Pattern Monitor requests and Pattern Storage routing still work.
- [ ] Fusion Reactor, Reactor IO, Heavy Energy Cable, anomaly, stabilizers, and reactor ring power behaviour still load and function normally.

## Pass criteria

This branch is ready to merge into `main` when:

- [ ] The build/client gate passes.
- [ ] Weapon module install/uninstall, normal pickup, shift-click, and persistence pass without duplication/deletion.
- [ ] All current screens have correct slot alignment and readable live/debug data.
- [ ] All corrected block models load with their expected existing legacy textures and no model/resource errors.
- [ ] Fusion Reactor Controller display rotation matches all four horizontal facing states.
- [ ] Existing machine, weapon, and reactor functionality has not regressed.

## Failure notes

Record the machine/weapon/block, GUI scale if visual, facing if directional, expected result, actual result, and relevant log/crash output.


## New regression pass

- [ ] Feed a living entity into the event horizon and confirm that killing it increases anomaly mass immediately and only once.
- [ ] Confirm the anomaly mass shown in the reactor/debug readout changes after the entity dies.
- [ ] Check Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun in first-person and third-person; confirm they are upright, point forwards and are not mirrored.
- [ ] Open the Charging Station GUI, insert a rechargeable battery, and confirm the battery charge rises gradually.
- [ ] Confirm the Charging Station GUI shows battery charge, station status and transfer rate; save/reload and retrieve the battery.
- [ ] Connect a stabiliser to reactor power through the reactor IO/cable network. Confirm it remains inactive with no reactor power and suppresses the anomaly while powered.
- [ ] Insert one or more upgrade_power items into a stabiliser. Confirm the upgrade is accepted, saved, dropped when the block is broken, and changes the stabiliser's suppression/power requirement.
- [ ] Confirm stabiliser power is drained from reactor FE, and that redstone blocks/signals no longer activate it.
