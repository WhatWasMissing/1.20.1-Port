# PDA + GuideME Integration — 2026-09-11

## Goal

Matter Overdrive now treats the PDA and GuideME as complementary systems rather than mutually exclusive front ends.

- **PDA:** player-specific gameplay state, discoveries, Overdrive Incident lore, facilities, research, Field Operations, contracts and scan history.
- **GuideME:** optional full technical/manual experience for machine setup, reference documentation and long-form guides.
- **Bundled DocumentationScreen:** fallback technical manual when GuideME is not installed or cannot open.

Installing GuideME must never suppress the PDA or make player-specific systems inaccessible.

## Root cause repaired

`ClientDataPadOpener` previously attempted to open GuideME first and returned before creating `DataPadScreen`. As a result, a GuideME installation effectively replaced the Data Pad UI.

The opener now **always** creates the PDA. GuideME is only invoked by an explicit Technical Manual action.

## PDA architecture

Opening the Data Pad on the server still builds the current research/Field Operations/scan journal. `ModNetwork.openDataPad` additionally reads the player's persistent Overdrive Incident mask from `StructureLoreSavedData` and sends it in `DataPadOpenPacket`.

The client receives only the immutable 16-bit mask plus journal text. It does not read server `SavedData` directly.

The PDA exposes these first-class sections:

- **Overview** — archive reconstruction and current field-journal summary.
- **Data Bank** — canonical 1–16 Overdrive Incident records with locked/unlocked presentation.
- **Facilities** — discovered facility index without revealing undiscovered facility names.
- **Research** — existing research campaign and progression data.
- **Operations** — existing Field Operations status.
- **Contracts** — current carried contracts, including the existing two-click abandon control.
- **Scan Log** — persistent Data Pad block-scan history.
- **Technical Manual** — explicit external/manual bridge.

The UI uses an original Matter Overdrive dark holographic/cyan field-computer presentation. It takes inspiration from diegetic PDA interfaces but does not reproduce another game's assets or exact UI.

## Non-linear story handling

Minecraft does not guarantee structure discovery order. The lore system therefore distinguishes:

- `ArchiveIndex`: fixed canonical/historical position, 1–16.
- archive reconstruction count: how many records this player has recovered.

Finding ORPHEUS before DUSTWELL is valid. ORPHEUS appears as Archive Entry 12 while reconstruction may still be 1/16.

Locked entries reveal only their archive slot/chapter context. Recovered records reveal facility, title, author, summary and cross-reference.

## Shared lore catalog

`StructureLoreCatalog` is now the single source of truth for all 16 records. Both `StructureLoreEvents` and `DataPadScreen` consume it.

Every catalog record contains:

- structure id and structure key;
- bit position;
- canonical archive index;
- narrative chapter;
- facility display name;
- record title;
- author/source;
- summary;
- cross-reference.

Physical recovered dossiers now also store `ArchiveIndex`, `LoreChapter` and `LoreFacility` in addition to the existing Overdrive Incident metadata.

## GuideME accessibility

GuideME remains fully accessible when installed.

`ClientDocumentationOpener` treats the System Guide as the technical-manual route:

1. attempt to open the registered Matter Overdrive GuideME guide;
2. if GuideME is absent or cannot open, fall back to `DocumentationScreen` document 2.

This route is used by the PDA's **TECH MANUAL** button and by the existing standalone System Guide documentation item.

The GuideME bridge remains reflective (`GuideMeCompatEvents`) so Matter Overdrive does not gain a hard runtime class-loading dependency when GuideME is absent.

## Network/persistence

`DataPadOpenPacket` now carries the 16-bit lore mask and the network protocol is bumped to 14.

The PDA reads lore persistence from the server Overworld data store so the player's archive remains visible after travelling to another dimension.

## Regression contract

`validate_pda_guideme_integration.py` / `VALIDATE_PDA_GUIDEME_INTEGRATION.bat` enforce:

- the PDA opener cannot reference GuideME;
- the lore-aware PDA screen must be opened directly;
- all 16 canonical records remain present;
- server SavedData mask is synchronized in `DataPadOpenPacket`;
- structure discovery and the PDA share the catalog;
- PDA gameplay sections remain present;
- the Technical Manual bridge remains present;
- GuideME registration/opening remains optional and available;
- the standalone System Guide item remains registered;
- the retired Star Map is not reintroduced into the PDA integration.
