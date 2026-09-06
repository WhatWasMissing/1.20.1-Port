---
navigation:
  title: Applied Energistics 2
  parent: index.md
  position: 8
  icon: matteroverdrive:network_router
---
# Applied Energistics 2 Integration

Matter Overdrive integrates with Applied Energistics 2 through standard Forge capabilities rather than replacing either mod's network system.

## Storage Bus automation

An AE2 Storage Bus can use Forge item-handler inventories exposed by Matter Overdrive blocks. Attach it to the side of a machine or storage block that exposes the inventory you want to automate. Matter Overdrive keeps internal/private state separate from public automation slots.

Useful targets include storage crates and the public input/output inventories of processing machines such as the Decomposer, Recycler, Inscriber and Replicator.

## Energy

Matter Overdrive uses Forge Energy. AE2's own power rules remain AE2-owned; this integration does not silently convert AE energy into Matter Overdrive FE or bypass either mod's intended power system.

## Networks

AE2 ME networks and Matter Overdrive Router/Switch networks remain distinct systems. They can meet at capability-exposed inventories. This preserves the original identity of both systems and avoids requiring AE2 for a normal Matter Overdrive playthrough.

## Optional dependency

AE2 is optional. Matter Overdrive loads and remains playable when AE2 is absent.
