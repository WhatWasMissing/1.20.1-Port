# Immersive World / QoL Runtime Test Plan

Branch under test: `feature/immersive-world-qol-2026-09-12`

This pass deliberately does **not** add full facility objective chains. Test the systems below independently and especially verify that environmental dressing does not disturb the already-valid structure routes.

## 1. Static preflight

Run from the repository root:

```bat
VALIDATE_IMMERSIVE_WORLD_QOL.bat
VALIDATE_ADVANCEMENT_DIALOGUE_QOL.bat
VALIDATE_STRUCTURE_TOPOLOGY.bat
VALIDATE_STRUCTURE_LORE.bat
VALIDATE_PDA_PRESENTATION.bat
```

Then:

```bat
gradlew.bat compileJava --no-daemon
gradlew.bat build --no-daemon
```

Do not continue runtime testing on a build with Java/JSON/datapack errors.

## 2. Fresh-world structure dressing

Use a fresh world or unexplored chunks. Existing generated structures will not retroactively receive the new StructurePiece.

For each of the 16 canonical facilities:

- [ ] structure generates without a hang or chunk-generation stall;
- [ ] save/reload beside the structure succeeds;
- [ ] normal entrance is unchanged;
- [ ] critical route remains traversable both directions;
- [ ] stairs/ladders/doors are not obstructed;
- [ ] no carpet/lantern/candle occupies the central route;
- [ ] no dressing appears unsupported or floating over fluids;
- [ ] no dressing replaces a machine, cache, spawner or structural block;
- [ ] dressing is visibly site-specific where candidate cells are valid;
- [ ] structure occupants/caches still behave as before.

Expected dressing themes:

- DUSTWELL — warning markers;
- MNEMOSYNE — archive/manual inventory marks;
- KESTREL — local-control service route;
- Atlas — evacuation route;
- HELIX — chassis-bay workstation marks;
- NEREID — evacuation markings;
- ECHO-9 — clock/relay stations;
- Halcyon-7 — safe-return markings;
- Voss residence — domestic/workbench detail;
- JANUS — quarantine marks;
- MORROW — mixed utility marks;
- Bastion — manual IFF route;
- HEPHAESTUS — human walking corridor;
- ICARUS — local SCRAM markings;
- ORPHEUS — records-process markings;
- LAGRANGE — evidence quarantine marks.

## 3. Environmental observations

- [ ] Enter each structure and remain inside long enough to receive a `FIELD OBSERVATION` action-bar message.
- [ ] Observation language matches the current site.
- [ ] Messages do not fire continuously every tick.
- [ ] Leaving/re-entering permits a later observation.
- [ ] Multiplayer players do not share the temporary observation timer incorrectly.

## 4. Faction standings

Using the normal branching-dialogue interaction:

- [ ] dialogue choices still work exactly as before;
- [ ] repeated use of the same topic does not farm trust/insight;
- [ ] Recovery Network standing reacts to relevant human-field choices;
- [ ] MORROW reacts to MORROW/truce choices;
- [ ] Chorus reacts to Chorus/JANUS/archive context;
- [ ] HEPHAESTUS reacts to cooperation/safety-context choices;
- [ ] Archive Council reacts mainly to Archive Insight/corroboration choices;
- [ ] standings survive relog and dimension travel.

## 5. Contact service channel

Crouch-interact with the six human roles and three synthetic roles.

- [ ] below Cooperative: service is refused and normal dialogue remains available when not crouching;
- [ ] Cooperative+: contact reports current standing and chain stage;
- [ ] if no Field Operation is active, an appropriate doctrine assignment can be offered;
- [ ] existing active operation is not silently overwritten;
- [ ] support package applies the expected temporary effects;
- [ ] immediate repeated use respects cooldown;
- [ ] Trusted/Allied standing shortens service cooldown;
- [ ] cooldown survives save/reload;
- [ ] contact-chain stage advances only when its real gameplay requirement is satisfied;
- [ ] no interaction grants unlimited XP by repeatedly clicking the same NPC.

## 6. Contact chains

Verify three stages for each network:

- [ ] Recovery Network uses Field Operations / Field Trust;
- [ ] MORROW uses optional lore / Synthetic Trust;
- [ ] Chorus uses primary records / Archive Insight;
- [ ] HEPHAESTUS uses Technology Codex / Synthetic Trust;
- [ ] Archive Council uses field logs + primary records / Archive Insight.

