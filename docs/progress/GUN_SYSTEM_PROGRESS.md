# Gun System Progress

## Current status — 2026-09-09

Branch: `testing/tech-overhaul`
Release line: `0.6`

The current implementation supersedes older Creative-mode and early weapon-station notes:

- Energy weapons require valid internal FE in **Survival and Creative**. Only the explicit **Creative Battery** module provides infinite energy.
- Supported reload sources are Energy Packs and `WeaponBatteryItem` batteries. Other energy weapons are deliberately excluded, preventing cross-weapon drain.
- Heat/overheat, reload state, module storage and server-authoritative firing remain implemented.
- First- and third-person transforms for Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun remain runtime visual checks.

## Current energy-state hardening

Weapon energy is stored on the weapon stack while the battery module defines the weapon's effective capacity. This creates a capacity-shrink edge case: without a persisted clamp, removing a large battery can hide excess FE above the reduced capacity and allow it to reappear when a larger battery is reinstalled.

The Weapon Station now persists the energy clamp **after the final module configuration is packed back into the weapon**. The clamp is deliberately not performed while the station temporarily unpacks modules for editing, because simply opening the GUI must not destroy charge. Normal pickup, shift-click, GUI close and station-break packing all converge on that final pack path.

The static gate `scripts/validate_weapon_consistency.py` verifies:

- firing has a pre-shot FE gate;
- a successful shot drains weapon FE;
- Energy Packs remain an explicit reload source;
- battery transfer accepts `WeaponBatteryItem` and rejects the weapon stack itself;
- the final Weapon Station pack persists a capacity-shrink clamp;
- Weapon Station take/close paths still pack modules back into the weapon.

It is included in `VERIFY_M2_BUILD.bat`.

## Weapon Station module implementation

- One dedicated weapon slot plus six typed module slots: battery, colour, barrel, sights, utility, utility.
- `WeaponSystem.isValidModuleForSlot` rejects incompatible modules and unsupported weapon/module combinations.
- Opening the station unpacks installed modules into editable slots without committing a capacity shrink.
- Taking the weapon or closing the GUI packs the selected modules back into the weapon and clears temporary station slots.
- Removing a module while editing uninstalls it when the weapon is packed again.
- Shift-clicking a weapon packs modules before it is moved into the player inventory.
- Station inventory persists through block-entity NBT.

## Runtime test pass

1. Test all four Matter Overdrive energy weapons in Survival **and Creative** at zero FE; none should fire without a Creative Battery module.
2. Reload from a charged normal/HC battery and confirm only that battery loses FE.
3. Put another charged weapon in the inventory and confirm it is never used as a reload source.
4. Verify Energy Packs still reload as the intended consumable source.
5. Install a high-capacity battery module and charge the weapon above its fallback capacity.
6. Open the Weapon Station without changing the battery, then close it: merely opening/editing must not discard that charge.
7. Reopen, actually remove or replace the battery with a lower-capacity configuration, and take/close the weapon: FE must clamp to the final capacity.
8. Reinstall the larger battery and confirm discarded excess FE does **not** reappear.
9. Save/reload and repeat via shift-click to ensure every final packing path behaves the same.
10. Verify heat, overheat, Ion Sniper aim/FOV, module effects and first/third-person transforms.

## Static validation

```text
python scripts/validate_weapon_consistency.py
```

## Next work

- Runtime-test the completed energy/module invariants.
- Keep weapon rendering/hand transforms as a dedicated visual pass if screenshots still show misalignment.
- Add/adjust recipes only from verified legacy ingredients rather than guessed parity.
