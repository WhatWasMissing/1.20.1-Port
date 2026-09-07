# Android Constellation / Artifact / Drone Commander Pass

Baseline: `work/0.3-pointblank-destiny-integration` at `1b04b96f74247b32fde14bc1fcfb7477ef458f29`.

Implementation branch: `work/0.3-titan-artifact-drone-specialisation`.

## Implemented

- Reworked the Aspect / Fragment screen into a constellation-style matrix inspired by radial sci-fi subclass layouts.
- Added a three-tab presentation: Matrix, Drone Commander and Artifact.
- Expanded Aspects from 6 to 12 across Assault, Chassis, Utility and Drone Commander identities.
- Expanded Fragments from 14 to 24, including additional ability, mobility, energy and drone-support modifiers.
- Raised maximum Fragment capacity to 6 when the equipped Aspects provide enough slots.
- Added a one-equipped-at-a-time Artifact system with 7 active artifacts plus the unequipped state.
- Added a 9-node Drone Commander specialisation from Android level 2 through level 10.
- Drone Commander progression is sequential and permits up to 5 installed specialisation nodes.
- Added three Drone Commander Aspects: Command Uplink, Swarm Logic and Guardian Directive.
- Added six drone-focused Fragments: Sentinel, Pack Tactics, Repair Beacon, Target Link, Escort and Ordnance.
- Added gameplay hooks for owned drones: damage scaling, marked-target bonuses, swarm scaling, Resistance, regeneration and operator damage reduction.
- Added artifact gameplay hooks for ability damage, shielding, nanite repair, Hunter Array, movement support, drone support and FE return.
- Extended Android client/server sync to persist and mirror Artifact and Drone Commander selections.
- Bumped the network protocol to 3 because the Android state packet has new fields.

## Persistence

Artifact selection and Drone Commander nodes are stored under `MatterOverdriveAndroidLoadout` and copied during player clone in the same way as Aspects and Fragments.

## Test checklist

1. Open the Android loadout screen and verify the Matrix, Drone Commander and Artifact tabs render at normal GUI scale and at a reduced GUI scale.
2. Equip two Aspects and verify a third cannot be equipped.
3. Verify Fragment capacity is the sum of equipped Aspect slots, capped at 6.
4. Equip six Fragments with two 3-slot Aspects; remove one Aspect and verify excess Fragments are trimmed.
5. Equip an Artifact, relog, die/respawn and verify it persists.
6. Equip another Artifact and verify it replaces the previous Artifact.
7. Verify Overclocked Relay, Aegis Prism, Nanite Crown, Hunter Lens and Capacitor Heart produce their listed effects.
8. Link one or more drones and verify Drone Commander nodes cannot be selected before their Android level gate.
9. Verify Drone Commander nodes must be selected in sequence and no more than five can be installed.
10. Verify Targeting Suite / Ordnance Link increase owned-drone damage.
11. Verify Field Repair / Overmind regenerate owned drones near the operator.
12. Verify Reinforced Drones / Sentinel / Guardian Directive grant drone Resistance as appropriate.
13. Verify Hunter Network / Target Link / Hunter Lens increase drone damage to glowing targets.
14. Verify Swarm Cohesion / Pack Tactics / Swarm Logic scale with multiple owned drones.
15. Verify Escort Fragment / Escort Protocol reduces incoming operator damage when an owned drone is nearby.
16. Verify loadout changes sync immediately without reopening the screen.
17. Verify old worlds with no Artifact/DronePerk tags default to No Artifact and zero Drone Commander nodes.
18. Verify multiplayer rejects mismatched protocol-2 clients rather than desynchronising the extended packet.

## Follow-up candidates

- Add bespoke icon textures for every Aspect, Fragment, Artifact and Drone Commander node rather than glyph-only nodes.
- Add branch-specific animated connector effects and a central Android silhouette/core render.
- Add explicit refund/respec costs for Drone Commander nodes rather than free specialist swapping.
- Add drone command hotkeys / radial command UI to complement the new specialisation tree.
- Add additional drone chassis/classes so Drone Commander can specialise different drone roles rather than only generic linked drones.
