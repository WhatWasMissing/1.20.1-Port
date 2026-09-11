# PDA Voice Bank Production

The approved Matter Overdrive PDA direction is a natural neural voice-actor performance followed by restrained synthetic post-processing. Do not ask the source TTS/VA system to perform an exaggerated robot voice; preserve human timing and pronunciation, then synthesize around it.

## IDs

Raw clips must use the exact `PdaVoiceLineCatalog` IDs:

- `field_link`
- `record_recovered`
- `reconstruction_complete`
- `matter_resonance`
- `anomaly_warning`
- `pressure_warning`
- `structural_warning`
- `signal_echo`
- `orpheus_security`
- `icarus_warning`
- `synthetic_contact`
- `closed_loop`
- `database_ready`

## Workflow

1. Generate/record a natural performance for each line.
2. Name the source `<line_id>.mp3` (or WAV/OGG/FLAC/M4A).
3. Put sources in `tools/pda_voicebank/raw/`.
4. Run `PROCESS_PDA_VOICE_BANK.bat`.
5. Review the generated 48 kHz mono WAVs in `src/main/resources/assets/matteroverdrive/pda_voice/`.
6. Compare important warnings against the approved ICARUS synthetic-VA reference before shipping.

The processor preserves the dry performance and adds only a quiet synthetic character layer: opposing micro-pitch ghosts, millisecond offsets, communications EQ/compression, tiny digital reflection and the Matter Overdrive PDA identification chime.

## Runtime fallback

`PdaEmbeddedAudio` tries the processed bank first, then `config/matteroverdrive/pda_voice/<line_id>.wav`, local OS TTS, Minecraft Narrator and captions. Missing audio must therefore never break gameplay or hide information.

## Pronunciation

Treat these as named proper terms and verify them by ear before release:
- ICARUS
- ORPHEUS
- JANUS
- LAGRANGE
- ECHO-9
- HEPHAESTUS
- MORROW
- M-zero

The voice bank is an original Matter Overdrive performance direction; it should evoke an advanced field computer without imitating a specific existing game actor.
