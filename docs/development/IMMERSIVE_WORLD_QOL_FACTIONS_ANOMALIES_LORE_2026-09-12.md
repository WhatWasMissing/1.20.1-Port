# Immersive World, Factions, Anomalies and QoL Expansion — 2026-09-12

## Scope

This pass deliberately avoids the larger structure-objective/missions overhaul. That work is deferred because it changes critical traversal, rewards and encounter sequencing and therefore deserves its own full runtime QA cycle.

Instead this pass expands eight lower-risk but broadly visible systems:

1. environmental storytelling and non-blocking facility dressing;
2. PDA/general QoL;
3. mastery advancements;
4. context-sensitive PDA voice processing;
5. optional collectible lore;
6. anomaly field gameplay;
7. contemporary NPC contact chains/services;
8. faction/reputation state derived from existing branching dialogue.

The retired Star Map remains excluded.

## Faction reputation

`FactionReputation` derives standings from the existing authoritative `DialogueStateSavedData`; it does not create a second reputation economy.

Current networks:

- Recovery Network;
- MORROW;
- Chorus;
- HEPHAESTUS;
- Archive Council.

Tiers are Distrusted, Unverified, Cooperative, Trusted and Allied. Scores combine Field Team Trust, Synthetic Trust, Archive Insight and durable topic flags already earned through server-validated conversations.

This makes dialogue choices mechanically useful without allowing the client to submit arbitrary reputation changes.

## NPC contact chains and services

Normal interaction with a contemporary NPC still opens the branching conversation graph. Crouch-interaction opens the contact/service channel.

Each faction/network has a three-stage contact chain tied to existing gameplay rather than a new grind currency. Requirements use combinations of:

- Field Operation completions;
- authenticated Field Logs;
- primary Incident records;
- Technology Codex discoveries;
- Field/Synthetic Trust;
- Archive Insight.

At Cooperative standing or better, contacts can assign an appropriate Field Operation when none is active and provide a role-sensitive support package. Services have persistent cooldowns; Trusted/Allied standings reduce the cooldown.

Human contacts provide survey, salvage, recovery, reactor, medical and archive support. MORROW, Chorus and HEPHAESTUS provide synthetic-network support consistent with their established identities.

## Anomaly field phenomena

`AnomalyFieldEvents` expands existing `GravitationalAnomalyBlockEntity` gameplay rather than creating another anomaly generator.

Safety contract:

- player-proximity only;
- checks every 40 player ticks;
- searches only the current chunk and immediately adjacent chunks using `getChunkNow`;
- never creates a chunk ticket;
- maximum event distance is bounded;
- events have randomized cooldowns.

Four signatures can be observed:

1. Gravity Shear;
2. Temporal Drag;
3. Resonance Echo;
4. Matter Static.

The Space-Time Equalizer reduces or removes the harsher effects. Observation state persists on the player. Witnessing all four awards **Anomaly Field Observer**.

## PDA QoL

The Data Pad scan history capacity is now 32.

Normal use still opens the complete PDA. Crouch-use returns a compact field summary containing:

- research status;
- active Field Operation;
- primary archive progress;
- optional Field Log progress;
- Technology Codex progress;
- anomaly-field study progress;
- faction standing;
- contact-chain stage.

The normal journal payload also carries faction/contact/anomaly status so existing PDA presentation can expose the information without a new network protocol.

## Context-sensitive PDA voice profiles

The approved natural neural-VA identity remains the base performance. Synthetic character is still added after acting/pronunciation.

`PdaVoiceProfile` classifies short callouts into:

- STANDARD;
- HAZARD;
- ANOMALY;
- ARCHIVE;
- ORPHEUS;
- SYNTHETIC;
- CORRUPTED.

Recorded playback now prefers a profile-specific WAV, then the ordinary line WAV, then the existing local OS speech/Narrator/caption fallback chain.

`tools/pda_voicebank/process_voice_bank.py` applies profile-sensitive DSP and writes files as `<line_id>.<profile>.wav`. This allows anomaly material to feel unstable, ORPHEUS material colder/classified, synthetic contact slightly machine-like and damaged archival material corrupted while preserving intelligibility.

No cloud TTS service is required at runtime.

