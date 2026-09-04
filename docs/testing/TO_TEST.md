# Matter Overdrive 1.20.1 - Alpha Runtime Testing Checklist

Branch: `testing/alpha`
Legacy reference: MatterOverdrive 1.12.2 `0.7.1.0` jar and recovered source/resources
Build identity: `Alpha Version 3`, made by MVQ1303

This checklist is also bundled in-game as the **M2 Testing Checklist** item. A successful GitHub Actions build proves the project compiles and packages; it does not prove an in-world model, renderer, save, network or gameplay path is correct.

## Already runtime-verified in the previous alpha test

These do not need a full retest unless a later item below touches them:

- [x] Rogue Android melee combat works.
- [x] Rogue Android sounds work.
- [x] Failed Cow, Pig, Sheep and Chicken all spawn and behave correctly.
- [x] Mad Scientist interaction and the current Puny Humans quest slice work.

## Priority 1 - Holo Sign regression retest

The latest screenshots confirm the two original failures are fixed: the sign is now a thin monitor and renamed-item sneak-use programs it. A third presentation issue is now isolated: the hologram still faces the camera instead of staying anchored to the monitor plane, so it visibly floats away from the panel from side/top angles.

- [x] Place a Holo Sign. It renders as the original thin monitor panel rather than a full cube.
- [ ] Place it while facing north, east, south and west. The monitor face and collision outline must rotate together.
- [x] Rename any item in an anvil, then sneak-use it on the sign. Programming works and the text is applied without the old placement failure.
- [ ] Confirm holographic text is readable, scales down for a long name, and remains physically anchored just in front of the monitor face from front/side/top views. **Current screenshots: readable, but camera-billboarded/detached.**
- [ ] Sneak-use with an empty hand and confirm the text clears.
- [ ] Save/reload and leave/re-enter the chunk. Confirm programmed text persists and resynchronises.
- [ ] Claim the sign with a Security Protocol and verify owner/access protection still applies.

## Priority 2 - Full source texture/model pass

This pass uses the legacy jar model geometry and original textures rather than wrapping legacy atlases around generic cubes. Check the following in-world and as inventory items.

### Legacy-shaped stations and displays

- [ ] Android Station is a low stepped 9-pixel platform with the original top/bottom/side artwork, not a cube.
- [ ] Weapon Station is the matching stepped platform using Weapon Station artwork.
- [ ] Star Map uses the stepped station geometry with the original Star Map side artwork.
- [ ] Contract Market is a thin monitor with a holographic front, network-port back and base-textured edges.
- [ ] Pattern Monitor is a thin monitor rather than a full cube and faces the player when placed.
- [ ] Microwave uses the original compact geometry and front/back/side textures rather than one texture on all six cube faces.
- [ ] Space-Time Accelerator uses the original narrow three-stage column shape rather than a Base-textured cube.
- [ ] Solar Panel is visually half-height with the original panel top and Base sides/bottom.

### Legacy machine face and geometry restoration

- [ ] Matter Analyzer uses its original detailed top geometry, Analyzer front, Network Port back, Vent side and Base bottom.
- [ ] Place Matter Analyzer north/east/south/west and confirm its front follows placement direction. Repeat while active and inactive.
- [ ] Decomposer front is the tank, top is Decomposer top, bottom is Vent2 and side/back is Base Stripes. Check all four facings and both active states.
- [ ] Matter Recycler uses Recycler sides with legacy top/bottom mapping. Check all four facings and both active states.
- [ ] Replicator uses the restored legacy 3D model/UV layout and points the intended front toward the player. Check all four facings and both active states.
- [ ] Pattern Storage uses the original legacy OBJ mesh and texture mapping, not a cube. Check all four facings.
- [ ] Charging Station uses the original tall legacy OBJ mesh and correct Charging Station atlas. Check all four facings.

### Previously restored visual regressions

- [ ] Tritanium Crates still use the legacy 3D crate mesh for base and coloured variants.
- [ ] Inscriber still uses its legacy OBJ model and faces correctly in all four directions.
- [ ] Industrial Glass remains transparent/cutout rather than opaque.
- [ ] Bounding Box, Matter Plasma and Molten Tritanium retain their intended translucent rendering.
- [ ] Tritanium armour renders correctly when worn.
- [ ] Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool remain visible and sensibly positioned in GUI, ground, item frame, first person and third person.

