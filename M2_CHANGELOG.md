# Milestone 2 changelog


## 0.8.0.0-alpha.4.1 - Pattern Monitor Network Discovery Fix

- Fixed Pattern Monitor failing to discover patterns already stored in a powered Pattern Storage.
- Data-network traversal now treats Network Pipe, Network Router, Network Switch, Matter Analyzer, Pattern Storage, Pattern Monitor, and Replicator as nodes on the same graph.
- Added an `M2 NETWORK:` log marker reporting how many powered Pattern Storage nodes and patterns a Pattern Monitor discovers.
- Matter Pipe and Heavy Matter Pipe remain a separate matter-transport graph.

## Existing Decomposer foundation
- Preserves the verified alpha.2 Matter Decomposer, FE, matter capability, persistence, GUI synchronization, and Creative Battery behavior.
- Refined Matter Dust can now be fed back into the Decomposer while raw failure dust is reserved for recycling.

## Matter Recycler
- Replaces the M1 placeholder `matter_recycler` with a BlockEntity machine, menu, screen, FE capability and persistent inventory.
- Ports the exact 1.12.2 constants: 512000 FE capacity, 80 recycle-speed coefficient, and 1000 FE per matter.
- Ports the logarithmic legacy recycle-speed formula.
- Converts raw Matter Dust into refined Matter Dust while preserving its NBT matter value.

## Matter Analyzer
- Replaces the M1 placeholder `matter_analyzer` with a functional machine.
- Ports the legacy analysis constants: 800 ticks, 64000 FE per analyzed item, and 20 pattern-progress points per consumed item.
- Pattern Drives persist the analyzed ItemStack, matter value, and 0-100% analysis progress in NBT.
- Creative Pattern Drive reaches 100% in one analysis cycle as the alpha verification helper; normal Pattern Drives retain 20% per item.

## Replicator
- Replaces the M1 placeholder `replicator` with a functional FE + matter machine.
- Ports 1024 kM and 512000 FE capacities, 120 replication speed coefficient, 16000 energy coefficient, and 0.5% base failure chance.
- Ports the legacy logarithmic replication speed/energy formulas and analysis-progress failure penalty.
- Receives matter through the custom Matter capability, including automatic transfer from the verified Decomposer.
- Success produces the Pattern Drive's stored ItemStack; failure produces raw Matter Dust with the matching matter value.

## Pattern network bridge
- Analyzer and Replicator communicate through Pattern Drive NBT for alpha.3.
- This deliberately does not claim the original Matter Network / Pattern Storage / Pattern Monitor system is complete. Those are a later milestone.

## 0.8.0.0-alpha.4.1 - Pattern and Matter Network Foundation

- Added functional Pattern Storage block entity, inventory, menu and developer screen.
- Restored the legacy Pattern Storage layout of six Pattern Drive slots.
- Restored the legacy normal Pattern Drive capacity of two patterns per drive (12 normal entries per Pattern Storage).
- Pattern Drives now support multiple persistent patterns while retaining compatibility with alpha.3 single-pattern NBT.
- Added functional Pattern Monitor block entity, menu and developer screen.
- Restored the legacy Pattern Monitor task queue capacity of 8 and 40-tick replication search/dispatch interval.
- Added Network Pipe graph traversal for Analyzer -> Pattern Storage -> Pattern Monitor -> Replicator pattern/task communication.
- Analyzer can now analyze directly into a connected powered Pattern Storage when its local Pattern Drive slot is empty.
- Replicator can now accept a network replication task while retaining direct Pattern Drive operation as a fallback.
- Added Matter Pipe / Heavy Matter Pipe graph traversal for Decomposer -> Replicator matter transport.
- Corrected Matter Analyzer FE capacity to the legacy 512,000 FE value.
- Preserved the verified Decomposer, Recycler, Analyzer and Replicator processing formulas from alpha.3.
- GUI work remains intentionally developer-grade while functionality is being ported.
