# Processing Machine Redstone Parity Testing

Target: Decomposer, Matter Recycler, Microwave.

## Persistence / compatibility
- [ ] Existing machines with no `RedstoneMode` NBT load as `DISABLED` and behave as before.
- [ ] `CYCLE RS` rotates DISABLED -> LOW -> HIGH -> DISABLED.
- [ ] Selected mode survives GUI close/reopen, chunk unload/reload and world restart.
- [ ] Menu text updates to the synchronized server-selected mode.

## LOW
- [ ] With no neighboring redstone signal, valid work proceeds normally.
- [ ] Applying a signal pauses an active work cycle without consuming FE or advancing progress.
- [ ] Removing the signal resumes preserved progress.

## HIGH
- [ ] With no signal, active work is paused.
- [ ] Applying a signal resumes/starts work.
- [ ] Removing signal pauses progress again.

## DISABLED
- [ ] Signal level has no effect on work behavior.

## Passive behavior while work is paused
- [ ] Energy items can still charge the machine.
- [ ] Decomposer can retain/expose/output already-stored matter.
- [ ] Existing output slots remain accessible.
- [ ] No input item is consumed while redstone blocks work.
- [ ] No active-work FE is consumed while redstone blocks work.

## GUI / visual checks
- [ ] HOME/TASKS/CONFIG/UPGRADES tabs fit without overlap.
- [ ] Physical machine and upgrade slots stay in the same positions on every page.
- [ ] CYCLE RS button does not overlap configuration text at normal/reduced GUI scales.
- [ ] LOW/HIGH/DISABLED descriptions remain readable.
