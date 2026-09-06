# Network Router Parity Testing

Authoritative references: Matter Overdrive 1.7.10 0.4.2 and 1.12.2 0.7.1.0.

## Routing regression
- [ ] Two ordinary item-handler endpoints connected through Network Pipes still transfer items.
- [ ] Multi-pipe chains continue routing after save/reload and pipe break/replacement.
- [ ] Transfer costs exactly 10 FE per item actually inserted.
- [ ] Router stops cleanly below 10 FE and resumes when powered.
- [ ] Ordinary filter slot moves only the exact item+NBT match.
- [ ] Empty filter routes any eligible item.
- [ ] Network Flash Drive restricts destinations to recorded block positions.
- [ ] An installed but empty Network Flash Drive permits no destinations rather than silently routing everywhere.
- [ ] Speed and Hyper-Speed upgrades change item budget, persist and remain the only accepted Router upgrades.
- [ ] Multiple connected Routers do not duplicate transfers; one powered Router executes while other graph members report standby.
- [ ] Existing route-sink protection still prevents immediate ping-pong/oscillation.

## Switch / topology diagnostics
- [ ] Disabled Network Switch prevents traversal exactly as before.
- [ ] Router reports at least one blocked/disabled switch when a disabled switch borders the scanned graph.
- [ ] Re-enabling the switch restores downstream endpoint discovery without rebuilding the network.
- [ ] Endpoint/node/router/Pylon counts update after placing/breaking graph components.
- [ ] A graph reaching the 1,024-node safety cap reports GRAPH CAPPED rather than hanging the server.

## Route status diagnostics
- [ ] No/one endpoint reports NEEDS 2 ENDPOINTS.
- [ ] Empty sources report NO SOURCE ITEMS.
- [ ] Non-matching ordinary filter reports FILTER MISS.
- [ ] Destination-drive with no reachable recorded destination reports NO ALLOWED DESTINATION.
- [ ] Full/blocked valid destinations report DESTINATION FULL.
- [ ] Unpowered executor graph reports NEEDS FE.
- [ ] Secondary routers report STANDBY ROUTER.
- [ ] Successful transfer reports ROUTING and resets stall count.
- [ ] Failed ticks increase the displayed stall count without changing routing behavior.
- [ ] Successful-route history remains bounded at 16 entries and survives save/reload.

## GUI
- [ ] Router screen opens at all common GUI scales without slot/text overlap.
- [ ] Filter and all four real upgrade slots align with their visible slot frames.
- [ ] Player inventory/hotbar slots align and remain usable.
- [ ] Filter/throughput panel correctly distinguishes NO FILTER / ITEM FILTER / DESTINATION DRIVE.
- [ ] Routing panel shows active executor vs graph-member state and live E/N/R counts.
- [ ] Moved, FE/t, stalls, route-history depth, blocked switches and guarded sinks agree with server behavior.
- [ ] No field shown as a button/control is decorative; the screen remains telemetry plus real physical slots only.

## Pylon compatibility note
- [ ] Existing matching-channel Pylon links continue working during this pass.
- [ ] Pylon link count changes when compatible channel links are added/removed.
- [ ] Treat wireless network-Pylon behavior as compatibility functionality pending the dedicated Dimensional Pylon restoration pass; do not use this test as proof of legacy Pylon parity.
