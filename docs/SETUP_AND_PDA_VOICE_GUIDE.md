# Matter Overdrive 1.20.1 — Setup and PDA Voice Bank Guide

This guide covers a clean Windows setup for the current `main` branch, building Matter Overdrive 0.6 locally, testing the mod, and producing/bundling the new PDA neural voice bank without storing binary audio in GitHub.

## 1. Pull the current main branch

From the Matter Overdrive repository folder:

```bat
git checkout main
git fetch origin
git pull --ff-only origin main
git rev-parse HEAD
```

The repository baseline is `main`. If local commits prevent a fast-forward, preserve anything you need before resetting. Only when you intentionally want to discard local changes:

```bat
git fetch origin
git checkout main
git reset --hard origin/main
```

## 2. Required tools

Matter Overdrive currently targets:

- Minecraft 1.20.1
- Forge 47.4.10
- Java 17
- Mod ID `matteroverdrive`
- Release line `0.6`

Verify Java:

```bat
java -version
```

For the PDA voice production workflow also verify Python and FFmpeg:

```bat
python --version
ffmpeg -version
```

`PROCESS_PDA_VOICE_BANK.bat` requires FFmpeg on `PATH`.

## 3. Run static validation

Recommended current validation pass:

```bat
VALIDATE_PDA_TECHNOLOGY_LORE.bat
VALIDATE_ADVANCEMENT_DIALOGUE_QOL.bat
VALIDATE_PDA_PRESENTATION.bat
VALIDATE_PDA_POPULATION_ANDROIDS.bat
VALIDATE_STRUCTURE_TOPOLOGY.bat
VALIDATE_STRUCTURE_LORE.bat
VALIDATE_PDA_GUIDEME_INTEGRATION.bat
```

These validators catch project-specific contract failures but do not replace a real Forge compile or runtime test.

## 4. Compile and build

First compile Java:

```bat
gradlew.bat compileJava --no-daemon
```

Then build:

```bat
gradlew.bat build --no-daemon
```

The build runs the bundled-documentation consistency gate automatically. The normal output JAR is expected under:

```text
build\libs\matteroverdrive-0.6.jar
```

## 5. Run the mod without prerecorded voices

Prerecorded PDA audio is optional. The mod remains usable if the neural voice bank is incomplete.

The current fallback chain is:

```text
config override WAV
-> bundled prerecorded WAV
-> local OS speech synthesis
-> Minecraft Narrator
-> on-screen caption/text
```

On Windows, local OS speech uses `System.Speech.Synthesis.SpeechSynthesizer`. Missing prerecorded lines therefore remain audible where Windows speech is available and never hide gameplay information.

## 6. Export the complete PDA voice queue

Run:

```bat
EXPORT_PDA_VOICE_QUEUE.bat
```

This writes:

```text
tools\pda_voicebank\voice_generation_queue.json
```

The exporter combines:

- the core/system/campaign bank;
- facility-specific discovery lines;
- optional physical-lore interpretation lines;
- every current `tech_*` first-discovery line from `TechnologyLoreCatalog`.

Use this generated JSON as the authoritative production list. Do not maintain a separate handwritten list of voice IDs.

## 7. Generate the dry neural performances

For each queue entry that does not already have an approved master, generate or record a natural performance and name the source exactly after its voice ID, for example:

```text
field_link.mp3
anomaly_warning.mp3
site_icarus.mp3
lore_bastion_kade.mp3
tech_decomposer.mp3
tech_replicator.mp3
tech_gravitational_anomaly.mp3
```

Place the source files in:

```text
tools\pda_voicebank\raw\
```

Accepted source formats include MP3, WAV, OGG, FLAC and M4A.

### Voice direction

The approved PDA direction is:

- calm, highly intelligent synthetic field-computer VA;
- feminine-androgynous presentation;
- neutral/subtle British English;
- human timing and natural pronunciation;
- restrained emotion;
- close and clear delivery;
- no exaggerated robot performance in the dry recording.

The synthetic character is added afterward by the Matter Overdrive post-processing chain.

Important proper-name pronunciations to check by ear include ICARUS, ORPHEUS, JANUS, LAGRANGE, ECHO-9, HEPHAESTUS, MORROW, M-zero, DUSTWELL, MNEMOSYNE, KESTREL, NEREID, Halcyon-7 and Bastion.

## 8. Preserve the approved ICARUS master

The approved `icarus_warning` sample is already synthetically processed. Do not run it through the synthetic processor a second time.

