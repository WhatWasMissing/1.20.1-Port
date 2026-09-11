# Advancements, Branching Dialogue and QoL Expansion — 2026-09-11

## Scope

This pass expands progression and presentation around the existing Matter Overdrive 0.6 systems without restoring the retired Star Map or replacing data-driven systems with GuideME logic.

## Branching NPC dialogue

The six contemporary human field roles and three independent synthetic roles now use a server-authored conversation graph rather than one-way text dumps.

Profiles:
- Field Researcher
- Salvage Specialist
- Recovery Specialist
- Reactor Recovery Engineer
- Anomaly Field Medic
- Incident Archivist
- MORROW Scout
- Chorus Courier
- HEPHAESTUS Liaison

Each profile exposes three context-specific questions. Responses preserve the Overdrive Incident canon and deliberately avoid flattening humans, Androids or ORPHEUS into single moral categories.

### Authority / anti-exploit contract

`DialogueSessionManager` creates a short-lived server session when the player actually interacts with an NPC. A client choice is accepted only when:
- the session is still live;
- the dialogue ID matches the active session;
- the node ID matches the active server node;
- the selected choice exists in the server `DialogueCatalog`.

The client never sends arbitrary response text, rewards or trust values.

### Persistent contact state

`DialogueStateSavedData` lives in the Overworld data store so dimension changes do not reset conversation progress. Per player it persists:
- visit counts by dialogue profile;
- last selected topic per profile;
- durable narrative flags;
- Field Team Trust (-20..20);
- Synthetic Trust (-20..20);
- Archive Insight (0..100).

Returning conversations acknowledge previous contact and higher context levels expose less simplified framing. The values are also synchronized when the PDA opens and shown as a compact `CONTACT LINK` status line.

## Dialogue-earned advancements

Three advancements use `minecraft:impossible` criteria and are awarded by authoritative server state:
- **Field Liaison** — Field Team Trust >= 8.
- **Synthetic Liaison** — Synthetic Trust >= 8.
- **Incident Analyst** — Archive Insight >= 18.

Each first unlock also queues a PDA milestone acknowledgement. This prevents inventory or client spoofing from granting social/progression milestones while keeping the result visible in both vanilla advancements and the PDA presentation layer.

## Expanded advancement tree

Nine additional advancement nodes were added:
- Pattern Architect
- Network Specialist
- Applied Energy Weapons
- Containment Engineer
- Industrialist
- Field Liaison
- Synthetic Liaison
- Incident Analyst
- Full-Spectrum Engineer

The item-driven branches reward actual Matter Overdrive system coverage rather than generic grind. `Full-Spectrum Engineer` is a challenge for owning representative Matter, routing, drone, fusion/containment and weapons infrastructure without implying the player should recreate Project OVERDRIVE's unsafe convergence.

## PDA voice-bank architecture

The canonical short-form bank now contains **16 line IDs**: the original discovery/hazard/archive set plus `field_liaison`, `synthetic_liaison` and `incident_analyst`. The approved production direction is a natural neural VA performance with synthetic post-processing based on the accepted ICARUS sample.

Runtime fallback order is now:
1. processed neural-VA WAV from `assets/matteroverdrive/pda_voice/<line_id>.wav`;
2. user/modpack override from `config/matteroverdrive/pda_voice/<line_id>.wav`;
3. local OS speech synthesis (Windows System.Speech, macOS `say`, Linux `espeak`/`spd-say`);
4. Minecraft Narrator;
5. on-screen caption remains available regardless of audio.

The manifest is `src/main/resources/assets/matteroverdrive/pda_voice/voice_bank_manifest.json` and the reproducible processor is `tools/pda_voicebank/process_voice_bank.py` / `PROCESS_PDA_VOICE_BANK.bat`.

The ICARUS processed sample approved during development is the mix reference: natural VA timing first, restrained synthetic double/ghost layers and communications processing second. The synthetic effect should never damage intelligibility.

## Network compatibility

Matter Overdrive network protocol is now **17** because NPC dialogue packets carry dialogue/node identifiers and choices, and the client sends validated choice requests.

## Compatibility constraints retained

- GuideME remains the technical manual only; it does not replace datapacks, advancements or PDA state.
- Legacy one-way dialogue packets remain supported through compatibility constructors/openers.
- No new worldgen or chunk-force-loading behavior is introduced.
- No Star Map content is restored.