Complete all five chains and verify **Network of Trust**.

## 7. PDA QoL

- [ ] normal Data Pad use opens the full PDA;
- [ ] crouch-use prints the compact Field Summary instead;
- [ ] summary shows current research;
- [ ] summary shows active Field Operation;
- [ ] summary shows 16 primary-record progress;
- [ ] summary shows optional lore out of 48;
- [ ] summary shows Technology Codex progress;
- [ ] summary shows anomaly signatures/events;
- [ ] summary shows all five faction standings and chain stages;
- [ ] Data Pad scan history accepts up to 32 entries without corrupting the item.

## 8. Anomaly field phenomena

Locate a natural or otherwise valid `GravitationalAnomalyBlockEntity`.

Without Space-Time Equalizer:

- [ ] Gravity Shear can occur;
- [ ] Temporal Drag can occur;
- [ ] Resonance Echo can occur;
- [ ] Matter Static can occur;
- [ ] event frequency feels intermittent rather than a constant potion aura;
- [ ] leaving the loaded/local anomaly area stops events;
- [ ] no distant chunks become force-loaded while standing near the anomaly.

With Space-Time Equalizer equipped:

- [ ] harsh effects are reduced/removed as documented;
- [ ] PDA reports mitigation;
- [ ] signatures still count as observations.

After observing all four:

- [ ] **Anomaly Field Observer** advancement unlocks once.

## 9. PDA context-sensitive audio

For prerecorded tests place appropriately named files under:

```text
config/matteroverdrive/pda_voice/
```

Examples:

```text
anomaly_warning.anomaly.wav
orpheus_security.orpheus.wav
synthetic_contact.synthetic.wav
lore_echo9_fragment.corrupted.wav
record_recovered.archive.wav
```

Verify:

- [ ] profiled WAV is preferred over the ordinary WAV;
- [ ] removing profiled WAV falls back to ordinary WAV;
- [ ] removing both falls back to OS TTS/Narrator/caption;
- [ ] anomaly processing remains intelligible;
- [ ] ORPHEUS processing feels classified/clinical rather than distorted beyond comprehension;
- [ ] synthetic processing preserves the established VA identity;
- [ ] corrupted archive effects do not swallow key words;
- [ ] automatic notification queue still prevents speech overlap.

Run `PROCESS_PDA_VOICE_BANK.bat` on dry neural sources and verify it emits profile-suffixed WAV files.

## 10. Expanded 48-record Field Logs

Each canonical facility now has three optional records.

- [ ] shared ambient/story-cache loot can produce the expanded records;
- [ ] each of the six modern facility-specific caches can produce its third local record;
- [ ] first authentication awards +15 XP and voice interpretation;
- [ ] duplicate copies remain readable but award no repeat XP;
- [ ] PDA FIELD LOGS counter reports `/48`;
- [ ] **Field Archivist** unlocks at 16;
- [ ] **Field Historian** unlocks at 32;
- [ ] **Every Scrap Matters** unlocks at 48;
- [ ] save/reload retains all authenticated IDs.

## 11. Mastery advancements

- [ ] Coalition Builder — at least three faction networks reach Trusted;
- [ ] Network of Trust — all five contact chains complete;
- [ ] Field Veteran — ten Field Operations complete;
- [ ] Anomaly Field Observer — all four anomaly signatures observed;
- [ ] Field Historian — 32 optional records authenticated.

Each should award once from authoritative server state.

## 12. Regression / safety

- [ ] no Star Map content returns;
- [ ] no new chunk tickets/forced chunks appear;
- [ ] structure generation remains chunk-safe;
- [ ] no new structure objective or mandatory puzzle has appeared in this pass;
- [ ] existing critical paths remain unchanged;
- [ ] GuideME remains technical documentation only;
- [ ] PDA remains player discovery/progression/lore authority;
- [ ] dialogue remains server-authoritative;
- [ ] existing 16 primary Incident records and five reconstructions still function;
- [ ] existing Technology Codex first-discovery callouts still function.

Record failures with structure/site, coordinates, layout where known, screenshots and `latest.log`/`debug.log` excerpts.
