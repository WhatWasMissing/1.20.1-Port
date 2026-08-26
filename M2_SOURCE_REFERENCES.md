# M2 source behavior references

Primary behavior reference: MatterOverdrive/MatterOverdrive-Legacy-Edition, branch `1.12.2`.

- Decomposer: `machines/decomposer/TileEntityMachineDecomposer.java`
- Recycler: `tile/TileEntityMachineMatterRecycler.java`
- Analyzer: `machines/analyzer/ComponentTaskProcessingAnalyzer.java` and `TileEntityMachineMatterAnalyzer.java`
- Replicator: `machines/replicator/TileEntityMachineReplicator.java` and `ComponentTaskProcessingReplicator.java`
- Matter values: `init/MatterOverdriveMatter.java`
- Creative Battery: `items/CreativeBattery.java`

Secondary modern API reference: ibonny/MatterOverdrive-Community-Edition-1.19.2 branch `1.19`.

Alpha.3 used a temporary direct Pattern Drive NBT bridge. Alpha.4 replaces the main path with functional Pattern Storage, Pattern Monitor, Network Pipe task discovery, and Matter Pipe transport while retaining direct Pattern Drive operation as a fallback.
