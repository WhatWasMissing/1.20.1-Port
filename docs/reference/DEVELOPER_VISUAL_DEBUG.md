# Developer Visual Diagnostics

Matter Overdrive 0.6 includes an opt-in, client-only visual diagnostics layer for GUI and model development. It is intended to shorten the edit/build/screenshot loop when validating screen geometry and item/weapon transforms.

## Safety and scope

- Diagnostics default to **off**.
- The feature is client-only and does not change server state, progression, packets, matter, FE, combat, or saved gameplay data.
- The GUI overlay currently inspects ordinary visible Minecraft `Button` children on the active screen. This makes it useful across Matter Overdrive screens without each screen needing its own debug renderer.
- Custom-drawn regions that are not real widgets are not automatically measurable yet; they should be exposed through a future opt-in region provider if they need geometry diagnostics.
- Model diagnostics read resource-pack JSON and report values only; they do not alter Minecraft's renderer or model resources.

## F8 — GUI diagnostics

Press **F8** to toggle the GUI diagnostics overlay.

While enabled, the overlay draws:

- a rectangle around every visible button on the active screen;
- a marker at each button's top-left origin;
- a marker at each button's centre/anchor;
- a highlighted outline for the region currently under the mouse;
- red outlines for buttons whose clickable rectangles intersect;
- a live count of visible regions and overlapping pairs;
- current mouse coordinates;
- exact hovered-region `x`, `y`, width, height, and centre coordinates.

### Colour meaning

- Blue: ordinary measured region.
- Yellow: hovered measured region.
- Red: region intersects at least one other measured region.
- Pale centre marker: computed centre/anchor.

The overlay is intentionally based on clickable rectangles rather than only painted textures. This makes it useful for finding invisible hitbox collisions as well as obvious visual overlap.

## Recommended GUI validation pass

For an affected screen such as the Android Class Matrix:

1. Open the screen with F8 off and capture the normal appearance if needed.
2. Enable F8.
3. Move the pointer across suspect controls and record their geometry from the debug panel.
4. Look for red regions and a non-zero overlap count.
5. Repeat at GUI scales 2, 3, and 4 where practical.
6. Resize the game window or switch fullscreen/windowed and re-check responsive layouts.
7. Change tabs/pages so dynamically rebuilt controls are measured too.
8. Disable F8 and verify no debug rendering remains.

## F9 — live model transform diagnostics

Press **F9** while in-world to toggle the held-item model diagnostics HUD.

The inspector identifies the held item, determines the current first-person or third-person left/right-hand display context, and reads that context directly from the item's active resource-pack model JSON. The HUD displays:

- item registry ID;
- active display context (`firstperson_righthand`, `firstperson_lefthand`, `thirdperson_righthand`, or `thirdperson_lefthand`);
- rotation X/Y/Z;
- translation X/Y/Z;
- scale X/Y/Z.

If the model has no direct display entry for that context, the HUD reports that instead of inventing values. The resource lookup is cached while diagnostics are enabled; enabling F9 clears the diagnostic cache so a fresh session reads the current resource stack.

This approach matches the current Matter Overdrive weapon assets: Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun use Forge OBJ item models with JSON `display` transforms. It therefore gives us the values that are actually authored for their first/third-person presentation without injecting into Minecraft's hand renderer.

The shared API `matteroverdrive.client.debug.DeveloperVisualDebug.recordModelTransform(...)` remains available for future custom renderers that need to publish a transform which is not represented by a direct JSON display block. `DeveloperVisualDebug.lastModelTransform()` exposes the most recent client-local snapshot.

### Why this matters for the current weapons

The current Phaser Rifle, Ion Sniper and Plasma Shotgun model JSONs use the same first-person and third-person transform values despite having different meshes. F9 makes those copied values visible in-game so screenshots can be tied directly to the authored transform that produced them. Weapon-specific placement can then be tuned deliberately rather than by guessing from screenshots alone.

## Recommended model validation pass

1. Hold the weapon/item to inspect.
2. Enable F9.
3. Capture a screenshot that includes the model and transform HUD.
4. Check both first-person and third-person views.
5. Where relevant, check right- and left-handed/off-hand presentation.
6. Compare different MO weapons; note when distinct meshes share identical values but produce visibly different placement.
7. Disable F9 and confirm the normal HUD returns unchanged.

## Planned extensions

Useful follow-on work once the 0.6 build is verified:

- semantic IDs for opt-in custom screen regions (Aspect slot, Fragment slot, Artifact slot, etc.);
- custom-drawn rectangle registration, not only `Button` widgets;
- overlap area in pixels rather than only pair detection;
- safe-area and panel-edge diagnostics;
- optional developer adjustment controls for translate/rotate/scale with copyable values;
- model-context selector for GUI/ground/fixed views in addition to the live held context;
- custom-renderer producers where an asset does not expose a direct JSON display transform.

## Screenshot workflow

For a GUI issue, capture both the normal view and the F8 diagnostic view at the same GUI scale. For a held-model issue, capture the normal model with F9 enabled so the screenshot records the item ID, context and transform values alongside the visual problem. This turns visual feedback into reproducible coordinate/transform work rather than approximate trial and error.
