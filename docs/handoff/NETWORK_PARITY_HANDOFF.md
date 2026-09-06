# Network Parity Pass Handoff

Branch: `testing/main`
Base head: `50b1c26706b08050013b28013f59415ab1ab1cb3`
Final head: use the commit containing this handoff / current `testing/main` head after the network pass.
Authoritative references: Matter Overdrive 1.7.10 0.4.2 and 1.12.2 0.7.1.0.

## What changed

- Audited legacy Router, dispatcher/broadcaster interfaces, packet queue and destination-filter behavior.
- Confirmed there is no source-backed standalone Network Controller/Dispatcher/Broadcaster machine to add.
- Preserved existing Forge item-network transfer semantics: network pipes, Switch gating, round robin, exact item+NBT filtering, Network Flash Drive destinations, Speed/Hyper-Speed budgets, 10 FE/item and multi-router anti-duplication.
- Added explicit route failure/status diagnostics.
- Added topology telemetry for router count, disabled switches, compatibility Pylon links and graph-cap state.
- Added persistent bounded sixteen-entry successful-route history, matching the recovered legacy task-queue size as a modern diagnostic analogue.
- Expanded Network Router GUI into a two-column operator panel while keeping all five real machine slots and player inventory visible.
- Added `docs/reference/NETWORK_PARITY.md` and `docs/testing/NETWORK_ROUTER_PARITY_TESTING.md`.

## Important discovery for the later visual/system pass

The authoritative 1.12 Pylon is a Dimensional Pylon multiblock/power machine (`TileEntityMachineDimensionalPylon`), not the current 1.20 matching-channel item-network relay. The relay remains temporarily as a compatibility feature to avoid breaking existing worlds. Do not claim legacy Pylon parity until the dedicated Dimensional Pylon pass restores its multiblock, charge, matter drain and generation mechanics.

## Runtime / visual verification still required

- Router operator layout at GUI scales 2, 3 and Auto.
- All five machine slot hitboxes after the widened layout.
- Each new stall/failure label under real routing conditions.
- Multi-router executor/standby transitions.
- Graph-cap diagnostics on a deliberately large test network.
- Compatibility Pylon link telemetry.

## Next pass

Start from the exact current `testing/main` head, re-fetch before editing, then audit the Star Map strategic layer against the 1.7 authoritative classes. First verify whether any ship/building classes are truly absent before adding new ones; current port already includes Scout/Colonizer, Base, Ship Factory, Hangars, Matter Extractors, Power Generators and Residential buildings. Prioritize missing source-backed travel events, galaxy/homeworld generation details and economic consequences rather than inventing classes already represented.
