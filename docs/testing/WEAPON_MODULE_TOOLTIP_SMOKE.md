# Weapon Module Tooltip Smoke Test

Small Alpha Version 3 QoL pass for `testing/main`.

The module tooltips now describe the same multipliers used by the live `WeaponSystem` backend instead of vague summaries.

## Verify
- Damage Barrel shows +50% damage and 50% energy cost.
- Fire Barrel shows ignition, -25% damage and 50% energy cost.
- Explosion Barrel shows 20% energy cost and 15% fire-cycle multiplier.
- Doomsday Barrel shows 20% energy cost and 10% fire-cycle multiplier.
- Heal and Block barrels show their 50% energy-cost multiplier.
- Holo Sights show 60% aimed / 80% hip-fire spread multipliers.
- Sniper Scope shows +50% range, 40% aimed spread and the intentional 180% hip-fire spread penalty.
- Ricochet notes that the rebound deals reduced damage.
- Weapon Station STATS should agree with the equipped module effects.

No weapon firing, FE, heat, reload or module-slot logic was changed in this pass.
