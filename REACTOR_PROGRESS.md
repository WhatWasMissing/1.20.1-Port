# Reactor Progress

## Current work

- The Fusion Reactor Assembly Guide provides four build/operation pages with resized diagrams and an in-game overlay legend.
- When enabled, the client renders the complete reactor outline continuously for loaded reactors within 128 blocks; it no longer requires the crosshair to remain on the controller.
- The world overlay uses green for a correctly filled position, red for a wrong block, blue for required hull, orange for required coil/IO, purple for the flexible controller-side position, and cyan for the anomaly centre/range.
- The overlay can be enabled or disabled by interacting with the reactor controller while holding the Assembly Guide. The controller stores the setting in block-entity data and synchronizes it to clients.
- Structure validation and reactor operation remain server-side; the overlay is visual guidance only.

## Usage

1. Hold the Fusion Reactor Assembly Guide and look directly at the reactor controller.
2. Right-click the controller with the guide to toggle the persistent overlay. The action bar reports ON/OFF.
3. Move, look away, or switch items; the complete outline remains visible while the controller is loaded and nearby.
4. Use the color legend: blue hull, orange coil/IO, purple flexible side, cyan anomaly, green correct, red incorrect.


## Latest implementation

- Reactor component models are now visually distinct: coils use `base_coil`, the controller uses `screen`, and reactor IO uses `network_port`.
- The controller GUI now reports the number of active stabilizers suppressing the linked anomaly.
- The reactor menu was extended and its inventory slots moved down to keep the new stabilizer status line readable.
- Next runtime test: power one and then four stabilizers with redstone, confirm the active-stabilizer count and reduced safe mass, while unsuppressed mass and energy output remain unchanged.
- Build and in-game verification are still pending for this commit.


## Stabilizer placement rework

- Stabilizer placement now prefers an aligned gravitational anomaly within 63 blocks and otherwise uses the player’s horizontal facing, preventing accidental downward-facing floor placement.
- The stabilizer model now has a distinct blue emitter face and contrasting sides, making the beam direction visible in-world.
- Placement reports the front direction and reminds the player that the beam travels forward and requires redstone power.
- Blocked and unsuccessful scans now include the facing direction in their status message.
- Next runtime test: place a stabilizer on the floor near the reactor, confirm its emitter face points at the anomaly, power it, and verify the controller’s active-stabilizer count increases.
- Build and in-game verification are pending for this commit.


## Stabilizer targeting correction

- Stabilizer beam obstruction now uses the target block’s collision shape, so solid blocks that do not advertise vanilla occlusion cannot be scanned through.
- Sneak-placement bypasses automatic anomaly targeting and uses the player’s horizontal facing, allowing deliberate placement away from an anomaly for negative tests.
- Sneak-right-click rotates an existing stabilizer through the four horizontal directions and reports the new beam direction.
- Blocked-beam messages now report the blocking distance and direction.
- Next test: rotate a powered stabilizer away from the anomaly and confirm it stops suppressing; place a solid block in its beam and confirm the blocked distance is reported.


## Reactor matter input recovery

- Reactor IO now actively pulls matter from connected matter sources through Matter Pipe and Heavy Matter Pipe routes, rather than relying only on decomposer push timing.
- Input and output route cursors are separate and persisted, preventing a rebuilt route from leaving the controller short by one matter unit.
- The pull path ignores other reactor IO blocks so multiple IOs cannot feed matter back into one another.
- Runtime test passed: a powered Decomposer connected through a matter pipe to the reactor IO refills the reactor after draining it to 2047 kM, including after breaking and replacing a pipe, without replacing the IO.
- For an exact 2048 kM reading, pause generation or test while the reactor is not consuming matter on the same tick.


## Next reactor milestone

- Begin gameplay parity for the gravitational anomaly: expose controlled range/effect diagnostics first, then add entity effects while keeping block destruction disabled until it has a separate safety test.

## Gravitational anomaly entity effects

- The anomaly now applies a capped inward force to nearby living entities, including players and mobs.
- The effect range uses suppressed mass, so powered stabilizers reduce both anomaly danger and the active pull range.
- The controller GUI now reports a dedicated `[DEBUG] Pull radius` line with the current effective radius and affected living-entity count.
- Block destruction is still disabled; this milestone is limited to observable, reversible entity movement.
- Next test: place a player or mob at several distances from the anomaly, confirm inward movement while unsuppressed, then power stabilizers and confirm the pull range and force decrease.
- Build and in-game verification are pending for this commit.


