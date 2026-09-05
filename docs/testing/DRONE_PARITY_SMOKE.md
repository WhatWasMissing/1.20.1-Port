# Drone Parity Smoke Test

Branch: `testing/main`
Legacy references: Matter Overdrive 1.7.10 `0.4.2` and 1.12.2 `0.7.1.0`.

This small parity pass restores several low-risk physical Drone traits before the larger flying-navigation rewrite.

## Implemented

- Drone ignores fall damage.
- Drone does not use ladder/climbable movement.
- Drone is not pushable by normal entity collision.
- Existing owner follow distances remain unchanged: follow starts beyond 5 blocks and settles near 3 blocks.
- Existing FOLLOW / DEFENSIVE / PASSIVE / AGGRESSIVE command modes remain unchanged.
- Full legacy flying navigation, hover movement and renderer/equipment parity are still future work.

## Runtime checks

- [ ] Drop an owned and unowned Drone from a large height; neither takes fall damage.
- [ ] Place a Drone against ladders/vines/scaffolding-like climbable paths; it should not treat them as climbing routes.
- [ ] Walk into and crowd the Drone with players/mobs; it should not be physically shoved around by normal collision pushing.
- [ ] Confirm linked Drone still begins following when more than roughly 5 blocks from owner and settles near roughly 3 blocks.
- [ ] Confirm all four command modes still cycle and persist after save/reload.
- [ ] Confirm unowned Drone hostility and owned Drone ally protections remain unchanged.

Build success only proves compilation; these behaviors still need runtime verification.
