# Matter Overdrive Tech Overhaul Test Plan

This file tracks focused runtime checks for the active technology/gameplay expansion work.

## Field Operations progression pass

The Data Pad now exposes repeatable research-stage operations that connect existing generated technology sites and anomaly gameplay to three doctrines: Recovery, Systems, and Anomaly.

### Assignment and persistence

- Run `/matteroverdrive research field status` before assigning anything; confirm it reports no active operation.
- Run `/matteroverdrive research field assign recovery`, `systems`, and `anomaly` at several research stages.
- Confirm only operations valid for the player's current `ResearchProgression.Stage` are selected.
- Confirm requesting a doctrine with no eligible operation falls back only when appropriate and never grants a future-stage objective.
- Save/reload the world and reconnect; confirm active operation, progress, and completion count persist.
- Confirm assigning another doctrine replaces the current operation cleanly without duplicating a reward.

### Recovery doctrine

- Assign Recovery and visit matching `abandoned_matter_lab` / `android_relay_outpost` compact sites.
- Confirm repeat visits to valid site instances advance the operation but the one-time discovery dossier/XP path still only rewards first discovery of that exact site position.
- Complete a Recovery operation and confirm one recovered Android artifact/protocol is awarded.
- Complete several Recovery operations and verify protocol selection varies deterministically rather than producing an invalid `NONE` artifact.

### Systems doctrine

- Assign Systems and visit matching `field_logistics_depot` / `matter_observatory` sites.
- Confirm the operation advances from the same bounded local site recognizer used by the research discovery system.
- Complete several operations and confirm rewards rotate between Speed and periodic Parallel Processing upgrades.

### Anomaly doctrine

- At Fusion Research, assign Anomaly and visit matching `anomaly_research_site` structures.
- At Anomaly Engineering, verify `Horizon Exposure` can be selected.
- Enter a live event horizon with Horizon Exposure active and confirm it completes without requiring a contract item.
- Verify normal anomaly contracts still progress in parallel when held.
- Complete several Anomaly operations and confirm Dilithium rewards plus periodic Failsafe upgrades.

### Data Pad / diagnostics

- Open the Data Pad and confirm the journal has a `Field Operations` section.
- Verify title, doctrine, progress/goal, description, and completed-operation count match `/matteroverdrive research field status`.
- Confirm `/matteroverdrive research status` includes the active field operation alongside campaign/site state.
- Test with two players and verify operation state/rewards remain per-player.

### Performance and abuse checks

- Confirm no global mission tick manager was introduced; operation progress is event-driven from existing discovery/anomaly events.
- Walk across chunk boundaries near a compact site and verify discovery recognition remains bounded to the existing local 25x13x25 scan cadence.
- Confirm one site recognition cannot award the one-time discovery dossier repeatedly.
- Confirm completion XP scales to a bounded maximum and operation completion count cannot overflow normal progression state.

## Hybrid conduit visual/junction pass

- Confirm Hybrid Conduit is visibly purple in inventory and in-world.
- Confirm Hybrid Conduit connects flush to another Hybrid Conduit.
- Confirm Hybrid Conduit connects flush to Heavy Energy Cable.
- Confirm Hybrid Conduit connects flush to Matter Pipe / other `VisualPipeBlock` variants.
- Check straight, corner, T-junction, cross, vertical and mixed-pipe arrangements for visible seams.
- Break and replace neighbouring pipes and confirm both sides refresh their connection arms.
- Verify FE routing still works through a Hybrid Conduit / Heavy Energy Cable junction.
- Verify Matter routing still works through a Hybrid Conduit / Matter Pipe junction.

## Vex Mythoclast audio pass

- Primary fire must call the dedicated `vex_mythoclast_fire` SoundEvent rather than the generic Matter Overdrive phaser event.
- Temporal Linear fire must call the dedicated `vex_mythoclast_linear_fire` SoundEvent rather than the generic sniper event.
- Verify the two modes remain independently resource-pack-overridable through their dedicated IDs.

## Follow-up network-system ideas

AE2-style lessons worth adapting without copying its implementation: explicit per-side resource configuration, routing priorities, diagnostic visibility, network segmentation and clean junction/facade presentation. The existing Hybrid Conduit is the natural bridge point for FE + Matter network diagnostics and filtering in a later pass.
