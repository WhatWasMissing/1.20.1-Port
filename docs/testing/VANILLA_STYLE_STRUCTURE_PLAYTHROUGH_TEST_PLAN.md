# Vanilla-Style Structure Playthrough Gate

Branch: `feature/lead-dev-expansion-2026-09-11`

Matter Overdrive structures should play like strong vanilla structures: readable from the outside, enterable without mining, understandable while moving through them, and composed of a critical path plus optional reward spaces rather than a maze of boxes.

## Required playthrough model

Every structure must support:

`terrain -> obvious approach -> entrance -> readable critical path -> focal objective -> optional side rewards -> safe return/exit`

A player should be able to complete the location without breaking or placing blocks.

## Vanilla-like principles

- Exterior silhouette tells the player what the structure is before entry.
- Entrance is visible or naturally discoverable from the approach side.
- Main route is wider/brighter/more consistently framed than side routes.
- Side rooms contain optional loot, lore, machines or encounters and reconnect cleanly.
- Important transitions use architectural cues: stairs, shafts, doors, windows, floor stripes, lighting or landmark sightlines.
- Structure complexity comes from spatial sequencing, not random corridor length.
- Combat rooms provide enough clear floor to fight normally.
- Loot is visible/reachable without hidden mandatory block breaking.
- Final/core room has a stronger visual identity than ordinary rooms.
- Return route is obvious; one-way drops are forbidden unless a guaranteed exit exists.

## Blocking failures

Any of these fail the structure:

- entrance buried by terrain with no usable approach;
- entrance opens into solid blocks;
- required corridor dead-ends unexpectedly;
- disconnected required room;
- machine/decoration occupies the only doorway;
- corridor below 2 blocks wide or 3 blocks high on the critical path;
- jump higher than normal player movement requires block placement;
- unavoidable fall damage on the intended route;
- ladder/stair/shaft does not meet the floor it is supposed to connect;
- damage variant removes the only connection;
- enemy spawner blocks the only usable combat lane;
- optional room accidentally becomes mandatory because the main route is sealed;
- player reaches the objective but cannot return without mining;
- two pieces overlap in a way that creates solid walls in the traversable lane;
- chunk boundary leaves a seam/void/solid obstruction through the path.

## Families to verify

### Legacy
Crashed Ship, Cargo Ship, Underwater Base, Mad Scientist Lab, Android Safehouse, Sand Pit/Excavation Site.

### Modern facilities
Synthetic Manufacturing Plant, Matter Refinery, Quantum Relay Station, Android Command Bunker, Fusion Research Complex, Black Site.

### Frontier
Deep Matter Vault, Autonomous Drone Foundry, Anomaly Quarantine Site, Orbital Recovery Array.

## Runtime method

For each family:
1. Use `/locate structure matteroverdrive:<id>` in a fresh test world.
2. Approach from normal terrain rather than teleporting directly inside.
3. Enter in Survival/Adventure-style movement without breaking blocks.
4. Follow the most visually obvious path first.
5. Reach the focal/core room.
6. Inspect all side rooms and loot spaces.
7. Return to terrain without mining or placing blocks.
8. Repeat on at least three generated starts/layout variants.
9. Repeat on a damaged/occupied variant where applicable.
10. Cross chunk boundaries while traversing and inspect for seams/blocked openings.

Any failed route is a generation bug, not an acceptable procedural variation.
