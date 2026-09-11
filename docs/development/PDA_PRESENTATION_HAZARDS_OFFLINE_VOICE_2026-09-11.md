# PDA Presentation, Hazards and Offline Voice — 2026-09-11

## Goal

Make Matter Overdrive's exploration layer feel like one coherent science-fiction interface rather than unrelated Minecraft screens and chat messages.

This pass connects facility discovery, environmental risk, NPC communications, PDA callouts and UI audio through one presentation layer while preserving three constraints:

1. no player-facing information may exist only in audio;
2. GuideME remains the technical manual and does not replace the PDA;
3. presentation systems must not force-load chunks or move structure work back into synchronous world generation.

## Animated facility discovery

`FacilityDiscoveryPacket` is sent when the player authenticates a structure family's unique Overdrive Incident record.

`PresentationOverlay` renders a top-centre animated panel containing:

- `FACILITY IDENTIFIED`;
- the canonical facility name;
- archive index;
- security/classification information.

The panel uses a smooth slide/fade in and out and the same cyan/orange visual language as the PDA and Matter Overdrive landing screen.

The banner is intentionally tied to first authentication rather than every re-entry into another copy of the same structure family. Repeated facilities remain useful for loot/encounters without repeatedly pretending the archival discovery is new.

## Environmental hazard HUD

The same overlay owns a persistent top-right environmental panel.

Severity contract:

- 1 — advisory / cyan;
- 2 — warning / orange pulse;
- 3 — critical / red pulse.

Current states include:

- structural damage in crashed/cargo wrecks;
- pressure damage at NEREID;
- M-0 resonance at DUSTWELL/KESTREL/MNEMOSYNE;
- legacy ORPHEUS security at HELIX/Bastion/ORPHEUS;
- acausal telemetry at ECHO-9 and LAGRANGE;
- ICARUS containment risk;
- anomaly-containment risk at JANUS;
- nearby natural gravitational anomalies, even outside authored structures.

Natural anomaly checks use only already-loaded blocks (`level.hasChunkAt`) and are bounded to an eight-block radius. No chunk ticket or force-load API is used.

## Notification queue

`ClientPdaNotificationManager` serializes short-form callouts.

Only one short PDA voice is active at once. Messages are queued and assigned priority; critical Closed Loop and anomaly/ICARUS warnings can move ahead of low-priority archive acknowledgements but do not interrupt a line already speaking.

Long-form player-requested narration has priority over the queue. If the player chooses `READ ALOUD` on a dossier or NPC conversation, queued automatic advisories wait until that manual narration is stopped/closed.

The currently active callout is always captioned in the HUD.

## Contextual PDA voice catalogue

The short-form catalogue is original Matter Overdrive writing. It uses the functional rhythm of a survival-computer assistant without copying Subnautica dialogue or imitating a specific actor.

The current catalogue covers:

- field link / first-world onboarding;
- PDA/database startup;
- generic record authentication;
- reconstruction completion;
- M-0 resonance;
- gravitational/anomaly warnings;
- pressure-compromised habitats;
- structural wrecks;
- acausal ECHO-9/LAGRANGE telemetry;
- ORPHEUS security;
- ICARUS containment;
- non-hostile synthetic contact;
- Closed Loop completion.

## Offline voice generation

The earlier hosted TTS route was intentionally removed from the runtime design.

Short-form PDA callouts now use speech engines already present on the player's operating system:

- Windows: `System.Speech.Synthesis.SpeechSynthesizer` through hidden Windows PowerShell;
- macOS: `say`;
- Linux: `espeak`, then `spd-say` fallback.

No text is uploaded and no TTS account/API key is required.

On Windows the mod selects the first enabled English female System.Speech voice when one exists, otherwise the first enabled English voice. The process receives the line through an environment variable rather than embedding user-controlled text into the PowerShell script.

If the local speech engine cannot be started, the queue preserves the caption and timing and falls back to Minecraft Narrator when Narrator is enabled.

Long-form dossier/NPC `READ ALOUD` remains Minecraft Narrator based because it is player-requested, arbitrary-length content rather than a small authored catalogue.

## Bespoke UI sounds

The presentation layer now has original short cue families:

- PDA/startup;
- record acquired;
- facility discovery;
- environmental hazard;
- communication channel opening.

The cues are synthesized in Java as short layered tone patterns at playback time. This keeps them original, lightweight and independent of binary asset hosting.

## NPC portrait / hologram panels

`HologramPortraitRenderer` adds a procedural portrait panel to the shared `NpcDialogueScreen`.

Human field contacts use cyan holography. MORROW/Chorus/HEPHAESTUS and other synthetic contacts use orange-accented synthetic geometry. The panel includes an animated scanline and role-sensitive contact code.

The portrait collapses automatically on narrow GUIs so dialogue wrapping always wins over decoration.

This remains a presentation layer over the real shared dialogue path:

`NPC -> ModNetwork.openDialogue -> NpcDialoguePacket -> ClientDialogueOpener -> NpcDialogueScreen`

## Network change

Matter Overdrive's simple-channel protocol is now `16` because `FacilityDiscoveryPacket` and `EnvironmentalHazardPacket` were added.

Client and server must therefore use the same updated build.

## Static gate

Run:

```bat
VALIDATE_PDA_PRESENTATION.bat
```

The validator checks the queue contract, offline speech backends, programmatic UI sounds, facility/hazard packets, overlay, portraits, structure/anomaly safety, contextual voice catalogue and absence of chunk-force-loading APIs in the new structure presentation path.
