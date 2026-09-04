# Consolidated Runtime Testing Checklist

Branch: `testing/alpha`
Legacy reference: MatterOverdrive 1.12.2 `0.7.1.0` jar plus recovered source/resources

Use this after a fresh pull and normal M2 client launch. Record the expected result, actual result, coordinates/orientation and relevant log lines for every failure.

The current parity batch compiles/packages successfully in GitHub Actions, but the items below still require in-game verification.

## Build and runtime gate

- [ ] Reach the main menu without registry, datapack, model, block-entity, entity or menu errors.
- [ ] Create/load a test world successfully and confirm no missing-registry warnings appear for existing test saves.
- [ ] Confirm the login marker reports `Alpha Version 3` and `Made by MVQ1303`.
- [ ] Confirm the Holo Sign block entity and restored legacy entity types register without client/server class-loading errors.
- [ ] Confirm a dedicated server can start without attempting to load client render classes.
- [ ] Run the normal M2 runtime/build checks and record any changed registry totals rather than relying on older `testing/main` counts.

## In-game documentation

- [ ] Obtain the M2 Testing Checklist, Current Feature Reference and Matter Overdrive System Guide items.
- [ ] Right-click each item and confirm it opens the correct paged document.
- [ ] Compare this checklist with the in-game Testing Checklist and `docs/testing/TO_TEST.md`.
- [ ] Compare the Current Feature Reference with `docs/reference/WORKING_FEATURES.md`.
- [ ] Verify Previous/Next, Page Up/Page Down, Left/Right, mouse wheel, index navigation, Done/Escape and multiple GUI scales.
- [ ] Close and reopen a document and confirm its last-opened page is remembered where persistence is expected.
- [ ] Confirm the newly restored Security/Holo Sign, Rogue Android/failed-animal and Mad Scientist/Puny Humans sections are visible in game.

## Security Protocol and machine ownership

- [ ] Obtain `security_protocol_empty`, `security_protocol_claim`, `security_protocol_access` and `security_protocol_remove`.
- [ ] Sneak-use an unbound protocol in the air. Confirm it binds to the player and becomes Claim mode.
- [ ] Continue sneak-using it and confirm the bound modes cycle Claim -> Access -> Remove -> Claim while retaining the same owner UUID.
- [ ] Confirm another Survival player cannot rebind a protocol owned by someone else.
- [ ] Apply a bound Claim protocol to several Matter Overdrive block-entity machines. Confirm the machine becomes claimed and one Claim protocol is consumed.
- [ ] Confirm the owner can open/use and break a claimed machine normally.
- [ ] Confirm a non-owner without a matching Access protocol cannot use or break the claimed machine.
- [ ] Give the second player an Access protocol bound to the owner's UUID and confirm use/break access is granted while it remains in inventory.
- [ ] Confirm an Access protocol for a different owner does not grant access.
- [ ] Apply a matching Remove protocol and confirm security is removed and one Remove protocol is consumed.
- [ ] Confirm mismatched/unbound Remove protocols cannot clear ownership.
- [ ] Save/reload the world and confirm machine ownership and protocol owner NBT persist.
- [ ] Test security on representative systems: crate, Decomposer, Replicator, Pattern Storage, Network Router, Transporter, Weapon Station, Android Station, Reactor Controller/IO and Holo Sign.

## Holographic Sign

- [ ] Place a Holo Sign and confirm its model renders without opaque/transparency artefacts.
- [ ] Rename an item in an anvil, then sneak-use that item on the Holo Sign. Confirm the renamed text becomes the holographic message.
- [ ] Confirm the text renders full-bright, remains readable from sensible angles and scales down instead of overflowing for long text.
- [ ] Sneak-use with an empty hand and confirm the sign clears.
- [ ] Normal-use the sign and confirm its current text/status message is reported.
- [ ] Save/reload, leave/re-enter the chunk and reconnect to the world/server; confirm text persists and resynchronises to clients.
- [ ] Test a long message near the 256-character cap and confirm no crash, packet overflow or visual corruption.
- [ ] Claim the Holo Sign with a Security Protocol and repeat editing/break/access tests with owner and non-owner players.

