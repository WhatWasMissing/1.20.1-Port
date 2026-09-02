# Handheld Matter Tools Runtime Testing

Branch: `feature/handheld-matter-tools`

This checklist covers the restored Matter Scanner, Portable Decomposer and Data Pad/guide loop.

## Recipes and item state

- [ ] Craft all three items in Survival using the recipe book or normal crafting grid.
- [ ] Confirm the existing item textures/models render correctly in inventory and in hand.
- [ ] Confirm each item is non-stackable.
- [ ] Confirm item NBT/state survives inventory movement, death according to normal keep/drop rules and save/reload.

## Matter Scanner

### Link

- [ ] Place a Pattern Storage, insert a normal Pattern Drive and supply at least 128 FE.
- [ ] Sneak-use the Scanner on it.
- [ ] Confirm 128 FE is consumed once and the tooltip records dimension and coordinates.
- [ ] Confirm an unpowered storage refuses the link without changing the previous valid link.

### Scan

- [ ] Hold-use the linked Scanner on a matter-valued block for the full 60 ticks.
- [ ] Confirm the block is consumed only when Pattern Storage accepts the result.
- [ ] Confirm one block adds 10% to its pattern and ten valid blocks reach 100%.
- [ ] Release early, walk out of range or replace the target mid-scan; confirm no block/pattern change.
- [ ] Test completed pattern, full drive, missing drive, unpowered storage, broken storage, unloaded link and another dimension.
- [ ] Confirm every failure preserves the target block and gives a useful message.
- [ ] Verify Scanner-created patterns remain usable by Pattern Monitor and Replicator.

## Portable Decomposer

### Charge and filter

- [ ] Charge it in a Charging Station to its 128,000 FE capacity.
- [ ] Sneak-right-click in air with a matter-valued item in the other hand.
- [ ] Confirm the item is added to the filter without being consumed.
- [ ] Repeat to remove it; add several distinct filters and verify the tooltip.

### Pickup conversion

- [ ] Pick up a filtered item and confirm it is consumed before entering inventory.
- [ ] Confirm each consumed item costs FE equal to its base matter value.
- [ ] Confirm matter yield is 10% and stored in the 512 kM internal buffer.
- [ ] Test low-value items across enough pickups to cross a whole-kM boundary; confirm fractional remainder is retained.
- [ ] Pick up a mixed/partial stack near full capacity; confirm only items that can be processed are consumed and the remainder enters inventory.
- [ ] Confirm unmatched items, zero-matter items, an empty battery and a full matter buffer do not consume pickups.

### Transfer

- [ ] Use the Portable Decomposer on a compatible matter-receiving machine.
- [ ] Confirm exact matter transfer, immediate tooltip update and no duplication.
- [ ] Confirm use on a non-matter block does nothing.
- [ ] Save/reload with FE, matter, fraction and filters present and repeat the tests.

## Data Pad

- [ ] Use in air and confirm the seven-page guide opens.
- [ ] Navigate Overview, Matter Replication, Power and Machines, Fusion Reactor, Android System, Survival Progression and Scan History.
- [ ] Verify the guide at small, normal and large GUI scales.
- [ ] Use on matter-valued and zero-matter blocks; confirm each history entry records name, registry ID and kM value.
- [ ] Scan more than 16 unique blocks; confirm only the newest 16 remain.
- [ ] Scan the same block again; confirm the duplicate moves to newest rather than multiplying entries.
- [ ] Save/reload and confirm history persists.

## Regression and pass criteria

- [ ] Normal item pickup and contract collection progress still work when no matching Portable Decomposer filter is active.
- [ ] Pattern Analyzer/Storage/Monitor/Replicator behavior remains unchanged.
- [ ] Charging Station still charges batteries and the Portable Decomposer without free FE.
- [ ] Dedicated-server/client launch shows no client-class loading error from the Data Pad packet.

The pass is ready to consolidate when the runtime marker includes `handheldMatterTools=enabled`, all failure paths are non-destructive and no item, FE, matter or pattern duplication is found.