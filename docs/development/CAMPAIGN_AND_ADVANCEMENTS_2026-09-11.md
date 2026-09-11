# Matter Overdrive Campaign and Advancements — 2026-09-11

Branch: `feature/lead-dev-expansion-2026-09-11`

## Campaign goal

The campaign uses systems that already exist instead of introducing a second disconnected quest framework. Scientist research remains the narrative spine; advancements provide the vanilla-style world/progression layer around it. The intended player loop is:

`discover -> recover -> research -> automate -> specialize -> master fusion/anomalies -> explore Frontier facilities -> breach the Black Site -> complete Overdrive`

Android progression remains an optional parallel character path rather than a mandatory campaign gate.

## Chapter 1 — First Contact

Primary location: Crashed Ship.

Player experience:
- discover a crashed Matter Overdrive vessel;
- salvage early technology and Matter Scanner access;
- meet/seek scientists through the existing campaign;
- learn that the abandoned structures are parts of one larger technological network.

Advancement: `First Contact`.

## Chapter 2 — The Matter Age

Uses:
- Matter Analyzer;
- Decomposer;
- Pattern Drive/Storage;
- Replicator;
- Matter Containers and Pipes.

The player learns the central economy: items have Matter value, patterns preserve knowledge, and Matter can be converted back into useful technology.

Advancement: `The Matter Age`.

## Chapter 3 — Networked Industry

Uses:
- Network Pipe;
- Network Switch;
- Network Router;
- Replicator queues;
- Transporter/local logistics;
- Matter Network Terminal where available.

The campaign changes from individual machines into an infrastructure game.

Advancement: `Networked Industry`.

## Optional specialization — Android path

The Red Pill opens the Android progression system. Android perks, abilities, chassis/body systems and FE management remain a powerful parallel progression route, but a human player can continue the technology campaign.

Advancement: `The Red Pill`.

## Optional specialization — Drone Commander

Uses the configurable Drone Core, Drone Fabricator, Charging Station and Drone Management systems. Combat, Repair, Logistics, Survey and Reactor Maintenance roles support later campaign chapters.

Advancement: `Drone Commander`.

## Chapter 4 — Advanced Power / Fusion

Uses:
- Heavy Energy distribution;
- Fusion Reactor Controller/IO/coils;
- reactor profiles;
- shared ring power;
- stabilizers;
- reactor telemetry and remote control.

The player transitions from ordinary power generation to deliberately operating dangerous high-output infrastructure.

Advancement: `Build the Impossible`.

## Chapter 5 — Anomaly Engineering

Uses:
- Gravitational Anomalies;
- Gravitational Stabilizers;
- Anomaly Containment Unit;
- Space-Time Equalizer;
- anomaly research/Field Operations.

The player stops treating anomalies as hazards and starts treating them as controllable technology.

Advancement: `Anomaly Engineer`.

## Chapter 6 — Frontier Expedition

The player must visit all four major Frontier facilities:
- Deep Matter Vault;
- Autonomous Drone Foundry;
- Anomaly Quarantine Site;
- Orbital Recovery Array.

Existing Frontier discovery SavedData, dossiers, security-clearance objectives and Field Operations remain authoritative gameplay systems. The advancement is an additional vanilla-visible milestone, not a replacement for those rewards.

Advancement: `Frontier Expedition`.

## Chapter 7 — The Black Site

The Black Site is the campaign's deliberate final exploration dungeon. Its layout should feel like a vanilla endgame structure: readable entry, escalating security, side research spaces, a deeper restricted layer, valuable optional rooms, then a safe route out.

Advancement: `The Black Site`.

## Capstone — Matter Overdrive

The player demonstrates mastery by combining:
- Parallel Processing research technology;
- a recovered experimental Artifact;
- Reactor Remote/control technology.

This represents control of research, exploration, anomalies, automation and reactor infrastructure rather than simply crafting one expensive item.

Advancement: `Matter Overdrive`.

## Exploration challenge — World Archivist

A separate challenge tracks visits to all 16 current structure families: six legacy structures, six modern technology facilities and four Frontier Expedition facilities. This is intentionally completionist and not required for the main campaign.

## Advancement implementation

Advancements live in `data/matteroverdrive/advancements/campaign/` and use vanilla triggers:
- `minecraft:tick` for the root tab;
- `minecraft:location` for structure discovery;
- `minecraft:inventory_changed` for technology milestones;
- `minecraft:consume_item` for Android conversion.

This keeps the system data-driven and multiplayer-safe without adding a per-player polling manager.

## Relationship to existing research

The advancement tree does not replace the scientist/research campaign. The two layers have different roles:
- research/contracts provide narrative, staged objectives and rewards;
- advancements show long-term milestones and exploration mastery in vanilla UI;
- Data Pad remains the detailed journal;
- Field Operations remain repeatable endgame activities.

## Structure campaign rule

Every campaign-critical structure must be completable in Adventure-like play without breaking blocks. Structure progression may require combat, interacting with machines, opening containers or choosing side routes, but never digging through a wall because generation failed to connect rooms.
