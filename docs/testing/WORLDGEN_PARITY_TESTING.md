# Matter Overdrive 1.20.1 - Worldgen Parity Regression

Legacy authorities:
- Matter Overdrive 1.7.10 0.4.2
- Matter Overdrive 1.12.2 0.7.1.0

The legacy world structures were primarily MOImageGen PNG-template structures. This pass translates their recovered logical envelopes and known machine/block roles into the 1.20.1 feature system; it does not claim byte-for-byte PNG placement parity.

The port intentionally uses **legacy identity + modern execution**. Recovered footprints, offsets, machine roles, loot themes and occupants are preserved, while 1.20.1 placement is allowed to reject clearly unsuitable terrain and keep entrances usable rather than blindly stamping a legacy template.

## Recovered geometry targets

- Android House: 21 x 21, legacy yOffset -2.
- Sand Pit: 24 x 24, legacy yOffset -9, airLeeway 3 and sand-site gating.
- Crashed Ship: 11 x 35.
- Cargo Ship: 58 x 23.
- Underwater Base: 43 x 43.
- Mad Scientist House: approximately 9 x 9 x 6 village-piece envelope.

## Modern placement layer

- [ ] Android House rejects severe terrain height changes instead of spanning cliffs/large ravines.
- [ ] Mad Scientist House rejects severe local terrain height changes.
- [ ] Crashed Ship and Cargo Ship tolerate natural terrain variation but reject obviously unsupported sites.
- [ ] Terrain checks do not prevent normal generation on ordinary plains/rolling terrain.
- [ ] Android House front doorway and two-block approach remain clear after placement.
- [ ] Mad Scientist House front doorway and two-block approach remain clear after placement.
- [ ] Entrance cleanup never removes Bedrock.
- [ ] Underwater Base remains governed by its deep-water requirement rather than generic land checks.
- [ ] Sand Pit remains governed by its recovered sand-site/corner leeway checks rather than generic land checks.

## Android House

- [ ] Generate only in fresh/new chunks.
- [ ] Confirm the physical footprint is 21 x 21 rather than the previous ~19 x 19 translation.
- [ ] Confirm the structure is sunk by the recovered -2 Y offset without burying its interior.
- [ ] Confirm the recovered machine-role palette appears: Star Map, Replicator, Network Switch/Pipe, Charging Station, Pattern Monitor, Tritanium Crates and Android Station or Weapon Station.
- [ ] Confirm 3-4 mixed Rogue Android defenders appear and persist.
- [ ] Confirm the optional hostile unowned Drone does not count as a linked player Drone.
- [ ] Confirm structure mobs do not duplicate after save/reload or chunk unload/reload.
- [ ] Confirm both salvage crates retain their generated contents after reload.

## Sand Pit

- [ ] Generate in fresh sandy terrain only.
- [ ] Confirm the footprint is 24 x 24 rather than the previous ~32 x 32 bowl.
- [ ] Confirm the deepest wreck/floor reaches the recovered -9 offset.
- [ ] Confirm corner terrain gating tolerates only the recovered small air/height leeway and rejects unsuitable terrain cleanly.
- [ ] Confirm the buried Tritanium/coil wreck and salvage crate are reachable.
- [ ] Confirm the Android guardian persists and the optional hostile Drone remains unowned.
- [ ] Confirm no fluid/terrain update creates a runaway falling-sand or repeated-generation loop.

## Crashed Ship

- [ ] Confirm an 11 x 35 envelope in new chunks.
- [ ] Confirm damaged/broken hull sections still read as a crash rather than a complete ship.
- [ ] Confirm salvage crate and Holo Sign persist.
- [ ] Confirm 1-2 mixed Rogue Android defenders persist and do not duplicate.

## Cargo Ship

- [ ] Confirm the translated hull occupies the recovered 58 x 23 envelope.
- [ ] Confirm interior route remains traversable after the footprint increase.
- [ ] Confirm Transporter, Network Switch/Pipe and cargo/salvage placements are present.
- [ ] Confirm 2-4 mixed Rogue Android defenders persist.
- [ ] Confirm optional hostile Drone remains unowned and does not duplicate.

## Underwater Base

- [ ] Generate in suitable deep-water/deep-ocean chunks only.
- [ ] Confirm the base occupies the recovered 43 x 43 envelope rather than the previous ~21-block diameter translation.
- [ ] Confirm the sealed interior is dry after generation and after chunk reload.
- [ ] Confirm Matter Analyzer, Pattern Storage/Monitor and salvage crates are reachable.
- [ ] Confirm the persistent hostile Drone and optional ranged Rogue Android do not duplicate.

## Mad Scientist House

- [ ] Confirm the house is approximately 9 x 9 x 6 rather than the previous 11 x 11 shell.
- [ ] Confirm Inscriber, Decomposer and scientist salvage crate are reachable.
- [ ] Confirm exactly one persistent Mad Scientist is created by initial feature placement.
- [ ] Confirm the optional Failed animal persists and does not duplicate.

## Whole-worldgen regression

- [ ] Existing chunks are not retroactively regenerated.
- [ ] New chunks can still generate all six restored structure types.
- [ ] Tritanium and Dilithium ore generation remains unaffected.
- [ ] Natural Gravitational Anomalies still generate independently at the configured conservative rate.
- [ ] Natural anomaly starting mass remains 2,048-10,240.
- [ ] Structure placement never embeds persistent occupants inside solid blocks.
- [ ] Structure occupants are not registered as Android-Spawner-owned units.
- [ ] Generated Drones begin unowned.
- [ ] Generated crates retain structure-specific loot through save/reload.
- [ ] Breaking/replacing blocks in a generated structure does not cause the feature to regenerate.
- [ ] Re-entering a generated chunk does not spawn another occupant wave.
- [ ] World save/reload with multiple generated structures produces no worldgen exceptions or registry errors.

BUILD VERIFIED is only a compile/resource gate. Geometry, spawn suitability and persistence above require runtime verification in newly generated chunks.
