# Drone Parity Pass Handoff

Branch: `testing/main`
Base head: `420996a660219976eead6831d32e7a9e65ec4409`
Final head: the commit containing this handoff after the Drone pass.
Authoritative references: Matter Overdrive 1.7.10 0.4.2 and 1.12.2 0.7.1.0.

## What changed

- Confirmed the authoritative 1.7.10 JAR contains no Drone implementation; Drone identity comes from 1.12.2.
- Recovered legacy Drone 0.5x0.5 dimensions, 12-health baseline, creator-follow thresholds, custom 3D move helper, dedicated mechanical model, texture and movement-particle behavior.
- Confirmed there is no authoritative Drone equipment/inventory subsystem; none was invented.
- Preserved the current port's richer server-authoritative FOLLOW/DEFENSIVE/PASSIVE/AGGRESSIVE modes, owner/allied protection, ranged combat, flight smoothing and persistence as intentional modern additions.
- Replaced the humanoid/player placeholder Drone renderer with a dedicated 1.20 reconstruction of the recovered chassis, four flaps, twin exhausts and front eye.
- Added legacy-style movement-dependent particle trail.
- Restored 0.5x0.5 entity hitbox and one-time migration to 12 max health / zero base armor for existing Drones.
- Added `docs/reference/DRONE_PARITY.md` and `docs/testing/DRONE_PARITY_TESTING.md`.

## Runtime / visual verification still required

- Model UV alignment against `drone_default.png`.
- Chassis/flap/exhaust orientation in third person and at rest/in flight.
- Particle trail density at slow/fast speeds.
- Smaller hitbox collision behavior in caves/interiors.
- Existing owner modes and ranged combat after health/hitbox migration.

## Next pass

Re-fetch exact current `testing/main` before editing. Start the machine configuration / GUI parity pass. Audit the generic legacy machine configuration/redstone concepts first and only add controls where a real current-port backend can support them. Prioritize remaining machine-specific operator screens and redstone/config persistence, while keeping physical slots continuously visible. Do not invent decorative controls for absent behavior. After that pass, create another handoff before moving to the dedicated Dimensional Pylon / visual-parity cleanup work.
