# PDA Voice, Onboarding and Presentation Pass — 2026-09-11

## Goal

Make Matter Overdrive announce itself as a coherent game experience instead of waiting for the player to discover that a collection of systems exists.

This pass adds a dedicated mod landing screen, a once-per-world field briefing, original short-form PDA voice lines, contextual campaign callouts and a more reliable shared NPC dialogue UI.

## PDA voice identity

The PDA is written as a calm, clinical field computer. It is intentionally understated around impossible events: the more alarming the evidence becomes, the more matter-of-fact the delivery should remain.

The lines are original Matter Overdrive writing. They are not transcriptions or imitations of another game's dialogue.

Every callout has a visible caption. Spoken output currently uses Minecraft's platform narrator when Narrator is enabled, which keeps the feature portable and accessible without making an external TTS service a runtime dependency.

### Callout set

- `field_link` — first-world introduction
- `database_ready` — first PDA activation in a client session
- `record_recovered` — generic recovered archive acknowledgement
- `reconstruction_complete` — evidence-chain completion
- `matter_resonance` — M-0 / Matter-chain facilities
- `anomaly_warning` — anomaly / gravity research sites
- `orpheus_security` — ORPHEUS / Directive-0 sites
- `icarus_warning` — ICARUS containment site
- `synthetic_contact` — MORROW / HEPHAESTUS synthetic contact sites
- `closed_loop` — archive-complete standing instruction

Long-form lore still uses the existing per-page PDA `READ ALOUD` system.

## First-world briefing

`WorldOnboardingSavedData` records the UUIDs that have received the briefing in the current world/server save.

On first login:

1. the normal Matter Overdrive version message is sent;
2. the client waits 40 ticks so the joining-world transition cannot overwrite the screen;
3. the server marks the player briefed;
4. `WelcomeBriefingPacket` opens `FirstLoginBriefingScreen`;
5. the `field_link` PDA callout is captioned and voiced if narration is enabled.

The briefing is three pages:

1. field-link and scope;
2. field kit and major systems;
3. Overdrive Incident exploration loop.

It is deliberately once per player per world, not once per installation. A new world therefore receives its own introduction, while normal relogs do not become annoying.

## Title-screen integration

The vanilla Minecraft title screen is not wholesale replaced. `TitleScreenEvents` adds a compact `MATTER OVERDRIVE` button at the upper-right, which is substantially safer for compatibility with menu/resource-pack mods.

The button opens `MatterOverdriveTitleScreen`, a three-page landing surface covering:

- project identity and status;
- major systems;
- exploration / NPC / campaign loop.

The retired Star Map remains explicitly outside progression.

## NPC dialogue reliability

`NpcDialogueScreen` is now the shared communication panel for legacy scientists, contemporary researchers and synthetic survivors.

Changes:

- responsive height based on current GUI scale;
- text width clamped for large and small windows;
- automatic pagination;
- page counter;
- separate human `FIELD COMMUNICATION` and synthetic `SYNTHETIC CONTACT` treatment;
- speaker and subject are independently fitted rather than allowed to overlap;
- `READ ALOUD` / `STOP VOICE` controls;
- narration stops on page navigation and screen close;
- empty dialogue degrades to an ellipsis instead of rendering a broken box.

Both `FacilityResearcherEntity` and `DefectorAndroidEntity` already route through `ModNetwork.openDialogue`, so the UI improvement applies to all nine new roles without one-off screens.

## Campaign callout routing

Structure discovery remains server-authoritative. `StructureLoreEvents` chooses a contextual callout by facility identity. If a discovery simultaneously completes an evidence reconstruction, the reconstruction callout takes precedence over the generic facility acknowledgement. Archive completion then takes final precedence with `closed_loop`.

No callout scans or loads chunks. It piggybacks only on already-successful structure discovery.

## Validation

Run:

```bat
VALIDATE_PDA_VOICE_ONBOARDING.bat
```

Then run the normal compile/build locally:

```bat
gradlew.bat compileJava --no-daemon
gradlew.bat build --no-daemon
```

Runtime coverage is in `docs/testing/PDA_VOICE_ONBOARDING_TEST_PLAN.md`.
