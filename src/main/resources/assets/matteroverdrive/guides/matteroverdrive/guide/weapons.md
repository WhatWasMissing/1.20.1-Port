---
navigation:
  title: Weapons
  parent: index.md
  position: 6
  icon: matteroverdrive:phaser_rifle
---
# Weapons

Matter Overdrive energy weapons are rechargeable weapons with their own heat, charge and module state. They are not intended to behave like ordinary bows with infinite ammunition: power management and heat are part of the weapon loop.

## Current weapon family

The 0.6 line includes the restored Matter Overdrive weapon framework and player-facing entries such as the **Phaser Rifle**, **Ion Sniper** and **Plasma Shotgun**. Exact handling differs by weapon type, but they share the same basic rules: internal FE, heat, reload/charging behavior and supported weapon modules.

## Weapon HUD

When a supported energy weapon is held, the lower-right HUD reports:

- weapon name;
- internal FE and capacity;
- current heat and maximum heat;
- READY / OVERHEATED / empty-energy state;
- the contextual firing/reload or Phaser mode information.

Use this readout before diagnosing firing problems. A weapon at zero internal FE should not be treated as ready even if another unrelated powered item is somewhere in the inventory.

## Reloading and portable power

Supported Matter Overdrive batteries/energy packs can be used by the weapon system where intended. **Unrelated energy weapons are not batteries.** This avoids one weapon silently draining another weapon's stored FE.

For non-Phaser weapons the HUD advertises the current reload input as **Shift + Use**. Reloading transfers energy through the supported Matter Overdrive energy-item path rather than creating charge for free.

The **Charging Station** is the straightforward way to recharge compatible portable energy storage between fights.

Creative mode does not make an ordinary configured gun ignore its FE rules. The explicit **Creative Battery** module is the infinite-energy path.

## Heat

Firing builds heat. The heat bar changes state as the weapon approaches its limit, and an overheated weapon must recover before it can be used normally again.

A weapon that has plenty of FE but refuses to fire may therefore be overheated rather than unpowered. Check both bars.

## Phaser modes

The Phaser uses selectable power modes rather than the exact same presentation as the other firearms. The HUD reports the current **STUN/KILL** mode and power level. Higher-output settings should be treated as more demanding weapon states rather than cosmetic labels.

## Weapon Station

The **Weapon Station** is the configuration and servicing machine for compatible weapons. Use it to install supported modules/components and inspect the weapon's configuration instead of modifying NBT or swapping arbitrary items by hand.

The module set includes restored component types such as:

- battery/capacity module;
- color/presentation module;
- damage focusing barrel module;
- incendiary/fire barrel module;
- explosive barrel module;
- regenerative/healing converter;
- specialised Doomsday/VENOM barrel variants;
- ricochet/rebound module;
- sniper scope.

Weapon FE is stored on the weapon stack while the installed battery module sets the weapon's effective capacity. If changing/removing a battery reduces that capacity, excess weapon FE is now discarded immediately and persistently rather than remaining hidden until a larger battery is reinstalled.

Not every module is necessarily valid for every weapon or slot. The station UI is the authority for what a particular weapon accepts.

## Survival checks

If a weapon behaves incorrectly, test it in this order:

1. Is there internal FE?
2. Is the weapon overheated?
3. Is it in the expected firing/power mode?
4. Does Shift + Use reload from a supported charged item?
5. Does the Weapon Station retain installed parts after closing and reloading the world?
6. Does firing consume the weapon's intended source rather than another gun?
7. Does removing/swapping a battery clamp internal FE to the new capacity permanently?

The 0.6 testing line has specifically addressed zero-energy firing, cross-weapon energy drain, heat/reload state, battery-capacity shrink and weapon-station persistence, so regressions in those areas are worth reporting with the held weapon, charge values and game mode.

## Rendering and placement

Weapon models use custom first/third-person presentation. If a weapon looks correct in inventory but is invisible, backwards or badly positioned in the hand, that is a rendering/transform issue rather than an energy or recipe issue. Include first-person and third-person screenshots when reporting those problems.
