# Matter Overdrive Tech Overhaul Test Plan

This file tracks focused runtime checks for `testing/tech-overhaul`.

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

- Primary fire must call the dedicated `destiny_vex_mythoclast_fire` SoundEvent rather than the generic Matter Overdrive phaser event.
- Temporal Linear fire must call the dedicated `destiny_vex_mythoclast_linear_fire` SoundEvent rather than the generic sniper event.
- Verify the two modes remain independently resource-pack-overridable through their dedicated IDs.

## Follow-up network-system ideas

AE2-style lessons worth adapting without copying its implementation: explicit per-side resource configuration, routing priorities, diagnostic visibility, network segmentation and clean junction/facade presentation. The existing Hybrid Conduit is the natural bridge point for FE + Matter network diagnostics and filtering in a later pass.
