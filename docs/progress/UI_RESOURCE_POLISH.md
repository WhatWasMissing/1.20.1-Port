# UI and Resource Polish

Branch: `feature/easy-parity-systems`

## First polish pass

This pass is intentionally cosmetic. It does not change machine recipes, inventories, FE/matter logic, networking, reactor behavior, or weapon behavior.

Implemented scope:

- added a shared dark Matter Overdrive machine-screen style;
- standardized slot frames, energy/matter meters, progress bars, section panels, and readable labels;
- retained always-visible debug information while grouping it into less intrusive status areas;
- cleaned up the Decomposer, Matter Recycler, Matter Analyzer, Replicator, Solar Panel, Molecular Inscriber, and Transporter screens;
- restored existing legacy machine face textures where the 1.20.1 model was stretching one fallback texture over all six faces;
- no newly generated PNG artwork was added in this pass: the block-model fixes reuse legacy textures already present in the repository.

## Texture/model corrections

- Decomposer: restored its dedicated top texture while retaining the shared machine base on the other faces.
- Matter Analyzer: uses the analyzer front and top textures instead of repeating the front on every face.
- Matter Recycler: keeps recycler side art on side faces and uses the machine base for top/bottom.
- Replicator: restores the dedicated front face rather than using the body texture everywhere.
- Transporter: restores separate front, side, and top textures.
- Solar Panel: keeps the solar texture on the upper face and uses the machine base on the body faces.

## Known visual limitations

- Several machine blocks do not yet expose a horizontal-facing blockstate, so a restored front face is fixed to model north. Adding safe placement/orientation and save compatibility is a later polish pass.
- Animated active-state faces remain deferred until the relevant blockstate/state synchronization exists.
- Complex legacy OBJ/custom-rendered models are not being re-enabled blindly; they need a dedicated 1.20.1 renderer pass.
- Fusion Reactor screens/overlays and the Weapon Station can receive focused second-pass polish after this common machine-screen pass is runtime-tested.

## Runtime test plan

- Open every polished machine screen and verify all real slots line up exactly with their rendered slot frames.
- Test normal GUI scale plus at least one larger/smaller GUI scale and check for clipping or overlapping text.
- Confirm the `INF FE` debug control still works on every screen that exposes it.
- Confirm live energy, matter, progress, recipe, network, and debug values still update.
- Inspect Decomposer, Analyzer, Recycler, Replicator, Transporter, and Solar Panel from every side in-world.
- Confirm no purple/black missing-texture faces appear.
- Confirm the restored top/front/side artwork appears on the expected model faces.
- Check the corresponding inventory item models for new model-bake or missing-texture errors.
- Save/reload the test world and verify the visual changes do not affect machine state or functionality.
- Run the normal M2 build/runtime gate and inspect the log for model/resource warnings.
