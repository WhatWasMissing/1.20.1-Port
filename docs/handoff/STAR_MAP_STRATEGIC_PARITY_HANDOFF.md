# Star Map Strategic Parity Handoff

Branch: `testing/main`
Base head: `3089237971da8f3e13a55f017b8345b5e2108dea`
Final head: current `testing/main` commit containing this handoff after Star Map pass.
Authoritative reference: Matter Overdrive 1.7.10 0.4.2.

## Pass summary

- Audited every authoritative Star Map ship/building class and confirmed the port already represents the complete strategic class set. No fake additional classes were added.
- Recovered GalaxyGenerator defaults: 3^3 = 27 quadrants, 2048-2304 stars, 1-4 configured planets (effectively 1-3 from the legacy random call), prefix chance 1.0 and suffix chance 0.8.
- Expanded deterministic catalog to 27 quadrants and 2176 stars while preserving all original q0-q3/s0-s5 indices and existing compatibility planet indices.
- Recovered seven weighted star classes and temperature/radius bands for appended stars.
- Removed new generation of the port-invented Oceanic planet class. New planets use source-backed Normal/Gas Giant/Dwarf weighting and recovered size bands.
- Preserved modern ordinal orbit for stable travel while adding a separate legacy 0-1 orbit property.
- Verified homeworld/capacity/building/ship constants against bytecode; retained the automatic Ship Factory only as an explicit 1.20 compatibility bridge.
- Clarified that current command-fleet random encounters are additive modern gameplay, not recovered TravelEvent subclasses.

## Runtime / visual verification required

- Galaxy view with 27 quadrants.
- Dense quadrant star navigation at GUI scales 2/3/Auto.
- Existing saved colonies after catalog expansion.
- Old fifth compatibility planets.
- Planet type/size distribution sampling.
- Ship dispatch and build-queue persistence after expansion.

## Next pass

Re-fetch exact current `testing/main` before edits. Start the Drone parity pass by decompiling the 1.12 `EntityDrone`, `DroneMoveHelper`, model and renderer. Preserve the current stable server-authoritative 3D movement, ownership and mode backend unless the legacy source identifies identity-defining behavior that can be added safely. Prioritize source-backed equipment/inventory, movement/attack distances, owner-management semantics and renderer/model identity; mark all render work visually unverified.
