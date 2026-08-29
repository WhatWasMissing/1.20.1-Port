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
