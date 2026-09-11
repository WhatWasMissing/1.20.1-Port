# Overdrive Incident Story Campaign — Runtime Test Plan

## Static gates

From repository root:

```bat
VALIDATE_STRUCTURE_TOPOLOGY.bat
VALIDATE_STRUCTURE_LORE.bat
gradlew.bat compileJava --no-daemon
```

Do not accept the story pass if either validator fails.

## Per-structure record test

In a fresh world, enter each of the 16 structure classes at least once.

For every first visit verify:

- exactly one recovered `facility_research` dossier is awarded;
- the item has the expected custom title;
- author/unit line is visible;
- incident summary is readable;
- the cross-reference points toward another relevant site;
- chat reports `RECOVERED RECORD` and correct progress out of 16;
- leaving/re-entering the same structure does not award another copy;
- another generated instance of the same structure class also does not duplicate the unique record;
- another player can independently recover their own copy.

## Persistence

After collecting several records:

1. save and exit;
2. reload the world;
3. re-enter previously discovered sites;
4. confirm no duplicate records are awarded;
5. enter an undiscovered structure class and confirm progress continues from the previous total.

Also repeat across dimension changes and death/respawn.

## Narrative ordering

Records may be discovered in any order. Confirm that no record assumes the player has already read a specific earlier entry. Cross-references should act as clues, not hard quest prerequisites.

Recommended narrative-order walkthrough:

1. Halcyon-7 / ECHO-9 clue
2. DUSTWELL / SAMPLE M-0
3. KESTREL contamination
4. NEREID / ICARUS anomaly evidence
5. MORROW / GLASS KNIFE
6. HELIX production order
7. Bastion purge order
8. ORPHEUS OVERDRIVE directive
9. MNEMOSYNE propagation evidence
10. HEPHAESTUS refusal cascade
11. JANUS resonance study
12. LAGRANGE final warning

## Completion

After recovering all sixteen records verify:

- progress reaches 16/16 exactly once;
- player receives the named `Closed Loop Artifact`;
- artifact contains `LoreArc=OVERDRIVE_INCIDENT_COMPLETE`;
- the `Reconstruct the Incident` challenge advancement completes;
- completion chat appears once;
- revisiting structures does not award another completion artifact;
- save/reload does not repeat the completion reward.

## Rare archive mezzanine

Locate layout-2 examples of at least three modern facilities and two Frontier sites.

For each rare variant verify:

- raised archive mezzanine exists inside the main volume;
- normal entrance-to-objective route remains unobstructed below it;
- three-wide stair is bidirectionally traversable without jumping/building;
- lectern and archive shelving read visually as a research/archive space;
- story cache is reachable;
- security spawner, when present, has enough combat room and does not block the stair;
- player can leave the mezzanine by the same route;
- no part of the mezzanine punches through exterior walls or neighboring rooms in a nonsensical way.

## Multiplayer

With two players:

- each player has independent 16-record progress;
- one player's discovery does not consume the other player's record;
- rare archive loot behaves according to normal container/loot rules, independent from lore-record persistence;
- completion artifact and advancement are per player.

## Failure conditions

Treat any of the following as blocking:

- duplicate unique records;
- missing record for any structure class;
- wrong structure producing a record;
- progress desynchronization after reload;
- completion reward farming;
- unreadable/missing dossier lore;
- rare mezzanine blocking the structure's critical path;
- guard spawning on stairs or inside blocks;
- forced chunk loading or structure-generation hangs.
