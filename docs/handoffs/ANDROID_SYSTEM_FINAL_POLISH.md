# Android System Final Polish

This pass closes out the current Android-system feature work for the 0.3 testing line.

## Final player-facing structure

Permanent progression remains in **Android Mastery** (`K`). The swappable combat build lives in the **Android Class Matrix** (`L`):

1. Class
2. Subclass
3. H subclass ability
4. N subclass tech ability
5. G subclass Ultimate
6. Up to two subclass Aspects
7. Fragment slots supplied by those Aspects
8. One high-impact Passive Protocol
9. Optional Drone Matrix progression

## 3 x 3 subclass matrix

### Strider
- **Phase Stalker** — H: Reflex Shift / N: Phase Snare / G: Phase Dominion
- **Hunter-Killer** — H: Pursuit Dash / N: Predator Sweep / G: Execution Lattice
- **Precision Frame** — H: Focus Step / N: Target Designator / G: Railstorm Protocol

### Juggernaut
- **Singularity Breaker** — H: Kinetic Ram / N: Seismic Charge / G: Singularity Cascade
- **Citadel** — H: Bastion Frame / N: Aegis Pulse / G: Citadel Protocol
- **Siege Frame** — H: Breach Advance / N: Ordnance Burst / G: Siege Engine

### Architect
- **Drone Commander** — H: Command Relay / N: Swarm Surge / G: Overmind Ascendant
- **Nanite Weaver** — H: Restoration Well / N: Nanite Surge / G: Nanite Bloom
- **Gravity Core** — H: Inertial Shift / N: Gravity Pulse / G: Event Horizon

All H/N actions are server-authoritative, level-gated, consume FE and use persistent cooldowns. The class screen displays their costs and base cooldowns.

## Passive Protocols

The old Artifact save slot is preserved internally for compatibility but is entirely player-facing as a freely selectable passive:

- Overclock Protocol
- Aegis Protocol
- Nanite Recovery
- Hunter Protocol
- Phase Stability
- Swarm Support
- Capacitor Feedback

These are deliberately stronger than Fragments so selecting one changes how a build plays.

## Polish / fixes in this pass

- Class Matrix now shows subclass-specific H/N/G kits rather than only class-wide H/N actions.
- Inspection panel includes FE cost and cooldown for H/N/G abilities.
- Main class UI was tightened and made more tolerant of smaller GUI sizes; fragment buttons no longer blindly draw through the footer.
- Nanite visuals were moved away from villager-heart-style particles to a more neutral synthetic particle effect.
- Drone Matrix progression cap was corrected from five to nine nodes. The chain is sequential from Android level 2 through the level-10 Synthetic Overmind capstone, so the former five-node cap made levels 7-10 unreachable.
- Existing first-four subclass enum ordinals and the internal Artifact NBT key remain unchanged to protect old saves.

## Final manual verification checklist

- Convert through the Mad Scientist Puny Humans quest and confirm the player starts as an Android without quest-state loss.
- Open `L`; switch between all three classes and all nine subclasses.
- For every subclass, use H, N and G and verify FE cost, cooldown, effects and status text.
- Verify switching subclass removes incompatible Aspects but keeps valid fragment limits.
- Select each Passive Protocol and confirm the described gameplay benefit.
- Progress Drone Matrix from Command Authority through Synthetic Overmind at levels 2-10 and verify all nine can now be installed sequentially.
- Re-log and die/respawn; verify class/loadout/passive/drone progression persists.
- Check the quest tracker remains visible and Mad Scientist quest progression is unchanged.
- Test the class UI at normal and small GUI scales for clipping/overlap.

## Status

The Android system should now be treated as feature-complete for the current 0.3 testing scope. Further work should be balance fixes, bug fixes, text/visual polish or changes driven by testing rather than additional progression layers.
