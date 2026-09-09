# `PORT_INVENTORY.json` scope

`PORT_INVENTORY.json` is a **historical legacy-parity inventory** captured during an early porting milestone. It is not the source of truth for the active 1.20.1 registry.

Use the current Java registries (`ModBlocks`, `ModItems`, entity/structure registries, and their integration code) to determine what is active. Use the inventory only to answer historical parity questions or to identify legacy content that may deserve a deliberate modern replacement.

## Retired content

The **Star Map is retired** from the active port. Its presence in this historical inventory, old localization strings, old handoffs, or archaeology documentation is not a requirement to restore it. It must remain absent from active registration, GuideME, world generation, and current testing requirements unless the project owner explicitly reverses that decision in the future.

The whole-port consistency audit enforces this distinction so an automated parity pass cannot accidentally convert historical inventory entries into active content.
