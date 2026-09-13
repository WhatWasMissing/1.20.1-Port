---
navigation:
  title: Transporter and Security
  parent: index.md
  position: 12
  icon: matteroverdrive:transporter
---
# Transporter and Security

The **Transporter** moves entities to saved destinations using stored Forge Energy and the restored Transport Flash Drive workflow. Machine security is a separate ownership/access layer shared by protected Matter Overdrive machines.

## Transporter basics

The restored Transporter currently uses the legacy operating constants as its baseline:

- **1,024,000 FE** internal capacity;
- **32 block** base transport range;
- **70 tick** transport cycle;
- **80 tick** transport delay/state timing.

Supported upgrades can change practical range, speed and power behavior. The GUI is the best place to check the machine's current effective state after upgrades.

The effective range boundary is inclusive: a target exactly at the displayed range is valid. A target beyond that range is not.

## Saving a destination

A **Transport Flash Drive** stores a transport destination. Treat it as the address book entry for the Transporter rather than as generic removable storage.

A normal setup flow is:

1. Create/use a Transport Flash Drive.
2. Record/bind the intended destination with the supported interaction.
3. Insert/select that drive in the Transporter.
4. Ensure the destination is within the machine's effective range.
5. Supply enough FE.
6. Stand/place the entity in the transport area and start the cycle.

If the machine is powered but refuses a destination, re-check the flash drive binding and range before rebuilding the power network.

## Destination safety

The Transporter does not force-load its destination. The target chunk must already be loaded before a transport cycle can complete.

At completion the machine searches for a supported, collision-free arrival position above and immediately around the bound target. It prefers the target column, then nearby positions, and can search several blocks upward for headroom. This avoids intentionally placing an entity inside a solid block simply because the saved coordinate is valid.

If no attempted entity can be placed safely, the cycle aborts without consuming the transport FE cost or starting the post-transport cooldown. If only some entities can be placed, telemetry reports only the entities that were actually transported and the successful cycle consumes its normal FE cost once.

## Range and upgrades

The 32-block value is the base restored range, not necessarily the final upgraded range. Range upgrades apply only where the Transporter supports them, while speed/power upgrades alter different parts of the transport cycle.

When comparing an upgraded machine against the guide, use the GUI's effective values rather than assuming every Transporter should still take exactly the base timing.

## Security Protocols

Security Protocol items have four useful states:

- **Empty** - unconfigured protocol item.
- **Claim** - establishes ownership on a supported machine.
- **Access** - grants/uses authorised access according to the machine's security state.
- **Remove** - clears the claim when the player is permitted to do so.

Security state belongs to the machine, not merely to the item that created it.

## Claiming a machine

Use the Claim protocol on a supported unclaimed machine. Once ownership is established, another player should not be able to bypass the intended access rules merely by opening the GUI or dismantling the block.

If you are building a shared base, establish ownership deliberately before installing a machine in an automated line so you know who is expected to administer it.

## Removing security

Use the **Remove** protocol only when you intend to clear the security state. The operation still respects permissions; carrying a Remove protocol is not meant to be a universal bypass for someone else's machine.

## Wrench behavior

The **Tritanium Wrench** safely rotates/disassembles supported machines, but dismantling respects security. If the wrench works on your own machine and refuses another claimed machine, that is expected protection rather than a wrench failure.

## Troubleshooting transport

**No transport begins:** verify FE, target drive, range and valid entity/transport area.

**Target is exactly at the displayed range:** it should be accepted; the effective range boundary is inclusive.

**Target is in an unloaded chunk:** load the destination normally. The Transporter deliberately does not force-load remote chunks.

**Cycle reaches completion but nothing moves:** inspect the destination for solid blocks/insufficient headroom. A fully obstructed arrival aborts without spending the transport FE cost.

**Drive is present but target is wrong:** re-bind or inspect the Transport Flash Drive rather than replacing the Transporter.

**Machine cannot be opened/removed:** check ownership/security state and use the appropriate protocol with the authorised player.

**Machine worked before upgrades but not after:** compare current FE demand and effective timing/range with the available supply.

The Transporter remains a local same-dimension entity-movement system in the active 0.7 port.
