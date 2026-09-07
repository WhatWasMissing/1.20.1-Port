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

## Known visual placeholder

The first pass deliberately uses Matter Overdrive's existing Phaser Rifle OBJ as the visible model. No model, texture, sound, animation, or configuration from the All Rights Reserved TACZ Destiny gun pack was copied. Replace this placeholder later with an original or properly licensed Mythoclast-compatible asset.
