# Facility Population / HUD Separation Pass

Date: 2026-09-12
Branch: `npc-structure-hud-pass`
Base: `testing/tech-overhaul` @ `1ee988c163cd5754d0f2b0b48c9ef08fb9b0f975`

## Scope

This pass addresses three immediately player-visible issues:

1. Android ability HUD obscuring vanilla chat and PDA/status messages.
2. Technology facilities lacking persistent human NPC presence.
3. Technology facilities reading as visually sparse despite already having correct native StructurePiece generation and MO loot plumbing.

## Android HUD

`AndroidHudOverlay` is no longer fixed to the lower-left chat quadrant. The 252x148 panel is anchored to the right side and vertically centered with safe edge clamping, preserving the lower-left for vanilla chat and PDA/system text across GUI scales.

## Facility NPCs

Added three persistent neutral NPC archetypes:

- Field Researcher
- Systems Engineer
- Facility Security

All three render through the standard 64x64 wide-arm Steve model. Their skins are original Matter Overdrive designs generated client-side rather than copied third-party skin files:

- Researcher: pale lab coat, dark technical undersuit, cyan interface optics, orange MO accent.
- Engineer: graphite work suit, orange harness/utility markings, cyan interface light, reinforced work areas.
- Security: dark navy/graphite protection, plate carrier, cyan optics, red warning accent.

Staff use lightweight ambient pathfinding and are deliberately not Villagers, preventing Rogue Android zombie targeting rules from immediately removing the intended facility population. Right-clicking a staff member gives role-specific facility flavor/status information.

## Facility population and dressing

Added `FacilityPopulationPiece`, a serialized, chunk-clipped native StructurePiece family. It is appended after room, infrastructure, and terrain pieces, preserving the safe modern generation architecture.

Population piece kinds:

- `RESEARCH_POST`
- `ENGINEERING_POST`
- `SECURITY_POST`
- `LOGISTICS_CACHE`
- `ARMORY_CACHE`

The six modern facilities receive layout-aware population nodes matching their existing room locations. These nodes add additional MO machinery, screens, vents, coils, security dressing, and multiple additional Tritanium crates without blocking the main room/corridor generation pass.

All added Tritanium crates are seeded with the existing Matter Overdrive facility-specific loot tables. This deliberately reuses the current research reward/loot system rather than introducing vanilla chest loot or unrelated external loot tables.

The Black Site is treated as an abandoned/hostile exception: it receives research evidence, security remains, and high-value cache dressing, but does not spawn friendly staff inside active containment/security threats.

## External visual/layout research policy

Sci-fi Minecraft lab/space/industrial references were reviewed for useful visual motifs: white research clothing, dark industrial/security clothing, reinforced lab shells, screens, vents, catwalks, control stations, and compartmentalized loot/storage rooms.

No third-party skin image, NBT structure, schematic, or copyrighted build asset was copied into this pass. This is intentional because licensing differs between mods and asset classes. The resulting layouts and skins are original Matter Overdrive implementations while retaining familiar futuristic design language.

## Runtime verification required

Generate **new chunks** for each test. Existing generated structures will not be retroactively populated.

- [ ] Open chat with Android HUD active at multiple GUI scales; verify chat and PDA/status text remain readable.
- [ ] Verify Field Researcher, Systems Engineer, and Facility Security use a normal wide-arm Steve silhouette and distinct textures.
- [ ] Right-click each NPC and verify role-specific text appears.
- [ ] Verify NPCs persist after save/reload.
- [ ] Verify Rogue Androids do not immediately target the neutral staff solely as Villager prey.
- [ ] Generate all three layouts of the Synthetic Manufacturing Plant and verify research/engineering/logistics/security nodes remain inside intended rooms.
- [ ] Repeat for Matter Refinery, Quantum Relay Station, Android Command Bunker, Fusion Research Complex, and Black Site.
- [ ] Confirm added crates use MO facility loot and still contain the facility research reward path.
- [ ] Confirm no population piece obstructs a required entrance, corridor, ladder, secure gate, machine interaction face, or two-block player path.
- [ ] Confirm Black Site contains no friendly staff while retaining Android/security encounters.
- [ ] Save/reload near generated facilities and verify structure pieces/NPCs remain valid.
- [ ] Stress-generate several facilities across chunk boundaries and verify no world-generation hang or cross-chunk synchronous stamping regression.
