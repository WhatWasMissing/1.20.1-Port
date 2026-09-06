# Machine Redstone Configuration Testing

## Decomposer / Microwave
- [ ] DISABLED behaves exactly like pre-pass saves.
- [ ] LOW runs with no signal and pauses with signal.
- [ ] HIGH pauses with no signal and runs with signal.
- [ ] Progress is preserved while redstone-paused.
- [ ] FE input/charging remains functional while paused.

## Matter Analyzer
- [ ] Existing CONFIG page still cycles LOW/HIGH/NONE.
- [ ] Analysis pauses/resumes without losing valid progress.

## Charging Station
- [ ] CONFIG page cycles LOW/HIGH/DISABLED.
- [ ] Station can continue receiving/storing FE while redstone-paused.
- [ ] Item charging is stopped while mode disallows work.
- [ ] Android wireless charging is stopped while mode disallows work.
- [ ] Item/Android transfer telemetry becomes zero while paused.
- [ ] Redstone mode persists through save/reload.

## Space-Time Accelerator
- [ ] Old save without RedstoneMode behaves as LOW.
- [ ] LOW runs without signal and pauses with signal.
- [ ] HIGH runs with signal and pauses without signal.
- [ ] DISABLED ignores signal.
- [ ] Pulse timer is preserved while redstone-paused.
- [ ] FE/matter storage and upgrades remain intact after mode changes/reload.
- [ ] CONFIG page correctly reports current mode.

## Regression
- [ ] Matter Recycler remains unchanged from the prior verified implementation.
- [ ] Replicator network queue behavior is unchanged in this stage.
- [ ] Inscriber recipe constants and persistence are unchanged in this stage.
