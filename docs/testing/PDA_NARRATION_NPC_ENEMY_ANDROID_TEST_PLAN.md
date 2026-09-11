# PDA Narration, NPC Population, Enemy Expansion and Android Rendering — Runtime Test Plan

Date: 2026-09-11
Branch: `feature/lead-dev-expansion-2026-09-11`

## 1. Static gate

Run:

```bat
VALIDATE_PDA_POPULATION_ANDROIDS.bat
```

Expected: `RESULT: PASS`.

Then run:

```bat
gradlew.bat compileJava --no-daemon
gradlew.bat build --no-daemon
```

## 2. PDA read-aloud

With Minecraft Narrator enabled to a mode that permits system narration:

1. Open the Data Pad.
2. Enter DATA BANK on a recovered record.
3. Confirm `READ ALOUD` and `STOP` are visible.
4. Test all three record pages:
   - source file,
   - evidence/forensics,
   - implication/cross-reference.
5. Confirm the spoken text matches the visible recovered entry and never reveals an unrecovered record.
6. Navigate to another page while narration is active; old narration must stop.
7. Switch tabs while narration is active; old narration must stop.
8. Close the PDA; narration must stop.
9. Open INCIDENT and confirm authenticated reconstructions can be read aloud.
10. Confirm locked reconstructions cannot be narrated.

With Minecraft Narrator disabled:

1. Press `READ ALOUD`.
2. Confirm a clear accessibility message explains that Narrator must be enabled.
3. Confirm there is no crash and no lore state changes.

## 3. Android visual repair

Check both in daylight and low light:

1. Spawn a melee Rogue Android.
2. Confirm its original 64x32 legacy texture maps correctly to head, body, arms and legs.
3. Spawn a Ranged Rogue Android.
4. Confirm the 96x64 ranged texture no longer appears stretched, offset or wrapped as a generic 64x64 zombie skin.
5. Inspect arm/head UV seams during walking, melee and ranged attack animations.
6. Spawn a MORROW/Defector Android and confirm the colorless legacy atlas maps correctly.
7. Spawn an M-0 Resonant Android and confirm its legacy holographic/resonant atlas is aligned rather than UV-scrambled.
8. Spawn an ORPHEUS Enforcer and confirm ranged Android UV alignment.

Blocking failure: any Android uses visibly incorrect head/body UVs or falls back to a purple/black missing texture.

## 4. New NPC interaction

Verify the following contemporary roles can be interacted with and open dialogue without a villager trading screen:

- Field Researcher
- Salvage Specialist
- Recovery Specialist
- Reactor Recovery Engineer
- Anomaly Field Medic
- Incident Archivist
- MORROW Scout
- Chorus Courier
- HEPHAESTUS Liaison

Confirm roles and custom names persist after save/reload.

## 5. Structure population

Use fresh structure starts. For each of the 16 structure families:

1. Approach normally without `/forceload`.
2. Enter the structure.
3. Confirm its expected finite population package appears.
4. Confirm entities appear on valid floor with headroom rather than inside walls, roofs or pits.
5. Confirm friendly/neutral NPCs are not placed in the sole critical traversal lane.
6. Confirm enemies have enough room to fight.
7. Leave and re-enter the structure.
8. Confirm the one-time population package is not duplicated.
9. Save/reload and re-enter.
10. Confirm population does not duplicate after restart.

Expected highlights:

- MORROW Safehouse: MORROW Scout + Chorus Courier.
- HELIX: Field Researcher + Directive-0 security.
- Bastion: Directive-0 security + MORROW presence.
- ICARUS: Reactor Recovery Engineer + resonant Androids.
- ORPHEUS Black Site: strongest Directive-0 package plus resonant threat.
- MNEMOSYNE: Incident Archivist + resonant Androids.
- HEPHAESTUS: HEPHAESTUS Liaison + Chorus Courier + security remnant.
- JANUS: Anomaly Field Medic + resonant Androids.
- LAGRANGE: Recovery Specialist + mixed security/resonance threat.

## 6. Performance / safety

During exploration:

- watch `latest.log` for structure-manager or entity errors;
- confirm no unexpected chunk tickets/force loads appear;
- confirm entering a structure does not cause a long server tick stall;
- confirm standing still does not repeatedly scan/spawn every second;
- confirm walking into a structure within the same chunk still triggers population after crossing an 8-block scan cell;
- inspect at least one structure spanning multiple chunks and verify all spawned occupants remain inside an actual StructurePiece.

## 7. Combat sanity

- ORPHEUS Directive-0 Enforcer should feel heavier than a normal ranged rogue Android.
- M-0 Resonant Android should be more aggressive/mobile than a normal melee rogue and slowly recover minor damage over time.
- Defector Androids should not initiate attacks on players.
- Defectors should be able to defend themselves against hostile monsters.
- Existing Rogue/Ranged Rogue Androids should retain their normal behavior outside structure populations.

## 8. Regression checks

- GuideME remains accessible from the PDA Technical Manual button when installed.
- PDA still opens instead of GuideME when the Data Pad is used.
- Structure lore discovery/reconstruction still works.
- Existing security spawners still work and remain finite.
- No Star Map progression is restored.
- Existing worlds containing old entity/structure serializers still load.
