# Survival Feasibility Pass

## Design rule

The research/quest campaign is guidance, story and rewards. It must not be a mandatory technology gate. Experienced players and repeat worlds may build machines and progress through Matter Overdrive directly when they have the materials and recipes.

Canonical suggested order remains:

Scientist Research -> Matter Technology -> Automation & Drones -> Advanced Power -> Fusion Research -> Anomaly Engineering

This is a teaching order, not a hard unlock chain.

## World generation fixes in this pass

Previous Forge biome modifiers used empty biome lists for legacy structures and gravitational anomalies. That left the configured/placed features present in data but without useful biome injection.

Changed:
- surface legacy structures inject into `#minecraft:is_overworld`;
- underwater bases inject into `#minecraft:is_ocean`;
- gravitational anomalies inject into `#minecraft:is_overworld`.

Discoverability tuning:
- Mad Scientist House: rarity 768 -> 192. This is the primary optional quest/tutorial entry and should be reasonably discoverable.
- Android House: 1536 -> 768.
- Cargo Ship: 4096 -> 1024.
- Underwater Base: 2048 -> 768, ocean-only.
- Crashed Ship remains 256.
- Sand Pit remains 384.
- Natural Gravitational Anomaly remains 200.

These values are initial survival-testing values, not final balance.

## Survival feasibility expectations

A fresh player should be able to progress without completing research quests:
1. Find or craft access to early Matter Overdrive materials.
2. Obtain Tritanium/Dilithium through normal world generation.
3. Craft basic matter machines using recipes rather than quest flags.
4. Establish matter processing and replication.
5. Build networks/automation when resources permit.
6. Establish an FE grid.
7. Build the fusion reactor and stabilizers from obtainable components.
8. Discover or create/use anomalies and reach anomaly engineering.

The scientist campaign should make this route easier to understand and provide useful rewards, but not be required to make recipes function.

## New-world runtime checklist

Use a NEW WORLD or unexplored chunks. Existing generated chunks will not retroactively receive structures.

Worldgen:
- [ ] Tritanium ore generates and can be mined in survival.
- [ ] Dilithium ore generates and can be mined in survival.
- [ ] Crashed Ship appears naturally.
- [ ] Mad Scientist House appears naturally and contains/produces the expected scientist encounter.
- [ ] Android House appears naturally.
- [ ] Sand Pit appears naturally.
- [ ] Cargo Ship appears naturally.
- [ ] Underwater Base appears on an ocean floor, not inland.
- [ ] Natural Gravitational Anomaly appears without commands.
- [ ] Structure placement does not noticeably damage chunk generation/TPS.

Quest independence:
- [ ] Without accepting a scientist quest, basic Matter Overdrive recipes remain usable.
- [ ] Decomposer/Recycler/Scanner progression works without research clearance.
- [ ] Pattern storage and Replicator work without quest completion.
- [ ] Network/automation blocks work without quest completion.
- [ ] Power generation/storage works without quest completion.
- [ ] Fusion reactor can be constructed and operated without quest completion.
- [ ] Anomaly systems work without quest completion.

Guided route:
- [ ] Mad Scientist is common enough to find during ordinary exploration.
- [ ] Scientist quests correctly point toward systems that are independently craftable.
- [ ] Quest rewards help progression without containing irreplaceable mandatory components.
- [ ] Data Pad describes the suggested stage without claiming later technology is locked.

## Follow-up after runtime testing

Adjust rarity from measured exploration results rather than guessing further. If structures are still too hard to find, add a survival locator/research lead rather than making every structure extremely common.