## Restored Rogue Android entity

- [ ] Confirm the Android Spawner now creates `matteroverdrive:rogue_android`, not a tagged vanilla Husk.
- [ ] Confirm the spawner searches collision-free locations and consumes 20,000 FE only after a spawn succeeds.
- [ ] Confirm natural Rogue Android spawning occurs only in valid hostile-spawn conditions and is not excessive.
- [ ] Inspect several Rogue Androids and confirm levels 0-3 occur and names match `Rogue Android [Lv N]`.
- [ ] Find or summon a Legendary Rogue Android and confirm the legendary name/state persists through chunk unload/reload.
- [ ] Verify base movement speed/follow behaviour feels consistent with the intended 0.30 movement speed and 24-block follow range.
- [ ] Verify normal health scales from 32 + 10 per Android level and melee damage from 4 + level; Legendary should use the stronger restored values.
- [ ] Confirm Rogue Androids are not sun-sensitive and reject potion effects as intended.
- [ ] Confirm original Rogue Android ambient/death sounds play.
- [ ] Kill multiple Rogue Androids and confirm bionic-part drops still work.
- [ ] Load an older world containing the previous tagged-Husk workaround and confirm it does not crash or invalidate the save.

## Failed animals

- [ ] Summon `matteroverdrive:failed_cow`, `failed_pig`, `failed_sheep` and `failed_chicken`.
- [ ] Confirm each uses its Matter Overdrive texture rather than the vanilla animal texture.
- [ ] Confirm movement, collisions, dimensions and basic passive-animal behaviour are sane.
- [ ] Confirm the restored failed-animal idle/death sounds play for the appropriate species.
- [ ] Save/reload with each failed animal present and confirm entities persist without registry or renderer errors.

## Mad Scientist and Puny Humans quest

- [ ] Summon `matteroverdrive:mad_scientist` and confirm the legacy scientist texture renders correctly.
- [ ] Spawn several and confirm the normal/Junkie state and display naming persist across save/reload.
- [ ] As a non-Android player, interact with a Mad Scientist and confirm `Puny Humans` starts with objective: become an Android, then return.
- [ ] Interact again before conversion and confirm the objective reminder appears without duplicating rewards or quest state.
- [ ] Become an Android, return to a Mad Scientist and confirm completion occurs once.
- [ ] Confirm the exact current reward bundle is one Battery, one Blue Android Pill and five Yellow Android Pills.
- [ ] Confirm the quest-start and quest-complete sounds play.
- [ ] Confirm a full inventory safely drops rewards rather than deleting them.
- [ ] Relog/death between starting and completing and confirm player-persistent quest state survives.
- [ ] Interact again after completion and confirm rewards cannot be claimed twice.
- [ ] Note: deeper legacy Mad Scientist dialogue/quest chains, including Cocktail of Ascension and Mutant Scientist progression, are not part of this test build yet.

## Android system and selectable perk tree

- [ ] Convert and confirm persistent Android FE/state, HUD and XP/level progression.
- [ ] Install Head, Chest, Arms and Legs and verify Cloak, Force Field, Sonic Shockwave and Ender Teleport gating.
- [ ] Use/rebind `V` cycle and `B` activate; verify selection, cooldown and persistent toggle indicators.
- [ ] Press `K` and verify the three-column, ten-level selectable perk tree.
- [ ] Confirm one point per reached level and that selections persist through level-up, relog, death and screen close.
- [ ] Test individual 2,500 FE refunds and the confirmed 25,000 FE full reset.
- [ ] Sneak with a charged Battery/HC Battery and verify actual stored FE transfers at the expected rate without creating energy.
- [ ] Drain Android FE to zero and verify the offline HUD state and 50% movement penalty recover after recharge/deactivation.
- [ ] Verify direct-melee Arms bonus, Force Field final-damage reduction, Shockwave cost/filtering and collision-safe Teleport.

## Fusion Reactor and anomaly

