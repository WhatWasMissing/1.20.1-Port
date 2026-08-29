# Reactor Progress

## Current work

- The Fusion Reactor Assembly Guide provides four build/operation pages with resized diagrams and an in-game overlay legend.
- The world overlay uses green for a correctly filled position, red for a wrong block, blue for required hull, orange for required coil/IO, purple for the flexible controller-side position, and cyan for the anomaly centre/range.
- The overlay can be enabled or disabled by interacting with the reactor controller while holding the Assembly Guide. The controller stores the setting in block-entity data, so it remains active after reopening the GUI or reloading the world.
- The anomaly search reports the nearest accepted anomaly and its vertical range; the overlay shows the accepted anomaly positions around the controller.
- Structure validation and reactor operation remain server-side; the overlay is visual guidance only.

## Usage

1. Hold the Fusion Reactor Assembly Guide and look directly at the reactor controller.
2. Right-click the controller with the guide to toggle the persistent overlay. The action bar reports ON/OFF.
3. Look at the controller again without the guide in hand; the saved overlay remains visible.
4. Use the guide’s page diagrams and the color legend when placing blocks.