## Space-Time Equalizer

- The existing Space-Time Equalizer is now a real chest-slot wearable rather than a generic item.
- Anomaly gravity ignores living entities wearing it, including players and mobs.
- The item uses the existing Space-Time Equalizer armor texture and tooltip.
- Next test: spawn or position a player/mob inside the pull radius, compare movement without the Equalizer, then equip it in the chest slot and confirm the entity no longer accelerates toward the anomaly.
- Build and in-game verification are pending for this commit.


## Anomaly block hazard diagnostics

- The reactor GUI now exposes the calculated block-effect radius separately from the living-entity pull radius.
- Block hazard is explicitly shown as DISABLED; this diagnostic milestone makes no world-changing block edits.
- Next test: compare pull radius and block radius with zero and four powered stabilizers, then confirm both values respond to suppression while the disabled state remains unchanged.


## Stabiliser radius test passed

- With zero active stabilisers, the reactor GUI reported a pull radius of approximately 7.59 blocks and a block radius of approximately 3.79 blocks.
- With four active stabilisers, the pull radius dropped to approximately 3.72 blocks and the block radius to approximately 1.86 blocks.
- Reactor structure remained valid, matter remained at 2048/2048 kM, output remained 177 FE/t, and block hazard remained DISABLED.
- This confirms stabiliser suppression changes anomaly effect radius without reducing reactor output.
- Next test: verify the Space-Time Equalizer prevents pull on a player or mob, then continue with controlled anomaly hazard design.


## Original anomaly parity

- Added the original Matter Overdrive event-horizon calculation: `max((2 * G * realMass) / c², 0.5)`.
- The reactor debug line now reports the event-horizon radius separately from pull and block-effect radii.
- The event horizon is diagnostic-only in this milestone; entities are not consumed and block destruction remains disabled.
- Next test: compare the horizon line at zero and four powered stabilisers, confirm it changes with suppression, and verify normal pull behavior remains unchanged.
- Build and in-game verification are pending for this commit.


## Debug matter tools

- Added a creative/debug matter container with a 1,000,000 kM capacity for fast reactor and machine testing.
- Added a client-side Shift tooltip for every item, including block items, showing its registered matter value per item; unknown items display 0 kM.
- The normal matter container remains capped at 1,000 kM.
- Next test: obtain the debug matter container, transfer matter into a reactor or machine, and hold Shift over several known and unknown items to verify values.


## Debug matter capability correction

- Corrected the debug container capability provider to receive its 1,000,000 kM capacity explicitly.
- Build verification is pending on GitHub Actions.


## Debug matter build fix

- Removed a duplicate matter capability field introduced while generalizing the container capacity.
- The attached local verification log confirmed all M1/M2 gates and found this final compile error; the corrected commit is ready for rebuild.


## Million-matter debug block

- Added a placeable `Debug Matter Block` block and block item for anomaly testing.
- Its item form is registered at 1,000,000 kM in `MatterValueRegistry`, so an item entity consumed by the anomaly increases anomaly mass by 1,000,000 kM.
- Use `/give @p matteroverdrive:debug_matter_block`, place it, break it, and let the dropped block enter the anomaly.
- This is intentionally a debug/testing item and is not part of reactor structure requirements.


## Reactor FE demand debug

- Added the current connected FE demand to the Fusion Reactor Controller debug GUI.
- The value is refreshed from connected FE receiver capacity when the reactor outputs, allowing comparison of reactor output against network demand.
- Next test: connect one or more machines, observe demand while idle and processing, then compare it with reactor output.


## Full network FE demand scan

- Replaced the first-endpoint demand estimate with a breadth-first scan from all reactor IO positions through connected energy pipes.
- Unique receiver machines are counted once, and demand is calculated as free FE capacity (`max energy - stored energy`).
- The controller GUI demand value now represents the combined connected network demand, including direct IO connections and chained pipes.
- Next test: connect multiple machines through different IOs and cable branches, compare the combined demand, then fill one machine and confirm the total decreases.


## FE demand scan correction

- Corrected demand accumulation so direct IO receivers and receivers reached through cable branches contribute to the same total.
- The full-network demand value is now ready for compilation and runtime verification.


