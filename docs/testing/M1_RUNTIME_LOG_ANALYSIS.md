# M1 runtime log analysis — verification R2

The first real Forge 1.20.1 client launch proved that the Java/Forge and registry foundation works:

- Java 17 was active.
- Minecraft 1.20.1 / Forge 47.4.10 launched.
- Matter Overdrive reached common setup.
- Runtime inventory marker reported 75 blocks, 72 block items, 98 standalone items and 57 sound events.

The same launch exposed the remaining M1 resource blocker: legacy 1.12 texture locations used plural roots (`matteroverdrive:items/...` and `matteroverdrive:blocks/...`). Minecraft 1.20.1's item/block atlases use the modern singular roots (`item/...` and `block/...`), so the game emitted 244 Matter Overdrive missing-texture model warnings.

Verification R2 fixes that by:

- moving the active texture trees to `textures/item` and `textures/block`;
- rewriting active model JSON texture locations;
- rewriting OBJ `.mtl` texture locations;
- updating the industrial-glass texture metadata;
- extending `verifyM1Resources` to reject old plural model texture paths and verify referenced model textures exist;
- adding `CHECK_M1_RUNTIME.bat`, which rejects any `Missing textures in model matteroverdrive:` warning after a client run.

The untouched 1.12 asset tree remains under `legacy_reference` for later visual reconstruction.

## R2 retest order

1. Run `VERIFY_M1_BUILD.bat` and require `[PASS] M1 BUILD GATE PASSED`.
2. Run `RUN_M1_CLIENT.bat`.
3. Reach the title screen and confirm Matter Overdrive is in Mods.
4. Create the disposable Creative test world and perform the representative placement test from `M1_VERIFICATION_PATH.md`.
5. Save, quit and reload that world.
6. Quit Minecraft normally. `RUN_M1_CLIENT.bat` will automatically invoke `CHECK_M1_RUNTIME.bat`; require `[PASS] M1 RUNTIME LOG GATE PASSED`.
7. Run the dedicated-server gate from `M1_VERIFICATION_PATH.md`.

Do not begin Milestone 2 until the build, runtime-resource, placement, reload and dedicated-server gates all pass.
