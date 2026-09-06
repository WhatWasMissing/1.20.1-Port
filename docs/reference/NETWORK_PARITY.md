# Matter Network Parity Pass

Authoritative references: Matter Overdrive 1.7.10 0.4.2 and 1.12.2 0.7.1.0.

## Source-backed findings

- The old `IMatterNetworkDispatcher` / `IMatterNetworkBroadcaster` APIs describe task queues and filters; they are not evidence for missing standalone Dispatcher/Broadcaster blocks.
- 1.12 `PacketDispatcher` is a packet-delivery helper rather than a gameplay machine.
- The legacy packet/task queue uses `TASK_QUEUE_SIZE = 16` and a two-tick broadcast delay. The 1.20 port preserves the useful bounded-task concept as a sixteen-entry routing history/diagnostic queue without slowing the already-working physical item transfer loop.
- The legacy Router exposes a destination-filter slot and Speed-upgrade behavior. The old free-form destination-filter string is not consumed by a meaningful routing backend in the authoritative implementation, so the port does not invent semantics for it.
- Network Flash Drive destination positions are source-backed and remain the authoritative explicit-destination filter in the modern item network.

## Modern routing core retained

- Network Pipe graph traversal.
- Network Switch persisted enable/disable gating.
- Round-robin endpoint selection.
- Exact item+NBT filtering.
- Network Flash Drive destination filtering.
- Speed/Hyper-Speed item budgets.
- 10 FE per successfully transferred item.
- Shared multi-router execution and route-sink anti-oscillation.

## Added operator/diagnostic parity

The Router now reports explicit route state rather than only `moved/t`: active executor vs standby router, endpoint/node/router counts, disabled switches encountered at the graph boundary, compatibility Pylon link count, graph safety-cap state, protected sink count, stall duration and a bounded sixteen-entry successful-route history. Failed route attempts distinguish no endpoints, no source items, filter miss, no allowed destination, full destination, missing FE, standby-router state and graph-limit state.

The Router GUI is widened into a two-column operator panel while keeping the real filter slot, all four Speed/Hyper-Speed slots and the complete player inventory visible and clickable.

## Important Pylon identity finding

The authoritative 1.12 `BlockPylon` / `TileEntityMachineDimensionalPylon` is a **Dimensional Pylon** with multiblock, charge, matter-drain and power-generation concepts. The current 1.20 matching-channel wireless item-network relay is therefore a compatibility feature from the port, not faithful Pylon parity. It is intentionally preserved during this network pass to avoid breaking existing worlds. A dedicated later Pylon pass should restore the actual Dimensional Pylon backend and migrate/preserve compatibility routing deliberately instead of silently deleting current links.
