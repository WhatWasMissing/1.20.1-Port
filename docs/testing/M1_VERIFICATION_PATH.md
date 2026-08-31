# Matter Overdrive 1.20.1 — Milestone 1 verification path

Do not begin Milestone 2 until every **GO gate** below passes.

## Test environment

- Windows 10/11
- 64-bit JDK 17
- Internet access for the first Gradle/Forge dependency download
- Test the workspace by itself first. Do **not** add Oculus, shaders, performance mods, or the main modpack until the isolated test passes.

## Gate A — Java

Open Command Prompt in the extracted project folder and run:

```bat
java -version
```

**PASS:** the first version line begins with `17.` and it is a 64-bit JDK/JVM.

**FAIL:** any other major Java version or Java is not found.

## Gate B — automated resource + compile test

Double-click:

```text
VERIFY_M1_BUILD.bat
```

Or run:

```bat
VERIFY_M1_BUILD.bat
```

The script will:

1. confirm Java 17;
2. bootstrap pinned Gradle 8.1.1 on first use and verify its SHA-256;
3. run `verifyM1Resources`;
4. parse every active JSON resource;
5. verify the M1 inventory is exactly 75 blocks, 72 block items, 98 standalone items and 57 sound events;
6. run `clean build` against Minecraft 1.20.1 / Forge 47.4.10.

**PASS:** the window ends with `M1 BUILD GATE PASSED` and this file exists:

```text
build\libs\matteroverdrive-0.8.0.0-alpha.3.jar
```

**FAIL:** stop here. Send the complete console output. Do not start Milestone 2.

## Gate C — Forge client startup

Run:

```text
RUN_M1_CLIENT.bat
```

Wait for the Forge development client to reach the title screen.

**PASS conditions:**

- Minecraft reports version 1.20.1;
- Forge is 47.4.10;
- Matter Overdrive is present in the Mods list;
- no mod-loading error screen appears;
- after quitting the client, `RUN_M1_CLIENT.bat` automatically runs `CHECK_M1_RUNTIME.bat`;
- the runtime checker must report **no Matter Overdrive missing-texture model warnings**;
- `run\logs\latest.log` contains a line beginning with `M1 VERIFY:` and reports:
  - `blocks=75`
  - `blockItems=72`
  - `standaloneItems=98`
  - `sounds=57`

**PASS:** after closing Minecraft, the console also prints `M1 RUNTIME LOG GATE PASSED`.

**FAIL:** send `run\logs\latest.log` plus the newest file from `run\crash-reports\` if that folder exists.

## Gate D — clean-world content test

Create a **new disposable Creative world**. Do not open an existing world.

In the inventory/search, verify these representative entries exist:

- Tritanium Ingot
- Tritanium Block
- Dilithium Ore
- Matter Decomposer / Decomposer
- Matter Replicator / Replicator
- Matter Analyzer
- Pattern Storage
- Pattern Monitor
- Android Station
- Transporter
- Fusion Reactor Controller
- Phaser
- Phaser Rifle
- Plasma Shotgun
- Matter Scanner

Then place these blocks in a line:

1. Tritanium Block
2. Machine Hull
3. Decomposer
4. Replicator
5. Matter Analyzer
6. Pattern Storage
7. Pattern Monitor
8. Android Station
9. Transporter
10. Fusion Reactor Controller
11. Industrial Glass
12. Tritanium Crate

For each placed block:

- it places without crashing;
- its block model renders (temporary M1 models are acceptable);
- it can be broken in Creative without crashing;
- no purple/black missing-texture model is acceptable for the representative test set;
- right-clicking unfinished machines must not crash. No functional GUI is required in M1.

## Gate E — save/reload test

1. Place the 12 representative blocks again.
2. Save and quit to title.
3. Re-open the same test world.

**PASS:** the world loads and the blocks remain present without missing-registry warnings or remapping errors.

## Gate F — dedicated-server smoke test

From the project folder run:

```bat
gradlew.bat runServer
```

The first run will create `run\eula.txt` and stop. Change:

```text
eula=false
```

to:

```text
eula=true
```

Then run `gradlew.bat runServer` again.

**PASS:** the server reaches the normal `Done` state with Matter Overdrive loaded and no client-only class crash.

Stop it with:

```text
stop
```

## GO / NO-GO decision

### GO to Milestone 2 only when all are true

- Gate A: Java 17 passes
- Gate B: `clean build` passes
- Gate C: client reaches title screen and the M1 registry log reports 75 / 72 / 98 / 57
- Gate D: representative items exist and representative blocks place/break without a crash
- Gate E: save/reload works without registry errors
- Gate F: dedicated server reaches `Done`

### NO-GO

Any crash, missing registry entry, build failure, invalid JSON, dedicated-server client-class crash, or save/reload registry failure is an M1 defect and should be fixed before machine functionality is added.

## What to send back after testing

If everything passes, send:

```text
M1 PASS
Build: PASS
Client: PASS
Registry log: 75 / 72 / 98 / 57
Placement: PASS
Reload: PASS
Server: PASS
```

If anything fails, send the failed gate plus the requested log/output. That is enough to diagnose the next change without guessing.
