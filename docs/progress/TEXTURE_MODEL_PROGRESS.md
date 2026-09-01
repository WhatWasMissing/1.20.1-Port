# Texture and model refinement pass

## Implemented

- Tritanium armour now resolves its worn layer textures from the active `textures/armor` directory.
- Weapon Station uses a complete 176x166 container background with visible module and inventory slots.
- Weapon Station, Decomposer, Replicator, Transporter, Inscriber and Pattern Monitor models use their dedicated face artwork.
- Transporter, Inscriber and Pattern Monitor now face the player when placed.
- Replicator blockstate rotation now follows its existing facing state for both active states.
- Replicator and Inscriber legacy texture atlases are split into correctly mapped 16x16 block faces.
- Quantum Fold Manipulator, Reactor Remote and Reactor Assembly Guide have distinct item icons.
- Tritanium Spine uses the correctly spelled texture path while retaining the legacy file for compatibility.
- Gun OBJ meshes are centred, scaled and rotated into Minecraft item space, with explicit transforms for GUI, ground, frames and both hands.
- Tritanium Crates now use the original legacy 3D crate mesh for all colour variants instead of mapping the atlas across a full cube.
- Inscriber now uses the original legacy 3D mesh and its atlas, with blockstate rotations matched to the legacy model orientation.
- Industrial Glass, Bounding Box, Matter Plasma and Molten Tritanium declare their render types in model JSON so modern Forge routes them through the intended transparent render layers.
- Energy weapons now enforce their internal FE requirement in Creative as well as Survival; Creative mode no longer makes a zero-energy gun fire indefinitely, while the installed Creative Battery module remains the explicit infinite-energy option.
- Anomaly consumption balancing now applies its mass bonus only when the anomaly's mass actually changes, preventing the previous consumed-entity sample from being multiplied again every later tick or once more after a world reload.
- Charging Station now stores one rechargeable FE item and charges it continuously from adjacent FE sources instead of applying a one-click energy burst. The station can transfer up to 4,096 FE/t, while each battery's own receive limit still applies; the standard battery therefore charges at 400 FE/t and the HC battery at 4,096 FE/t.

## Test checklist

1. Equip each Tritanium armour piece and confirm helmet, chest, legs and boots render on the player.
2. Open the Weapon Station and verify all seven module slots and all 36 inventory slots align with the background.
3. Place the six refined machines and confirm their top/front/side faces are distinct.
4. Place a Transporter, Inscriber, Pattern Monitor and Replicator while facing each cardinal direction.
5. Confirm existing machine inventories and saved contents survive a save/reload after the new facing states are introduced.
6. Check the Quantum Fold Manipulator, Reactor Remote, Reactor Assembly Guide and Tritanium Spine in inventory and in-hand.
7. Check every gun in inventory, dropped on the ground, in an item frame, first person and third person in both hands.
8. Fire and charge every gun to confirm use animations do not reverse or hide the corrected model.
9. Place the base Tritanium Crate plus several coloured variants and verify the 3D frame/lid geometry and atlas mapping are correct in-world and as items.
10. Place the Inscriber facing north/east/south/west and verify its 3D shape, atlas alignment and front orientation.
11. Place Industrial Glass, Bounding Box, Matter Plasma and Molten Tritanium and verify intended transparent/cutout pixels render correctly from multiple viewing angles.
12. In both Survival and Creative, drain each normal weapon to zero FE and confirm it cannot fire again until reloaded; then verify an installed Creative Battery still provides infinite firing as intended.
13. Feed one living entity into an anomaly, note the mass increase, then wait at least 10 seconds with nothing else entering it and confirm the mass stays stable; reload the world and confirm it still does not gain that old bonus again, then feed a second entity and verify exactly one new mass jump occurs.
14. Connect the Charging Station to an adjacent FE source, insert an empty standard battery, verify its charge rises gradually at 400 FE/t rather than instantly, leave it charging through a save/reload, then retrieve it with an empty hand. Repeat with an HC battery and verify it charges at up to 4,096 FE/t. Break a station containing a battery and verify the battery drops instead of being deleted.

## Known follow-up

- Gun module geometry is not yet attached to the corrected base OBJ meshes; module effects remain functional and visible in tooltips.
- Restored OBJ models and model-level render types still require runtime visual verification in the development client.
