# Drone Parity Pass

Authoritative references: Matter Overdrive 1.12.2 0.7.1.0 for Drone identity. The authoritative 1.7.10 0.4.2 JAR contains no Drone implementation.

## Source-backed findings

- Legacy Drone entity size is 0.5 x 0.5 blocks.
- Legacy maximum health is 12.
- Legacy Drone has no source-backed armor value, inventory or equipment subsystem.
- Legacy AI follows its creator with 5-block start / 3-block stop thresholds and a 0.2 movement-speed request.
- Legacy movement is true 3D velocity steering through a custom move helper.
- Legacy client presentation uses `drone_default.png`, a dedicated compact mechanical model and a movement-dependent `CRIT_MAGIC` trail every other tick.
- The recovered model consists of a 6x5x6 main chassis, four hinged front flaps, two rear exhausts and a 2x2 front eye.

## 1.20 translation

The existing 1.20 server-authoritative flight, ownership persistence and FOLLOW/DEFENSIVE/PASSIVE/AGGRESSIVE modes are preserved as deliberate modern additions. They are not falsely described as legacy features.

This pass restores the source-backed physical identity around that backend:

- Entity hitbox changed from the placeholder 0.8 x 0.8 to 0.5 x 0.5.
- Existing/saved Drones migrate to a 12-health baseline and zero base armor once on the server.
- The humanoid/player-model placeholder renderer is replaced by a purpose-built 1.20 `DroneModel` reconstructed from the authoritative 1.12 box dimensions and rotations.
- Four legacy flap pieces animate with flight speed and twin exhaust pieces pitch with chassis/flight movement.
- The original texture remains `textures/entities/drone_default.png`.
- Client movement trail restores the legacy every-other-tick / speed*30 / max-6 behavior using the closest current enchanted-hit particle equivalent.

## Intentional modern behavior retained

- Linked Drone command modes.
- Owner/allied protection.
- Server-authoritative combat targeting.
- Ranged combat and standoff/strafing behavior.
- Gravity immunity, no fall damage and non-pushable behavior.
- Smoother acceleration/damping rather than reintroducing the old single-step move-helper velocity assignment.

No Drone equipment GUI/inventory was added because neither authoritative implementation supplies one.
