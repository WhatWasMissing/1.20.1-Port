---
navigation:
  title: World Structures
  parent: index.md
  position: 9
  icon: matteroverdrive:tritanium_crate
---
# World Structures

Matter Overdrive adds abandoned facilities, crashed spacecraft, Android sites and gravitational anomalies to normal survival exploration. These structures are **optional discoveries**: they can introduce the scientist campaign, provide loot, encounters and lore, but they do not lock the technology tree. Experienced players may craft and use Matter Overdrive technology without finding every structure or completing the quests.

> World-generation changes only affect **new or unexplored chunks**. If you update an existing world, travel beyond previously generated terrain when looking for these sites.

## What to look for

| Structure | Where | Relative rarity | Why visit? |
| --- | --- | --- | --- |
| **Mad Scientist House** | Overworld surface | Common MO landmark | Best optional entry into scientist quests and research guidance. |
| **Crashed Space Ship** | Overworld surface | Uncommon | Salvage, crates and occasional weapon technology. |
| **Sand Pit** | Overworld surface | Uncommon | Legacy exploration site and salvage opportunity. |
| **Android House** | Overworld surface | Rare | Dangerous Android encounter with a guaranteed high-tier defender. |
| **Cargo Ship** | Overworld surface | Very rare | Large salvage site associated with contracts/logistics. |
| **Underwater Base** | Ocean floor | Rare | Large submerged research/salvage site. Bring water-breathing equipment. |
| **Gravitational Anomaly** | Overworld | Uncommon, dangerous | Natural route into anomaly research. Approach carefully. |
| **Matter Observatory** | Stable Overworld terrain | Rare | Analyze the central sensor, inspect telemetry and recover anomaly research data. |
| **Field Logistics Depot** | Stable Overworld terrain | Rare | Recharge equipment, extend a Matter Network route and recover field supplies. |

## Matter Observatory

The observatory is a compact field station rather than a decorative ruin. Its raised sensor mast marks the site from a distance, while the ring fence leaves a deliberate service approach. The Matter Analyzer is the scientific objective and the Facility Network Controller exposes the site's telemetry; bring power and a Data Pad if you want to continue the research trail. The site is only placed on a stable, mostly level nine-by-nine footprint, so it should not generate floating over ravines or with its entrance buried in a slope.

## Field Logistics Depot

Depots are expedition infrastructure: a marked service approach leads to a Charging Station, Network Switch, pipe junction and two separated Tritanium caches. They are intentionally small enough to read as a maintained field stop rather than a full laboratory, and use the same stable-footprint rule as the Observatory. Their main value is extending powered logistics while travelling between larger facilities.

All five compact technology sites have a deterministic salvage cache. The cache is assigned during generation and resolves lazily when opened, so chunk-safe placement does not depend on a loaded loot context. Entering a site with its paired machinery intact records a persistent field discovery, grants XP, a Data Pad and a site-specific research dossier. Use that dossier to archive the finding for a one-time upgrade and additional XP, then check the Data Pad's discovered-site count.

The rarity descriptions above are intentionally approximate. They are more useful during survival play than promising an exact distance, because Minecraft placement attempts, biome eligibility and terrain checks all affect what a player actually encounters.

## Mad Scientist House

This is the structure a new player should be happiest to find early. Mad Scientists provide the optional guided research campaign and explain Matter Overdrive systems in a suggested order.

**You do not need to find one before using the mod.** If you already understand Matter Overdrive, normal recipes and material progression remain available without research clearance.

The current survival tuning makes Scientist Houses substantially easier to encounter than before so the tutorial route can be discovered naturally rather than requiring a wiki or commands.

## Crashed Space Ship

The recovered ship template is **11 x 35**. Crashed ships are useful early exploration targets because their Tritanium Crates can contain Matter Overdrive salvage. The recovered population rules do not automatically add Android or Drone defenders, although the wreck itself may still be hazardous depending on terrain and nearby mobs.

Weapon Stations can rarely contain generated energy weapon technology. Treat a wreck as a useful shortcut or bonus, not a mandatory progression step.

## Android House

The recovered template is **21 x 21** with a **-2 Y offset**. A generated house contains a Matter Overdrive machine palette and several Rogue Android defenders. The recovered rules use **3-5 ordinary defenders** and guarantee one stronger level-3 legendary ranged Rogue Android.

