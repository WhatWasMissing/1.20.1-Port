# Vanilla-Standard Structure Static Audit — 2026-09-11

Branch: `feature/lead-dev-expansion-2026-09-11`

This audit evaluates all 16 current Matter Overdrive structure families against `VANILLA_STYLE_STRUCTURE_PLAYTHROUGH_TEST_PLAN.md`.

## Audit standard

Every structure should support:

`terrain -> obvious approach -> entrance -> readable critical path -> focal objective -> optional side rewards -> safe return/exit`

A structure fails the static audit when source geometry proves that a required route is disconnected, inaccessible without placing/breaking blocks, buried without a surface approach, or can be obstructed by stale helper pieces.

Static PASS means the source topology is internally coherent. It does **not** replace fresh-world runtime traversal.

## Summary

- Legacy family: 2 provisional pass, 4 blocking failures.
- Modern technology facilities: primary redesign is broadly sound, but all 6 are currently **conditional/fail** because `FacilityInfrastructurePiece` and `FacilityTerrainPiece` still contain offsets from the previous layouts and are assembled on top of the redesigned geometry.
- Frontier family: 3 provisional pass, 1 blocking failure.
- Overall: the structure set is **not yet vanilla-standard compliant**.

## Legacy structures

### Crashed Ship — FAIL

Good:
- distinct wreck silhouette;
- clear internal central aisle;
- cockpit/service/engineering sequencing;
- impact damage is kept away from the centre path.

Blocking issue:
- the hull floor is generated at `surfaceY - 2`, while the outside terrain is at `surfaceY`;
- the south breach opens directly into the lowered deck and no ramp/stair returns from the deck to terrain;
- dropping in may be harmless, but a two-block rise cannot be walked back out normally.

Required repair:
- add a wrecked boarding ramp / collapsed hull slope that connects terrain to the central aisle in both directions.

### Cargo Ship — FAIL

Good:
- bridge, cargo bays, engineering and loading lane are spatially distinct;
- circulation spine is wide and readable.

Blocking issue:
- `LegacyNativeStructure` places the ship at `max(86, surfaceY + 24)`;
- there is no generated boarding tower, lift, terrain pillar or ramp down to normal terrain;
- normal survival access therefore requires block placement, flight or an unrelated terrain coincidence.

Required repair:
- either generate the freighter at/near terrain as a landed/grounded ship, or create a guaranteed vanilla-playable boarding structure reaching the ground.

### Underwater Base — PROVISIONAL PASS

Good:
- obvious radial hub topology;
- four pods use guaranteed cardinal doors;
- connecting pressure tubes are explicit;
- south airlock gives a recognisable approach.

Runtime risks:
- verify water updates do not turn intended dry routes into broken partial flooding;
- verify the south airlock is not terrain-sealed against uneven ocean floor;
- verify all four pressure tubes remain connected across chunk borders.

### Mad Scientist Lab — FAIL

Good:
- strong vanilla-like reveal: small house -> hidden larger lab;
- laboratory has a clear three-wide centre route and side functions.

Blocking issue:
- stair centres advance by two blocks in Z per one block of descent (`z = 4 + i * 2`) while each step only creates floor on one Z row;
- this creates unsupported gaps between successive steps rather than a continuous staircase/ramp.

Required repair:
- fill the intermediate Z row for every descent step or replace with real stair blocks over a continuous 3-wide run.

### Android Safehouse — FAIL

Good:
- H-like footprint is a large improvement over a box;
- central atrium, maintenance wing, logistics wing and secure rear room are functionally distinct.

Blocking issue:
- west/east wing centres sit at X -11/+11 with half-width 5, so their nearest walls are X -6/+6;
- connector openings are cut at X -7/+7, which opens the atrium wall but leaves the adjacent wing wall solid;
- both side technology wings are therefore statically disconnected by one block.

Required repair:
- cut matching openings at X -6/+6 or move side-wing centres one block inward.

### Sand Pit / Excavation Site — FAIL

Good:
- terraced dig, crane landmark and buried technology create strong identity;
- first ramp leg is broad and readable.

