---
navigation:
  title: Weapons
  parent: index.md
  position: 6
  icon: matteroverdrive:phaser_rifle
---
# Weapons

Matter Overdrive energy weapons are self-contained FE weapons with heat, cooldown and module state stored on the weapon itself. In 0.6, firing requires enough **internal weapon FE** before a shot can start.

## Current energy weapons

The current weapon set includes:

- **Phaser** - short-range utility sidearm with selectable STUN/KILL power modes.
- **Phaser Rifle** - sustained mid-range energy rifle.
- **Ion Sniper** - long-range high-damage precision weapon with charge/aim behavior.
- **Plasma Shotgun** - short-range multi-pellet weapon whose charge changes pellet concentration.

All four use the same internal FE/heat framework but have different range, damage, cooldown, heat and FE-per-shot values.

## Internal FE and firing

A normal weapon must contain enough internal FE for its current shot cost. If it does not, firing is refused and the weapon reports **No internal weapon energy**. Creative mode does not make an ordinary weapon ignore this check.

The previous behavior where a zero-FE gun could silently pull power from inventory and fire in the same click has been removed. This makes the HUD state truthful: 0 FE means the gun cannot fire until it is explicitly reloaded.

## Reloading

For normal non-Phaser weapons, **Shift+Use** performs an explicit reload from a supported portable Matter Overdrive energy source.

Supported reload sources are deliberately narrow:

- Energy Packs;
- Weapon Batteries / HC Batteries that implement the weapon-battery item path;
- Creative Battery as the explicit unlimited-energy exception.

Another energy weapon is **not** a battery and should never be drained to recharge the held gun.

The Phaser uses Shift+Use for its power-mode control before the generic reload branch, so treat Phaser mode/recharge behavior as a separate interaction when testing.

## Heat and overheat

Every shot adds weapon-specific heat. Heat cools while the weapon sits in inventory. When heat reaches the weapon's maximum, the weapon enters an overheated state and refuses further firing until it has cooled sufficiently.

The HUD is expected to distinguish at least:

- READY;
- NO INTERNAL FE;
- OVERHEATED;
- active Phaser STUN/KILL mode where applicable.

Heat, overheat and internal FE are stored on the ItemStack and should survive save/reload.

## Phaser

The Phaser has multiple power settings. Lower settings apply strong slowing/mining-fatigue-style stun effects instead of normal lethal damage; higher settings use the lethal damage path. Shift+Use cycles the power mode and reports the selected mode.

The Phaser supports a more limited module set than the larger weapons, primarily barrel/color-style customization.

## Ion Sniper

The Ion Sniper uses a release-to-fire charge cycle. Longer charge improves shot quality/damage scaling and reduces spread. It has the longest base range and highest base damage of the current standard weapons.

Aiming is intentionally important. The current 0.6 target behavior uses a **0.40 base FOV multiplier**, while the recovered **Sniper Scope** module provides a **0.85 override** in the module/stat system. Unzoomed sniper recoil is intended to remain more pronounced than properly aimed recoil.

## Plasma Shotgun

The Plasma Shotgun also fires on release. Charge changes the pellet pattern: short charge produces more pellets and wider spread, while longer charge concentrates the shot into fewer, stronger pellets. Heat per discharge also depends on the resulting pellet concentration.

## Barrel and utility effects

Installed weapon modules can alter live behavior such as damage, FE use, cooldown, range, capacity, spread/aiming or special hit effects. Current barrel-style effects include paths for fire, explosion/doomsday behavior, block interaction and ricochet where supported by the weapon.

The Weapon Station is the authoritative place to inspect whether a module is accepted by a particular frame.

## Weapon Station

The Weapon Station exposes seven physical slots across HOME, MODULES and STATS views. It unpacks supported module data from the inserted weapon while editing and repacks that data when the weapon/menu is removed or closed.

Use the pages as follows:

- **HOME** - frame identity, FE, heat/overheat, module count, sight and combat-ready/reload state.
- **MODULES** - Battery, Color, Barrel, Sights, Utility A and Utility B mapping.
- **STATS** - live damage, energy, cooldown, range, capacity and aim-related values after installed-module multipliers.

Weapon Station inventory is serialized with the block entity, and weapon module state is stored back onto the weapon ItemStack.

## Troubleshooting

**Weapon says 0 FE but I have batteries:** explicitly reload; firing no longer performs an automatic inventory reload.

**A different gun lost energy:** this is a regression. Normal reload should only accept Energy Packs or WeaponBatteryItem sources.

**Weapon will not fire with FE:** check overheat and shot cooldown before assuming the energy system is broken.

**Stats do not change after a module:** re-open/refresh the STATS page and confirm the module is in the correct physical slot and is supported by that frame.

**Model looks wrong in first/third person:** weapon placement/model choreography is still a visual-parity test area even when the underlying FE/heat/module state is working correctly.
