# M2 source behavior references

Authoritative legacy parity references for current port work:

- Matter Overdrive 1.7.10 `0.4.2` (`MatterOverdrive-1.7.10-0.4.2.jar`).
- Matter Overdrive 1.12.2 `0.7.1.0` (`MatterOverdrive-1.12.2-0.7.1.0-universal.jar`).

These two legacy builds are the reference pair for GUI layout, machine presentation, front-facing controls, progression concepts and behavior that still maps cleanly onto the 1.20.1 backend. Where the two versions differ, prefer functionality that is common to both or preserve the newer 1.20.1 behavior rather than reproducing a legacy limitation.

The `0.8.0.0-alpha.4.1` jar is an older port build and is explicitly excluded as a parity authority. It may only be consulted as historical port archaeology when diagnosing a regression, not as a source-of-truth for original Matter Overdrive behavior.

Primary source-code behavior reference: MatterOverdrive/MatterOverdrive-Legacy-Edition, branch `1.12.2`.

- Decomposer: `machines/decomposer/TileEntityMachineDecomposer.java`
- Recycler: `tile/TileEntityMachineMatterRecycler.java`
- Analyzer: `machines/analyzer/ComponentTaskProcessingAnalyzer.java` and `TileEntityMachineMatterAnalyzer.java`
- Replicator: `machines/replicator/TileEntityMachineReplicator.java` and `ComponentTaskProcessingReplicator.java`
- Matter values: `init/MatterOverdriveMatter.java`
- Creative Battery: `items/CreativeBattery.java`

The 1.7.10 `0.4.2` binary is additionally used to validate original GUI structure and feature ancestry. In particular, its `MOGuiMachine` establishes the shared Home / Configurations / Upgrades model, while machine-specific GUIs add task or specialist pages only when the machine has matching functionality.

Secondary modern API reference: ibonny/MatterOverdrive-Community-Edition-1.19.2 branch `1.19`.

Historical port note: Alpha.3 used a temporary direct Pattern Drive NBT bridge. Alpha.4 replaced the main path with functional Pattern Storage, Pattern Monitor, Network Pipe task discovery, and Matter Pipe transport while retaining direct Pattern Drive operation as a fallback. This note documents port history only and does not make the alpha jar a parity reference.
