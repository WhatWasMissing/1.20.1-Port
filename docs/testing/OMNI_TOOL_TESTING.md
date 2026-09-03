# Omni Tool Runtime Testing

Branch: `testing/main`

This checklist covers the restored Omni Tool as a combined FE weapon and tritanium-tier pickaxe, axe and shovel.

## Item, recipe and persistence

- [ ] Craft the Omni Tool using the restored shaped recipe.
- [ ] Confirm the inventory and held texture render correctly.
- [ ] Confirm the item is non-stackable.
- [ ] Confirm energy, heat and installed modules survive inventory movement and save/reload.
- [ ] Confirm `/give @s matteroverdrive:omni_tool` gives the functional item rather than a generic placeholder.

## Energy and charging

- [ ] Charge the Omni Tool through a compatible FE charger/Charging Station and confirm the energy tooltip updates.
- [ ] Shift-right-click with an Energy Pack in inventory; confirm energy transfers and the pack is consumed in Survival.
- [ ] Shift-right-click with a charged Battery or High Capacity Battery in inventory; confirm energy transfers from that battery only.
- [ ] Confirm a Creative Battery module provides infinite weapon/tool power.
- [ ] Confirm no free FE is created when transferring between the Omni Tool and normal batteries.

## Mining tool parity

### Pickaxe

- [ ] Mine stone, deepslate, iron ore and another high-tier pickaxe block.
- [ ] Confirm tritanium-tier mining speed and normal drops.
- [ ] Confirm each successful powered block break consumes FE.

### Axe

- [ ] Mine logs and wooden blocks at axe-like speed.
- [ ] Right-click a log and confirm stripping works.
- [ ] Right-click oxidized copper and confirm scraping works.
- [ ] Right-click waxed copper and confirm wax removal works.

### Shovel

- [ ] Mine dirt, sand and gravel at shovel-like speed.
- [ ] Right-click grass/dirt and confirm path flattening works.
- [ ] Right-click a lit campfire and confirm it extinguishes.

### Empty-energy behavior

- [ ] Drain the Omni Tool to 0 FE.
- [ ] Confirm powered mining speed/tool actions stop working rather than generating free work.
- [ ] Recharge it and confirm all mining functions resume without replacing the item.

## Weapon parity

- [ ] Hold right-click and confirm the Omni Tool fires repeatedly rather than performing a one-shot use.
- [ ] Confirm each shot consumes 512 FE.
- [ ] Confirm the base shot deals approximately 7 damage before armor/effects.
- [ ] Confirm maximum practical range is about 24 blocks.
- [ ] Confirm the beam follows the crosshair and does not hit through solid blocks.
- [ ] Confirm repeated fire raises heat and eventually triggers overheat.
- [ ] Confirm an overheated Omni Tool refuses to fire, cools while carried, and becomes usable again.
- [ ] Confirm the laser fire, overheat and reload sounds play without looping/sticking after firing stops.

## Weapon Station modules

### Supported

- [ ] Install a normal weapon battery and confirm capacity/charging behavior is valid.
- [ ] Install a Creative Battery and confirm infinite power behavior.
- [ ] Install several Color Modules and confirm the beam color changes.
- [ ] Install the VENOM / Block Barrel and confirm shots can destroy ordinary blocks with hardness up to the supported limit.
- [ ] Confirm the Block Barrel cannot destroy bedrock.
- [ ] Confirm the Block Barrel no longer deals normal entity damage.

### Rejected

- [ ] Confirm holo sights and sniper scope are rejected.
- [ ] Confirm ricochet is rejected.
- [ ] Confirm damage, fire, heal, explosion and doomsday barrels are rejected.
- [ ] Confirm rejected modules are not deleted or duplicated by the Weapon Station.

## Regression checks

- [ ] Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun still fire/reload/cool normally.
- [ ] Existing Weapon Station items still persist through save/reload.
- [ ] Normal tritanium pickaxe/axe/shovel behavior is unchanged.
- [ ] Charging Station still charges normal batteries and weapons correctly.
- [ ] Dedicated server and client both launch without Omni Tool classloading errors.

## Debug values expected

- Weapon damage: `7`
- Weapon range: `24 blocks`
- Shot cooldown: `18 ticks`
- Shot cost: `512 FE`
- Max heat: `80`
- Tool tier/speed: current Tritanium tier (`9.0` mining speed)
- Powered tool-action cost: `8 FE`
- Supported special modules: Battery, Color, VENOM / Block Barrel

The Omni Tool pass is ready to mark runtime-complete when all sections above pass and no FE, block-drop, item or module duplication is observed.
