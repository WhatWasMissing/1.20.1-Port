# Weapon energy-state hardening test plan

Use this after the Android/state consistency pass when validating `testing/tech-overhaul`.

## Zero-energy / reload isolation

- [ ] In Survival, all Matter Overdrive energy weapons fail to fire at zero internal FE.
- [ ] In Creative, ordinary batteries/empty weapons obey the same FE requirement.
- [ ] Installing a Creative Battery is the only intended infinite-energy path.
- [ ] A charged Battery or HC Battery reloads the weapon and loses exactly the transferred FE.
- [ ] A charged second gun in the inventory is never used as a reload battery.
- [ ] Energy Packs remain valid consumable reload sources.

## Battery-capacity mutation

- [ ] Install the highest-capacity supported battery module and charge above the no-battery fallback capacity.
- [ ] Open the Weapon Station, make no module changes, then close/take the weapon: temporary unpacking must **not** discard charge.
- [ ] Reopen and actually remove the battery: when the final configuration is packed, displayed/internal weapon FE clamps to the fallback maximum.
- [ ] Reinstall the larger battery: discarded excess FE does not reappear.
- [ ] Repeat by swapping directly from a larger battery module to a smaller one.
- [ ] Close/reopen the station and save/reload after the clamp; the reduced FE remains persisted.
- [ ] Creative Battery -> normal battery transitions expose only persisted normal weapon FE, not infinite/stale charge.

## Regression

- [ ] Weapon Station normal pickup and shift-click both retain installed modules.
- [ ] Heat and overheat still operate after battery swaps.
- [ ] Phaser mode switching, Ion Sniper aim/FOV and Plasma Shotgun charge/release still work.
- [ ] Weapon HUD updates capacity/FE after the final Station configuration is packed.

## Static gate

```text
python scripts/validate_weapon_consistency.py
```
