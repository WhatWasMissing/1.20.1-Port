# Developer Visual Diagnostics — 0.6 Test Checklist

Test against the current `main` 0.6 build. These checks validate the client-only development overlay and must not be interpreted as gameplay feature parity checks.

## GUI overlay — F8

- [ ] Launch normally and confirm no diagnostic rectangles or panel are visible by default.
- [ ] Open the Android Class Matrix and press F8; confirm the action-bar message reports GUI diagnostics ON.
- [ ] Confirm visible buttons receive measured outlines.
- [ ] Confirm each measured button shows a top-left origin marker and centre marker.
- [ ] Hover a class/subclass/aspect/fragment control and confirm the debug panel reports mouse coordinates plus the button `x`, `y`, width, height, and centre.
- [ ] Confirm any truly intersecting clickable rectangles are red and increase the overlap-pair count.
- [ ] Confirm non-overlapping regions do not report false intersections when edges merely touch.
- [ ] Switch Android Class Matrix tabs and confirm rebuilt controls are measured without closing/reopening the screen.
- [ ] Test the Android Class Matrix at GUI scales 2, 3, and 4 where the display supports them.
- [ ] Resize the game window or change fullscreen/windowed mode and confirm diagnostics follow the new widget locations.
- [ ] Open at least one machine GUI and confirm its ordinary buttons are measurable too.
- [ ] Press F8 again; confirm the action-bar message reports OFF and all debug rendering disappears.

## Live model diagnostics — F9

- [ ] Hold the Phaser and press F9; confirm the action-bar message reports model diagnostics ON.
- [ ] Confirm the top-left model panel identifies `matteroverdrive:phaser`.
- [ ] In first person, confirm the panel reports the appropriate `firstperson_righthand` or `firstperson_lefthand` context.
- [ ] Confirm rotation, position/translation, and scale vectors are shown.
- [ ] Change to third-person view and confirm the context changes to the matching `thirdperson_*` entry.
- [ ] Repeat with Phaser Rifle, Ion Sniper and Plasma Shotgun.
- [ ] Confirm the Rifle/Sniper/Shotgun currently report their shared authored hand-transform values; use screenshots to judge whether each mesh actually needs unique values.
- [ ] Test an item whose model has no direct display transform and confirm the HUD reports that condition rather than showing fabricated values.
- [ ] Press F9 again and confirm the model panel disappears and the action-bar message reports OFF.
- [ ] Confirm toggling F9 has no gameplay, weapon-energy, animation, inventory, or server-state side effects.

## Regression / safety

- [ ] F8/F9 remain client-only and do not disconnect from a dedicated server.
- [ ] Android loadout selections still send/apply correctly with diagnostics enabled.
- [ ] Normal screen hover/click/focus behavior is unchanged.
- [ ] Escape/CLOSE controls still work.
- [ ] Save and reload the world; no debug-only state is written to world/player progression data.
- [ ] Run once with diagnostics never enabled and confirm normal 0.6 presentation is unchanged.

## Useful bug-report evidence

For a GUI layout bug, capture:

1. a normal screenshot;
2. an F8 screenshot at the same window size and GUI scale;
3. the hovered debug geometry for the affected control;
4. the current overlap-pair count;
5. GUI scale and fullscreen/windowed state.

For a weapon/model placement bug, capture:

1. the weapon in the problematic view with F9 enabled;
2. the item ID and display context visible in the panel;
3. the reported rotation, translation, and scale vectors;
4. whether the player is right- or left-handed and whether the item is in main/off hand;
5. FOV and first/third-person state if relevant.

This turns visual issues into reproducible coordinate/transform fixes rather than estimates from the rendered image alone.
