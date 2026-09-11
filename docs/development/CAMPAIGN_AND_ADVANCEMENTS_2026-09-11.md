# Matter Overdrive Campaign and Advancements — 2026-09-11

Branch: `feature/lead-dev-expansion-2026-09-11`

## Campaign goal

The campaign uses systems that already exist instead of introducing a second disconnected quest framework. Scientist/recovery research remains the narrative spine; advancements provide the vanilla-style world/progression layer around it. The intended player loop is:

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

Uses Matter Analyzer, Decomposer, Pattern Drive/Storage, Replicator and Matter Containers/Pipes. The player learns the central economy: items have Matter value, patterns preserve knowledge, and Matter can be converted back into useful technology.

Advancement: `The Matter Age`.

New mastery branch: **Pattern Architect** requires the working Pattern Storage + Pattern Monitor + Replicator workflow rather than merely reaching the chapter.

## Chapter 3 — Networked Industry

Uses Network Pipe, Network Switch, Network Router, Replicator queues, Transporter/local logistics and related routing systems. The campaign changes from individual machines into an infrastructure game.

Advancement: `Networked Industry`.

New specialization branches:
- **Network Specialist** — pipe, router, switch, pylon and Network Flash Drive filtering;
- **Industrialist** — Transporter, Charging Station, Space-Time Accelerator, Inscriber and Recycler;
- **Applied Energy Weapons** — a Weapon Station plus at least one supported energy weapon.

## Optional specialization — Android path

The Red Pill opens the Android progression system. Android perks, abilities, chassis/body systems and FE management remain a powerful parallel progression route, but a human player can continue the technology campaign.

Advancement: `The Red Pill`.

The new **Synthetic Liaison** goal is social rather than inventory-driven. It is awarded only after persistent server-side Synthetic Trust reaches the threshold through conversations with MORROW, Chorus and HEPHAESTUS contacts.

## Optional specialization — Drone Commander

Uses the configurable Drone Core, Drone Fabricator, Charging Station and Drone Management systems. Combat, Repair, Logistics, Survey and Reactor Maintenance roles support later campaign chapters.

Advancement: `Drone Commander`.

## Chapter 4 — Advanced Power / Fusion

Uses Heavy Energy distribution, Fusion Reactor Controller/IO/coils, reactor profiles, shared ring power, stabilizers, telemetry and remote control.

Advancement: `Build the Impossible`.

New challenge: **Containment Engineer** requires controller, coil, IO, Gravitational Stabilizer and Space-Time Equalizer. Its framing deliberately separates safe fusion engineering from Project OVERDRIVE's unsafe convergence.

## Chapter 5 — Anomaly Engineering

Uses Gravitational Anomalies, Gravitational Stabilizers, containment systems, Space-Time Equalizer and anomaly research/Field Operations.

Advancement: `Anomaly Engineer`.

## Chapter 6 — Frontier Expedition

The player must visit all four major Frontier facilities:
- Deep Matter Vault;
- Autonomous Drone Foundry;
- Anomaly Quarantine Site;
- Orbital Recovery Array.

Existing Frontier discovery SavedData, dossiers, security-clearance objectives and Field Operations remain authoritative gameplay systems. The advancement is an additional vanilla-visible milestone, not a replacement for those rewards.

Advancement: `Frontier Expedition`.

The new **Incident Analyst** challenge is awarded when server-side Archive Insight reaches 18 through evidence-heavy NPC dialogue. It represents understanding context rather than simply collecting another item.

## Chapter 7 — The Black Site

The Black Site is the campaign's deliberate final exploration dungeon. Its layout should feel like a vanilla endgame structure: readable entry, escalating security, side research spaces, a deeper restricted layer, valuable optional rooms, then a safe route out.

Advancement: `The Black Site`.

## Capstone — Matter Overdrive

The player demonstrates mastery by combining Parallel Processing research technology, a recovered experimental Artifact and Reactor Remote/control technology. This represents control of research, exploration, anomalies, automation and reactor infrastructure rather than simply crafting one expensive item.

Advancement: `Matter Overdrive`.

A parallel completionist challenge, **Full-Spectrum Engineer**, asks the player to own representative Matter, routing, drone, fusion/containment and weapon infrastructure. The description explicitly warns against interpreting broad technical mastery as a requirement to rebuild Project OVERDRIVE's single coupled state.

## Exploration challenge — World Archivist

A separate challenge tracks visits to all 16 current structure families: six legacy structures, six modern technology facilities and four Frontier Expedition facilities. This is intentionally completionist and not required for the main campaign.

## Social/contact progression

Branching conversations now provide three additional vanilla-visible goals:
- **Field Liaison** — Field Team Trust >= 8;
- **Synthetic Liaison** — Synthetic Trust >= 8;
- **Incident Analyst** — Archive Insight >= 18.

Their JSON uses `minecraft:impossible`, with the criterion awarded by `DialogueAdvancementEvents` only after the authoritative persistent state reaches the threshold. The client cannot submit trust or advancement values.

## Advancement implementation

Advancements live in `data/matteroverdrive/advancements/campaign/` and use vanilla triggers wherever the condition is naturally data-driven:
- `minecraft:tick` for the root tab;
- `minecraft:location` for structure discovery;
- `minecraft:inventory_changed` for technology milestones;
- `minecraft:consume_item` for Android conversion.

Only social/contact milestones use a small server check because their criteria are stored in Matter Overdrive's conversation SavedData rather than vanilla inventory/location state.

## Relationship to existing research

The advancement tree does not replace research or Field Operations. The layers have different roles:
- research/contracts provide narrative, staged objectives and rewards;
- advancements show long-term milestones and exploration mastery in vanilla UI;
- branching NPC dialogue carries present-day interpretation and persistent relationship context;
- Data Pad remains the detailed journal;
- Field Operations remain repeatable endgame activities.

GuideME remains a technical manual and never gates, replaces or owns the advancement/datapack layer.

## Structure campaign rule

Every campaign-critical structure must be completable in Adventure-like play without breaking blocks. Structure progression may require combat, interacting with machines, opening containers or choosing side routes, but never digging through a wall because generation failed to connect rooms.