This is deliberately a higher-risk structure than the Scientist House. Prepare for combat before entering rather than treating it as an early-game tutorial building.

## Sand Pit

The recovered template is **24 x 24** with a **-9 Y offset**. It is a legacy exploration/salvage location. Its authoritative generation hook does not automatically create Android or Drone defenders.

## Cargo Ship

The recovered template is **58 x 23**, making this one of the largest surface discoveries. Cargo Ships are intentionally much rarer than Scientist Houses or wrecks. Their legacy population logic is associated with contracts and cargo rather than automatic combat defenders.

For the 1.20.1 survival pass the old extremely sparse placement has been relaxed so a player can realistically encounter one through long-distance exploration without making Cargo Ships commonplace.

## Underwater Base

The recovered template is **43 x 43** and generates on the ocean floor. This is a later exploration target simply because reaching and searching a large submerged facility is dangerous without preparation.

Bring water breathing, doors/air management or suitable Android abilities. The recovered population hook does not automatically spawn Rogue Androids or Drones.

## Natural Gravitational Anomaly

Gravitational Anomalies can generate naturally in the Overworld. They are part of the **Anomaly Engineering** end of the suggested research path, but finding one early does not mean you have to interact with it.

Anomalies exert dangerous gravitational effects and become increasingly relevant once you understand stabilizers, reactor technology and anomaly mass. Mark the coordinates of an early discovery and return when equipped for it.

Natural anomalies are not intended to be the only route to late-game anomaly experimentation; the fusion/anomaly systems remain technology-driven rather than exploration-gated.

## Suggested exploration order

For a first playthrough, a comfortable order is:

1. **Mad Scientist House** - learn what the mod can do and optionally begin the research campaign.
2. **Crashed Ship / Sand Pit** - collect salvage while establishing Matter Technology.
3. **Android House** - tackle a dangerous combat site once equipped.
4. **Cargo Ship / Underwater Base** - pursue rarer large facilities during automation and advanced-power exploration.
5. **Gravitational Anomaly** - investigate seriously once fusion/stabilizer technology is available.

This is advice, **not an unlock chain**.

## If you cannot find a structure

- Make sure you are exploring **new chunks** after installing/updating the mod.
- Scientist Houses, wrecks and most legacy sites are Overworld discoveries; Underwater Bases require ocean terrain.
- Do not spend hours searching for one structure just to progress: all core technology should remain independently craftable.
- If a new test world produces none of these structures over substantial exploration, report the world seed and approximate explored distance. That is useful world-generation test data.

## Port/parity note

The structures are reconstructed from the 1.12.2 structure classes and recovered templates. Modern 1.20.1 terrain placement is used where necessary while source-backed dimensions and population behaviour are retained where practical. Exact visual/layout parity can continue to improve independently of survival functionality.


## Technology facilities and field research

Six large facilities offer optional exploration rewards: Synthetic Manufacturing Plants, Matter Refineries, Quantum Relay Stations, Android Command Bunkers, Fusion Research Complexes and rare Black Sites.

Search shipping, processing, control, armoury and vault rooms for Tritanium caches. Each main cache contains themed supplies and a recovered research dossier. **Use the dossier** to archive its finding and earn a one-time equipment/XP reward for that facility family. Keep it to reread or share it with a friend. Repeated copies do not pay the same explorer again, and discoveries do not skip scientist assignments.

Facility security stations run on finite emergency reserves and wake when a Survival player approaches. They deploy small guard groups over time. Clear the station before salvaging nearby equipment; an abandoned facility can still defend itself. Black Sites carry the strongest reserves and favour ranged defenders.

Some rooms have collapsed corners, damaged support machines and reduced security reserves. Surface facilities may also have broken service yards with lower-value salvage. New rewards and encounters appear only in newly generated facilities.

### Optional investigation chain

Some compact sites form an ordered investigation: **Android Relay Outpost -> Matter Observatory -> Anomaly Research Site**. Discovering the next site advances the per-player chain and the final site awards a Parallel Processing Upgrade. Your Data Pad reports the current step and the next lead, so the chain remains navigable after logging out or taking a break. The chain is optional, persistent and does not replace normal recipes or scientist assignments; discovering sites out of order still grants their ordinary dossier and discovery rewards.
