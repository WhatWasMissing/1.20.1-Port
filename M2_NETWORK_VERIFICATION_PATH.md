# M2 alpha.4 - Pattern / Matter Network Verification

Target: Minecraft 1.20.1, Forge 47.4.10, Java 17.

## Gate A - Build

Run:

```bat
VERIFY_M2_BUILD.bat
```

Required result:

```text
[PASS] M2 BUILD GATE PASSED
```

Expected JAR:

```text
build\libs\matteroverdrive-0.8.0.0-alpha.4.1.jar
```

Stop and send the full console output if this gate fails.

## Gate B - Regression

Run:

```bat
RUN_M2_CLIENT.bat
```

Confirm the already-verified machines still work:

- Decomposer accepts FE and converts Dirt to 1 kM.
- Recycler processes raw Matter Dust.
- Analyzer can still write directly to a Pattern Drive if one is inserted.
- Replicator can still read a Pattern Drive directly as a fallback.

GUI layout is still developer-grade and is not a failure unless it prevents testing.

## Gate C - Pattern Storage network analysis

Build this data network using `Network Pipe`:

```text
Matter Analyzer -- Network Pipe -- Pattern Storage
                                  |
                              Pattern Monitor
                                  |
                              Network Pipe
                                  |
                               Replicator
```

Direct adjacency also works in alpha.4, but Network Pipe is the intended test.

Pattern Storage setup:

1. Put a Creative Battery into its energy slot.
2. Put one normal Pattern Drive into any of its six drive slots.
3. The storage should report power and `Patterns: 0 / 12` initially.

Analyzer setup:

1. Put a Creative Battery in the Analyzer.
2. Leave the Analyzer Pattern Drive slot EMPTY.
3. Put 5 Dirt into the Analyzer input.

Expected:

- Each ~40 second analysis consumes one Dirt.
- Pattern Storage receives the pattern over Network Pipe.
- The stored Dirt pattern progresses 20 -> 40 -> 60 -> 80 -> 100%.
- Removing the Pattern Drive from Pattern Storage and hovering it should show Dirt at 100%, 1 kM.
- A normal Pattern Drive can contain 2 distinct patterns.
- Pattern Storage has 6 drive slots, giving 12 normal pattern entries total.

Put the Pattern Drive back into Pattern Storage after checking it.

## Gate D - Pattern Monitor -> Replicator task

1. Keep Pattern Storage, Pattern Monitor, and Replicator on the same Network Pipe graph.
2. Put a Creative Battery into the Replicator.
3. Open Pattern Monitor.
4. Dirt should appear as one of the network patterns.
5. Click the Dirt icon once.

Expected:

- Pattern Monitor queue increases briefly (up to 8 tasks supported).
- Within about 2 seconds / 40 ticks, the request is accepted by a connected Replicator.
- The Replicator shows the network pattern even with its local Pattern Drive slot empty.

## Gate E - Matter Pipe transport

Build a separate matter path:

```text
Decomposer -- Matter Pipe -- Replicator
```

`Heavy Matter Pipe` is also accepted by the alpha.4 matter transport traversal.

1. Put Creative Batteries into the Decomposer and Replicator.
2. Decompose one Dirt.
3. The resulting 1 kM should travel through the Matter Pipe to the Replicator.
4. With the queued Dirt task from Pattern Monitor, the Replicator should output 1 Dirt.
5. Its matter should return to 0 kM after successful replication.

This proves the intended alpha.4 loop:

```text
Analyzer -> Pattern Storage -> Pattern Monitor -> Replicator
                Network Pipe / task data

Decomposer -> Matter Pipe -> Replicator
                matter transport
```

## Gate F - Queue and persistence

- Click the Dirt pattern several times in Pattern Monitor. Queue capacity is 8.
- Provide enough matter for multiple replications and verify repeated Dirt output.
- Save and quit while Pattern Storage contains patterns.
- Reopen the world.
- Pattern Drives and their pattern data must persist.
- Pattern Monitor should rediscover the stored patterns after reopening.
- Replicator FE, matter, inventory and any accepted in-progress network task must persist.

## Gate G - Runtime and server

After closing the client normally, run:

```bat
CHECK_M2_RUNTIME.bat
```

Required:

```text
[PASS] M2 RUNTIME LOG GATE PASSED
```

Then run:

```bat
RUN_M2_SERVER.bat
```

The dedicated server must reach `Done`. Ctrl+C is acceptable after that because ForgeGradle userdev may not forward the Minecraft `stop` command.

## PASS report

```text
M2 alpha.4 PASS
Build: PASS
Regression: PASS
Network analysis: PASS
Pattern Storage: PASS
Pattern Monitor: PASS
Network task dispatch: PASS
Matter Pipe: PASS
Replicator network output: PASS
Persistence: PASS
Runtime checker: PASS
Server: PASS
```
