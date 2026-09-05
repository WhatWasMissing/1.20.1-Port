# Drone Flight Testing

This pass modernizes Drone movement while preserving the Matter Overdrive ownership/command model.

## What changed
- Drones now remain gravity-free and hover rather than relying on ground RandomStroll navigation.
- Owned Drones steer in full 3D toward the owner while preserving the existing ~5 block follow-start and ~3 block settle distances.
- Follow height targets above the operator rather than forcing ground-level pathing.
- Combat movement keeps a ranged standoff, adjusts altitude toward the target, and adds light lateral strafing.
- Minecraft's normal entity collision/movement physics still resolve walls and solid blocks; this is not noclip flight.
- Existing no-fall-damage, no-climb and non-pushable legacy traits remain.

## Runtime checks
1. Claim a Drone and walk over uneven terrain, stairs, ravines and small cliffs. It should follow through 3D space instead of repeatedly searching for a ground path.
2. Climb a hill/tower. The Drone should gain altitude and settle above/near the operator rather than remaining at the base.
3. Walk back toward the Drone. It should stop actively following at roughly the existing 3 block settle distance and should not oscillate violently.
4. Put solid walls between operator and Drone. It must not noclip through blocks. Note any cases where it becomes permanently trapped.
5. Test FOLLOW and PASSIVE. The Drone must clear/avoid combat targets as before.
6. Test DEFENSIVE. After the owner is attacked, the Drone should acquire the attacker and hover at ranged standoff while firing.
7. Test AGGRESSIVE. The Drone should acquire valid hostile mobs, keep approximate ranged distance, adjust vertically and strafe rather than behaving like a ground mob.
8. Confirm the owner and allies cannot be targeted or damaged by Drone targeting logic.
9. Release a Drone. It should remain gravity-free and settle into a stable hover rather than dropping like a normal Monster.
10. Save/reload the world with claimed Drones in each command mode. Ownership, mode, hover behavior and no-gravity state must persist.
11. Confirm generated unowned Drones remain hostile and can still engage players while airborne.
12. Confirm no fall damage, no ladder climbing and normal entity pushing remain disabled.

## Modernization policy
The original Drone identity is preserved (flying robotic ranged unit, ownership and command behavior), but 1.20.1 movement is intentionally not a byte-for-byte recreation of legacy navigation. The port uses smoother server-authoritative 3D steering and modern collision handling instead of reproducing old pathing limitations.
