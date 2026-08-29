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
