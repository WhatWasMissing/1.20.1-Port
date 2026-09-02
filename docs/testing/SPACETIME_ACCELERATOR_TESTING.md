# Space-Time Accelerator Runtime Testing

Branch: `feature/handheld-matter-tools`

Use this checklist before treating the restored Space-Time Accelerator as integrated.

## Intended base behaviour

- 512,000 FE internal storage.
- 1,024 kM internal matter storage.
- 64 FE/t while active.
- 0.2 kM matter per acceleration pulse.
- One pulse every 40 ticks.
- Base horizontal radius of two blocks on the Accelerator's Y level.
- A redstone signal disables operation.
- Supported upgrades: Speed, Hyper Speed, Power, Power Storage, Matter Storage and Range.

## Build and interface

- [ ] Craft, place and open the Accelerator.
- [ ] Confirm the screen reports FE, matter, FE/t, pulse interval, radius, pulse progress, active/redstone state and the last accelerated-target count.
- [ ] Confirm the FE and matter debug buttons work without affecting other machines.
- [ ] Confirm the existing block/item model remains correctly oriented.

## Power, matter and pulse behaviour

- [ ] Supply FE without matter and confirm the Accelerator remains inactive.
- [ ] Supply matter without FE and confirm it remains inactive.
- [ ] Supply both and confirm it activates.
- [ ] At base settings, confirm it consumes approximately 64 FE each tick and one whole kM after five 0.2-kM pulses.
- [ ] Confirm the base pulse occurs every 40 ticks and no matter is silently lost through save/reload rounding.
- [ ] Apply a redstone signal and confirm FE/matter consumption and acceleration stop immediately.
- [ ] Remove the signal and confirm operation resumes.

## Random-tick targets

- [ ] Place crops/saplings or another randomly ticking block on the same Y level inside the radius.
- [ ] Confirm pulse activity advances the target faster than an equivalent control target outside the radius.
- [ ] Confirm a target that changes block state during its random tick does not crash or tick using a stale state.

## Block-entity targets

- [ ] Test a Furnace and at least two Matter Overdrive machines inside the radius.
- [ ] Confirm their normal server ticker receives extra ticks and processing visibly accelerates.
- [ ] Confirm the Accelerator does not tick itself or another Space-Time Accelerator.
- [ ] Confirm a removed/replaced target is not ticked afterward.
- [ ] Run several mixed machines together and check for duplication, skipped output, crashes or excessive server lag.

## Radius and upgrades

- [ ] At base radius, place equivalent targets at +X, -X, +Z and -Z boundary positions.
- [ ] Confirm all four directions are included symmetrically.
- [ ] Install Speed and Hyper Speed upgrades; confirm pulse intervals shorten without bypassing FE or matter checks.
- [ ] Install Power upgrades; confirm displayed/actual FE usage changes coherently.
- [ ] Install Power Storage and Matter Storage upgrades; confirm capacities increase and clamp safely when removed.
- [ ] Install Range upgrades; confirm radius expands but never exceeds the hard 12-block cap.
- [ ] Confirm increased range does not load unloaded chunks.

## Persistence and break safety

- [ ] Save/reload with FE, matter, fractional matter remainder, pulse progress and upgrades present.
- [ ] Confirm all values remain coherent and operation resumes.
- [ ] Break the Accelerator and confirm installed upgrades drop exactly once.

## Pass criteria

The slice passes when power/matter gating, redstone control, symmetric range, random/block-entity acceleration, upgrades, persistence and break safety all work without duplication, crashes or unacceptable tick-time impact.