# Testing Branch Audit Fixes

Branch: `testing/main`

This checklist covers the code issues found during the full-system static audit and fixed before the next merge-to-main decision.

## Gravitational Anomaly mass

- [ ] Record anomaly mass.
- [ ] Put a high-health living entity inside the event horizon and allow it to survive several damage cycles.
- [ ] Confirm the entity takes horizon damage but anomaly mass does **not** rise on each damage tick.
- [ ] When the entity dies to the event horizon, confirm anomaly mass increases once.
- [ ] Repeat with dropped item stacks. Confirm items are consumed once and add their matter value once.
- [ ] Save/reload and confirm mass persists.

## Weapon battery reload

- [ ] Charge a normal Battery to only 1 FE and carry it with an empty energy weapon.
- [ ] Attempt to fire/reload. Confirm the battery can contribute only its real 1 FE and cannot fill the weapon.
- [ ] Repeat with a partially charged Battery and HC Battery. Confirm weapon charge gained matches FE removed from the battery.
- [ ] Confirm drained batteries remain as items and can be recharged in the Charging Station.
- [ ] Carry several partially charged batteries. Confirm reload can draw from multiple batteries until the requested shot/full reload charge is met.
- [ ] Confirm an Energy Pack still contributes 32,000 FE and is consumed normally.
- [ ] Confirm weapons still cannot fire when the weapon plus available reload sources cannot meet the shot cost.

## Weapon Station break safety

- [ ] Put a weapon with modules into the Weapon Station and close/reopen the GUI. Confirm persistence.
- [ ] Leave a weapon/modules in the station and break the block.
- [ ] Confirm the weapon is dropped with its installed modules packed back into it and no station contents disappear.
- [ ] Place the station again and repeat with an unmodified weapon.

## Fusion Reactor upgrades

### Speed

- [ ] Run a valid reactor with no upgrades and record FE generated/tick and matter drain.
- [ ] Add one Speed Upgrade.
- [ ] Confirm FE generation rises rather than falls/staying unchanged.
- [ ] Confirm matter drain rises proportionally so Speed does not create free extra FE per unit matter.
- [ ] Add/remove multiple Speed Upgrades and confirm the debug values update immediately.

### Range

- [ ] With no Range Upgrade, confirm the vertical anomaly search remains the base 3 blocks from the ring centre.
- [ ] Move the anomaly outside the base range and confirm the reactor becomes invalid.
- [ ] Add a Range Upgrade and confirm the reactor can validate an anomaly farther away.
- [ ] Confirm upgraded search distance is hard-capped at 16 blocks even with stacked Range Upgrades.
- [ ] Confirm removing Range Upgrades revalidates the structure promptly and invalidates an anomaly that is now out of range.

## Inventory-loss regression

- [ ] Put an item in a Network Router filter slot, break the Router, and confirm the filter item drops.
- [ ] Put a battery in a Charging Station, break it, and confirm the battery drops.
- [ ] Put upgrades in a Gravitational Stabilizer, break it, and confirm they drop.
- [ ] Put a weapon/modules in a Weapon Station, break it, and confirm all contents are preserved.

## Visual/runtime follow-up

These cannot be proven by the Java build alone and still need an in-client pass:

- [ ] Industrial Glass/Bounding Box/Matter Plasma/Molten Tritanium render with transparency.
- [ ] Tritanium armour uses the correct equipped textures.
- [ ] Weapon first-person/third-person held transforms are aligned correctly.
- [ ] Tritanium crate faces/UVs are correct.
- [ ] Inscriber block texture/model alignment is correct.
- [ ] Inventory item models and placed-block models match where expected.
