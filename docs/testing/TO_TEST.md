# Main Runtime Regression Checklist

Branch: `main`

Use this list after a fresh pull and client launch. Record the expected result, actual result, block/item orientation, and relevant log lines for any failure.

## Build and world gate

- [ ] Run the normal development build/client launch.
- [ ] Reach the main menu without registry, data-pack, model, or menu errors.
- [ ] Create or load a test world successfully.
- [ ] Confirm the new block recipes appear in the recipe book/JEI where applicable.

## Reactor, anomaly, and stabilizers

- [ ] Assemble a valid Fusion Reactor and confirm its controller/guide UI reports structure, matter, FE, and anomaly state.
- [ ] Feed an item into the event horizon and confirm it is consumed and anomaly mass visibly increases.
- [ ] Feed a living entity into the horizon and confirm its death increases mass once, not repeatedly after ticks or a world reload.
- [ ] Connect Reactor IO to a Charging Station with a valid cable route. Confirm the station receives FE while the reactor is producing.
- [ ] Open the Charging Station GUI; insert a rechargeable battery and confirm its charge rises gradually, persists through reload, and can be retrieved.
- [ ] Connect a Gravitational Stabilizer to reactor FE. Confirm it is inactive with no power and suppresses the aligned anomaly when powered.
- [ ] Open the Stabilizer GUI and install Power Upgrades. Confirm upgrades persist/drop correctly and reduce anomaly pull rather than increasing it.
- [ ] Confirm redstone blocks/signals no longer activate stabilizers.

## Weapons

- [ ] In Survival, an empty energy weapon must not fire. The explicit Creative Battery remains the only intended infinite-energy module.
- [ ] Test Phaser, Phaser Rifle, Ion Sniper, and Plasma Shotgun through the Weapon Station: compatible modules install, persist, and do not duplicate through normal or shift-click removal.
- [ ] Check first- and third-person holding for all four weapons. The current transforms still need visual confirmation/tuning; report a screenshot with the weapon name and camera mode if misaligned.
- [ ] Confirm reload, heat/cooling, projectile/beam effects, and energy use still behave after weapon editing.

## Machines, storage, and visuals

- [ ] Open each implemented machine GUI at normal and a second GUI scale; confirm slots, text, progress bars, and player inventory align.
- [ ] Check Industrial Glass and other transparent blocks in-world for correct transparency.
- [ ] Check Tritanium Crates and the Inscriber in-world and inventory for correct 3D models/textures.
- [ ] Confirm Tritanium Crate storage, Transporter targeting, Pattern Storage/Monitor, Matter machines, Solar Panel, and Energy Pipe retain their existing behaviour.

## Pass criteria

The build is ready for the next original-mod feature slice when the build/world gate passes and there are no new crashes, data-pack errors, item loss/duplication, reactor power regressions, or anomaly/stabilizer contradictions.
