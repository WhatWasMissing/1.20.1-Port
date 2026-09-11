# PDA Technology Codex — 2026-09-11

## Goal

Give the Personal Data Assistant a persistent, authored technology archive for the major functional Matter Overdrive machines, networks, reactor/anomaly systems, weapons, Android/drone equipment and field tools. The first meaningful encounter with a technology should feel like the PDA has learned something, not merely unlocked a tooltip.

## Discovery model

`TechnologyLoreCatalog` contains one canonical record per major technology or deliberately grouped family. Purely decorative blocks, debug-only objects, raw crafting intermediates and colour-only variants are excluded from narration.

A record contains:

- stable canonical ID;
- display title;
- category;
- concise functional explanation;
- lore/history context tied into the Overdrive Incident world;
- a practical field note;
- a short first-discovery voice line.

Variant aliases intentionally collapse into a single record where separate announcements would be noise. Current grouped families include Tritanium crates, Android replacement body parts, chassis modules, storage-cell tiers, machine upgrades, security-protocol media, weapon modules, Tritanium tools and Tritanium armour.

## First-discovery triggers

`TechnologyLoreEvents` authenticates a record only once per player/world. Discovery can occur through:

1. crafting the item;
2. picking it up;
3. placing the block;
4. periodic inventory audit after container/machine/command/creative acquisition;
5. explicitly scanning an intact Matter Overdrive block with the Data Pad.

The periodic audit runs every 40 player ticks and emits at most one new discovery per audit, preventing mature inventories from filling the serialized PDA notification queue all at once after an update.

`TechnologyLoreSavedData` is owned by the Overworld and persists the canonical record IDs per player, so dimension changes and duplicate copies do not replay discoveries.

## Presentation

On first authentication the player receives:

- `PDA // TECHNOLOGY INDEXED` notification;
- short function summary;
- archive/lore context;
- collection progress;
- the record's `tech_*` queued PDA callout.

The Data Pad now has a dedicated `TECHNOLOGY` tab. It browses only records the player has actually authenticated and displays FUNCTION, ARCHIVE CONTEXT and FIELD NOTE sections. The tab supports the existing PREV/NEXT and READ ALOUD controls.

The PDA Overview also reports technology-codex completion independently from primary Incident records and optional physical field logs.

## Voice behavior

Technology callouts use stable IDs derived as `tech_<canonical_id>`. `PdaVoiceLineCatalog` resolves these dynamically through `TechnologyLoreCatalog`, so a technology does not need to duplicate its text in the core Java voice map.

Playback remains:

1. processed prerecorded WAV if locally/bundled;
2. local OS speech fallback;
3. Minecraft Narrator fallback;
4. caption/text remains available.

`EXPORT_PDA_VOICE_QUEUE.bat` runs `tools/pda_voicebank/export_generation_queue.py`, merging the packaged core/facility/field-log manifest with every live technology callout into `tools/pda_voicebank/voice_generation_queue.json`. This gives local neural-voice production one authoritative queue without storing binary audio in GitHub.

## Design examples

Technology lore is intentionally connected to the world rather than written as a second manual. Examples include:

- Pattern Drives remaining useful because removable media could be physically isolated when network chronology became untrustworthy;
- MORROW treating Android charging infrastructure as continuity/life-support rather than optional equipment;
- the ICARUS microwave/canteen records surviving more reliably than some classified telemetry;
- modern Reactor RUN/SCRAM presentation being explicit because ICARUS demonstrated the danger of hidden remote authority;
- Data Pad doctrine preserving contradictory evidence rather than behaving like an ORPHEUS briefing terminal.

GuideME/the Technical Manual remains the operational reference. The Technology Codex records what the player's PDA has personally encountered and places it in-world context.

## Safety / compatibility

- No network protocol bump is required; first-discovery speech reuses `PdaVoicePacket`.
- No chunk tickets, forced loading or worldgen behavior are introduced.
- Star Map is not part of this feature.
- Missing prerecorded technology audio cannot hide information because local TTS/Narrator/text remain fallbacks.
