# Exploration-Native Structure Architecture — 2026-09-11

## Goal
The retained Matter Overdrive structure definitions are designed as exploration/dungeon content rather than a source of free fully-functional machinery. They are dormant in 0.7 while visual/content quality is reviewed.

If placement is approved in a later release, new-world structures should follow:

`terrain -> readable entrance -> critical path -> guarded focal reward -> optional story/salvage rooms -> return route`

No intended playthrough requires mining, pillaring, flight, or placing blocks.

## Retained definitions
- Legacy six: `LegacyVanillaStructurePiece`
- Modern six: `ModernExplorationStructurePiece`
- Frontier four: `FrontierExplorationStructurePiece`

No Matter Overdrive `structure_set` or structure biome modifier places these definitions in new worlds in 0.7. The piece serializers remain registered for existing-save compatibility and future review.

The previous piece serializers remain registered so old worlds can still deserialize already-generated structures.

## Reward policy
If reactivated after review, exploration generators may place:
- seeded Tritanium loot caches;
- finite facility-security Android spawners guarding focal rewards;
- vanilla/decorative industrial wreckage;
- lecterns, shelves, containment remains, cargo racks, debris, broken coils and other storytelling props.

They must not directly place functioning Matter Overdrive production/network/reactor machines.

`StructureExplorationSanitizer` remains as an old-world/fallback safety net. It is not an active new-world placement mechanism in 0.7.

## Legacy sites
Crashed Ship, Cargo Ship, Underwater Base, Mad Scientist Lab, Android Safehouse and Excavation Site use exploration-native layouts. A dedicated `LegacyTraversalRepairPiece` is authored after the Mad Scientist shell so the basement stair cannot be erased by room generation.

## Modern facilities
Synthetic Manufacturing Plant, Matter Refinery, Quantum Relay Station, Android Command Bunker, Fusion Research Complex and Black Site retain their `ModernExplorationStructurePiece` definitions for compatibility and future review. They do not generate in new worlds in 0.7.

The old `TechnologyFacilityStructurePiece`, infrastructure and terrain piece types remain registered for save compatibility but are not assembled into new structure starts.

Modern facility identity now comes from architecture and ruins rather than working machines:
- Manufacturing Plant: assembly benches, loading flow, dispatch archive.
- Matter Refinery: extraction pit, copper processing remains, storage/research caches.
- Quantum Relay: tall relay masts, signal/power side rooms, guarded core archive.
- Android Command Bunker: surface descent, security, command, drone/barracks scenery, guarded armory.
- Fusion Research Complex: large circular reactor chamber, broken coil/stabilizer pedestals, research reward.
- Black Site: concealed entrance, labs/containment, deep descended vault and strongest security reward.

`ModernTraversalRepairPiece` is generated after the primary shell for two audited critical transitions: Refinery core-to-lowered excavation and Black Site core-to-deep-descent.

## Frontier sites
Deep Matter Vault, Autonomous Drone Foundry, Anomaly Quarantine Site and Orbital Recovery Array now generate from `FrontierExplorationStructurePiece`.

- Vault: surface archive, long returnable descent, security/core, archive/refinery ruins.
- Foundry: reception/security, fabrication and drone-rack wings, marked hangar focal room.
- Quarantine: surface approach, decontamination, observation/security, containment focal room.
- Recovery Array: control route, signal/storage wings and a large antenna landmark.

## Static gate
Run:

`VALIDATE_STRUCTURE_TOPOLOGY.bat`

The validator now rejects:
- fallback to retired machine-room generators;
- missing exploration serializers;
- missing traversal repair pieces;
- known buried/elevated entrance regressions;
- force-loading APIs;
- missing chunk clipping;
- functional Matter Overdrive machine IDs in any active exploration generator;
- missing guarded-cache safety systems.

## Runtime remains mandatory
Static validation cannot prove terrain interaction, collision or player perception. Each structure must still be generated and walked in Survival-style movement from terrain to focal reward and back out, with multiple starts/variants inspected.
