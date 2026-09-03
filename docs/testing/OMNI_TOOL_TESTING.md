# Omni Tool Runtime Testing

Branch: `testing/main`

This checklist covers the restored Omni Tool as a combined FE weapon and Tritanium-tier pickaxe, axe, shovel and hoe, including the standard weapon-module framework.

## Item, recipe and persistence

- [ ] Craft the Omni Tool using the restored shaped recipe.
- [ ] Confirm the inventory and held texture render correctly.
- [ ] Confirm the item is non-stackable.
- [ ] Confirm energy, heat and all six installed module slots survive inventory movement and save/reload.
- [ ] Confirm `/give @s matteroverdrive:omni_tool` gives the functional item rather than a generic placeholder.

## Energy and charging

- [ ] Charge the Omni Tool through a compatible FE charger/Charging Station and confirm the energy tooltip updates.
- [ ] Shift-right-click with an Energy Pack in inventory; confirm energy transfers and the pack is consumed in Survival.
- [ ] Shift-right-click with a charged Battery or High Capacity Battery in inventory; confirm energy transfers from that battery only.
- [ ] Confirm a Creative Battery module provides infinite weapon/tool power.
- [ ] Confirm no free FE is created when transferring between the Omni Tool and normal batteries.
- [ ] Leave the tool with 1-7 FE and confirm an 8-FE direct tool action cannot complete for free.
- [ ] Leave exactly 8 FE and confirm one direct powered tool action completes and drains the remaining FE.
- [ ] Confirm module-adjusted shot costs are respected by both firing and automatic reload; the tool must not require a hard-coded 512 FE when a barrel changes shot cost.

## Direct mining tool parity

### Pickaxe

- [ ] Mine stone, deepslate, iron ore and another high-tier pickaxe block.
- [ ] Confirm Tritanium-tier mining speed and normal drops.
- [ ] Confirm each successful powered direct block break consumes 8 FE.

### Axe

- [ ] Mine logs and wooden blocks at axe-like speed.
- [ ] Right-click a log and confirm stripping works.
- [ ] Right-click oxidized copper and confirm scraping works.
- [ ] Right-click waxed copper and confirm wax removal works.

### Shovel

- [ ] Mine dirt, sand and gravel at shovel-like speed.
- [ ] Right-click grass/dirt and confirm path flattening works.
- [ ] Right-click a lit campfire and confirm it extinguishes.

### Hoe

- [ ] Mine a hoe-tagged block and confirm powered Tritanium-tier behavior.
- [ ] Right-click valid dirt/farmland input and confirm tilling works.
- [ ] Confirm tilling consumes 8 FE and does not double-charge.

### Empty-energy behavior

- [ ] Drain the Omni Tool to 0 FE.
- [ ] Confirm powered mining speed/tool actions stop working rather than generating free work.
- [ ] Recharge it and confirm all mining functions resume without replacing the item.

## Remote mining

- [ ] Hold right-click while looking at a mineable block and confirm ranged mining begins.
- [ ] Confirm remote mining consumes 1 FE per active mining tick and does not also charge the 8-FE direct-break cost when the remote break completes.
- [ ] Confirm the mining crack progress resets when moving the crosshair to a different block.
- [ ] Confirm releasing right-click clears stale crack progress.
- [ ] Confirm remote mining never breaks unbreakable blocks.
- [ ] Confirm a visible beam is emitted while remote mining.
- [ ] Confirm the beam follows installed Color Modules.
- [ ] Confirm base remote-mining reach is about 24 blocks.
- [ ] Install a Sniper Scope and confirm remote-mining reach increases with the shared range multiplier.
- [ ] Confirm Energy Packs and charged weapon batteries can automatically refill an empty/low tool during remote mining.

## Weapon parity

