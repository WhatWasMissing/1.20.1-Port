---
navigation:
  title: Dimensional Pylon
  parent: index.md
  position: 11
  icon: matteroverdrive:pylon
---
# Dimensional Pylon

The restored **Dimensional Pylon** has two behaviors for compatibility: the authentic formed multiblock, and the older single-block wireless-network fallback retained for existing saves.

## Formed multiblock

The authentic structure is a **2 x 3 x 2 solid arrangement of 12 Pylon blocks**. Once the assembly is recognised, the blocks act as one shared Dimensional Pylon machine rather than twelve unrelated devices.

If the structure does not form, verify all twelve positions first. A hollow frame or a 2 x 2 x 3 shape with missing internal positions is not equivalent to the required 12-block assembly.

## Shared storage

A formed assembly currently provides:

- up to **2,048 Matter** storage;
- up to **1,000,000 FE** storage;
- up to **2,048 FE/t** output.

Because the structure is shared, do not interpret each visible Pylon block as having a separate one-million-FE bank.

## Dimensional-rift generation

The formed Pylon uses Matter as part of its dimensional-field generation behavior. Matter consumption therefore accompanies useful generation; an empty Matter store can stop the process even when the assembly itself is correctly formed.

When diagnosing it, check both FE and Matter rather than only the energy bar.

## Legacy single-block behavior

For compatibility with worlds made during earlier stages of the 1.20.1 port, an **unformed single Pylon** keeps the older channel-based wireless item-network relay behavior.

This means one Pylon block may appear to work even though the authentic multiblock is not formed. That is intentional migration behavior, not proof that the twelve-block structure is unnecessary.

Forming the full 2 x 3 x 2 assembly switches the system to the restored Dimensional Pylon behavior.

## Practical setup

1. Build the complete 2 x 3 x 2 Pylon assembly.
2. Confirm the formed/shared state.
3. Supply Matter to the shared storage.
4. Connect compatible FE consumers/storage to the output path.
5. Watch both Matter consumption and FE generation/output.

## Troubleshooting

**It will not form:** count all twelve blocks and check the exact rectangular dimensions.

**It forms but does not generate:** check Matter first, then check whether FE storage/output has room.

**One block behaves like an old wireless relay:** the full multiblock probably is not formed; that fallback is retained for save compatibility.

**Output is lower than expected:** distinguish generated energy from transferred energy. A destination with no free FE capacity can make useful output appear lower even when the Pylon is operating.

The Pylon is separate from the Fusion Reactor. If you are looking for anomaly mass, stabilizers or Reactor IO, see [Fusion Reactor](reactor.md).
