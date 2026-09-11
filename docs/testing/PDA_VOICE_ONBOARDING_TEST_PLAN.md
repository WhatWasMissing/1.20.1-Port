# PDA Voice / Onboarding / Title / NPC Dialogue Runtime Test Plan

Branch target: `feature/lead-dev-expansion-2026-09-11`

## 1. Static gate

Run:

```bat
VALIDATE_PDA_VOICE_ONBOARDING.bat
```

Expected: `PDA VOICE / ONBOARDING VALIDATION PASSED`.

Then:

```bat
gradlew.bat compileJava --no-daemon
gradlew.bat build --no-daemon
```

## 2. Minecraft title screen

1. Launch the client.
2. Confirm the normal Minecraft title screen still renders normally.
3. Confirm a `MATTER OVERDRIVE` button appears at the upper-right.
4. Resize the game window and revisit the title screen.
5. Open the Matter Overdrive screen.
6. Verify all three pages render without clipping at GUI scales 2, 3 and Auto.
7. Confirm PREV / NEXT / RETURN work.
8. Confirm the normal Minecraft menus remain usable after returning.

Blocking failures: vanilla title controls removed, buttons overlap at normal 16:9 sizes, screen traps the player, resize crashes, or another title-screen replacement is forced.

## 3. First login to a new world

1. Create a fresh world.
2. Enter the world and wait roughly two seconds.
3. Confirm the three-page `FIELD LINK` briefing opens.
4. Confirm its first page explains exploration and the absence of Star Map progression.
5. Confirm page two explains the Data Pad / technical manual split and major systems.
6. Confirm page three explains the sixteen-facility evidence campaign and current NPC populations.
7. Close with SKIP and repeat in another new world.
8. Confirm the new world shows its own briefing.
9. Return to the first world.
10. Confirm the briefing does NOT automatically appear again.

Multiplayer: two different players joining the same server save should each receive the briefing once.

## 4. PDA short voice callouts

### Narrator enabled

Set Minecraft Narrator to a mode that is active.

Verify:

- first-world briefing produces the `field_link` line;
- first PDA open in a client session produces `database_ready`;
- normal facility recovery can produce `record_recovered`;
- KESTREL / MNEMOSYNE / DUSTWELL class sites produce the M-0 resonance advisory;
- NEREID / JANUS class sites produce the gravitational advisory;
- HELIX / Bastion / ORPHEUS class sites produce the ORPHEUS-security advisory;
- ICARUS produces its shutdown-chain warning;
- MORROW / HEPHAESTUS sites produce the independent-synthetic contact advisory;
- a newly authenticated reconstruction produces `reconstruction_complete` rather than a second generic line;
- archive completion produces the `closed_loop` standing instruction.

Every voice line must also display a visible `[PDA]` caption.

### Narrator disabled

Repeat at least one discovery with Narrator Off.

Expected:

- no crash;
- no spoken audio;
- the `[PDA]` caption is still shown;
- the player does not lose mechanical or story information.

## 5. New NPC dialogue boxes

Test at least one of every role.

Human field team:

- Field Researcher
- Salvage Specialist
- Recovery Specialist
- Reactor Recovery Engineer
- Anomaly Field Medic
- Incident Archivist

Synthetic survivors:

- MORROW Scout
- Chorus Courier
- HEPHAESTUS Liaison

For each:

1. interact normally;
2. confirm the dialogue opens once per interaction;
3. confirm the speaker name is correct;
4. confirm human roles show `FIELD COMMUNICATION`;
5. confirm synthetic roles show `SYNTHETIC CONTACT`;
6. confirm speaker/subject text do not overlap;
7. confirm long lines wrap rather than clip;
8. confirm Back / Continue / Close work;
9. confirm `READ ALOUD` works with Narrator enabled;
10. confirm `STOP VOICE` stops the utterance;
11. confirm closing the dialogue stops narration;
12. test GUI scales 2, 3 and Auto.

## 6. Legacy dialogue regression

Interact with the existing Mad Scientist / story NPC paths that use `ModNetwork.openDialogue`.

Expected: they use the same redesigned box and retain all text. No legacy dialogue should disappear because the screen was generalized for new NPCs.

## 7. Campaign precedence

Find a facility whose recovered record completes an Incident reconstruction.

Expected:

1. normal recovered-record chat is shown;
2. reconstruction-complete chat is shown;
3. the spoken/captioned PDA callout is the reconstruction message, not a generic record acknowledgement.

On the sixteenth archive record, the Closed Loop warning should be the final PDA callout.

## 8. Safety / worldgen regression

While testing the presentation pass:

- create multiple new worlds;
- `/locate` several Matter Overdrive structures;
- travel between them normally;
- cross chunk borders near structures;
- relog inside a structure.

There must be no new chunk-force-loading, world-load hang, structure-generation stall or population duplication caused by presentation/onboarding code.

## Acceptance gate

Pass when title integration, first-world persistence, contextual captions/voice, all nine new NPC dialogue roles, legacy dialogue regression and fresh-world traversal all succeed locally.
