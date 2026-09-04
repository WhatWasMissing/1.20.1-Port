# Texture and Model Parity Progress

Branch: `testing/alpha`
Legacy reference: MatterOverdrive 1.12.2 `0.7.1.0` jar and recovered source/resources.

## Why this pass was needed

A large number of port textures were already the correct original PNGs, but many 1.20.1 models were still generic `cube_all` placeholders or had incomplete face/facing mappings. That made correct legacy atlases look wrong in game. This pass checks texture file, model geometry, UV/face assignment and blockstate rotation separately.

## Previously restored

- Tritanium worn armour textures.
- Weapon Station GUI alignment.
- Tritanium Crate original OBJ mesh across colour variants.
- Inscriber original OBJ mesh and facing.
- Gun base OBJ transforms for inventory/world/hands.
- Industrial Glass, Bounding Box, Matter Plasma and Molten Tritanium render-type declarations.
- Dedicated machine face art for several machines.

## Current source-faithful restoration

- **Holo Sign:** original thin monitor panel, Holo Monitor front, Base edges/back and horizontal facing. Runtime confirms the panel/programming fix; hologram text still needs face-anchored orientation instead of camera billboarding.
- **Android Station:** original stepped station geometry and source top/bottom/side mapping.
- **Weapon Station:** original stepped station geometry and source top/bottom/side mapping.
- **Star Map:** original station geometry with `starmap_side` artwork.
- **Contract Market:** original thin display geometry, Holo Monitor front, Network Port back and Base sides; horizontal facing added.
- **Matter Analyzer:** original detailed JSON geometry/UVs and source face textures; active state no longer prevents directional model selection.
- **Decomposer:** legacy Vent2 bottom, Decomposer top, Tank front and Base Stripes side/back mapping, with all four facings across active/inactive states.
- **Matter Recycler:** legacy Vent2 bottom, Decomposer top and Recycler side/front/back mapping, with all four facings across active/inactive states.
- **Microwave:** original compact model with separate front/back/body textures; horizontal facing added.
- **Pattern Monitor:** original thin monitor geometry and facing.
- **Pattern Storage:** original legacy OBJ/MTL restored with modern Forge OBJ references and horizontal facing.
- **Replicator:** original legacy model geometry/UV layout restored with correct source-facing orientation for all active states.
- **Charging Station:** original tall legacy OBJ/MTL restored, centred for modern block coordinates and given horizontal facing.
- **Space-Time Accelerator:** original narrow three-stage column model and dedicated side/top-bottom textures.
- **Solar Panel:** restored half-height visual model with panel top and Base sides/bottom.

## Runtime tests required

1. Holo Sign four-direction/persistence regression plus the remaining hologram front-plane anchoring fix (thin geometry and programming now pass).
2. Android Station, Weapon Station and Star Map stepped geometry.
3. Contract Market, Microwave and Pattern Monitor front/back orientation in four directions.
4. Matter Analyzer, Decomposer, Recycler and Replicator in all four facings, including active/inactive states where present.
5. Pattern Storage and Charging Station OBJ model baking, UV alignment and inventory appearance.
6. Space-Time Accelerator column and Solar Panel half-height appearance.
7. Existing Inscriber and crate OBJ regression.
8. Existing transparency, armour and held-gun regression.
9. Save/reload existing worlds containing newly directional Contract Market, Microwave, Pattern Storage and Charging Station.
10. Confirm machine contents, FE/matter/upgrades/contracts/drives survive the state additions.

## Still intentionally incomplete

- Matter Pipe, Heavy Energy Cable and Network Pipe still need centre-plus-six-connection geometry/state parity.
- Pylon needs a dedicated safe pass for its original multi-block OBJ and overlay presentation.
- The old Charging Station connected-texture/overlay layer is not yet fully recreated; the base source mesh is restored first.
- Some legacy glow/emissive/overlay/CTM effects require dedicated modern render code rather than static model JSON.
- Gun module meshes, recoil, zoom and final hand animation remain deeper renderer work.

These items remain open even if the corresponding PNG/OBJ files are present. Visual parity is only marked complete after an in-game pass.
