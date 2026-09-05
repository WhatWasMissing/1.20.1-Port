# Weapon Energy Balance Smoke Test

Branch: `testing/main`
Build identity: Alpha Version 3

This pass increases weapon endurance without changing damage, heat, recoil, cooldown, range, module effects, or FE-per-shot.

## Expected values
- Standard weapon buffer with no installed battery: **128,000 FE** (previously 32,000 FE).
- Energy Pack refill: **128,000 FE** (previously 32,000 FE).
- Installed Weapon Battery and HC Battery capacities remain unchanged and continue to override the standard buffer.
- Creative Battery remains effectively infinite.

## Runtime checks
- [ ] Fresh Phaser, Phaser Rifle, Ion Sniper and Plasma Shotgun show a 128,000 FE maximum with no battery installed.
- [ ] Existing weapons with stored FE retain their stored amount and expose the larger maximum after loading the new build.
- [ ] One Energy Pack can refill up to 128,000 FE and is consumed once.
- [ ] Weapon Battery and HC Battery still provide their larger configured capacities when installed at the Weapon Station.
- [ ] Weapon Station HOME/STATS agrees with the weapon tooltip/capability capacity.
- [ ] FE-per-shot remains unchanged for every weapon and module combination.
- [ ] Heat, overheat, reload, recoil and scope behavior are unchanged.
- [ ] Weapon modules retain the corrected tooltips from the preceding pass.