## Live FE/t demand correction

- Changed reactor network demand from total empty storage capacity to simulated FE acceptance per tick.
- Each unique connected machine is queried with a simulated maximum receive operation, so its configured input-rate limit and current fullness determine its displayed demand.
- The GUI demand value now answers how much FE/t the connected network can accept now, rather than how many FE it would take to fill every buffer.
- Next test: compare an idle/full machine, an empty machine, and multiple active machines; demand should rise and fall with their current acceptance.


## Actual reactor network FE usage

- Replaced simulated FE acceptance with measured internal machine work, so filling an empty machine buffer no longer appears as reactor demand.
- Matter Overdrive machine energy storage now records FE explicitly consumed for processing during the current server tick; ordinary charging, cable relay, and battery transfer are excluded.
- The reactor scans every unique connected Matter Overdrive machine and totals its current/recent processing usage. The previous-tick fallback prevents the display from flickering because of block-entity tick order.
- The controller GUI label is now `usage: N FE/t`.
- Next test: connect an idle empty Decomposer and confirm usage remains 0 while its buffer charges; start decomposition and confirm usage matches the Decomposer drain; add a second active machine and confirm both usages are summed.


## Functional anomaly event horizon

- Compared the port with the original 1.12.2 Gravitational Anomaly logic and promoted the event horizon from a diagnostic value to the real entity-absorption boundary.
- Item entities are now consumed when they enter the calculated, stabilizer-suppressed event horizon instead of a fixed one-block radius. Their registered matter value still increases anomaly mass.
- Living entities inside the horizon take capped magic damage every five ticks; actual health removed is added to anomaly mass. Normal death handling remains intact rather than forcibly deleting players or mobs.
- A worn Space-Time Equalizer now protects against both gravitational pull and event-horizon damage.
- Added synchronized horizon occupancy and last-feed telemetry to the controller. The controller GUI is widened and its inventory repositioned so reactor diagnostics no longer spill far outside the panel.
- Block destruction remains disabled and is reserved for a separate default-off safety milestone.
- Next test: compare horizon distance with zero/four stabilizers, feed a normal item and the million-matter debug block, place a mob inside the horizon, then repeat while wearing the Space-Time Equalizer.

## Internal reactor ring power bus

- Validated reactors now discover FE-capable machines inside the horizontal ring on the controller's Y level and share available reactor FE directly without a fixed per-machine ceiling.
- The enclosed footprint follows the rounded ring: local forward rows 1/9 use lateral -2..2, rows 2/8 use -3..3, and rows 3..7 use -4..4.
- A Decomposer placed in either flexible controller-side ring slot at local (+1, 0) or (-1, 0) also joins the internal bus without a cable.
- Internal receivers are served in a rotating deterministic order so a low-energy reactor does not permanently favor the same machine.
- The controller debug GUI now reports discovered internal machines and FE actually sent to them during the latest tick; this transfer may exceed 512 FE/t.
- Internal machines are included in the existing actual-processing `usage: N FE/t` total and are deduplicated if a cable also reaches them.
- Reactor IO now starts external FE transfer only through an adjacent Heavy Energy Cable. A machine directly beside IO but outside the ring is intentionally not powered.
- Controller and IO sided FE capabilities no longer expose a cable-free external bypass; matter IO behavior is unchanged.
- Runtime verification steps are in `REACTOR_RING_POWER_TESTING.md`.

## Mass-scaled reactor output correction

- Removed the artificial 512 FE/t ceiling from both internal ring delivery and Reactor IO cable-routed delivery.
- Internal machines now share the reactor's available stored FE fairly, with the starting receiver rotated each tick.
- Reactor IO still requires an adjacent Heavy Energy Cable before it can reach outside machines, and duplicate endpoints in a cable network are transferred to only once per IO tick.
- Added one controller transfer helper so simulation, extraction, receiver acceptance, and refund behavior are identical for internal and external power.
- The GUI now separates **Output capacity** (the mass-scaled `2048 x efficiency x unsuppressed mass` rate) from **generated** (FE actually accepted into reactor storage this tick).
- This matches the original controller's separation between mass-scaled `energyPerTick` and high-rate extraction from reactor storage, while retaining the port's tested cable boundary.
- Added Test 8 to `REACTOR_RING_POWER_TESTING.md` for anomaly-mass scaling above 512 FE/t.