## Expanded optional lore

The optional physical archive expands from 32 to **48 records**: three associated with each of the sixteen canonical facilities.

The third record at each site intentionally emphasizes ordinary environmental evidence rather than another executive summary. Examples include:

- DUSTWELL improvised warning paint;
- MNEMOSYNE handwritten inventory labels;
- KESTREL local-only valve tags;
- Atlas civilian evacuation cards;
- HELIX toolboard behaviour around a supposedly blank chassis;
- NEREID dry-route chalk;
- ECHO-9 LOCAL/ORIGIN/ARRIVAL clock stickers;
- Halcyon `NOT AN EXIT` markings;
- Voss' deliberately useless blue mug;
- JANUS bedside memory-verification instructions;
- MORROW household rosters;
- Bastion manual-IFF floor tape;
- HEPHAESTUS human walking corridors;
- ICARUS LOCAL SCRAM paint;
- ORPHEUS KEEP/REWRITE/DESTROY bins;
- LAGRANGE photograph-before-moving quarantine procedure.

Shared ambient/story-cache loot can surface all records. The six modern facility-specific caches now explicitly bias all three records authored for their own site.

Archive mastery now has three useful thresholds:

- **Field Archivist** — 16 records;
- **Field Historian** — 32 records;
- **Every Scrap Matters** — all 48 records.

## Environmental storytelling observations

`EnvironmentalStorytellingCatalog` provides three site-specific observations for every canonical facility. `EnvironmentalStorytellingEvents` occasionally surfaces these as sparse action-bar notes while the player is physically inside a valid Matter Overdrive structure.

The observation system places no blocks and changes no structure state.

## Environmental structure dressing

The user requirement for structure-facing work is explicit: start from known-valid structures, copy their safe generation techniques, and edit those patterns rather than inventing a new generator.

`EnvironmentalDressingPiece` follows the same native `StructurePiece` model as the current valid legacy, modern and Frontier pieces:

- registered serializer;
- stable NBT identity/origin;
- fixed bounding box;
- current-chunk `BoundingBox` clipping;
- no chunk force-loading;
- placement only into existing air;
- solid/non-fluid support required;
- central traversal axes excluded by the stripe helper;
- no doors, stairs, pits, machines, loot, spawners or objectives.

The piece is added **last** to all three current structure families:

- six `LegacyNativeStructure` sites;
- six `TechnologyFacilityStructure` sites;
- four `FrontierSiteStructure` sites.

It is intentionally an overlay. Existing validated rooms, corridors, stairs, traversal-repair pieces and rare archive mezzanines remain the owners of gameplay geometry.

Dressing mirrors the written lore with carpets, lanterns and occasional candles in off-route supported cells. If a candidate cell is occupied, unsupported, fluid-filled or outside the current chunk clip, nothing is placed.

## Mastery advancements

New server-earned advancements introduced by this pass:

- **Anomaly Field Observer** — witness all four anomaly signatures;
- **Coalition Builder** — reach Trusted standing with at least three contact networks;
- **Network of Trust** — complete all five contact chains;
- **Field Veteran** — complete ten Field Operations;
- **Field Historian** — authenticate 32 optional records.

State-driven advancements use `minecraft:impossible` and are awarded by authoritative server state rather than client or inventory spoofing.

## Validation

Primary static gate:

```bat
VALIDATE_IMMERSIVE_WORLD_QOL.bat
```

This runs both the cross-system validator and the dedicated environmental-dressing validator.

The dressing validator asserts:

- all 16 site IDs are handled;
- all three proven structure families add the dressing piece;
- serializer registration exists;
- chunk clipping/open-cell/support checks exist;
- central traversal lane protection exists;
- force-loading APIs are absent;
- machines, loot, entity spawning and objective logic are absent from the dressing piece;
- all six modern facility caches contain their new third local record.

A successful local Forge compile and fresh-world runtime pass remain mandatory before merging/release confidence.

## Deferred intentionally

Not included in this pass:

- structure objective chains;
- mandatory facility puzzles;
- new locked traversal gates;
- major room topology changes;
- new structure-family generators.

Those are deferred to a later structure-objective milestone with dedicated traversal and save/reload testing.
