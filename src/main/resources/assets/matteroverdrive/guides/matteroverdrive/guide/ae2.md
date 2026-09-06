---
navigation:
  title: Applied Energistics 2
  parent: index.md
  position: 8
  icon: matteroverdrive:network_router
---
# Applied Energistics 2 Integration

Matter Overdrive 1.20.1 integrates with Applied Energistics 2 15.4.10+ through the same Forge `IItemHandler` capability path used by AE2's own `ForgeExternalStorageStrategy` and `ItemHandlerAdapter`. This is the complete supported bridge: ME item buses see the public automation inventory of an MO machine without merging the two network systems.

## Supported automation targets

- **Tritanium Crates:** normal storage inventory; Storage Bus, Import Bus and Export Bus can use the exposed item handler.
- **Decomposer:** public input, energy-item and output inventory plus separate Forge Energy and Matter capabilities. Upgrade slots stay private.
- **Matter Recycler:** public processing inventory plus Forge Energy. Upgrade slots stay private.
- **Molecular Inscriber:** primary/secondary input, output and energy-item inventory plus Forge Energy. Upgrade slots stay private.
- **Replicator:** public pattern/output/energy inventory plus Forge Energy and Matter. Replication task/progress state remains machine-owned and is not exposed as ME storage.
- **Pattern Storage:** Pattern Drive/energy inventory plus Forge Energy. Pattern Drive NBT remains on the item when moved by automation.

## Recommended bus use

Use an **Export Bus** to feed machine inputs, an **Import Bus** to collect completed outputs, or a **Storage Bus** when you intentionally want the full public machine inventory visible to ME. AE2 respects each handler's normal insertion validity; MO never exposes its machine-upgrade inventory through the public handler.

If an automation setup behaves unexpectedly, point at the machine and run `/moae2`. The runtime audit reports every face on which Forge item automation is visible, the slot count AE2 can discover, and whether FE/Matter capabilities are also present.

## Energy and Matter

Matter Overdrive uses Forge Energy and its own Matter capability. AE2 power remains AE2-owned. Matter Overdrive does **not** convert AE power to FE, does not expose Matter as an AE fluid/item substitute, and does not let a Storage Bus bypass a machine's normal processing costs.

## Network boundary

AE2 ME networks and Matter Overdrive Router/Switch/Pylon networks remain separate. They may meet at ordinary inventories: for example, an ME Export Bus can insert items into a Tritanium Crate or machine that is also reachable by a Matter Overdrive item-routing network. Each network still performs its own transfer and energy rules.

## Persistence rules

Automation must remain safe across chunk unload/reload. Machine inventories, Pattern Drive NBT, FE/Matter storage and active machine progress are server-owned. A bus reconnecting after a chunk reload must see the restored public inventory rather than a duplicate or replacement inventory.

## Optional dependency

AE2 is optional. Matter Overdrive loads and remains fully playable without it. When AE2 is installed, no additional compatibility addon is required.
