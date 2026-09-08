---
navigation:
  title: Fusion Reactor
  parent: index.md
  position: 5
  icon: matteroverdrive:fusion_reactor_controller
---
# Fusion Reactor

The Fusion Reactor is a multiblock FE generator built around an existing Gravitational Anomaly. It is not a single-block generator: the controller validates the ring, finds the anomaly, links Reactor IO blocks and powers supported internal-ring receivers.

## What the reactor stores

The controller currently has a very large internal FE buffer and its own matter reserve. Its important operating state is persistent: FE, matter, upgrades, RUN/SCRAM state, redstone mode, overlay state and the internal fair-share output sequence all survive save/reload.

The reactor only generates while the structure is valid, operation is allowed, matter is available and the FE buffer has room.

## Structure validation

The controller checks the expected ring positions periodically. A valid ring needs the expected Machine Hull / Fusion Reactor Coil / permitted controller-side blocks and an anomaly at the ring centre line. Reactor IO blocks may replace supported ring positions and are linked back to the controller when validation succeeds.

The GUI fault/status text is useful when building. Typical failures include:

- incorrect hull position;
- incorrect coil position;
- incorrect controller-side position;
- no anomaly at ring centre;
- unloaded structure area;
- anomaly data unavailable.

Use the **Reactor Assembly Guide**/overlay when positioning the ring rather than guessing the geometry.

## Anomaly distance and efficiency

The controller searches around the expected anomaly centre. Base operation supports a close anomaly; Range upgrades extend the accepted search distance. Efficiency falls as the anomaly sits farther from the ideal centre relative to the effective search range.

## Generation and anomaly mass

Generation is mass-scaled. The controller calculates potential output from:

- the reactor base output;
- structure/anomaly-distance efficiency;
- the anomaly's unsuppressed real mass;
- the anomaly mass multiplier;
- the current generation-rate multiplier from supported upgrades.

The GUI separates **potential FE/t** from **generated FE/t**. Potential is what the reactor could produce under the current mass/efficiency; generated is what was actually accepted into the controller buffer that tick.

A larger anomaly can therefore support substantially larger reactor output. If generated FE/t is lower than potential, first check whether the controller buffer is nearly full or whether downstream demand is low.

## Matter consumption

The reactor consumes matter proportionally to its active generation. Fractional drain is accumulated internally so small per-tick values are not lost. If the matter reserve reaches zero the reactor reports **No matter** and generation stops.

Reactor IO can participate in matter input/output through the Matter capability, allowing the controller's matter reserve to be connected to the normal matter network.

## RUN / SCRAM and redstone

The reactor can be enabled or disabled directly with its RUN/SCRAM controls. Redstone mode can additionally require a signal, require no signal, or ignore redstone. These states are saved with the controller.

If a structurally valid reactor refuses to run, check in this order: RUN/SCRAM state, redstone permission, matter reserve, FE-buffer headroom, then anomaly availability.

## Reactor IO and external FE

A Reactor IO exposes the linked controller's FE/matter capabilities and also performs source-side discovery of external FE receivers. It can feed a machine directly beside the IO or search through connected Heavy Energy Pipes.

External receivers are collected and sorted, then the IO rotates the starting receiver over time. Available FE is divided with a fair-offer calculation so one machine should not permanently monopolise a branched network.

## Heavy Energy Pipes

The existing Heavy Matter Pipe block identity also acts as the Heavy Energy Pipe relay for FE. Each relay has an 8,192 FE buffer and a 1,024 FE/t transfer limit for its local handoff.

For branched networks, each cable searches for reachable receiver branches and rotates between the reachable first hops instead of always selecting the first breadth-first-search result. The route sequence is serialized, so receiver preference does not reset every time the chunk/world reloads.

If a cable chain stops after breaking/replacing a section, verify that every replacement cable has loaded and that the destination still exposes FE on the connected side. The 0.6 release test specifically includes replacing the first and middle cable in multi-cable runs.

## Internal ring power

Supported machines positioned on the reactor's internal ring power bus can draw directly from shared reactor FE without an external cable. Internal receivers are also fair-shared and the internal power sequence persists.

This is intentionally separate from arbitrary external machines: outside the supported ring positions, use Reactor IO plus Heavy Energy Pipes or a direct IO connection.

## Stabilizers

Powered Gravitational Stabilizers reduce anomaly effects when correctly aimed and unobstructed. Their GUI/diagnostics distinguish conditions such as no lock, blocked beam, insufficient power and redstone-paused operation. Stabilizer upgrades and redstone mode are persistent.

A reactor can power supported stabilizers through the internal ring system. If suppression appears wrong, inspect both the stabilizer beam state and the reactor's internal power telemetry rather than only the stabilizer FE bar.

## Event horizon feeding

Existing anomalies can gain mass from consumed items and living entities.

- Item consumption uses the same recipe-aware matter value as the 0.6 matter economy and records the feed once before the item is discarded.
- Living-entity mass is recorded only after event-horizon damage actually kills the entity, preventing repeated mass awards from a victim that survives multiple ticks.

The **Space-Time Equalizer** protects the wearer from the anomaly pull/event-horizon effects supported by the current runtime.

## Telemetry to watch

The reactor screen exposes enough state to diagnose most failures: structure validity, anomaly distance, potential FE/t, generated FE/t, matter drain/consumption, connected FE demand, internal ring output, anomaly mass, suppression, affected/horizon entity counts, recent feed information and related anomaly diagnostics.

For a release-quality test, compare the displayed generated FE/t and demand against real connected machines, then save and fully restart Minecraft to confirm RUN/SCRAM, redstone, upgrades, FE/matter state and fair-share routing persist.
