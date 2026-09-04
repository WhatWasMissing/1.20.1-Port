# Gun Model Placement Testing

This checklist covers the September 2026 gun placement pass for the OBJ weapon models.

## 1.12.2 restoration baseline

The current placement pass deliberately uses the original Matter Overdrive 1.12.2 weapon geometry and renderer transforms as the baseline instead of the temporary transformed `_item.obj` copies.

Restored model targets:

- Phaser -> `models/item/phaser2.obj`
- Phaser Rifle -> `models/item/phaser_rifle.obj`
- Ion Sniper -> `models/item/ion_sniper.obj`
- Plasma Shotgun -> `models/item/plasma_shotgun.obj`

The 1.12.2 renderer used a shared weapon transform with a Phaser-specific GUI override. Those original perspective values have been translated into 1.20.1 item-display transforms for the first runtime pass. Left-hand transforms are mirrored equivalents because the old renderer was main-hand focused.

Important: the old first-person recoil/zoom behavior lived in a dedicated `WeaponRenderHandler`, not in the item JSON. This pass restores the original models and static placement first; recoil/ADS animation parity should be judged separately from base placement.

## Weapons

Test all four weapons:

- Phaser
- Phaser Rifle
- Ion Sniper
- Plasma Shotgun

## Placement checks

For each weapon verify:

- Inventory/GUI model is visible and not mirrored.
- First-person right-hand model points forward and sits beside/below the crosshair rather than backwards through the player camera.
- First-person left-hand model mirrors the right-hand placement cleanly.
- Third-person right-hand model points away from the player and is held around the grip/stock area.
- Third-person left-hand model mirrors correctly.
- Dropped/ground model is visible, sensibly sized, and lies/orients consistently.
- Item-frame/fixed rendering is visible and faces a useful direction.

## Runtime checks

For each weapon:

1. Fire several times while watching the first-person model.
2. Reload and confirm the model does not jump to a reversed orientation.
3. If the weapon supports aiming/zoom, enter and leave that state and confirm the base placement is restored.
4. Switch rapidly between two guns and confirm neither becomes invisible.
5. Press F3+T and confirm all four OBJ models reload without purple/black missing textures.
6. Relog to the world and confirm placement remains unchanged.

## Regression checks

- Firing still consumes the same energy as before this pass.
- Heat/reload behaviour is unchanged.
- Weapon modules and installed upgrades are unchanged.
- Gun station contents/persistence are unchanged.
- Omni Tool behaviour is unchanged.

## Notes for visual tuning

Record issues per gun and view, for example:

- `Phaser / first person: 1 block too high`
- `Ion Sniper / third person: stock clips shoulder`
- `Plasma Shotgun / left hand: mirrored incorrectly`

Screenshots from first-person and third-person views are especially useful for a final fine-positioning pass.
