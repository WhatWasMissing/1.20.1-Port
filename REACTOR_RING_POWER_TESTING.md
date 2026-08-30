# Reactor Ring Power Testing

This checklist covers the internal reactor power bus and the external-cable boundary on `feature/fusion-reactor`.

## Pull and launch

From the repository folder in Command Prompt:

```bat
git switch feature/fusion-reactor
git pull origin feature/fusion-reactor
RUN_M2_CLIENT.bat
```

Use a copy of the test world before event-horizon tests. Block destruction remains disabled, but entities and item drops inside the horizon can be damaged or consumed.

## Intended behavior

- The reactor must have a valid ring and stored/generated FE.
- FE-capable machines inside the ring, at the same Y level as the controller, share available reactor FE without a fixed 512 FE/t ceiling.
- A Decomposer may also occupy either flexible slot immediately left or right of the controller and receive internal power.
- Machines outside that footprint require at least one Heavy Energy Cable between Reactor IO and the machine.
- Reactor IO matter input/output still uses Matter Pipe or Heavy Matter Pipe as before.
- The controller GUI shows `[DEBUG] Ring power: <machines> machines | sent <FE/t>`.
- The existing `usage: <FE/t>` value remains actual machine processing consumption, not charging demand.

## Useful commands

```mcfunction
/give @p matteroverdrive:reactor_assembly_guide
/give @p matteroverdrive:fusion_reactor_controller
/give @p matteroverdrive:fusion_reactor_coil 16
/give @p matteroverdrive:machine_hull 64
/give @p matteroverdrive:fusion_reactor_io 4
/give @p matteroverdrive:gravitational_anomaly
/give @p matteroverdrive:decomposer 8
/give @p matteroverdrive:heavy_matter_pipe 64
/give @p matteroverdrive:creative_battery
/give @p matteroverdrive:debug_matter_block 64
```

`matteroverdrive:heavy_matter_pipe` is the registered item ID currently displayed as **Heavy Energy Cable**.

For isolated power tests, open the reactor controller GUI and toggle **INF FE**. Turn it off again when checking real reactor generation and storage drain.

## Internal footprint

Coordinates are local to the controller: **forward** points into the ring and **lateral** is left/right. Rotating the controller rotates this footprint.

| Local forward row | Powered lateral positions |
|---:|:---|
| 1 and 9 | -2 through +2 |
| 2 and 8 | -3 through +3 |
| 3 through 7 | -4 through +4 |
| 0 | Only the two flexible ring slots at -1 and +1 |

The controller, Reactor IO, anomaly, and Heavy Energy Cable are not counted as internal receiver machines.

## Test 1 — Internal machine, no cable

1. Build and validate the reactor.
2. Put a Decomposer within the internal footprint on the controller's Y level.
3. Remove every energy cable touching that Decomposer.
4. Enable **INF FE**, or run the reactor with stored FE.
5. Open the Decomposer and controller GUIs.

Pass conditions:

- The Decomposer FE increases.
- The controller reports one additional ring machine.
- `sent` is greater than 0 while the Decomposer has room and can exceed 512 FE/t.
- `sent` returns to 0 when its energy buffer is full.

## Test 2 — Flexible controller-side Decomposer

1. Put a Decomposer in one flexible ring slot immediately beside the controller.
2. Keep it cable-free and let structure validation refresh (up to two seconds).
3. Confirm the ring remains **VALID**.

Pass conditions:

- The Decomposer appears in the ring-machine count and receives FE.
- Replacing it with an allowed hull, coil, or IO keeps the structure valid but removes that receiver from the count.

## Test 3 — External machine must use cable

1. Place a Decomposer outside the footprint directly beside Reactor IO.
2. Empty or partly drain its FE buffer and leave no cable between it and IO.
3. Wait several seconds.
4. Add one Heavy Energy Cable so the route is `Reactor IO -> Heavy Energy Cable -> Decomposer`.

Pass conditions:

- Direct IO adjacency does not charge the outside Decomposer.
- Charging begins after the cable is added.
- Removing the cable stops external transfer without breaking the reactor's internal bus.

## Test 4 — Boundary and height

1. Place one Decomposer at an included edge coordinate, such as local forward 3/lateral 4.
2. Place another one block farther outward at forward 3/lateral 5.
3. Test a third machine directly above a valid inside coordinate.

Pass conditions:

- Only the same-level machine at lateral 4 receives cable-free FE.
- The outside and above machines require Heavy Energy Cable.
- The controller's ring-machine count matches the eligible machines.

## Test 5 — Multiple machines and fairness

1. Place at least three partly empty Decomposers inside the footprint.
2. First test with **INF FE** on.
3. Then turn it off and reduce available reactor FE below the receivers' combined acceptance.

Pass conditions:

- With enough FE, total ring transfer can exceed 512 FE/t and is limited by reactor storage and receiver acceptance.
- With limited FE, the available amount is shared and service order rotates rather than permanently favoring one machine.
- No machine is counted twice if a cable also touches it.

## Test 6 — Actual usage telemetry

1. Let an internal Decomposer charge while idle.
2. Confirm controller `usage` remains 0 FE/t while `sent` may be nonzero.
3. Start a decomposition operation.
4. Add a second actively processing internal or cabled machine.

Pass conditions:

- Charging does not inflate `usage`.
- Processing increases `usage`.
- Two active machines combine in the displayed usage total.

## Test 7 — Rebuild and persistence

1. Save and reload the world with internal and external machines connected.
2. Break and replace an internal machine.
3. Break and replace a non-first external cable.
4. Break and replace the first cable beside Reactor IO.

Pass conditions:

- The internal bus rediscovers receivers within two seconds.
- The external network recovers after cable replacement without replacing Reactor IO.
- Reactor matter, energy, structure, stabilizer state, and persistent overlay remain correct.

## Test 8 — Anomaly-mass output scaling

1. Turn **INF FE** off.
2. Open the controller and note **Output capacity** and **generated**.
3. Ensure the controller has storage room or connect an empty internal machine/external battery so generated FE has somewhere to go.
4. Feed one or more Debug Matter Blocks into the anomaly and wait for the mass value to update.
5. Compare the two power values before and after the mass increase.

Pass conditions:

- **Output capacity** increases with unsuppressed anomaly mass and is not pinned to 512 FE/t.
- **Generated** can rise above 512 FE/t when the controller has storage room.
- **Generated** may be below capacity when the controller is nearly full; this is demand/storage throttling, not a reactor-output cap.
- Internal `sent` and cable-routed transfer can exceed 512 FE/t when receivers can accept it.
- Stabilizers continue to affect safe mass and hazard radii without reducing the unsuppressed-mass output calculation.

## Matter regression check

Connect a powered Decomposer to Reactor IO through a Matter Pipe, drain the reactor below 2048 kM, and confirm it refills. An active reactor may display 2047/2048 because it consumes matter in the same tick; pause generation or use a non-running reactor for an exact full reading.

## Result report

```text
Commit tested:
Minecraft/Forge:
Existing or new world:

Test 1 internal no-cable:
Test 2 flexible-slot Decomposer:
Test 3 external cable boundary:
Test 4 footprint/height boundary:
Test 5 multi-machine fairness:
Test 6 actual usage:
Test 7 reload/rebuild:
Test 8 anomaly-mass scaling:
Matter regression:

Observed output capacity FE/t:
Observed generated FE/t:
Observed ring machine count:
Observed ring FE/t sent:
Observed processing usage FE/t:
Notes/screenshots:
```
