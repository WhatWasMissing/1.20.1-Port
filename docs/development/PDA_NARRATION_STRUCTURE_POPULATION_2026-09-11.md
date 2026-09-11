# PDA Narration, Structure Population and Android Visual Repair — 2026-09-11

## Scope

This pass expands the exploration campaign in three connected directions:

1. recovered PDA lore can be read aloud;
2. structures receive finite, story-appropriate contemporary occupants and new hostile remnants;
3. legacy Android textures are rendered against their original atlas geometry instead of Minecraft's generic zombie layer.

## PDA narration

`PdaNarrationController` uses Minecraft's client narrator. No prerecorded voice assets are bundled.

Recovered DATA BANK pages and authenticated INCIDENT reconstructions expose `READ ALOUD` and `STOP` controls. Narration never returns text for unrecovered records or locked reconstructions. Changing lore pages/tabs or closing the PDA clears playback.

If Minecraft Narrator is disabled, the player receives an accessibility message explaining how to enable narration instead of receiving silent failure.

## Android texture repair

The existing PNGs were retained because they are the original legacy assets.

The rendering bug was model/atlas-side:

- normal Android atlas: 64x32;
- ranged Android atlas: 96x64.

The old renderer baked both against Minecraft's generic zombie layer. The new custom model layers preserve the original atlas sizes and the renderers use those layers directly.

New Android variants reuse appropriate original assets:

- MORROW/Defector Android -> `android_colorless.png`;
- ORPHEUS Directive-0 Enforcer -> `android_ranged.png`;
- M-0 Resonant Android -> `android_holo.png`.

## Contemporary NPC roster

`FacilityResearcherEntity` provides six persistent field roles:

- Field Researcher
- Salvage Specialist
- Recovery Specialist
- Reactor Recovery Engineer
- Anomaly Field Medic
- Incident Archivist

These NPCs are post-collapse characters, not resurrected historic cast members. Interacting with them opens role-specific dialogue through the existing dialogue packet/UI. They are excluded from hostile mob AI target selection so mixed encounter packages do not erase their dialogue before the player arrives.

`DefectorAndroidEntity` provides three neutral synthetic roles:

- MORROW Scout
- Chorus Courier
- HEPHAESTUS Liaison

They do not target players by default and can defend against hostile monsters.

## New enemy roster

### ORPHEUS Directive-0 Enforcer

Heavy ranged Android security remnant with stronger health, armor, follow range and knockback resistance than ordinary ranged rogue Androids.

### M-0 Resonant Android

Fast melee/resonance threat with increased health/damage and slow self-recovery. It represents damaged over-coupled neural lattices rather than the Chorus itself being inherently hostile.

## Structure population

`StructurePopulationEvents` reacts to player movement through 8-block scan cells. It only attempts population while a player is already inside a valid Matter Overdrive structure.

Safety properties:

- finite per-structure-start population key;
- persistent Overworld SavedData ledger;
- search radius 9;
- hard cap of 420 candidate position checks;
- candidate/current/floor blocks must already be loaded;
- candidate must lie inside one of the current `StructureStart` pieces;
- solid floor and two blocks of clear space are mandatory;
- occupants receive bounded home restrictions;
- no force-loading APIs are used.

This population layer is intentionally separate from authored structure security spawners. Security spawners remain dungeon mechanics; population provides contemporary inhabitants and encounter texture.

## Validation

Run:

```bat
VALIDATE_PDA_POPULATION_ANDROIDS.bat
```

Then perform the runtime matrix in:

`docs/testing/PDA_NARRATION_NPC_ENEMY_ANDROID_TEST_PLAN.md`

A local Forge compile/build remains the hard Java/API gate.
