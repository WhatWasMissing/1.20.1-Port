# PDA Voice Bank Production

The approved Matter Overdrive PDA direction is a natural neural voice-actor performance followed by restrained synthetic post-processing. Do not ask the source TTS/VA system to perform an exaggerated robot voice; preserve human timing and pronunciation, then synthesize around it.

## Catalogue contract

The prerecorded campaign/facility/field-log bank is defined by:

- `src/main/java/matteroverdrive/pda/PdaVoiceLineCatalog.java`
- `src/main/resources/assets/matteroverdrive/pda_voice/voice_bank_manifest.json`

Major technology discovery callouts are authored beside their full codex entries in:

- `src/main/java/matteroverdrive/world/TechnologyLoreCatalog.java`

This avoids maintaining the same machine/item text in two handwritten catalogues. `PdaVoiceLineCatalog.line(...)` resolves both the core bank and `tech_*` technology IDs at runtime, so a missing prerecorded technology WAV automatically falls through to local OS speech, Minecraft Narrator and captions.

For voice production, run:

```bat
EXPORT_PDA_VOICE_QUEUE.bat
```

That writes `tools/pda_voicebank/voice_generation_queue.json` by merging the stable packaged manifest with every technology discovery line. The exporter validates duplicate/empty IDs and reports the live catalogue counts, so future technology additions are automatically included.

The packaged bank currently starts with the original 64 authored lines:

- 16 core/system/campaign callouts;
- 16 unique facility-discovery callouts, one for every canonical structure;
- 32 physical-lore interpretation lines, one for every optional recovered field record.

The generated production queue then appends all current `tech_*` discovery callouts for functional machines, networks, reactor/anomaly hardware, weapons, Android/drone systems, field tools, storage media and progression equipment.

## Workflow

1. Run `EXPORT_PDA_VOICE_QUEUE.bat` to produce the complete live generation queue.
2. Generate or record a natural performance for each queue line that does not already have an approved master.
3. Name each source `<line_id>.mp3` (WAV/OGG/FLAC/M4A are also accepted).
4. Put sources in `tools/pda_voicebank/raw/`.
5. Run `PROCESS_PDA_VOICE_BANK.bat`.
6. Review the generated 48 kHz mono WAVs in `src/main/resources/assets/matteroverdrive/pda_voice/`.
7. Compare important warnings and several ordinary lines against the approved ICARUS synthetic-VA reference before shipping.
8. Either run the normal Gradle build so those WAVs are bundled automatically, or inject them into an already-built JAR with:

```bat
INJECT_PDA_VOICES_INTO_JAR.bat build\libs\matteroverdrive-0.7.jar
```

The injector creates a separate `matteroverdrive-0.7-pda-voice.jar` and leaves the original build untouched.

The processor preserves the dry performance and adds only a quiet synthetic character layer: opposing micro-pitch ghosts, millisecond offsets, communications EQ/compression, tiny digital reflection and the Matter Overdrive PDA identification chime.

## Voice groups

Core lines include startup, database, hazard, social-progression and Incident-reconstruction events.

`site_*` lines play when the player first authenticates the canonical record for a facility. Hazard callouts remain separate, so an unsafe facility can produce an immediate safety warning followed by a quieter historical identification line.

`lore_*` lines are short PDA interpretations of optional physical notes found in crates, salvage and archive caches. The full recovered note remains readable in the physical item and the PDA `FIELD LOGS` tab; the voice should summarize rather than recite the entire document.

`tech_*` lines play only when that technology family is first authenticated for that player/world. Variant items deliberately share a canonical record where separate narration would be noise: crate colours, Android body parts, chassis modules, storage-cell tiers, machine upgrades, security media, weapon modules, Tritanium tools and Tritanium armour are grouped into families.

## Runtime playback order

`PdaEmbeddedAudio` uses this order:

1. `config/matteroverdrive/pda_voice/<line_id>.wav` — local/modpack override;
2. bundled `/assets/matteroverdrive/pda_voice/<line_id>.wav` inside the JAR;
3. local OS speech synthesis;
4. Minecraft Narrator;
5. captions/text remain available regardless.

That means a processed WAV can be tested without rebuilding the mod: put it in the Minecraft instance's `config/matteroverdrive/pda_voice/` directory and restart the game. The local file intentionally overrides the copy bundled inside the JAR.

Binary voice assets therefore do not need to be committed to GitHub. When a bank is approved, either bundle it during the normal build or use the local JAR injector.

## Pronunciation

Treat these as named proper terms and verify them by ear before release:

- ICARUS
- ORPHEUS
- JANUS
- LAGRANGE
- ECHO-9 / ECHO-nine
- HEPHAESTUS
- MORROW
- M-zero
- DUSTWELL
- MNEMOSYNE
- KESTREL
- NEREID
- Halcyon-7
- Bastion

The voice bank is an original Matter Overdrive performance direction; it should evoke an advanced field computer without imitating a specific existing game actor or performance.
