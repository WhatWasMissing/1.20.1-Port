# Texture and Model Parity Progress

Branch: `testing/main`
Legacy reference: MatterOverdrive 1.12.2 `0.7.1.0` jar and recovered source/resources.
Build identity: `Alpha Version 3`, made by MVQ1303.

## Runtime-confirmed visual pass

The latest player test confirmed the restored Android Station, Weapon Station, Star Map, Contract Market, Matter Analyzer, Decomposer, Recycler, Microwave, Pattern Monitor, Pattern Storage, Replicator, Charging Station, Space-Time Accelerator and Solar Panel models are working. The only reported defects were neighbouring faces disappearing beside Pattern Monitor and Space-Time Accelerator.

The Holo Sign now renders as a thin monitor and renamed-item programming works. Its text was readable but camera-billboarded in the latest screenshots.

## Second source-parity pass implemented

- Pattern Monitor and Space-Time Accelerator are now non-occluding so their non-full geometry does not cull adjacent block faces.
- Holo Sign text is now transformed from the sign's FACING state and anchored directly in front of its monitor plane instead of facing the camera.
- Matter Pipe, Heavy Energy Cable (`heavy_matter_pipe`) and Network Pipe now use the original 6x6 centre plus six directional-arm concept with modern multipart blockstates and matching collision outlines.
- Matter Pipe and Network Pipe received visual connection state blocks without changing their existing routing IDs; Heavy Energy Cable keeps its existing block entity/network logic.
- Fusion Reactor Coil now uses the original Base Stripes artwork.
- Reactor Controller restores Base Stripes top/bottom/back, Decomposer Top sides and Holo Monitor front.
- Reactor IO restores Decomposer Top on all faces.
- Gravitational Stabilizer restores Base Coil caps, Vent2 sides, Holo Monitor front and Network Port back, and its blockstate rotations now match the original source-facing convention.
- Bright/Dark vent assignments were corrected to original `vent2`/`vent` respectively.
- Holo Matrix restores Weapon Station Top artwork.
- Striped Tritanium Plate restores ordinary Tritanium Plate top/bottom and yellow-striped sides.
- `decorative.clean` restores Transporter Side artwork.
- Pylon is no longer a one-block cube placeholder: a source-proportioned tall centre, rings, base and four arms reproduce the original overall multi-part silhouette. The exact old CTM/animated OBJ overlay remains a renderer-specific follow-up.
- Shared machine GUIs now use the original 18x18 `slot_small.png` artwork for machine slots while retaining modern 1.20.1 menus and telemetry.
- Pattern Drives now use the original empty / partially-full / full inventory textures according to stored-pattern capacity.
- Matter Scanner now uses the original offline icon until linked and switches to the original online scanner icon when linked.
- Existing Matter Container empty/partial/full model predicates, battery FE bars, weapon FE/overheat bars and Portable Decomposer state display remain in place.

## Asset audit result

Corresponding original block, item and GUI PNGs present in the port matched the recovered 1.12.2 assets byte-for-byte during the audit. The dominant visual problems were model geometry, UV/face assignment, blockstate rotation, occlusion and unused state artwork rather than damaged PNGs.

## Still renderer-specific / not claimed complete

- Exact Pylon legacy multi-block OBJ plus animated/CTM overlay state.
- Remaining legacy emissive, animated overlay and connected-texture effects that require dedicated modern render code.
- Full weapon module meshes, recoil, zoom and every original first/third-person renderer animation.
- Exact per-machine legacy GUI background recreation where current 1.20.1 menu layout no longer matches the old container coordinates. Original shared slot artwork is restored now without breaking modern slot placement.

Visual parity is only marked complete after an in-game test; a successful Actions build only verifies compilation/packaging.
