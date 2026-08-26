# Matter Overdrive 1.20.1 Port

Work-in-progress Forge 1.20.1 source port of Matter Overdrive Legacy Edition.

## Target

- Minecraft 1.20.1
- Forge 47.4.10
- Java 17
- Mod ID `matteroverdrive`
- Current port version `0.8.0.0-alpha.4.1`

## Milestones

### M1 - verified

M1 established the registry/resource shell and was verified on a real Forge client and dedicated server:

- 75 block IDs
- 72 block items
- 98 standalone items
- 57 sound events
- active 1.20.1-safe models/resources
- client placement/reload verification
- dedicated-server verification

### M2 - current test build

M2 alpha.4 extends the verified machine foundation with the first real networked gameplay loop:

- functional Matter Decomposer
- functional Matter Recycler
- functional Matter Analyzer
- functional Matter Replicator
- six-slot Pattern Storage
- two patterns per normal Pattern Drive (legacy capacity)
- Pattern Monitor with an 8-request queue
- Network Pipe pattern/task discovery
- Matter Pipe / Heavy Matter Pipe matter transport
- persistent machine inventory, FE, matter, pattern data and replication tasks
- direct Pattern Drive operation retained as a fallback

Use `M2_NETWORK_VERIFICATION_PATH.md` and start with `VERIFY_M2_BUILD.bat`.

The GUIs are intentionally developer-grade while functionality is being ported. Major later work still includes upgrades, routing/configuration, Pattern Storage/Monitor polish, additional machines, Android systems, weapons, entities, world generation, transporter, fusion reactor and compatibility systems.

