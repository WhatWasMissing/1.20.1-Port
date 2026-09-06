# Drone Parity Testing

Authoritative reference: Matter Overdrive 1.12.2 0.7.1.0.

## Backend / migration
- [ ] Fresh Drone spawns with a 0.5 x 0.5 hitbox.
- [ ] Fresh Drone reports 12 max health.
- [ ] Drone has no base armor from the legacy identity layer.
- [ ] Existing saved Drones created before this pass migrate to 12 max health without duplicating/resetting owner UUID or command mode.
- [ ] FOLLOW still starts outside roughly 5 blocks and settles inside roughly 3 blocks.
- [ ] FOLLOW/DEFENSIVE/PASSIVE/AGGRESSIVE modes still persist through save/reload.
- [ ] Linked Drone never attacks its owner or allied players.
- [ ] Unowned Drone behavior remains unchanged from the current port.
- [ ] Gravity immunity, no fall damage and non-pushable behavior remain intact.

## Renderer / animation — visually verify later
- [ ] Drone no longer renders as a humanoid/player body.
- [ ] Main chassis is compact and approximately matches the recovered 6x5x6 legacy body.
- [ ] Front eye faces the travel/look direction.
- [ ] Upper/lower/left/right flaps are visible and animate more strongly as movement speed increases.
- [ ] Twin rear exhaust blocks remain attached and pitch naturally during flight.
- [ ] Existing `drone_default.png` UVs do not show obvious face scrambling or missing regions.
- [ ] Shadow scale is close to the old 0.5 renderer value.
- [ ] Fast-moving Drone emits an enchanted critical-style trail every other tick, with particle count increasing with speed and capped at six.
- [ ] Stationary/near-stationary Drone does not spam particles.

## Combat regression
- [ ] DEFENSIVE responds to a valid attacker of the owner.
- [ ] PASSIVE clears targets and does not fight.
- [ ] AGGRESSIVE acquires hostile mobs and maintains flight standoff.
- [ ] Existing ranged shot behavior still works after hitbox/model change.
- [ ] Smaller hitbox does not cause persistent collision clipping into ceilings/walls during follow.

## Multiplayer
- [ ] One player cannot steal another player's linked Drone.
- [ ] Owner release and relink remain server-authoritative.
- [ ] Renderer/particles are visible to observing clients without changing server movement state.