- [ ] Build the horizontal ring in all four controller facings and verify structure faults/overlay positions.
- [ ] Verify anomaly offset efficiency, Range upgrades and the current 16-block discovery cap.
- [ ] Verify potential and actual FE/t against the displayed formula and confirm matter use scales with accepted generation.
- [ ] Test direct FE receiver, one cable, 5+ cable chains, cable rebuild and multiple formed IO outputs.
- [ ] Verify shared ring FE, internal machine distribution, connected-demand telemetry and stabilizer power integration/upgrades.
- [ ] Test persistent RUN/SCRAM, redstone modes, comparator output and Reactor Remote controls.
- [ ] Feed dropped items and kill living entities in the event horizon; confirm mass is added once and persists.
- [ ] Verify pull, Space-Time Equalizer immunity, block/fluid hazard, stabilizer beam/suppression and acceptable tick time.
- [ ] Re-test output across several anomaly masses to ensure reactor generation is not stuck at an old fixed cap.

## Weapons and Weapon Station

- [ ] Phaser, Phaser Rifle, Ion Sniper, Plasma Shotgun and Omni Tool fire only when their own valid energy path can pay the full shot cost.
- [ ] Confirm empty weapons cannot drain unrelated weapons in the inventory.
- [ ] Verify Battery/HC Battery transfer only real FE and remain rechargeable; Energy Packs remain consumable.
- [ ] Test heat, overheat, reloads, cooldowns and all implemented barrel/sight/ricochet/colour modules.
- [ ] Test first- and third-person held placement, aiming, firing and module rendering. Record any remaining visual mismatch with the 1.12.2 jar.
- [ ] Verify Weapon Station contents/modules persist across reload and return safely on block break.

## Matter machines and networks

- [ ] Decomposer, Recycler, Analyzer, Pattern Storage, Pattern Monitor and Replicator complete their normal FE/matter workflows.
- [ ] Verify Pattern Drives, Scanner-created pattern progress, queue routing and failure behaviour.
- [ ] Test Matter Pipes and Heavy Energy Cables across short and long chains, break/rebuild and multiple endpoints.
- [ ] Test Network Pipe/Switch/Router/Pylon traversal, filtering and anti-bounce behaviour.
- [ ] Confirm machine inventories, matter, FE and upgrades persist through save/reload and return safely where expected on break.

## Power, transport and utility machines

- [ ] Solar Panel generates/exports FE correctly.
- [ ] Charging Station charges compatible batteries/items without duplication.
- [ ] Microwave accepts food-smelting inputs only and obeys FE/timing/upgrades/output blocking.
- [ ] Space-Time Accelerator requires FE and matter, accelerates targets symmetrically and respects redstone/upgrades/range.
- [ ] Transporter/Transport Flash Drive bind and transport correctly within supported limits.
- [ ] Tritanium Crates retain 54-slot contents in-world and as dropped items.

## Survival/world generation and visual regression

- [ ] In fresh Overworld chunks confirm Tritanium and Dilithium ores generate in their documented ranges.
- [ ] Smelt/blast ores into intended resources and verify survival recipes still load.
- [ ] Confirm Industrial Glass and other translucent blocks render transparently.
- [ ] Check Tritanium Crate and Inscriber UV/model alignment.
- [ ] Check equipped Tritanium armour textures and held weapon transforms.
- [ ] Verify original Rogue Android, Mad Scientist and failed-animal textures load without purple/black missing textures.

## Contracts, Data Pad and Star Map

- [ ] Contract collect/hunt progress advances exactly one matching contract and rewards redeem once.
- [ ] Verify Contract Market refresh timing and persistence.
- [ ] Confirm the Data Pad guide/history works and persists.
- [ ] Confirm the Star Map contract-status screen is viewer-specific and stable.
- [ ] Do not treat the current Star Map as full galaxy simulation parity; galaxy/star/planet gameplay remains future work.

## Pass criteria

The alpha build is ready for the next parity pass when there are no new crashes, registry/datapack failures, item loss/duplication, ownership bypasses, free-energy/free-matter exploits or save corruption, and the new Holo Sign, security protocols, restored entities and Puny Humans progression have all been verified in a fresh world and after save/reload.
