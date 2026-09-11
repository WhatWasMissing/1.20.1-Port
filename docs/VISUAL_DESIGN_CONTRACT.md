# Matter Overdrive visual design contract

The visual baseline for ordinary machines and facility props is the existing Matter Overdrive asset language, not an external generated concept.

- **Silhouette:** readable block-scale forms; broad industrial base, vertical frame, exposed core or emitter, and one intentional status beacon.
- **Palette:** charcoal/iron body panels, muted blue-grey structural parts, cyan telemetry accents, and restrained warning orange/red.
- **Surface language:** hard-edged voxel geometry with shallow panel breaks; no noisy micro-detail that disappears at Minecraft camera distance.
- **Functional read:** a player should identify the machine role from the front-facing core, screen, tray, or emitter before reading its name.
- **Technical target:** JSON/block models remain within a 16x16x18-ish block silhouette, use existing project textures where possible, and keep pivots/orientation compatible with blockstate facing variants.
- **Validation:** inspect model JSON and texture dimensions locally, build the resource pack, boot a dedicated server, and verify no missing-texture/resource-load errors. A Blockbench MCP screenshot pass is required when that connector is available; it is not currently callable in this environment.