### Save compatibility after new facing properties

Contract Market, Microwave, Pattern Storage and Charging Station now have explicit horizontal facing states for their restored directional models.

- [ ] Load an existing test world containing each of these blocks and confirm there is no missing-state or registry error.
- [ ] Verify existing inventories, FE, matter, upgrades, contracts, drives and charging contents are intact.
- [ ] Break/re-place each block in every direction and confirm it faces the player correctly.
- [ ] Save/reload after re-placement and confirm both facing and machine contents persist.

## Priority 3 - Security Protocol

- [ ] Sneak-use Empty Security Protocol in air: it binds to the player and becomes Claim.
- [ ] Continue sneak-using: Claim -> Access -> Remove -> Claim while retaining owner UUID.
- [ ] Claim representative block-entity machines and confirm Claim is consumed.
- [ ] Non-owner Survival player without matching Access cannot use or break a claimed machine.
- [ ] Matching Access in inventory grants use/break access; mismatched Access does not.
- [ ] Matching Remove clears ownership and is consumed.
- [ ] Ownership and protocol owner NBT persist through save/reload.
- [ ] Include Holo Sign, Tritanium Crate, Decomposer, Replicator, Pattern Storage, Weapon Station, Android Station and Reactor IO/controller in the spot check.

## Priority 4 - Core regression after visual changes

- [ ] Decomposer, Recycler, Analyzer, Pattern Storage, Pattern Monitor and Replicator still complete their normal FE/matter/pattern workflows.
- [ ] Microwave still cooks only valid food recipes and returns contents safely on break.
- [ ] Charging Station still charges a standard Battery at its item receive limit and an HC Battery at up to the station transfer limit.
- [ ] Solar Panel still generates and exports FE.
- [ ] Weapon Station still preserves installed weapon/modules and returns contents on break.
- [ ] Android Station still installs parts and charges nearby Android players.
- [ ] Contract Market still provides/redeems contracts and survives save/reload.
- [ ] Star Map screen still opens and reports viewer-specific contract status.

## Reactor/anomaly regression

- [ ] Ring validation, Reactor IO, cable output and matter input still work.
- [ ] Shared ring power and stabilizer reactor-power integration still work.
- [ ] One consumed living entity adds anomaly mass exactly once; waiting/reloading does not repeat the old bonus.
- [ ] Reactor output scales with anomaly mass and is not stuck at the old fixed-cap behaviour.
- [ ] RUN/SCRAM, redstone mode, remote control, overlay and persistence still work.

## Weapon/Android regression

- [ ] Empty normal weapons cannot fire in Survival or Creative unless the explicit Creative Battery path is installed.
- [ ] Weapons cannot drain unrelated guns to pay shot energy.
- [ ] Heat/reload/cooldown behaviour still works.
- [ ] Android HUD, V cycle, B activate and K skill tree still work.
- [ ] Selected perks survive level-up, relog and death and only reset through the confirmed reset/refund flows.

## Known visual follow-up not claimed complete in this pass

- Connected Matter Pipe, Heavy Energy Cable and Network Pipe geometry is still a modern full-block approximation. The original used centre plus six connection pieces and needs a dedicated connection-state renderer/model pass.
- Pylon gameplay works, but the complete legacy multi-block OBJ/overlay presentation still needs a dedicated renderer-safe pass.
- Legacy emissive/overlay/connected-texture effects are not all reproduced by ordinary model JSON.
- Gun base transforms are restored, but every legacy module mesh, recoil/zoom animation and renderer detail is not yet at full parity.
- A model being copied from the legacy jar does not guarantee a shader/resource-pack combination will render it correctly; report screenshots for any remaining mismatch.

## Pass criteria for this build

The visual pass can be marked runtime-passed when the Priority 1 and Priority 2 checks are correct in a fresh world and an existing test world, there are no purple/black missing textures or model-bake errors, directional machines retain their contents, and the Priority 4 gameplay regressions remain functional.
