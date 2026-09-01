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

## Test checklist

1. Equip each Tritanium armour piece and confirm helmet, chest, legs and boots render on the player.
2. Open the Weapon Station and verify all seven module slots and all 36 inventory slots align with the background.
3. Place the six refined machines and confirm their top/front/side faces are distinct.
4. Place a Transporter, Inscriber, Pattern Monitor and Replicator while facing each cardinal direction.
5. Confirm existing machine inventories and saved contents survive a save/reload after the new facing states are introduced.
6. Check the Quantum Fold Manipulator, Reactor Remote, Reactor Assembly Guide and Tritanium Spine in inventory and in-hand.
7. Check every gun in inventory, dropped on the ground, in an item frame, first person and third person in both hands.
8. Fire and charge every gun to confirm use animations do not reverse or hide the corrected model.

## Known follow-up

- Gun module geometry is not yet attached to the corrected base OBJ meshes; module effects remain functional and visible in tooltips.
