# PDA Technology Codex Runtime Test Plan

Use a fresh test world/player where practical. Existing mature inventories are valid migration tests but should not be the only coverage.

## Static/local gate

Run:

```bat
VALIDATE_PDA_TECHNOLOGY_LORE.bat
EXPORT_PDA_VOICE_QUEUE.bat
```

The validator should report broad major-technology coverage, persistent discovery wiring, Data Pad presentation and voice-production hooks. The exporter should produce a generation queue with no duplicate IDs.

## First acquisition

1. Craft a Matter Decomposer.
2. Confirm exactly one `PDA // TECHNOLOGY INDEXED` notification appears.
3. Confirm a short Decomposer PDA voice/caption is queued.
4. Drop and pick the same Decomposer back up.
5. Move it between inventory/container slots.
6. Confirm no second discovery notification/voice occurs.

Repeat representative coverage with:

- Pattern Drive;
- Network Router;
- Weapon Battery;
- Phaser;
- Android Station;
- Drone Deployment Core;
- Reactor Remote;
- Space-Time Equalizer.

## Block discovery by scanning

1. Locate/place an MO machine the test player has never owned, e.g. Matter Analyzer.
2. Right-click it with the Data Pad.
3. Confirm the normal scan-history message still occurs.
4. Confirm the Technology Codex indexes the Analyzer once.
5. Scan it again and confirm the technology notification does not repeat.
6. Pick the block up afterward and confirm acquisition also does not replay the voice.

## Placement path

Use `/give` or Creative to place a previously unseen machine without waiting for the periodic inventory audit. Confirm block placement immediately authenticates the corresponding technology once.

## Inventory migration / anti-spam

1. Give a player several unseen major MO items before logging/reloading.
2. Wait through multiple 40-tick inventory audits.
3. Confirm at most one new technology record is emitted per audit.
4. Confirm all carried major technologies eventually authenticate.
5. Confirm the PDA voice queue speaks sequentially rather than overlapping.

## Alias/family deduplication

Verify each family indexes only once:

- Tritanium crate plus two coloured crate variants;
- Android head/chest/arms/legs parts;
- at least three Android chassis modules;
- 64k and 4m Matter Storage Cells;
- Speed, Range and Failsafe machine upgrades;
- Claim and Access security media;
- Tritanium pickaxe and sword;
- Tritanium helmet and chestplate;
- two weapon barrel/sight modules plus a colour module.

The first family member should unlock the canonical record; later variants should be silent.

## PDA TECHNOLOGY tab

1. Open the Data Pad after collecting several technologies.
2. Confirm Overview shows `Technology codex: X/Y` independently from Incident and Field Logs.
3. Open TECHNOLOGY.
4. Confirm PREV/NEXT traverses only authenticated entries.
5. Confirm title, category, registry ID, FUNCTION, ARCHIVE CONTEXT and FIELD NOTE are readable.
6. Confirm READ ALOUD reads the long-form entry and STOP interrupts it.
7. Test small GUI scale/window height and ensure sidebar tabs do not overlap the Technical Manual button.

## Voice fallback

For a technology with no prerecorded WAV:

1. Confirm first discovery uses local OS TTS when available.
2. Disable/unavailable OS TTS and enable Minecraft Narrator; confirm narrator fallback.
3. Confirm the textual discovery message remains even if both speech paths fail.

For a technology with a locally generated WAV in `config/matteroverdrive/pda_voice/<tech_id>.wav`, confirm the prerecorded processed voice takes precedence over OS speech.

## Persistence

1. Discover at least five technologies.
2. Change dimensions.
3. Relog.
4. Restart the world/server.
5. Reacquire the same technologies.
6. Confirm no duplicate first-discovery announcements occur.
7. Confirm TECHNOLOGY entries remain present in the PDA.

## Multiplayer isolation

With two players on the same server, have Player A discover a Phaser and Player B discover a Replicator. Confirm each player's Technology Codex and first-discovery voices are independent.

## Regression checks

- Data Pad legacy crop-scanner quest still scans/destroys its intended crop targets.
- Generic Data Pad block scan history still records ordinary blocks.
- FIELD LOGS and primary Incident records remain available.
- GuideME Technical Manual link remains accessible.
- No chunk force-loading/worldgen behavior is introduced.
- No retired Star Map UI/system is introduced by this pass.
