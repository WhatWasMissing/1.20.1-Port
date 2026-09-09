# Weapon Renderer 2.0

Branch target: `main` / Matter Overdrive 0.6

## Goal

Restore an owned, reliable first-person rendering path for the Matter Overdrive energy weapons instead of depending on the normal held-item transform stack.

## Architecture

Weapon Renderer 2.0 follows the same broad architecture used by modern gun mods: the weapon owns its first-person view-model render and keeps presentation state separate from server-authoritative weapon gameplay. The implementation uses Forge 1.20.1 public client events rather than copying third-party mixins or renderer code.

- `WeaponClientEffects` owns aim, recoil, charge and camera state.
- `WeaponRenderProfile` owns per-weapon presentation tuning only.
- `WeaponItemRenderer` renders the weapon + supported optic modules.
- The first-person `RenderHandEvent` is cancelled for Matter Overdrive energy weapons and replaced by the dedicated view-model path.
- The normal baked-model paths remain available for GUI, dropped, fixed and third-person contexts.

## Renderer 2.0 behavior

- Always uses a dedicated main-hand first-person weapon render.
- Suppresses the off hand while a Matter Overdrive energy weapon owns the first-person view.
- Reconstructs the recovered legacy hip transform independently of vanilla bow/use transforms.
- Smoothly interpolates hip -> ADS.
- Uses shot timestamps for an impulse recoil animation rather than held-item wobble.
- Adds subtle movement sway and equip/swing motion, heavily reduced while aiming.
- Adds charge presentation for Ion Sniper and Plasma Shotgun.
- Keeps the existing recovered Ion Sniper FOV values and camera recoil path.
- Renders Holo Sights / Sniper Scope through the weapon renderer instead of relying on them appearing as unrelated held items.

## External implementation references

The design was compared with the public Forge 1.20.1 TaCZ rendering path. TaCZ owns first-person gun rendering, cancels the ordinary hand event for guns, keeps aim/recoil/animation state separately, and suppresses inappropriate off-hand rendering. Matter Overdrive implements the same architectural ideas with its own renderer and existing OBJ assets; no TaCZ source or assets are copied into the mod.

## Runtime test

1. Hold each of Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun in first person.
2. Confirm the weapon is visible immediately after equip and does not use the vanilla bow pose.
3. Fire each weapon and verify the model recoil impulse plus camera recoil.
4. Hold/release Ion Sniper and Plasma Shotgun and verify charge motion is smooth.
5. Aim with Ion Sniper and verify the model centers while FOV transitions smoothly.
6. Install Holo Sights / Sniper Scope and check the optic remains attached while hip-firing, aiming and recoiling.
7. Move, sprint, stop, switch weapons and rapidly re-equip; verify no model disappears or becomes stuck.
8. Put an item in the off hand. While a Matter Overdrive gun is active, verify it does not overlap the weapon view model.
9. Switch to a non-Matter-Overdrive item and verify vanilla first-person rendering returns immediately.
10. Check third person, GUI, dropped item and Weapon Station rendering for regressions.

## Not claimed yet

Runtime tuning still depends on real screenshots/gameplay. Exact hand meshes, animated module bones, muzzle flash geometry and fully skeletal reload choreography remain follow-up work after the base first-person path is proven visible and stable.
