# Reactor Progress

## Current work

- The Fusion Reactor Assembly Guide provides four build/operation pages with resized diagrams and an in-game overlay legend.
- When enabled, the client renders the complete reactor outline continuously for loaded reactors within 128 blocks; it no longer requires the crosshair to remain on the controller.
- The world overlay uses green for a correctly filled position, red for a wrong block, blue for required hull, orange for required coil/IO, purple for the flexible controller-side position, and cyan for the anomaly centre/range.
- The overlay can be enabled or disabled by interacting with the reactor controller while holding the Assembly Guide. The controller stores the setting in block-entity data and synchronizes it to clients.
- Structure validation and reactor operation remain server-side; the overlay is visual guidance only.

## Usage

1. Hold the Fusion Reactor Assembly Guide and look directly at the reactor controller.
2. Right-click the controller with the guide to toggle the persistent overlay. The action bar reports ON/OFF.
3. Move, look away, or switch items; the complete outline remains visible while the controller is loaded and nearby.
4. Use the color legend: blue hull, orange coil/IO, purple flexible side, cyan anomaly, green correct, red incorrect.


## Latest implementation

- Reactor component models are now visually distinct: coils use `base_coil`, the controller uses `screen`, and reactor IO uses `network_port`.
- The controller GUI now reports the number of active stabilizers suppressing the linked anomaly.
- The reactor menu was extended and its inventory slots moved down to keep the new stabilizer status line readable.
- Next runtime test: power one and then four stabilizers with redstone, confirm the active-stabilizer count and reduced safe mass, while unsuppressed mass and energy output remain unchanged.
- Build and in-game verification are still pending for this commit.


## Stabilizer placement rework

- Stabilizer placement now prefers an aligned gravitational anomaly within 63 blocks and otherwise uses the player’s horizontal facing, preventing accidental downward-facing floor placement.
- The stabilizer model now has a distinct blue emitter face and contrasting sides, making the beam direction visible in-world.
- Placement reports the front direction and reminds the player that the beam travels forward and requires redstone power.
- Blocked and unsuccessful scans now include the facing direction in their status message.
- Next runtime test: place a stabilizer on the floor near the reactor, confirm its emitter face points at the anomaly, power it, and verify the controller’s active-stabilizer count increases.
- Build and in-game verification are pending for this commit.


## Stabilizer targeting correction

- Stabilizer beam obstruction now uses the target block’s collision shape, so solid blocks that do not advertise vanilla occlusion cannot be scanned through.
- Sneak-placement bypasses automatic anomaly targeting and uses the player’s horizontal facing, allowing deliberate placement away from an anomaly for negative tests.
- Sneak-right-click rotates an existing stabilizer through the four horizontal directions and reports the new beam direction.
- Blocked-beam messages now report the blocking distance and direction.
- Next test: rotate a powered stabilizer away from the anomaly and confirm it stops suppressing; place a solid block in its beam and confirm the blocked distance is reported.


## Reactor matter input recovery

- Reactor IO now actively pulls matter from connected matter sources through Matter Pipe and Heavy Matter Pipe routes, rather than relying only on decomposer push timing.
- Input and output route cursors are separate and persisted, preventing a rebuilt route from leaving the controller short by one matter unit.
- The pull path ignores other reactor IO blocks so multiple IOs cannot feed matter back into one another.
- Runtime test passed: a powered Decomposer connected through a matter pipe to the reactor IO refills the reactor after draining it to 2047 kM, including after breaking and replacing a pipe, without replacing the IO.
- For an exact 2048 kM reading, pause generation or test while the reactor is not consuming matter on the same tick.


## Next reactor milestone

- Begin gameplay parity for the gravitational anomaly: expose controlled range/effect diagnostics first, then add entity effects while keeping block destruction disabled until it has a separate safety test.

## Gravitational anomaly entity effects

- The anomaly now applies a capped inward force to nearby living entities, including players and mobs.
- The effect range uses suppressed mass, so powered stabilizers reduce both anomaly danger and the active pull range.
- The controller GUI now reports a dedicated `[DEBUG] Pull radius` line with the current effective radius and affected living-entity count.
- Block destruction is still disabled; this milestone is limited to observable, reversible entity movement.
- Next test: place a player or mob at several distances from the anomaly, confirm inward movement while unsuppressed, then power stabilizers and confirm the pull range and force decrease.
- Build and in-game verification are pending for this commit.


## Space-Time Equalizer

- The existing Space-Time Equalizer is now a real chest-slot wearable rather than a generic item.
- Anomaly gravity ignores living entities wearing it, including players and mobs.
- The item uses the existing Space-Time Equalizer armor texture and tooltip.
- Next test: spawn or position a player/mob inside the pull radius, compare movement without the Equalizer, then equip it in the chest slot and confirm the entity no longer accelerates toward the anomaly.
- Build and in-game verification are pending for this commit.


## Anomaly block hazard diagnostics

- The reactor GUI now exposes the calculated block-effect radius separately from the living-entity pull radius.
- Block hazard is explicitly shown as DISABLED; this diagnostic milestone makes no world-changing block edits.
- Next test: compare pull radius and block radius with zero and four powered stabilizers, then confirm both values respond to suppression while the disabled state remains unchanged.
