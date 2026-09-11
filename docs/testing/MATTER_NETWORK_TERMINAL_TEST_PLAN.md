# Matter Network Terminal Test Plan

1. Build a Matter Storage Matrix, Matter Pipe route, and Matter Network Terminal. Confirm the terminal is craftable and appears in the Matter Overdrive tab.
2. Put Matter in a Matter Container and use it on the terminal. Confirm at most 1,000 Matter moves into the first eligible network endpoint and the container syncs immediately.
3. Empty the container, ensure a connected endpoint stores Matter, and use the terminal again. Confirm up to 1,000 Matter is pulled back into the container.
4. Disable the endpoint's Matter input/output side policy with the Network Diagnostic Probe. Confirm the terminal reports no transfer and does not lose Matter.
5. Break a pipe segment or unload a remote chunk. Confirm traversal remains bounded and the terminal reports no transfer without hanging the server.
6. Connect multiple storage matrices. Confirm repeated requests rotate by game-time route index and do not duplicate or delete matter.
7. Compare the terminal's status message and comparator output with the Facility Network Controller telemetry.
8. With two players using separate containers, confirm transfers affect only the requesting player's held container and shared network totals remain authoritative.
9. Save/reload and restart the server. Confirm stored Matter and endpoint routing remain intact.
10. Connect a Drone Fabricator to an item network containing its three recipe ingredients. Select the matching router channel with the Fabricator's CHANNEL control, then confirm missing ingredients are pulled into the correct slots at a maximum of 16 items per tick, inputs are not consumed until completion, and an interrupted/out-of-energy cycle preserves them.
11. Configure a Logistics Core on a loaded target inventory connected to a channel-0 Network Pipe/Router graph. Deploy the Logistics drone, confirm it pulls arbitrary source stacks into the target with no duplication, honors the target's item input policy, persists its cursor across reload, and falls back to nearby dropped items when the network is empty.

Static coverage is provided by `scripts/validate_network_transport_consistency.py` and `tools/full-sanity-check.ps1`. In-world transfer, side-policy, multiplayer, and persistence checks remain **Needs runtime verification**.
