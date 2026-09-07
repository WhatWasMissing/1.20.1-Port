# Matter Overdrive Research Progression Spine

The modern 1.20.1 port uses this as its canonical non-Android progression:

1. Scientist Research
2. Matter Technology
3. Automation & Drones
4. Advanced Power
5. Fusion Research
6. Anomaly Engineering

Android progression remains a parallel character-build system and is not a prerequisite for the technology campaign beyond legacy quest compatibility where applicable. Star Map content is legacy compatibility content and is not a progression pillar.

## Stage 1 - Scientist Research

Purpose: introduce the setting, scientists, recovered legacy quests and research-clearance language.

Current implementation:
- Puny Humans introduction/conversion path.
- Restored six-quest legacy scientist campaign.
- Scientist dialogue UI.
- Persistent research clearance.
- Completion automatically unlocks Matter Technology.

## Stage 2 - Matter Technology

Player goal: establish a closed matter-processing loop.

Core existing systems to integrate:
- Matter Scanner / Matter Analyzer
- Decomposer / Portable Decomposer
- Matter Container
- Matter Recycler
- Pattern Storage / Pattern Drive
- Replicator

Planned milestone checks:
- scan/learn matter values;
- decompose resources;
- store a useful amount of matter;
- create/store a pattern;
- successfully replicate an item;
- sustain several replications from stored/decomposed matter.

Completion unlocks Automation & Drones.

## Stage 3 - Automation & Drones

Player goal: turn individual machines into infrastructure.

Core existing systems to integrate:
- Matter Pipe / Network Pipe
- Network Router / Network Switch
- Pattern Monitor / Pattern Storage
- Transporter where useful for local logistics
- Drones and drone-related Android features where appropriate

Planned additions:
- research tasks for networked transfer and automated machine operation;
- non-Android drone infrastructure so drones are a mod system, not only an Android perk;
- logistics/repair/scout roles;
- machine-network status and diagnostics.

Completion unlocks Advanced Power.

## Stage 4 - Advanced Power

Player goal: build a stable grid capable of sustaining automated matter infrastructure.

Core existing systems:
- Solar Panel
- Batteries / Charging Station
- Heavy Matter Pipe energy relay
- machine upgrades and storage upgrades

Planned milestone checks:
- renewable generation;
- energy storage;
- multi-machine distribution;
- sustained automated load;
- failsafe/power-storage upgrades.

Completion unlocks Fusion Research.

## Stage 5 - Fusion Research

Player goal: safely construct and operate the reactor as an experimental research machine rather than simply a large generator.

Core existing systems:
- Fusion Reactor Controller
- Fusion Reactor Coil
- Fusion Reactor IO
- Gravitational Stabilizers
- Reactor Remote
- ring power sharing and upgrade support

Planned milestone checks:
- valid multiblock;
- IO connectivity;
- stabilizer containment;
- controlled matter feeding;
- sustained output under load;
- safety/failsafe operation.

Completion unlocks Anomaly Engineering.

## Stage 6 - Anomaly Engineering

Player goal: deliberately manipulate the anomaly instead of treating it as reactor exhaust.

Core existing systems:
- Gravitational Anomaly
- mass accumulation
- stabilizers
- spacetime technology

Planned additions:
- anomaly growth/containment research;
- useful mass thresholds;
- exotic outputs/resources;
- controlled extraction;
- high-mass reactor modes;
- dangerous instability states and emergency containment;
- optional natural anomaly research events.

This is the intended technological endgame.

## Design rules

- Quests teach systems rather than merely gate recipes.
- Existing worlds keep registry compatibility.
- Star Map does not gate this progression.
- Androids receive alternative advantages but do not replace the research campaign.
- Each stage must have a visible current objective, completion milestone and clear transition to the next stage.
- New machines/features should have an explicit home in one of these six stages before implementation.