- [ ] Left-click in air/entity combat input and confirm the Omni Tool fires using the server-authoritative fire packet.
- [ ] Confirm the unmodified shot consumes 512 FE.
- [ ] Confirm the base shot deals approximately 7 damage before armor/effects.
- [ ] Confirm base practical range is about 24 blocks.
- [ ] Confirm the beam follows the crosshair and does not hit through solid blocks.
- [ ] Confirm repeated fire raises heat and eventually triggers overheat.
- [ ] Confirm an overheated Omni Tool refuses to fire, cools while carried, and becomes usable again.
- [ ] Confirm the laser fire, overheat and reload sounds play without looping/sticking.
- [ ] Confirm the tooltip reports effective damage, range, cooldown, energy, heat, module count and FE costs.

## Weapon Station modules

The Omni Tool now accepts the same six-slot standard module framework as the other energy weapons. Test every category rather than expecting sights/barrels/ricochet to be rejected.

### Battery and Color

- [ ] Install a normal weapon battery and confirm capacity/charging behavior is valid.
- [ ] Install a Creative Battery and confirm infinite power behavior.
- [ ] Install several Color Modules and confirm both combat and remote-mining beams change color.

### Barrel effects

- [ ] Damage Barrel: confirm direct entity damage increases and effective damage changes in the tooltip.
- [ ] Fire Barrel: confirm living targets ignite and block impacts can light an adjacent valid air block.
- [ ] Heal Barrel: confirm living targets are healed instead of damaged.
- [ ] Explosion Barrel: confirm an impact explosion occurs without turning the Omni Tool into unrestricted terrain griefing.
- [ ] Doomsday Barrel: confirm the stronger impact explosion works and uses its module-scaled energy/cooldown values.
- [ ] VENOM / Block Barrel: confirm ordinary blocks up to the supported hardness limit can be destroyed.
- [ ] Confirm the Block Barrel cannot destroy bedrock.
- [ ] Confirm the Block Barrel no longer deals normal entity damage.
- [ ] Confirm barrel-specific effective shot energy/cooldown values shown in the tooltip match actual firing behavior.

### Sights and range

- [ ] Install Holo Sights and confirm combat spread tightens relative to an unmodified tool.
- [ ] Install a Sniper Scope and confirm combat range increases from 24 to about 36 blocks.
- [ ] Confirm Sniper Scope also changes spread according to the shared weapon accuracy rules.
- [ ] Confirm Sniper Scope extends remote-mining reach as described above.

### Ricochet

- [ ] Install Ricochet in an Other slot and fire at a solid block at an angle.
- [ ] Confirm one reflected follow-up beam/trace is produced.
- [ ] Confirm the reflected trace has reduced range and damage.
- [ ] Confirm the ricochet does not recursively bounce forever.

### Station safety

- [ ] Confirm every accepted module remains installed after closing/reopening the Weapon Station.
- [ ] Confirm invalid slot placement is rejected without deleting or duplicating the module.
- [ ] Confirm breaking the Weapon Station safely returns the Omni Tool and installed modules according to the station's normal behavior.

## Regression checks

- [ ] Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun still fire/reload/cool normally.
- [ ] Existing Weapon Station items still persist through save/reload.
- [ ] Normal Tritanium pickaxe/axe/shovel/hoe behavior is unchanged.
- [ ] Charging Station still charges normal batteries and weapons correctly.
- [ ] Dedicated server and client both launch without Omni Tool classloading errors.

## Debug values expected without modules

- Weapon damage: `7`
- Weapon range: `24 blocks`
- Shot cooldown: `18 ticks`
- Shot cost: `512 FE`
- Max heat: `80`
- Tool tier/speed: current Tritanium tier (`9.0` mining speed)
- Direct powered tool-action cost: `8 FE`
- Remote mining cost: `1 FE/tick`
- Module slots: `6` (Battery, Color, Barrel, Sights, Other, Other)

The Omni Tool pass is ready to mark runtime-complete when all sections above pass and no FE, block-drop, item or module duplication is observed.
