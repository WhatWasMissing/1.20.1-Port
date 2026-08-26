# Port Status

## M1 - Registry and asset scaffold - VERIFIED

The M1 foundation passed build, client, registry, texture, placement, persistence, runtime-log and dedicated-server testing on Forge 47.4.10 / Minecraft 1.20.1 / Java 17.

Preserved inventory:

- 75 blocks
- 72 block items
- 98 standalone items
- 57 sounds

## M2 - Machine foundation and Matter Decomposer - CURRENT

### Implemented

- `decomposer` upgraded from placeholder `Block` to a facing/active `BaseEntityBlock`.
- `DecomposerBlockEntity` with server ticking and NBT persistence.
- BlockEntityType registration.
- MenuType registration and client screen.
- Forge `ITEM_HANDLER` capability.
- Forge `ENERGY` capability.
- Custom Matter capability registered through `RegisterCapabilitiesEvent`.
- Reusable machine energy and matter storage classes.
- Matter output attempt every 32 ticks for future adjacent Matter-capable machines.
- Legacy Decomposer constants and logarithmic processing/energy formulas.
- 0.5% legacy decomposition failure path.
- Matter Dust now stores the failed matter amount in NBT rather than removed 1.12 metadata.
- Functional Creative Battery with infinite extractable Forge Energy.
- Seed matter-value registry using representative Legacy Edition values plus common Minecraft item tags.
- GUI synchronization for progress, energy, matter, input matter value and FE/t.
- Shift-click routing for matter inputs and energy items.
- M2 build/runtime/server verification scripts.

### Deliberately deferred

- Automatic matter-value derivation from modern crafting/smelting recipes.
- Upgrade slots/effects.
- Machine side-configuration UI.
- Machine looping sound and old animated GUI styling.
- Matter containers/pipes/network graph.
- Replicator, Analyzer, Recycler, Pattern Storage and Pattern Monitor functionality.
- Remaining machines and all non-machine gameplay systems.

## M3 candidate

After M2 is verified, extend the proven machine/capability/menu foundation rather than creating parallel systems. Recommended order:

1. recipe-derived Matter Registry,
2. Matter Replicator,
3. Matter Analyzer,
4. Matter Recycler,
5. Pattern Storage / Pattern Monitor,
6. matter pipes/network transport.