Blocking issue:
- second ramp leg changes Z by three blocks per descent step while writing only one Z row of floor;
- this leaves multi-block gaps between ramp segments and breaks the intended descent.

Required repair:
- generate a continuous stair/switchback with every intermediate horizontal cell supported and three-block headroom.

## Modern facilities

The redesigned `TechnologyFacilityStructurePiece` primary topology is substantially closer to vanilla structure logic. The main room/corridor offsets generally overlap correctly and use explicit critical paths. However, `TechnologyFacilityStructure` still always assembles `FacilityInfrastructurePiece` and `FacilityTerrainPiece` after the redesigned primary pieces.

Those helper assemblers still reference old layout coordinates and old layout-specific fixes. This makes their source state part of the current audit.

### Synthetic Manufacturing Plant — CONDITIONAL FAIL

Primary route:
`entrance -> security -> manufacturing core -> fabrication/assembly -> shipping`

Good:
- route is readable and production-oriented;
- wing/corridor overlap is coherent;
- shipping is clearly the rear objective/logistics area.

Blocking risk:
- infrastructure still injects old layout-specific service corridors and `PLANT_EAST_ENTRANCE_FIX` coordinates that no longer correspond to the redesigned entrance/wing arrangement;
- old secure gates can be written into the new route at legacy positions.

Required repair:
- rewrite infrastructure assembly from the new plant coordinates, removing old layout-2 corridor fixes entirely.

### Matter Refinery — CONDITIONAL FAIL

Primary route:
`entrance -> security -> refinery core -> excavation / processing / storage`

Good:
- process flow reads logically;
- excavation is intentionally lower and has a dedicated vertical piece.

Blocking risk:
- old `LADDER_UP_12` and lowered-transition helpers still target previous excavation offsets while the new excavation shaft moved to X +/-28 and Y -10;
- helper pieces can intersect the new stepped shaft or create redundant/illogical vertical routes.

Required repair:
- remove ladder-era helper geometry and align support pieces to the new stepped shaft only.

### Quantum Relay Station — CONDITIONAL FAIL

Primary route:
`entrance -> control -> relay core -> power/relay wings -> mast/observation`

Good:
- strong central landmark and clear control-before-core sequencing;
- optional elevated observation fits vanilla side-content principles.

Blocking/logical risk:
- terrain mast foundations remain at old +/-18, Z 11 coordinates while redesigned masts are at +/-22, Z 16;
- this creates support plinths under empty space and leaves actual masts without the intended foundations;
- old secure gate coordinates also target the previous wing geometry.

Required repair:
- move mast foundations and all gates/service access to the new relay coordinates.

### Android Command Bunker — CONDITIONAL FAIL

Primary route:
`surface entrance -> descent -> security -> command -> drone/android bays -> armoury`

Good:
- hierarchy is clear and vanilla-like;
- critical path has an explicit descent rather than a conceptual underground jump.

Blocking risk:
- the old `LADDER_DOWN_7` and terrain `BUNKER_ANDROID_LINK_Z` pieces are still assembled even though the redesigned entrance now owns its own stepped descent and the deployment wings moved;
- these helpers can overlap the new stair volume or create dead/meaningless side connectors.

Required repair:
- remove obsolete ladder/link pieces and generate terrain approach solely around the new stepped entrance.

### Fusion Research Complex — CONDITIONAL FAIL

Primary route:
`entrance -> reactor control -> circular fusion core -> stabilizer/service wings`

Good:
- strongest primary layout in the modern set;
- reactor chamber is a true focal objective;
- cardinal doors and large combat floor satisfy the new standard well.

Blocking risk:
- old service spine, service access, bridge link, lowered-step and secure-gate helpers remain positioned for the previous geometry;
- some are now decorative at best and route-overwriting at worst.

Required repair:
- rebuild helper assembly specifically around the new observation bridge and reactor-control topology, keeping only pieces that add reachable optional traversal.

### Black Site — CONDITIONAL FAIL

Primary route:
`concealed entrance -> security -> command -> lab/containment -> lower vault`

Good:
- progression ordering is excellent;
- the redesigned primary generator now includes a guaranteed lower-level shaft and lower corridor.

