# PDA Presentation / Hazard / Offline Voice Runtime Test Plan

Branch target: `feature/lead-dev-expansion-2026-09-11`

The static validator is necessary but not sufficient. This test plan verifies client presentation, local speech engines, GUI scaling and structure transitions in an actual Forge 1.20.1 client.

## 1. Build gate

Run:

```bat
VALIDATE_PDA_PRESENTATION.bat
VALIDATE_PDA_VOICE_ONBOARDING.bat
VALIDATE_PDA_POPULATION_ANDROIDS.bat
VALIDATE_STRUCTURE_TOPOLOGY.bat
VALIDATE_STRUCTURE_LORE.bat
VALIDATE_PDA_GUIDEME_INTEGRATION.bat
gradlew.bat compileJava --no-daemon
gradlew.bat build --no-daemon
```

Expected: every validator passes and the mod builds with protocol 16.

## 2. First-world startup

Create a fresh world with a fresh player.

Expected:

- the normal first-world Matter Overdrive briefing opens once;
- the `field_link` PDA caption is queued;
- the startup cue plays;
- a calm local-system voice speaks the field-link line when an offline speech engine is available;
- the visible caption remains sufficient if speech is unavailable;
- relogging to the same world does not repeat the onboarding briefing.

On Windows confirm no visible PowerShell window remains open after the line finishes.

## 3. Voice queue serialization

Trigger multiple short callouts close together, for example by entering a first-time facility that both establishes a hazard and authenticates a record.

Expected:

- one line speaks at a time;
- no two PowerShell/System.Speech processes speak simultaneously;
- captions correspond to the currently speaking line;
- critical/high-priority items move ahead of low-priority waiting items but do not cut off the line already playing;
- `closed_loop` remains the highest priority;
- changing dimensions/relogging clears stale client queue state.

## 4. Manual narration exclusion

While short PDA notifications are waiting, open a recovered dossier or NPC dialogue and press `READ ALOUD`.

Expected:

- player-requested long-form narration takes priority;
- queued automatic callouts wait rather than talking over the dossier/NPC;
- changing page or closing the dialogue stops manual narration;
- queued alerts then resume.

Repeat with Minecraft Narrator disabled. Short-form local OS speech should still work; long-form `READ ALOUD` should show its accessibility notice instead of silently failing.

## 5. Facility discovery banner

Use `/locate structure matteroverdrive:<id>` in a fresh test world and enter an undiscovered structure normally.

Expected:

- top-centre `FACILITY IDENTIFIED` banner slides/fades into view;
- canonical facility name fits without clipping;
- archive index and classification are shown;
- facility cue plays;
- the banner fades away without leaving stale pixels;
- another instance of the same already-authenticated structure family does not pretend to be a new archive discovery.

Test at GUI scales 1, 2, 3, 4 and Auto where available.

## 6. Environmental hazard matrix

Verify at least one location in every category:

| Category | Example | Expected HUD / voice |
| --- | --- | --- |
| Structural damage | Crashed Ship | cyan advisory + structural warning |
| Pressure damage | NEREID | cyan advisory + pressure warning |
| M-0 resonance | KESTREL / DUSTWELL | orange warning + matter-resonance line |
| Legacy security | ORPHEUS / Bastion | orange warning + ORPHEUS security line |
| Acausal telemetry | ECHO-9 / LAGRANGE | orange warning + impossible-signal line |
| ICARUS containment | Fusion Research Complex | red critical panel + ICARUS warning |
| Anomaly containment | JANUS | orange warning + anomaly line |
| Natural anomaly | ordinary world anomaly | red critical gravitational warning |

Expected:

- panel appears in the upper-right without covering the facility banner;
- severity colour/pulse is correct;
- leaving the relevant area clears or changes the warning after the next movement-cell refresh;
- repeated movement inside the same hazard does not spam audio continuously.

## 7. Natural anomaly safety

Approach a naturally generated Gravitational Anomaly outside any Matter Overdrive structure.

Expected:

- warning appears within the bounded detection radius;
- anomaly warning is queued once when the hazard state changes;
- moving away clears it;
- no neighboring chunks are force-loaded;
- world movement/TPS remains normal.

For a stronger runtime check, enable chunk debugging/logging and verify there is no region-ticket growth attributable to the presentation system.

## 8. NPC portrait/hologram matrix

Interact with all contemporary roles if practical:

Human:
- Field Researcher
- Salvage Specialist
- Recovery Specialist
- Reactor Recovery Engineer
- Anomaly Field Medic
- Incident Archivist

Synthetic:
- MORROW Scout
- Chorus Courier
- HEPHAESTUS Liaison

Expected:

- human contacts show cyan hologram portraits;
- synthetic contacts show orange synthetic portraits;
- scanline animates;
- contact code is sensible for role;
- dialogue text never overlaps portrait;
- Back/Continue/Close remain clickable;
- `READ ALOUD / STOP VOICE` works;
- narrow GUI scales suppress the portrait rather than destroying dialogue layout.

Also verify at least one legacy scientist dialogue still opens through the same shared screen.

## 9. Bespoke UI cue audit

Trigger each cue independently:

- title/landing screen or first PDA startup -> startup cue;
- archive/PDA callout -> record cue;
- first facility record -> facility cue;
- orange/red hazard transition -> hazard cue;
- NPC interaction -> communication cue.

Expected:

- cues are short and non-fatiguing;
- cues do not drown out dialogue;
- no missing-sound/resource errors are logged because the sounds are generated in Java rather than resolved from `sounds.json`.

## 10. Windows offline voice test

Primary user platform is Windows 10.

Verify:

- Windows PowerShell can load `System.Speech`;
- an installed English voice is selected;
- speech occurs with no internet connection;
- text is not sent to a web service;
- stopping/closing a queued line terminates the child speech process;
- Minecraft closes normally with no orphaned PowerShell process.

Optional manual diagnostic in Windows PowerShell:

```powershell
Add-Type -AssemblyName System.Speech
(New-Object System.Speech.Synthesis.SpeechSynthesizer).GetInstalledVoices() | ForEach-Object { $_.VoiceInfo.Name }
```

## 11. Narrator/system speech fallback

If possible test a machine/VM where System.Speech or the platform speech command cannot start.

Expected:

- short callout captions still display;
- if Minecraft Narrator is enabled, it becomes the fallback voice;
- if Narrator is disabled too, no information is lost because the caption remains visible.

## 12. Multiplayer

With two players:

- enter different facilities at roughly the same time;
- open NPC dialogue independently;
- trigger a natural anomaly warning for only one player.

Expected:

- queue, captions, banner and hazards are client-local;
- one player's speech process/state does not affect the other player's notifications;
- server sends only the relevant state packet to the relevant player.

## 13. Regression checks

Confirm:

- GuideME still opens from the PDA Technical Manual route when installed;
- Data Pad itself still opens regardless of GuideME;
- Android HUD still renders without being covered by the new environmental panel in ordinary layouts;
- Star Map remains retired;
- structure traversal still requires no mining/block placement;
- no structure-generation hang or forced neighboring chunk load is introduced.
