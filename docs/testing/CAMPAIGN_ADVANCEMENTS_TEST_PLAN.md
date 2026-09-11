# Matter Overdrive Campaign / Advancement Test Plan

Branch: `feature/lead-dev-expansion-2026-09-11`

## Load / datapack gate

1. Run a clean Forge 1.20.1 build.
2. Start a fresh world and confirm no advancement JSON parse errors appear.
3. Open the Advancements screen and confirm the Matter Overdrive tab is visible.
4. Confirm the root icon/background render correctly.

## Main path

1. Enter a generated Crashed Ship: `First Contact` must complete.
2. Obtain a Matter Analyzer, Decomposer and Pattern Drive: `The Matter Age` must complete only after all three criteria are satisfied.
3. Obtain a Network Router, Network Switch and Replicator: `Networked Industry` must complete only after all three are satisfied.
4. Consume a Red Pill on a valid player: `The Red Pill` should complete. Confirm an already-converted player does not create campaign corruption.
5. Obtain a Configurable Drone Core: `Drone Commander` completes.
6. Obtain a Fusion Reactor Controller: `Build the Impossible` completes.
7. Obtain both Space-Time Equalizer and Anomaly Containment Unit: `Anomaly Engineer` completes only after both.
8. Enter Deep Matter Vault, Autonomous Drone Foundry, Anomaly Quarantine Site and Orbital Recovery Array: `Frontier Expedition` completes after the fourth distinct site type.
9. Enter a Black Site: `The Black Site` completes.
10. Possess Parallel Processing Upgrade, Artifact and Reactor Remote: `Matter Overdrive` capstone completes only when all three criteria have been met.

## Exploration challenge

Visit each current structure family and verify `World Archivist` records each independently:
- crashed_ship
- cargo_ship
- underwater_base
- mad_scientist_house
- android_house
- sand_pit
- synthetic_manufacturing_plant
- matter_refinery
- quantum_relay_station
- android_command_bunker
- fusion_research_complex
- black_site
- deep_matter_vault
- autonomous_drone_foundry
- anomaly_quarantine_site
- orbital_recovery_array

The challenge must complete only after all sixteen criteria are recorded.

## Multiplayer / persistence

- Two players progress independently.
- One player's structure entry must not grant another player's advancement unless that player also satisfies the criterion.
- Save/reload preserves partial advancement progress.
- Dedicated-server restart preserves progress.
- Existing research SavedData and Frontier discovery rewards still behave normally; advancements must not duplicate dossier/Field Operation rewards.

## Regression

- Scientist research and contracts remain authoritative narrative progression.
- Android conversion remains optional for the main campaign.
- World Archivist is optional and does not gate the Black Site or capstone.
- No retired Star Map advancement or campaign requirement is present.
