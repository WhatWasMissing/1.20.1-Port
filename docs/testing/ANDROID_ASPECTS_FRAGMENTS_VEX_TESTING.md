# Android Aspects / Fragments + Vex Mythoclast Testing

## Android loadout

1. Convert to Android and press **L**. The Aspect / Fragment Matrix should open.
2. Equip one Aspect. Confirm its Fragment capacity is added.
3. Equip a second Aspect. Confirm no third Aspect can be equipped.
4. Fill Fragment slots to capacity. Confirm additional Fragments are rejected.
5. Remove an Aspect while over the new Fragment capacity. Confirm excess Fragments are automatically trimmed.
6. Die/revive and relog. Confirm Aspects and Fragments persist.
7. Confirm Ascension-tree perks remain unchanged while swapping Aspects/Fragments.

### Gameplay checks

- Vanguard Protocol: compare Android ability damage and ordinary powered attacks before/after.
- Temporal Overdrive: above 75% Android FE, confirm speed/strength overclock while powered.
- Aegis Weave: compare incoming damage before/after.
- Nanite Bastion: below 50% health, confirm powered repair.
- Hunter-Killer Array: confirm nearby hostile mobs receive Glowing through terrain.
- Recursive Core: confirm ability hits return FE and held-battery crouch charging is faster.
- Test each Fragment individually, especially Barrier/Veil while Force Field/Cloak is active.

## Vex Mythoclast

1. Obtain/craft `matteroverdrive:vex_mythoclast`.
2. Charge it using an FE-capable charging block. Capacity is 96,000 FE.
3. Hold right click: primary mode should fire automatically every 3 ticks and consume 600 FE per shot.
4. Kill enemies with primary fire. Confirm Temporal Charges increase to a maximum of 3.
5. Shift-right-click with at least one charge to enter **Temporal Linear** mode.
6. Hold/release right click for at least 12 ticks. Confirm a high-damage linear shot fires, consumes 3,200 FE, and consumes one Temporal Charge.
7. Confirm linear mode automatically exits when no Temporal Charges remain.
8. Confirm Shift-right-click cannot enter linear mode at zero Temporal Charges.
9. Confirm primary and linear beams use distinct visual intensity/pitch.
10. Confirm the item appears in the Matter Overdrive creative tab and the crafting recipe resolves.

### Model / presentation checks

- Confirm the exotic now renders `models/item/vex_mythoclast.obj`, not the Phaser Rifle placeholder.
- Inventory/GUI: the entire stock, split muzzle prongs and side fins should remain inside the item preview without severe clipping.
- First person: grip should sit near the player's hand and the twin forward prongs should point away from the camera.
- Third person: rifle should sit across the player's hands rather than intersecting the torso.
- Ground/fixed display: the longer Mythoclast silhouette should remain readable and not be oversized.
- Confirm the central radiolarian chamber, muzzle channel and upper sight use the cyan glow material, with bronze, dark Vex metal and ivory sections remaining visibly distinct.

## Asset provenance

The Mythoclast mesh and material palette in this branch are an original low-poly Matter Overdrive implementation created specifically for this port. No mesh, texture, sound, animation or configuration was copied from Destiny, the TACZ Destiny gun pack, Sketchfab, Thingiverse or another third-party model. Existing public models were used only as general visual reference for the recognizable fusion-rifle silhouette.
