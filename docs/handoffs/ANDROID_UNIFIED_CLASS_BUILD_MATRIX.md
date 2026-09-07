# Android Unified Class Build Matrix

## Current hierarchy

The player-facing Android build system is now presented as:

`Class -> Specialisation -> Class Ability -> Tech Ability -> Ultimate -> Aspects -> Fragments -> Passive`

Permanent level-based perks remain a separate long-term progression layer and should be treated as **Android Mastery**, opened with the existing mastery/skill-tree key.

## Classes

### Strider
- Hunter-style agile synthetic frame.
- Current specialisation: Utility.
- Class ability: **Reflex Shift** (`H`).
- Tech ability: **Predator Sweep** (`N`).
- Current Ultimate: **Phase Dominion** (`G`).

### Juggernaut
- Titan-style front-line synthetic frame.
- Current specialisations: Assault and Chassis.
- Class ability: **Bastion Frame** (`H`).
- Tech ability: **Seismic Charge** (`N`).
- Ultimate depends on specialisation: **Singularity Cascade** or **Citadel Protocol** (`G`).

### Architect
- Warlock-style synthetic technomancer/support frame.
- Current specialisation: Drone Commander.
- Class ability: **Restoration Well** (`H`).
- Tech ability: **Nanite Surge** (`N`).
- Current Ultimate: **Overmind Ascendant** (`G`).

## Passive protocol

The old player-facing Artifact concept has been retired. The existing save/network ordinal is intentionally retained for world compatibility, but the UI now presents it as one freely selectable always-on **Passive Protocol**.

Current choices:
- Overclock Protocol
- Aegis Protocol
- Nanite Recovery
- Hunter Protocol
- Phase Stability
- Swarm Support
- Capacitor Feedback
- No Passive

## Compatibility notes

- Existing saved Artifact ordinals remain valid and map directly to the equivalent Passive Protocol.
- Existing saved specialisations still determine the top-level class, preserving old loadouts.
- Changing top-level class selects that class's default specialisation through the existing server-authoritative loadout packet.
- No quest state, Mad Scientist progression, Android conversion state, or permanent perk data is migrated by this pass.

## Test checklist

1. Open the class matrix and verify exactly three top-level classes are shown.
2. Switch Strider -> Juggernaut -> Architect and verify the displayed specialisations update.
3. Verify Juggernaut exposes both Assault and Chassis.
4. Verify `H`, `N`, and `G` ability cards show the correct ability names for each class/specialisation.
5. Select Aspects and Fragments and confirm limits still apply.
6. Select each Passive Protocol and confirm only one is active at once.
7. Reload the world and verify class/specialisation, Aspects, Fragments, drone perks and Passive persist.
8. Verify existing Mad Scientist quests continue progressing after class changes.
9. Verify `K` still opens permanent Android progression and selections persist independently of class swaps.
