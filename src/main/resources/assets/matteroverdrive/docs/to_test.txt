# Matter Overdrive 1.20.1 - Alpha Runtime Testing Checklist

Branch: `testing/main`
Legacy reference: MatterOverdrive 1.12.2 `0.7.1.0` jar and recovered source/resources
Build identity: `Alpha Version 3`, made by MVQ1303

This file is bundled in-game as the **M2 Testing Checklist**. GitHub Actions compilation is not a substitute for runtime verification.

## Already runtime-passed

- [x] Rogue Android combat.
- [x] Rogue Android sounds.
- [x] Failed Cow, Pig, Sheep and Chicken spawning/behaviour.
- [x] Mad Scientist interaction and current Puny Humans quest.
- [x] Holo Sign thin monitor geometry.
- [x] Holo Sign renamed-item programming.
- [x] Previous restored machine-model batch: Android Station, Weapon Station, Star Map, Contract Market, Analyzer, Decomposer, Recycler, Microwave, Pattern Monitor, Pattern Storage, Replicator, Charging Station, Space-Time Accelerator and Solar Panel. Latest player report: all worked except neighbour-face culling beside Pattern Monitor and Space-Time Accelerator.

## Priority 1 - fixes from the latest screenshots/test

- [ ] Put a normal full block directly against every side of Pattern Monitor. No neighbouring face should disappear.
- [ ] Repeat against Space-Time Accelerator. No neighbouring face should disappear.
- [ ] Program a Holo Sign and view it from front, both sides and above. Text must remain attached just in front of the screen rather than billboard toward the camera.
- [ ] Place Holo Signs north/east/south/west; text and panel must rotate together.
- [ ] Clear Holo text empty-handed, save/reload and confirm text sync/persistence still work.

## Priority 2 - connected pipe restoration

Test Matter Pipe, Heavy Energy Cable and Network Pipe.

- [ ] A pipe by itself is a small central 6x6 section, not a full cube.
- [ ] Adjacent same-type pipes grow an arm toward each other.
- [ ] A pipe beside its supported machine/block entity grows an arm toward the machine.
- [ ] North/south/east/west/up/down arms render in the correct direction.
- [ ] Breaking the neighbour removes the arm.
- [ ] Collision/selection outline follows centre + active arms rather than a full cube.
- [ ] Matter Pipe still transfers matter across chains.
- [ ] Heavy Energy Cable still transfers FE across long chains and multiple outputs.
- [ ] Network Pipe still participates in Pattern Storage/Monitor/Router/Switch networks.
- [ ] Opening a Heavy Energy Cable GUI still works.

## Priority 3 - corrected original block artwork

Place each in world and compare front/back/top/side orientation:

- [ ] Fusion Reactor Coil = Base Stripes.
- [ ] Fusion Reactor Controller = Holo Monitor front, Decomposer Top sides, Base Stripes top/bottom/back.
- [ ] Fusion Reactor IO = Decomposer Top artwork.
- [ ] Gravitational Stabilizer = Holo Monitor front, Network Port back, Vent2 sides, Base Coil caps. Beam/front direction must still agree with gameplay.
- [ ] Decorative Vent Bright uses the original bright `vent2` texture and Vent Dark uses `vent`.
- [ ] Holo Matrix uses Weapon Station Top artwork.
- [ ] Striped Tritanium Plate has ordinary plate top/bottom and yellow-striped sides.
- [ ] Decorative Clean uses Transporter Side artwork.
- [ ] Pylon is a tall multi-part source-proportioned structure rather than a cube; confirm its extended rendering does not cause missing neighbouring faces.

## Priority 4 - item-state indicators

- [ ] Empty Pattern Drive uses the base original icon.
- [ ] Partially occupied Pattern Drive uses `pattern_drive_partially_full`.
- [ ] Full Pattern Drive uses `pattern_drive_full`.
- [ ] Repeat with Creative Pattern Drive across partial/full capacity.
- [ ] Unlinked Matter Scanner uses `matter_scanner_offline`.
- [ ] Link Matter Scanner to powered Pattern Storage; icon changes to the normal/online scanner image.
- [ ] Matter Container still changes empty/partial/full models with stored matter.
- [ ] Battery/HC Battery FE bars, weapon FE/overheat bars and Portable Decomposer indicator/tooltips still update.

## Priority 5 - GUI visual regression

- [ ] Open Decomposer, Recycler, Analyzer, Replicator, Pattern Storage, Pattern Monitor, Microwave, Space-Time Accelerator, Solar Panel, Charging Station, Weapon Station, Android Station, Reactor, Stabilizer, Router, Transporter and crates.
- [ ] Machine slots now use the original Matter Overdrive small-slot artwork and line up with the actual clickable slots.
- [ ] No text, FE/matter bars, debug panels or buttons overlap the slot artwork.
- [ ] All inventories still accept/return exactly the same items as before the GUI styling pass.

## Priority 6 - security and save regression

- [ ] Empty Security Protocol binds and cycles Claim -> Access -> Remove -> Claim.
- [ ] Claim, matching Access and matching Remove work after save/reload.
- [ ] Non-owner access/break is denied without a matching Access protocol.
- [ ] Existing directional machines retain inventories, FE, matter, upgrades, contracts and drives after loading an old test world.

## Core gameplay regression

- [ ] Matter production/storage/replication still works end-to-end.
- [ ] Reactor ring validation, Reactor IO, anomaly mass, stabilizers, shared ring power, RUN/SCRAM, remote and overlay still work.
- [ ] Weapons still require valid FE, heat/reload correctly and do not drain unrelated guns.
- [ ] Android HUD, V cycle, B activate, K tree and perk persistence/refunds still work.

## Known renderer-specific follow-up

- Exact legacy Pylon OBJ/CTM animated overlay is not claimed yet; this build restores the source proportions/silhouette without reintroducing the obsolete 1.12 CTM state system.
- Some original emissive/animated/connected-texture layers still require dedicated 1.20.1 renderer code.
- Full gun module meshes, recoil/zoom and every original hand animation remain deeper weapon-renderer parity work.
- Original per-machine GUI background textures exist, but modern menus do not all share the old slot coordinates; this build restores safe shared legacy slot artwork first rather than misaligning functional slots.

## Pass criteria

Pass this build when no purple/black missing models appear, the two adjacent-face bugs are gone, Holo text stays attached to its panel, all three pipe types connect visually without breaking transport, corrected source textures face the intended directions, item state icons update, GUI slots align, and the core gameplay regression remains functional.
