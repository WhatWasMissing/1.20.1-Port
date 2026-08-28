# Gun System Progress

## Current branch

`feature/gun-system`

## Latest fixes

- Weapon Station inventories now save and reload through block-entity NBT.
- Weapon Station contents mark the block entity dirty when changed.
- Weapon Station shift-click bounds are limited to the slots actually exposed by the menu.
- Weapons can reload from charged FE batteries in the player inventory or off-hand.
- Energy packs remain automatic reload items.
- Shift-right-click reloads non-Phaser weapons in Survival and Creative.
- Other guns are no longer treated as reload batteries.
- Charged weapon batteries are consumed once per reload and no longer drained per shot.

## Important testing note

Creative mode bypasses weapon energy consumption. Use Survival mode when testing battery reload, energy costs, and empty-weapon behavior.

## Next work

- Apply modules to guns through the Weapon Station.
- Replace the placeholder station screen with the finished layout and slot textures.
- Add the remaining gun recipes and advanced legacy weapon behavior.