Convert the approved processed MP3 directly to the runtime WAV format if needed:

```bat
ffmpeg -y -i MatterOverdrive_PDA_ICARUS_synthetic_VA.mp3 -ar 48000 -ac 1 -c:a pcm_s16le src\main\resources\assets\matteroverdrive\pda_voice\icarus_warning.wav
```

Use it as the mix reference for the rest of the bank.

## 9. Process the remaining voice bank

Once dry source performances are present under `tools\pda_voicebank\raw\`, run:

```bat
PROCESS_PDA_VOICE_BANK.bat
```

The processor creates 48 kHz mono PCM WAV files using the approved synthetic treatment:

- natural dry performance remains dominant;
- subtle opposing micro-pitch ghost layers;
- millisecond timing displacement;
- restrained communications EQ/compression;
- small digital reflection;
- Matter Overdrive PDA identification chime.

Processed files are written under:

```text
src\main\resources\assets\matteroverdrive\pda_voice\
```

## 10. Test voices without rebuilding the JAR

For rapid iteration, copy any processed WAV into the Minecraft instance override folder:

```text
config\matteroverdrive\pda_voice\
```

For example:

```text
config\matteroverdrive\pda_voice\icarus_warning.wav
config\matteroverdrive\pda_voice\tech_decomposer.wav
config\matteroverdrive\pda_voice\site_icarus.wav
```

The config folder is checked before the bundled JAR resource, so it is the preferred way to audition replacements without rebuilding.

## 11. Test first-discovery narration correctly

Technology discoveries are persistent per player/world. A technology can be indexed through:

- crafting;
- item pickup;
- placing the block;
- container/machine/command/Creative acquisition detected by the periodic inventory audit;
- Data Pad scanning of an intact Matter Overdrive block.

Once a canonical technology family is indexed, later copies should not repeat the first-discovery voice line.

For clean testing, use a fresh temporary world or a fresh player identity.

## 12. Bundle voices during a normal build

If the processed WAV files are already under:

```text
src\main\resources\assets\matteroverdrive\pda_voice\
```

run:

```bat
gradlew.bat clean build --no-daemon
```

Gradle packages them into the normal Matter Overdrive JAR.

## 13. Or inject voices after the build

If you prefer to keep binary audio out of the source tree, first build the normal JAR and then run:

```bat
INJECT_PDA_VOICES_INTO_JAR.bat build\libs\matteroverdrive-0.6.jar
```

The injector reads processed WAV files from the voice-bank resource directory and creates a separate voiced copy:

```text
build\libs\matteroverdrive-0.6-pda-voice.jar
```

The original JAR is left untouched.

Do not install both the original JAR and `-pda-voice.jar` in the same `mods` directory because both contain the same mod ID.

## 14. Install the finished build

A typical voiced installation is:

```text
.minecraft\
  mods\
    matteroverdrive-0.6-pda-voice.jar
  config\
    matteroverdrive\
      pda_voice\
        optional local overrides...
```

If the audio was bundled during Gradle build instead, use the normal `matteroverdrive-0.6.jar`.

## 15. Recommended first runtime smoke test

Use a fresh world and verify the following sequence:

1. first-world PDA briefing appears;
2. PDA/Data Pad opens correctly;
3. INCIDENT, FIELD LOGS and TECHNOLOGY archives are visible;
4. acquiring a simple technology such as a Solar Panel or Decomposer produces one first-discovery notification/voice line;
5. acquiring another copy remains silent;
6. entering a fresh Matter Overdrive facility produces its discovery banner and queued site-specific narration;
7. applicable structures display environmental hazard warnings;
8. recovering a physical lore fragment adds it to FIELD LOGS and plays its interpretation line;
9. branching NPC dialogue accepts real server-authored choices and remembered topics cannot farm trust repeatedly;
10. anomaly/ICARUS warnings queue rather than talking over another PDA line.

## 16. Long-term voice-production workflow

Whenever new PDA lore or technology is added:

```text
source changes
-> EXPORT_PDA_VOICE_QUEUE.bat
-> neural VA / recorded dry masters
-> tools\pda_voicebank\raw\
-> PROCESS_PDA_VOICE_BANK.bat
-> config override testing
-> final Gradle bundle or JAR injection
```

This keeps gameplay code, voice IDs and local binary production separate. Binary neural-voice assets do not need to be hosted in GitHub for the complete voiced mod to work.