Blocking risk:
- legacy `LADDER_DOWN_12`, `BLACK_SURFACE_HATCH`, `BLACK_VAULT_STAIR` and layout-2 lower corridor are still assembled using coordinates from the old Black Site;
- the old vault stair overlaps the new shaft region and the old lower corridor targets Y -6 / Z 19 while the new vault is at Y -8 / Z 31;
- this can produce contradictory vertical routes and dead architecture.

Required repair:
- retire the old Black Site traversal fixes and create one surface hatch/approach aligned to the new entrance plus one lower route aligned to the new vault.

## Frontier structures

### Deep Matter Vault — FAIL

Good:
- underground critical path itself is coherent: entry -> shaft -> security -> core -> archive/refinery;
- core has multiple doors and cannot become a trap.

Blocking issue:
- structure origin is `surfaceY - 22`;
- the entry centre is only `origin + 14`, placing it around `surfaceY - 8`;
- no surface approach piece exists;
- a structure advertised as having a surface entry is therefore buried approximately eight blocks below normal terrain.

Required repair:
- raise the entry to terrain level and extend the shaft accordingly, or add a dedicated chunk-safe surface access building connected to the existing underground entrance.

### Autonomous Drone Foundry — PROVISIONAL PASS

Good:
- surface entrance at terrain height;
- control precedes fabrication;
- hangar is larger than normal rooms;
- salvage is optional side content;
- critical route and optional branch are legible.

Runtime risks:
- verify hangar/corridor openings remain clear around spawner placement;
- verify salvage branch reconnects naturally and does not visually read as mandatory.

### Anomaly Quarantine Site — PROVISIONAL PASS

Good:
- entry is approximately at surface level;
- decon -> security -> containment sequence is excellent environmental logic;
- containment is a large focal room with four cardinal openings;
- observation is side content rather than the only route.

Runtime risks:
- shaft extends above the entry level and should be checked for odd exposed geometry;
- verify surface terrain does not cut through the entry/shaft join.

### Orbital Recovery Array — PROVISIONAL PASS

Good:
- surface access is direct;
- control comes before processing/storage;
- recovery array is an unmistakable final landmark;
- side wings are optional and readable;
- large exterior objective is vanilla-like in exploration flow.

Runtime risks:
- verify catwalk is optional and not required for completion;
- verify tall array pieces remain visually intact across chunk boundaries.

## Cross-family systemic findings

### 1. Stale helper-piece problem — BLOCKING

The largest systemic issue is that the primary modern facility redesign was not accompanied by a rewrite of `FacilityInfrastructurePiece` and `FacilityTerrainPiece`. Since all three families are assembled together, old helper offsets can invalidate otherwise-correct new geometry.

### 2. Surface-access contract is not universally enforced — BLOCKING

Cargo Ship and Deep Matter Vault demonstrably violate the requirement that normal terrain connects to the structure without player block placement. Crashed Ship also lacks a guaranteed return transition from its lowered deck.

### 3. Continuous stair/ramp generation needs a shared primitive — BLOCKING

Mad Scientist Lab and Sand Pit independently implement stepped routes and both create discontinuous support geometry. A shared 3-wide stair/ramp helper should replace bespoke arithmetic in future structures.

### 4. Static route validation is possible and should be added

The current structure code already has deterministic room origins. A lightweight development validator can define required connector endpoints per layout and assert that adjacent required pieces overlap/touch on compatible walkable elevations. It cannot replace runtime traversal, but it can catch off-by-one errors such as the Android Safehouse wings before launching Minecraft.

## Audit verdict

The visual redesign is a substantial improvement and several structures now have good vanilla-like spatial sequencing. However, the new standard is intentionally stricter than "looks better" or "generates without crashing".

Current verdict: **FAIL — repair required before the structure set can be called vanilla-standard compliant.**

Priority repair order:
1. Remove/rewrite stale modern facility helper offsets.
2. Fix Cargo Ship and Deep Matter Vault surface access.
3. Fix Android Safehouse wing connectors.
4. Replace Mad Scientist and Sand Pit discontinuous stairs/ramps.
5. Add Crashed Ship terrain-return ramp.
6. Run the fresh-world three-start-per-family runtime playthrough gate.
