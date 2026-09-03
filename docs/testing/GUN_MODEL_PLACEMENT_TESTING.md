# Gun Model Placement Testing

This checklist covers the September 2026 gun placement pass for the OBJ weapon models.

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
