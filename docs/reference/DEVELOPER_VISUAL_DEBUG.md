# Developer Visual Diagnostics

Matter Overdrive 0.6 includes an opt-in, client-only visual diagnostics layer for GUI and model development. It is intended to shorten the edit/build/screenshot loop when validating screen geometry and item/weapon transforms.

## Safety and scope

- Diagnostics default to **off**.
- The feature is client-only and does not change server state, progression, packets, matter, FE, combat, or saved gameplay data.
- The GUI overlay currently inspects ordinary visible Minecraft `Button` children on the active screen. This makes it useful across Matter Overdrive screens without each screen needing its own debug renderer.
- Custom-drawn regions that are not real widgets are not automatically measurable yet; they should be exposed through a future opt-in region provider if they need geometry diagnostics.

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

## F9 — model transform diagnostics foundation

Press **F9** to toggle the model diagnostics state.

The shared API is `matteroverdrive.client.debug.DeveloperVisualDebug.recordModelTransform(...)`. A renderer can publish the transform it is actively applying using:

- item/model identifier;
- render context;
- translation X/Y/Z;
- rotation X/Y/Z;
- scale X/Y/Z.

`DeveloperVisualDebug.lastModelTransform()` exposes the latest client-local snapshot to future debug UI/render hooks.

At the current 0.6 stage this is deliberately a **telemetry foundation**, not a claim that every weapon transform is already captured. Current main does not expose a clear custom `ItemInHandRenderer`/`IClientItemExtensions` path for the Matter Overdrive weapons, so renderer-specific producers should be wired only after the active model/display-transform path has been confirmed. This avoids invasive or speculative hand-render interception.

## Planned extensions

Useful follow-on work once the 0.6 build is verified:

- semantic IDs for opt-in custom screen regions (Aspect slot, Fragment slot, Artifact slot, etc.);
- custom-drawn rectangle registration, not only `Button` widgets;
- overlap area in pixels rather than only pair detection;
- safe-area and panel-edge diagnostics;
- live model transform HUD once active renderer producers are known;
- optional developer adjustment controls for translate/rotate/scale with copyable values;
- named transform contexts such as first-person right/left, third-person right/left, GUI, and ground.

## Screenshot workflow

When reporting a visual issue, a screenshot with F8 enabled is considerably more useful than a normal screenshot alone because the image contains the clickable bounds and exact layout coordinates. Where possible, capture both the normal view and the F8 diagnostic view at the same GUI scale.
