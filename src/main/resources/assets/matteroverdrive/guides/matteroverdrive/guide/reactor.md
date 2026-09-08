---
navigation:
  title: Fusion Reactor
  parent: index.md
  position: 5
  icon: matteroverdrive:fusion_reactor_controller
---
# Fusion Reactor

The Fusion Reactor is Matter Overdrive's late-game multiblock generator. It combines a validated ring structure, a Gravitational Anomaly, Matter storage, Reactor IO and stabilizers. The controller GUI should be treated as the authoritative source for structure and operating state.

## Required structure

The restored controller validates the reactor as a real multiblock rather than accepting a decorative ring. The structure requires:

- **1 Fusion Reactor Controller**;
- **4 Fusion Reactor Coils** in the expected ring positions;
- **Fusion Reactor IO** in a valid IO position;
- a **Gravitational Anomaly** at the ring centre, no farther than the controller's allowed anomaly distance.

If validation fails, use the controller's structure feedback or the Reactor Assembly Guide rather than randomly moving blocks. Typical diagnostics distinguish missing/incorrect coil positions from a missing or misplaced anomaly.

## Reactor Assembly Guide

The **Reactor Assembly Guide** is the dedicated build aid for the multiblock. Use it before powering the reactor if you are unsure about orientation or the required positions. The in-world overlay is intended to make exact block positions visible instead of relying on a text-only diagram.

## Matter and anomaly mass

The controller stores up to **2,048 Matter**. Matter is consumed while the reactor operates. The base drain model is restored from the legacy behavior and reactor upgrades/state can affect practical operation.

Anomaly mass is not decorative. Feeding valid matter-bearing entities/items into the event horizon increases the anomaly's real mass, and the reactor reads that unsuppressed mass when calculating its output potential. A larger anomaly can therefore support much greater generation than the base reactor state.

Because the event horizon is destructive, keep ordinary storage, players and loose items away from it unless you intend to feed them into the anomaly.

## Moving an anomaly

The **Anomaly Containment Unit** lets you relocate an existing Gravitational Anomaly instead of permanently building a base around wherever world generation happened to place one.

Use an **empty** containment unit directly on a Gravitational Anomaly. The device records the anomaly's accumulated mass, removes the anomaly from the world and marks itself as loaded. Its tooltip reports that it contains an anomaly and shows the preserved mass.

To redeploy it, use the loaded unit on the face of a solid block with an empty adjacent block. A new Gravitational Anomaly is placed in that adjacent position and receives the preserved mass. The same containment unit becomes empty again and can be reused.

Important rules:

- one containment unit holds one anomaly at a time;
- you cannot overwrite a solid block when redeploying;
- accumulated anomaly mass is preserved across the move;
- suppressor links are intentionally not transported, because they belong to stabilizers at the old location;
- move the anomaly **before** constructing the reactor ring when practical.

This means a naturally generated anomaly can be transported to a purpose-built reactor room, underground facility or existing base without losing the mass you have already fed into it.

## Energy storage and output

The reactor controller has **100,000,000 FE** of internal capacity. Output scales from a restored base generation model and anomaly mass rather than being permanently limited to the old cable-sized 512 FE/t behavior.

The controller telemetry distinguishes **output potential** from **generated last tick**. Potential is what the reactor could produce under the current state; generated output can be lower when there is nowhere for the energy to go.

## Internal ring power

Supported reactor-ring components share an internal power bus. This is deliberate QoL: stabilizers and other valid internal components should not each need an exposed cable snaking around the ring.

That internal bus ends at the reactor boundary. For machines outside the structure, use **Reactor IO -> Heavy Energy Cable -> external receiver**. The IO's external cable traversal is designed to follow chained cables without accidentally treating internal reactor components as ordinary external loads.

## Stabilizers

A **Gravitational Stabilizer** targets and suppresses an anomaly when it is powered, correctly oriented and within its supported line/range rules. Current restored constants include a maximum targeting distance of **63 blocks** and a base suppression factor of **0.7**.

A stabilizer that is present but unpowered is not equivalent to an active stabilizer. If suppression is not changing, check:

1. whether the stabilizer has FE;
2. whether it can target the intended anomaly;
3. whether its orientation/beam path is valid;
4. whether the reactor's internal power-sharing state is active when you expect the reactor to power it.

## RUN and SCRAM

Use the controller's **RUN/SCRAM** controls rather than breaking the multiblock to stop it. SCRAM is the safe operational stop and keeps the structure available for inspection/reconfiguration.

Before starting:

- verify **STRUCTURE VALID**;
- verify an anomaly is detected;
- verify Matter is available;
- verify external output has somewhere to go;
- verify stabilizers are powered if your build relies on suppression.

## Reactor IO troubleshooting

If the controller is generating but an external machine is not charging:

1. check that the Reactor IO itself belongs to the validated structure;
2. connect the first Heavy Energy Cable directly to the external side of the IO;
3. verify that first cable receives FE;
4. extend the cable chain one segment at a time;
5. check the destination machine's FE capability and free storage.

The 0.6 line contains explicit fixes for chained-cable discovery, break/replace behavior and the old external-transfer bottleneck. A reproducible case where only the first cable works should be reported rather than accepted as intended reactor behavior.

## Safety equipment

The **Space-Time Equalizer** wearable exists for work around gravitational anomalies. When worn in the intended slot it protects the operator from anomaly pull/event-horizon effects supported by the current implementation. It does not make nearby blocks, mobs or dropped items safe.

## What to watch in telemetry

During testing or tuning, the most useful readings are:

- structure validity;
- stored Matter;
- stored FE;
- anomaly distance and mass;
- output potential;
- actual generated FE for the last tick;
- connected/current FE demand;
- stabilizer state.

Those values make it possible to distinguish a generation problem from an output-network problem without dismantling the reactor.
