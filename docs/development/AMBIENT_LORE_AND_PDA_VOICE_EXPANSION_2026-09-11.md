# Ambient Lore and PDA Voice Expansion — 2026-09-11

## Goal

Make the Overdrive Incident feel present in the world outside the sixteen major structure dossiers. Facilities should contain mundane, technical and personal evidence that players can physically find, keep, authenticate and revisit without turning every optional note into a mandatory campaign objective.

## Two-layer archive

The existing sixteen `StructureLoreCatalog` records remain the authoritative reconstruction spine. They are still awarded by first-time facility discovery and still control the five major Incident reconstructions.

A second optional layer now exists in `AmbientLoreCatalog`: **32 physical field records**, two associated with each canonical facility. These records do not gate the main story. They add worker testimony, maintenance evidence, clinical notes, security workarounds, personal messages and synthetic memory fragments that make the structures feel inhabited rather than built only around exposition.

The optional catalogue covers:

- DUSTWELL excavation;
- MNEMOSYNE deep Matter vault;
- KESTREL refinery;
- Atlas Freight 12;
- HELIX manufacturing;
- NEREID underwater base;
- ECHO-9 quantum relay;
- Halcyon-7 wreck;
- Voss research residence;
- JANUS quarantine site;
- MORROW safehouse;
- Bastion command bunker;
- HEPHAESTUS drone foundry;
- ICARUS fusion complex;
- ORPHEUS Black Site;
- LAGRANGE orbital recovery array.

## Physical record item

`matteroverdrive:recovered_lore_fragment` carries one stable `MatterOverdriveLoreId` NBT value. Its displayed name, tooltip, recovered text and PDA interpretation come from the common catalogue rather than duplicating prose in loot tables.

Right-clicking a valid fragment:

1. displays classification, source, recovered excerpt and PDA analysis;
2. authenticates the record into `AmbientLoreSavedData` the first time;
3. awards 15 XP only on first authentication;
4. queues the record's dedicated `lore_*` PDA voice line;
5. retains the physical item so collectors can keep or display the evidence;
6. treats later copies as readable duplicates with no repeat XP/progression.

The SavedData lives in the Overworld store, so dimension travel cannot reset the collection.

## Optional-lore advancements

Two server-earned advancements use `minecraft:impossible` criteria:

- **Field Archivist** — authenticate 16 optional records;
- **Every Scrap Matters** — authenticate all 32 optional records.

The item awards these only from authoritative SavedData counts.

## Loot distribution

`chests/facilities/ambient_lore.json` contains the complete 32-record pool.

Shared `salvage.json` has a low 22% optional lore roll. This lets legacy and Frontier structures using the common salvage table surface displaced documents without making paperwork dominate ordinary loot.

`story_cache.json` strongly favors the ambient-lore pool as one of its document results.

The six modern facility loot tables retain their normal material/research rewards and add a local record roll biased toward the two records authored for that facility:

- HELIX manufacturing — 55%;
- KESTREL refinery — 55%;
- ECHO-9 relay — 55%;
- Bastion bunker — 55%;
- ICARUS complex — 55%;
- ORPHEUS Black Site — 60%.

This creates two useful discovery patterns: local evidence feels intentionally placed, while generic salvage can still reveal paperwork that was moved, copied or abandoned elsewhere during the Incident.

## PDA presentation

The Data Pad now has a dedicated **FIELD LOGS** tab. Recovered optional records are encoded into the existing server-authored journal payload as `@lore:<id>` references, so this UI expansion does not require another network protocol revision.

The page shows:

- collection progress out of 32;
- record title;
- classification;
- associated site;
- physical source/location;
- complete recovered excerpt;
- PDA forensic analysis;
- PREV/NEXT navigation;
- READ ALOUD / STOP narration controls.

The Overview also shows optional-record completion separately from the sixteen primary archives, making it clear that these records enrich rather than gate reconstruction.

## PDA voice expansion

`PdaVoiceLineCatalog` and `voice_bank_manifest.json` now contain **64 authored callouts**:

- 16 core/system/campaign lines;
- 16 facility-specific `site_*` discovery lines;
- 32 physical-record `lore_*` interpretation lines.

Facility discoveries no longer all collapse into generic categories. Immediate hazard warnings still play through the hazard system, while the subsequent site line explains the historical significance of that specific location.

Physical-record voice lines deliberately summarize the implication rather than reading the complete document. This keeps exploration moving while the full prose remains available in the item and FIELD LOGS page.

## Audio production

The runtime continues to prefer:

`processed neural VA WAV -> local OS TTS -> Minecraft Narrator -> caption`

Binary voice files do not need to live in GitHub. The 64-line manifest is the generation contract for the local voice-bank workflow. Raw performances are named by line ID and processed through the approved ICARUS synthetic-VA preset with `PROCESS_PDA_VOICE_BANK.bat`.

## Safety / compatibility

This pass does not add structure stamping, chunk tickets, chunk force-loading or synchronous world scans. Lore comes from existing chest loot and player item interaction. The sixteen structure discovery bits remain unchanged for save compatibility. The retired Star Map is not restored.
