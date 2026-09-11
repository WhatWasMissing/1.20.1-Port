# PDA Voice Bank Production

The approved Matter Overdrive PDA direction is a natural neural voice-actor performance followed by restrained synthetic post-processing. Do not ask the source TTS/VA system to perform an exaggerated robot voice; preserve human timing and pronunciation, then synthesize around it.

## Catalogue contract

The authoritative voice IDs and transcripts are now stored in both:

- `src/main/java/matteroverdrive/pda/PdaVoiceLineCatalog.java`
- `src/main/resources/assets/matteroverdrive/pda_voice/voice_bank_manifest.json`

The validator requires the two files to have exactly the same IDs in the same order. The bank currently contains **64 authored lines**:

- 16 core/system/campaign callouts;
- 16 unique facility-discovery callouts, one for every canonical structure;
- 32 physical-lore interpretation lines, one for every optional recovered field record.

Do not maintain another handwritten ID list here. Use the manifest when generating or auditing audio so future additions remain scalable.

## Workflow

1. Generate or record a natural performance for each manifest line.
2. Name each source `<line_id>.mp3` (WAV/OGG/FLAC/M4A are also accepted).
3. Put sources in `tools/pda_voicebank/raw/`.
4. Run `PROCESS_PDA_VOICE_BANK.bat`.
5. Review the generated 48 kHz mono WAVs in `src/main/resources/assets/matteroverdrive/pda_voice/`.
6. Compare important warnings and several ordinary lines against the approved ICARUS synthetic-VA reference before shipping.

The processor preserves the dry performance and adds only a quiet synthetic character layer: opposing micro-pitch ghosts, millisecond offsets, communications EQ/compression, tiny digital reflection and the Matter Overdrive PDA identification chime.

## Voice groups

Core lines include startup, database, hazard, social-progression and Incident-reconstruction events.

`site_*` lines play when the player first authenticates the canonical record for a facility. Hazard callouts remain separate, so an unsafe facility can produce an immediate safety warning followed by a quieter historical identification line.

`lore_*` lines are short PDA interpretations of optional physical notes found in crates, salvage and archive caches. The full recovered note remains readable in the physical item and the PDA `FIELD LOGS` tab; the voice should summarize rather than recite the entire document.

## Runtime fallback

`PdaEmbeddedAudio` tries the processed bank first, then `config/matteroverdrive/pda_voice/<line_id>.wav`, local OS TTS, Minecraft Narrator and captions. Missing audio must therefore never break gameplay or hide information.

This is why binary voice assets do not need to be committed to GitHub. A local/modpack voice bank can be injected after the normal Gradle build and the Java catalogue will still work when some or all recordings are absent.

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
